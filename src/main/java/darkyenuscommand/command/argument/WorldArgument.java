package darkyenuscommand.command.argument;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.match.Match;
import darkyenuscommand.match.MatchUtils;
import darkyenuscommand.util.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 */
public final class WorldArgument extends CommandProcessor.Argument<World> {

	public WorldArgument(@NotNull String symbol) {
		super(symbol, World.class);
	}

	@Override
	public @NotNull Match<World> match(@NotNull CommandSender sender, @NotNull Parameters params) {
		final String name = params.take(null);
		if (name == null) {
			return missing();
		}

		final World[] worlds = Bukkit.getWorlds().toArray(new World[0]);
		return MatchUtils.match("World", worlds, World::getName, name);
	}

	@Override
	public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
		for (World world : Bukkit.getWorlds()) {
			suggestionConsumer.accept(world.getName());
		}
	}
}
