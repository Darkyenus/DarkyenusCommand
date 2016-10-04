package darkyenuscommand.util;

/**
 *
 */
public class TextProcessingInput {

    private int[] codePoints = new int[512];
    private int length = 0;
    private int position = 0;

    private void addCodePoint(int cp) {
        if(length == codePoints.length) {
            int[] newCodePoints = new int[codePoints.length << 1];
            System.arraycopy(codePoints, 0, newCodePoints, 0, length);
            codePoints = newCodePoints;
        }
        codePoints[length] = cp;
        length++;
    }

    private boolean validCodePoint(int cp) {
        return Character.isValidCodePoint(cp) && Character.isDefined(cp);
    }

    public TextProcessingInput(String from) {
        from.codePoints().forEach(cp -> {
            if(validCodePoint(cp)) {
                addCodePoint(cp);
            }
        });
    }

    public int peek() {
        if(position >= length) {
            return -1;
        }
        return codePoints[position];
    }

    public int pop() {
        if(position >= length) {
            return -1;
        }
        return codePoints[position++];
    }

    public boolean peekEqualsIgnoreCaseAndPop(CharSequence cs){
        return peekEquals(cs, true, true);
    }

    public boolean peekEquals(CharSequence cs, boolean ignoreCase, boolean andPop){
        if(position + cs.length() <= length) {
            for (int i = 0; i < cs.length(); i++) {
                if (ignoreCase) {
                    if (Character.toLowerCase(codePoints[position+i]) != Character.toLowerCase(cs.charAt(i))) return false;
                } else {
                    if (codePoints[position+i] != cs.charAt(i)) return false;
                }
            }
            if(andPop) {
                position += cs.length();
            }
            return true;
        } else {
            return false;
        }
    }

}
