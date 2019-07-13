package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public final class MuteSystem implements Listener {

	private final Map<UUID, Long> muteTimer = Collections.synchronizedMap(new HashMap<>());

	@Cmd
	public void mute(@NotNull CommandSender sender, @NotNull Player player, @Cmd.UseDefault int minutes) {
		final long timeMs = minutes <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + 60000 * minutes;
		muteTimer.put(player.getUniqueId(), timeMs);
		sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " muted.");
	}

	@Cmd
	public void unMute(@NotNull CommandSender sender, @NotNull Player player) {
		if (muteTimer.remove(player.getUniqueId()) != null) {
			sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " unmuted.");
		} else {
			sender.sendMessage(ChatColor.RED + "Player not muted.");
		}
	}

	public boolean isMuted (@NotNull UUID name) {
		final Long mutedUntil = muteTimer.get(name);
		if (mutedUntil == null) {
			return false;
		}
		return mutedUntil < System.currentTimeMillis();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void stopTheMutedOnes (@NotNull AsyncPlayerChatEvent event) {// Not called for commands
		if (isMuted(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are muted!");
		}
	}
}
