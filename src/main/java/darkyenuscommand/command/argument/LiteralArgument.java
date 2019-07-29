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
public class LiteralArgument extends CommandProcessor.Argument<String> {

    private final @NotNull String[] variants;

    public LiteralArgument(@NotNull String symbol, @NotNull String[] variants) {
        super(symbol, String.class);
        assert variants.length > 0;
        this.variants = variants;
    }

    @Override
    public @NotNull Match<String> match(@NotNull CommandSender sender, @NotNull Parameters params) {
        final String item = params.take(null);
        if (item == null) {
            return missing();
        }

        final @NotNull String[] variants = this.variants;
        for (String variant : variants) {
            if (variant.equalsIgnoreCase(item)) {
                return Match.success(item);
            }
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("Expected '").append(variants[0]).append('\'');
        for (int i = 1; i < variants.length; i++) {
            if (i + 1 == variants.length) {
                sb.append(" or ");
            } else {
                sb.append(", ");
            }
            sb.append('\'').append(variants[i]).append('\'');
        }
        return Match.failure(sb.toString());
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
        for (String variant : variants) {
            suggestionConsumer.accept(variant);
        }
    }
}
