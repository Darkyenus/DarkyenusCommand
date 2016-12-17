/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package darkyenuscommand;

import darkyenuscommand.util.Parameters;
import darkyenuscommand.util.StringMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.regex.Pattern;

/** @author Darkyen */
final class WarpSystem {

	private final Plugin plugin;
	private final PluginData data;

	WarpSystem(Plugin plugin, PluginData data) {
		this.plugin = plugin;
		this.data = data;
	}

	public boolean onCommand (Player player, String[] args) {
		final Parameters parameters = new Parameters(args);
		final StringMap<Location> warps = data.warps;

		//Exact matches
		if (parameters.match("goto","warp","to")) {
			//Teleport to somewhere
			final String warpName = parameters.rest(" ");
			final MatchUtils.MatchResult<String> foundWarps = matchWarps(warpName);
			if (foundWarps.isDefinite) {
				warpTo(player, warps.get(foundWarps.result()));
			} else {
				printWarpNotFound(player, warpName, "goto");
			}
		} else if (parameters.match("create", "new")) {
			final String warpName = parameters.rest(" ");
			if (warpName.contains("*") || warpName.contains("?")) {
				player.sendRawMessage(ChatColor.RED+"Warp may not contain '*' or '?'");
			} else if (warps.containsKey(warpName)) {
				player.sendRawMessage(ChatColor.RED+"Warp called \""+warpName+"\" already exists");
			} else {
				warps.put(warpName, player.getLocation());
				player.sendRawMessage(ChatColor.BLUE+"Warp \""+warpName+"\" created");
			}
		} else if (parameters.match("delete", "remove")) {
			final String warpName = parameters.rest(" ");
			if (warps.remove(warpName) != null) {
				player.sendRawMessage(ChatColor.BLUE+"Warp \"" + warpName + "\" removed");
			} else {
				printWarpNotFound(player, warpName, "delete");
			}
		} else if (parameters.match("help", "-", "-help")) {
			player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Arguments:");
			player.sendMessage(ChatColor.BLUE.toString() + "create <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "remove <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "goto <Warp name>");
			player.sendMessage(ChatColor.BLUE.toString() + "list");
			player.sendMessage(ChatColor.BLUE.toString() + "Or write the warp name without any sub-command parameter");
		} else if (parameters.match("list") || parameters.eof()) {
			//TODO overflow logic
			if (parameters.eof()) {
				//List all warps
				final ArrayList<String> warpNames = warps.keys().toArray();
				player.sendRawMessage(ChatColor.BLUE+"There are "+warpNames.size()+" warps:");
				printWarpList(net.md_5.bungee.api.ChatColor.AQUA, player, warpNames.toArray(new String[warpNames.size()]), "goto");
			} else {
				final String warpName = parameters.rest(" ");
				final MatchUtils.MatchResult<String> foundWarps = matchWarps(warpName);
				player.sendRawMessage(ChatColor.BLUE+"There are "+foundWarps.results.length+" warps matching '"+warpName+"':");
				printWarpList(net.md_5.bungee.api.ChatColor.AQUA, player, foundWarps.results, "goto");
			}
		} else {
			//Guesswork
			final String warpName = parameters.rest(" ");
			final MatchUtils.MatchResult<String> foundWarps = matchWarps(warpName);
			if (foundWarps.results.length == 0) {
				final TextComponent message = new TextComponent("No warp called \"" + warpName + "\" or similar found. ");
				message.setColor(net.md_5.bungee.api.ChatColor.RED);
				final TextComponent createButton = new TextComponent("Create?");
				createButton.setUnderlined(true);
				createButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp create "+warpName));
				message.addExtra(createButton);
				player.spigot().sendMessage(message);
			} else if (foundWarps.isDefinite){
				warpTo(player, warps.get(foundWarps.result()));
			} else {
				printWarpNotFound(player, warpName, "goto");
			}
		}
		return true;
	}

	public MatchUtils.MatchResult<String> matchWarps(String like) {
		if (like.contains("*") || like.contains("?")) {
			//Wildcard delete!
			final StringBuilder regexSB = new StringBuilder();
			for (int i = 0; i < like.length(); i++) {
				final char c = like.charAt(i);
				if (c == '*') {
					regexSB.append(".*");
				} else if (c == '?') {
					regexSB.append(".?");
				} else {
					//Escape everything else
					if (".^$*+?()[{\\|".indexOf(c) != -1) {
						regexSB.append("\\");
					}
					regexSB.append(Character.toLowerCase(c));
				}
			}
			final Pattern regex = Pattern.compile(regexSB.toString());

			final ArrayList<String> result = new ArrayList<>();
			for (String warpName : data.warps.keys()) {
				if (regex.matcher(warpName.toLowerCase()).matches()) {
					result.add(warpName);
				}
			}
			return new MatchUtils.MatchResult<>(result.toArray(new String[result.size()]));
		} else {
			final ArrayList<String> warpNames = data.warps.keys().toArray();
			return MatchUtils.match(warpNames.toArray(new String[warpNames.size()]), String::toLowerCase, like.toLowerCase());
		}
	}

	private void printWarpNotFound(Player player, String warpName,  String action) {
		final MatchUtils.MatchResult<String> possibleMatches = matchWarps(warpName);
		player.sendRawMessage(ChatColor.RED+"Warp \""+warpName+"\" not found." + (possibleMatches.results.length == 0 ? "" : " Did you mean: "));
		printWarpList(net.md_5.bungee.api.ChatColor.GRAY, player, possibleMatches.results, action);
	}

	private void printWarpList(net.md_5.bungee.api.ChatColor color, Player to, String[] warps, String action) {
		if (warps == null || warps.length == 0) return;
		TextComponent line = new TextComponent();
		line.setColor(color);
		int warpsOnLine = 0;
		for (String warp : warps) {
			if (warpsOnLine == 4) {
				to.spigot().sendMessage(line);
				line = new TextComponent();
				line.setColor(color);
				warpsOnLine = 0;
			}
			final TextComponent warpComponent = new TextComponent(warp);
			warpComponent.setUnderlined(true);
			if (action != null) warpComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp "+action+" "+warp));
			line.addExtra("   ");
			line.addExtra(warpComponent);
			warpsOnLine++;
		}
		to.spigot().sendMessage(line);
	}

	public void warpTo(Player player, Location location) {
		plugin.teleportPlayer(player, location, true);
	}
}
