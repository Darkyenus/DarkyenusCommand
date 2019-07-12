
package darkyenuscommand;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** @author Darkyen */
final class PluginListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void commandSigns (PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if ((ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_BLOCK)// Is clicking block
			&& (Tag.SIGNS.isTagged(ev.getClickedBlock().getType()))// Is the block a sign
			&& ev.getPlayer().hasPermission("darkyenuscommand.sign")// Has permission?
			&& !ev.isCancelled() && !ev.getPlayer().isSneaking()) {// Is not already cancelled && classic sneak check
			Sign sign = (Sign)ev.getClickedBlock().getState();
			StringBuilder signTextBuilder = new StringBuilder();
			for (String line : sign.getLines()) {
				signTextBuilder.append(line);
			}
			String signText = signTextBuilder.toString();
			if (signText.startsWith("//")) {
				// Cancel event
				ev.setCancelled(true);
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setUseInteractedBlock(Event.Result.DENY);
				try {
					BlockState state = ev.getClickedBlock().getState();
					if (state != null) {
						state.update();
					}
				} catch (NullPointerException ignored) {
				}
				// Perform command and, if success, make fire dance
				if (ev.getPlayer().performCommand(signText.trim().substring(2))) {
					ev.getPlayer().getWorld().playEffect(ev.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), Effect.SMOKE, 4);
				}
			}
		}

		if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.getPlayer().hasPermission("darkyenuscommand.infostick")
			&& ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
			// Infostick time!
			Block clicked = ev.getClickedBlock();
			Player player = ev.getPlayer();
			player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Infostick report on block at X: "
				+ clicked.getLocation().getBlockX() + " Y: " + clicked.getLocation().getBlockY() + " Z: "
				+ clicked.getLocation().getBlockZ());
			player.sendMessage(ChatColor.BLUE.toString() + " Material: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clicked.getBlockData().getAsString());
			player.sendMessage(ChatColor.BLUE.toString() + " Biome: " + ChatColor.WHITE + ChatColor.ITALIC + clicked.getBiome()
				+ " (Temperature: " + clicked.getTemperature() + " Humidity:" + clicked.getHumidity() + ")");
			player.sendMessage(ChatColor.BLUE.toString() + " Redstone power: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clicked.getBlockPower() + " (D" + clicked.getBlockPower(BlockFace.DOWN) + " U"
				+ clicked.getBlockPower(BlockFace.UP) + " N" + clicked.getBlockPower(BlockFace.NORTH) + " S"
				+ clicked.getBlockPower(BlockFace.SOUTH) + " W" + clicked.getBlockPower(BlockFace.WEST) + " E"
				+ clicked.getBlockPower(BlockFace.EAST) + ")");
			player.sendMessage(ChatColor.BLUE.toString() + " Light: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clicked.getLightLevel() + " (Sky: " + clicked.getLightFromSky() + " Blocks: " + clicked.getLightFromBlocks()
				+ ")");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void infoStickEntity (PlayerInteractEntityEvent ev) {
		if (!ev.getPlayer().hasPermission("darkyenuscommand.infostick")
				|| ev.getPlayer().getInventory().getItemInMainHand().getType() != Material.STICK) {
			return;
		}
		Entity clicked = ev.getRightClicked();
		Player player = ev.getPlayer();
		player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Infostick report on entity " + clicked.getEntityId()
			+ " at X: " + clicked.getLocation().getX() + " Y: " + clicked.getLocation().getX() + " Z: "
			+ clicked.getLocation().getX());
		player.sendMessage(ChatColor.BLUE.toString() + " EntityType: " + ChatColor.WHITE + ChatColor.ITALIC
			+ clicked.getType() + " (Class: " + clicked.getClass().getSimpleName() + ")");
		player.sendMessage(ChatColor.BLUE.toString() + " Fire: " + ChatColor.WHITE + ChatColor.ITALIC
			+ clicked.getFireTicks() + "/" + clicked.getMaxFireTicks());
		player.sendMessage(ChatColor.BLUE.toString() + " Dead: " + ChatColor.WHITE + ChatColor.ITALIC + clicked.isDead());
		if (clicked.isInsideVehicle()) {
			player.sendMessage(ChatColor.BLUE.toString() + " Inside vehicle");
		}
		for (Entity passenger : clicked.getPassengers()) {
			player.sendMessage(ChatColor.BLUE.toString() + " Passenger: "+ ChatColor.WHITE + ChatColor.ITALIC + passenger);
		}
		if (clicked instanceof LivingEntity) {
			LivingEntity clickedLE = (LivingEntity)clicked;
			player.sendMessage(ChatColor.BLUE.toString() + " Health: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clickedLE.getHealth() + "/" + clickedLE.getAttribute(Attribute.GENERIC_MAX_HEALTH));
			player.sendMessage(ChatColor.BLUE.toString() + " Air: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clickedLE.getRemainingAir() + "/" + clickedLE.getMaximumAir());
			player.sendMessage(ChatColor.BLUE.toString() + " Can pickup items: " + ChatColor.WHITE + ChatColor.ITALIC
				+ clickedLE.getCanPickupItems());
			if (clicked instanceof Player) {
				player
					.sendMessage(ChatColor.BLUE + "For more info about player, use \"/playerinfo " + clicked.getName() + "\"");
			}
		}
		if (clicked instanceof Creature) {
			Creature creature = (Creature)clicked;
			player.sendMessage(ChatColor.BLUE.toString()
				+ " Target: "
				+ ChatColor.WHITE
				+ ChatColor.ITALIC
				+ (creature.getTarget() != null ? creature.getTarget().getType() + " ID: " + creature.getTarget().getEntityId()
					: "None"));
		}
	}

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
		if (held == null || held.getType() == Material.AIR) {
			event.setDamage(Integer.MAX_VALUE);
			damager.playEffect(event.getEntity().getLocation(), Effect.EXTINGUISH, null);
		}
	}
}
