/**
 * Problem of the optimal distribution of a stock on warehouses
 *
 * Code written in the framework of the IGI-2102 unit by Corentin Poupry (corentin.poupry@edu.esiee.fr) and Neo Jonas
 * (neo.jonas@edu.esiee.fr). All rights reserved.
 *
 * Created with Java 18
 **/

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class MaximumPathTriangle {
    /**
     * Maximum number of levels
     */
    public static final int LMAX = 100;

    /**
     * Maximum value for data
     */
    public static final int VMAX = 50;

    public static void main(String[] args) {
        double[] data = launch(5_000 * 2);
        Utils.export_data("maximum_path_triangle", data);
    }

    /**
     * Launch the different strategies and review the results data
     * @param run_limit number of runs to make
     * @return the relative distances
     */
    public static double[] launch(int run_limit) {
        // we use a thread local PRNG to ensure that there will be no unnecessary instantiation & allocations
        Random rand = ThreadLocalRandom.current();
        double[] data = new double[run_limit];

        long start_time = System.nanoTime();

        for (int run = 0; run < run_limit; run++) {
            System.out.printf("--- Run number #%d ---", run + 1);
            System.out.println();

            int l = rand.nextInt(LMAX) + 1;
            System.out.printf("Levels: %d", l);
            System.out.println();

            // calculation of the number of values to fill all the levels
            int values = (int)(0.5 * l * (l + 1));
            System.out.printf("Number of values: %d", values);

            // triangle's data
            int[] T = new int[values];

            for (int i = 0; i < values; i++) {
                T[i] = rand.nextInt(VMAX);
            }

            // applies the naive strategy to sort objects
            int g = glouton(T, l);

            // apply optimised strategy
            int[] M = calculerM(T);
            int v = M[0];

            Utils.print_result(g, v);

            data[run] = v == 0 ? 0 : (double) (v - g) / (double) v;
        }

        long elapsed_time = System.nanoTime() - start_time;

        System.out.println();
        System.out.printf("Elapsed time: %fms", (double) TimeUnit.NANOSECONDS.toMillis(elapsed_time));

        return data;
    }

    /**
     * Apply the greedy strategy
     * @param data values of the triangle
     * @param max_level levels that the triangle has
     * @return sum of the maximum path taken
     */
    public static int glouton(int[] data, int max_level) {
        int i = 0;
        // the first node is always part of the path
        int sum = data[0];

        // we will have a choice to make at each level before hitting the bottom
        for (int j = 0; j < max_level - 1; j++) {
            int i_left = g(i);
            int i_right = d(i);

            // we choose the minimum between the two descendants
            int max = Math.max(data[i_left], data[i_right]);

            // we give the index its new value
            if (max == data[i_left]) i = i_left;
            if (max == data[i_right]) i = i_right;

            // we add the weight of the node
            sum += max;
        }

        return sum;
    }

    /**
     * Apply the optimal strategy
     * @param T triangle's data
     * @return the resolution array
     */
    public static int[] calculerM(int[] T){
        int[] M = new int[T.length];

        // On commence par le bas du triangle pour ensuite remonter
        for(int i = T.length - 1; i >= 0; i--){
            // On regarde si on sort du triangle.
            // Si oui, on est alors sur une feuille
            if(g(i) >= T.length){
                M[i] = T[i];
            } else {
                // Sinon on est sur un nœud qui n'est pas une feuille
                // il faut alors prendre en compte le sous-problème de la somme du triangle de
                // gauche et de celle du triangle de droite auquel on additionnera le poids du nœud actuel.
                // On sélectionnera le plus grand sous-problème
                // m(i) = max(m(g(i)), m(d(i))) + T[i]
                M[i] = Math.max(M[g(i)], M[d(i)]) + T[i];
            }
        }

        return M;
    }

    /**
     * Return the index of the left descendant of the parent index
     * @param i parent index
     * @return left descendant index
     */
    public static int g(int i) {
        // we determine the level at which the value of index i is
        int l = 1;
        while (0.5 * l * (l + 1) - 1 < i) l++;

        // then we determine the index i_max of the last value of the level l
        int i_max = (int)(0.5 * l * (l + 1) - 1);

        // we calculate the relative distance between i_max and our index i
        // this will give us the relative position p at the end of the level l
        int diff = i_max - i;

        // we know that on the line l there are l + 1 values, and we know the relative position to the last value.
        // We calculate the positioning p index
        int p = l - 1 - diff;

        // i_max + 1 is the index of the first element of the level l + 1 and p its position in the line,
        // we add them together to obtain the new index
        return i_max + 1 + p;
    }

    /**
     * Return the index of the right descendant of the parent index
     * @param i parent index
     * @return right descendant index
     */
    public static int d(int i) {
        return g(i) + 1;
    }
}
