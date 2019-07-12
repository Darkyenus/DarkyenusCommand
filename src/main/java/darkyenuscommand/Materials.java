package darkyenuscommand;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.EnumSet;

/**
 * Various sets of material related utilities.
 */
public final class Materials {

	public static final EnumSet<Material> GLASS_PANE = EnumSet.of(
			Material.BLACK_STAINED_GLASS_PANE,
			Material.BLUE_STAINED_GLASS_PANE,
			Material.BROWN_STAINED_GLASS_PANE,
			Material.CYAN_STAINED_GLASS_PANE,
			Material.GLASS_PANE,
			Material.GRAY_STAINED_GLASS_PANE,
			Material.GREEN_STAINED_GLASS_PANE,
			Material.LIGHT_BLUE_STAINED_GLASS_PANE,
			Material.LIGHT_GRAY_STAINED_GLASS_PANE,
			Material.LIME_STAINED_GLASS_PANE,
			Material.MAGENTA_STAINED_GLASS_PANE,
			Material.ORANGE_STAINED_GLASS_PANE,
			Material.PINK_STAINED_GLASS_PANE,
			Material.PURPLE_STAINED_GLASS_PANE,
			Material.RED_STAINED_GLASS_PANE,
			Material.WHITE_STAINED_GLASS_PANE,
			Material.YELLOW_STAINED_GLASS_PANE
	);

	public static final EnumSet<Material> GLASS = EnumSet.of(
			Material.BLACK_STAINED_GLASS,
			Material.BLUE_STAINED_GLASS,
			Material.BROWN_STAINED_GLASS,
			Material.CYAN_STAINED_GLASS,
			Material.GLASS,
			Material.GRAY_STAINED_GLASS,
			Material.GREEN_STAINED_GLASS,
			Material.LIGHT_BLUE_STAINED_GLASS,
			Material.LIGHT_GRAY_STAINED_GLASS,
			Material.LIME_STAINED_GLASS,
			Material.MAGENTA_STAINED_GLASS,
			Material.ORANGE_STAINED_GLASS,
			Material.PINK_STAINED_GLASS,
			Material.PURPLE_STAINED_GLASS,
			Material.RED_STAINED_GLASS,
			Material.WHITE_STAINED_GLASS,
			Material.YELLOW_STAINED_GLASS
	);

	public static final EnumSet<Material> SAPLING = EnumSet.of(
			Material.ACACIA_SAPLING,
			Material.BAMBOO_SAPLING,
			Material.BIRCH_SAPLING,
			Material.DARK_OAK_SAPLING,
			Material.JUNGLE_SAPLING,
			Material.OAK_SAPLING,
			Material.SPRUCE_SAPLING
	);

	public static final EnumSet<Material> FLOWERS = EnumSet.of(
			Material.DANDELION,
			Material.POPPY,
			Material.BLUE_ORCHID,
			Material.ALLIUM,
			Material.AZURE_BLUET,
			Material.RED_TULIP,
			Material.ORANGE_TULIP,
			Material.WHITE_TULIP,
			Material.PINK_TULIP,
			Material.OXEYE_DAISY,
			Material.CORNFLOWER,
			Material.LILY_OF_THE_VALLEY,
			Material.WITHER_ROSE,
			Material.SUNFLOWER,
			Material.LILAC,
			Material.ROSE_BUSH,
			Material.PEONY
	);

	public static final EnumSet<Material> PLANTS = EnumSet.of(
			Material.BAMBOO,
			Material.BEETROOTS,
			Material.BEETROOT_SEEDS,
			Material.CACTUS,
			Material.CARROTS, //"carrot" is item
			Material.COCOA,
			Material.GRASS,
			Material.FERN,
			Material.TALL_GRASS,
			Material.LARGE_FERN,
			Material.LILY_PAD,
			Material.MELON,
			Material.MELON_STEM,
			Material.ATTACHED_MELON_STEM,
			Material.POTATOES, // "potato" is item
			Material.PUMPKIN,
			Material.CARVED_PUMPKIN,
			Material.PUMPKIN_SEEDS,
			Material.PUMPKIN_STEM,
			Material.ATTACHED_PUMPKIN_STEM,
			Material.SEAGRASS,
			Material.TALL_SEAGRASS,
			Material.SUGAR_CANE,
			Material.SWEET_BERRY_BUSH,
			Material.VINE,
			Material.WHEAT,
			Material.DEAD_BUSH
	);

	public static final EnumSet<Material> MUSHROOMS = EnumSet.of(
			Material.BROWN_MUSHROOM,
			Material.RED_MUSHROOM
	);
	public static final EnumSet<Material> HUGE_MUSHROOMS = EnumSet.of(
			Material.BROWN_MUSHROOM_BLOCK,
			Material.RED_MUSHROOM_BLOCK,
			Material.MUSHROOM_STEM
	);

	public static final EnumSet<Material> CHORUS_PLANT = EnumSet.of(
			Material.CHORUS_FLOWER,
			Material.CHORUS_FRUIT,
			Material.CHORUS_PLANT,
			Material.POPPED_CHORUS_FRUIT
	);

	public static final EnumSet<Material> WOODEN_DOORS = EnumSet.of(
			Material.ACACIA_DOOR,
			Material.BIRCH_DOOR,
			Material.DARK_OAK_DOOR,
			Material.JUNGLE_DOOR,
			Material.OAK_DOOR,
			Material.SPRUCE_DOOR
	);

	public static final EnumSet<Material> WOODEN_TRAP_DOORS = EnumSet.of(
			Material.ACACIA_TRAPDOOR,
			Material.BIRCH_TRAPDOOR,
			Material.DARK_OAK_TRAPDOOR,
			Material.IRON_TRAPDOOR,
			Material.JUNGLE_TRAPDOOR,
			Material.OAK_TRAPDOOR,
			Material.SPRUCE_TRAPDOOR
	);

	public static final EnumSet<Material> RAILS = EnumSet.of(
			Material.ACTIVATOR_RAIL,
			Material.DETECTOR_RAIL,
			Material.POWERED_RAIL,
			Material.RAIL
	);

	public static final EnumSet<Material> WOODEN_BUTTONS = EnumSet.of(
			Material.ACACIA_BUTTON,
			Material.BIRCH_BUTTON,
			Material.DARK_OAK_BUTTON,
			Material.JUNGLE_BUTTON,
			Material.OAK_BUTTON,
			Material.SPRUCE_BUTTON
	);

	public static final EnumSet<Material> WOODEN_PRESSURE_PLATES = EnumSet.of(
			Material.ACACIA_PRESSURE_PLATE,
			Material.BIRCH_PRESSURE_PLATE,
			Material.DARK_OAK_PRESSURE_PLATE,
			Material.JUNGLE_PRESSURE_PLATE,
			Material.OAK_PRESSURE_PLATE,
			Material.SPRUCE_PRESSURE_PLATE
	);

	public static final EnumSet<Material> TORCHES = EnumSet.of(
			Material.TORCH,
			Material.WALL_TORCH
	);

	public static final EnumSet<Material> REDSTONE_TORCHES = EnumSet.of(
			Material.REDSTONE_TORCH,
			Material.REDSTONE_WALL_TORCH
	);

	public static final EnumSet<Material> SKULLS = EnumSet.of(
			Material.SKELETON_SKULL,
			Material.WITHER_SKELETON_SKULL,
			Material.ZOMBIE_HEAD,
			Material.PLAYER_HEAD,
			Material.CREEPER_HEAD,
			Material.DRAGON_HEAD,
			Material.SKELETON_WALL_SKULL,
			Material.WITHER_SKELETON_WALL_SKULL,
			Material.ZOMBIE_WALL_HEAD,
			Material.PLAYER_WALL_HEAD,
			Material.CREEPER_WALL_HEAD,
			Material.DRAGON_WALL_HEAD
	);

	public static final EnumSet<Material> WOODEN_FENCES = EnumSet.of(
			Material.ACACIA_FENCE,
			Material.BIRCH_FENCE,
			Material.DARK_OAK_FENCE,
			Material.JUNGLE_FENCE,
			Material.OAK_FENCE,
			Material.SPRUCE_FENCE
	);

	public static final EnumSet<Material> WOODEN_FENCE_GATES = EnumSet.of(
			Material.ACACIA_FENCE_GATE,
			Material.BIRCH_FENCE_GATE,
			Material.DARK_OAK_FENCE_GATE,
			Material.JUNGLE_FENCE_GATE,
			Material.OAK_FENCE_GATE,
			Material.SPRUCE_FENCE_GATE
	);

	public static final EnumSet<Material> CARPETS = EnumSet.of(
			Material.BLACK_CARPET,
			Material.BLUE_CARPET,
			Material.BROWN_CARPET,
			Material.CYAN_CARPET,
			Material.GRAY_CARPET,
			Material.GREEN_CARPET,
			Material.LIGHT_BLUE_CARPET,
			Material.LIGHT_GRAY_CARPET,
			Material.LIME_CARPET,
			Material.MAGENTA_CARPET,
			Material.ORANGE_CARPET,
			Material.PINK_CARPET,
			Material.PURPLE_CARPET,
			Material.RED_CARPET,
			Material.WHITE_CARPET,
			Material.YELLOW_CARPET
	);
}
