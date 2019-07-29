package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.Match;
import darkyenuscommand.util.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static darkyenuscommand.command.argument.OfflinePlayerArgument.*;

/**
 *
 */
public final class PlayerArgument extends CommandProcessor.Argument<Player> {

	public PlayerArgument(@NotNull String symbol) {
		super(symbol, Player.class);
	}

	@Override
	public @NotNull Match<Player> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String name = params.take(null);
		if (name == null) {
			return missing();
		}
		final OfflinePlayer[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(NO_OFFLINE_PLAYERS);
		final Match<OfflinePlayer> result = matchOfflinePlayer(onlinePlayers, name);
		if (result.success()) {
			//noinspection ConstantConditions
			return Match.success(result.successResult().getPlayer());
		}
		// Create pretty fail message
		if (result.suggestions().length > 0) {
			return Match.failure(matchPlayerFail(result.suggestions()));
		}

		// Try offline players
		final Match<OfflinePlayer> offlineResult = matchOfflinePlayer(Bukkit.getOfflinePlayers(), name);
		if (offlineResult.success()) {
			//noinspection ConstantConditions
			return Match.failure(offlineResult.successResult().getName()+" is offline");
		} else {
			return Match.failure(matchPlayerFail(result.suggestions()));
		}
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		suggestOfflinePlayers(Bukkit.getOnlinePlayers().toArray(NO_OFFLINE_PLAYERS), suggestionConsumer);
	}
}
