package darkyenuscommand.match;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public final class MatchUtils {

    /** @param s0 text user entered
     *  @param s1 text of result */
    public static int levenshteinDistance(CharSequence s0, CharSequence s1, int insertCost, int replaceCost, int deleteCost) {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newCost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i * deleteCost;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int i1 = 1; i1 < len1; i1++) {
            // initial cost of skipping prefix in String s1
            newCost[0] = i1 * insertCost;

            // transformation cost for each letter in s0
            for (int i0 = 1; i0 < len0; i0++) {
                // matching current letters in both strings
                int match = (s0.charAt(i0 - 1) == s1.charAt(i1 - 1)) ? 0 : replaceCost;

                // computing cost for each transformation
                int cost_insert = cost[i0] + insertCost; //          \/
                int cost_replace = cost[i0 - 1] + match; //          _|
                int cost_delete = newCost[i0 - 1] + deleteCost; //   >

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

    private static boolean contentEquals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        final int length = a.length();
        if (length != b.length()) return false;
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) return false;
        }
        return true;
    }

    public static <T> Match<T> match(String noun, T[] from, Function<T, CharSequence> toString, CharSequence target) {
        final int BAD_SCORE = 1000;
        final CharSequence[] fromString = new CharSequence[from.length];
        for (int i = 0; i < from.length; i++) {
            fromString[i] = toString.apply(from[i]);
        }

        //Insert only search
        boolean considerOnlyPerfectMatches = false;
        final int[] scores = new int[from.length];
        int goodScores = 0;
        for (int i = 0; i < from.length; i++) {
            final CharSequence itemName = fromString[i];
            if (considerOnlyPerfectMatches) {
                if(contentEquals(target, itemName)) {
                    scores[i] = 0;
                    goodScores++;
                } else {
                    scores[i] = BAD_SCORE;
                }
            } else {
                final int score = levenshteinDistance(target, itemName, 1, BAD_SCORE, BAD_SCORE);
                if(score == 0){
                    //Perfect match, continue searching, there may be dupes
                    considerOnlyPerfectMatches = true;
                }
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
                    return Match.success(from[i]);
                }
            }
            assert false;
        }

        boolean findCanBeUnambiguous = true;

        if (goodScores == 0) {
            //No good scores, search again with weights allowing other edits than adding
            findCanBeUnambiguous = false;
            for (int i = 0; i < from.length; i++) {
                scores[i] =  levenshteinDistance(target, fromString[i], 1, 6, 3);
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
            return Match.success(from[bestScoreIndex]);
        } else {
            final List<MatchResultItem> results = new ArrayList<>();
            for (int i = 0; i < from.length; i++) {
                final int score = scores[i];
                if (score < BAD_SCORE) {
                    results.add(new MatchResultItem(i, score));
                }
            }
            Collections.sort(results);

            final int resultItems = Math.min(8, results.size());
            @SuppressWarnings("unchecked")
            final T[] resultArray = (T[]) Array.newInstance(from.getClass().getComponentType(), resultItems);
            for (int i = 0; i < resultItems; i++) {
                resultArray[i] = from[results.get(i).index];
            }
            return Match.failure(noun, resultArray, toString);
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
}
