/**
 * Problem of the robot minimum path
 *
 * Derived from the basic code provided by René Natowicz (rene.natowicz@esiee.fr)
 * Code written in the framework of the IGI-2102 unit by Corentin Poupry (corentin.poupry@edu.esiee.fr) and Neo Jonas
 * (neo.jonas@edu.esiee.fr). All rights reserved.
 *
 * Created with Java 18
 **/

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MinimumPathRobot {
    /**
     * Maximum width of the grid
     */
    public static final int LMAX = 1000;

    /**
     * Maximum height of the grid
     */
    public static final int CMAX = 1000;

    /**
     * Maximum value for a grid element
     */
    public static final int VMAX = 200;

    public static void main(String[] args) {
        var data = launch(10_000);
        Utils.export_data("minimum_path_robot", data);
    }

    /**
     * Launch the different strategies and review the results data
     * @param run_limit number of runs to make
     * @return the relative distances
     */
    static double[] launch(int run_limit) {
        // we use a thread local PRNG to ensure that there will be no unnecessary instantiation & allocations
        Random rand = ThreadLocalRandom.current();
        double[] data = new double[run_limit];

        long start_time = System.nanoTime();

        for (int run = 0; run < run_limit; run++) {
            System.out.printf("--- Run number #%d ---", run + 1);
            System.out.println();

            // let's generate the dimensions of the problem grid at random
            // one is added so that the grid cannot be zero sized
            int L = rand.nextInt(LMAX) + 1;
            int C = rand.nextInt(CMAX) + 1;

            System.out.printf("Grid dimension: %d x %d", L, C);
            System.out.println();

            // we generate our moving cost matrices (or grid)
            int[][] N = generateGrid(L, C);
            int[][] E = generateGrid(L, C);
            int[][] NE = generateGrid(L, C);

            // apply the naive way
            int g = glouton(N, E, NE);

            // apply optimised strategy
            int[][] M = calculerM(N, E, NE);
            int v = M[L - 1][C - 1];

            Utils.print_result(g, v);

            data[run] = v == 0 ? 0 : (double) (g - v) / (double) v;
        }

        long elapsed_time = System.nanoTime() - start_time;

        System.out.println();
        System.out.printf("Elapsed time: %fms", (double) TimeUnit.NANOSECONDS.toMillis(elapsed_time));

        return data;
    }


    /**
     * Apply the naive strategy
     *
     * @param north_grid     northbound movement grid
     * @param east_grid      eastward movement grid
     * @param northeast_grid north-eastward movement grid
     * @return the total cost of getting from (0, 0) to (l - 1, c - 1)
     */
    public static int glouton(int[][] north_grid, int[][] east_grid, int[][] northeast_grid) {
        int total = 0;

        int l = 0, c = 0;
        int L = north_grid.length, C = north_grid[0].length;

        while (l < L - 1 || c < C - 1) {
            // We calculate in which direction it is less expensive to go
            int n_cost = N(l, c, L, C, north_grid);
            int e_cost = E(l, c, L, C, east_grid);
            int ne_cost = NE(l, c, L, C, northeast_grid);

            int min_cost = min(n_cost, e_cost, ne_cost);

            if (min_cost == n_cost) {
                l++; // go to North
            } else if (min_cost == e_cost) {
                c++; // go to East
            } else {
                l++; // go to North...
                c++; // ...and East
            }

            // add the cost of moving
            total += min_cost;
        }

        return total;
    }

    /**
     * Apply the optimised strategy
     *
     * @param north_grid     northbound movement grid
     * @param east_grid      eastward movement grid
     * @param northeast_grid north-eastward movement grid
     * @return the problem-solving matrix
     */
    public static int[][] calculerM(int[][] north_grid, int[][] east_grid, int[][] northeast_grid) {
        int L = north_grid.length;
        int C = north_grid[0].length;

        int[][] M = new int[L][C]; // de terme général M[l][c] = m(l,c)
        // base
        M[0][0] = 0;
        for (int c = 1; c < C; c++) M[0][c] = M[0][c - 1] + E(0, c - 1, L, C, east_grid);
        for (int l = 1; l < L; l++) M[l][0] = M[l - 1][0] + N(l - 1, 0, L, C, north_grid);

        // cas général
        for (int c = 1; c < C; c++)
            for (int l = 1; l < L; l++)
                M[l][c] = min(
                        M[l][c - 1] + E(l, c - 1, L, C, east_grid),
                        M[l - 1][c] + N(l - 1, c, L, C, north_grid),
                        M[l - 1][c - 1] + NE(l - 1, c - 1, L, C, northeast_grid)
                );

        return M;
    }

    /**
     * Generate a L-C grid as a matrix L x C with random values
     *
     * @param L how many L cells the matrix should have
     * @param C how many C cells the matrix should have
     * @return the grid created
     */
    public static int[][] generateGrid(int L, int C) {
        // we use a thread local PRNG to ensure that there will be no unnecessary instantiation & allocations
        Random rand = ThreadLocalRandom.current();

        // Matrix M(L, C) as our grid
        int[][] grid = new int[L][C];

        for (int i = 0; i < L; i++) {
            for (int j = 0; j < C; j++) {
                grid[i][j] = rand.nextInt(VMAX);
            }
        }

        return grid;
    }

    /**
     * Minimum between three value
     *
     * @param a first value
     * @param b second value
     * @param c third value
     * @return the minimum
     */
    public static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * Cost of moving the robot to the East of its position
     *
     * @param l current height coordinate
     * @param c current width coordinate
     * @param L dimension of the height of the grid
     * @param C dimension of the width of the grid
     * @param S the cost grid
     * @return the cost of moving
     */
    private static int E(int l, int c, int L, int C, int[][] S) {
        // prevent out of bounds
        if (c + 1 >= C) return Integer.MAX_VALUE;
        return S[l][c + 1];
    }

    /**
     * Cost of moving the robot to the North of its position
     *
     * @param l current height coordinate
     * @param c current width coordinate
     * @param L dimension of the height of the grid
     * @param C dimension of the width of the grid
     * @param S the cost grid
     * @return the cost of moving
     */
    private static int N(int l, int c, int L, int C, int[][] S) {
        // prevent out of bounds
        if (l + 1 >= L) return Integer.MAX_VALUE;
        return S[l + 1][c];
    }

    /**
     * Cost of moving the robot to the North East of its position
     *
     * @param l current height coordinate
     * @param c current width coordinate
     * @param L dimension of the height of the grid
     * @param C dimension of the width of the grid
     * @param S the cost grid
     * @return the cost of moving
     */
    private static int NE(int l, int c, int L, int C, int[][] S) {
        // prevent out of bounds
        if (l + 1 >= L || c + 1 >= C) return Integer.MAX_VALUE;
        return S[l + 1][c + 1];
    }
}
