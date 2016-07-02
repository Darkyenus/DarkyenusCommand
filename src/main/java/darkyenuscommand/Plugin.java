
package darkyenuscommand;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

/**
 *
 */
public class Plugin extends JavaPlugin {

	PluginData data;

	private static final String TIME_KEEP_GAMERULE = "doDaylightCycle";

	PluginListener listener;
	private HashMap<String, Long> kickTimer = new HashMap<>();
	private HashMap<String, Long> muteTimer = new HashMap<>();
	private HashMap<String, Location> recalls = new HashMap<>();
	private HashMap<String, Location> preJailLocations = new HashMap<>();
	private WarpSystem warpSystem;

	@Override
	@SuppressWarnings("LoggerStringConcat")
	public void onDisable () {
		try {
			PluginData.save(data);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Could not save files: " + e);
		}
		warpSystem = null;
		data = null;
		listener = null;

		getLogger().info("Disabled!");
	}

	@Override
	@SuppressWarnings("LoggerStringConcat")
	public void onEnable () {
		listener = new PluginListener(this);

		try {
			data = PluginData.load();
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Could not load files: " + e);
		}
		if (data == null) data = new PluginData();

		warpSystem = new WarpSystem(data);
		FixManager.initialize(this);

		getLogger().info("Enabled!");
	}

	/** For how many minutes player can't connect
	 *
	 * @param name player's name
	 * @return how many minutes will it take to not kick this player again */
	public int kickedMinutes (String name) {
		if (kickTimer.containsKey(name)) {
			long value = kickTimer.get(name);
			if (value < System.currentTimeMillis()) {
				kickTimer.remove(name);
				return 0;
			} else {
				int minutesOut = (int)((value - System.currentTimeMillis()) / 60000);
				if (minutesOut == 0) {
					minutesOut = 1;
				}
				return minutesOut;
			}
		} else {
			return 0;
		}
	}

	public synchronized boolean isMuted (String name) {
		if (muteTimer.containsKey(name)) {
			long value = muteTimer.get(name);
			if (value < System.currentTimeMillis()) {
				muteTimer.remove(name);
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
		try {
			if (command.getName().equals("rules")) {
				// --------------------------------------- RULES
				final List<String> rules = data.rules;
				if (!rules.isEmpty()) {
					sendRefined(sender, rules);
				} else {
					sender.sendMessage(ChatColor.RED + "Rules file is empty, here are some rules you should follow:");
					sender.sendMessage(ChatColor.BLUE + "Play fair. No hacks, cheats.");
					sender.sendMessage(ChatColor.BLUE + "Don't swear, no racism, be nice.");
					sender.sendMessage(ChatColor.BLUE + "Admin is always right.");
				}
				// --------------------------------------- RULES END
				return true;
			} else if (command.getName().equals("kick")) {
				// --------------------------------------- KICK
				OfflinePlayer playerOff = matchPlayer(args[0]);
				int kickTimeMin = 0;
				if (playerOff != null) {
					Player toKick = playerOff instanceof Player && playerOff.isOnline() ? (Player)playerOff : null;
					if (toKick != null) {
						int messageOff = 1;
						if (args.length > 1 && args[1].startsWith("-")) {
							messageOff = 2;
							try {
								kickTimeMin = Integer.parseInt(args[1].substring(1));
							} catch (Exception ignored) {
							}
							kickTimer.put(toKick.getName(), System.currentTimeMillis() + kickTimeMin * 60000);
						}
						StringBuilder messageB = new StringBuilder();
						for (int i = messageOff; i < args.length; i++) {
							messageB.append(args[i]);
							messageB.append(" ");
						}
						String message;
						if (messageB.length() != 0) {
							message = messageB.substring(0, messageB.length() - 1);
						} else {
							message = "You have been kicked.";
						}

						toKick.kickPlayer(message);
						sender.sendMessage(ChatColor.GREEN.toString() + toKick.getName() + " has been kicked for " + kickTimeMin
							+ " minutes.");
					} else {
						sender.sendMessage(ChatColor.RED + "Player is not online.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
				// --------------------------------------- KICK END
				return true;
			} else if (command.getName().equals("say")) {
				// --------------------------------------- SAY
				String sayAs = "Server";
				int offset = 0;
				if (args[0].startsWith("-")) {
					offset = 1;
					sayAs = args[0].substring(1);
				}
				StringBuilder messageB = new StringBuilder();
				for (int i = offset; i < args.length; i++) {
					messageB.append(args[i]);
					messageB.append(" ");
				}
				if (messageB.length() != 0) {
					String message = messageB.substring(0, messageB.length() - 1);
					getServer().broadcastMessage(
						ChatColor.DARK_PURPLE + sayAs + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + message);
				} else {
					return false;
				}
				// --------------------------------------- SAY END
				return true;
			} else if (command.getName().equals("panic")) {
				// --------------------------------------- PANIC
				int timeToPanic = 3;
				try {
					timeToPanic = Integer.parseInt(args[0]);
				} catch (Exception ignored) {
				}

				if (sender instanceof Player) {
					List<Entity> nearbyEntities = ((Player)sender).getNearbyEntities(50, 100, 50);
					List<Player> playersToPanic = new ArrayList<>();
					for (Entity nearbyEntity : nearbyEntities) {
						if (nearbyEntity instanceof Player && !nearbyEntity.equals(sender)) {
							Player panicking = (Player)nearbyEntity;
							if (!panicking.hasPermission("darkyenuscommand.donotpanic")) {
								playersToPanic.add(panicking);
							}
						}
					}
					onPanicCommand(sender, playersToPanic, timeToPanic);
					/*
					 * Player player = (Player) sender; int added = 0;
					 * 
					 * 
					 * for (Entity entity : player.getNearbyEntities(50, 100, 50)) { if (entity instanceof Player &&
					 * !entity.equals(player)) { Player panicking = (Player) entity; if
					 * (!panicking.hasPermission("darkyenuscommand.donotpanic")) { panicking.setMetadata("locked", new
					 * FixedMetadataValue(this, true)); panicPlayers.put(panicking.getName(), System.currentTimeMillis() + timeToPanic
					 * * 60000); added++; } } }
					 * 
					 * if (added != 0) { sender.sendMessage(ChatColor.GREEN + "You have locked " + added + "players for " + timeToPanic
					 * + " minutes."); unPanicker.notify(); } else { sender.sendMessage(ChatColor.RED + "Nobody to panic in range."); }
					 */
				} else {
					final Collection<? extends Player> players = getServer().getOnlinePlayers();
					List<Player> playersToPanic = new ArrayList<>();
					for (Player player : players) {
						if (!player.hasPermission("darkyenuscommand.donotpanic")) {
							playersToPanic.add(player);
						}
					}
					onPanicCommand(sender, playersToPanic, timeToPanic);
					// sender.sendMessage(ChatColor.RED + "In-Game only. Try /globalpanic instead.");
				}
				// --------------------------------------- PANIC END
				return true;
			} else if (command.getName().equals("globalpanic")) {
				// --------------------------------------- GLOBALPANIC
				int timeToPanic = 5;
				try {
					timeToPanic = Integer.parseInt(args[0]);
				} catch (Exception ignored) {
				}

				final Collection<? extends Player> players = getServer().getOnlinePlayers();
				List<Player> playersToPanic = new ArrayList<>();
				for (Player player : players) {
					if (!player.hasPermission("darkyenuscommand.donotpanic") && !player.equals(sender)) {
						playersToPanic.add(player);
					}
				}
				onPanicCommand(sender, playersToPanic, timeToPanic);
				/*
				 * synchronized (unPanicker) { int added = 0; int timeToPanic = 5; try { timeToPanic = Integer.parseInt(args[0]); }
				 * catch (Exception ignored) {}
				 * 
				 * for (Entity entity : getServer().getOnlinePlayers()) { if (entity instanceof Player && !entity.equals(sender)) {
				 * Player panicking = (Player) entity; if (!panicking.hasPermission("darkyenuscommand.donotpanic")) {
				 * panicking.setMetadata("locked", new FixedMetadataValue(this, true)); panicPlayers.put(panicking.getName(),
				 * System.currentTimeMillis() + timeToPanic * 60000); added++; } } }
				 * 
				 * if (added != 0) { sender.sendMessage(ChatColor.GREEN + "You have locked " + added + "players for " + timeToPanic +
				 * " minutes."); unPanicker.notify(); } else { sender.sendMessage(ChatColor.RED + "Nobody to panic."); } }
				 */
				// --------------------------------------- GLOBALPANIC END
				return true;
			} else if (command.getName().equals("depanic")) {
				// --------------------------------------- DEPANIC
				int depanicked = 0;
				for (Player toDepanicPlayer : getServer().getOnlinePlayers()) {
					if (toDepanicPlayer != null) {
						if (!toDepanicPlayer.getMetadata("locked").isEmpty()) {
							toDepanicPlayer.removeMetadata("locked", this);
							depanicked++;
							toDepanicPlayer.sendMessage(ChatColor.BLUE + "You have been unlocked.");
						}
					}
				}
				sender.sendMessage(ChatColor.GREEN.toString() + depanicked + " players unlocked.");
				// --------------------------------------- DEPANIC END
				return true;
			} else if (command.getName().equals("strike")) {
				// --------------------------------------- STRIKE
				OfflinePlayer playerOff = matchPlayer(args[0]);
				if (playerOff != null) {
					Player player = playerOff instanceof Player && playerOff.isOnline() ? (Player)playerOff : null;
					if (player != null) {
						int damage = 2;
						if (args.length > 1) {
							try {
								damage = Integer.parseInt(args[1]);
							} catch (NumberFormatException ignored) {
							}
						}

						player.getWorld().strikeLightningEffect(player.getLocation());
						int healthToSet = (int)player.getHealth() - damage;
						if (healthToSet < 0) {
							healthToSet = 0;
						} else if (healthToSet > 20) {
							healthToSet = 20;
						}
						player.setHealth(healthToSet);
						player.playEffect(EntityEffect.HURT);
						sender.sendMessage(ChatColor.GREEN.toString() + playerOff.getName() + " has been striked.");
					} else {
						sender.sendMessage(ChatColor.RED + "Player is not online.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
				// --------------------------------------- STRIKE END
				return true;
			} else if (command.getName().equals("kill")) {
				// --------------------------------------- KILL
				OfflinePlayer playerToKillOff;
				Player playerToKill = null;
				try {
					playerToKillOff = matchPlayer(args[0]);
					if (playerToKillOff != null) {
						playerToKill = playerToKillOff instanceof Player && playerToKillOff.isOnline() ? (Player)playerToKillOff : null;
					}
				} catch (Exception ignored) {
				}

				if (sender instanceof Player) {
					Player player = (Player)sender;
					if (!player.hasPermission("darkyenuscommand.command.kill.anyone") || args.length == 0) {
						player.setHealth(0);
					} else {
						if (playerToKill == null) {
							player.sendMessage(ChatColor.RED + "Player not found.");
						} else {
							playerToKill.setHealth(0);
						}
					}
				} else {
					if (args.length == 0) {
						return false;
					} else {
						if (playerToKill == null) {
							sender.sendMessage(ChatColor.RED + "Player not found.");
						} else {
							playerToKill.setHealth(0);
						}
					}
				}
				// --------------------------------------- KILL END
				return true;
			} else if (command.getName().equals("report")) {
				// --------------------------------------- REPORT
				if (!args[0].startsWith("-") || !sender.hasPermission("darkyenuscommand.command.report.view")) {
					StringBuilder messageB = new StringBuilder();
					for (String arg : args) {
						messageB.append(arg);
						messageB.append(" ");
					}
					if (messageB.length() != 0) {
						String message = messageB.substring(0, messageB.length() - 1);
						sender.sendMessage(ChatColor.GREEN + "We'll look at it soon.");
						data.reports.add(sender.getName() + ": " + message);
						getServer().broadcast(ChatColor.BLUE + "New report from " + sender.getName() + " added!",
							"darkyenuscommand.command.report.view");
					} else {
						return false;
					}
				} else {
					if (data.reports.size() == 0) {
						sender.sendMessage(ChatColor.RED + "There are no reports.");
					} else {
						int index = 0;
						if (args.length > 1) {
							try {
								index = Integer.parseInt(args[1]);
								if (index < 0) {
									index = 0;
								} else if (index >= data.reports.size()) {
									index = data.reports.size() - 1;
								}
							} catch (NumberFormatException ignored) {
							}
						}
						if ("-read".equalsIgnoreCase(args[0])) {
							sender.sendMessage(ChatColor.BLUE.toString() + index + ")" + ChatColor.ITALIC.toString()
								+ data.reports.get(index));
						} else if ("-delete".equalsIgnoreCase(args[0])) {
							data.reports.remove(index);
							sender.sendMessage(ChatColor.BLUE.toString() + "Report #" + index + " removed!");
						} else {
							sender.sendMessage(ChatColor.BLUE.toString() + "There are " + data.reports.size() + " reports.");
							sender.sendMessage(ChatColor.BLUE.toString() + "Arguments: -read -delete");
						}
					}
				}
				// --------------------------------------- REPORT END
				return true;
			} else if (command.getName().equals("mute")) {
				// --------------------------------------- MUTE
				OfflinePlayer toMute = matchPlayer(args[0]);
				if (toMute != null) {
					long time = Long.MAX_VALUE;
					try {
						time = System.currentTimeMillis() + 60000 * Integer.parseInt(args[1]);
					} catch (Exception ignored) {
					}
					muteTimer.put(toMute.getName(), time);
					sender.sendMessage(ChatColor.GREEN + "Player " + toMute.getName() + " muted.");
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
				// --------------------------------------- MUTE END
				return true;
			} else if (command.getName().equals("unmute")) {
				// --------------------------------------- UNMUTE
				OfflinePlayer toUnMute = matchPlayer(args[0]);
				if (toUnMute != null) {
					if (muteTimer.containsKey(toUnMute.getName())) {
						muteTimer.remove(toUnMute.getName());
						sender.sendMessage(ChatColor.GREEN + "Player " + toUnMute.getName() + " unmuted.");
					} else {
						sender.sendMessage(ChatColor.RED + "Player not muted.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
				// --------------------------------------- UNMUTE END
				return true;
			} else if (command.getName().equals("warp")) {
				// --------------------------------------- WARP
				if (!(sender instanceof Player)) {
					sender.sendMessage("In-game only.");
					return true;
				}
				return warpSystem.onCommand((Player)sender, args);
				// --------------------------------------- WARP END
			} else if (command.getName().equals("playerinfo")) {
				// --------------------------------------- PLAYERINFO
				OfflinePlayer getInfo = matchPlayer(args[0]);
				if (getInfo == null) {
					sender.sendMessage(ChatColor.RED + "Player not found");
					return true;
				}

				sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + getInfo.getName() + ChatColor.BLUE
					+ "'s info");
				sender.sendMessage(ChatColor.RED + "Online: " + ChatColor.BLUE.toString() + getInfo.isOnline());
				sender.sendMessage(ChatColor.RED + "Op: " + ChatColor.BLUE.toString() + getInfo.isOp() + ChatColor.RED
					+ " Whitelisted: " + ChatColor.BLUE.toString() + getInfo.isWhitelisted() + ChatColor.RED + " Banned: "
					+ ChatColor.BLUE.toString() + getInfo.isBanned());
				if (getInfo instanceof Player) {
					Player player = (Player)getInfo;
					sender.sendMessage(ChatColor.RED + "Position: " + ChatColor.BLUE.toString()
						+ player.getLocation().getWorld().getName() + " " + player.getLocation().getBlockX() + " "
						+ player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ());
					sender.sendMessage(ChatColor.RED + "Health: " + ChatColor.BLUE.toString() + (((float)player.getHealth()) / 2)
						+ "/" + (float)player.getMaxHealth());
					if (player.getFireTicks() >= 0)
						sender.sendMessage(ChatColor.RED + "Fire Ticks: " + ChatColor.BLUE.toString() + player.getFireTicks());
					if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() != null)
						sender.sendMessage(ChatColor.RED + "Last Damage: " + ChatColor.BLUE.toString() + player.getLastDamage()
							+ " from " + player.getLastDamageCause().getCause().toString().toLowerCase().replace("_", " "));
					sender.sendMessage(ChatColor.RED + "Food: " + ChatColor.BLUE.toString() + (((float)player.getFoodLevel()) / 2)
						+ "/10");
					sender.sendMessage(ChatColor.RED + "Gamemode: " + ChatColor.BLUE.toString() + player.getGameMode().toString());
					sender.sendMessage(ChatColor.RED + "Level: " + ChatColor.BLUE.toString() + player.getLevel());
					sender.sendMessage(ChatColor.RED + "Experience: " + ChatColor.BLUE.toString() + player.getTotalExperience()
						+ " - " + (int)(player.getExp() * 100.0) + "%");
					sender.sendMessage(ChatColor.RED + "Locked: " + ChatColor.BLUE.toString() + player.hasMetadata("locked"));
					sender.sendMessage(ChatColor.RED + "Walk Speed: " + ChatColor.BLUE.toString() + player.getWalkSpeed()
						+ ChatColor.RED + " Fly Speed: " + ChatColor.BLUE.toString() + player.getFlySpeed());
					if (player.isOnline()) {
						sender.sendMessage(ChatColor.RED + "IP: " + ChatColor.BLUE.toString()
							+ player.getAddress().getAddress().getHostAddress());
					}
				}
				// --------------------------------------- PLAYERINFO END
				return true;
			} else if (command.getName().equals("compass")) {
				// --------------------------------------- COMPASS
				if (!(sender instanceof Player)) {
					sender.sendMessage("In-game only.");
					return true;
				}

				OfflinePlayer set = matchPlayer(args[0]);
				if (set == null) {
					sender.sendMessage(ChatColor.RED + "Player not found");
				} else if (!(set instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player is offline");
				} else {
					((Player)sender).setCompassTarget(((Player)set).getLocation());
				}
				// --------------------------------------- COMPASS END
				return true;
			} else if (command.getName().equals("recall")) {
				// --------------------------------------- RECALL
				Player toRecall;

				if (args.length != 0) {
					OfflinePlayer toRecallOff = matchPlayer(args[0]);
					if (toRecallOff == null) {
						sender.sendMessage(ChatColor.RED + "Player not found");
						return true;
					} else if (!(toRecallOff instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Player is offline");
						return true;
					} else {
						toRecall = (Player)toRecallOff;
					}
				} else if (sender instanceof Player) {
					toRecall = (Player)sender;
				} else {
					return false;// From command line without args? see docs!
				}

				if (recalls.containsKey(toRecall.getName())) {
					toRecall.teleport(recalls.get(toRecall.getName()));
					if (!toRecall.isOnline()) {
						toRecall.saveData();
					}
				} else {
					sender.sendMessage(ChatColor.RED + "No recall location!");
				}
				// --------------------------------------- RECALL END
				return true;
			} else if (command.getName().equals("teleporthereall")) {
				// --------------------------------------- TELEPORTHEREALL
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only." + ChatColor.BLUE + " Use /teleport command instead!");
					return true;
				}

				for (Player player : getServer().getOnlinePlayers()) {
					if (player != sender) teleportPlayer(player, ((Player)sender).getLocation());
				}
				// --------------------------------------- TELEPORTHEREALL END
				return true;
			} else if (command.getName().equals("teleporthere")) {
				// --------------------------------------- TELEPORTHERE
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only. Use /teleport command instead!");
					return true;
				}

				OfflinePlayer toTPOff = matchPlayer(args[0]);
				if (toTPOff == null) {
					sender.sendMessage(ChatColor.RED + "Player not found");
				} else if (!(toTPOff instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player is offline");
				} else {
					teleportPlayer((Player)toTPOff, ((Player)sender).getLocation());
					if (!toTPOff.isOnline()) {
						((Player)toTPOff).saveData();
					}
				}
				// --------------------------------------- TELEPORTHERE END
				return true;
			} else if (command.getName().equals("teleportto")) {
				// --------------------------------------- TELEPORTTO
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only." + ChatColor.BLUE + " Use /teleport command instead!");
					return true;
				}

				OfflinePlayer toTPOff = matchPlayer(args[0]);
				if (toTPOff == null) {
					sender.sendMessage(ChatColor.RED + "Player not found");
				} else if (!(toTPOff instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player is offline");
				} else {
					teleportPlayer((Player)sender, ((Player)toTPOff).getLocation());
				}
				// --------------------------------------- TELEPORTTO END
				return true;
			} else if (command.getName().equals("teleport")) {
				// --------------------------------------- TELEPORT
				// usage: /teleport [+]<X> [+]<Y> [+]<Z> [Player] OR <Top|Up|Down|Bottom> [Player]
				if (args.length >= 3) {
					Player player = getPlayer(args, 3, sender);
					if (player == null) {
						sender.sendMessage(ChatColor.RED + "Please specify player.");
						return false;
					} else {
						int[] source = new int[] {player.getLocation().getBlockX(), player.getLocation().getBlockY(),
							player.getLocation().getBlockZ()};
						boolean[] relative = new boolean[3];
						for (int i = 0; i < 3; i++) {
							relative[i] = args[i].startsWith("+");
						}
						int[] result = new int[3];
						for (int i = 0; i < 3; i++) {
							try {
								result[i] = Integer.parseInt(relative[i] ? args[i].substring(1) : args[i]);
								if (relative[i]) {
									result[i] += source[i];
								}
							} catch (Exception ex) {
								sender.sendMessage(ChatColor.RED + "Invalid location format.");
								return false;
							}
						}

						Location to = player.getLocation().clone();
						to.setX(result[0]);
						to.setY(result[1]);
						to.setZ(result[2]);
						teleportPlayer(player, to);
					}
				} else if (args.length >= 1) {
					Player player = getPlayer(args, 1, sender);
					if (player == null) {
						sender.sendMessage(ChatColor.RED + "Please specify player.");
						return false;
					} else {
						// OR <Top|Up|Down|Bottom> [Player]
						char teleport = args[0].toLowerCase().charAt(0);
						int x = player.getLocation().getBlockX();
						int z = player.getLocation().getBlockZ();
						int nowY = player.getLocation().getBlockY();
						World world = player.getWorld();
						int toY = -1;
						switch (teleport) {
						case 't':
							// Top
							for (int y = world.getHighestBlockYAt(x, z) + 1; y > nowY; y--) {
								if (isGoodTeleportLocation(world, x, y, z)) {
									toY = y;
									break;
								}
							}
							break;
						case 'u':
							// Up
							for (int y = nowY + 1; y < world.getMaxHeight(); y++) {
								if (isGoodTeleportLocation(world, x, y, z)) {
									toY = y;
									break;
								}
							}
							break;
						case 'd':
							// Down
							for (int y = nowY - 1; y > 0; y--) {
								if (isGoodTeleportLocation(world, x, y, z)) {
									toY = y;
									break;
								}
							}
							break;
						case 'b':
							// Bottom
							for (int y = 1; y < nowY; y++) {
								if (isGoodTeleportLocation(world, x, y, z)) {
									toY = y;
									break;
								}
							}
							break;
						default:
							sender.sendMessage(ChatColor.RED + "Invalid location format.");
							return false;
						}
						if (toY != -1) {
							Location teleportTo = player.getLocation().clone();
							teleportTo.setX(x + 0.5);
							teleportTo.setY(toY);
							teleportTo.setZ(z + 0.5);
							teleportPlayer(player, teleportTo);
						} else {
							sender.sendMessage(ChatColor.BLUE + "There's no better place.");
						}
					}
				} else {
					return false;
				}
				// --------------------------------------- TELEPORT END
				return true;
			} else if (command.getName().equals("darkyenuscommand")) {
				// --------------------------------------- DARKYENUSCOMMAND
				if (args.length != 0 && "reload".equalsIgnoreCase(args[0])
					&& sender.hasPermission("darkyenuscommand.command.darkyenuscommand")) {
					onDisable();
					onEnable();
					sender.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
				} else if (args.length != 0 && "gc".equalsIgnoreCase(args[0])
					&& sender.hasPermission("darkyenuscommand.command.darkyenuscommand")) {
					System.gc();
				} else {
					sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Darkyenus Command "
						+ getDescription().getVersion());
					sender.sendMessage(ChatColor.BLUE + "Created by " + ChatColor.GOLD + ChatColor.BOLD + "Darkyen");
					sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC + "   (c) 2012 - 2016 darkyen@me.com");
				}
				// --------------------------------------- DARKYENUSCOMMAND END
				return true;
			} else if (command.getName().equals("command")) {
				// --------------------------------------- COMMAND
				boolean beHonest = sender.hasPermission("darkyenuscommand.command.command.all");
				if (args.length == 0) {
					if (beHonest) {
						// Show plugins
						sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Active plugins:");
						for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
							sender.sendMessage(ChatColor.YELLOW + "  " + plugin.getDescription().getName() + " " + ChatColor.ITALIC
								+ plugin.getDescription().getVersion());
						}
						ArrayList<String> availaibleCommands = new ArrayList<>();
						for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
							for (String maybeAvailaibleCommand : plugin.getDescription().getCommands().keySet()) {
								availaibleCommands.add(maybeAvailaibleCommand);
							}
						}
						sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Availaible Commands:");
						sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.ITALIC + String.join(", ", availaibleCommands));
					} else {
						ArrayList<String> availableCommands = new ArrayList<>();
						for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
							for (String maybeAvailaibleCommand : plugin.getDescription().getCommands().keySet()) {
								if (getCommand(maybeAvailaibleCommand).testPermissionSilent(sender)) {
									availableCommands.add(maybeAvailaibleCommand);
								}
							}
						}
						sender.sendMessage(ChatColor.BLUE + "Availaible Commands: " + ChatColor.YELLOW + ChatColor.ITALIC
							+ String.join(", ", availableCommands));
					}
				} else {
					boolean outputDone = false;
					org.bukkit.plugin.Plugin pluginOfThatName = getServer().getPluginManager().getPlugin(args[0]);
					if (pluginOfThatName != null) {
						outputDone = true;
						sender.sendMessage(ChatColor.BLUE + "Plugin: " + ChatColor.BOLD + pluginOfThatName.getDescription().getName()
							+ " " + ChatColor.ITALIC + pluginOfThatName.getDescription().getVersion());
						if (args.length > 1 || pluginOfThatName.getDescription().getCommands().size() <= 5) {
							Set<String> strings = pluginOfThatName.getDescription().getCommands().keySet();
							if (!beHonest) {
								for (Iterator<String> iterator = strings.iterator(); iterator.hasNext();) {
									String commandThatMayNeedToBeHidden = iterator.next();
									if (!getCommand(commandThatMayNeedToBeHidden).testPermissionSilent(sender)) {
										iterator.remove();
									}
								}
							}
							sender.sendMessage(ChatColor.BLUE + " Commands: " + ChatColor.YELLOW + ChatColor.ITALIC
								+ String.join(", ", strings));
							return true;
						} else if (!pluginOfThatName.getDescription().getCommands().isEmpty()) {
							sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC + "Use command \"/command "
								+ pluginOfThatName.getDescription().getName() + " list\" to see list of "
								+ pluginOfThatName.getDescription().getCommands().size() + " commands this plugin has.");
						}
					}
					PluginCommand commandOfThatName = getServer().getPluginCommand(args[0]);
					if (commandOfThatName != null && (beHonest || commandOfThatName.testPermissionSilent(sender))) {
						outputDone = true;
						sender.sendMessage(ChatColor.BLUE + "Command: " + ChatColor.BOLD + commandOfThatName.getName()
							+ ChatColor.RESET + ChatColor.BLUE.toString() + ChatColor.ITALIC + " (of plugin "
							+ commandOfThatName.getPlugin().getDescription().getName() + ")");
						sender.sendMessage(ChatColor.BLUE + "  Aliases: " + ChatColor.YELLOW
							+ String.join(", ", commandOfThatName.getAliases()));
						sender.sendMessage(ChatColor.BLUE + "  Description: " + ChatColor.YELLOW + commandOfThatName.getDescription());
						sender.sendMessage(ChatColor.BLUE + "  Usage: " + ChatColor.YELLOW + commandOfThatName.getUsage());
						sender.sendMessage(ChatColor.BLUE + "  Permission: " + ChatColor.YELLOW + commandOfThatName.getPermission());
						sender.sendMessage(ChatColor.BLUE.toString()
							+ ChatColor.ITALIC
							+ (commandOfThatName.testPermissionSilent(sender) ? "You " + ChatColor.GREEN + "can"
								+ ChatColor.BLUE.toString() + ChatColor.ITALIC + " perform this command." : "You " + ChatColor.RED
								+ "can't" + ChatColor.BLUE.toString() + ChatColor.ITALIC + " perform this command."));
					}

					if (!outputDone) {
						sender.sendMessage(ChatColor.RED + "No plugins nor commands with name \"" + args[0] + "\" found");
					}
				}
				// --------------------------------------- COMMAND END
				return true;
			} else if (command.getName().equals("time")) {
				// --------------------------------------- TIME
				World world = getWorld(args, 1, sender);
				if (world == null) {
					world = getWorld(args, 0, sender);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "Please specify a valid world.");
					} else {
						sender.sendMessage(ChatColor.BLUE + "Time in world " + world.getName() + " is " + world.getTime());
					}
					return true;
				}
				// World it not null here
				if (args.length == 0) {
					sender.sendMessage(ChatColor.BLUE + "Time in world " + world.getName() + " is " + world.getTime());
					return false;// They may want manual
				}

				int timeToSet;
				try {
					timeToSet = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					if ("morning".equalsIgnoreCase(args[0]) || "sunrise".equalsIgnoreCase(args[0])) {
						timeToSet = 0;
					} else if ("day".equalsIgnoreCase(args[0]) || "noon".equalsIgnoreCase(args[0])) {
						timeToSet = 6000;
					} else if ("afternoon".equalsIgnoreCase(args[0])) {
						timeToSet = 9000;
					} else if ("evening".equalsIgnoreCase(args[0]) || "sunset".equalsIgnoreCase(args[0])) {
						timeToSet = 12000;
					} else if ("night".equalsIgnoreCase(args[0]) || "midnight".equalsIgnoreCase(args[0])) {
						timeToSet = 18000;
					} else if ("dusk".equalsIgnoreCase(args[0])) {
						timeToSet = 13000;
					} else if ("dawn".equalsIgnoreCase(args[0])) {
						timeToSet = 23000;
					} else {
						sender.sendMessage(ChatColor.RED + "Could not determine time.");
						return true;
					}
				}

				world.setGameRuleValue(TIME_KEEP_GAMERULE, "true");

				world.setTime(timeToSet);
				getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set time in " + world.getName() + " to "
						+ timeToSet + "]", "darkyenuscommand.staff");
				// --------------------------------------- TIME END
				return true;
			} else if (command.getName().equals("timemaintain")) {
				// --------------------------------------- TIMEMAINTAIN
				int timeToSet;
				try {
					timeToSet = Integer.parseInt(args[0]);
				} catch (Exception e) {
					if ("morning".equalsIgnoreCase(args[0]) || "sunrise".equalsIgnoreCase(args[0])) {
						timeToSet = 0;
					} else if ("day".equalsIgnoreCase(args[0]) || "noon".equalsIgnoreCase(args[0])) {
						timeToSet = 6000;
					} else if ("afternoon".equalsIgnoreCase(args[0])) {
						timeToSet = 9000;
					} else if ("evening".equalsIgnoreCase(args[0]) || "sunset".equalsIgnoreCase(args[0])) {
						timeToSet = 12000;
					} else if ("night".equalsIgnoreCase(args[0]) || "midnight".equalsIgnoreCase(args[0])) {
						timeToSet = 18000;
					} else if ("dusk".equalsIgnoreCase(args[0])) {
						timeToSet = 13000;
					} else if ("dawn".equalsIgnoreCase(args[0])) {
						timeToSet = 23000;
					} else {
						sender.sendMessage(ChatColor.RED + "Could not determine time.");
						return true;
					}
				}
				World world;
				if (args.length == 1) {
					if (sender instanceof Player) {
						world = ((Player)sender).getWorld();
					} else {
						sender.sendMessage(ChatColor.RED + "Specify world.");
						return true;
					}
				} else {
					world = getServer().getWorld(args[1]);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "World not found.");
						return true;
					}
				}

				boolean timeStopped = "false".equalsIgnoreCase(world.getGameRuleValue(TIME_KEEP_GAMERULE));

				if (timeStopped) {
					if (world.getTime() != timeToSet) {// Should it be updated?
						// Will just change time
						getServer().broadcast(
							ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " updated time to maintain in "
								+ world.getName() + " to " + timeToSet + "]", "darkyenuscommand.staff");
					} else {// No, it should be removed.
						world.setGameRuleValue(TIME_KEEP_GAMERULE, "true");
						getServer().broadcast(
							ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " cancelled time maintain in "
								+ world.getName() + "]", "darkyenuscommand.staff");
					}
				} else {
					world.setGameRuleValue(TIME_KEEP_GAMERULE, "false");
					getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set time to maintain in "
							+ world.getName() + " to " + timeToSet + "]", "darkyenuscommand.staff");
				}
				world.setTime(timeToSet);
				// --------------------------------------- TIMEMAINTAIN END
				return true;
			} else if (command.getName().equals("weather")) {
				// --------------------------------------- WEATHER
				World world;
				if (args.length == 1) {
					if (sender instanceof Player) {
						world = ((Player)sender).getWorld();
					} else {
						sender.sendMessage(ChatColor.RED + "Specify world.");
						return true;
					}
				} else {
					world = getServer().getWorld(args[1]);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "World not found.");
						return true;
					}
				}

				if (args.length == 0) {
					if (!world.hasStorm()) {
						sender.sendMessage(ChatColor.BLUE + "Weather in world " + world.getName() + " is clear.");
					} else if (!world.isThundering()) {
						sender.sendMessage(ChatColor.BLUE + "Weather in world " + world.getName() + " is rainy.");
					} else {
						sender.sendMessage(ChatColor.BLUE + "World " + world.getName() + " has thunderstorm.");
					}
					return false;// They may want manual
				}

				if (args[0].toLowerCase().startsWith("sun") || args[0].toLowerCase().startsWith("c")) {
					world.setStorm(false);
					world.setThundering(false);
					world.setWeatherDuration(Integer.MAX_VALUE);
					getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set weather in " + world.getName()
							+ " to clear]", "darkyenuscommand.staff");
				} else if (args[0].toLowerCase().startsWith("storm") || args[0].toLowerCase().startsWith("rain")
					|| args[0].toLowerCase().startsWith("snow")) {
					world.setStorm(true);
					world.setThundering(false);
					world.setWeatherDuration(Integer.MAX_VALUE);
					getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set weather in " + world.getName()
							+ " to rainy]", "darkyenuscommand.staff");
				} else if (args[0].toLowerCase().startsWith("thunder") || args[0].toLowerCase().startsWith("light")
					|| args[0].toLowerCase().startsWith("rage")) {
					world.setStorm(true);
					world.setThundering(true);
					world.setWeatherDuration(Integer.MAX_VALUE);
					world.setThunderDuration(Integer.MAX_VALUE);
					getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set weather in " + world.getName()
							+ " to thunder]", "darkyenuscommand.staff");
				} else if (args[0].toLowerCase().startsWith("reset")) {
					world.setStorm(false);
					world.setThundering(false);
					world.setWeatherDuration(0);
					world.setThunderDuration(0);
					getServer().broadcast(
						ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set weather in " + world.getName()
							+ " to thunder]", "darkyenuscommand.staff");
				} else {
					sender.sendMessage(ChatColor.RED + "Unknown weather (Try clear, rainy, thunder or reset)");
				}
				// --------------------------------------- WEATHER END
				return true;
			} else if (command.getName().equals("gamemode")) {
				// --------------------------------------- GAMEMODE
				Player toSet;
				if (args.length == 1) {
					if (sender instanceof Player) {
						toSet = ((Player)sender);
					} else {
						sender.sendMessage(ChatColor.RED + "Specify player.");
						return true;
					}
				} else {
					OfflinePlayer toSetOff = matchPlayer(args[1]);
					if (toSetOff == null) {
						sender.sendMessage(ChatColor.RED + "Player not found.");
						return true;
					} else if (!(toSetOff instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Player not online.");
						return true;
					} else {
						toSet = (Player)toSetOff;
					}
				}
				GameMode gameMode;
				if (!"toggle".equalsIgnoreCase(args[0]) && !"t".equalsIgnoreCase(args[0])) {
					try {
						gameMode = GameMode.getByValue(Integer.parseInt(args[0]));
						if (gameMode == null) {
							throw new Exception();
						}
					} catch (Exception e) {
						try {
							gameMode = GameMode.valueOf(args[0].toUpperCase());
						} catch (Exception ex) {
							sender.sendMessage(ChatColor.RED + "Gamemode not found.");
							return true;
						}
					}
				} else {
					if (toSet.getGameMode() == GameMode.CREATIVE) {
						gameMode = GameMode.SURVIVAL;
					} else {
						gameMode = GameMode.CREATIVE;
					}
				}
				toSet.setGameMode(gameMode);
				if (!toSet.isOnline()) {
					toSet.saveData();
				}
				sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Gamemode of " + toSet.getName() + " set to "
					+ gameMode.toString());
				// --------------------------------------- GAMEMODE END
				return true;
			} else if (command.getName().equals("item")) {
				// --------------------------------------- ITEM
				if (args.length != 0) {
					Player toSet;
					if (args.length < 4) {
						if (sender instanceof Player) {
							toSet = ((Player)sender);
						} else {
							sender.sendMessage(ChatColor.RED + "Specify player.");
							return true;
						}
					} else {
						OfflinePlayer toSetOff = matchPlayer(args[3]);
						if (toSetOff == null) {
							sender.sendMessage(ChatColor.RED + "Player not found.");
							return true;
						} else if (!(toSetOff instanceof Player)) {
							sender.sendMessage(ChatColor.RED + "Player not online.");
							return true;
						} else {
							toSet = (Player)toSetOff;
						}
					}

					Material material = EnumMatcher.matchOne(Material.class, args[0]);
					int amount = 1;
					short data = 0;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception ignored) {
					}
					try {
						data = (short)Integer.parseInt(args[2]);
					} catch (Exception ignored) {
					}

					if (amount >= 1 && data >= 0) {
						ItemStack toGive = material != null ? new ItemStack(material, amount, data) : null;
						if (toGive != null) {
							toSet.getInventory().addItem(toGive);
							if (toSet.isOnline()) {
								toSet.updateInventory();
							} else {
								toSet.saveData();
							}
							sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + toSet.getName() + " was given " + amount
								+ " " + toGive.getType().toString().toLowerCase());
						} else {
							sender.sendMessage(ChatColor.RED + "Material \"" + args[0] + "\" not found.");
							final Material nearest = Util.findNearest(Arrays.asList(Material.values()), Enum::name, args[0], 10000);
							if (nearest != null) {
								sender.sendMessage(ChatColor.RED.toString() + ChatColor.ITALIC + " Did you mean " + nearest + "?");
							}
						}
					} else {
						return false;
					}
				} else if (sender instanceof Player) {
					ItemStack item = ((Player)sender).getItemInHand();
					if (item == null) {
						return false;
					} else {
						sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Item Info");
						sender.sendMessage(ChatColor.BLUE.toString() + "Material: " + item.getType().toString());
						sender.sendMessage(ChatColor.BLUE.toString() + "Data: " + item.getData());
						sender.sendMessage(ChatColor.BLUE.toString() + "Durability: " + item.getDurability());
						sender.sendMessage(ChatColor.BLUE.toString() + "Amount: " + item.getAmount());
						for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
							sender.sendMessage(ChatColor.BLUE.toString() + " " + entry.getKey().getName() + " - " + entry.getValue());
						}
					}
				} else {
					return false;
				}
				// --------------------------------------- ITEM END
				return true;
			} else if (command.getName().equals("repair")) {
				// --------------------------------------- REPAIR
				Player player;
				if (args.length < 1) {
					if (sender instanceof Player) {
						player = ((Player)sender);
					} else {
						sender.sendMessage(ChatColor.RED + "Specify player.");
						return true;
					}
				} else {
					OfflinePlayer toSetOff = matchPlayer(args[0]);
					if (toSetOff == null) {
						sender.sendMessage(ChatColor.RED + "Player not found.");
						return true;
					} else if (!(toSetOff instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Player not online.");
						return true;
					} else {
						player = (Player)toSetOff;
					}
				}

				if (player.getItemInHand().getType().getMaxDurability() != 0) {
					ItemStack itemStackToFix = player.getItemInHand();
					itemStackToFix.setDurability((short)0);
					player.setItemInHand(itemStackToFix);
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s "
						+ itemStackToFix.getType().toString().toLowerCase() + " has been repaired.");
				} else {
					sender.sendMessage(ChatColor.RED.toString() + player.getItemInHand().getType().toString().toLowerCase()
						+ " is not repairable.");
				}
				// --------------------------------------- REPAIR END
				return true;
			} else if (command.getName().equals("trash")) {
				// --------------------------------------- TRASH
				Player player;
				if (args.length < 4) {
					if (sender instanceof Player) {
						player = ((Player)sender);
					} else {
						sender.sendMessage(ChatColor.RED + "Specify player.");
						return true;
					}
				} else {
					OfflinePlayer toSetOff = matchPlayer(args[3]);
					if (toSetOff == null) {
						sender.sendMessage(ChatColor.RED + "Player not found.");
						return true;
					} else if (!(toSetOff instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Player not online.");
						return true;
					} else {
						player = (Player)toSetOff;
					}
				}
				if (!player.equals(sender) && !sender.hasPermission("darkyenuscommand.command.trash.anyone")) {
					if (sender instanceof Player) {
						player = (Player)sender;
					}
				}

				if ("item".equalsIgnoreCase(args[0]) || "single".equalsIgnoreCase(args[0])) {
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s held item trashed.");
				} else if ("hotbar".equalsIgnoreCase(args[0]) || "actionbar".equalsIgnoreCase(args[0])) {
					for (int i = 0; i <= 8; i++) {
						player.getInventory().clear(i);
					}
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s hotbar trashed.");
				} else if ("inventory".equalsIgnoreCase(args[0])) {
					for (int i = 0; i <= 35; i++) {
						player.getInventory().clear(i);
					}
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s inventory trashed.");
				} else if ("full".equalsIgnoreCase(args[0]) || "all".equalsIgnoreCase(args[0])) {
					for (int i = 0; i <= 35; i++) {
						player.getInventory().clear(i);
					}
					player.getInventory().setArmorContents(new ItemStack[4]);
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName()
						+ "'s inventory including armor trashed.");
				} else if ("armor".equalsIgnoreCase(args[0])) {
					player.getInventory().setArmorContents(new ItemStack[4]);
					sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s armor trashed.");
				} else {
					return false;
				}
				if (player.isOnline()) {
					player.updateInventory();
				} else {
					player.saveData();
				}
				// --------------------------------------- TRASH END
				return true;
			} else if (command.getName().equals("difficulty")) {
				// --------------------------------------- DIFFICULTY
				World world;
				if (args.length == 1) {
					if (sender instanceof Player) {
						world = ((Player)sender).getWorld();
					} else {
						sender.sendMessage(ChatColor.RED + "Specify world.");
						return true;
					}
				} else {
					world = getServer().getWorld(args[1]);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "World not found.");
						return true;
					}
				}

				if (args.length == 0) {
					sender.sendMessage(ChatColor.BLUE + "Difficulty of world " + world.getName() + " is "
						+ world.getDifficulty().toString().toLowerCase());
					return false;// They may want manual
				}

				Difficulty difficulty;
				try {
					difficulty = Difficulty.getByValue(Integer.parseInt(args[0]));
					if (difficulty == null) {
						throw new Exception();
					}
				} catch (Exception e) {
					try {
						difficulty = Difficulty.valueOf(args[0].toUpperCase());
					} catch (Exception ex) {
						sender.sendMessage(ChatColor.RED + "Difficulty not found. " + ChatColor.BLUE
							+ Arrays.toString(Difficulty.values()));
						return true;
					}
				}
				world.setDifficulty(difficulty);
				getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " set difficulty in " + world.getName()
						+ " to " + difficulty.toString().toLowerCase() + "]", "darkyenuscommand.staff");
				// --------------------------------------- DIFFICULTY END
				return true;
			} else if (command.getName().equals("clear")) {
				// --------------------------------------- CLEAR
				if (args.length == 0) {
					sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "/Clear Arguments");
					sender.sendMessage(ChatColor.BLUE.toString() + "[EntityType] - exact entity type");
					sender.sendMessage(ChatColor.BLUE.toString() + "item - same as DROPPED_ITEM");
					sender.sendMessage(ChatColor.BLUE.toString() + "mob - all mobs");
					sender.sendMessage(ChatColor.BLUE.toString() + "monster - all usually hotile mobs");
					sender.sendMessage(ChatColor.BLUE.toString() + "animal - domestic animals");
					sender.sendMessage(ChatColor.BLUE.toString() + "water - water mobs");
					sender.sendMessage(ChatColor.BLUE.toString() + "projectiles - all shootable items");
					sender.sendMessage(ChatColor.BLUE.toString() + "vehicle - all vehicles");
					sender.sendMessage(ChatColor.BLUE.toString() + "all - all entities");
					return true;
				}

				World world;
				if (args.length == 1) {
					if (sender instanceof Player) {
						world = ((Player)sender).getWorld();
					} else {
						sender.sendMessage(ChatColor.RED + "Specify world.");
						return true;
					}
				} else {
					world = getServer().getWorld(args[1]);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "World not found.");
						return true;
					}
				}
				ArrayList<EntityType> toTrash = new ArrayList<>();
				if (args[0].toLowerCase().startsWith("item")) {
					toTrash.add(EntityType.DROPPED_ITEM);
				} else if (args[0].toLowerCase().startsWith("mob") || args[0].toLowerCase().startsWith("creature")) {
					toTrash.add(EntityType.BLAZE);
					toTrash.add(EntityType.CAVE_SPIDER);
					toTrash.add(EntityType.CHICKEN);
					toTrash.add(EntityType.COW);
					toTrash.add(EntityType.CREEPER);
					toTrash.add(EntityType.ENDERMAN);
					toTrash.add(EntityType.GHAST);
					toTrash.add(EntityType.GIANT);
					toTrash.add(EntityType.IRON_GOLEM);
					toTrash.add(EntityType.MAGMA_CUBE);
					toTrash.add(EntityType.MUSHROOM_COW);
					toTrash.add(EntityType.OCELOT);
					toTrash.add(EntityType.PIG);
					toTrash.add(EntityType.PIG_ZOMBIE);
					toTrash.add(EntityType.SHEEP);
					toTrash.add(EntityType.SILVERFISH);
					toTrash.add(EntityType.SKELETON);
					toTrash.add(EntityType.SLIME);
					toTrash.add(EntityType.SNOWMAN);
					toTrash.add(EntityType.SPIDER);
					toTrash.add(EntityType.SQUID);
					toTrash.add(EntityType.VILLAGER);
					toTrash.add(EntityType.WOLF);
					toTrash.add(EntityType.ZOMBIE);
				} else if (args[0].toLowerCase().startsWith("monster")) {
					toTrash.add(EntityType.BLAZE);
					toTrash.add(EntityType.CAVE_SPIDER);
					toTrash.add(EntityType.CREEPER);
					toTrash.add(EntityType.ENDERMAN);
					toTrash.add(EntityType.GHAST);
					toTrash.add(EntityType.GIANT);
					toTrash.add(EntityType.MAGMA_CUBE);
					toTrash.add(EntityType.OCELOT);
					toTrash.add(EntityType.PIG_ZOMBIE);
					toTrash.add(EntityType.SILVERFISH);
					toTrash.add(EntityType.SKELETON);
					toTrash.add(EntityType.SLIME);
					toTrash.add(EntityType.SPIDER);
					toTrash.add(EntityType.ZOMBIE);
				} else if (args[0].toLowerCase().startsWith("animal")) {
					toTrash.add(EntityType.CHICKEN);
					toTrash.add(EntityType.COW);
					toTrash.add(EntityType.MUSHROOM_COW);
					toTrash.add(EntityType.PIG);
					toTrash.add(EntityType.SHEEP);
					toTrash.add(EntityType.WOLF);
				} else if (args[0].toLowerCase().startsWith("water")) {
					toTrash.add(EntityType.SQUID);
				} else if (args[0].toLowerCase().startsWith("projectile")) {
					toTrash.add(EntityType.ARROW);
					toTrash.add(EntityType.EGG);
					toTrash.add(EntityType.FIREBALL);
					toTrash.add(EntityType.SNOWBALL);
					toTrash.add(EntityType.SMALL_FIREBALL);
					toTrash.add(EntityType.SPLASH_POTION);
					toTrash.add(EntityType.THROWN_EXP_BOTTLE);
				} else if (args[0].toLowerCase().startsWith("vehicle")) {
					toTrash.add(EntityType.MINECART);
					toTrash.add(EntityType.BOAT);
				} else if (args[0].toLowerCase().startsWith("all")) {
					toTrash.addAll(Arrays.asList(EntityType.values()));
				} else {
					EntityType match = EntityType.fromName(args[0].toUpperCase());
					if (match != null) {
						toTrash.add(match);
					} else {
						sender.sendMessage(ChatColor.RED + "No suitable entity (class) found.");
						return true;
					}
				}
				toTrash.remove(EntityType.PLAYER);
				if (toTrash.isEmpty()) {
					sender.sendMessage(ChatColor.RED + "You can't remove players!");
				}
				int trashed = 0;
				for (Entity entity : world.getEntities()) {
					if (toTrash.contains(entity.getType())) {
						entity.remove();
						trashed++;
					}
				}
				getServer().broadcast(
					ChatColor.GRAY.toString() + ChatColor.ITALIC + "[" + sender.getName() + " removed " + trashed
						+ " entities from world " + world.getName() + "]", "darkyenuscommand.staff");
				// --------------------------------------- CLEAR END
				return true;
			} else if (command.getName().equals("jail")) {
				// --------------------------------------- JAIL
				Player toJail;
				OfflinePlayer toSetOff = matchPlayer(args[0]);
				if (toSetOff == null) {
					sender.sendMessage(ChatColor.RED + "Player not found.");
					return true;
				} else if (!(toSetOff instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player not online.");
					return true;
				} else {
					toJail = (Player)toSetOff;
				}

				if (args.length >= 2 && warpSystem.getWarp(args[1]) != null) {
					preJailLocations.put(toJail.getName(), toJail.getLocation());
					warpSystem.warp(toJail, args[1]);
					toJail.setMetadata("locked", new FixedMetadataValue(this, true));
					if (toJail.isOnline()) {
						toJail.sendMessage(ChatColor.BLUE + "You have been jailed.");
					} else {
						toJail.saveData();
					}
					sender.sendMessage(ChatColor.GREEN.toString() + toJail.getName() + " jailed in " + args[1]);
				} else {
					List<String> availaibleWarps = warpSystem.getWarps("jail_");
					if (availaibleWarps.isEmpty()) {
						sender.sendMessage(ChatColor.RED.toString() + "Could not jail, no jails found or specified.");
					} else {
						int jailID = new Random().nextInt(availaibleWarps.size());
						preJailLocations.put(toJail.getName(), toJail.getLocation());
						warpSystem.warp(toJail, availaibleWarps.get(jailID));
						toJail.setMetadata("locked", new FixedMetadataValue(this, true));
						if (toJail.isOnline()) {
							toJail.sendMessage(ChatColor.BLUE + "You have been jailed.");
						} else {
							toJail.saveData();
						}
						sender.sendMessage(ChatColor.GREEN.toString() + toJail.getName() + " jailed in " + availaibleWarps.get(jailID));
					}
				}
				// --------------------------------------- JAIL END
				return true;
			} else if (command.getName().equals("unjail")) {
				// --------------------------------------- UNJAIL
				Player player;
				OfflinePlayer toSetOff = matchPlayer(args[0]);
				if (toSetOff == null) {
					sender.sendMessage(ChatColor.RED + "Player not found.");
					return true;
				} else if (!(toSetOff instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player not online.");
					return true;
				} else {
					player = toSetOff.getPlayer();
				}

				if (preJailLocations.containsKey(player.getName())) {
					player.removeMetadata("locked", this);
					teleportPlayer(player, preJailLocations.remove(player.getName()));
					if (player.isOnline()) {
						player.sendMessage(ChatColor.BLUE + "You have been set free from jail.");
					} else {
						player.saveData();
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player not jailed.");
				}
				// --------------------------------------- UNJAIL END
				return true;
			} else if (command.getName().equals("viewinventory")) {
				// --------------------------------------- VIEWINVENTORY
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only.");
					return true;
				}

				Player viewInventory;
				OfflinePlayer toSetOff = matchPlayer(args[0]);
				if (toSetOff == null) {
					sender.sendMessage(ChatColor.RED + "Player not found.");
					return true;
				} else if (!(toSetOff instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Player not online.");
					return true;
				} else {
					viewInventory = (Player)toSetOff;
				}

				Player toShowTo = (Player)sender;
				toShowTo.openInventory(viewInventory.getInventory());
				// --------------------------------------- VIEWINVENTORY END
				return true;
			} else if (command.getName().equals("setspawn")) {
				// --------------------------------------- SETSPAWN
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only.");
					return true;
				}
				if (((Player)sender).getWorld().setSpawnLocation(((Player)sender).getLocation().getBlockX(),
					((Player)sender).getLocation().getBlockY(), ((Player)sender).getLocation().getBlockZ())) {
					sender.sendMessage(ChatColor.GREEN + "Spawn set.");
				} else {
					sender.sendMessage(ChatColor.RED + "Spawn couldn't be set.");
				}
				// --------------------------------------- SETSPAWN END
				return true;
			} else if (command.getName().equals("spawn")) {
				// --------------------------------------- SETSPAWN
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only.");
					return true;
				}
				teleportPlayer((Player)sender, ((Player)sender).getWorld().getSpawnLocation());

				sender.sendMessage(ChatColor.GREEN + "Teleported to spawn");

				// --------------------------------------- SETSPAWN END
				return true;
			} else if (command.getName().equals("spawnentity")) {
				// --------------------------------------- SPAWNENTITY
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Ingame use only.");
					return true;
				}

				EntityType type;
				int amount = 1;
				Location at = ((Player)sender).getTargetBlock((Set<Material>)null, 200).getLocation().add(0.5, 1, 0.5);
				if (at == null) {
					at = ((Player)sender).getLocation();
				}
				try {
					type = Util.findNearest(Arrays.asList(EntityType.values()), Enum::toString, args[0], 3);
					try {
						amount = Integer.parseInt(args[1]);
						if (amount > 50) {
							amount = 50;
						}
					} catch (Exception ignored) {
					}

					if (type != null) {
						int spawned = 0;
						for (int i = 0; i < amount; i++) {
							if (((Player)sender).getWorld().spawnEntity(at, type) != null) {
								spawned++;
							}
						}
						sender.sendMessage(ChatColor.GREEN + "Spawned " + spawned + " of " + type.toString());
					} else {
						sender.sendMessage(ChatColor.RED + "Please specify entity.");
					}
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Please specify entity.");
				}
				// --------------------------------------- SPAWNENTITY END
				return true;
			} else if (command.getName().equals("setflyspeed")) {
				// --------------------------------------- SETFLYSPEED
				float speed = 0.2f;// Probably default speed?
				Player player;
				try {
					speed = ((float)Integer.parseInt(args[0])) / 10.0f;
					if (speed < -1) {
						speed = -1;
					} else if (speed > 1) {
						speed = 1;
					}
				} catch (Exception ignored) {
				}
				try {
					String playerString = args[1];
					OfflinePlayer playerOff = this.matchPlayer(playerString);
					if (playerOff.isOnline() && playerOff instanceof Player) {
						player = (Player)playerOff;
					} else {
						sender.sendMessage(ChatColor.RED + "That player is offline.");
						return true;
					}
				} catch (Exception e) {
					if (sender instanceof Player) {
						player = (Player)sender;
					} else {
						return false;
					}
				}
				player.setFlySpeed(speed);
				sender.sendMessage(ChatColor.GREEN + "Flying speed adjusted.");
				// --------------------------------------- SETFLYSPEED END
				return true;
			} else if (command.getName().equals("world")) {
				// --------------------------------------- WORLD
				try {
					if (!args[0].toLowerCase().startsWith("l")) {// List
						String worldName = args[1];
						if (args[0].toLowerCase().startsWith("g")) {// Goto
							World world = getServer().getWorld(worldName);
							if (world == null) {
								sender.sendMessage(ChatColor.RED + "World " + worldName + " does not exist.");
							} else {
								Player player;
								if (sender instanceof Player) {
									player = (Player)sender;
									if (player.getWorld().equals(world)) {
										sender.sendMessage(ChatColor.BLUE + "You already are in world \"" + worldName + "\".");
										return true;
									}
								} else {
									try {
										player = (Player)matchPlayer(args[2]);
									} catch (Exception e) {
										sender.sendMessage(ChatColor.RED + "Player could not be found.");
										return true;
									}
									if (player.getWorld().equals(world)) {
										sender.sendMessage(ChatColor.BLUE + "Player \"" + player.getName() + "\" already is in world \""
											+ worldName + "\".");
										return true;
									}
								}

								teleportPlayer(player, world.getSpawnLocation());
								sender.sendMessage(ChatColor.GREEN + "Teleported!");
								if (!player.isOnline()) {
									player.saveData();
								}
								return true;
							}
						} else if (args[0].toLowerCase().startsWith("c") || args[0].toLowerCase().startsWith("d")) {// If create or
// remove
							if (sender.hasPermission("darkyenuscommand.command.world.manage")) {
								if (args[0].toLowerCase().startsWith("c")) {// Create world
									getServer().broadcastMessage(ChatColor.GREEN + "Creating a new world, this may lag a bit.");
									WorldCreator creator = new WorldCreator(worldName);
									if (args.length > 2) {
										creator.generator(args[2], sender);
									}
									getServer().createWorld(creator);
									sender.sendMessage(ChatColor.GREEN + "World created (or loaded)!");
								} else {// Delete world
									World world = getServer().getWorld(worldName);
									if (world == null) {
										sender.sendMessage(ChatColor.RED + "World \"" + worldName + "\" does not exist.");
									} else if (world.getPlayers().isEmpty()) {
										getServer().unloadWorld(world, false);
										if (world.getWorldFolder().delete()) {
											sender.sendMessage(ChatColor.GREEN + "World deleted.");
										} else {
											sender.sendMessage(ChatColor.RED + "World could not be deleted.");
										}
									} else {
										sender.sendMessage(ChatColor.RED + "There are people in \"" + worldName + "\"!");
									}
								}
							} else {
								sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
							}
						} else {
							throw new Exception();// Unknown command, show help
						}
					} else {
						List<World> worlds = getServer().getWorlds();
						if (worlds.isEmpty()) {
							sender.sendMessage(ChatColor.BLUE + "Availaible worlds: <none>");// WTF why that would ever happen? But
// still, its prettier that way.
						} else {
							sender.sendMessage(ChatColor.BLUE + "Availaible worlds:");
							for (World world : worlds) {
								sender.sendMessage(ChatColor.BLUE + world.getName() + ": " + ChatColor.WHITE + " "
									+ world.getEnvironment().toString() + " | " + world.getWorldType().getName() + " | Players: "
									+ world.getPlayers().size());
							}
						}
					}
				} catch (Exception e) {
					if (sender instanceof Player) {
						sender.sendMessage(ChatColor.BLUE + "You are in world \"" + ((Player)sender).getWorld().getName() + "\"");
					}
					sender.sendMessage(ChatColor.RED + command.getUsage());
				}
				// --------------------------------------- WORLD END
				return true;
			} else if (command.getName().equals("heal")) {
				int amount = 999;
				boolean mistakeInSyntax = false;
				Player player = sender instanceof Player ? (Player)sender : null;

				if (args.length >= 1) {
					try {
						OfflinePlayer offlinePlayer = matchPlayer(args[0]);
						if (offlinePlayer != null && offlinePlayer.isOnline()) {
							player = offlinePlayer.getPlayer();
						} else {
							player = null;
						}
					} catch (Exception ignored) {
						mistakeInSyntax = true;
					}

					if (args.length >= 2) {
						try {
							amount = Integer.parseInt(args[1]);
							if (amount < 1) {
								amount = 999;
							}
						} catch (Exception ignored) {
							mistakeInSyntax = true;
						}
					}
				}

				if (player != null) {
					player.setHealth(Math.min(player.getHealth() + amount, player.getMaxHealth()));
					player.setFireTicks(0);
					player.setNoDamageTicks(player.getMaximumNoDamageTicks());
					player.setRemainingAir(player.getMaximumAir());
					player.removePotionEffect(PotionEffectType.BLINDNESS);
					player.removePotionEffect(PotionEffectType.CONFUSION);
					player.removePotionEffect(PotionEffectType.HARM);
					player.removePotionEffect(PotionEffectType.HUNGER);
					player.removePotionEffect(PotionEffectType.POISON);
					player.removePotionEffect(PotionEffectType.SLOW);
					player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
					player.removePotionEffect(PotionEffectType.WEAKNESS);
					player.removePotionEffect(PotionEffectType.WITHER);
					sender.sendMessage(ChatColor.BLUE + player.getName() + " has been healed and cured!");
				} else {
					sender.sendMessage(ChatColor.RED + "Please specify an online player first.");
				}
				return !mistakeInSyntax;
			} else if (command.getName().equals("find")) {
				if (args.length != 0) {
					OfflinePlayer player = matchPlayer(args[0]);
					Biome biome = null;
					try {
						biome = Biome.valueOf(args[0].toUpperCase());
					} catch (Exception ignored) {
					}
					EntityType entity = null;
					try {
						entity = EntityType.fromName(args[0]);
						if (entity == null) {
							entity = EntityType.valueOf(args[0].toUpperCase());
						}
					} catch (Exception ignored) {
					}

					if (player == null && biome == null && entity == null) {
						sender.sendMessage(ChatColor.RED + "\"" + args[0] + "\" does not match nor player, biome or entity.");
					} else {
						int matches = 0;
						if (player != null) matches++;
						if (biome != null) matches++;
						if (entity != null) matches++;
						if (matches != 1) {
							if (args.length >= 2) {
								String args1 = args[1].toLowerCase();
								if (args1.startsWith("p")) {
									biome = null;
									entity = null;
								} else if (args1.startsWith("b")) {
									player = null;
									entity = null;
								} else if (args1.startsWith("e")) {
									biome = null;
									player = null;
								}
							}
							matches = 0;
							if (player != null) matches++;
							if (biome != null) matches++;
							if (entity != null) matches++;
							if (matches != 1) {
								sender.sendMessage(ChatColor.RED + "\"" + args[0]
									+ "\" does match too many things, please specify what are you looking for.");
								return true;
							}
						}
						Location foundAt = null;
						String message;

						if (player != null) {
							if (player instanceof Player) {
								Player locatedPlayer = (Player)player;
								foundAt = locatedPlayer.getLocation();
								message = locatedPlayer.getName();
							} else {
								message = player.getName() + " is not online.";
							}
						} else if (biome != null) {
							if (sender instanceof Player) {
								Location startLocation = ((Player)sender).getLocation();
								int startX = startLocation.getBlockX();
								int startZ = startLocation.getBlockZ();
								World world = startLocation.getWorld();

								int precision = 16;
								int probes = 2000 * precision;
								float angle = 0;
								float distance = 0;
								float angleStep = (1f / precision) * (2f * (float)Math.PI);
								float distanceStep = 100f / precision;

								message = biome.toString().toLowerCase().replace("_", " ") + " not found.";

								for (int i = 0; i < probes; i++) {
									int deltaX = (int)(Math.sin(angle) * distance);
									int deltaZ = (int)(Math.cos(angle) * distance);
									int x = startX + deltaX;
									int z = startZ + deltaZ;

									Biome biomeThere = world.getBiome(x, z);
									if (biomeThere.equals(biome)) {
										foundAt = startLocation.clone().add(x, 0, z);
										message = biome.toString().toLowerCase().replace("_", " ");
										break;
									}

									angle += angleStep;
									distance += distanceStep;
								}
							} else {
								message = "You must be in-game to search for biomes.";
							}
						} else {
							if (sender instanceof Player) {
								Location startLocation = ((Player)sender).getLocation();
								World world = startLocation.getWorld();

								Collection<? extends Entity> foundEntities = world.getEntitiesByClass(entity.getEntityClass());
								if (foundEntities.size() == 0) {
									message = entity.toString().toLowerCase().replace("_", " ") + " not found.";
								} else {
									message = foundEntities.size() + " " + entity.toString().toLowerCase().replace("_", " ") + " found.";
									float closestYet = Float.MAX_VALUE;

									Iterator<? extends Entity> foundEntitiesIterator = foundEntities.iterator();
									for (Entity foundEntity = foundEntitiesIterator.next(); foundEntitiesIterator.hasNext(); foundEntity = foundEntitiesIterator
										.next()) {
										float distance = (float)foundEntity.getLocation().distanceSquared(startLocation);
										if (distance < closestYet) {
											closestYet = distance;
											foundAt = foundEntity.getLocation();
										}
									}
								}
							} else {
								message = "You must be in-game to search for entities.";
							}
						}
						sender.sendMessage(ChatColor.BLUE + message);
						if (foundAt != null) {
							sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC + "X: " + foundAt.getBlockX() + " Y: "
								+ foundAt.getBlockY() + " Z: " + foundAt.getBlockZ());
							if (sender instanceof Player) {
								Location senderLocation = ((Player)sender).getLocation();
								sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC
									+ String.format("Distance: %.2f", foundAt.distance(senderLocation)));
								// TODO Look in direction

								if (((Player)sender).getItemInHand().getType() == Material.COMPASS) {
									((Player)sender).setCompassTarget(foundAt);
								}
							}
						}
					}
					return true;
				}
				return false;
			} else if (command.getName().equals("setbiome")) {
				if (args.length > 0) {
					try {
						int radius = Integer.parseInt(args[0]);
						Biome biome = Biome.valueOf(args[1].toUpperCase());
						boolean circle = false;
						if (args.length > 2 && args[2].equalsIgnoreCase("circle")) {
							circle = true;
						}
						World world;
						int x, z;
						if (circle && args.length >= 7) {
							world = getServer().getWorld(args[3]);
							x = Integer.parseInt(args[4]);
							z = Integer.parseInt(args[5]);
						} else if (args.length >= 6) {
							world = getServer().getWorld(args[2]);
							x = Integer.parseInt(args[3]);
							z = Integer.parseInt(args[4]);
						} else if (sender instanceof Player) {
							world = ((Player)sender).getWorld();
							x = ((Player)sender).getLocation().getBlockX();
							z = ((Player)sender).getLocation().getBlockZ();
						} else {
							sender.sendMessage(ChatColor.RED + "Please specify location when using from command line.");
							return true;
						}
						if (world == null) {
							sender.sendMessage(ChatColor.RED + "Please specify valid world!");
							return true;
						}
						int changedBlocks = 0;
						for (int blockX = x - radius; blockX < x + radius; blockX++) {
							for (int blockZ = z - radius; blockZ < z + radius; blockZ++) {
								if (!circle || ((blockX - x) * (blockX - x) + (blockZ - z) * (blockZ - z)) < radius * radius) {
									if (world.getBiome(blockX, blockZ) != biome) {
										changedBlocks++;
									}
									world.setBiome(blockX, blockZ, biome);
								}
							}
						}
						sender.sendMessage(ChatColor.GREEN.toString() + changedBlocks + " columns had their biome changed. "
							+ ChatColor.ITALIC + "Note: You may have to relog to see changes.");
						return true;
					} catch (NumberFormatException ignored) {
						return false;
					} catch (IllegalArgumentException ignored) {
						sender.sendMessage(ChatColor.RED + "Biome \"" + args[1] + "\" does not exist. Select one from following list:");
						sender.sendMessage(ChatColor.YELLOW + Arrays.toString(Biome.values()));
						return true;
					}
				}
			} else if (command.getName().equals("donothing")) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		return false;
	}

	private boolean isGoodTeleportLocation (World world, int x, int y, int z) {
		return world.getBlockAt(x, y - 1, z).getType().isSolid()
			&& (y == world.getMaxHeight() || (!world.getBlockAt(x, y, z).getType().isSolid() && !world.getBlockAt(x, y + 1, z)
				.getType().isSolid()));
	}

	private void teleportPlayer (Player who, Location to) {
		recalls.put(who.getName(), who.getLocation());
		getLogger().info(who.getName() + " teleported from " + formatLocation(who.getLocation()) + " to " + formatLocation(to));
		who.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
	}

	private String formatLocation (Location location) {
		return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
	}

	private void sendRefined (CommandSender player, List<String> toRefine) {
		for (String message : toRefine) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('$', message));
		}
	}

	private Player getPlayer (String[] args, int argIndex, CommandSender sender) {
		if (args.length <= argIndex) {
			if (sender instanceof Player) {
				return (Player)sender;
			} else {
				return null;
			}
		} else {
			OfflinePlayer player = matchPlayer(args[argIndex]);
			if (player == null || !(player instanceof Player)) {
				if (sender instanceof Player) {
					return (Player)sender;
				} else {
					return null;
				}
			} else {
				return (Player)player;
			}
		}
	}

	private World getWorld (String[] args, int argIndex, CommandSender sender) {
		if (args.length <= argIndex) {
			if (sender instanceof Player) {
				return ((Player)sender).getWorld();
			} else {
				return null;
			}
		} else {
			return getServer().getWorld(args[argIndex]);
		}
	}

	private OfflinePlayer matchPlayer (String from) {
		return Util.findNearest(Arrays.asList(getServer().getOfflinePlayers()), offPlayer -> offPlayer.getName().toLowerCase(),
			from.toLowerCase(), 3);
	}

	private void onPanicCommand (CommandSender sender, Collection<Player> players, int minutes) {
		if (players.size() == 0) {
			sender.sendMessage(ChatColor.RED + "Nobody to panic.");
		} else {
			if (minutes <= 0) {
				sender.sendMessage(ChatColor.RED + "Inavlid amount of time, setting to 3.");
				minutes = 3;
			}
			long toExpireAt = System.currentTimeMillis() + minutes * 60000;
			for (Player playerToLock : players) {
				playerToLock.setMetadata("locked", new ExpiringMetadataValue(toExpireAt));
				playerToLock.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "You have been locked for " + minutes
					+ " minutes. Do not panic, server admin is probably dealing with problems and will unlock you shortly.");
			}
			sender.sendMessage(ChatColor.BLUE.toString() + players.size() + " players locked for " + minutes
				+ " minutes. Use /depanic to unlock them all.");
		}
	}

	private class ExpiringMetadataValue implements MetadataValue {

		private long toExpireAtMillis;

		private ExpiringMetadataValue (long toExpireAtMillis) {
			this.toExpireAtMillis = toExpireAtMillis;
		}

		@Override
		public Object value () {
			return asBoolean();
		}

		@Override
		public int asInt () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public float asFloat () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public double asDouble () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public long asLong () {
			return (asBoolean() ? 1 : 0);
		}

		@Override
		public short asShort () {
			return (short)(asBoolean() ? 1 : 0);
		}

		@Override
		public byte asByte () {
			return (byte)(asBoolean() ? 1 : 0);
		}

		@Override
		public boolean asBoolean () {
			return System.currentTimeMillis() < toExpireAtMillis;
		}

		@Override
		public String asString () {
			return Boolean.toString(asBoolean());
		}

		@Override
		public org.bukkit.plugin.Plugin getOwningPlugin () {
			return Plugin.this;
		}

		@Override
		public void invalidate () {
		}
	}
}
