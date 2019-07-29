
package darkyenuscommand;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonException;
import com.esotericsoftware.jsonbeans.JsonSerializer;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.OutputType;
import darkyenuscommand.util.ComplexKeyMap;
import darkyenuscommand.util.StringMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@SuppressWarnings("unchecked")
public final class PluginData {

	private static final Logger LOG = Plugin.logger(PluginData.class);

	public ArrayList<String> rules = new ArrayList<>();
	public ArrayList<String> reports = new ArrayList<>();
	public StringMap<Location> warps = new StringMap<>();

	private static final String PLUGIN_FILE_NAME = "DarkyenusCommandData.json";
	private transient File loadedFrom = null;

	private static final Json JSON = new Json(OutputType.json);

	static {
		JSON.setSerializer(Location.class, new JsonSerializer<Location>() {
			@Override
			public void write(Json json, Location object, Class knownType) {
				json.writeObjectStart();
				final World world = object.getWorld();
				json.writeValue("world", world == null ? null : world.getName());
				json.writeValue("x", object.getBlockX());
				json.writeValue("y", object.getBlockY());
				json.writeValue("z", object.getBlockZ());
				json.writeValue("yaw", Math.round(object.getYaw()));
				json.writeValue("pitch", Math.round(object.getPitch()));
				json.writeObjectEnd();
			}

			@Override
			public Location read(Json json, JsonValue jsonData, Class type) {
				final String worldString = jsonData.getString("world");
				final int x = jsonData.getInt("x");
				final int y = jsonData.getInt("y");
				final int z = jsonData.getInt("z");
				final int yaw = jsonData.getInt("yaw");
				final int pitch = jsonData.getInt("pitch");
				final World world = worldString == null ? Bukkit.getWorlds().get(0) : Bukkit.getWorld(worldString);
				return new Location(world, x, y, z, yaw, pitch);
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
		JSON.setSerializer(ComplexKeyMap.class, (JsonSerializer<ComplexKeyMap>)(JsonSerializer) ComplexKeyMap.SERIALIZER);
		JSON.setUsePrototypes(false);
		JSON.setIgnoreUnknownFields(true);
	}

	public static PluginData load (File pluginFolder) {
		final File pluginFile = new File(pluginFolder, PLUGIN_FILE_NAME);
		try {
			PluginData pluginData = JSON.fromJson(PluginData.class, pluginFile);
			pluginData.loadedFrom = pluginFile;
			return pluginData;
		} catch (JsonException e) {
			if(!e.causedBy(FileNotFoundException.class)) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load PluginData", e);
			}
			if(pluginFile.isFile()) {
				//noinspection ResultOfMethodCallIgnored
				pluginFile.renameTo(new File(pluginFolder, PLUGIN_FILE_NAME+"."+System.currentTimeMillis()+".corrupted"));
			}
			PluginData newPluginData = new PluginData();
			newPluginData.loadedFrom = pluginFile;
			save(newPluginData);
			return newPluginData;
		}
	}

	public static void save (PluginData data) {
		final String prettyPrint = JSON.prettyPrint(data);

		if (data.loadedFrom == null) {
			LOG.log(Level.SEVERE, "Not saving, don't know where");
		} else {
			//noinspection ResultOfMethodCallIgnored
			data.loadedFrom.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(data.loadedFrom)) {
				writer.append(prettyPrint);
				writer.flush();
				return;
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Failed to save", e);
			}
		}

		System.out.println("\n\nUnsaved json:\n");
		System.out.println(prettyPrint);
		System.out.println("\n\n\n");
	}
}
