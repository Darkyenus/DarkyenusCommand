package darkyenuscommand;

import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class Util {
    public static int levenshteinDistance (CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static <T> T findNearest(Iterable<T> selection, Function<T, String> toString, String target, int atLeast){
        int nearest = Integer.MAX_VALUE;
        T nearestItem = null;
        for(T s : selection){
            final int distance = levenshteinDistance(toString.apply(s), target);
            if(distance == 0){
                return s;
            } else if(distance < nearest){
                nearest = distance;
                nearestItem = s;
            }
        }
        if(nearest <= atLeast) return nearestItem;
        else return null;
    }
}
