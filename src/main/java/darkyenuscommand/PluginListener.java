
package darkyenuscommand;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/** @author Darkyen */
final class PluginListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void godFist (EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		final Player damager = (Player)event.getDamager();
		if (event.getEntity() instanceof Player
				|| damager.getGameMode() != GameMode.CREATIVE
				|| !damager.hasPermission("darkyenuscommand.godfist")) return;
		final ItemStack held = damager.getInventory().getItemInMainHand();
		if (held.getType() == Material.AIR) {
			event.setDamage(Integer.MAX_VALUE);
			damager.playEffect(event.getEntity().getLocation(), Effect.EXTINGUISH, null);
		}
	}
}
