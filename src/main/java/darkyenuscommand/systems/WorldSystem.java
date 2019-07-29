package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

/**
 *
 */
public final class WorldSystem implements Listener {

	private final TeleportSystem teleportSystem;

	public WorldSystem(TeleportSystem teleportSystem) {
		this.teleportSystem = teleportSystem;
	}

	@Cmd
	public void world(Player sender) {
		sender.sendMessage(ChatColor.BLUE + "You are in world \"" + sender.getWorld().getName() + "\"");
	}

	@Cmd
	public void world(CommandSender sender, @Cmd.OneOf("list") String command) {
		List<World> worlds = getServer().getWorlds();
		sender.sendMessage(ChatColor.BLUE + "Available worlds:");
		for (World world : worlds) {
			final WorldType worldType = world.getWorldType();
			sender.sendMessage(ChatColor.BLUE + world.getName() + ": " + ChatColor.WHITE + " "
					+ world.getEnvironment().toString() + " | " + (worldType == null ? "<unknown-world-type>" : worldType
					.getName()) + " | Players: "
					+ world.getPlayers().size());
		}
	}

	@Cmd
	public void world(CommandSender sender, @Cmd.OneOf("create") String command, String world, @Cmd.UseDefault String generator) {
		if (!sender.hasPermission("darkyenuscommand.command.world.manage")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
			return;
		}

		getServer().broadcastMessage(ChatColor.GREEN + "Creating a new world, this may lag a bit.");
		WorldCreator creator = new WorldCreator(world);
		if (generator != null && !generator.isEmpty()) {
			creator.generator(generator, sender);
		}
		getServer().createWorld(creator);
		sender.sendMessage(ChatColor.GREEN + "World created (or loaded)!");
	}

	@Cmd
	public void world(CommandSender sender, @Cmd.OneOf("delete") String command, World world) {
		if (!sender.hasPermission("darkyenuscommand.command.world.manage")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
			return;
		}

		if (!world.getPlayers().isEmpty()) {
			sender.sendMessage(ChatColor.RED + "There are people in \"" + world.getName() + "\"!");
			return;
		}

		getServer().unloadWorld(world, false);
		if (world.getWorldFolder().delete()) {
			sender.sendMessage(ChatColor.GREEN + "World deleted.");
		} else {
			sender.sendMessage(ChatColor.RED + "World could not be deleted.");
		}
	}

	enum WorldCommandMode {
		GOTO, GOTOEXACT
	}

	@Cmd
	public void world(CommandSender sender, WorldCommandMode command, World world, @Cmd.UseImplicit Player player) {
		if (player.getWorld().equals(world)) {
			if (player.equals(sender)) {
				sender.sendMessage(ChatColor.BLUE + "You already are in world \"" + world.getName() + "\"");
			} else {
				sender.sendMessage(ChatColor.BLUE + "Player \"" + player.getName() + "\" already is in world \"" + world.getName() + "\"");
			}
			return;
		}

		final Location targetLocation;
		switch (command) {
			case GOTO:
				targetLocation = world.getSpawnLocation();
				break;
			case GOTOEXACT:
				targetLocation = player.getLocation();
				targetLocation.setWorld(world);
				break;
			default:
				throw new AssertionError(command);
		}

		teleportSystem.teleportPlayer(player, targetLocation, true);
		if (player.equals(sender)) {
			sender.sendMessage(ChatColor.GREEN + "Teleported!");
		} else {
			sender.sendMessage(ChatColor.GREEN + player.getName()+ " teleported!");
		}
	}
}
