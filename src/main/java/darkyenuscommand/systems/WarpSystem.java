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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Pattern;

public final class WarpSystem {

	private final PluginData data;
	private final TeleportSystem teleportSystem;

	public WarpSystem(PluginData data, TeleportSystem teleportSystem) {
		this.data = data;
		this.teleportSystem = teleportSystem;
	}

	@Cmd("warp")
	public void warpCreate(Player sender, @Cmd.OneOf({"create", "new"}) String create, String warp) {
		final StringMap<Location> warps = data.warps;

		if (warp.contains("*") || warp.contains("?")) {
			sender.sendRawMessage(ChatColor.RED+"Warp may not contain '*' or '?'");
		} else if (warps.containsKey(warp)) {
			sender.sendRawMessage(ChatColor.RED+"Warp called \""+ warp +"\" already exists");
		} else {
			warps.put(warp, sender.getLocation());
			sender.sendRawMessage(ChatColor.BLUE+"Warp \""+ warp +"\" created");
		}
	}

	@Cmd("warp")
	public void warpRemove(Player sender, @Cmd.OneOf({"remove", "delete"}) String remove, String warp) {
		final StringMap<Location> warps = data.warps;

		if (warps.remove(warp) != null) {
			sender.sendRawMessage(ChatColor.BLUE+"Warp \"" + warp + "\" removed");
		} else {
			printWarpNotFound(sender, warp, "delete");
		}
	}

	@Cmd("warp")
	public void warpGoTo(Player sender, @Cmd.OneOf({"goto", "to"}) String to, String warp) {
		final Match<String> foundWarps = matchWarps(warp);
		if (foundWarps.success()) {
			teleportSystem.teleportPlayer(sender, getWarp(foundWarps.successResult()), true);
		} else {
			printWarpNotFound(sender, warp, "goto");
		}
	}

	@Cmd("warp")
	public void warpList(Player sender, @Cmd.OneOf({"list"}) String list, @Nullable @Cmd.UseDefault String warp) {
		final StringMap<Location> warps = data.warps;

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
	}

	@Cmd("warp")
	public void warpHelp(Player sender, @Cmd.OneOf({"help", "?"}) String help) {
		sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Available Arguments:");
		sender.sendMessage(ChatColor.AQUA.toString() + "create <Warp name>");
		sender.sendMessage(ChatColor.AQUA.toString() + "remove <Warp name>");
		sender.sendMessage(ChatColor.AQUA.toString() + "goto <Warp name>");
		sender.sendMessage(ChatColor.AQUA.toString() + "list");
	}

	private int nextWarp = (int)System.currentTimeMillis();

	public Match<String> matchWarps(@NotNull String like) {
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

			if (result.isEmpty()) {
				return Match.failure("Warp");
			}

			// Pick one pseudo-randomly
			return Match.success(result.get(((nextWarp++) & 0x7FFF_FFFF) % result.size()));
		} else {
			final ArrayList<String> warpNames = data.warps.keys().toArray();
			return MatchUtils.match("Warp", warpNames.toArray(new String[0]), String::toLowerCase, like.toLowerCase());
		}
	}

	private void printWarpNotFound(@NotNull Player player, @NotNull String warpName,  @NotNull String action) {
		final String[] possibleMatches = matchWarps(warpName).asArray(String.class);
		player.sendRawMessage(ChatColor.RED+"Warp \""+warpName+"\" not found." + (possibleMatches.length == 0 ? "" : " Did you mean: "));
		printWarpList(net.md_5.bungee.api.ChatColor.GRAY, player, possibleMatches, action);
	}

	private void printWarpList(@NotNull net.md_5.bungee.api.ChatColor color, @NotNull Player to, @Nullable String[] warps, @Nullable String action) {
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
