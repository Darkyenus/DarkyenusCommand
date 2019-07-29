package darkyenuscommand.systems;

import darkyenuscommand.command.Cmd;
import darkyenuscommand.util.TextProcessingInput;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Book formatting and notice boards.
 */
public final class InfoSystem implements Listener {

	@Cmd
	public void bookFormat(@NotNull Player sender, @Cmd.OneOf("help") String mode) {
		sender.sendMessage(ChatColor.BLUE + "Book Formatting Rules");
		sender.sendMessage(ChatColor.AQUA + "Existing color tags are stripped and then control codes are interpreted.");
		sender.sendMessage(ChatColor.AQUA + "Control code is in form #?#, where ? is one of codes below. \\# is interpreted as single # without interpreting anything.");
		final StringBuilder colorsSB = new StringBuilder();
		for (ChatColor chatColor : ChatColor.values()) {
			if (chatColor == ChatColor.RESET) continue;
			colorsSB.append(chatColor.asBungee().getName()).append('/').append(chatColor.getChar())
					.append(", ");
		}
		colorsSB.setLength(colorsSB.length() - 2);//Strip extra ", "
		sender.sendMessage(ChatColor.ITALIC + colorsSB
				.toString() + ChatColor.RESET + ": Are color codes, in long form/alias form");
		sender.sendMessage(ChatColor.ITALIC + "reset/r/" + ChatColor.RESET + ": Clear color code and mode. Also triggered by ##.");
		sender.sendMessage(ChatColor.ITALIC + "full_width/fw" + ChatColor.RESET + ": Switch to mode in which all (ASCII) characters are converted to full width variants");
		sender.sendMessage(ChatColor.ITALIC + "runic/rc" + ChatColor.RESET + ": Switch to mode in which all basic characters are converted to runes");
	}

	@Cmd
	public void bookFormat(@NotNull Player sender) {
		final PlayerInventory inventory = sender.getInventory();
		final ItemStack item = inventory.getItemInMainHand();
		final ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta instanceof BookMeta) {
			final BookMeta bookMeta = (BookMeta) itemMeta;
			for (int i = 1; i <= bookMeta.getPageCount(); i++) {
				bookMeta.setPage(i, formatPage(bookMeta.getPage(i)));
			}
			item.setItemMeta(bookMeta);
			inventory.setItemInMainHand(item);//Just to refresh in client UI

			sender.sendMessage(ChatColor.BLUE + "" + bookMeta.getPageCount() + " pages formatted");
		} else {
			sender.sendMessage(ChatColor.RED + "Held item is not a book");
		}
	}

	@NotNull
	public static String formatPage(@NotNull String page) {
		return formatPage(page, null);
	}

	/**
	 * @param signStop null or int[1] that serves as an "out" parameter.
	 *                    Will contain the translated first location of "#~#" in resulting page or -1 if this substring
	 *                    does not appear anywhere in the page.
	 */
	@NotNull
	public static String formatPage(@NotNull String page, int[] signStop) {
		if (signStop != null) {
			assert signStop.length == 1 : "Length of sign stop must be exactly 1";
			signStop[0] = -1;
		}
		final TextProcessingInput in = new TextProcessingInput(page);
		final StringBuilder sb = new StringBuilder();

		final byte MODE_NORMAL = 1;
		final byte MODE_FULL_WIDTH = 2;
		final byte MODE_RUNIC = 3;

		byte mode = MODE_NORMAL;

		charWhile:
		while(true) {
			final int c = in.pop();

			if(c == -1) {
				break;
			} else if(c == ChatColor.COLOR_CHAR) {
				in.pop();
			} else if(c == '\n' || c == '\r') {
				while(sb.length() != 0 && Character.isWhitespace(sb.charAt(sb.length()-1)) && sb.charAt(sb.length()-1) != '\n'){
					final int length = sb.length();
					if (signStop != null && signStop[0] == length) {
						signStop[0]--;
					}
					sb.setLength(length - 1);
				}
				sb.append('\n');
			} else if(c == '#') {
				if(in.peekEqualsIgnoreCaseAndPop("#")) {
					sb.append(ChatColor.RESET.toString());
					mode = MODE_NORMAL;
				} else {
					for (ChatColor color : ChatColor.values()) {
						if(in.peekEqualsIgnoreCaseAndPop(color.asBungee().getName()+"#") || in.peekEqualsIgnoreCaseAndPop(color.getChar()+"#")) {
							sb.append(color.toString());
							if(color == ChatColor.RESET) {
								mode = MODE_NORMAL;
							}
							continue charWhile;
						}
					}
					if (in.peekEqualsIgnoreCaseAndPop("~#")) {
						if (signStop != null && signStop[0] == -1) {
							signStop[0] = sb.length();
						}
					} else if(in.peekEqualsIgnoreCaseAndPop("full_width#") || in.peekEqualsIgnoreCaseAndPop("fw#")) {
						mode = MODE_FULL_WIDTH;
					} else if(in.peekEqualsIgnoreCaseAndPop("runic#") || in.peekEqualsIgnoreCaseAndPop("rc#")) {
						mode = MODE_RUNIC;
					} else {
						sb.append('#');
					}
				}
			} else {
				int cc = c;
				if(cc == '\\') {
					if(in.peekEqualsIgnoreCaseAndPop("#")){
						cc = '#';
					}
				}
				final int printC;
				switch (mode) {
					case MODE_NORMAL:
						printC = cc;
						break;
					case MODE_FULL_WIDTH:
						if(cc >= 0x21 && cc <= 0x7E) {
							printC = (cc - 0x21) + 0xFF01;
						} else {
							printC = cc;
						}
						break;
					case MODE_RUNIC:
						//TODO Faux-mapping
						if(cc >= 'a' && cc <= 'z') {
							printC = (cc - 'a') + 0x16A0;
						} else if(cc >= 'A' && cc <= 'Z') {
							printC = (cc - 'A') + 0x16A0 + ('z' - 'a');
						} else if(cc >= '0' && cc <= '9') {
							printC = (cc - '0') + 0x16A0 + ('z' - 'a') + ('Z' - 'A');
						} else {
							printC = cc;
						}
						break;
					default:
						throw new IllegalStateException("Unknown mode "+mode);
				}
				sb.appendCodePoint(printC);
			}
		}
		return sb.toString();
	}
}
