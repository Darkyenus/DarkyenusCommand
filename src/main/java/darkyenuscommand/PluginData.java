package darkyenuscommand;

import com.google.gson.Gson;
import org.bukkit.Location;
import org.bukkit.Server;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class PluginData {

    public List<String> rules = new ArrayList<>();
    public List<String> reports = new ArrayList<>();
    public Map<String, SerializedLocation> warps = new LinkedHashMap<>();

    public boolean saddleRecipe = false;
    public boolean recordRecipe = false;
    public boolean horseArmorRecipe = false;
    public boolean nameTagRecipe = false;
    public boolean blockDropFix = false;
    public boolean chatFormat = false;
    public boolean chatFormatColorShuffle = false;
    public boolean bonemealGrassFix = false;
    public boolean fireFix = false;

    private static final File PATH_HOME = new File("plugins/");
    private static final File PLUGIN_DATA_FILE = new File(PATH_HOME, "DarkyenusCommandData.json");

    public static PluginData load() {
        try {
            return new Gson().fromJson(new FileReader(PLUGIN_DATA_FILE), PluginData.class);
        } catch (FileNotFoundException e) {
            final PluginData pluginData = new PluginData();
            save(pluginData);
            return pluginData;
        }
    }

    public static boolean save(PluginData data){
        try {
            final FileWriter output = new FileWriter(PLUGIN_DATA_FILE, false);
            new Gson().toJson(data, PluginData.class, output);
            output.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static final class SerializedLocation {
        public String world;
        public double x;
        public double y;
        public double z;
        public float pitch;
        public float yaw;

        public SerializedLocation() {
        }

        public SerializedLocation(String world, double x, double y, double z, float pitch, float yaw) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public static SerializedLocation serialize(Location location){
            return new SerializedLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        }

        public Location deserialize(Server server){
            return new Location(server.getWorld(world), x, y, z ,yaw, pitch);
        }
    }
}
