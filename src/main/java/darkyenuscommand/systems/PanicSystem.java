package darkyenuscommand.systems;

import darkyenuscommand.Plugin;
import darkyenuscommand.command.Cmd;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

/**
 * Implements locking of players when /panic is started.
 */
public final class PanicSystem implements Listener {

	public static final String METADATA_KEY_LOCKED = "locked";

	private final org.bukkit.plugin.Plugin plugin;

	public PanicSystem(org.bukkit.plugin.Plugin plugin) {
		this.plugin = plugin;
	}

	private void onPanicCommand (@NotNull CommandSender sender, @NotNull Collection<Player> players, int minutes) {
		if (players.size() == 0) {
			sender.sendMessage(ChatColor.RED + "Nobody to lock down");
		} else {
			if (minutes <= 0) {
				sender.sendMessage(ChatColor.RED + "Invalid amount of time, setting to 3.");
				minutes = 3;
			}
			long toExpireAt = System.currentTimeMillis() + minutes * 60000;
			for (Player playerToLock : players) {
				playerToLock.setMetadata(METADATA_KEY_LOCKED, new ExpiringMetadataValue(toExpireAt));
				playerToLock.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "You have been locked for " + minutes
						+ " minutes. Please wait until an administrator unlocks you.");
			}
			sender.sendMessage(ChatColor.BLUE.toString() + players.size() + " players locked for " + minutes
					+ " minutes. Use /depanic to unlock them all.");
		}
	}

	@Cmd
	public void panic(@NotNull CommandSender sender, @Cmd.UseDefault int minutes) {
		if (minutes == 0) {
			minutes = 3;
		}

		if (sender instanceof Player) {
			List<Entity> nearbyEntities = ((Player) sender).getNearbyEntities(100, 100, 100);
			List<Player> playersToPanic = new ArrayList<>();
			for (Entity nearbyEntity : nearbyEntities) {
				if (nearbyEntity instanceof Player && !nearbyEntity.equals(sender)) {
					Player panicking = (Player) nearbyEntity;
					if (!panicking.hasPermission("darkyenuscommand.donotpanic")) {
						playersToPanic.add(panicking);
					}
				}
			}
			onPanicCommand(sender, playersToPanic, minutes);
		} else {
			final Collection<? extends Player> players = getServer().getOnlinePlayers();
			List<Player> playersToPanic = new ArrayList<>();
			for (Player player : players) {
				if (!player.hasPermission("darkyenuscommand.donotpanic")) {
					playersToPanic.add(player);
				}
			}
			onPanicCommand(sender, playersToPanic, minutes);
		}
	}

	@Cmd
	public void globalPanic(@NotNull CommandSender sender, @Cmd.UseDefault int minutes) {
		if (minutes == 0) {
			minutes = 5;
		}

		final Collection<? extends Player> players = getServer().getOnlinePlayers();
		List<Player> playersToPanic = new ArrayList<>();
		for (Player player : players) {
			if (!player.hasPermission("darkyenuscommand.donotpanic") && !player.equals(sender)) {
				playersToPanic.add(player);
			}
		}
		onPanicCommand(sender, playersToPanic, minutes);
	}

	@Cmd
	public void depanic(@NotNull CommandSender sender) {
		int dePanicked = 0;
		for (Player toDePanicPlayer : getServer().getOnlinePlayers()) {
			if (toDePanicPlayer != null) {
				if (!toDePanicPlayer.getMetadata(METADATA_KEY_LOCKED).isEmpty()) {
					toDePanicPlayer.removeMetadata(METADATA_KEY_LOCKED, plugin);
					dePanicked++;
					toDePanicPlayer.sendMessage(ChatColor.BLUE + "You have been unlocked.");
				}
			}
		}
		sender.sendMessage(ChatColor.GREEN.toString() + dePanicked + " players unlocked.");
	}

	private boolean isLocked (@NotNull Player player) {
		if (player.hasMetadata(METADATA_KEY_LOCKED)) {
			List<MetadataValue> values = player.getMetadata(METADATA_KEY_LOCKED);
			for (MetadataValue value : values) {
				if (value.asBoolean()) {
					return true;
				} else if (value.getOwningPlugin() instanceof Plugin) {
					player.removeMetadata(METADATA_KEY_LOCKED, plugin);
				}
			}
			return false;
		} else {
			return false;
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull PlayerMoveEvent ev) {
		if (isLocked(ev.getPlayer())) {
			final Location to = ev.getTo();
			if (to == null) {
				ev.setCancelled(true);
				return;
			}
			final Location from = ev.getFrom();
			if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ())
				ev.setTo(from);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull BlockBreakEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull EntityPickupItemEvent ev) {
		final LivingEntity entity = ev.getEntity();
		if (entity instanceof Player && isLocked((Player) entity)) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull PlayerDropItemEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull InventoryClickEvent ev) {
		if (isLocked((Player)ev.getWhoClicked())) {
			ev.setResult(Event.Result.DENY);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player && isLocked((Player)ev.getEntity())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull PlayerInteractEntityEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (@NotNull PlayerInteractEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setUseItemInHand(Event.Result.DENY);
			try {
				ev.getPlayer().updateInventory();
			} catch (Exception ignored) {}
		}
	}

	private class ExpiringMetadataValue implements MetadataValue {

		private long toExpireAtMillis;

		private ExpiringMetadataValue (long toExpireAtMillis) {
			this.toExpireAtMillis = toExpireAtMillis;
		}

		@Override
		public Object value () {
			return asBoolean();
		}

		@Override
		public int asInt () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public float asFloat () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public double asDouble () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public long asLong () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public short asShort () {
			return (short)(asBoolean() ? 1 : 0);
		}

		@Override
		public byte asByte () {
			return (byte)(asBoolean() ? 1 : 0);
		}

		@Override
		public boolean asBoolean () {
			return System.currentTimeMillis() < toExpireAtMillis;
		}

		@NotNull
		@Override
		public String asString () {
			return Boolean.toString(asBoolean());
		}

		@Override
		public org.bukkit.plugin.Plugin getOwningPlugin () {
			return plugin;
		}

		@Override
		public void invalidate () {
		}
	}
}
