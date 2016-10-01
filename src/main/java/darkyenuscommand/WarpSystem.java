/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package darkyenuscommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author Darkyen */
final class WarpSystem {
	private final PluginData data;

	WarpSystem (PluginData data) {
		this.data = data;
	}

	public boolean onCommand (Player player, String[] args) {
		final Map<String, Location> warps = data.warps;

		if ("create".equalsIgnoreCase(args[0]) && args.length > 1 && !args[1].contains(":")) {
			createWarp(args[1], player.getLocation());
			player.sendMessage(ChatColor.GREEN + "Warp " + args[1] + " created!");
		} else if ("remove".equalsIgnoreCase(args[0]) && args.length > 1 && !args[1].contains(":")) {
			if (warps.containsKey(args[1])) {
				removeWarp(args[1]);
				player.sendMessage(ChatColor.GREEN + "Warp " + args[1] + " removed!");
			} else
				player.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
		} else if (("warp".equalsIgnoreCase(args[0]) || "goto".equalsIgnoreCase(args[0]))
			&& (args.length > 1 && !args[1].contains(":"))) {
			if (warps.containsKey(args[1])) {
				warp(player, args[1]);
			} else
				player.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
		} else if ("list".equalsIgnoreCase(args[0])) {
			player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Warps:");
			if (warps.size() != 0)
				player.sendMessage(ChatColor.BLUE.toString() + Arrays.toString(warps.keySet().toArray(new String[warps.size()])));
			else
				player.sendMessage(ChatColor.BLUE.toString() + "None");
		} else {
			player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Arguments:");
			player.sendMessage(ChatColor.BLUE.toString() + "create <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "remove <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "warp <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "list");
			return false;
		}
		return true;
	}

	void warp (Player player, String warpName) {
		player.teleport(data.warps.get(warpName));
	}

	public void createWarp(String warpName, Location where) {
		data.warps.put(warpName, where);
	}

	public void removeWarp (String warpName) {
		data.warps.remove(warpName);
	}

	public List<String> getWarps (String prefix) {
		ArrayList<String> result = new ArrayList<>();
		for (String warp : data.warps.keySet()) {
			if (warp.startsWith(prefix)) result.add(warp);
		}
		return result;
	}

	public Location getWarp (String warp) {
		return data.warps.get(warp);
	}
}
