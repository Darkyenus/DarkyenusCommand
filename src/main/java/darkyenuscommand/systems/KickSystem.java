package darkyenuscommand.systems;

import darkyenuscommand.Plugin;
import darkyenuscommand.command.Cmd;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class KickSystem implements Listener {

	private static final Logger LOG = Plugin.logger(KickSystem.class);

	private final HashMap<UUID, Long> kickTimer = new HashMap<>();

	@Cmd
	public void kick(@NotNull CommandSender sender, @NotNull Player kickedPlayer, @Cmd.UseDefault @Cmd.Prefix("-") int minutes, @Cmd.UseDefault @Cmd.VarArg String message) {
		if (message == null || message.isEmpty()) {
			message = "You have been kicked.";
		}
		kickedPlayer.kickPlayer(message);

		if (minutes > 0) {
			kickTimer.put(kickedPlayer.getUniqueId(), System.currentTimeMillis() + minutes * 60000L);
			sender.sendMessage(ChatColor.GREEN.toString() + kickedPlayer.getName() + " has been kicked for " + minutes + " minutes");
		} else {
			sender.sendMessage(ChatColor.GREEN.toString() + kickedPlayer.getName() + " has been kicked");
		}
	}

	/** For how many minutes player can't connect
	 *
	 * @param name player's name
	 * @return how many minutes will it take to not kick this player again */
	public int kickedMinutes(@NotNull UUID name) {
		if (kickTimer.containsKey(name)) {
			long value = kickTimer.get(name);
			if (value < System.currentTimeMillis()) {
				kickTimer.remove(name);
				return 0;
			} else {
				int minutesOut = (int)((value - System.currentTimeMillis()) / 60000);
				if (minutesOut == 0) {
					minutesOut = 1;
				}
				return minutesOut;
			}
		} else {
			return 0;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void kickAndBanCheck(@NotNull PlayerLoginEvent ev) {
		// Kick check
		int kickedMinutes = kickedMinutes(ev.getPlayer().getUniqueId());
		if (kickedMinutes > 0) {
			String minuteSuffix = kickedMinutes != 1 ? "s" : "";// To be grammatically correct
			ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You have been kicked. Try again in " + kickedMinutes + " minute" + minuteSuffix + ".");
			LOG.log(Level.INFO, "Kicking " + ev.getPlayer().getName() + " on join, may return in " + kickedMinutes + " minute" + minuteSuffix + ".");
		}
	}

}
