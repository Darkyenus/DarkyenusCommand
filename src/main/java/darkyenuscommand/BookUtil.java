package darkyenuscommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 *
 */
public class BookUtil {

    private static Plugin plugin;

    public static void initialize(Plugin plugin) {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "MC|BOpen");
        BookUtil.plugin = plugin;
    }

    public static ItemStack createBookForDisplay(String...pages) {
        final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta itemMeta = (BookMeta) book.getItemMeta();
        itemMeta.setDisplayName("_");
        //Minecraft is buggy and resets don't work for colors. So we need to make it work explicitly.
        for (String page : pages) {
            itemMeta.addPage(page.replace(ChatColor.RESET.toString(), ChatColor.RESET.toString() + ChatColor.BLACK.toString()));
        }
        //itemMeta.addPage(pages);
        book.setItemMeta(itemMeta);
        return book;
    }

    public static boolean displayBook(Player player, ItemStack book) {
        if(!addChannel(player, "MC|BOpen")){
            return false;
        }

        if(plugin == null) {
            Bukkit.getLogger().log(Level.WARNING, "displayBook called before initialize!");
            return false;
        }

        //Important workaround, server would think that the player is still in own inventory, while actually being in the book!
        player.closeInventory();

        final ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        try {
            player.getInventory().setItemInMainHand(book);
            player.sendPluginMessage(plugin, "MC|BOpen", new byte[]{0});
        } finally {
            player.getInventory().setItemInMainHand(itemInMainHand);
        }
        return true;
    }

    private static boolean addChannel(Player player, String channel) {
        try {
            final Method addChannel = player.getClass().getMethod("addChannel", String.class);
            addChannel.invoke(player, channel);
            return true;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to add channel", e);
            return false;
        }
    }

}
