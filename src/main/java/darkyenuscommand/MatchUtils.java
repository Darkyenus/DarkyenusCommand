package darkyenuscommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class MatchUtils {
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

    public static <T> MatchResult<T> match(T[] from, Function<T, CharSequence> toString, CharSequence target) {
        final int BAD_SCORE = 1000;

        //Insert only search
        final int[] scores = new int[from.length];
        int goodScores = 0;
        for (int i = 0; i < from.length; i++) {
            final T item = from[i];
            final int score = levenshteinDistance(toString.apply(item), target, BAD_SCORE, 1, BAD_SCORE);
            if(score == 0){
                //Perfect match
                return new MatchResult<>(item);
            } else {
                if(score < BAD_SCORE) {
                    goodScores++;
                }
                scores[i] = score;
            }
        }

        if(goodScores == 1){
            //Clear winner however bad it may be
            for (int i = 0; i < from.length; i++) {
                if(scores[i] < BAD_SCORE) {
                    return new MatchResult<>(from[i]);
                }
            }
            assert false;
        }

        boolean findCanBeUnambiguous = true;

        if (goodScores == 0) {
            //No good scores, search again with weights allowing other edits than adding
            findCanBeUnambiguous = false;
            for (int i = 0; i < from.length; i++) {
                scores[i] =  levenshteinDistance(toString.apply(from[i]), target, 3, 1, 3);
            }
        }

        int bestScore = BAD_SCORE;
        int bestScoreAmbiguityThreshold = BAD_SCORE;
        int bestScoreIndex = -1;
        boolean bestScoreIsUnambiguous = false;

        for (int i = 0; i < from.length; i++) {
            final int score = scores[i];
            if(score < bestScore) {
                bestScoreAmbiguityThreshold = bestScore + Math.max(bestScore / 3, 2);
                bestScoreIsUnambiguous = bestScore < bestScoreAmbiguityThreshold;
                bestScore = score;
                bestScoreIndex = i;
            } else if(score < bestScoreAmbiguityThreshold){
                bestScoreIsUnambiguous = false;
            }
        }

        if (bestScoreIsUnambiguous && findCanBeUnambiguous) {
            return new MatchResult<>(from[bestScoreIndex]);
        } else {
            final List<MatchResultItem> results = new ArrayList<>();
            for (int i = 0; i < from.length; i++) {
                final int score = scores[i];
                if (score < BAD_SCORE) {
                    results.add(new MatchResultItem(i, score));
                }
            }
            Collections.sort(results);

            final int resultItems = Math.min(5, results.size());
            //noinspection unchecked
            final T[] resultArray = (T[]) new Object[resultItems];
            for (int i = 0; i < resultItems; i++) {
                resultArray[i] = from[results.get(i).index];
            }
            return new MatchResult<>(resultArray);
        }
    }

    private static final class MatchResultItem implements Comparable<MatchResultItem> {
        public final int index;
        public final int score;

        private MatchResultItem (int index, int score) {
            this.index = index;
            this.score = score;
        }

        @Override
        public int compareTo (MatchResultItem o) {
            return score - o.score;
        }
    }

    @Deprecated
    public static <T> T findNearest (Iterable<T> selection, Function<T, String> toString, String target, int atLeast) {
        int nearest = Integer.MAX_VALUE;
        T nearestItem = null;
        boolean ambiguous = false;

        for (T s : selection) {
            final int distance = levenshteinDistance(toString.apply(s), target, 7, 1, 10);
            if (distance == 0) {
                return s;
            } else if (distance < nearest) {
                nearest = distance;
                nearestItem = s;
            } else if (distance == nearest) {
                ambiguous = true;
            }
        }
        if (nearest <= atLeast && !ambiguous)
            return nearestItem;
        else
            return null;
    }
}
