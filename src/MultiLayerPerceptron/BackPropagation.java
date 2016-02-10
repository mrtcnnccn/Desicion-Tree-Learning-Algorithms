package MultiLayerPerceptron;

import Utils.Instance;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import misc.Util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by mertcan on 21.10.2015.
 */
public class BackPropagation {

    public static MultiLayerNetwork multi_perceptron;
    public static Random r = new Random();
    public static int input_number_train = 60000;
    public static int input_number_test = 60000;
    public static int hidden_neuron_number = 40;//28*28*2/3;
    public static int input_dimension = 28*28;
    public static int output_dimension = 10;
    public static int number_of_epochs = 50;
    public static boolean drawable = false;
    public static ArrayList<Instance> train_instances = new ArrayList<>();
    public static ArrayList<Instance> test_instances = new ArrayList<>();
    public static double[][] inputs = new double[input_number_train][input_dimension + 1];
    public static double[][] outputs_train = new double[input_number_train][output_dimension];
    public static double[][] output_hats_train = new double[input_number_train][output_dimension];
    public static double[][] outputs_test = new double[input_number_test][output_dimension];
    public static double[][] output_hats_test = new double[input_number_train][output_dimension];

    public static double[] B2 = new double[output_dimension];
    public static double[][] G2 = new double[output_dimension][hidden_neuron_number + 1];
    public static double[] B1 = new double[hidden_neuron_number + 1];
    public static double[][] G1 = new double[hidden_neuron_number][input_dimension + 1];

    public static MatlabProxyFactory factory;
    public static MatlabProxy proxy;
    public static MatlabTypeConverter processor;

    public BackPropagation(String trainfile, String testfile, int hidden_number, int epochs, double learn_rate, boolean draw) throws MatlabConnectionException, MatlabInvocationException {


        try {
//            Util.readFile(train_instances, "iris.data.txt");
//            Util.readFile(train_instances, "iris.data.v2.txt");
//            Util.readFile(train_instances, "data_set_nonlinear_1.data.txt");
//            Util.readFile(train_instances, "data_sdt\\mnist\\mnist.txt");
//            Util.readFile(test_instances, "data_sdt\\mnist\\mnist.txt");

            readFile(train_instances, trainfile);
            readFile(test_instances, testfile);
//            Util.readFile(train_instances, "data_sdt\\mnist\\mnist_ordered_01.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        input_number_train = train_instances.size();
        input_number_test = test_instances.size();
        output_dimension = CLASS_NAMES.size();
        MultiLayerNetwork.learn_rate_main = learn_rate;
        MultiLayerNetwork.learn_rate = learn_rate;
        number_of_epochs = epochs;
        hidden_neuron_number = hidden_number;
        drawable = draw;

        inputs = new double[input_number_train][input_dimension + 1];
        outputs_train = new double[input_number_train][output_dimension];
        output_hats_train = new double[input_number_train][output_dimension];
        outputs_test = new double[input_number_test][output_dimension];
        output_hats_test = new double[input_number_train][output_dimension];

        B2 = new double[output_dimension];
        G2 = new double[output_dimension][hidden_neuron_number + 1];
        B1 = new double[hidden_neuron_number + 1];
        G1 = new double[hidden_neuron_number][input_dimension + 1];


//        for(int i = 0; i < 3; i++){
//            hidden_neuron_number = 40;
//            for(int j = 0; j < 3; j++){
//                MultiLayerNetwork.learn_rate = MultiLayerNetwork.learn_rate_main;
//                G2 = new double[output_dimension][hidden_neuron_number + 1];
//                B1 = new double[hidden_neuron_number + 1];
//                G1 = new double[hidden_neuron_number][input_dimension + 1];
//                if(hidden_neuron_number == 0)
//                    multi_perceptron = new MultiLayerNetwork(input_dimension + 1,hidden_neuron_number,output_dimension);
//                else
//                    multi_perceptron = new MultiLayerNetwork(input_dimension + 1,hidden_neuron_number + 1,output_dimension);
//
//
//                createArrays();
//
//                train_backPropagate();
//
//                test();
//
//                plotPoints();
//                graph_all();
//                misc.Util.printOutMatrix(multi_perceptron.W1, "ww1_" + hidden_neuron_number + "_" + MultiLayerNetwork.learn_rate_main + ".txt");
//                hidden_neuron_number += 20;
//            }
//            MultiLayerNetwork.learn_rate_main += 0.002;
//            MultiLayerNetwork.learn_rate = MultiLayerNetwork.learn_rate_main;
//        }

    }

    public void runPerceptron() throws MatlabInvocationException, MatlabConnectionException {
        if(hidden_neuron_number == 0)
            multi_perceptron = new MultiLayerNetwork(input_dimension + 1,hidden_neuron_number,output_dimension);
        else
            multi_perceptron = new MultiLayerNetwork(input_dimension + 1,hidden_neuron_number + 1,output_dimension);


        if(input_dimension == 2 && drawable){
            factory = new MatlabProxyFactory();
            proxy = factory.getProxy();
            processor = new MatlabTypeConverter(proxy);
        }




        createArrays();

        train_backPropagate();

        System.out.println("train: " + test(train_instances) + " test: " + test(test_instances));

        if(drawable) {
            plotPoints();
            graph_all();
        }
    }

    private static void plotPoints() throws MatlabInvocationException {
        if(input_dimension == 2){
            String points_x[] = new String[CLASS_COUNT];
            String points_y[] = new String[CLASS_COUNT];
            int class_size = train_instances.size() / CLASS_COUNT;
            for(int i = 0; i < CLASS_COUNT; i++){
                points_x[i] = "[" + train_instances.get(i * train_instances.size() / CLASS_COUNT).attributes[0];
                points_y[i] = "[" + train_instances.get(i * train_instances.size() / CLASS_COUNT).attributes[1];
                for(int j = 1 + i * class_size; j < (i + 1) * class_size; j++){
                    points_x[i] += "," + train_instances.get(j).attributes[0];
                    points_y[i] += "," + train_instances.get(j).attributes[1];
                }
                points_x[i] += "]";
                points_y[i] += "]";
            }
            for(int i = 0; i < CLASS_COUNT; i++) {
                proxy.eval("points_x" + i + " = " + points_x[i]);
                proxy.eval("points_y" + i + " = " + points_y[i]);
            }
            String eval1 = "[";
            String eval2 = "[";
            for(int i = 0; i < CLASS_COUNT; i++){
                eval1 += "points_x" + i + " ";
                eval2 += "points_y" + i + " ";
            }
            eval1 += "]";
            eval2 += "]";

            proxy.eval("points_x = " + eval1);
            proxy.eval("points_y = " + eval2);

            proxy.eval("xmin = min(points_x)");
            proxy.eval("xmax = max(points_x)");
            proxy.eval("difference = xmax - xmin");
            proxy.eval("xmin = xmin - (difference) / 3");
            proxy.eval("xmax = xmax + (difference) / 3");
            proxy.eval("xlin = linspace(xmin, xmax)");
            proxy.eval("xlin2 = linspace(xmin, xmax,50)");
            proxy.eval("ymin = min(points_y)");
            proxy.eval("ymax = max(points_y)");
            proxy.eval("difference_y = ymax - ymin");
            proxy.eval("ymin = ymin - (difference_y) / 3");
            proxy.eval("ymax = ymax + (difference_y) / 3");
            proxy.eval("ylin = linspace(ymin, ymax,50)");
            proxy.eval("[xg, yg] = meshgrid(xlin2, ylin)");
            double[][] xg = processor.getNumericArray("xg").getRealArray2D();
            double[][] yg = processor.getNumericArray("yg").getRealArray2D();
            double[][] zg = new double[xg.length][xg[0].length];
            for(int i = 0; i < xg.length; i++){
                for(int j = 0; j < xg[0].length; j++){
                    zg[i][j] = findClass(new Instance(new double[]{xg[i][j], yg[i][j], 1}));
//                    System.out.print(zg[i][j]);
                }
//                System.out.println();
            }
            processor.setNumericArray("zg", new MatlabNumericArray(zg, null));
        }
    }

    private static double findClass(Instance i) {
        double[] results = feed_forward(i.attributes);
//        System.out.println(toString1dArray(i.attributes) + " " + toString1dArray(results));
        double max = 0;
        int maxIndex = 0;
        for(int j = 0; j < results.length; j++){
            if(results[j] > max){
                max = results[j];
                maxIndex = j;
            }
        }
        return maxIndex;
    }

    private static void graph_all() throws MatlabInvocationException {
        if(input_dimension == 2) {
            proxy.eval("figure");
            proxy.eval("surf(xg,yg,zg)");
//            proxy.eval("figure");
//            proxy.eval("surfc(xg,yg,zg)");
//            proxy.eval("figure");
//            proxy.eval("surfc(zg)");
            proxy.eval("figure");
            String v = "[1";
            for(int i = 2; i < CLASS_COUNT; i++)
                v += " " + i;
            v += "]";
            proxy.eval("contour(xg,yg,zg, " + v + ", 'ShowText','on')");

            String plot2 = "";
            for(int i = 0; i < CLASS_COUNT; i++){
                plot2 += ",points_x" + i + ", points_y" + i + ", '.'";
            }
            plot2 += ")";
            proxy.eval("hold on");
            plot2 = "(" + plot2.substring(1);
            proxy.eval("plot" + plot2);

        }
    }

    private static String test(ArrayList<Instance> T) {
        int trues = 0;
        int falses = 0;
        for(int i = 0; i < T.size(); i++){
            double[] atts  = Arrays.copyOf(T.get(i).attributes, T.get(i).attributes.length + 1);
            atts[atts.length - 1] = 1;
            int prediction = Util.argMax(feed_forward(atts));
            if(prediction == T.get(i).classNumber)
                trues++;
            else
                falses++;
        }
        //System.out.println("True: " + trues + " False: " + falses + " Percentage: " + ((double) trues / input_number_train));
        return "True: " + trues + " False: " + falses + " Percentage: " + ((double) trues / input_number_train);
    }

//    private static String test() {
//        int trues = 0;
//        int falses = 0;
//        for(int i = 0; i < input_number_train; i++){
////            double sum = 0;
//            double max = 0;
//            int maxIndex = 0;
//            for(int j = 0; j < output_dimension; j++){
////                sum += Math.abs((outputs_train[i][j] - output_hats_train[i][j]) * (1 - output_hats_train[i][j]) * output_hats_train[i][j]);
//                if(output_hats_train[i][j] > max){
//                    max = output_hats_train[i][j];
//                    maxIndex = j;
//                }
//
//            }
////            if(sum < Math.pow(10, -1) && sum > - Math.pow(10, -1))
////                trues++;
////            else
////                falses++;
////            if(maxIndex == i / (input_number_train / CLASS_COUNT))
//            if(outputs_train[i][maxIndex] == 1)
//                trues++;
//            else
//                falses++;
//        }
//        System.out.println("True: " + trues + " False: " + falses + " Percentage: " + ((double) trues / input_number_train));
//        return "True: " + trues + " False: " + falses + " Percentage: " + ((double) trues / input_number_train);
//    }

    private static void createArrays() {
        for(int a = 0; a < train_instances.size(); a++) {
            Instance T = train_instances.get(a);
            for (int i = 0; i < input_dimension; i++) {
                inputs[a][i] = T.attributes[i];
            }
            inputs[a][input_dimension] = 1;
            outputs_train[a] = new double[CLASS_COUNT];
            Arrays.fill(outputs_train[a], 0);
            outputs_train[a][T.classNumber] = 1;
        }
    }

    private static void train_backPropagate() {
//        System.out.println("W1: " + toString2dArray(multi_perceptron.W1));
//
//        System.out.println("W2: " + toString2dArray(multi_perceptron.W2));
//
//        System.out.println("inputs: " + toString2dArray(inputs));


        ArrayList<Integer> shuffler = new ArrayList<>();
        for(int i = 0; i < input_number_train; i++) shuffler.add(i);

        for(int trial = 0; trial < number_of_epochs; trial++) {
            Collections.shuffle(shuffler);
            for (int i = 0; i < input_number_train; i++) {
                int theInput = shuffler.get(i);
                double[] output_hat = feed_forward(inputs[theInput]);
//                System.out.println(toString1dArray(inputs[theInput]) + " " + toString1dArray(output_hat) + " " + toString1dArray(outputs_train[theInput]));
                output_hats_train[theInput] = output_hat;
                if(multi_perceptron.hidden_layer != 0) {
                    for (int j = 0; j < B2.length; j++) {
                        B2[j] = -2 * (outputs_train[theInput][j] - output_hat[j]) * multi_perceptron.output_neurons[j].derivative_sigma();
                    }
                    for (int j = 0; j < G2.length; j++) {
                        for (int t = 0; t < G2[0].length; t++) {
                            G2[j][t] = B2[j] * multi_perceptron.hidden_neurons[t].output;
                        }
                    }

                    for (int j = 0; j < B1.length; j++) {
                        double temp = 0;
                        for (int t = 0; t < B2.length; t++) {
                            temp += B2[t] * multi_perceptron.W2[t][j];
                        }
                        if(j != B1.length - 1)
                            temp *= multi_perceptron.hidden_neurons[j].derivative_sigma();
                        B1[j] = temp;
                    }
                    for (int j = 0; j < G1.length; j++) {
                        for (int t = 0; t < G1[0].length; t++) {
                            G1[j][t] = B1[j] * multi_perceptron.input_neurons[t].output;
                        }
                    }


                    for (int j = 0; j < G1.length; j++) {
                        for (int t = 0; t < G1[0].length; t++) {
                            multi_perceptron.W1[j][t] -= multi_perceptron.learn_rate * G1[j][t];
                        }
                    }
                    for (int j = 0; j < G2.length; j++) {
                        for (int t = 0; t < G2[0].length; t++) {
                            multi_perceptron.W2[j][t] -= multi_perceptron.learn_rate * G2[j][t];
                        }
                    }
                }else{
                    G2 = new double[output_dimension][input_dimension + 1];
                    for (int j = 0; j < B2.length; j++) {
                        if(j != B2.length - 1)
                            B2[j] = -2 * (outputs_train[theInput][j] - output_hat[j]) * multi_perceptron.hidden_neurons[j].derivative_sigma();
                        else
                            B2[j] = -2 * (outputs_train[theInput][j] - output_hat[j]);
                    }
                    for (int j = 0; j < G2.length; j++) {
                        for (int t = 0; t < G2[0].length; t++) {
                            G2[j][t] = B2[j] * multi_perceptron.input_neurons[t].output;
                        }
                    }
                    for (int j = 0; j < G2.length; j++) {
                        for (int t = 0; t < G2[0].length; t++) {
                            multi_perceptron.W1[j][t] -= multi_perceptron.learn_rate * G2[j][t];
                        }
                    }
                }

            }
//            if(trial == 10 || trial == 20 || trial == 50 || trial == 100 ||trial == 1000){
//            System.out.print("Epoch: " + trial + " ");
//            test();
            MultiLayerNetwork.learn_rate *= 0.99;
//            }
//            try {
//                outputToFile(trial);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

//        System.out.println("W1: " + toString2dArray(multi_perceptron.W1));
//
//        System.out.println("W2: " + toString2dArray(multi_perceptron.W2));
//
//        System.out.println("real outputs_train: " + toString2dArray(outputs_train));
//
//        System.out.println("outputs_train: " + toString2dArray(output_hats_train));


    }

    private static void outputToFile(int e) throws IOException {
        File file2 = new File("log" + File.separator + "mnist_multilayer_lesshidden2" + File.separator + "learning_rate_" + (int)(MultiLayerNetwork.learn_rate_main * 10000) + "_hidden_" + hidden_neuron_number + ".txt");
        file2.getParentFile().mkdirs();
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2, true));
        writer2.write("Epoch " + e + " train: " + test(train_instances) + " test: " +  test(test_instances) + "\n");
        writer2.flush();
        writer2.close();
    }

    private static double[] feed_forward(double[] input) {
        double[] output_hat = new double[output_dimension];

        for(int i = 0; i < multi_perceptron.input_layer; i++) {
            multi_perceptron.input_neurons[i].feed_neuron(input[i]);
        }

        if(multi_perceptron.hidden_layer != 0) {
            for(int i = 0; i < multi_perceptron.hidden_layer - 1; i++){
                double sum_hidden_neuron_i = 0;
                for(int j = 0; j < multi_perceptron.input_layer; j++){
                    sum_hidden_neuron_i += multi_perceptron.input_neurons[j].output * multi_perceptron.W1[i][j];
                }
                multi_perceptron.hidden_neurons[i].feed_neuron(sum_hidden_neuron_i);
            }
            multi_perceptron.hidden_neurons[multi_perceptron.hidden_layer - 1].output = 1;
            multi_perceptron.hidden_neurons[multi_perceptron.hidden_layer - 1].row = 1;

            for (int i = 0; i < multi_perceptron.output_layer; i++) {
                double sum_hidden_neuron_i = 0;
                for (int j = 0; j < multi_perceptron.hidden_layer; j++) {
                    sum_hidden_neuron_i += multi_perceptron.hidden_neurons[j].output * multi_perceptron.W2[i][j];
                }
                multi_perceptron.output_neurons[i].feed_neuron(sum_hidden_neuron_i);

                output_hat[i] = multi_perceptron.output_neurons[i].output;
            }
        }else{
            for(int i = 0; i < multi_perceptron.output_layer; i++){
                double sum_hidden_neuron_i = 0;
                for(int j = 0; j < multi_perceptron.input_layer; j++){
                    sum_hidden_neuron_i += multi_perceptron.input_neurons[j].output * multi_perceptron.W1[i][j];
                }
                multi_perceptron.hidden_neurons[i].feed_neuron(sum_hidden_neuron_i);
                output_hat[i] = multi_perceptron.hidden_neurons[i].output;
//                output_hat[i] = sum_hidden_neuron_i;
            }
        }


        return  output_hat;
    }

    public static String toString2dArray(double[][] a){
        String s = "";
        for(int i = 0; i < a.length; i++){
            for(int j = 0; j < a[0].length; j++){
                s += a[i][j] + " ";
            }
            s += "\n";
        }
        return s;
    }

    public static String toString1dArray(double[] a){
        String s = "";
        for(int i = 0; i < a.length; i++){
                s += a[i] + " ";
        }
        return s;
    }
    public static ArrayList<String> CLASS_NAMES = new ArrayList<>();
    public static int CLASS_COUNT = 0;

    private void readFile(ArrayList<Instance> I, String filename) throws IOException {
        String line;

        InputStream fis = new FileInputStream(filename);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);

        line = br.readLine();

        br.close();
        String[] s;
        String splitter;
        if (!line.contains(","))
            splitter = "\\s+";
        else
            splitter = ",";
        s = line.split(splitter);

        input_dimension = s.length - 1;
//        System.out.println(input_dimension + " " + line);
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            s = line.split(splitter);

            double[] attributes = new double[input_dimension];
            for (int i = 0; i < input_dimension; i++) {
                attributes[i] = Double.parseDouble(s[i]);
            }
//            System.out.println();
            String className = s[input_dimension];

            double classNumber;
            if (CLASS_NAMES.contains(className)) {
                classNumber = CLASS_NAMES.indexOf(className);
            } else {
                CLASS_NAMES.add(className);
                classNumber = CLASS_NAMES.indexOf(className);
            }
            I.add(new Instance((int)classNumber, attributes));
        }
        CLASS_COUNT = CLASS_NAMES.size();
    }

}
