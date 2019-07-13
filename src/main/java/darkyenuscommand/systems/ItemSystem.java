package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import darkyenuscommand.match.Alt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class ItemSystem {

	@Cmd
	public void item(CommandSender sender, @Cmd.UseImplicit Player player){
		ItemStack item = player.getInventory().getItemInMainHand();
		sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Item Info");
		sender.sendMessage(ChatColor.BLUE.toString() + "Material: " + item.getType()
				.toString());
		final ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable && ((Damageable) meta).hasDamage()) {
			sender.sendMessage(ChatColor.BLUE.toString() + "Damage: " + ((Damageable) meta).getDamage());
		}
		sender.sendMessage(ChatColor.BLUE.toString() + "Amount: " + item.getAmount());
		for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
			sender.sendMessage(ChatColor.BLUE.toString() + " " + entry.getKey().getKey() + " - " + entry.getValue());
		}
	}

	@Cmd
	public void item(CommandSender sender, Material material, @Cmd.UseDefault int amount, @Cmd.UseImplicit Player toPlayer) {
		if (!material.isItem()) {
			sender.sendMessage(ChatColor.RED + "Material " + material + " can not be created as item");
			return;
		}

		if (amount < 0) {
			sender.sendMessage(ChatColor.RED + "Invalid amount");
			return;
		} else if (amount == 0) {
			amount = 1;
		}

		int remainingToGive = amount;
		int given = 0;

		final PlayerInventory inventory = toPlayer.getInventory();
		while (remainingToGive > 0) {
			final int itemStackSize = Math.min(material.getMaxStackSize(), remainingToGive);
			final ItemStack item = new ItemStack(material, itemStackSize);

			final HashMap<Integer, ItemStack> rejected = inventory.addItem(item);
			int givenNow = itemStackSize;
			for (ItemStack rejectedItem : rejected.values()) {
				givenNow -= rejectedItem.getAmount();
			}
			if (givenNow == 0) {
				break;
			}
			given += givenNow;
			remainingToGive -= givenNow;
		}

		if (given == 0) {
			sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.ITALIC + toPlayer
					.getName() + "'s inventory is full");
		} else {
			sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + toPlayer
					.getName() + " was given " + given
					+ " " + material);
		}
	}

	@Cmd
	public void repair(CommandSender sender, @Cmd.UseImplicit Player player) {
		final ItemStack itemStackToFix = player.getInventory().getItemInMainHand();
		if (itemStackToFix.getType() == Material.AIR) {
			sender.sendMessage(ChatColor.RED.toString() + "Not holding anything");
			return;
		}

		ItemMeta itemMeta = itemStackToFix.getItemMeta();
		if (itemMeta instanceof Damageable) {
			((Damageable) itemMeta).setDamage(0);
			itemStackToFix.setItemMeta(itemMeta);
			player.getInventory().setItemInMainHand(itemStackToFix);
			sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + player.getName() + "'s "
					+ itemStackToFix.getType().getKey() + " has been repaired");
		} else {
			sender.sendMessage(ChatColor.RED.toString() + itemStackToFix.getType().getKey() + " is not repairable");
		}
	}

	enum TrashWhat {
		@Alt("SINGLE")
		ITEM,
		@Alt({"ACTION_BAR", "BAR"})
		HOT_BAR,
		INVENTORY,
		ARMOR,
		@Alt("FULL")
		ALL
	}

	@Cmd
	public void trash(CommandSender sender, TrashWhat trashWhat, @Cmd.UseImplicit Player player) {
		if (!player.equals(sender) && !sender.hasPermission("darkyenuscommand.command.trash.anyone")) {
			sender.sendMessage(ChatColor.RED+"You do not have a permission to do that");
			return;
		}

		final String what;

		switch (trashWhat) {
			case ITEM:
			{
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
				what = "held item";
			}
				break;
			case HOT_BAR:
			{
				for (int i = 0; i <= 8; i++) {
					player.getInventory().clear(i);
				}
				what = "hotbar";
			}
				break;
			case INVENTORY:
			{
				for (int i = 0; i <= 35; i++) {
					player.getInventory().clear(i);
				}
				what = "inventory";
			}
				break;
			case ARMOR:
			{
				player.getInventory().setArmorContents(new ItemStack[4]);
				what = "armor";
			}
				break;
			case ALL:
			{
				for (int i = 0; i <= 35; i++) {
					player.getInventory().clear(i);
				}
				player.getInventory().setArmorContents(new ItemStack[4]);
				what = "inventory including armor";
			}
				break;
			default:
				what = "nothing";
		}
		player.updateInventory();

		final String whose = player.equals(sender) ? "Your" : (player.getName() + "'s");
		sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + whose +" "+what+" thrown away");
	}

}
