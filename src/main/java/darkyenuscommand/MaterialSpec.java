package darkyenuscommand;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 */
public class MaterialSpec {
    public final Material material;
    public final int data;
    public final boolean hasData;

    public MaterialSpec(Material material, int data) {
        this.material = material;
        this.data = data;
        this.hasData = true;
    }

    public MaterialSpec(Material material) {
        this.material = material;
        this.data = 0;
        this.hasData = false;
    }

    public ItemStack toItemStack(int amount) {
        if(hasData) {
            return new ItemStack(material, amount, (short)data);
        } else {
            return new ItemStack(material, amount);
        }
    }
}
