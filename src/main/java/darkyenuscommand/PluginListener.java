
package darkyenuscommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.MetadataValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/** @author Darkyen */
final class PluginListener implements Listener {

	private final Plugin plugin;

	PluginListener (Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		BookUtil.initialize(plugin);
	}

	@EventHandler
	@SuppressWarnings("LoggerStringConcat")
	public void kickAndBanCheck (PlayerLoginEvent ev) throws IOException {
		// Kick check
		int kickedMinutes = plugin.kickedMinutes(ev.getPlayer().getUniqueId());
		if (kickedMinutes > 0) {
			String minuteSuffix = "";// To be grammatically correct
			if (kickedMinutes != 1) {
				minuteSuffix = "s";
			}

			ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You have been kicked. Try again in " + kickedMinutes + " minute"
				+ minuteSuffix + ".");
			plugin.getLogger().log(Level.INFO,
				"Kicking " + ev.getPlayer().getName() + " on join, may return in " + kickedMinutes + " minute" + minuteSuffix + ".");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void informAboutReportsOnJoin (PlayerJoinEvent ev) {
		if (ev.getPlayer().hasPermission("darkyenuscommand.staff")) {
			final List<String> reports = plugin.data.reports;
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

	@EventHandler(priority = EventPriority.LOW)
	public void stopTheMutedOnes (AsyncPlayerChatEvent event) {// Not called for commands
		if (plugin.isMuted(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are muted!");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void commandSigns (PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if (isLocked(ev.getPlayer())) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setUseItemInHand(Event.Result.DENY);
			try {
				// noinspection deprecation
				ev.getPlayer().updateInventory();
			} catch (Exception ignored) {
			}
		} else {
			if ((ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_BLOCK)// Is clicking block
				&& (ev.getClickedBlock().getType() == Material.WALL_SIGN || ev.getClickedBlock().getType() == Material.SIGN_POST)// Is
// the block a sign
				&& ev.getPlayer().hasPermission("darkyenuscommand.sign")// Has permission?
				&& !ev.isCancelled() && !ev.getPlayer().isSneaking()) {// Is not already cancelled && classic sneak check
				Sign sign = (Sign)ev.getClickedBlock().getState();
				String signText = "";
				for (String line : sign.getLines()) {
					signText = signText + line;
				}
				if (signText.startsWith("//")) {
					// Cancel event
					ev.setCancelled(true);
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setUseInteractedBlock(Event.Result.DENY);
					try {
						BlockState state = ev.getClickedBlock().getState();
						if (state != null) {
							state.update();
						}
					} catch (NullPointerException ignored) {
					}
					// Perform command and, if success, make fire dance
					if (ev.getPlayer().performCommand(signText.trim().substring(2))) {
						ev.getPlayer().getWorld().playEffect(ev.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), Effect.SMOKE, 4);
					}
				}
			}

			if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.getPlayer().hasPermission("darkyenuscommand.infostick")
				&& ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
				// Infostick time!
				Block clicked = ev.getClickedBlock();
				Player player = ev.getPlayer();
				player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Infostick report on block at X: "
					+ clicked.getLocation().getBlockX() + " Y: " + clicked.getLocation().getBlockY() + " Z: "
					+ clicked.getLocation().getBlockZ());
				player.sendMessage(ChatColor.BLUE.toString() + " Material: " + ChatColor.WHITE + ChatColor.ITALIC
					+ clicked.getType().toString() + " (ID: " + clicked.getTypeId() + " Data: " + clicked.getData() + ")");
				player.sendMessage(ChatColor.BLUE.toString() + " Biome: " + ChatColor.WHITE + ChatColor.ITALIC + clicked.getBiome()
					+ " (Temperature: " + clicked.getTemperature() + " Humidity:" + clicked.getHumidity() + ")");
				player.sendMessage(ChatColor.BLUE.toString() + " Redstone power: " + ChatColor.WHITE + ChatColor.ITALIC
					+ clicked.getBlockPower() + " (D" + clicked.getBlockPower(BlockFace.DOWN) + " U"
					+ clicked.getBlockPower(BlockFace.UP) + " N" + clicked.getBlockPower(BlockFace.NORTH) + " S"
					+ clicked.getBlockPower(BlockFace.SOUTH) + " W" + clicked.getBlockPower(BlockFace.WEST) + " E"
					+ clicked.getBlockPower(BlockFace.EAST) + ")");
				player.sendMessage(ChatColor.BLUE.toString() + " Light: " + ChatColor.WHITE + ChatColor.ITALIC
					+ clicked.getLightLevel() + " (Sky: " + clicked.getLightFromSky() + " Blocks: " + clicked.getLightFromBlocks()
					+ ")");
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void infoStickEntity (PlayerInteractEntityEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		} else {
			if (ev.getPlayer().hasPermission("darkyenuscommand.infostick")
				&& ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
				Entity clicked = ev.getRightClicked();
				Player player = ev.getPlayer();
				player.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + "Infostick report on entity " + clicked.getEntityId()
					+ " at X: " + clicked.getLocation().getX() + " Y: " + clicked.getLocation().getX() + " Z: "
					+ clicked.getLocation().getX());
				player.sendMessage(ChatColor.BLUE.toString() + " EntityType: " + ChatColor.WHITE + ChatColor.ITALIC
					+ clicked.getType() + " (Class: " + clicked.getClass().getSimpleName() + ")");
				player.sendMessage(ChatColor.BLUE.toString() + " Fire: " + ChatColor.WHITE + ChatColor.ITALIC
					+ clicked.getFireTicks() + "/" + clicked.getMaxFireTicks());
				player.sendMessage(ChatColor.BLUE.toString() + " Dead: " + ChatColor.WHITE + ChatColor.ITALIC + clicked.isDead());
				player.sendMessage(ChatColor.BLUE.toString() + " Vehicle: " + ChatColor.WHITE + ChatColor.ITALIC + "In: "
					+ clicked.isInsideVehicle() + " Is: " + (clicked.getPassenger() != null));
				if (clicked instanceof LivingEntity) {
					LivingEntity clickedLE = (LivingEntity)clicked;
					player.sendMessage(ChatColor.BLUE.toString() + " Health: " + ChatColor.WHITE + ChatColor.ITALIC
						+ clickedLE.getHealth() + "/" + clickedLE.getMaxHealth());
					player.sendMessage(ChatColor.BLUE.toString() + " Air: " + ChatColor.WHITE + ChatColor.ITALIC
						+ clickedLE.getRemainingAir() + "/" + clickedLE.getMaximumAir());
					player.sendMessage(ChatColor.BLUE.toString() + " Can pickup items: " + ChatColor.WHITE + ChatColor.ITALIC
						+ clickedLE.getCanPickupItems());
					if (clicked instanceof Player) {
						player
							.sendMessage(ChatColor.BLUE + "For more info about player, use \"/playerinfo " + clicked.getName() + "\"");
					}
				}
				if (clicked instanceof Creature) {
					Creature creature = (Creature)clicked;
					player.sendMessage(ChatColor.BLUE.toString()
						+ " Target: "
						+ ChatColor.WHITE
						+ ChatColor.ITALIC
						+ (creature.getTarget() != null ? creature.getTarget().getType() + " ID: " + creature.getTarget().getEntityId()
							: "None"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void saveOfflineInventories (InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof Player) {
			Player holder = (Player)event.getInventory().getHolder();
			if (!holder.isOnline()) {
				holder.saveData();
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void godFist (EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player damager = (Player)event.getDamager();
			if (!damager.getWorld().getPVP() && event.getEntity() instanceof Player) return;
			if (damager.getGameMode() == GameMode.CREATIVE
				&& damager.hasPermission("darkyenuscommand.godfist")
				&& (damager.getItemInHand() == null || damager.getItemInHand().getTypeId() == 0 || damager.getItemInHand()
					.getAmount() == 0)) {
				event.setDamage(Integer.MAX_VALUE);
				damager.playEffect(event.getEntity().getLocation(), Effect.EXTINGUISH, null);
			}
		}
	}

	// Cancel for locked
	private boolean isLocked (Player player) {
		if (player.hasMetadata("locked")) {
			List<MetadataValue> values = player.getMetadata("locked");
			for (MetadataValue value : values) {
				if (value.asBoolean()) {
					return true;
				} else if (value.getOwningPlugin() instanceof Plugin) {
					player.removeMetadata("locked", plugin);
				}
			}
			return false;
		} else {
			return false;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (PlayerMoveEvent ev) {
		if (isLocked(ev.getPlayer())) {
			if (ev.getFrom().getBlockX() != ev.getTo().getBlockX() || ev.getFrom().getBlockZ() != ev.getTo().getBlockZ())
				ev.setTo(ev.getFrom());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (BlockBreakEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (PlayerPickupItemEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (PlayerDropItemEvent ev) {
		if (isLocked(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (InventoryClickEvent ev) {
		if (isLocked((Player)ev.getWhoClicked())) {
			ev.setResult(Event.Result.DENY);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void checkAction (EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player && isLocked((Player)ev.getEntity())) {
			ev.setCancelled(true);
		}
	}

	//Book Notice Board
	@EventHandler(ignoreCancelled = true)
	public void interactWithNoticeBoard(PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if(!plugin.data.bookNoticeBoardsEnabled) return;
		final Player player = ev.getPlayer();
		final Block block = ev.getClickedBlock();
		if (block == null || (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) || player.isSneaking()) {
			return;
		}
		final Location location = block.getLocation();

		final PluginData.NoticeBoard noticeBoard = plugin.data.bookNoticeBoards.get(location);
		if(noticeBoard == null) {
			//Maybe player wants to make this sign into notice board?
			final Sign state = (Sign) block.getState();
			final String[] lines = state.getLines();
			if (lines.length == 0) return;
			if (!"[notice]".equalsIgnoreCase(lines[0])) return;
			final ItemStack item = ev.getItem();
			if(item == null || (item.getType() != Material.WRITTEN_BOOK && item.getType() != Material.BOOK_AND_QUILL)) return;
			final BookMeta itemMeta = (BookMeta) item.getItemMeta();

			final PluginData.NoticeBoard board = new PluginData.NoticeBoard();
			final String signFragment = board.init(player, itemMeta.getPages());

			if(lines.length >= 2) {
				if("readonly".equalsIgnoreCase(lines[1])) {
					board.neverFreeForAll();
				} else {
					try {
						board.freeForAllAfterDays(Integer.parseInt(lines[1]));
					} catch (NumberFormatException ignored) {
					}
				}
			}

			plugin.data.bookNoticeBoards.put(location, board);
			if(board.pages.length != 0) {
				final List<String> strings = formatForSign(signFragment);
				for (int i = 0; i < strings.size() && i < 4; i++) {
					state.setLine(i, strings.get(i));
				}
				state.update();
			}

			final TextComponent component = new TextComponent(net.md_5.bungee.api.ChatColor.BLUE+"Notice Board Created");
			//component.setColor(net.md_5.bungee.api.ChatColor.BLUE);
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
		} else {
			//Show the sign!
			BookUtil.displayBook(player, noticeBoard.getDisplayItem());
		}

		ev.setUseInteractedBlock(Event.Result.DENY);
		ev.setUseItemInHand(Event.Result.DENY);
		ev.setCancelled(true);
	}

	private int formatForSign_lengthWithoutChatColor(CharSequence cs){
		int result = 0;
		for (int i = 0; i < cs.length(); i++) {
			if(cs.charAt(i) == ChatColor.COLOR_CHAR) {
				i++;
			} else {
				result++;
			}
		}
		return result;
	}

	private ChatColor formatForSign_getEndChatColor(CharSequence cs) {
		ChatColor result = null;
		for (int i = 0; i < cs.length()-1; i++) {
			if(cs.charAt(i) == ChatColor.COLOR_CHAR) {
				final ChatColor code = ChatColor.getByChar(cs.charAt(i + 1));
				if(code != null) {
					if(code.isColor()) {
						result = code;
					} else if(code == ChatColor.RESET){
						result = null;
					}
				}
				i++;
			}
		}

		return result;
	}

	private CharSequence formatForSign_makeItalic(CharSequence cs, ChatColor leadingColor) {
		final StringBuilder sb = new StringBuilder();
		if(leadingColor != null){
			sb.append(leadingColor.toString());
		}
		sb.append(ChatColor.ITALIC.toString());

		for (int i = 0; i < cs.length(); i++) {
			final char c = cs.charAt(i);
			if(c == ChatColor.COLOR_CHAR) {
				if(i == cs.length()) continue;//Invalid format code
				final ChatColor code = ChatColor.getByChar(cs.charAt(i + 1));
				if(code != null) {
					if(code.isColor()) {
						sb.append(code).append(ChatColor.ITALIC);
					} else if(code == ChatColor.RESET){
						sb.append(ChatColor.RESET).append(ChatColor.ITALIC);
					}
				}
				i++;//Eat code
			} else {
				sb.append(c);
			}
		}
		return sb;
	}

	private void formatForSign_addResult(List<String> result, CharSequence append) {
		if(result.size() >= 4) return;
		ChatColor leading = null;
		if(!result.isEmpty()) {
			leading = formatForSign_getEndChatColor(result.get(result.size() - 1));
		}
		result.add(formatForSign_makeItalic(append, leading).toString());
	}

	private List<String> formatForSign(String page) {
		//Let's assume, that line will fit only 16 characters.
		final int LINE_LIMIT = 16;
		final List<String> result = new ArrayList<>(4);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < page.length(); i++) {
			final char c = page.charAt(i);
			if(sb.length() == 0 && Character.isWhitespace(c)) {
				//Don't append if just more whitespace
				continue;
			} else if(c == '\n') {
				if(sb.length() != 0) {
					formatForSign_addResult(result, sb);
					sb.setLength(0);
				}
				continue;
			} else {
				sb.append(c);
			}
			if (formatForSign_lengthWithoutChatColor(sb) >= LINE_LIMIT) {
				//We are at line limit

				int lineBreakAt = -1;
				for (int sbi = sb.length() - 1; sbi >= 0; sbi--) {
					if(Character.isWhitespace(sb.charAt(sbi))) {
						lineBreakAt = sbi;
						break;
					}
				}
				if(lineBreakAt == sb.length() - 1) {
					//Perfect end
					sb.setLength(sb.length()-1);//Trim end whitespace
					formatForSign_addResult(result, sb);
					sb.setLength(0);
				} else if(lineBreakAt == -1) {
					//One long word, add hyphen
					final StringBuilder line = new StringBuilder();
					line.append(sb);
					line.setCharAt(sb.length()-1, '-');
					formatForSign_addResult(result, line);
					sb.setCharAt(0, sb.charAt(sb.length()-1));
					sb.setLength(1);
				} else {
					formatForSign_addResult(result, sb.substring(0, lineBreakAt));
					final String sbRemaining = sb.substring(lineBreakAt + 1);
					sb.setLength(0);
					sb.append(sbRemaining);
				}
			}

			if(result.size() >= 4) break;
		}
		formatForSign_addResult(result, sb);
		return result;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void noticeBoardDestroyed(BlockBreakEvent event) {
		plugin.data.bookNoticeBoards.remove(event.getBlock().getLocation());
	}
}
