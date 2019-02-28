package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.Match;
import darkyenuscommand.util.Parameters;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 */
public final class StringArgument extends CommandProcessor.Argument<String> {

	public StringArgument(@NotNull String symbol) {
		super(symbol, String.class);
	}

	@Override
	public @NotNull Match<String> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String collected = params.take(null);
		if (collected == null) {
			return missing();
		}
		return Match.success(collected);
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {}
}
