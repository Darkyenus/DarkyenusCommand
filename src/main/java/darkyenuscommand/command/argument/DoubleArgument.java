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
public final class DoubleArgument extends CommandProcessor.Argument<Double> {

	public DoubleArgument(@NotNull String symbol) {
		super(symbol, Double.TYPE);
	}

	@Override
	public @NotNull Match<Double> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String token = params.take(null);
		if (token == null) {
			return missing();
		}
		if (token.startsWith("+")) {
			return Match.failure("\"" + token + "\" is not a number (leading + is not allowed)");
		}

		final double result;
		try {
			result = Double.parseDouble(token);
		} catch (NumberFormatException ex) {
			return Match.failure("\"" + token + "\" is not a number");
		}
		return Match.success(result);
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		suggestionConsumer.accept("0");
	}
}