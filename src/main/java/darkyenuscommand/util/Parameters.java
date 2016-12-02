package darkyenuscommand.util;

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

    public String rest(CharSequence separator) {
        if (eof()) return "";
        final StringBuilder sb = new StringBuilder();
        sb.append(args[index++]);
        while(!eof()) {
            sb.append(separator);
            sb.append(args[index++]);
        }
        return sb.toString();
    }

    public boolean eof() {
        return index >= args.length;
    }
}
