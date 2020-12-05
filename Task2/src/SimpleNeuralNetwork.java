//Matt Luettgen
//followed this tutorial https://towardsdatascience.com/understanding-and-implementing-neural-networks-in-java-from-scratch-61421bb6352c
//modified to fit this project
//(pretty much the same. CosineDifference took a long time to get working so I was running out of time.)
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimpleNeuralNetwork {
    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";
    public static class NeuralNetwork {
        //ih = input hidden
        //ho = hidden output
        //bias-h = hidden bias
        //bias-o = output bias.
        Matrix weights_ih , weights_ho , bias_h , bias_o;
        double l_rate=0.01;
        public NeuralNetwork(int i,int h,int o) {
            weights_ih = new Matrix(h,i);
            weights_ho = new Matrix(o,h);
            bias_h= new Matrix(h,1);
            bias_o= new Matrix(o,1);
        }
        public List<Double> predict(double[] X) {
            Matrix input = Matrix.fromArray(X);
            Matrix hidden = Matrix.multiply(weights_ih, input);
            hidden.add(bias_h);
            hidden.sigmoid();

            Matrix output = Matrix.multiply(weights_ho,hidden);
            output.add(bias_o);
            output.sigmoid();

            return output.toArray();
        }
        public void train(double[] X, double [] Y){
            Matrix input = Matrix.fromArray(X);
            Matrix hidden = Matrix.multiply(weights_ih, input);
            hidden.add(bias_h);
            hidden.sigmoid();

            Matrix output = Matrix.multiply(weights_ho, hidden);
            output.add(bias_o);
            output.sigmoid();

            Matrix target = Matrix.fromArray(Y);

            Matrix error = Matrix.subtract(target, output);
            Matrix gradient = output.dsigmoid();
            gradient.multiply(error);
            gradient.multiply(l_rate);

            Matrix hidden_T = Matrix.transpose(hidden);
            Matrix who_delta = Matrix.multiply(gradient, hidden_T);

            weights_ho.add(who_delta);
            bias_o.add(gradient);
            Matrix who_T = Matrix.transpose(weights_ho);
            Matrix hidden_errors = Matrix.multiply(who_T, error);

            Matrix h_gradient = hidden.dsigmoid();
            h_gradient.multiply(hidden_errors);
            h_gradient.multiply(l_rate);

            Matrix i_T = Matrix.transpose(input);
            Matrix wih_delta = Matrix.multiply(h_gradient, i_T);

            weights_ih.add(wih_delta);
            bias_h.add(h_gradient);
        }
        public void fit(double[][]X, double[][]Y, int epochs){
            for(int i = 0; i < epochs; i++){
                int sampleN = (int)(Math.random() * X.length);
                this.train(X[sampleN], Y[sampleN]);
            }
        }
    }
    //stuff I actually coded:
    //reads in the output of OtherFeatures. gets data.
    public static double[][] readData(String dataset, String state) throws FileNotFoundException {
        List<double[]> lst = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_" + dataset + ".txt"));
        int N = 0;
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            String[] user = line.split("\t");
            double[] userData = new double[user.length - 1];
            for(int i = 1; i < user.length; i++){
                userData[i - 1] = Double.parseDouble(user[i]);
            }
            lst.add(userData);
            N = userData.length;
        }
        double[][] returnDouble = new double[lst.size()][N];
        for(int i = 0; i < lst.size(); i++){
            returnDouble[i] = lst.get(i);
        }
        return returnDouble;
    }
    //reads in output of otherFeatures. gets answers
    public static double[][] readAnswers(String dataset, String state) throws FileNotFoundException {
        List<double[]> lst = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_" + dataset + "_answers.txt"));
        int N = 0;
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            String[] user = line.split("\t");
            N = user.length;
            double[] answer = new double[] {Double.parseDouble(user[1])};
            lst.add(answer);
        }
        double[][] returnDouble = new double[lst.size()][N];
        for(int i = 0; i < lst.size(); i++){
            returnDouble[i] = lst.get(i);
        }
        return returnDouble;
    }

    public static void print2DArray(double[][] double2D){
        for(double[] doubles: double2D){
            for(double d: doubles){
                System.out.print(d + ", ");
            }
            System.out.println();
        }
    }

    //gonna do y = (x-mean/standard_deviation) for all these. Really uneven data.
    public static double[] findMeans(double[][] double2DArray){
        int numExamples = double2DArray.length;      //number of training examples
        int numFeatures = double2DArray[0].length;    //number of features;
        double[] means = new double[numFeatures];
        for(int i = 0; i  < numFeatures; i++){
            double mean = 0.0;
            for(int j = 0; j < numExamples; j++){
                mean += double2DArray[j][i];
            }
            mean = mean/numExamples;
            means[i] = mean;
        }
        return means;
    }
    public static double[] findStandardDeviations(double[][] double2DArray){
        int numExamples = double2DArray.length;      //number of training examples
        int numFeatures = double2DArray[0].length;    //number of features;
        double[] means = findMeans(double2DArray);
        double[] sds = new double[numFeatures];
        for(int i = 0; i < numFeatures; i++) {
            double sdSum = 0;
            double mean = means[i];
            for (int j = 0; j < numExamples; j++) {
                sdSum += Math.pow(double2DArray[j][i] - mean, 2);
            }
            sds[i] = Math.sqrt(sdSum/numExamples);
        }
        return sds;
    }

    public static double[][] standardizeData(double[][] double2DArray){
        int numExamples = double2DArray.length;      //number of training examples
        int numFeatures = double2DArray[0].length;
        double[][] standardized2DArray = new double[numExamples][numFeatures];
        double[] means = findMeans(double2DArray);
        double[] sds = findStandardDeviations(double2DArray);
        for(int i = 0; i < numFeatures; i++){
            double mean = means[i];
            double sd = sds[i];
            for(int j = 0; j < numExamples; j++){
                standardized2DArray[j][i] = (double2DArray[j][i] - mean)/sd;
            }
        }
        return standardized2DArray;
    }

    public static void run(String state) throws IOException {
        double[][] trainingData = readData("training", state);
        double[][] trainingAnswers = readAnswers("training", state);
        double[][] standardizedData = standardizeData(trainingData);
        NeuralNetwork nn = new NeuralNetwork(8, 8, 1);
        nn.fit(standardizedData, trainingAnswers, 50000);

        double[][] testingData = readData("testing", "NV");
        double[][] testingAnswers = readAnswers("testing", "NV");
        FileWriter writer = new FileWriter(FILEPATH + "\\data\\" + state + "_evaluation.txt");
        for(int i = 0; i < testingData.length; i++){
            double output = nn.predict(testingData[i]).get(0);
            double answer = testingAnswers[i][0];
            writer.write(output + "\t" + answer + "\n");
        }
        writer.close();
    }
    public static void main(String[] args) throws IOException {
        run("NV");
        /**
        double[][] trainingData = readData("training", "NV");
        double[] means = findMeans(trainingData);
        for(double mean: means){ System.out.print(mean + ", "); }
        System.out.println();
        double[] sds = findStandardDeviations(trainingData);
        for(double sd: sds){ System.out.print(sd + ", "); }
         **/
    }
}
