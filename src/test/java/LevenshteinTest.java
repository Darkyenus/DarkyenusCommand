import darkyenuscommand.MatchUtils;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 *
 */
public class LevenshteinTest {

    private static void add(StringBuilder sb, int[] line){
        sb.append(line[0]);
        for (int i = 1; i < line.length; i++) {
            sb.append(',').append(line[i]);
        }
        sb.append('\n');
    }

    public static int levenshteinDistance(CharSequence s0, CharSequence s1, int insertCost, int replaceCost, int deleteCost) throws IOException {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        final StringBuilder debug = new StringBuilder();

        // the array of distances
        int[] cost = new int[len0];
        int[] newCost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i * deleteCost;

        add(debug, cost);

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

            add(debug, cost);
        }

        final File file = new File("debug.csv");
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.append(debug);
        fileWriter.close();

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    private static int min (int a, int b, int c) {
        if (a <= b && a <= c) return a;
        if (b <= a && b <= c) return b;
        return c;
    }

    public static void main(String[] args) throws IOException {
        //System.out.println(levenshteinDistance("inksack","ink", 1, 1000, 1000));

        System.out.println(MatchUtils.matchMaterialData("phseeds", new CommandSender() {
            @Override
            public void sendMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void sendMessage(String[] messages) {
                for (String message : messages) {
                    System.out.println(message);
                }
            }

            @Override
            public Server getServer() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean isPermissionSet(String name) {
                return false;
            }

            @Override
            public boolean isPermissionSet(Permission perm) {
                return false;
            }

            @Override
            public boolean hasPermission(String name) {
                return false;
            }

            @Override
            public boolean hasPermission(Permission perm) {
                return false;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
                return null;
            }

            @Override
            public void removeAttachment(PermissionAttachment attachment) {

            }

            @Override
            public void recalculatePermissions() {

            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean value) {

            }
        }));
    }
}
