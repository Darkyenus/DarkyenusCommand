package darkyenuscommand.systems;

import com.esotericsoftware.jsonbeans.ObjectMap;
import darkyenuscommand.BookUtil;
import darkyenuscommand.command.Cmd;
import darkyenuscommand.util.TextProcessingInput;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Book formatting and notice boards.
 */
public final class InfoSystem implements Listener {

	private final boolean bookNoticeBoardsEnabled;
	private final ObjectMap<Location, NoticeBoard> bookNoticeBoards;

	public InfoSystem(boolean bookNoticeBoardsEnabled, ObjectMap<Location, NoticeBoard> bookNoticeBoards) {
		this.bookNoticeBoardsEnabled = bookNoticeBoardsEnabled;
		this.bookNoticeBoards = bookNoticeBoards;
	}

	enum BookFormatMode {
		HELP
	}

	@Cmd
	public void bookFormat(Player sender, @Cmd.UseDefault BookFormatMode mode) {
		if (mode == BookFormatMode.HELP) {
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
			return;
		}
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

	@EventHandler(ignoreCancelled = true)
	public void interactWithNoticeBoard(PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if (!bookNoticeBoardsEnabled) return;
		final Player player = ev.getPlayer();
		final Block block = ev.getClickedBlock();
		if (block == null || (!Tag.SIGNS.isTagged(block.getType())) || player.isSneaking()) {
			return;
		}
		final Location location = block.getLocation();

		final NoticeBoard noticeBoard = bookNoticeBoards.get(location);
		if(noticeBoard == null) {
			//Maybe player wants to make this sign into notice board?
			final Sign state = (Sign) block.getState();
			final String[] lines = state.getLines();
			if (lines.length == 0) return;
			if (!"[notice]".equalsIgnoreCase(lines[0])) return;
			final ItemStack item = ev.getItem();
			if(item == null || (item.getType() != Material.WRITTEN_BOOK && item.getType() != Material.WRITABLE_BOOK)) return;
			final BookMeta itemMeta = (BookMeta) item.getItemMeta();

			final NoticeBoard board = new NoticeBoard();
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

			bookNoticeBoards.put(location, board);
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
		bookNoticeBoards.remove(event.getBlock().getLocation());
	}


	public static String formatPage(String page) {
		return formatPage(page, null);
	}

	/**
	 * @param signStop null or int[1] that serves as an "out" parameter.
	 *                    Will contain the translated first location of "#~#" in resulting page or -1 if this substring
	 *                    does not appear anywhere in the page.
	 */
	public static String formatPage(String page, int[] signStop) {
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


	public static final class NoticeBoard {
		public UUID owner;
		public String[] pages;
		public long freeForAllAfter;

		private transient ItemStack displayItemCache = null;

		/** @return Fragment to display on the sign */
		public String init(Player owner, List<String> pages) {
			String signFragment = "";
			this.owner = owner.getUniqueId();
			this.pages = new String[pages.size()];
			for (int i = 0; i < this.pages.length; i++) {
				if (i == 0) {
					final int[] stopLocation = {-1};
					final String frontPage = formatPage(pages.get(i), stopLocation);
					this.pages[i] = frontPage;
					if (stopLocation[0] == -1) {
						signFragment = frontPage;
					} else {
						signFragment = frontPage.substring(0, stopLocation[0]);
					}
				} else {
					this.pages[i] = formatPage(pages.get(i));
				}
			}
			this.freeForAllAfter = 0;
			return signFragment;
		}

		public boolean isFreeForAll(){
			return freeForAllAfter < System.currentTimeMillis();
		}

		public void neverFreeForAll() {
			freeForAllAfter = Long.MAX_VALUE;
		}

		public void alwaysFreeForAll() {
			freeForAllAfter = 0;
		}

		public void freeForAllAfterDays(int days) {
			freeForAllAfter = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * days;
		}

		public ItemStack getDisplayItem() {
			if(displayItemCache != null) return displayItemCache;
			return displayItemCache = BookUtil.createBookForDisplay(pages);
		}

	}
}
