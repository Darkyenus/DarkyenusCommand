/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package darkyenuscommand.systems;

import darkyenuscommand.PluginData;
import darkyenuscommand.command.Cmd;
import darkyenuscommand.match.Match;
import darkyenuscommand.match.MatchUtils;
import darkyenuscommand.util.StringMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.regex.Pattern;

public final class WarpSystem {

	private final PluginData data;
	private final TeleportSystem teleportSystem;

	public WarpSystem(PluginData data, TeleportSystem teleportSystem) {
		this.data = data;
		this.teleportSystem = teleportSystem;
	}

	enum WarpCommand {
		CREATE, NEW,
		REMOVE, DELETE,
		GOTO, WARP, TO,
		LIST, HELP
	}

	@Cmd
	public void warp(Player sender, WarpCommand command, @Cmd.UseDefault String warp) {
		final StringMap<Location> warps = data.warps;

		switch (command) {
			case CREATE:
			case NEW:
				if (warp.contains("*") || warp.contains("?")) {
					sender.sendRawMessage(ChatColor.RED+"Warp may not contain '*' or '?'");
				} else if (warps.containsKey(warp)) {
					sender.sendRawMessage(ChatColor.RED+"Warp called \""+ warp +"\" already exists");
				} else {
					warps.put(warp, sender.getLocation());
					sender.sendRawMessage(ChatColor.BLUE+"Warp \""+ warp +"\" created");
				}
				break;
			case REMOVE:
			case DELETE:
				if (warps.remove(warp) != null) {
					sender.sendRawMessage(ChatColor.BLUE+"Warp \"" + warp + "\" removed");
				} else {
					printWarpNotFound(sender, warp, "delete");
				}
				break;
			case GOTO:
			case WARP:
			case TO: {
				//Teleport to somewhere
				final Match<String> foundWarps = matchWarps(warp);
				if (foundWarps.success()) {
					teleportSystem.teleportPlayer(sender, getWarp(foundWarps.successResult()), true);
				} else {
					printWarpNotFound(sender, warp, "goto");
				}
				break;
			}
			case LIST:
				if (warp == null || warp.isEmpty()) {
					//List all warps
					final ArrayList<String> warpNames = warps.keys().toArray();
					sender.sendRawMessage(ChatColor.BLUE+"There are "+warpNames.size()+" warps:");
					printWarpList(net.md_5.bungee.api.ChatColor.AQUA, sender, warpNames.toArray(new String[0]), "goto");
				} else {
					final String[] foundWarps = matchWarps(warp).asArray(String.class);
					sender.sendRawMessage(ChatColor.BLUE+"There are "+foundWarps.length+" warps matching '"+warp+"':");
					printWarpList(net.md_5.bungee.api.ChatColor.AQUA, sender, foundWarps, "goto");
				}
				break;
			case HELP:
				sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Arguments:");
				sender.sendMessage(ChatColor.BLUE.toString() + "create <Warp name>");
				sender.sendMessage(ChatColor.BLUE.toString() + "remove <Warp name>");
				sender.sendMessage(ChatColor.BLUE.toString() + "goto <Warp name>");
				sender.sendMessage(ChatColor.BLUE.toString() + "list");
				sender.sendMessage(ChatColor.BLUE.toString() + "Or write the warp name without any sub-command parameter");
				break;
		}
	}

	public Match<String> matchWarps(String like) {
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

			if (result.size() == 1) {
				return Match.success(result.get(0));
			} else {
				return Match.failure("Warp", result.toArray(new String[0]));
			}
		} else {
			final ArrayList<String> warpNames = data.warps.keys().toArray();
			return MatchUtils.match("Warp", warpNames.toArray(new String[0]), String::toLowerCase, like.toLowerCase());
		}
	}

	private void printWarpNotFound(Player player, String warpName,  String action) {
		final String[] possibleMatches = matchWarps(warpName).asArray(String.class);
		player.sendRawMessage(ChatColor.RED+"Warp \""+warpName+"\" not found." + (possibleMatches.length == 0 ? "" : " Did you mean: "));
		printWarpList(net.md_5.bungee.api.ChatColor.GRAY, player, possibleMatches, action);
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

	public Location getWarp(String name) {
		return data.warps.get(name);
	}
}
