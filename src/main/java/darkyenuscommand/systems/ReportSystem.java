package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

/**
 *
 */
public final class ReportSystem {

	private final List<String> reports;

	public ReportSystem(@NotNull List<String> reports) {
		this.reports = reports;
	}

	@Cmd
	public void report(@NotNull CommandSender sender, @NotNull @Cmd.VarArg String problemDescription) {
		sender.sendMessage(ChatColor.GREEN + "Thank you for your report, administrator will review it as soon as possible");
		reports.add(sender.getName() + ": " + problemDescription);
		getServer().broadcast(ChatColor.BLUE + "New report from " + sender.getName(), "darkyenuscommand.command.viewreport");
	}

	@SuppressWarnings("unused")
	public enum ViewReportMode {
		ADD,
		REMOVE
	}

	@Cmd(value = "view-report", order = 0)
	public void viewReport(@NotNull CommandSender sender, @Cmd.OneOf("ADD") ViewReportMode mode, @Cmd.VarArg @Cmd.UseDefault String message) {
		if (message == null || message.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "Specify message to report");
		} else {
			reports.add(sender.getName() + ": " + message);
			sender.sendMessage(ChatColor.GREEN + "Added to reports");
		}
	}

	@Cmd(value = "view-report", order = 1)
	public void viewReport(@NotNull CommandSender sender, @Cmd.OneOf("REMOVE") ViewReportMode mode, @Cmd.UseDefault int index, @Cmd.UseDefault int expectedHash, @Cmd.UseDefault int thenShowIndex) {
		if (index < 0 || index >= reports.size()) {
			sender.sendMessage(ChatColor.RED + "Specify valid index of message to remove");
			return;
		}

		if (expectedHash != 0 && reports.get(index).hashCode() != expectedHash) {
			sender.sendMessage(ChatColor.RED + "Reports changed, please refresh and try again");
			return;
		}
		reports.remove(index);
		sender.sendMessage(ChatColor.GREEN + "Report removed");

		if (thenShowIndex != 0) {
			viewReport(sender, thenShowIndex - 1);
		}
	}

	@Cmd(value = "view-report", order = 2)
	public void viewReport(@NotNull CommandSender sender, @Cmd.UseDefault int showReport) {
		if (reports.isEmpty()) {
			sender.sendMessage(ChatColor.AQUA + "There are no reports");
			return;
		}
		if (showReport < 0 || showReport >= reports.size()) {
			sender.sendMessage(ChatColor.RED + "There are only " + reports.size() + " reports");
			return;
		}

		final String reportText = reports.get(showReport);
		sender.sendMessage("");
		sender.sendMessage(ChatColor.BLUE + "Report " + (showReport + 1) + "/" + reports.size());
		sender.sendMessage(ChatColor.ITALIC + reportText);
		if (sender instanceof Player) {
			//Send buttons only to the player
			final Player.Spigot spigot = ((Player) sender).spigot();
			final TextComponent message = new TextComponent("");

			if (showReport > 0) {
				final TextComponent previous = new TextComponent("Previous");
				previous.setColor(net.md_5.bungee.api.ChatColor.BLUE);
				previous.setUnderlined(true);
				previous.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/view-report " + (showReport - 1)));
				message.addExtra(previous);
				message.addExtra("    ");
			}
			{
				final TextComponent remove = new TextComponent("Remove");
				remove.setColor(net.md_5.bungee.api.ChatColor.BLUE);
				remove.setUnderlined(true);
				remove.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/view-report remove " + showReport + " " + reportText.hashCode() + " " + (Math.min(reports.size() - 1, Math.max(0, showReport)) + 1)));
				message.addExtra(remove);
			}
			if (showReport < reports.size() - 1) {
				message.addExtra("    ");
				final TextComponent next = new TextComponent("Next");
				next.setColor(net.md_5.bungee.api.ChatColor.BLUE);
				next.setUnderlined(true);
				next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/view-report " + (showReport + 1)));
				message.addExtra(next);
			}

			spigot.sendMessage(message);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void informAboutReportsOnJoin (@NotNull PlayerJoinEvent ev) {
		if (ev.getPlayer().hasPermission("darkyenuscommand.staff")) {
			if (!reports.isEmpty()) {
				if (reports.size() == 1) {
					ev.getPlayer().sendMessage(
							ChatColor.GRAY + "There is " + ChatColor.RED + "1" + ChatColor.GRAY + " pending report.");
				} else {
					ev.getPlayer().sendMessage(
							ChatColor.GRAY + "There are " + ChatColor.RED + reports.size() + ChatColor.GRAY + " pending reports.");
				}
			}
		}
	}
}
