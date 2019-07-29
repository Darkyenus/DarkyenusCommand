package darkyenuscommand;

import darkyenuscommand.command.Cmd;
import darkyenuscommand.match.EnumMatcher;
import darkyenuscommand.match.Match;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static darkyenuscommand.systems.PanicSystem.METADATA_KEY_LOCKED;
import static org.bukkit.Bukkit.getServer;

/**
 *
 */
public final class Commands {

	private final Plugin plugin;
	private final PluginData data;

	public Commands(@NotNull Plugin plugin) {
		this.plugin = plugin;
		this.data = plugin.data;
	}

	@Cmd
	public void rules(@NotNull CommandSender sender) {
		final List<String> rules = data.rules;
		if (!rules.isEmpty()) {
			for (String message : rules) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('$', message));
			}
		} else {
			sender.sendMessage(ChatColor.BLUE + "1. "+ChatColor.RESET+ChatColor.ITALIC+"Thou shalt not be a hindrance");
			sender.sendMessage(ChatColor.BLUE + "2. "+ChatColor.RESET+ChatColor.ITALIC+"Thou shalt not be malicious nor vile");
			sender.sendMessage(ChatColor.BLUE + "3. "+ChatColor.RESET+ChatColor.ITALIC+"Thou shalt not open the wrong end of a banana");
		}
	}

	@Cmd ///say -[Name] <Message>
	public void say(@NotNull CommandSender sender, @Cmd.UseDefault @Cmd.Prefix("-") String as, @Cmd.VarArg String message) {
		if (as == null) {
			as = "Server";
		}
		getServer().broadcastMessage(ChatColor.DARK_PURPLE + as + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + message);
	}


	@Cmd
	public void strike(@NotNull CommandSender sender, Player target, @Cmd.UseDefault int damage) {
		if (damage == 0) {
			damage = 2;
		}

		target.getWorld().strikeLightningEffect(target.getLocation());
		final AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		final double maxHealth = maxHealthAttribute == null ? 1 : maxHealthAttribute.getValue();
		double healthToSet = target.getHealth() - damage;
		if (healthToSet < 0) {
			healthToSet = 0;
		} else if (healthToSet > maxHealth) {
			healthToSet = maxHealth;
		}
		target.setHealth(healthToSet);
		target.playEffect(EntityEffect.HURT);
		sender.sendMessage(ChatColor.GREEN.toString() + target.getName() + " has been struck");
	}

	@Cmd
	public void kill(@NotNull CommandSender sender, @Cmd.UseImplicit Player target) {
		if (sender != target && !sender.hasPermission("darkyenuscommand.command.kill.anyone")) {
			sender.sendMessage(ChatColor.RED+"I can't let you do that.");
			return;
		}
		target.setHealth(0);
	}

	@Cmd
	public void heal(@NotNull CommandSender sender, @Cmd.UseDefault int amount, @Cmd.UseImplicit Player player) {
		final AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		final double maxHealth = maxHealthAttr == null ? 20 : maxHealthAttr.getValue();
		final double to = amount == 0 ? maxHealth : Math.min(maxHealth, Math.max(0, player.getHealth() + amount));

		player.setHealth(to);
		player.setFireTicks(0);
		player.setNoDamageTicks(player.getMaximumNoDamageTicks());
		player.setRemainingAir(player.getMaximumAir());
		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		player.removePotionEffect(PotionEffectType.HARM);
		player.removePotionEffect(PotionEffectType.CONFUSION);
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.removePotionEffect(PotionEffectType.HUNGER);
		player.removePotionEffect(PotionEffectType.WEAKNESS);
		player.removePotionEffect(PotionEffectType.POISON);
		player.removePotionEffect(PotionEffectType.WITHER);
		player.removePotionEffect(PotionEffectType.UNLUCK);
		player.removePotionEffect(PotionEffectType.BAD_OMEN);
		sender.sendMessage(ChatColor.BLUE + player.getName() + " has been healed and cured!");
	}

	@Cmd
	public void playerInfo(@NotNull CommandSender sender, OfflinePlayer aboutPlayer) {
		final String playerName = aboutPlayer.getName();
		sender.sendMessage(""+ChatColor.WHITE + ChatColor.BOLD + playerName + ChatColor.AQUA
				+ "'s info");

		sender.sendMessage((aboutPlayer.isOnline() ? "Online" : "Offline") +
				ChatColor.AQUA + ", " + ChatColor.RESET +
				(aboutPlayer.isOp() ? "Operator" : "Not operator") +
				ChatColor.AQUA + ", " + ChatColor.RESET +
				(aboutPlayer.isWhitelisted() ? "Whitelisted" : "Not whitelisted") +
				ChatColor.AQUA + ", " + ChatColor.RESET +
				(aboutPlayer.isBanned() ? "Banned" : "Not banned"));

		if (aboutPlayer instanceof Player) {
			Player player = (Player) aboutPlayer;
			final Location location = player.getLocation();
			sender.sendMessage(ChatColor.AQUA + "Position: " + ChatColor.RESET
					+ player.getWorld().getName() + " " + location.getBlockX() + " "
					+ location.getBlockY() + " " + location.getBlockZ());
			final AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			sender.sendMessage(ChatColor.AQUA + "Health: " + ChatColor.RESET + player.getHealth()
					+ (maxHealth == null ? "" : "/"+ maxHealth.getValue()));
			if (player.getFireTicks() >= 0)
				sender.sendMessage(ChatColor.AQUA + "Fire Ticks: " + ChatColor.RESET + player.getFireTicks());
			if (player.getLastDamageCause() != null) {
				sender.sendMessage(ChatColor.AQUA + "Last Damage: " + ChatColor.RESET + player.getLastDamage()
						+ " from " + player.getLastDamageCause().getCause().toString().toLowerCase()
						.replace("_", " "));
			}
			sender.sendMessage(ChatColor.AQUA + "Last Slept: "+ ChatColor.RESET + String.format("%.1f", player.getStatistic(Statistic.TIME_SINCE_REST) / 24000f)+" days ago");
			sender.sendMessage(ChatColor.AQUA + "Food: " + ChatColor.RESET + player.getFoodLevel() + "/20");
			sender.sendMessage(ChatColor.AQUA + "Game Mode: " + ChatColor.RESET + player.getGameMode());
			sender.sendMessage(ChatColor.AQUA + "Level: " + ChatColor.RESET + player.getLevel());
			sender.sendMessage(ChatColor.AQUA + "Experience: " + ChatColor.RESET + player.getTotalExperience() + " - " + (int) (player.getExp() * 100.0) + "%");
			sender.sendMessage(ChatColor.AQUA + "Locked: " + ChatColor.RESET + player.hasMetadata(METADATA_KEY_LOCKED));
			sender.sendMessage(ChatColor.AQUA + "Walk Speed: " + ChatColor.RESET + player.getWalkSpeed() + ChatColor.AQUA + " Fly Speed: " + ChatColor.RESET + player.getFlySpeed());
			if (player.isOnline()) {
				final InetSocketAddress address = player.getAddress();
				if (address != null) {
					sender.sendMessage(ChatColor.AQUA + "IP: " + ChatColor.RESET + address.getAddress().getHostAddress());
				}
			}
		}
		sender.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.RESET + aboutPlayer.getUniqueId()
				.toString());

		for (OfflinePlayer player : getServer().getOfflinePlayers()) {
			if (player != aboutPlayer && Objects.equals(player.getName(), playerName)) {
				sender.sendMessage(ChatColor.RED + "Duplicate Player: " + ChatColor.RESET + player
						.getUniqueId() + (player.isOnline() ? " (online)" : ""));
			}
		}
	}

	@Cmd
	public void compass(@NotNull Player sender, @Cmd.UseImplicit Player targetPlayer) {
		sender.setCompassTarget(targetPlayer.getLocation());
		sender.sendMessage(ChatColor.BLUE + "Compass target set to current location of " + targetPlayer.getName());
	}

	enum DarkyenusCommandAction {
		GC
	}

	@Cmd
	public void darkyenusCommand(@NotNull CommandSender sender, @Cmd.UseDefault DarkyenusCommandAction action) {
		if (action == DarkyenusCommandAction.GC && sender.hasPermission("darkyenuscommand.command.darkyenuscommand")) {
			for (int i = 0; i < 10; i++) {
				System.gc();
			}
		} else {
			sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Darkyenus Command " + plugin.getDescription().getVersion());
			sender.sendMessage(ChatColor.BLUE + "Created by " + ChatColor.GOLD + ChatColor.BOLD + "Darkyen");
			sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC + "   (c) 2012 - 2019 darkyen@me.com");
		}
	}

	@Cmd
	public void gamemode(@NotNull CommandSender sender, @NotNull GameMode gameMode, @Cmd.UseImplicit Player player) {
		player.setGameMode(gameMode);
		sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Gamemode of " + player
				.getName() + " set to "
				+ gameMode.toString());
	}

	@SuppressWarnings("unused")
	enum GameModeToggle { TOGGLE }

	@Cmd
	public void gamemode(@NotNull CommandSender sender, @NotNull GameModeToggle toggle, @Cmd.UseImplicit Player player) {
		GameMode gameMode;
		if (player.getGameMode() == GameMode.CREATIVE) {
			gameMode = GameMode.SURVIVAL;
		} else {
			gameMode = GameMode.CREATIVE;
		}
		player.setGameMode(gameMode);
		sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Gamemode of " + player
				.getName() + " set to "
				+ gameMode.toString());
	}

	@Cmd
	public void clear(@NotNull CommandSender sender) {
		sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "/Clear Arguments");
		sender.sendMessage(ChatColor.BLUE.toString() + "[EntityType] - exact entity type");
		sender.sendMessage(ChatColor.BLUE.toString() + "item - same as DROPPED_ITEM");
		sender.sendMessage(ChatColor.BLUE.toString() + "mob - all mobs");
		sender.sendMessage(ChatColor.BLUE.toString() + "monster - all usually hostile mobs");
		sender.sendMessage(ChatColor.BLUE.toString() + "animal - domestic animals");
		sender.sendMessage(ChatColor.BLUE.toString() + "water - water mobs");
		sender.sendMessage(ChatColor.BLUE.toString() + "projectiles - all shootable items");
		sender.sendMessage(ChatColor.BLUE.toString() + "vehicle - all vehicles");
		sender.sendMessage(ChatColor.BLUE.toString() + "all - all entities");
	}

	@Cmd
	public void clear(@NotNull CommandSender sender, @NotNull String what, @Cmd.UseImplicit World world) {
		what = what.toLowerCase();

		final Predicate<Entity> filter;
		if (what.startsWith("item")) {
			filter = e -> e instanceof Item;
		} else if (what.startsWith("mob") || what.startsWith("creature")) {
			filter = e -> e instanceof Creature;
		} else if (what.startsWith("monster")) {
			filter = e -> e instanceof Monster;
		} else if (what.startsWith("animal")) {
			filter = e -> e instanceof Animals;
		} else if (what.startsWith("water")) {
			filter = e -> e instanceof WaterMob;
		} else if (what.startsWith("projectile")) {
			filter = e -> e instanceof Projectile;
		} else if (what.startsWith("vehicle")) {
			filter = e -> e instanceof Vehicle;
		} else if (what.startsWith("all")) {
			filter = e -> !(e instanceof Player);
		} else {
			final Match<EntityType> result = EnumMatcher.match(EntityType.class, what);
			if (result.success()) {
				final EntityType entityType = result.successResult();
				if (entityType == EntityType.PLAYER) {
					sender.sendMessage(ChatColor.RED + "Removing player entities is not allowed");
					return;
				}
				filter = e -> e.getType() == entityType;
			} else {
				sender.sendMessage(result.suggestionMessage());
				return;
			}
		}
		int trashed = 0;
		for (Entity entity : world.getEntities()) {
			if (filter.test(entity)) {
				entity.remove();
				trashed++;
			}
		}
		getServer().broadcast(
				ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender
						.getName() + " removed " + trashed
						+ " entities from world " + world.getName() + "]", "darkyenuscommand.staff");
	}

	@Cmd
	public void viewInventory(@NotNull Player sender, @NotNull Player player) {
		sender.openInventory(player.getInventory());
	}

	@Cmd
	public void spawnEntity(@NotNull Player sender, EntityType entityType, @Cmd.UseDefault int amount) {
		Location at = sender.getTargetBlock(null, 200).getLocation().add(0.5, 1, 0.5);

		if (amount <= 0) {
			amount = 1;
		} else if (amount > 50) {
			amount = 50;
		}

		for (int i = 0; i < amount; i++) {
			sender.getWorld().spawnEntity(at, entityType);
		}
		sender.sendMessage(ChatColor.GREEN + "Spawned " + amount + " of " + entityType);
	}

	@Cmd
	public void setFlySpeed(@NotNull CommandSender sender, @Cmd.UseDefault float speed, @Cmd.UseImplicit Player player) {
		if (speed == 0f) {
			speed = 0.1f;// Probably default speed?
		}
		// TODO(jp): Test this
		if (speed < -1) {
			speed = -1;
		} else if (speed > 1) {
			speed = 1;
		}

		player.setFlySpeed(speed);
		if (sender == player) {
			sender.sendMessage(ChatColor.GREEN + "Flying speed adjusted to " + speed);
		} else {
			sender.sendMessage(ChatColor.GREEN + "Flying speed of " + player
					.getName() + " adjusted to " + speed);
		}
	}

	@Cmd
	public void command(@NotNull CommandSender sender) {
		boolean beHonest = sender.hasPermission("darkyenuscommand.command.command.all");
		if (beHonest) {
			// Show plugins
			sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Active plugins:");
			for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
				sender.sendMessage(ChatColor.YELLOW + "  " + plugin.getDescription()
						.getName() + " " + ChatColor.ITALIC
						+ plugin.getDescription().getVersion());
			}
			ArrayList<String> availableCommands = new ArrayList<>();
			for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
				availableCommands.addAll(plugin.getDescription().getCommands().keySet());
			}
			sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Commands:");
			sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.ITALIC + String
					.join(", ", availableCommands));
		} else {
			ArrayList<String> availableCommands = new ArrayList<>();
			for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
				for (String maybeAvailableCommand : plugin.getDescription().getCommands().keySet()) {
					final PluginCommand command = Bukkit.getPluginCommand(maybeAvailableCommand);
					if (command != null && command.testPermissionSilent(sender)) {
						availableCommands.add(maybeAvailableCommand);
					}
				}
			}
			sender.sendMessage(ChatColor.BLUE + "Available Commands: " + ChatColor.YELLOW + ChatColor.ITALIC
					+ String.join(", ", availableCommands));
		}
	}

	@Cmd
	public void command(@NotNull CommandSender sender, @Cmd.UseDefault String pluginOrCommandName) {
		boolean beHonest = sender.hasPermission("darkyenuscommand.command.command.all");
		boolean outputDone = false;
		org.bukkit.plugin.Plugin pluginOfThatName = getServer().getPluginManager().getPlugin(pluginOrCommandName);
		if (pluginOfThatName != null) {
			outputDone = true;
			sender.sendMessage(ChatColor.BLUE + "Plugin: " + ChatColor.BOLD + pluginOfThatName
					.getDescription().getName()
					+ " " + ChatColor.ITALIC + pluginOfThatName.getDescription().getVersion());
			Set<String> strings = pluginOfThatName.getDescription().getCommands().keySet();
			if (!beHonest) {
				strings.removeIf(commandThatMayNeedToBeHidden -> {
					final PluginCommand command = Bukkit.getPluginCommand(commandThatMayNeedToBeHidden);
					return command == null || !command.testPermissionSilent(sender);
				});
			}
			sender.sendMessage(ChatColor.BLUE + " Commands: " + ChatColor.YELLOW + ChatColor.ITALIC
					+ String.join(", ", strings));
		}
		PluginCommand commandOfThatName = getServer().getPluginCommand(pluginOrCommandName);
		if (commandOfThatName != null && (beHonest || commandOfThatName.testPermissionSilent(sender))) {
			outputDone = true;
			sender.sendMessage(ChatColor.BLUE + "Command: " + ChatColor.BOLD + commandOfThatName
					.getName()
					+ ChatColor.RESET + ChatColor.BLUE.toString() + ChatColor.ITALIC + " (of plugin "
					+ commandOfThatName.getPlugin().getDescription().getName() + ")");
			sender.sendMessage(ChatColor.BLUE + "  Aliases: " + ChatColor.YELLOW
					+ String.join(", ", commandOfThatName.getAliases()));
			sender.sendMessage(ChatColor.BLUE + "  Description: " + ChatColor.YELLOW + commandOfThatName
					.getDescription());
			sender.sendMessage(ChatColor.BLUE + "  Usage: " + ChatColor.YELLOW + commandOfThatName
					.getUsage());
			sender.sendMessage(ChatColor.BLUE + "  Permission: " + ChatColor.YELLOW + commandOfThatName
					.getPermission());
			sender.sendMessage(ChatColor.BLUE.toString()
					+ ChatColor.ITALIC
					+ (commandOfThatName.testPermissionSilent(sender) ? "You " + ChatColor.GREEN + "can"
					+ ChatColor.BLUE
					.toString() + ChatColor.ITALIC + " perform this command." : "You " + ChatColor.RED
					+ "can't" + ChatColor.BLUE
					.toString() + ChatColor.ITALIC + " perform this command."));
		}

		if (!outputDone) {
			sender.sendMessage(ChatColor.RED + "No plugins nor commands with name \"" + pluginOrCommandName + "\" found");
		}
	}

	enum PlayerFaceMode {
		UP, DOWN, NORTH, SOUTH, EAST, WEST,
		PANORAMA0, PANORAMA1, PANORAMA2, PANORAMA3, PANORAMA4, PANORAMA5,
	}

	@Cmd
	public void playerFace(@NotNull Player sender, @NotNull PlayerFaceMode mode) {
		final Location location = sender.getLocation();

		switch (mode) {
			case NORTH:
			case PANORAMA0:
				location.setYaw(180);
				location.setPitch(0);
				break;
			case EAST:
			case PANORAMA1:
				location.setYaw(270);
				location.setPitch(0);
				break;
			case SOUTH:
			case PANORAMA2:
				location.setYaw(0);
				location.setPitch(0);
				break;
			case WEST:
			case PANORAMA3:
				location.setYaw(90);
				location.setPitch(0);
				break;
			case UP:
			case PANORAMA4:
				location.setYaw(180);
				location.setPitch(-90);
				break;
			case DOWN:
			case PANORAMA5:
				location.setYaw(180);
				location.setPitch(90);
				break;
			default:
				return;
		}

		sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
		sender.sendMessage(ChatColor.BLUE + "Facing " + mode.toString().toLowerCase());
	}

	@Cmd("server-stats")
	public void serverStats(@NotNull CommandSender sender) {
		final Runtime runtime = Runtime.getRuntime();
		sender.sendMessage(ChatColor.BLUE + "OS: " + ChatColor.WHITE + System
				.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") on " + runtime
				.availableProcessors() + " cores");
		try {
			final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			sender.sendMessage(ChatColor.BLUE + "CPU load: " + ChatColor.WHITE + os.getSystemLoadAverage());
		} catch (Throwable ex) {
			sender.sendMessage(ChatColor.RED + "Failed to retrieve full server stats");
			if (sender instanceof ConsoleCommandSender) {
				ex.printStackTrace();
			}
		}

		final long freeMemory = runtime.freeMemory();
		final long totalMemory = runtime.totalMemory();
		final long maxMemory = runtime.maxMemory();
		sender.sendMessage(ChatColor.BLUE + "Free Memory: " + ChatColor.WHITE + formatBytes(freeMemory) + " (" + (int) Math
				.round((double) (totalMemory - freeMemory) * 100.0 / (double) maxMemory) + "% of max used)");
		sender.sendMessage(ChatColor.BLUE + "Total Memory: " + ChatColor.WHITE + formatBytes(totalMemory));
		sender.sendMessage(ChatColor.BLUE + "Max Memory: " + ChatColor.WHITE + formatBytes(maxMemory));

		final File file = new File(".");
		final long freeSpace = file.getFreeSpace();
		final long totalSpace = file.getTotalSpace();
		sender.sendMessage(ChatColor.BLUE + "Free Space: " + ChatColor.WHITE + formatBytes(freeSpace) + " (" + (int) Math
				.round((double) (totalSpace - freeSpace) * 100.0 / (double) totalSpace) + "% of total used)");
		sender.sendMessage(ChatColor.BLUE + "Total Space: " + ChatColor.WHITE + formatBytes(totalSpace));
	}

	@NotNull
	private static String formatBytes(long bytes) {
		if (bytes < 1024) {
			return bytes+" B";
		} else if (bytes < 1024 * 1024) {
			return (bytes / 1024)+" KiB";
		} else {
			return (bytes / (1024 * 1024))+" MiB";
		}
	}
}
