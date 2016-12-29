package darkyenuscommand.util;

import com.darkyen.nbtapi.reflection.ReflectionClass;
import com.darkyen.nbtapi.reflection.ReflectionMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.logging.Level;

/**
 *
 */
public final class UnsafeUtil {

    private static final Reflection Reflection;
    static {
        Reflection ref = null;
        try {
            ref = new Reflection();
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Reflection tool creation failed", ex);
        }
        Reflection = ref;
    }

    public static boolean isValidItemMaterial(Material material) {
        return Reflection == null || Reflection.CraftMagicNumbers_getItem.invokeStatic(material) != null;
    }

    private static final class Reflection {
        private final ReflectionClass CraftMagicNumbers = new ReflectionClass("org.bukkit.craftbukkit.{BukkitVersion}.util.CraftMagicNumbers");
        private final ReflectionMethod<Object> CraftMagicNumbers_getItem = CraftMagicNumbers.method("getItem", Material.class);
    }
}
