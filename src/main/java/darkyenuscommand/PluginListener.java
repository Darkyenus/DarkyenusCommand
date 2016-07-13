
package darkyenuscommand;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.MetadataValue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/** @author Darkyen */
final class PluginListener implements Listener {

	private final Plugin plugin;

	PluginListener (Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	@SuppressWarnings("LoggerStringConcat")
	public void kickAndBanCheck (PlayerLoginEvent ev) throws IOException {
		// Kick check
		int kickedMinutes = plugin.kickedMinutes(ev.getPlayer().getUniqueId());
		if (kickedMinutes > 0) {
			String minuteSuffix = "";// To be grammatically correct
			if (kickedMinutes != 1) {
				minuteSuffix = "s";
			}

			ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You have been kicked. Try again in " + kickedMinutes + " minute"
				+ minuteSuffix + ".");
			plugin.getLogger().log(Level.INFO,
				"Kicking " + ev.getPlayer().getName() + " on join, may return in " + kickedMinutes + " minute" + minuteSuffix + ".");
		}
	}

	@EventHandler
	public void informAboutReportsOnJoin (PlayerJoinEvent ev) {
		if (ev.getPlayer().hasPermission("darkyenuscommand.staff")) {
			final List<String> reports = plugin.data.reports;
			if (!reports.isEmpty()) {
				if (reports.size() == 1) {
					ev.getPlayer().sendMessage(
						ChatColor.GRAY + "There is " + ChatColor.RED + "1" + ChatColor.GRAY + " pending report.");
				} else {
					ev.getPlayer().sendMessage(
						ChatColor.GRAY + "There are " + ChatColor.RED + reports.size() + ChatColor.GRAY + " pending reports.");
				}
			}
		}
	}

	@EventHandler
	public void stopTheMutedOnes (AsyncPlayerChatEvent event) {// Not called for commands
		if (plugin.isMuted(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are muted!");
		}
	}

	@EventHandler
	public void commandSigns (PlayerInteractEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setUseItemInHand(Event.Result.DENY);
			try {
				// noinspection deprecation
				ev.getPlayer().updateInventory();
			} catch (Exception ignored) {
			}
		} else {
			if ((ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_BLOCK)// Is clicking block
				&& (ev.getClickedBlock().getType() == Material.WALL_SIGN || ev.getClickedBlock().getType() == Material.SIGN_POST)// Is
// the block a sign
				&& ev.getPlayer().hasPermission("darkyenuscommand.sign")// Has permission?
				&& !ev.isCancelled() && !ev.getPlayer().isSneaking()) {// Is not already cancelled && classic sneak check
				Sign sign = (Sign)ev.getClickedBlock().getState();
				String signText = "";
				for (String line : sign.getLines()) {
					signText = signText + line;
				}
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
					+ clicked.getType().toString() + " (ID: " + clicked.getTypeId() + " Data: " + clicked.getData() + ")");
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
	}

	@EventHandler
	public void infoStickEntity (PlayerInteractEntityEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		} else {
			if (ev.getPlayer().hasPermission("darkyenuscommand.infostick")
				&& ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
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
				player.sendMessage(ChatColor.BLUE.toString() + " Vehicle: " + ChatColor.WHITE + ChatColor.ITALIC + "In: "
					+ clicked.isInsideVehicle() + " Is: " + (clicked.getPassenger() != null));
				if (clicked instanceof LivingEntity) {
					LivingEntity clickedLE = (LivingEntity)clicked;
					player.sendMessage(ChatColor.BLUE.toString() + " Health: " + ChatColor.WHITE + ChatColor.ITALIC
						+ clickedLE.getHealth() + "/" + clickedLE.getMaxHealth());
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
		}
	}

	@EventHandler
	public void saveOfflineInventories (InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof Player) {
			Player holder = (Player)event.getInventory().getHolder();
			if (!holder.isOnline()) {
				holder.saveData();
			}
		}
	}

	@EventHandler
	public void godFist (EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player damager = (Player)event.getDamager();
			if (!damager.getWorld().getPVP() && event.getEntity() instanceof Player) return;
			if (damager.getGameMode() == GameMode.CREATIVE
				&& damager.hasPermission("darkyenuscommand.godfist")
				&& (damager.getItemInHand() == null || damager.getItemInHand().getTypeId() == 0 || damager.getItemInHand()
					.getAmount() == 0)) {
				event.setDamage(Integer.MAX_VALUE);
				damager.playEffect(event.getEntity().getLocation(), Effect.EXTINGUISH, null);
				event.getEntity().setVelocity(
					event
						.getEntity()
						.getVelocity()
						.add(
							event.getEntity().getLocation().toVector().subtract(damager.getLocation().toVector()).normalize()
								.multiply(3)));// Knockback
			}
		}
	}

	// Cancel for locked
	private boolean isLocked (Player player) {
		if (player.hasMetadata("locked")) {
			List<MetadataValue> values = player.getMetadata("locked");
			for (MetadataValue value : values) {
				if (value.asBoolean()) {
					return true;
				} else if (value.getOwningPlugin() instanceof Plugin) {
					player.removeMetadata("locked", plugin);
				}
			}
			return false;
		} else {
			return false;
		}
	}

	@EventHandler
	public void checkAction (PlayerMoveEvent ev) {
		if (isLocked(ev.getPlayer())) {
			if (ev.getFrom().getBlockX() != ev.getTo().getBlockX() || ev.getFrom().getBlockZ() != ev.getTo().getBlockZ())
				ev.setTo(ev.getFrom());
		}
	}

	@EventHandler
	public void checkAction (BlockBreakEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void checkAction (PlayerPickupItemEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void checkAction (PlayerDropItemEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void checkAction (InventoryClickEvent ev) {
		if (isLocked((Player)ev.getWhoClicked())) {
			ev.setResult(Event.Result.DENY);
		}
	}

	@EventHandler
	public void checkAction (EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player && isLocked((Player)ev.getEntity())) {
			ev.setCancelled(true);
		}
	}
}
