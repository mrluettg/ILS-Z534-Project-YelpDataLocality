//Matt Luettgen

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.math3.stat.inference.TTest;



public class tTests {
    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";
    public static ArrayList<double[]> readFile(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filePath));
        ArrayList<double[]> allFeatures = new ArrayList<>();
        while(scanner.hasNextLine()){
            String[] lineLst = scanner.nextLine().split("\t");
            double[] features = new double[lineLst.length - 1];
            for(int i = 1; i < lineLst.length; i++){
                features[i - 1] = Double.parseDouble(lineLst[i]);
            }
            allFeatures.add(features);
        }
        return allFeatures;
    }
    //returns p-value

    public static double tTest(double[] locals, double[] nonLocals){
        TTest tt = new TTest();
        return tt.tTest(locals, nonLocals);
    }
    public static double[][] transpose(ArrayList<double[]> array){
        int length = array.size();
        int width = array.get(0).length;
        double[][] transposedArray = new double[width][length];
        for(int i = 0; i < array.size(); i++){
            double[] lst = array.get(i);
            for(int j = 0; j < width; j++){
                transposedArray[j][i] = lst[j];
            }
        }
        return transposedArray;
    }
    public static double findMean(double[] ds){
        double mean = 0;
        double n = ds.length;
        for(double d: ds){
            mean += d/n;
        }
        return mean;
    }
    public static double findSd(double mean, double[] ds){
        double sd = 0;
        double n = ds.length;
        for(double d: ds){
            sd += Math.pow(mean - d, 2)/n;
        }
        sd = Math.sqrt(sd);
        return sd;
    }
    public static void run(String state) throws FileNotFoundException {
        double[][] locals = transpose(readFile(FILEPATH + "\\data\\" + state + "_allLocals.txt"));
        double[][] nonLocals = transpose(readFile(FILEPATH + "\\data\\" + state + "_allNonLocals.txt"));
        for(int i = 0; i < locals.length; i++){
            double[] localFeatures = locals[i];
            double[] nonLocalFeatures = nonLocals[i];
            double localMean = findMean(localFeatures);
            double nonLocalMean = findMean(nonLocalFeatures);
            double localSd = findSd(localMean, localFeatures);
            double nonLocalSd = findSd(nonLocalMean, nonLocalFeatures);
            double pValue = tTest(localFeatures, nonLocalFeatures);
            System.out.println(i + " localMean: " + localMean +
                    " nonLocalMean: " + nonLocalMean +
                    " localSd: " + localSd +
                    " nonLocalSd: " + nonLocalSd +
                    " tTest p value: " + pValue);
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        run("NV");
    }
    /**
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

}
