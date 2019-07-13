package darkyenuscommand.match;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class EnumMatcher<E extends Enum<E>> {

	@NotNull
	private final String noun;
	@Nullable
	private final Class<E> enumClass;
	@NotNull
	private final EnumPair<E>[] values;

	@SuppressWarnings("unchecked")
	public EnumMatcher(@NotNull String noun, @NotNull E @Nullable[] values) {
		this.noun = noun;

		if (values == null || values.length == 0) {
			this.enumClass = null;
			this.values = (EnumPair<E>[]) new EnumPair[0];
			return;
		}

		final Class<E> declaringClass = values[0].getDeclaringClass();
		enumClass = declaringClass;

		final ArrayList<EnumPair<E>> pairs = new ArrayList<>();
		for (E value : values) {
			final Field field;
			try {
				field = declaringClass.getField(value.name());
			} catch (NoSuchFieldException e) {
				throw new IllegalStateException(declaringClass+" does not have a field named after "+value);
			}
			pairs.add(new EnumPair<>(simplify(value.name()), value));
			final Alt alternates = field.getAnnotation(Alt.class);
			if (alternates != null) {
				for (String altName : alternates.value()) {
					pairs.add(new EnumPair<>(simplify(altName), value));
				}
			}
		}
		this.values = pairs.toArray(new EnumPair[0]);
	}

	private static final class EnumPair<E extends Enum<E>> {
		public final @NotNull String name;
		public final @NotNull E value;

		EnumPair(@NotNull String name, @NotNull E value) {
			this.name = name;
			this.value = value;
		}
	}

	@NotNull
	private String simplify(@NotNull String string) {
		StringBuilder fromBuilder = new StringBuilder(string.length());
		for (char character : string.toCharArray()) {
			if (Character.isLetterOrDigit(character)) {
				fromBuilder.append(Character.toLowerCase(character));
			}
		}
		return fromBuilder.toString();
	}

	@NotNull
	public Match<E> match(@NotNull String searched) {
		final Match<EnumPair<E>> resultMatch = MatchUtils.match(noun, values, e -> e.name, simplify(searched));
		return resultMatch.map(enumClass, pair -> pair.value, Enum::toString);
	}

	private static final Map<Class<?>, EnumMatcher<?>> MATCHER_CACHE = new HashMap<>();

	@SuppressWarnings("unchecked")
	@NotNull
	private static <E extends Enum<E>> EnumMatcher<E> matcher (@NotNull Class<E> type) {
		final EnumMatcher<?> existing = MATCHER_CACHE.get(type);
		if (existing != null) return (EnumMatcher<E>)existing;

		assert type.isEnum();
		final EnumMatcher<E> newMatcher = new EnumMatcher<>(type.getSimpleName(), type.getEnumConstants());
		MATCHER_CACHE.put(type, newMatcher);
		return newMatcher;
	}

	@NotNull
	public static <E extends Enum<E>> Match<E> match(@NotNull Class<E> type, @NotNull String searched) {
		final EnumMatcher<E> matcher = matcher(type);
		return matcher.match(searched);
	}
}
