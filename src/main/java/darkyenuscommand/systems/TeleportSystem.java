package darkyenuscommand.systems;

import darkyenuscommand.Plugin;
import darkyenuscommand.command.Cmd;
import darkyenuscommand.util.StackMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

/**
 *
 */
public final class TeleportSystem {

	private static final Logger LOG = Plugin.logger(TeleportSystem.class);

	private final StackMap<UUID, Location> recalls = new StackMap<>(32);//32 recalls stored

	@Cmd
	public void teleportHere(@NotNull Player sender, @NotNull Player playerToTeleport) {
		teleportPlayer(playerToTeleport, sender.getLocation(), true);
	}

	@Cmd
	public void teleportTo(@NotNull Player sender, @NotNull Player playerToTeleportTo) {
		teleportPlayer(sender, playerToTeleportTo.getLocation(), true);
	}

	@Cmd
	public void teleportHereAll(@NotNull Player sender) {
		for (Player player : getServer().getOnlinePlayers()) {
			if (player != sender) teleportPlayer(player, sender.getLocation(), true);
		}
	}

	@Cmd(value = "teleport", order = 0, description = "relative teleport")
	public void teleportRelative(@NotNull CommandSender sender, @Cmd.Prefix("+") double x, @Cmd.Prefix("+") double y, @Cmd.Prefix("+") double z, @Cmd.UseImplicit Player player) {
		final Location to = player.getLocation().clone();
		to.setX(to.getX() + x);
		to.setY(to.getY() + y);
		to.setZ(to.getZ() + z);
		teleportPlayer(player, to, true);
	}

	@Cmd(value = "teleport", order = 1, description = "absolute teleport")
	public void teleportAbsolute(@NotNull CommandSender sender, double x, double y, double z, @Cmd.UseImplicit Player player) {
		final Location to = player.getLocation().clone();
		to.setX(x);
		to.setY(y);
		to.setZ(z);
		teleportPlayer(player, to, true);
	}

	enum TeleportDirection {
		TOP,
		UP,
		DOWN,
		BOTTOM
	}

	@Cmd(value = "teleport", order = 2, description = "directional teleport")
	public void teleportDirection(@NotNull CommandSender sender, TeleportDirection direction, @Cmd.UseImplicit Player player) {
		int x = player.getLocation().getBlockX();
		int z = player.getLocation().getBlockZ();
		int nowY = player.getLocation().getBlockY();
		World world = player.getWorld();
		int toY = -1;
		switch (direction) {
			case TOP:
				for (int y = world.getHighestBlockYAt(x, z) + 1; y > nowY; y--) {
					if (isGoodTeleportLocation(world, x, y, z)) {
						toY = y;
						break;
					}
				}
				break;
			case UP:
				for (int y = nowY + 1; y < world.getMaxHeight(); y++) {
					if (isGoodTeleportLocation(world, x, y, z)) {
						toY = y;
						break;
					}
				}
				break;
			case DOWN:
				for (int y = nowY - 1; y > 0; y--) {
					if (isGoodTeleportLocation(world, x, y, z)) {
						toY = y;
						break;
					}
				}
				break;
			case BOTTOM:
				for (int y = 1; y < nowY; y++) {
					if (isGoodTeleportLocation(world, x, y, z)) {
						toY = y;
						break;
					}
				}
				break;
		}

		if (toY != -1) {
			Location teleportTo = player.getLocation().clone();
			teleportTo.setY(toY);
			teleportPlayer(player, teleportTo, true);
		} else {
			sender.sendMessage(ChatColor.BLUE + "There's no better place.");
		}
	}

	@Cmd
	public void recall(@NotNull CommandSender sender, @Cmd.UseImplicit Player playerToRecall) {
		final Location recallTo = recalls.popOrNull(playerToRecall.getUniqueId());
		if (recallTo == null) {
			sender.sendMessage(ChatColor.RED + "Nowhere to recall");
		} else {
			teleportPlayer(playerToRecall, recallTo, false);
		}
	}

	@Cmd
	public void setSpawn(@NotNull Player sender) {
		final Location location = sender.getLocation();
		if (sender.getWorld()
				.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
			sender.sendMessage(ChatColor.GREEN + "Spawn set.");
		} else {
			sender.sendMessage(ChatColor.RED + "Spawn couldn't be set.");
		}
	}

	@Cmd
	public void spawn(@NotNull Player sender) {
		teleportPlayer(sender, sender.getWorld().getSpawnLocation(), true);

		sender.sendMessage(ChatColor.GREEN + "Teleported to spawn");
	}


	public void teleportPlayer(Player who, Location to, boolean saveRecall) {
		if (saveRecall) {
			recalls.push(who.getUniqueId(), who.getLocation());
		}
		LOG.info(who.getName() + " teleported from " + formatLocation(who.getLocation()) + " to " + formatLocation(to));

		if (who.isInsideVehicle() && who.getVehicle() instanceof Animals && !who.getVehicle().isDead()) {
			final Entity vehicle = who.getVehicle();
			//Teleport player first, to load the chunk
			who.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
			//Then teleport the vehicle
			vehicle.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
			//Then place the player back on the vehicle
			vehicle.addPassenger(who);
		} else {
			who.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}
	}

	private static String formatLocation (Location location) {
		final World world = location.getWorld();
		return (world == null ? "<unknown-world>" : world.getName()) + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
	}


	private static boolean isGoodTeleportLocation (World world, int x, int y, int z) {
		return world.getBlockAt(x, y - 1, z).getType().isSolid()
				&& (y == world.getMaxHeight() || (!world.getBlockAt(x, y, z).getType().isSolid() && !world.getBlockAt(x, y + 1, z)
				.getType().isSolid()));
	}


}
