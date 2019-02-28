package darkyenuscommand.systems;

import darkyenuscommand.Plugin;
import darkyenuscommand.command.Cmd;
import darkyenuscommand.match.Match;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static darkyenuscommand.systems.PanicSystem.METADATA_KEY_LOCKED;

/**
 *
 */
public final class JailSystem {

	private final Plugin plugin;
	private final WarpSystem warpSystem;
	private final TeleportSystem teleportSystem;

	private final HashMap<UUID, Location> preJailLocations = new HashMap<>();

	public JailSystem(Plugin plugin, WarpSystem warpSystem, TeleportSystem teleportSystem) {
		this.plugin = plugin;
		this.warpSystem = warpSystem;
		this.teleportSystem = teleportSystem;
	}

	@Cmd
	public void jail(CommandSender sender, Player playerToArrest, @Cmd.UseDefault String warp) {
		final Match<String> matchingWarps = warp != null ? warpSystem.matchWarps(warp) : null;

		if (matchingWarps != null && matchingWarps.success()) {
			preJailLocations.put(playerToArrest.getUniqueId(), playerToArrest.getLocation());
			teleportSystem.teleportPlayer(playerToArrest, warpSystem.getWarp(matchingWarps.successResult()), true);
			playerToArrest.setMetadata(METADATA_KEY_LOCKED, new FixedMetadataValue(plugin, true));
			if (playerToArrest.isOnline()) {
				playerToArrest.sendMessage(ChatColor.BLUE + "You have been jailed.");
			} else {
				playerToArrest.saveData();
			}
			sender.sendMessage(ChatColor.GREEN.toString() + playerToArrest.getName() + " jailed in " + warp);
		} else {
			String[] availableWarps = warpSystem.matchWarps("jail_*").asArray(String.class);
			if (availableWarps.length == 0) {
				sender.sendMessage(ChatColor.RED
						.toString() + "Could not jail, no jails found or specified.");
			} else {
				int jailID = new Random().nextInt(availableWarps.length);
				preJailLocations.put(playerToArrest.getUniqueId(), playerToArrest.getLocation());
				teleportSystem.teleportPlayer(playerToArrest, warpSystem.getWarp(availableWarps[jailID]), true);
				playerToArrest.setMetadata(METADATA_KEY_LOCKED, new FixedMetadataValue(plugin, true));
				if (playerToArrest.isOnline()) {
					playerToArrest.sendMessage(ChatColor.BLUE + "You have been jailed.");
				} else {
					playerToArrest.saveData();
				}
				sender.sendMessage(ChatColor.GREEN.toString() + playerToArrest
						.getName() + " jailed in " + availableWarps[jailID]);
			}
		}
	}

	@Cmd
	public void unjail(CommandSender sender, Player player) {
		if (player.hasMetadata(METADATA_KEY_LOCKED)) {
			player.removeMetadata(METADATA_KEY_LOCKED, plugin);
			final Location preJailLocation = preJailLocations.remove(player.getUniqueId());
			if (preJailLocation == null) {
				teleportSystem.teleportPlayer(player, player.getWorld().getSpawnLocation(), false);
			} else {
				teleportSystem.teleportPlayer(player, preJailLocation, false);
			}
			player.sendMessage(ChatColor.BLUE + "You have been set free from jail.");
		} else {
			sender.sendMessage(ChatColor.RED + "Player not jailed.");
		}
	}

}
