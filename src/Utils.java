/**
 * Code written in the framework of the IGI-2102 unit by Corentin Poupry (corentin.poupry@edu.esiee.fr) and Neo Jonas
 * (neo.jonas@edu.esiee.fr). All rights reserved.
 *
 * Created with Java 18
 **/
import java.io.FileWriter;

public class Utils {
    private static final String SEPARATOR = "\n";

    /**
     * Export data under the requested format in order to generate graph
     * @param name name of the file to create
     * @param data data to be used in the file
     */
    public static void export_data(String name, double[] data) {
        try {
            FileWriter file = new FileWriter("./data/%s.csv".formatted(name));

            for (double datum : data) {
                file.append(Double.toString(datum));
                file.append(SEPARATOR);
            }

            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the results of the strategies
     * @param g result of the greedy strategy
     * @param v result of the optimised strategy
     */
    public static void print_result(int g, int v) {
        System.out.println();
        System.out.println("RESULT");
        System.out.printf("naive : %d", g);
        System.out.println();
        System.out.printf("optimised : %d", v);
        System.out.println();
    }
}
