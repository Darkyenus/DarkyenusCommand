
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
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Private property. User: Darkyen Date: 7/30/13 Time: 11:34 AM */
final class FixManager {

	private FixManager () {
	}

	private static final Logger LOG = Logger.getLogger("DarkyenusCommand:FixManager");
	
	private static DarkyenusCommandPlugin plugin;

	@SuppressWarnings("SpellCheckingInspection")
	static void initialize (DarkyenusCommandPlugin plugin) {
		FixManager.plugin = plugin;
		final PluginData config = plugin.data;

		final Server server = plugin.getServer();
		if (config.saddleRecipe) {
			try {
				ShapedRecipe saddleCraftRecipe = new ShapedRecipe(new NamespacedKey(plugin,"saddle"), new ItemStack(Material.SADDLE));
				saddleCraftRecipe.shape("lll", "lil", "i i");
				saddleCraftRecipe.setIngredient('l', Material.LEATHER);
				saddleCraftRecipe.setIngredient('i', Material.IRON_INGOT);
				if (!server.addRecipe(saddleCraftRecipe)) {
					LOG.warning("Saddle recipe could not be added");
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Could not activate saddle crafting", e);
			}
		}
		if (config.recordRecipe) {
			try {
				addRecordRecipe(server, Material.MUSIC_DISC_13, DyeColor.YELLOW);// 13
				addRecordRecipe(server, Material.MUSIC_DISC_CAT, DyeColor.GREEN);// cat
				addRecordRecipe(server, Material.MUSIC_DISC_BLOCKS, DyeColor.ORANGE);// blocks
				addRecordRecipe(server, Material.MUSIC_DISC_CHIRP, DyeColor.RED);// chirp
				addRecordRecipe(server, Material.MUSIC_DISC_FAR, DyeColor.LIME);// far
				addRecordRecipe(server, Material.MUSIC_DISC_MALL, DyeColor.LIGHT_BLUE);// mall
				addRecordRecipe(server, Material.MUSIC_DISC_MELLOHI, DyeColor.MAGENTA);// mellohi
				addRecordRecipe(server, Material.MUSIC_DISC_STAL, DyeColor.BLACK);// stal
				addRecordRecipe(server, Material.MUSIC_DISC_STRAD, DyeColor.WHITE);// strad
				addRecordRecipe(server, Material.MUSIC_DISC_WARD, DyeColor.CYAN);// ward
				addRecordRecipe(server, Material.MUSIC_DISC_11, DyeColor.GRAY);// 11
				addRecordRecipe(server, Material.MUSIC_DISC_WAIT, DyeColor.BLUE);// wait
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Could not activate record crafting", e);
			}
		}
		if (config.horseArmorRecipe) {
			try {
				addHorseArmorRecipe(server, Material.IRON_INGOT, Material.IRON_BLOCK, Material.IRON_HORSE_ARMOR);
				addHorseArmorRecipe(server, Material.GOLD_INGOT, Material.GOLD_BLOCK, Material.GOLDEN_HORSE_ARMOR);
				addHorseArmorRecipe(server, Material.DIAMOND, Material.DIAMOND_BLOCK, Material.DIAMOND_HORSE_ARMOR);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Could not activate horse armor crafting", e);
			}
		}
		if (config.nameTagRecipe) {
			try {
				ShapedRecipe nameTagCraftRecipe = new ShapedRecipe(new NamespacedKey(plugin,"name_tag"), new ItemStack(Material.NAME_TAG));
				nameTagCraftRecipe.shape("  s", " p ", "p  ");
				nameTagCraftRecipe.setIngredient('s', Material.STRING);
				nameTagCraftRecipe.setIngredient('p', Material.PAPER);
				if (!server.addRecipe(nameTagCraftRecipe)) {
					LOG.log(Level.WARNING, "Could not add name tag recipe");
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Could not activate name tag crafting", e);
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
		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "record_"+record), new ItemStack(record));
		recipe.shape(" c ", "cdc", " c ");
		recipe.setIngredient('c', Material.COAL_BLOCK);
		Dye dyeToUse = new Dye(dye);
		recipe.setIngredient('d', dyeToUse);

		if (!server.addRecipe(recipe)) {
			LOG.log(Level.WARNING, "Could not add recipe for " + record);
		}
	}

	private static void addHorseArmorRecipe (Server server, Material ingot, Material block, Material result) {
		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "horse_armor_"+block), new ItemStack(result));
		recipe.shape("i  ", "bwb", "i i");
		recipe.setIngredient('w', new RecipeChoice.MaterialChoice(new ArrayList<>(Materials.WOOL)));
		recipe.setIngredient('b', block);
		recipe.setIngredient('i', ingot);

		if (!server.addRecipe(recipe)) {
			LOG.log(Level.WARNING, "Could not add recipe for "+result);
		}
	}

	private static final class BlockDropFixListener implements Listener {

		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
		public void onBlockBreak (BlockBreakEvent event) {
			if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
				return;// They are fine in creative!
			}
			try {
				final Material blockMaterial = event.getBlock().getType();
				if (Materials.GLASS.contains(blockMaterial) || Materials.GLASS_PANE.contains(blockMaterial) || blockMaterial == Material.BOOKSHELF) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(blockMaterial));
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Could not fix block drops", e);
			}
		}
	}

	private static final class ChatFormatterListener implements Listener {

		private final List<ChatColor> hashedColors = Arrays.asList(ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_AQUA,
			ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.GREEN,
			ChatColor.RED, ChatColor.YELLOW);

		private ChatFormatterListener (boolean shuffle) {
			if (shuffle) {
				// Every day, different seed!
				Collections.shuffle(hashedColors, new Random(System.currentTimeMillis() / (24 * 60 * 60 * 1000)));
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
			if (event.getMaterial() == Material.BONE_MEAL) {
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

		private static final EnumSet<Material> FRAGILE_MATERIALS = EnumSet.noneOf(Material.class);
		static {
			FRAGILE_MATERIALS.addAll(Materials.GLASS);
			FRAGILE_MATERIALS.addAll(Materials.GLASS_PANE);
			FRAGILE_MATERIALS.addAll(Materials.SAPLING);
			FRAGILE_MATERIALS.addAll(Materials.POT);
			FRAGILE_MATERIALS.addAll(Materials.LEAVES);
			FRAGILE_MATERIALS.addAll(Materials.FLOWERS);
			FRAGILE_MATERIALS.add(Material.COBWEB);
			FRAGILE_MATERIALS.add(Material.NETHER_WART);
			FRAGILE_MATERIALS.addAll(Materials.PLANTS);
			FRAGILE_MATERIALS.addAll(Materials.CHORUS_PLANT);
			FRAGILE_MATERIALS.addAll(Materials.MUSHROOMS);
			FRAGILE_MATERIALS.addAll(Materials.HUGE_MUSHROOMS);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_DOORS);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_TRAP_DOORS);
			FRAGILE_MATERIALS.add(Material.LADDER);
			FRAGILE_MATERIALS.add(Material.TNT);
			FRAGILE_MATERIALS.addAll(Materials.TORCHES);
			FRAGILE_MATERIALS.addAll(Materials.RAILS);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_BUTTONS);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_PRESSURE_PLATES);
			FRAGILE_MATERIALS.addAll(Materials.REDSTONE_TORCHES);
			FRAGILE_MATERIALS.add(Material.TRIPWIRE);
			FRAGILE_MATERIALS.add(Material.TRIPWIRE_HOOK);
			FRAGILE_MATERIALS.add(Material.REDSTONE_WIRE);
			FRAGILE_MATERIALS.add(Material.COMPARATOR);
			FRAGILE_MATERIALS.add(Material.REPEATER);

			FRAGILE_MATERIALS.add(Material.ICE);
			FRAGILE_MATERIALS.add(Material.BREWING_STAND);
			FRAGILE_MATERIALS.add(Material.CAKE);
			FRAGILE_MATERIALS.addAll(Materials.SKULLS);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_FENCES);
			FRAGILE_MATERIALS.addAll(Materials.WOODEN_FENCE_GATES);
			FRAGILE_MATERIALS.addAll(Materials.CARPETS);
			FRAGILE_MATERIALS.addAll(Materials.BANNERS);
			FRAGILE_MATERIALS.add(Material.END_ROD);
		}

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

		private boolean neverSleeps(GameMode mode){
			switch (mode) {
				case CREATIVE:
				case SPECTATOR:
					return true;
				default:
					return false;
			}
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void adminsDoNotSleep(PlayerGameModeChangeEvent event) {
			event.getPlayer().setSleepingIgnored(neverSleeps(event.getNewGameMode()));
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void minersDoNotSleep(PlayerMoveEvent event){
			final Player player = event.getPlayer();
			if (neverSleeps(player.getGameMode())) return;

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
