package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.Match;
import darkyenuscommand.match.MatchUtils;
import darkyenuscommand.util.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 *
 */
public final class OfflinePlayerArgument extends CommandProcessor.Argument<OfflinePlayer> {

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

	private static boolean allHaveSameName(OfflinePlayer[] players) {
		if (players.length <= 1) return true;
		final String name = players[0].getName();
		for (int i = 1; i < players.length; i++) {
			if (!players[i].getName().equalsIgnoreCase(name)) return false;
		}
		return true;
	}


	public static Match<OfflinePlayer> matchOfflinePlayer(OfflinePlayer[] players, String from) {
		//There can be two players with same name. We prefer that one which is online/played last.
		//However, others can be specified with ~[num] syntax, to select a different one
		int index = -1;
		{
			final int separatorIndex = from.indexOf('~');
			if (separatorIndex != -1) {
				if (separatorIndex + 1 == from.length()) {
					index = 0;
				} else {
					try {
						index = Integer.parseInt(from.substring(separatorIndex+1));
					} catch (NumberFormatException nfe) {
						index = 0;
					}
				}
				from = from.substring(0, separatorIndex);
			}
		}

		final Match<OfflinePlayer> match = MatchUtils.match("Player", players, offPlayer -> offPlayer.getName().toLowerCase(), from.toLowerCase());
		if (match.success()) {
			return match;
		}
		if (index != -1 && !allHaveSameName(match.suggestions()) && match.suggestions().length > 1) {
			// Use resolution specifier
			final OfflinePlayer[] suggestions = match.suggestions();
			Arrays.sort(suggestions, Comparator.comparingLong(OfflinePlayer::getLastPlayed));
			//Pick one
			index = Math.max(0, Math.min(index, suggestions.length));
			return Match.success(suggestions[index]);
		}

		return match;
	}

	static void suggestOfflinePlayers(@NotNull OfflinePlayer[] players, @NotNull Consumer<String> suggestionConsumer) {
		Arrays.sort(players, Comparator.comparingLong(OfflinePlayer::getLastPlayed).reversed());

		String lastPlayerName = null;
		int counter = 0;
		for (OfflinePlayer player : players) {
			final String name = player.getName();
			if (lastPlayerName != null && lastPlayerName.equals(name)) {
				suggestionConsumer.accept(name+'~'+(++counter));
			} else {
				lastPlayerName = name;
				counter = 0;
				suggestionConsumer.accept(name);
			}
		}
	}

	public static String matchPlayerFail(@NotNull final OfflinePlayer[] suggestions){
		if(suggestions.length == 0){
			return ChatColor.RED+"Player not found";
		} else if(suggestions.length == 1){
			return ChatColor.RED+"Player not found. "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET + suggestions[0].getName() + ChatColor.DARK_RED + " ?";
		} else {
			//Will be concatenated at compile time
			final StringBuilder sb = new StringBuilder(ChatColor.RED+"Player not found. "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET);
			final boolean appendIdentifier = allHaveSameName(suggestions);
			if (appendIdentifier) {
				Arrays.sort(suggestions, Comparator.comparingLong(OfflinePlayer::getLastPlayed).reversed());
			}
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
				if (appendIdentifier) {
					sb.append('~').append(i);
				}
			}
			sb.append(ChatColor.DARK_RED).append('?');
			return sb.toString();
		}
	}
}
