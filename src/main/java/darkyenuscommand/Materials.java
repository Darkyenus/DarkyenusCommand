package darkyenuscommand;

import org.bukkit.Material;

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

	public static final EnumSet<Material> LARGE_FLOWERS = EnumSet.of(
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

	public static final EnumSet<Material> WOODEN_FENCE_GATES = EnumSet.of(
			Material.ACACIA_FENCE_GATE,
			Material.BIRCH_FENCE_GATE,
			Material.DARK_OAK_FENCE_GATE,
			Material.JUNGLE_FENCE_GATE,
			Material.OAK_FENCE_GATE,
			Material.SPRUCE_FENCE_GATE
	);
}
