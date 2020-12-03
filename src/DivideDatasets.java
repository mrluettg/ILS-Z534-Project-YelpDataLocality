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
        String trainingPath = "D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\calculateLocals_output_" + state +  ".txt";
        //String testingPath = "D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\calculateLocals_output_" + state +  "_testing.txt";
        //String validationPath = "D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\calculateLocals_output_" + state +  "_validation.txt";
        FileWriter trainingWriter = new FileWriter(trainingPath);
        //FileWriter testingWriter = new FileWriter(testingPath);
        //FileWriter validationWriter = new FileWriter(validationPath);
        int incrementer = 0;
        int training = 0;
        int testing = 0;
        int validation = 0;
        int unused = 0;
        while(scanner.hasNextLine()){
            int i = incrementer % 100;
            String line = scanner.nextLine() + "\n";
            if(i == 9){
                //validationWriter.write(line);
                //validation++;
            }
            if (i == 8){
                //testingWriter.write(line);
                //testing ++;
            }
            if (i < 8){
                trainingWriter.write(line);
                training++;
            }
            if(i> 9){
                unused++;
            }
            incrementer ++;
        }
        //testingWriter.close();
        trainingWriter.close();
        //validationWriter.close();
        System.out.println("training: " + training + "\ntesting: " + testing + "\nvalidation: " + validation + "\nunused: " + unused);
    }
    public static void main(String[] args) throws IOException {
        shortenDatasets("NV");
    }
}
