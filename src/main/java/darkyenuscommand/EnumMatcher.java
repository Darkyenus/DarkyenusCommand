
package darkyenuscommand;

import java.util.*;

/**
 *
 */
public final class EnumMatcher<E extends Enum<E>> {

	private final E[] VALUES;
	private final String[] NAMES;

	private EnumMatcher (E[] values) {
		VALUES = values;
		NAMES = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			NAMES[i] = simplify(values[i].toString());
		}
	}

	private static final Map<Class<?>, EnumMatcher<?>> MATCHER_CACHE = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumMatcher<E> matcher (Class<E> type) {
		final EnumMatcher<?> existing = MATCHER_CACHE.get(type);
		if (existing != null) return (EnumMatcher<E>)existing;

		assert type.isEnum();
		final EnumMatcher<E> newMatcher = new EnumMatcher<>(type.getEnumConstants());
		MATCHER_CACHE.put(type, newMatcher);
		return newMatcher;
	}

	public static <E extends Enum<E>> List<E> match (Class<E> type, String searched) {
		return matcher(type).match(searched);
	}

	public static <E extends Enum<E>> E matchOne (Class<E> type, String searched) {
		return matcher(type).matchOne(searched);
	}

	/** Tries to match element from name
	 * @param searched to match from
	 * @return found elements in order of relevance, single item if perfect match, empty list if not found */
	@SuppressWarnings("unchecked")
	public List<E> match (String searched) {
		final List<FindResult> rawResults = matchRaw(searched);
		final E[] values = VALUES;

		if(rawResults.isEmpty()){
			return (List<E>)Collections.EMPTY_LIST;
		} else {
			final int maxResults = 5;
			final ArrayList<E> result = new ArrayList<>();
			for (int i = 0; i < Math.min(maxResults, rawResults.size()); i++) {
				final FindResult findResult = rawResults.get(i);
				result.add(values[findResult.data]);
			}
			return result;
		}
	}

	public E matchOne (String searched) {
		final List<FindResult> rawResults = matchRaw(searched);

		if (rawResults.isEmpty()) {
			return null;
		} else if (rawResults.size() == 1 || rawResults.get(0).relevance != rawResults.get(1).relevance) {
			return VALUES[rawResults.get(0).data];
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<FindResult> matchRaw (String searched) {
		searched = simplify(searched);
		if (searched.isEmpty()) return (List<FindResult>)Collections.EMPTY_LIST;

		final int minimalDistance = 25;
		final List<FindResult> results = new ArrayList<>();

		final String[] names = NAMES;

		for (int i = 0; i < names.length; i++) {
			final String nam = names[i];
			if (nam.equals(searched)) {
				return Collections.singletonList(new FindResult(i, 0));
			}

			// Favor adding chars from removing or changing them
			final int relevance = Util.levenshteinDistance(searched, nam, 7, 1, 10);

			if (relevance < minimalDistance) {
				results.add(new FindResult(i, relevance));
			}
		}

		if (results.isEmpty()) {
			return (List<FindResult>)Collections.EMPTY_LIST;
		} else {
			results.sort(Comparator.naturalOrder());
			return results;
		}
	}

	private static String simplify (String string) {
		StringBuilder fromBuilder = new StringBuilder();
		for (char character : string.toCharArray()) {
			if (Character.isLetterOrDigit(character)) {
				fromBuilder.append(Character.toLowerCase(character));
			}
		}
		return fromBuilder.toString();
	}

	private static final class FindResult implements Comparable<FindResult> {
		public final int data;
		public final int relevance;

		private FindResult (int data, int relevance) {
			this.data = data;
			this.relevance = relevance;
		}

		@Override
		public int compareTo (FindResult o) {
			return relevance - o.relevance;
		}
	}
}
