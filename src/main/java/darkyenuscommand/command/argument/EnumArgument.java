package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.EnumMatcher;
import darkyenuscommand.match.Match;
import darkyenuscommand.util.Parameters;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 */
public class EnumArgument <T extends Enum<T>> extends CommandProcessor.Argument<T> {

	private final EnumMatcher<T> enumMatcher;
	private final T[] constants;

	public EnumArgument(@NotNull String symbol, @NotNull Class<T> type, T[] constants) {
		super(symbol, type);
		if (constants.length == 0) {
			throw new IllegalArgumentException("no enum constants specified");
		}
		this.constants = constants;
		enumMatcher = new EnumMatcher<>(type.getSimpleName(), constants);
	}

	@NotNull
	@Override
	public Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String item = params.take(null);
		if (item == null) {
			return missing();
		}
		return enumMatcher.match(item);
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		for (T constant : constants) {
			suggestionConsumer.accept(prettyEnumName(constant));
		}
	}

	@NotNull
	public static <E extends Enum<E>> String prettyEnumName(@NotNull E constant) {
		// If enum provides custom toString, use it, otherwise fallback to lowercase name
		final String constantString = constant.toString();
		final String constantName = constant.name();
		if (constantString.equals(constantName)) {
			return constantName.toLowerCase();
		} else {
			return constantString;
		}
	}
}
