
package darkyenuscommand;

import darkyenuscommand.command.CommandProcessor;
import darkyenuscommand.systems.EnvironmentSystem;
import darkyenuscommand.systems.InfoSystem;
import darkyenuscommand.systems.ItemSystem;
import darkyenuscommand.systems.JailSystem;
import darkyenuscommand.systems.KickSystem;
import darkyenuscommand.systems.MuteSystem;
import darkyenuscommand.systems.PanicSystem;
import darkyenuscommand.systems.ReportSystem;
import darkyenuscommand.systems.TeleportSystem;
import darkyenuscommand.systems.WarpSystem;
import darkyenuscommand.systems.WorldSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class Plugin extends JavaPlugin {

	private static final Logger LOG = logger(Plugin.class);

	PluginData data;

	@Override
	public void onEnable () {
		try {
			data = PluginData.load(getDataFolder());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Could not load files: " + e);
		}
		if (data == null) data = new PluginData();

		FixManager.initialize(this);

		final TeleportSystem teleportSystem = new TeleportSystem();
		final WarpSystem warpSystem = new WarpSystem(data, teleportSystem);
		CommandProcessor.registerCommandsAndEvents(this, new EnvironmentSystem());
		CommandProcessor.registerCommandsAndEvents(this, new InfoSystem(data.bookNoticeBoardsEnabled, data.bookNoticeBoards));
		CommandProcessor.registerCommandsAndEvents(this, new ItemSystem());
		CommandProcessor.registerCommandsAndEvents(this, new JailSystem(this, warpSystem, teleportSystem));
		CommandProcessor.registerCommandsAndEvents(this, new KickSystem());
		CommandProcessor.registerCommandsAndEvents(this, new MuteSystem());
		CommandProcessor.registerCommandsAndEvents(this, new PanicSystem(this));
		CommandProcessor.registerCommandsAndEvents(this, new ReportSystem(data.reports));
		CommandProcessor.registerCommandsAndEvents(this, teleportSystem);
		CommandProcessor.registerCommandsAndEvents(this, warpSystem);
		CommandProcessor.registerCommandsAndEvents(this, new WorldSystem(teleportSystem));

		CommandProcessor.registerCommandsAndEvents(this, new PluginListener());
		CommandProcessor.registerCommandsAndEvents(this, new Commands(this));

		LOG.info("Enabled!");
	}

	@Override
	public void onDisable () {
		try {
			PluginData.save(data);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Could not save files", e);
		}
		data = null;

		LOG.info("Disabled!");
	}

	@Override
	public boolean onCommand (@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
		return "donothing".equals(command.getName());
	}

	public static Logger logger(Class<?> of) {
		return Logger.getLogger("DarkyenusCommand:"+of.getSimpleName());
	}
}
