
package darkyenuscommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;

import java.util.*;

import static org.bukkit.Material.*;

/** Private property. User: Darkyen Date: 7/30/13 Time: 11:34 AM */
public final class FixManager {

	private FixManager () {
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static void initialize (Plugin plugin) {
		final PluginData config = plugin.data;

		final Server server = plugin.getServer();
		if (config.saddleRecipe) {
			try {
				ShapedRecipe saddleCraftRecipe = new ShapedRecipe(new ItemStack(Material.SADDLE));
				saddleCraftRecipe.shape("lll", "lil", "i i");
				saddleCraftRecipe.setIngredient('l', Material.LEATHER);
				saddleCraftRecipe.setIngredient('i', Material.IRON_INGOT);
				if (!server.addRecipe(saddleCraftRecipe)) {
					System.out.println("DarkyenusCommand: Saddle recipe could not be added.");
				}
			} catch (Exception e) {
				System.out.println("DarkyenusCommand: Could not activate saddleCraft.");
				e.printStackTrace();
			}
		}
		if (config.recordRecipe) {
			try {
				addRecordRecipe(server, Material.GOLD_RECORD, DyeColor.YELLOW);// 13
				addRecordRecipe(server, Material.GREEN_RECORD, DyeColor.GREEN);// cat
				addRecordRecipe(server, Material.RECORD_3, DyeColor.ORANGE);// blocks
				addRecordRecipe(server, Material.RECORD_4, DyeColor.RED);// chirp
				addRecordRecipe(server, Material.RECORD_5, DyeColor.LIME);// far
				addRecordRecipe(server, Material.RECORD_6, DyeColor.LIGHT_BLUE);// mall
				addRecordRecipe(server, Material.RECORD_7, DyeColor.MAGENTA);// mellohi
				addRecordRecipe(server, Material.RECORD_8, DyeColor.BLACK);// stal
				addRecordRecipe(server, Material.RECORD_9, DyeColor.WHITE);// strad
				addRecordRecipe(server, Material.RECORD_10, DyeColor.CYAN);// ward
				addRecordRecipe(server, Material.RECORD_11, DyeColor.GRAY);// 11
				addRecordRecipe(server, Material.RECORD_12, DyeColor.BLUE);// wait
			} catch (Exception e) {
				System.out.println("DarkyenusCommand: Could not activate recordCraft.");
				e.printStackTrace();
			}
		}
		if (config.horseArmorRecipe) {
			try {
				addHorseArmorRecipe(server, Material.IRON_INGOT, Material.IRON_BLOCK, Material.IRON_BARDING);
				addHorseArmorRecipe(server, Material.GOLD_INGOT, Material.GOLD_BLOCK, Material.GOLD_BARDING);
				addHorseArmorRecipe(server, Material.DIAMOND, Material.DIAMOND_BLOCK, Material.DIAMOND_BARDING);
			} catch (Exception e) {
				System.out.println("DarkyenusCommand: Could not activate horseArmorCraft.");
				e.printStackTrace();
			}
		}
		if (config.nameTagRecipe) {
			try {
				ShapedRecipe nameTagCraftRecipe = new ShapedRecipe(new ItemStack(Material.NAME_TAG));
				nameTagCraftRecipe.shape("  s", " p ", "p  ");
				nameTagCraftRecipe.setIngredient('s', Material.STRING);
				nameTagCraftRecipe.setIngredient('p', Material.PAPER);
				if (!server.addRecipe(nameTagCraftRecipe)) {
					System.out.println("DarkyenusCommand: Name Tag recipe could not be added.");
				}
			} catch (Exception e) {
				System.out.println("DarkyenusCommand: Could not activate nameTagCraft.");
				e.printStackTrace();
			}
		}
		if (config.blockDropFix) {
			server.getPluginManager().registerEvents(new BlockDropFixListener(), plugin);
		}
		if (config.chatFormat) {
			server.getPluginManager().registerEvents(new ChatFormatterListener(config.chatFormatColorShuffle), plugin);
		}
		if (config.bonemealGrassFix) {
			server.getPluginManager().registerEvents(new BonemealGrassFix(), plugin);
		}
		if (config.fireFix) {
			server.getPluginManager().registerEvents(new FireFix(), plugin);
		}
		if (config.creeperFix) {
			server.getPluginManager().registerEvents(new CreeperFix(), plugin);
		}
		if (config.betterBeds) {
			server.getPluginManager().registerEvents(new BetterBeds(config.minerInsomniaDepth), plugin);
		}
	}

	private static void addRecordRecipe (Server server, Material record, DyeColor dye) {
		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(record));
		recipe.shape(" c ", "cdc", " c ");
		recipe.setIngredient('c', Material.COAL_BLOCK);
		Dye dyeToUse = new Dye(Material.INK_SACK);
		dyeToUse.setColor(dye);
		recipe.setIngredient('d', dyeToUse);

		if (!server.addRecipe(recipe)) {
			System.out.println("DarkyenusCommand: Recipe for " + record.toString() + " could not be added.");
		}
	}

	private static void addHorseArmorRecipe (Server server, Material ingot, Material block, Material result) {
		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(result));
		recipe.shape("i  ", "bwb", "i i");
		recipe.setIngredient('w', Material.WOOL);
		recipe.setIngredient('b', block);
		recipe.setIngredient('i', ingot);

		if (!server.addRecipe(recipe)) {
			System.out.println("DarkyenusCommand: Recipe for " + result + " could not be added.");
		}
	}

	private static final class BlockDropFixListener implements Listener {

		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
		public void onBlockBreak (BlockBreakEvent event) {
			if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
				return;// They are fine in creative!
			}
			try {
				if (event.getBlock().getType() == Material.GLASS) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GLASS));
				} else if (event.getBlock().getType() == Material.THIN_GLASS) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.THIN_GLASS));
				} else if (event.getBlock().getType() == Material.BOOKSHELF) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.BOOKSHELF));
				}
			} catch (Exception e) {
				System.out.println("DarkyenusCommand: blockDropFix crashed.");
				e.printStackTrace();
			}
		}
	}

	private static final class ChatFormatterListener implements Listener {

		private final List<ChatColor> hashedColors = Arrays.asList(ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_AQUA,
			ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.GREEN,
			ChatColor.RED, ChatColor.YELLOW);

		private ChatFormatterListener (boolean shuffle) {
			if (shuffle) {
				Collections.shuffle(hashedColors, new Random(System.currentTimeMillis() / (24 * 60 * 60 * 1000)));// Every day,
// different seed!
			}
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
		// Highest because this changes behavior of chat event
		// and we want it called only after making sure that nobody wants it dead
		public void chatFormatter (AsyncPlayerChatEvent event) {
			ChatColor playerColor = hashedColors.get(Math.abs(event.getPlayer().getDisplayName().hashCode()) % hashedColors.size());

			event.getRecipients().remove(event.getPlayer());
			event.setFormat(playerColor + "<" + ChatColor.WHITE + ChatColor.BOLD + "%s" + ChatColor.RESET + playerColor + "> "
				+ ChatColor.RESET + "%s");
			event.getPlayer().sendMessage(
				String.format(playerColor + "<" + ChatColor.WHITE + ChatColor.BOLD + "%s" + ChatColor.RESET + playerColor + "> "
					+ ChatColor.RESET + "%s" + ChatColor.ITALIC, event.getPlayer().getDisplayName(), event.getMessage()));
		}
	}

	private static final class BonemealGrassFix implements Listener {

		/**  Allow bonemeal to grow grass on dirt. */
		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
		public void onBonemealUse (PlayerInteractEvent event) {
			if (event.getHand() != EquipmentSlot.HAND) return;
			if (event.hasBlock() && event.hasItem() && event.getMaterial() == Material.INK_SACK
				&& ((Dye)(event.getItem().getData())).getColor() == DyeColor.WHITE) {
				if (event.getClickedBlock().getType() == Material.DIRT) {
					event.getClickedBlock().setType(Material.GRASS);
					event.setUseInteractedBlock(Event.Result.DENY);
					event.setUseItemInHand(Event.Result.DENY);
					event.setCancelled(true);
					if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						ItemStack bonemealItemStack = event.getItem();
						int finalAmount = bonemealItemStack.getAmount() - 1;
						if (finalAmount == 0) {
							event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
						} else
							bonemealItemStack.setAmount(finalAmount);
					}
				}
			}
		}
	}

	private static final class FireFix implements Listener {

		/** Limits spread distance only to nearest blocks */
		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
		public void onFireSpread (BlockSpreadEvent event) {
			final Block source = event.getSource();
			final Block fire = event.getBlock();
			if (Math.abs(source.getX() - fire.getX()) > 1 || Math.abs(source.getY() - fire.getY()) > 1
				|| Math.abs(source.getZ() - fire.getZ()) > 1) {
				event.setCancelled(true);
			}
		}
	}

	private static final class CreeperFix implements Listener {

		private static final EnumSet<Material> FRAGILE_MATERIALS = EnumSet.of(
				GLASS,
				STAINED_GLASS,
				THIN_GLASS,
				STAINED_GLASS_PANE,
				SAPLING,
				LEAVES,
				LEAVES_2,
				WEB,
				LONG_GRASS,
				DEAD_BUSH,
				YELLOW_FLOWER,
				RED_ROSE,
				BROWN_MUSHROOM,
				RED_MUSHROOM,
				TNT,
				TORCH,
				REDSTONE_WIRE,
				CROPS,
				WOODEN_DOOR,
				ACACIA_DOOR,
				BIRCH_DOOR,
				JUNGLE_DOOR,
				SPRUCE_DOOR,
				SPRUCE_DOOR,
				TRAP_DOOR,
				LADDER,
				RAILS,
				REDSTONE_TORCH_ON,
				REDSTONE_TORCH_OFF,
				SNOW,
				ICE,
				SNOW_BLOCK,
				CACTUS,
				SUGAR_CANE_BLOCK,
				FENCE,
				FENCE_GATE,
				CAKE_BLOCK,
				DIODE_BLOCK_ON,
				DIODE_BLOCK_OFF,
				HUGE_MUSHROOM_1,
				HUGE_MUSHROOM_2,
				PUMPKIN_STEM,
				MELON_STEM,
				VINE,
				WATER_LILY,
				NETHER_WARTS,
				BREWING_STAND,
				COCOA,
				TRIPWIRE_HOOK,
				TRIPWIRE,
				FLOWER_POT,
				CARROT,
				POTATO,
				WOOD_BUTTON,
				WOOD_PLATE,
				SKULL,
				REDSTONE_COMPARATOR_ON,
				REDSTONE_COMPARATOR_OFF,
				DAYLIGHT_DETECTOR,
				DAYLIGHT_DETECTOR_INVERTED,
				ACTIVATOR_RAIL,
				DETECTOR_RAIL,
				POWERED_RAIL,
				CARPET,
				DOUBLE_PLANT,
				STANDING_BANNER,
				WALL_BANNER,
				END_ROD,
				CHORUS_PLANT,
				CHORUS_FLOWER,
				CHORUS_FRUIT,
				CHORUS_FRUIT_POPPED,
				BEETROOT_BLOCK
		);

		/** Limit creeper explosions only to more fragile blocks */
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void onCreeperExplode (EntityExplodeEvent event) {
			if(event.getEntityType() == EntityType.CREEPER) {
				event.blockList().removeIf(b -> !FRAGILE_MATERIALS.contains(b.getType()));
			}
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void onCreeperDestroyItem (EntityDamageByEntityEvent event) {
			if (event.getEntityType() == EntityType.DROPPED_ITEM && event.getDamager() instanceof Creeper) {
				event.setCancelled(true);
			}
		}
	}

	private static final class BetterBeds implements Listener {
		private final int minerInsomniaDepth;

		private BetterBeds(int minerInsomniaDepth) {
			this.minerInsomniaDepth = minerInsomniaDepth;
		}

		private boolean doesSleep(GameMode mode){
			switch (mode) {
				case CREATIVE:
				case SPECTATOR:
					return false;
				default:
					return true;
			}
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void adminsDoNotSleep(PlayerGameModeChangeEvent event) {
			event.getPlayer().setSleepingIgnored(!doesSleep(event.getNewGameMode()));
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void minersDoNotSleep(PlayerMoveEvent event){
			final Player player = event.getPlayer();
			if (!doesSleep(player.getGameMode())) return;

			final int sleepEdge = minerInsomniaDepth;
			final boolean sleepBefore = event.getFrom().getBlockY() < sleepEdge;
			final boolean sleepNow = event.getTo().getBlockY() < sleepEdge;
			if (sleepBefore != sleepNow) {
				player.setSleepingIgnored(!sleepNow);
			}
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void playersKnowHowManyPeopleAreInBed(PlayerBedEnterEvent event) {
			int totalSleepy = 0;
			int sleeping = 0;
			final Player player = event.getPlayer();
			for (Player p : player.getWorld().getPlayers()) {
				if (p == player) continue;
				if (p.isSleepingIgnored()) continue;
				totalSleepy++;
				if (p.isSleeping()) {
					sleeping++;
				}
			}

			final int remaining = totalSleepy - sleeping;
			if (totalSleepy == 0 || remaining < 1) return;

			final TextComponent component = new TextComponent(ChatColor.DARK_AQUA + "Waiting for " + remaining + " more "+(remaining == 1?"person":"people")+" to sleep...");
			for (Player p : player.getWorld().getPlayers()) {
				if (p.isSleepingIgnored() && !p.isOp()) continue;
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
			}
		}

	}
}
