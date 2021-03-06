package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.EnumMatcher;
import darkyenuscommand.match.Match;
import darkyenuscommand.util.Parameters;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 *
 */
public final class MaterialArgument extends CommandProcessor.Argument<Material> {

	@SuppressWarnings("deprecation")
	private static final Material[] MATERIALS = Arrays.stream(Material.values())
			.filter((m) -> {
				try {
					return !m.isLegacy();
				} catch (Throwable isLegacyWasProbablyRemoved) {
					return !m.name().startsWith(Material.LEGACY_PREFIX);
				}
			}).toArray(Material[]::new);
	private static final EnumMatcher<Material> materialMatcher = new EnumMatcher<>("Material", MATERIALS);

	public MaterialArgument(@NotNull String symbol) {
		super(symbol, Material.class);
	}

	@Override
	public @NotNull Match<Material> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String name = params.take(null);
		if (name == null) {
			return missing();
		}

		return materialMatcher.match(name);
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		for (Material material : MATERIALS) {
			suggestionConsumer.accept(material.getKey().getKey());
		}
	}
}
