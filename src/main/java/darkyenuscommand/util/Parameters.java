package darkyenuscommand.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 *
 */
@SuppressWarnings("unused")
public final class Parameters {
    public final String[] args;
    public int index;
    public final int end;

    public Parameters(@NotNull String[] args) {
        this(args, 0, args.length);
    }

    public Parameters(@NotNull String[] args, int index, int end) {
        this.args = args;
        this.index = index;
        this.end = end;
    }

    public int mark() {
        return index;
    }

    public void rollback(int index) {
        this.index = index;
    }

    @Nullable
    public String peek() {
        if (eof()) return null;

        return args[index];
    }

    @Nullable
    @Contract("!null -> !null")
    public String take(@Nullable String eofValue) {
        if (eof()) return eofValue;

        return args[index++];
    }

    public boolean match(@NotNull String...text) {
        if(eof()) return false;

        for (String t : text) {
            if(t.equalsIgnoreCase(args[index])) {
                index++;
                return true;
            }
        }

        return false;
    }

    public int matchInt(int failValue) {
        if(eof()) return failValue;

        try {
            final int result = Integer.parseInt(args[index]);
            index++;
            return result;
        } catch (NumberFormatException ex) {
            return failValue;
        }
    }

    @NotNull
    public String rest(@NotNull CharSequence separator) {
        return collect(separator, Integer.MAX_VALUE);
    }

    @NotNull
    public String collect(@NotNull CharSequence separator, int elements) {
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
        return index >= end;
    }

    public int remaining() {
        return Math.max(end - index, 0);
    }

    @Contract("-> new")
    @NotNull
    public Parameters copy() {
        return new Parameters(Arrays.copyOfRange(args, index, end, String[].class));
    }
}
