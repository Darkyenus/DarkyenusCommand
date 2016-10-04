
package darkyenuscommand;

import com.esotericsoftware.jsonbeans.*;
import darkyenuscommand.util.ComplexKeyMap;
import darkyenuscommand.util.TextProcessingInput;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 *
 */
public final class PluginData {

	public ArrayList<String> rules = new ArrayList<>();
	public ArrayList<String> reports = new ArrayList<>();
	public LinkedHashMap<String, Location> warps = new LinkedHashMap<>();
	public ComplexKeyMap<Location, NoticeBoard> bookNoticeBoards = new ComplexKeyMap<>(Location.class, NoticeBoard.class);

	public boolean saddleRecipe = false;
	public boolean recordRecipe = false;
	public boolean horseArmorRecipe = false;
	public boolean nameTagRecipe = false;
	public boolean blockDropFix = false;
	public boolean chatFormat = false;
	public boolean chatFormatColorShuffle = false;
	public boolean bonemealGrassFix = false;
	public boolean fireFix = false;
	public boolean bookNoticeBoardsEnabled = false;

	private static final Json JSON = new Json(OutputType.javascript);
	private static final File PATH_HOME = new File("plugins/");
	private static final File PLUGIN_DATA_FILE = new File(PATH_HOME, "DarkyenusCommandData.json");

	static {
		JSON.setSerializer(Location.class, new JsonSerializer<Location>() {
			@Override
			public void write(Json json, Location object, Class knownType) {
				json.writeObjectStart();
				json.writeValue("world", object.getWorld().getName());
				json.writeValue("x", object.getBlockX());
				json.writeValue("y", object.getBlockY());
				json.writeValue("z", object.getBlockZ());
				json.writeValue("yaw", Math.round(object.getYaw()));
				json.writeValue("pitch", Math.round(object.getPitch()));
				json.writeObjectEnd();
			}

			@Override
			public Location read(Json json, JsonValue jsonData, Class type) {
				final String world = jsonData.getString("world");
				final int x = jsonData.getInt("x");
				final int y = jsonData.getInt("y");
				final int z = jsonData.getInt("z");
				final int yaw = jsonData.getInt("yaw");
				final int pitch = jsonData.getInt("pitch");
				return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
			}
		});
		JSON.setSerializer(UUID.class, new JsonSerializer<UUID>() {
			@Override
			public void write(Json json, UUID object, Class knownType) {
				json.writeValue(object.toString());
			}

			@Override
			public UUID read(Json json, JsonValue jsonData, Class type) {
				try {
					return UUID.fromString(jsonData.asString());
				} catch (Exception ex) {
					Bukkit.getLogger().log(Level.WARNING, "Failed to deserialize UUID from JsonValue: "+jsonData);
					return null;
				}

			}
		});
		//noinspection unchecked
		JSON.setSerializer(ComplexKeyMap.class, (JsonSerializer<ComplexKeyMap>)(JsonSerializer) ComplexKeyMap.SERIALIZER);
		JSON.setUsePrototypes(false);
		JSON.setIgnoreUnknownFields(true);
	}

	public static PluginData load () {
		try {
			return JSON.fromJson(PluginData.class, PLUGIN_DATA_FILE);
		} catch (JsonException e) {
			if(!e.causedBy(FileNotFoundException.class)) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load PluginData", e);
			}
			if(PLUGIN_DATA_FILE.isFile()) {
				//noinspection ResultOfMethodCallIgnored
				PLUGIN_DATA_FILE.renameTo(new File(PLUGIN_DATA_FILE.getParentFile(), PLUGIN_DATA_FILE.getName()+"."+System.currentTimeMillis()+".corrupted"));
			}
			final PluginData pluginData = new PluginData();
			save(pluginData);
			return pluginData;
		}
	}

	public static boolean save (PluginData data) {
		//noinspection ResultOfMethodCallIgnored
		PLUGIN_DATA_FILE.getParentFile().mkdirs();
		final String prettyPrint = JSON.prettyPrint(data);
		try(FileWriter writer = new FileWriter(PLUGIN_DATA_FILE)) {
			writer.append(prettyPrint);
			writer.flush();
			return true;
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to save PluginData", e);
			System.out.println("\n\nUnsaved PluginData:\n");
			System.out.println(prettyPrint);
			System.out.println("\n\n\n");
			return false;
		}
	}

	public static String formatPage(String page) {
        final TextProcessingInput in = new TextProcessingInput(page);
        final StringBuilder sb = new StringBuilder();

        final byte MODE_NORMAL = 1;
        final byte MODE_FULL_WIDTH = 2;
        final byte MODE_RUNIC = 3;

        byte mode = MODE_NORMAL;

        charWhile:
        while(true) {
            final int c = in.pop();

            if(c == -1) {
                break;
            } else if(c == ChatColor.COLOR_CHAR) {
                in.pop();
            } else if(c == '\n' || c == '\r') {
                while(sb.length() != 0 && Character.isWhitespace(sb.charAt(sb.length()-1)) && sb.charAt(sb.length()-1) != '\n'){
                    sb.setLength(sb.length() - 1);
                }
                sb.append('\n');
            } else if(c == '#') {
                if(in.peekEqualsIgnoreCaseAndPop("#")) {
                    sb.append('#');
                } else {
                    for (ChatColor color : ChatColor.values()) {
                        if(in.peekEqualsIgnoreCaseAndPop("("+color.asBungee().getName()+")") || in.peekEqualsIgnoreCaseAndPop("("+color.getChar()+")")) {
                            sb.append(color.toString());
                            if(color == ChatColor.RESET) {
                                mode = MODE_NORMAL;
                            }
                            continue charWhile;
                        }
                    }
                    if(in.peekEqualsIgnoreCaseAndPop("(full_width)") || in.peekEqualsIgnoreCaseAndPop("(fw)")) {
                        mode = MODE_FULL_WIDTH;
                    } else if(in.peekEqualsIgnoreCaseAndPop("(runic)") || in.peekEqualsIgnoreCaseAndPop("(rc)")) {
                        mode = MODE_RUNIC;
                    } else {
                        sb.append('#');
                    }
                }
            } else {
                final int printC;
                switch (mode) {
                    case MODE_NORMAL:
                        printC = c;
                        break;
                    case MODE_FULL_WIDTH:
                        if(c >= 0x21 && c <= 0x7E) {
                            printC = (c - 0x21) + 0xFF01;
                        } else {
                            printC = c;
                        }
                        break;
                    case MODE_RUNIC:
                        //TODO Faux-mapping
                        if(c >= 'a' && c <= 'z') {
                            printC = (c - 'a') + 0x16A0;
                        } else if(c >= 'A' && c <= 'Z') {
                            printC = (c - 'A') + 0x16A0 + ('z' - 'a');
                        } else if(c >= '0' && c <= '9') {
                            printC = (c - '0') + 0x16A0 + ('z' - 'a') + ('Z' - 'A');
                        } else {
                            printC = c;
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unknown mode "+mode);
                }
                sb.appendCodePoint(printC);
            }
        }
        return sb.toString();
    }

	public static final class NoticeBoard {
		public UUID owner;
		public String[] pages;
		public long freeForAllAfter;

		private transient ItemStack displayItemCache = null;

		public void init(Player owner, List<String> pages){
			this.owner = owner.getUniqueId();
			this.pages = new String[pages.size()];
			for (int i = 0; i < this.pages.length; i++) {
				this.pages[i] = formatPage(pages.get(i));
			}
			this.freeForAllAfter = 0;
		}

		public boolean isFreeForAll(){
			return freeForAllAfter < System.currentTimeMillis();
		}

		public void neverFreeForAll() {
			freeForAllAfter = Long.MAX_VALUE;
		}

		public void alwaysFreeForAll() {
			freeForAllAfter = 0;
		}

		public void freeForAllAfterDays(int days) {
			freeForAllAfter = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * days;
		}

		public ItemStack getDisplayItem() {
			if(displayItemCache != null) return displayItemCache;
			final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			final BookMeta itemMeta = (BookMeta) book.getItemMeta();
			itemMeta.setDisplayName("Note Sign");
			itemMeta.addPage(pages);
			book.setItemMeta(itemMeta);
			return displayItemCache = book;
		}

	}
}
