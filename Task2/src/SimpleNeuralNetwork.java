//Matt Luettgen
//followed this tutorial https://towardsdatascience.com/understanding-and-implementing-neural-networks-in-java-from-scratch-61421bb6352c
//modified to fit this project
//(pretty much the same. CosineDifference took a long time to get working so I was running out of time.)
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.*;
import java.lang.Comparable;

public class SimpleNeuralNetwork {
    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";
    public static class NeuralNetwork {
        //ih = input hidden
        //ho = hidden output
        //bias-h = hidden bias
        //bias-o = output bias.
        Matrix weights_ih , weights_ho , bias_h , bias_o;

        //sigmoid .005 at 6 hidden units tends to work
        double l_rate;
        public NeuralNetwork(int i,int h,int o, double lr) {
            weights_ih = new Matrix(h,i);
            weights_ho = new Matrix(o,h);
            bias_h= new Matrix(h,1);
            bias_o= new Matrix(o,1);
            l_rate = lr;
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
        public void train(double[] X, double [] Y, String activationFunction) throws CloneNotSupportedException {
            Matrix input = Matrix.fromArray(X);
            Matrix hidden = Matrix.multiply(weights_ih, input);
            Matrix hiddenClone = hidden.clone();
            hidden.add(bias_h);
            if(activationFunction.equals("sigmoid")){ hidden.sigmoid(); }
            else{
                if(activationFunction.equals("gaussian")){ hidden.gaussian();}
                else{
                    System.out.println("Warning not valid activation function") ;
                }
            }

            Matrix output = Matrix.multiply(weights_ho, hidden);
            output.add(bias_o);
            Matrix outputClone = output.clone();
            if(activationFunction.equals("sigmoid")){ output.sigmoid();}
            else{
                if(activationFunction.equals("gaussian")){
                    output.gaussian();
                }
            }

            Matrix target = Matrix.fromArray(Y);

            Matrix error = Matrix.subtract(target, output);
            Matrix gradient = null;
            if(activationFunction.equals("sigmoid")){ gradient = output.dsigmoid();}
            else{
                if(activationFunction.equals("gaussian")){
                    gradient = outputClone.dgaussian();
                }
            }

            gradient.multiply(error);
            gradient.multiply(l_rate);
            System.out.println("error: " + error.data[0][0]);

            Matrix hidden_T = Matrix.transpose(hidden);
            Matrix who_delta = Matrix.multiply(gradient, hidden_T);

            weights_ho.add(who_delta);
            bias_o.add(gradient);
            Matrix who_T = Matrix.transpose(weights_ho);
            Matrix hidden_errors = Matrix.multiply(who_T, error);

            Matrix h_gradient = null;
            if(activationFunction.equals("sigmoid")){h_gradient = hidden.dsigmoid();}
            else{
                if(activationFunction.equals("gaussian")){
                    h_gradient = hiddenClone.dgaussian();
                }
            }
            h_gradient.multiply(hidden_errors);
            h_gradient.multiply(l_rate);

            Matrix i_T = Matrix.transpose(input);
            Matrix wih_delta = Matrix.multiply(h_gradient, i_T);

            weights_ih.add(wih_delta);
            bias_h.add(h_gradient);
        }
        public void fit(double[][]X, double[][]Y, int epochs, String activationFunction) throws CloneNotSupportedException {
            for(int i = 0; i < epochs; i++){
                int sampleN = (int)(Math.random() * X.length);
                this.train(X[sampleN], Y[sampleN], activationFunction);
            }
        }
    }
    //stuff I actually coded:
    //reads in the output of OtherFeatures. gets data.
    public static double[][] readData(String dataset, int batchSize, String state) throws FileNotFoundException {
        List<double[]> lst = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_" + dataset + ".txt"));
        int N = 0;
        int incrementer = 0;
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            String[] user = line.split("\t");
            double[] userData = new double[user.length - 1];
            for(int i = 1; i < user.length; i++){
                userData[i - 1] = Double.parseDouble(user[i]);
            }
            lst.add(userData);
            N = userData.length;
            incrementer++;
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
    public static ArrayList<String> getTestUsers(String state) throws FileNotFoundException {
        ArrayList<String> users = new ArrayList<String>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_testing_answers.txt"));
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String user = line.split("\t")[0];
            users.add(user);
        }
        return users;
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

    public static class UserResult implements Comparable<UserResult>{
        String userId;
        double resultNum;
        public UserResult(String id, double rn){
            this.userId = id;
            this.resultNum = rn;
        }

        @Override
        public int compareTo(UserResult o) {
            return Double.compare(this.resultNum, o.resultNum);
        }
    }


    public static void run(String state, int epochs, String activationFunction, int hiddenLayers, double learningRate, int batchSize) throws IOException, CloneNotSupportedException {
        double[][] trainingData = readData("training", batchSize, state);
        double[][] trainingAnswers = readAnswers("training", state);
        double[][] standardizedData = standardizeData(trainingData);
        NeuralNetwork nn = new NeuralNetwork(8, hiddenLayers, 1, learningRate);
        nn.fit(standardizedData, trainingAnswers, epochs, activationFunction);

        double[][] testingData = readData("testing", Integer.MAX_VALUE, "NV");
        double[][] testingAnswers = readAnswers("testing", "NV");
        FileWriter writer = new FileWriter(FILEPATH + "data\\" + state + "_evaluation.txt");
        double threshold = 0;
        if(activationFunction.equals("sigmoid")){
            threshold = .5;
        }else{
            threshold = 0;
        }
        ArrayList<UserResult> results = new ArrayList<>();
        ArrayList<String> users = getTestUsers("NV");
        for(int i = 0; i < testingData.length; i++){
            double output = nn.predict(testingData[i]).get(0);
            double answer = testingAnswers[i][0];
            writer.write(output + "\t" + answer + "\n");
            if(output > threshold){
                results.add(new UserResult(users.get(i), output));
            }
        }
        writer.close();
        //write a trec_eval file.
        Collections.sort(results);
        Collections.reverse(results);
        FileWriter trecWriter = new FileWriter(FILEPATH + "evaluation\\"+ state + "_" + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + "_bs" + batchSize + ".txt");
        for(int i = 0; i < results.size(); i++){
            UserResult result = results.get(i);
            trecWriter.write("1 0 " + state + "-" + result.userId + " " + (i + 1) + " " + result.resultNum + " " + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + "\n");
        }
        trecWriter.close();

    }
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        run("NV", 10000, "gaussian", 4, .001, 1000);
    }
}
