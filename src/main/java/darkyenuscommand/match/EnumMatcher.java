package darkyenuscommand.match;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public final class EnumMatcher<E extends Enum<E>> implements Function<E, CharSequence> {

	private final String noun;
	private final E[] VALUES;
	private final String[] NAMES;

	public EnumMatcher(String noun, E[] values) {
		this.noun = noun;
		VALUES = values;
		NAMES = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			NAMES[i] = simplify(values[i].toString());
		}
	}

	private String simplify(String string) {
		StringBuilder fromBuilder = new StringBuilder();
		for (char character : string.toCharArray()) {
			if (Character.isLetterOrDigit(character)) {
				fromBuilder.append(Character.toLowerCase(character));
			}
		}
		return fromBuilder.toString();
	}

	@Override
	public String apply(E e) {
		return NAMES[e.ordinal()];
	}

	public Match<E> match(String searched) {
		return MatchUtils.match(noun, VALUES, this, simplify(searched));
	}

	private static final Map<Class<?>, EnumMatcher<?>> MATCHER_CACHE = new HashMap<>();

	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> EnumMatcher<E> matcher (Class<E> type) {
		final EnumMatcher<?> existing = MATCHER_CACHE.get(type);
		if (existing != null) return (EnumMatcher<E>)existing;

		assert type.isEnum();
		final EnumMatcher<E> newMatcher = new EnumMatcher<>(type.getSimpleName(), type.getEnumConstants());
		MATCHER_CACHE.put(type, newMatcher);
		return newMatcher;
	}

	public static <E extends Enum<E>> Match<E> match(Class<E> type, String searched) {
		final EnumMatcher<E> matcher = matcher(type);
		return matcher.match(searched);
	}
}
