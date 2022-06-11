/**
 * Problem of the optimal planning
 *
 * Derived from the basic code provided by René Natowicz (rene.natowicz@esiee.fr)
 * Code written in the framework of the IGI-2102 unit by Corentin Poupry (corentin.poupry@edu.esiee.fr) and Neo Jonas
 * (neo.jonas@edu.esiee.fr). All rights reserved.
 *
 * Created with Java 18
 **/

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class OptimalPlanning {
    /**
     * Maximum working hours
     */
    static public final int HMAX = 25;

    /**
     * Maximum possible topics
     */
    static public final int TMAX = 10;


    public static void main(String[] Args) {
        var data = launch(5000);
        Utils.export_data("optimal_planning", data);
    }

    /**
     * Launch the different strategies and review the results data
     * @param run_limit number of runs to make
     * @return the relative distances
     */
    static public double[] launch(int run_limit) {
        // we use a thread local PRNG to ensure that there will be no unnecessary instantiation & allocations
        Random rand = ThreadLocalRandom.current();
        double[] data = new double[run_limit];

        long start_time = System.nanoTime();

        int v;
        for (int run = 0; run < run_limit; run++) {
            System.out.printf("--- Run number #%d ---", run + 1);
            System.out.println();

            int units = rand.nextInt(TMAX) + 1;
            int hours_max = rand.nextInt(HMAX) + 1;

            System.out.printf("Number of units: %d", units);
            System.out.println();
            System.out.printf("Number of working hours: %d", hours_max);
            System.out.println();

            int[][] notes = generateNotes(units, hours_max);

            // Juliette travaille H heures, 0 ≤ H <= hours_max
            int[][][] MA = calculerMA(notes);
            int[][] M = MA[0];

            v = M[units][hours_max];

            int g = glouton(notes, hours_max);

            Utils.print_result(g, v);

            data[run] = v == 0 ? 0 : (double) (v - g) / (double) v;
        }

        long elapsed_time = System.nanoTime() - start_time;

        System.out.println();
        System.out.printf("Elapsed time: %fms", (double) TimeUnit.NANOSECONDS.toMillis(elapsed_time));

        return data;
    }

    private static int glouton(int[][] notes, int quota) {
        int units = notes.length;
        int[] hours_allocation = new int[units];

        // hours allocated are standardised in relation to the fact that the first mark is "free".
        Arrays.fill(hours_allocation, 1);

        while (quota > 0) {
            int max = 0;
            int index = 0;

            // we find the most advantageous unit to work with by looking at the
            // difference between the score with h hours and h-1 hours
            for (int i = 1; i < units; i++) {
                int allocated = hours_allocation[i];
                // always > 0 because the scores are in ascending order
                int diff = notes[i][allocated] - notes[i][allocated - 1];

                if (diff > max) {
                    max = diff;
                    index = i;
                }
            }

            // a new hour is allocated to the most interesting unit
            hours_allocation[index] += 1;
            // our quota of hours to be allocated is reduced by one hour
            quota -= 1;
        }

        int sum = 0;
        for (int i = 0; i < units; i++) {
            int hour = hours_allocation[i];
            sum += notes[i][hour - 1];
        }

        return sum;
    }

    /**
     * Generate random marks on units according to the time spent on revision
     * @param units number of units
     * @param h_max maximum number of hours of revision
     * @return a 2D table representing the units and the nested table, the scores that can
     * be expected with a revision time corresponding to the index
     */
    static public int[][] generateNotes(int units, int h_max) {
        Random rand = ThreadLocalRandom.current();

        // E[i][h] = e(i,h). Les estimations sont aléatoires, croissantes selon h.
        int[][] E = new int[units][h_max + 1];

        for (int i = 0; i < units; i++) E[i][0] = rand.nextInt(2);
        for (int i = 0; i < units; i++)
            for (int h = 1; h <= h_max; h++)
                E[i][h] = Math.min(E[i][h - 1] + 1 + rand.nextInt(2), 20);

        return E;
    }

    static int[][][] calculerMA(int[][] E) {    // E : tableau des notes estimées.
        // E[0:n][0:H+1] est de terme général E[i][h] = e(i,h).
        // Retourne M et A : M[0:n+1][0:H+1] de terme général M[k][h] = m(k,h), somme maximum
        // des notes d'une répartition de h heures sur le sous-ensemble des k premières unités.

        int n = E.length, H = E[0].length - 1;
        int[][] M = new int[n + 1][H + 1], A = new int[n + 1][H + 1];

        // base, k = 0.
        int s0 = 0; // somme des notes pour 0 heure travaillée
        for (int[] ints : E) s0 = s0 + ints[0];

        // Base : m(0,h) = s0 pour tout h, 0 ≤ h < H+1
        for (int h = 0; h < H + 1; h++)
            M[0][h] = s0;

        // Cas général, 1 ≤ k < n+1 pour tout h, h, 0 ≤ h < H+1 :
        // m(k,h) = ( Max m(k-1, h - h_k) + e(k-1,h_k) sur h_k, 0 ≤ h_k < h+1 ) - e(k-1,0)
        // Calcul des valeur m(k,h) par k croissants et mémorisation dans le tableau M.
        // Calcul à la volée des a(k,h) = arg m(k,h) et mémorisation dans le tableau A.
        for (int k = 1; k < n + 1; k++) // par tailles k croissantes
            for (int h = 0; h < H + 1; h++) { // calcul des valeurs m(k,h), 0 ≤ h < H+1
                // Calcul de M[k][h] = Max M[k-1][h-h_k] + e(k-1,h_k), h_k, 0 ≤ h_k < h+1 ) - e(k-1,0)
                M[k][h] = -1;
                for (int h_k = 0; h_k < h + 1; h_k++) {
                    int mkhh_k = M[k - 1][h - h_k] + E[k - 1][h_k];
                    if (mkhh_k > M[k][h]) {
                        M[k][h] = mkhh_k;
                        A[k][h] = h_k;
                    }
                }

                // M[k][h] = (max M[k-1][h-h_k] + e(k-1,h_k), h_k, 0 ≤ h_k < h+1)
                M[k][h] = M[k][h] - E[k - 1][0];  // M[k][h] = m(k,h)
            }

        return new int[][][]{M, A};
    }
}