/**
 * splits the users into a 8-1-1 training-testing-validation ratio.
 */
import java.io.*;
import java.util.Scanner;


public class DivideDatasets {
    public static void shortenDatasets(String state) throws IOException {
        String filePath = "D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\calculateLocals_output_" + state +  ".txt";
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        String trainingPath = "D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\" + state + "_calculateLocals_output_short.txt";
        FileWriter trainingWriter = new FileWriter(trainingPath);
        int count = 0;
        int incrementer = 0;
        while(scanner.hasNextLine()){
            int i = incrementer % 100;
            String line = scanner.nextLine() + "\n";
            if (i % 100 < 5){
                trainingWriter.write(line);
                count++;
            }
            incrementer ++;
        }
        //testingWriter.close();
        trainingWriter.close();
        //validationWriter.close();
        System.out.println(count + "/" + incrementer + " users selected");
    }
    public static void main(String[] args) throws IOException {
        shortenDatasets("NV");
    }
}
