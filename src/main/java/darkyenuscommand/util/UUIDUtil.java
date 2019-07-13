package darkyenuscommand.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 *
 */
public class UUIDUtil {

    @Nullable
    public static UUID parseUUID(@NotNull String from) {
        String[] components = from.split("-");
        if (components.length != 5)
            return null;

        long mostSigBits = Long.parseLong(components[0], 16);
        mostSigBits <<= 16;
        mostSigBits |= Long.parseLong(components[1], 16);
        mostSigBits <<= 16;
        mostSigBits |= Long.parseLong(components[2], 16);

        long leastSigBits = Long.parseLong(components[3], 16);
        leastSigBits <<= 48;
        leastSigBits |= Long.parseLong(components[4], 16);

        return new UUID(mostSigBits, leastSigBits);
    }

}
