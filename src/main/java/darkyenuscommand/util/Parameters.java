package darkyenuscommand.util;

import darkyenuscommand.MatchUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 */
public final class Parameters {
    private final String[] args;
    private int index;

    public Parameters(String[] args) {
        this.args = args;
        index = 0;
    }

    public String take(String eofValue) {
        if (eof()) return eofValue;
        else {
            return args[index++];
        }
    }

    public boolean match(String...text) {
        if(index >= args.length) return false;

        for (String t : text) {
            if(t.equalsIgnoreCase(args[index])) {
                index++;
                return true;
            }
        }

        return false;
    }

    public int matchInt(int failValue) {
        if(index >= args.length) return failValue;
        try {
            final int result = Integer.parseInt(args[index]);
            index++;
            return result;
        } catch (NumberFormatException ex) {
            return failValue;
        }
    }

    private boolean peekCanBePlayer() {
        if (eof()) return false;
        final String peek = args[index];
        if (peek.length() < 3 || peek.length() > 16) return false;
        for (int i = 0; i < peek.length(); i++) {
            final char c = peek.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_'))) {
                return false;
            }
        }
        return true;
    }

    public Player matchPlayer (Player eofValue, CommandSender reportErrorsTo) {
        if (!peekCanBePlayer()) {
            if (eofValue == null && reportErrorsTo != null) {
                reportErrorsTo.sendMessage(ChatColor.RED+"You must specify a player");
            }
            return eofValue;
        } else {
            final String name = take("");
            final MatchUtils.MatchResult<OfflinePlayer> playerMatch = MatchUtils.matchPlayer(name);
            if (playerMatch.isDefinite) {
                final OfflinePlayer offlinePlayer = playerMatch.result();
                final Player player = offlinePlayer.getPlayer();
                if (player != null) {
                    return player;
                } else {
                    if(reportErrorsTo != null) reportErrorsTo.sendMessage(ChatColor.RED+offlinePlayer.getName()+" is offline");
                    return null;
                }
            } else {
                MatchUtils.matchPlayerFail(playerMatch, reportErrorsTo);
                return null;
            }
        }
    }

    public String rest(CharSequence separator) {
        return collect(separator, Integer.MAX_VALUE);
    }

    public String collect(CharSequence separator, int elements) {
        if (eof()) return "";
        final StringBuilder sb = new StringBuilder();
        sb.append(args[index++]);
        elements--;
        while(!eof() && elements > 0) {
            sb.append(separator);
            sb.append(args[index++]);
            elements--;
        }
        return sb.toString();
    }

    public boolean eof() {
        return index >= args.length;
    }

    public int remaining() {
        return args.length - index;
    }
}
