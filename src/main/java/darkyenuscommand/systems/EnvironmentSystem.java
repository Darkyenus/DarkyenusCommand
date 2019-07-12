package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import darkyenuscommand.match.Alt;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import static org.bukkit.Bukkit.getServer;

/**
 *
 */
public final class EnvironmentSystem {

	private static String formatTime(long worldTime) {
		final int time = (int)Math.floorMod(worldTime + 6000, 24000);
		final int hours = time / 1000;
		final int minutes = (int) (((time % 1000) / 1000f) * 60f); // Gotta floor, otherwise we could get :60 minutes
		return String.format("%d:%02d", hours, minutes) + " (" + worldTime+")";
	}

	@Cmd(order = 0)
	public void time(CommandSender sender, @Cmd.UseImplicit World world) {
		sender.sendMessage(ChatColor.BLUE + "Time in world " + ChatColor.WHITE + world.getName() + ChatColor.BLUE + " is " + formatTime(world.getTime()));
	}

	@Cmd(order = 1)
	public void time(CommandSender sender, int time, @Cmd.UseImplicit World world) {
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

		world.setTime(time);
		getServer().broadcast(
				ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
						.getName() + " set time in " + world.getName() + " to "
						+ formatTime(time) + "]", "darkyenuscommand.staff");
	}

	@SuppressWarnings("unused")
	enum TimeAlias {
		@Alt("SUNRISE")
		MORNING(0),
		@Alt("NOON")
		DAY(6000),
		AFTERNOON(9000),
		@Alt("SUNSET")
		EVENING(12000),
		DUSK(13000),
		@Alt("MIDNIGHT")
		NIGHT(18000),
		DAWN(23000),
		;
		public final int time;

		TimeAlias(int time) {
			this.time = time;
		}
	}

	@Cmd(order = 1)
	public void time(CommandSender sender, TimeAlias time, @Cmd.UseImplicit World world) {
		time(sender, time.time, world);
	}

	@Cmd(order = 0)
	public void timeMaintain(CommandSender sender, @Cmd.UseImplicit World world) {
		if (world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)) {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
							.getName() + " set time to maintain in "
							+ world.getName() + " at " + world.getTime() + "]", "darkyenuscommand.staff");
		} else {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
			getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
							.getName() + " cancelled time maintain in "
							+ world.getName() + "]", "darkyenuscommand.staff");
		}
	}

	@Cmd(order = 1)
	public void timeMaintain(CommandSender sender, int time, @Cmd.UseImplicit World world) {
		if (world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)) {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
							.getName() + " set time to maintain in "
							+ world.getName() + " to " + formatTime(time) + "]", "darkyenuscommand.staff");
		} else {
			if (world.getTime() != time) {// Should it be updated?
				// Will just change time
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " updated time to maintain in "
								+ world.getName() + " to " + formatTime(time) + "]", "darkyenuscommand.staff");
			} else {// No, it should be removed.
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " cancelled time maintain in "
								+ world.getName() + "]", "darkyenuscommand.staff");
			}
		}
		world.setTime(time);
	}

	@Cmd(order = 2)
	public void timeMaintain(CommandSender sender, TimeAlias time, @Cmd.UseImplicit World world) {
		timeMaintain(sender, time.time, world);
	}

	@Cmd(order = 0)
	public void weather(CommandSender sender, @Cmd.UseImplicit World world) {
		if (!world.hasStorm()) {
			sender.sendMessage(ChatColor.BLUE + "Weather in world " + world.getName() + " is clear.");
		} else if (!world.isThundering()) {
			sender.sendMessage(ChatColor.BLUE + "Weather in world " + world.getName() + " is rainy.");
		} else {
			sender.sendMessage(ChatColor.BLUE + "World " + world.getName() + " has thunderstorm.");
		}
	}

	enum Weather {
		@Alt("CLEAR")
		SUNNY,
		@Alt({"STORM", "SNOW"})
		RAIN,
		@Alt("LIGHTNING")
		THUNDER,
		RESET
	}

	@Cmd(order = 1)
	public void weather(CommandSender sender, Weather weather, @Cmd.UseImplicit World world) {
		switch (weather) {
			case SUNNY:
				world.setStorm(false);
				world.setThundering(false);
				world.setWeatherDuration(Integer.MAX_VALUE);
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " set weather in " + world.getName()
								+ " to clear]", "darkyenuscommand.staff");
				break;
			case RAIN:
				world.setStorm(true);
				world.setThundering(false);
				world.setWeatherDuration(Integer.MAX_VALUE);
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " set weather in " + world.getName()
								+ " to rainy]", "darkyenuscommand.staff");
				break;
			case THUNDER:
				world.setStorm(true);
				world.setThundering(true);
				world.setWeatherDuration(Integer.MAX_VALUE);
				world.setThunderDuration(Integer.MAX_VALUE);
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " set weather in " + world.getName()
								+ " to thunder]", "darkyenuscommand.staff");
				break;
			case RESET:
				world.setStorm(false);
				world.setThundering(false);
				world.setWeatherDuration(0);
				world.setThunderDuration(0);
				getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
								.getName() + " set weather in " + world.getName()
								+ " to thunder]", "darkyenuscommand.staff");
				break;
		}
	}


	@Cmd(order = 0)
	public void difficulty(CommandSender sender, @Cmd.UseImplicit World world) {
		sender.sendMessage(ChatColor.BLUE + "Difficulty of world " + world.getName() + " is "
				+ world.getDifficulty().toString().toLowerCase());
	}

	@Cmd(order = 1)
	public void difficulty(CommandSender sender, Difficulty difficulty, @Cmd.UseImplicit World world) {
		world.setDifficulty(difficulty);
		getServer().broadcast(
				ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
						.getName() + " set difficulty in " + world.getName()
						+ " to " + difficulty.toString().toLowerCase() + "]", "darkyenuscommand.staff");
	}

}
