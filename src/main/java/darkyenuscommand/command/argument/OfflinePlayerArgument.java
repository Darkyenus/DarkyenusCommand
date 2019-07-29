package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.Match;
import darkyenuscommand.match.MatchUtils;
import darkyenuscommand.util.Parameters;
import darkyenuscommand.util.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 */
public final class OfflinePlayerArgument extends CommandProcessor.Argument<OfflinePlayer> {

	static final OfflinePlayer[] NO_OFFLINE_PLAYERS = new OfflinePlayer[0];
	private static final Comparator<OfflinePlayer> LAST_PLAYED_COMPARATOR =
			Comparator.comparingLong(OfflinePlayer::getLastPlayed).reversed();

	public OfflinePlayerArgument(@NotNull String symbol) {
		super(symbol, OfflinePlayer.class);
	}

	@Override
	public @NotNull Match<OfflinePlayer> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String name = params.take(null);
		if (name == null) {
			return missing();
		}
		final Match<OfflinePlayer> result = matchOfflinePlayer(Bukkit.getOfflinePlayers(), name);
		if (result.success()) {
			return result;
		}
		// Create pretty fail message
		return Match.failure(matchPlayerFail(result.suggestions()));
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		suggestOfflinePlayers(Bukkit.getOfflinePlayers(), suggestionConsumer);
	}

	private static boolean allHaveSameName(@NotNull OfflinePlayer[] players) {
		if (players.length <= 1) return true;
		final OfflinePlayer firstPlayer = players[0];
		final String name = firstPlayer.getName();
		if (name == null) {
			return false;
		}
		for (int i = 1; i < players.length; i++) {
			final OfflinePlayer otherPlayer = players[i];
			if (firstPlayer.equals(otherPlayer)) {
				continue;
			}
			final String otherName = otherPlayer.getName();
			if (otherName == null || !otherName.equalsIgnoreCase(name)) return false;
		}
		return true;
	}

	@NotNull
	public static Match<OfflinePlayer> matchOfflinePlayer(@NotNull OfflinePlayer[] players, @NotNull String from) {
		{
			final UUID playerUUID = UUIDUtil.parseUUID(from);
			if (playerUUID != null) {
				for (OfflinePlayer player : players) {
					if (playerUUID.equals(player.getUniqueId())) {
						return Match.success(player);
					}
				}
			}
		}

		final Match<OfflinePlayer> match = MatchUtils.match("Player", players, offPlayer -> {
			final String name = offPlayer.getName();
			return name == null ? null : name.toLowerCase();
		}, from.toLowerCase());

		if (match.success()) {
			return match;
		}

		if (match.suggestions().length > 1 && allHaveSameName(match.suggestions())) {
			// Use most recent one
			final OfflinePlayer[] suggestions = match.suggestions();
			Arrays.sort(suggestions, LAST_PLAYED_COMPARATOR);
			return Match.success(suggestions[0]);
		}

		return match;
	}

	static void suggestOfflinePlayers(@NotNull OfflinePlayer[] players, @NotNull Consumer<String> suggestionConsumer) {
		Arrays.sort(players, LAST_PLAYED_COMPARATOR);

		for (OfflinePlayer player : players) {
			final String name = player.getName();
			if (name == null) {
				continue;
			}
			suggestionConsumer.accept(name);
		}
	}

	@NotNull
	public static String matchPlayerFail(@NotNull final OfflinePlayer[] suggestions){
		if (suggestions.length == 0) {
			return ChatColor.RED+"Player not found";
		} else if (suggestions.length == 1) {
			return ChatColor.RED+"Player not found. "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET + suggestions[0].getName() + ChatColor.DARK_RED + " ?";
		} else {
			//Will be concatenated at compile time
			final StringBuilder sb = new StringBuilder(ChatColor.RED+"Player not found. "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET);
			for (int i = 0; i < suggestions.length; i++) {
				if(i != 0) {
					sb.append(ChatColor.DARK_RED);
					sb.append(i + 1 == suggestions.length ? " or " : ", ");
					sb.append(ChatColor.RESET);
				}
				final OfflinePlayer player = suggestions[i];
				if(player.isOnline()) {
					sb.append(ChatColor.DARK_GREEN);
				}
				sb.append(player.getName());
			}
			sb.append(ChatColor.DARK_RED).append('?');
			return sb.toString();
		}
	}
}
