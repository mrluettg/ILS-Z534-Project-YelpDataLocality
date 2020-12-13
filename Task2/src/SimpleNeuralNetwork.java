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
            //System.out.println("error: " + error.data[0][0]);
            Matrix gradient = null;
            if(activationFunction.equals("sigmoid")){ gradient = output.dsigmoid();}
            else{
                if(activationFunction.equals("gaussian")){
                    gradient = outputClone.dgaussian();
                }
            }

            gradient.multiply(error);
            gradient.multiply(l_rate);

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
    public static double[][] readData(String dataset, int[] featuresToInclude, String state) throws FileNotFoundException {
        List<double[]> lst = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_" + dataset + ".txt"));
        int N = 0;
        int numFeatures = featuresToInclude.length;
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            String[] user = line.split("\t");
            double[] userData = new double[numFeatures];
            for(int i = 0; i < numFeatures; i++){
                userData[i] = Double.parseDouble(user[ 1 + featuresToInclude[i]]);
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
    public static ArrayList<String> getUsers(String data, String state) throws FileNotFoundException {
        ArrayList<String> users = new ArrayList<String>();
        Scanner scanner = new Scanner(new File(FILEPATH + "\\data\\" + state + "_" + data +  "_answers.txt"));
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
    //for sorting the results
    public static class UserResult implements Comparable<UserResult>{
        String userId;
        double resultNum;
        double answer;
        public UserResult(String id, double rn, double a){
            this.userId = id;
            this.resultNum = rn;
            this.answer = a;
        }

        @Override
        public int compareTo(UserResult o) {
            return Double.compare(this.resultNum, o.resultNum);
        }
    }


    public static class NNRun implements Comparable<NNRun>{
        NeuralNetwork model;
        double precision;
        double recall;
        double accuracy;
        double F;
        public NNRun(NeuralNetwork model, double precision, double recall, double accuracy){
            this.model = model;
            this.precision = precision;
            this.recall = recall;
            this.accuracy = accuracy;
            this.F = 2*(precision * recall)/(precision + recall);
        }
        @Override
        public int compareTo(NNRun o) {
            return Double.compare(this.F, o.F);
        }
        public void display(){
            System.out.println("Neural Network Run");
            System.out.println("\tPrecision: " + precision);
            System.out.println("\tRecall: " + recall);
            System.out.println("\tAccuracy: " + accuracy);
            System.out.println("\tFScore: " + F);
        }
    }
    public static NNRun runNeuralNetwork(String state, int[] featuresToInclude, int epochs, String activationFunction, int hiddenLayers, double learningRate) throws IOException, CloneNotSupportedException {
        /**
         * features to include:
         * 0    avg cosine difference
         * 1    avg reviewed business stars
         * 2    avg difference b/t rating user gave and business stars
         * 3    avg useful/reviewCount(business)
         * 4    avg funny/reviewCount(business)
         * 5    avg cool/reviewCount(business)
         * 6    avg review count(business)
         * 7    avg reviewed business longitude
         * 8    avg reviewed business latitude
         */
        //Lowest one in OtherFeatures.java.
        double[][] trainingData = readData("training", featuresToInclude, state);
        double[][] trainingAnswers = readAnswers("training", state);
        NeuralNetwork nn = new NeuralNetwork(6, hiddenLayers, 1, learningRate);
        nn.fit(trainingData, trainingAnswers, epochs, activationFunction);

        double[][] validationData = readData("validation", featuresToInclude, state);
        double[][] validationAnswers = readAnswers("validation", state);
        FileWriter writer = new FileWriter(FILEPATH + "data\\" + state + "_evaluation.txt");
        //get results for neural network.
        double threshold;
        if(activationFunction.equals("sigmoid")){
            threshold = .5;
        }else{
            threshold = 0;
        }
        ArrayList<UserResult> results = new ArrayList<>();
        ArrayList<String> users = getUsers("validation", state);
        for(int i = 0; i < validationData.length; i++){
            double output = nn.predict(validationData[i]).get(0);
            double answer = validationAnswers[i][0];
            if(output > threshold){
                results.add(new UserResult(users.get(i), output, answer));
            }
        }
        writer.close();
        //do precision, recall, accuracy.
        double numResults = results.size();
        double truePositive = 0;
        for(UserResult ur: results){
            if(ur.answer == 1){
                truePositive++;
            }
        }
        double falsePositive = numResults - truePositive;
        double precision = truePositive/numResults;
        double positiveTotal= 0;
        for(double[] d: validationAnswers){
            if(d[0] == 1){
                positiveTotal ++;
            }
        }
        double negativeTotal = validationAnswers.length - positiveTotal;
        double falseNegative = positiveTotal - truePositive;
        double trueNegative = negativeTotal - falseNegative;
        double recall = truePositive/positiveTotal;
        double accuracy = (truePositive+trueNegative)/(positiveTotal + negativeTotal);

        /**
        //write a trec_eval file.
        Collections.sort(results);
        Collections.reverse(results);
        FileWriter trecWriter = new FileWriter(FILEPATH + "evaluation\\"+ state + "_" + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + ".txt");
        for(int i = 0; i < results.size(); i++){
            UserResult result = results.get(i);
            trecWriter.write("1 0 " + state + "-" + result.userId + " " + (i + 1) + " " + result.resultNum + " " + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + "\n");
        }
        trecWriter.close();
        System.out.println("results size: " + results.size());
        System.out.println("Precision: " + precision+ " Recall: " + recall);
         **/
        return new NNRun(nn, precision, recall, accuracy);

    }
    //trains a neural network and returns the run with the highest F score.
    public static NNRun runNetworkNTimes(int numRuns, String state, int[] featuresToInclude, int epoch, String activationFunction, int hiddenLayers, double learningRate) throws IOException, CloneNotSupportedException {
        ArrayList<NNRun> runs = new ArrayList<>();
        for(int i = 0; i < numRuns; i++){
            runs.add(runNeuralNetwork(state, featuresToInclude, epoch, activationFunction, hiddenLayers, learningRate));
        }
        Collections.sort(runs);
        Collections.reverse(runs);
        return runs.get(0);
    }
    //finds the best Neural Network model after training 100 times. Evaluates the result on a new dataset.
    public static NNRun evaluateBestNN(String state, int[] featuresToInclude, int epoch, String activationFunction, int hiddenLayers, double learningRate) throws IOException, CloneNotSupportedException {
        NeuralNetwork nn = runNetworkNTimes(100, state, featuresToInclude, epoch, activationFunction, hiddenLayers, learningRate).model;
        double[][] testingData = readData("testing", featuresToInclude, state);
        double[][] testingAnswers = readAnswers("testing", state);
        FileWriter writer = new FileWriter(FILEPATH + "data\\" + state + "_evaluation.txt");
        //get results for neural network.
        double threshold;
        if(activationFunction.equals("sigmoid")){
            threshold = .5;
        }else{
            threshold = 0;
        }
        ArrayList<UserResult> results = new ArrayList<>();
        ArrayList<String> users = getUsers("testing", state);
        for(int i = 0; i < testingData.length; i++){
            double output = nn.predict(testingData[i]).get(0);
            double answer = testingAnswers[i][0];
            if(output > threshold){
                results.add(new UserResult(users.get(i), output, answer));
            }
        }
        writer.close();
        //do precision, recall, accuracy.
        double numResults = results.size();
        double truePositive = 0;
        for(UserResult ur: results){
            if(ur.answer == 1){
                truePositive++;
            }
        }
        double falsePositive = numResults - truePositive;
        double precision = truePositive/numResults;
        double positiveTotal= 0;
        for(double[] d: testingAnswers){
            if(d[0] == 1){
                positiveTotal ++;
            }
        }
        double negativeTotal = testingAnswers.length - positiveTotal;
        double falseNegative = positiveTotal - truePositive;
        double trueNegative = negativeTotal - falseNegative;
        double recall = truePositive/positiveTotal;
        double accuracy = (truePositive+trueNegative)/(positiveTotal + negativeTotal);
         //write a trec_eval file.
         Collections.sort(results);
         Collections.reverse(results);
         FileWriter trecWriter = new FileWriter(FILEPATH + "evaluation\\"+ state + "_" + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + ".txt");
         for(int i = 0; i < results.size(); i++){
         UserResult result = results.get(i);
         trecWriter.write("1 0 " + state + "-" + result.userId + " " + (i + 1) + " " + result.resultNum + " " + activationFunction + "_lr" + learningRate + "_h" + hiddenLayers + "\n");
         }
         trecWriter.close();
         return new NNRun(nn, precision, recall, accuracy);
    }
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        NNRun best = evaluateBestNN("NV", new int[] {0, 1, 2, 6, 7, 8}, 10000, "sigmoid", 16, .001);
        best.display();
    }
}
