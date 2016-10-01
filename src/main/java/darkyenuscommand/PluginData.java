
package darkyenuscommand;

import com.esotericsoftware.jsonbeans.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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

	public boolean saddleRecipe = false;
	public boolean recordRecipe = false;
	public boolean horseArmorRecipe = false;
	public boolean nameTagRecipe = false;
	public boolean blockDropFix = false;
	public boolean chatFormat = false;
	public boolean chatFormatColorShuffle = false;
	public boolean bonemealGrassFix = false;
	public boolean fireFix = false;

	private static final Json JSON = new Json(OutputType.javascript);
	private static final File PATH_HOME = new File("plugins/");
	private static final File PLUGIN_DATA_FILE = new File(PATH_HOME, "DarkyenusCommandData.json");

	static {
		JSON.setSerializer(Location.class, new JsonSerializer<Location>() {
			@Override
			public void write(Json json, Location object, Class knownType) {
				json.writeObjectStart();
				json.writeValue("world", object.getWorld().getName());
				json.writeValue("x", object.getX());
				json.writeValue("y", object.getY());
				json.writeValue("z", object.getZ());
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
				return new Location(Bukkit.getWorld(world), x + 0.5f, y, z + 0.5f, yaw, pitch);
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
}
