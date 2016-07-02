
package darkyenuscommand;

import java.util.function.Function;

/**
 *
 */
public class Util {

	public static int levenshteinDistance (CharSequence s0, CharSequence s1, int replaceCost, int insertCost, int deleteCost) {
		int len0 = s0.length() + 1;
		int len1 = s1.length() + 1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newCost = new int[len0];

		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i * insertCost;

		// dynamically computing the array of distances

		// transformation cost for each letter in s1
		for (int i1 = 1; i1 < len1; i1++) {
			// initial cost of skipping prefix in String s1
			newCost[0] = i1 * deleteCost;

			// transformation cost for each letter in s0
			for (int i0 = 1; i0 < len0; i0++) {
				// matching current letters in both strings
				int match = (s0.charAt(i0 - 1) == s1.charAt(i1 - 1)) ? 0 : replaceCost;

				// computing cost for each transformation
				int cost_replace = cost[i0 - 1] + match;
				int cost_insert = cost[i0] + insertCost;
				int cost_delete = newCost[i0 - 1] + deleteCost;

				// keep minimum cost
				newCost[i0] = min(cost_insert, cost_delete, cost_replace);
			}

			// swap cost/newCost arrays
			int[] swap = cost;
			cost = newCost;
			newCost = swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}

	private static int min (int a, int b, int c) {
		if (a <= b && a <= c) return a;
		if (b <= a && b <= c) return b;
		return c;
	}

	public static <T> T findNearest (Iterable<T> selection, Function<T, String> toString, String target, int atLeast) {
		int nearest = Integer.MAX_VALUE;
		T nearestItem = null;
		for (T s : selection) {
			final int distance = levenshteinDistance(toString.apply(s), target, 7, 1, 10);
			if (distance == 0) {
				return s;
			} else if (distance < nearest) {
				nearest = distance;
				nearestItem = s;
			}
		}
		if (nearest <= atLeast)
			return nearestItem;
		else
			return null;
	}
}
