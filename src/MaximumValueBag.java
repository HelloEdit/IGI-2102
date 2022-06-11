/**
 * Problem of the maximum value of a bag
 *
 * Derived from the basic code provided by René Natowicz (rene.natowicz@esiee.fr)
 * Code written in the framework of the IGI-2102 unit by Corentin Poupry (corentin.poupry@edu.esiee.fr) and Neo Jonas
 * (neo.jonas@edu.esiee.fr). All rights reserved.
 *
 * Created with Java 18
 **/

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MaximumValueBag {
    /**
     * maximum number of objects
     */
    public static final int NMAX = 100;

    /**
     * maximum capacity of the bag
     */
    public static final int CMAX = 1000;

    /**
     * maximum value of one object
     */
    public static final int VMAX = 100;

    /**
     * maximum size of one object
     */
    public static final int SMAX = 50;

    public static void main(String[] args) {
        var data_ratio = launch(5000, GloutonStrategy.BY_RATIO);
        Utils.export_data("maximum_value_bag_ratio", data_ratio);

        var data_value = launch(5000, GloutonStrategy.BY_VALUE);
        Utils.export_data("maximum_value_bag_value", data_value);

        var data_size = launch(5000, GloutonStrategy.BY_SIZE);
        Utils.export_data("maximum_value_bag_size", data_size);
    }

    /**
     * Launch the different strategies and review the results data
     * @param run_limit number of runs to make
     * @return the relative distances
     */
    static double[] launch(int run_limit, GloutonStrategy strategy) {
        // we use a thread local PRNG to ensure that there will be no unnecessary instantiation & allocations
        Random rand = ThreadLocalRandom.current();
        double[] data = new double[run_limit];

        long start_time = System.nanoTime();

        for (int run = 0; run < run_limit; run++) {
            System.out.printf("--- Run number #%d ---", run + 1);
            System.out.println();

            int c = rand.nextInt(CMAX) + 1;
            System.out.printf("Capacity of the bag: %d", c);
            System.out.println();

            // 20 <= Number of objects <= 100
            int n = 20 + rand.nextInt(NMAX - 20) + 1;
            System.out.printf("Number of objects: %d", n);
            System.out.println();

            // create our list of objects
            BagObject[] objects = new BagObject[n];

            // fill it
            for (int i = 0; i < n; i++) {
                objects[i] = BagObject.CreateRandomObject();
            }

            Comparator<BagObject> comparator = null;

            if (strategy == GloutonStrategy.BY_RATIO) comparator = Comparator.comparing(BagObject::ratio).reversed();
            if (strategy == GloutonStrategy.BY_VALUE) comparator = Comparator.comparing(BagObject::value).reversed();
            if (strategy == GloutonStrategy.BY_SIZE) comparator = Comparator.comparing(BagObject::size).reversed();

            // applies the naive strategy to sort objects
            int g = glouton(objects, c, comparator);

            // apply optimised strategy
            int[][] M = calculerM(objects, c);
            int v = M[n][c];

            Utils.print_result(g, v);

            data[run] = v == 0 ? 0 : (double) (v - g) / (double) v;
        }

        long elapsed_time = System.nanoTime() - start_time;

        System.out.println();
        System.out.printf("Elapsed time: %fms", (double) TimeUnit.NANOSECONDS.toMillis(elapsed_time));

        return data;
    }

    /**
     * Apply the optimised strategy
     * @param objects objects that can be chosen
     * @param C max capacity of the bag
     * @return the problem-solving matrix
     */
    static int[][] calculerM(BagObject[] objects, int C) {
        int n = objects.length;
        // Retourne M[0:n+1][0:C+1], de terme général M[k][c] = m(k,c)
        int[][] M = new int[n + 1][C + 1];

        // Base : m(0,c) = 0 pour toute contenance c, 0 <= c < C+1
        for (int c = 0; c < C + 1; c++) M[0][c] = 0;

        // Cas général, pour tous k et c, 1 <= k < n+1, 0 <= c < C+1,
        // m(k,c) = max(M[k-1][c], V[k-1] + M[k-1][c-T[k-1]])
        for (int k = 1; k < n + 1; k++) {
            for (int c = 0; c < C + 1; c++) {
                // calcul et mémorisation de m(k,c)
                if (c - objects[k - 1].size < 0) // le k-ème objet est trop gros pour entrer dans le sac
                    M[k][c] = M[k - 1][c];
                else
                    M[k][c] = Math.max(M[k - 1][c], objects[k - 1].value + M[k - 1][c - objects[k - 1].size]);
            }
        }

        return M;
    }

    /**
     * Apply the greedy strategy
     * @param objects  list of possible objects
     * @param capacity bag capacity limit
     * @return the sum of the values of the items in the bag
     */
    static int glouton(BagObject[] objects, int capacity, Comparator<BagObject> comparator) {
        // sort the objects in descending order of ratio (from the most to the least interesting object to take)
        Arrays.sort(objects, comparator);

        int sum = 0;

        for (BagObject object : objects) {
            // the object is too big for the actual capacity
            if (capacity < object.size) continue;

            capacity -= object.size;
            sum += object.value;
        }

        return sum;
    }

    /**
     * Existing naive strategies
     */
    enum GloutonStrategy {
        /**
         * Compare objects by descending ratio
         */
        BY_RATIO,

        /**
         * Compare objects in descending order of value
         */
        BY_VALUE,

        /**
         * Compare objects in descending order of size
         */
        BY_SIZE
    }

    /**
     * Represents an object used in the problem
     *
     * @param size  Size of the object
     * @param value Value of the object
     */
    public record BagObject(int size, int value) {
        /**
         * Creates an object whose size and value are random
         *
         * @return the randomly created object
         */
        static BagObject CreateRandomObject() {
            Random rand = ThreadLocalRandom.current();

            int size = rand.nextInt(SMAX) + 1;
            int value = rand.nextInt(VMAX) + 1;

            return new BagObject(size, value);
        }

        /**
         * Gives the ratio of the object, calculated by dividing the value by the size
         *
         * @return the ratio of the object
         */
        public float ratio() {
            return (float) this.value / (float) this.size;
        }

        @Override
        public String toString() {
            return "BagObject{size=%d, value=%d, ratio=%f}".formatted(size, value, this.ratio());
        }
    }
}
