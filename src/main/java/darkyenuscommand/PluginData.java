
package darkyenuscommand;

import com.esotericsoftware.jsonbeans.*;
import darkyenuscommand.util.ComplexKeyMap;
import darkyenuscommand.util.StringMap;
import darkyenuscommand.util.TextProcessingInput;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public final class PluginData {

	public ArrayList<String> rules = new ArrayList<>();
	public ArrayList<String> reports = new ArrayList<>();
	public StringMap<Location> warps = new StringMap<>();
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
		return formatPage(page, null);
	}

	/**
	 * @param signStop null or int[1] that serves as an "out" parameter.
	 *                    Will contain the translated first location of "#~#" in resulting page or -1 if this substring
	 *                    does not appear anywhere in the page.
	 */
	public static String formatPage(String page, int[] signStop) {
		if (signStop != null) {
			assert signStop.length == 1 : "Length of sign stop must be exactly 1";
			signStop[0] = -1;
		}
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
					final int length = sb.length();
					if (signStop != null && signStop[0] == length) {
						signStop[0]--;
					}
					sb.setLength(length - 1);
                }
                sb.append('\n');
            } else if(c == '#') {
				if(in.peekEqualsIgnoreCaseAndPop("#")) {
                    sb.append(ChatColor.RESET.toString());
					mode = MODE_NORMAL;
                } else {
                    for (ChatColor color : ChatColor.values()) {
                        if(in.peekEqualsIgnoreCaseAndPop(color.asBungee().getName()+"#") || in.peekEqualsIgnoreCaseAndPop(color.getChar()+"#")) {
                            sb.append(color.toString());
                            if(color == ChatColor.RESET) {
                                mode = MODE_NORMAL;
                            }
                            continue charWhile;
                        }
                    }
					if (in.peekEqualsIgnoreCaseAndPop("~#")) {
                    	if (signStop != null && signStop[0] == -1) {
                    		signStop[0] = sb.length();
						}
					} else if(in.peekEqualsIgnoreCaseAndPop("full_width#") || in.peekEqualsIgnoreCaseAndPop("fw#")) {
                        mode = MODE_FULL_WIDTH;
                    } else if(in.peekEqualsIgnoreCaseAndPop("runic#") || in.peekEqualsIgnoreCaseAndPop("rc#")) {
                        mode = MODE_RUNIC;
                    } else {
                        sb.append('#');
                    }
                }
            } else {
				int cc = c;
				if(cc == '\\') {
					if(in.peekEqualsIgnoreCaseAndPop("#")){
						cc = '#';
					}
				}
				final int printC;
				switch (mode) {
                    case MODE_NORMAL:
                        printC = cc;
                        break;
                    case MODE_FULL_WIDTH:
                        if(cc >= 0x21 && cc <= 0x7E) {
                            printC = (cc - 0x21) + 0xFF01;
                        } else {
                            printC = cc;
                        }
                        break;
                    case MODE_RUNIC:
                        //TODO Faux-mapping
                        if(cc >= 'a' && cc <= 'z') {
                            printC = (cc - 'a') + 0x16A0;
                        } else if(cc >= 'A' && cc <= 'Z') {
                            printC = (cc - 'A') + 0x16A0 + ('z' - 'a');
                        } else if(cc >= '0' && cc <= '9') {
                            printC = (cc - '0') + 0x16A0 + ('z' - 'a') + ('Z' - 'A');
                        } else {
                            printC = cc;
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

		/** @return Fragment to display on the sign */
		public String init(Player owner, List<String> pages) {
			String signFragment = "";
			this.owner = owner.getUniqueId();
			this.pages = new String[pages.size()];
			for (int i = 0; i < this.pages.length; i++) {
				if (i == 0) {
					final int[] stopLocation = {-1};
					final String frontPage = formatPage(pages.get(i), stopLocation);
					this.pages[i] = frontPage;
					if (stopLocation[0] == -1) {
						signFragment = frontPage;
					} else {
						signFragment = frontPage.substring(0, stopLocation[0]);
					}
				} else {
					this.pages[i] = formatPage(pages.get(i));
				}
			}
			this.freeForAllAfter = 0;
			return signFragment;
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
			return displayItemCache = BookUtil.createBookForDisplay(pages);
		}

	}
}
