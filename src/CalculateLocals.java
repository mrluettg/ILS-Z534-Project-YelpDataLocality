//creates a dictionary of all the places each user has visited.

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class CalculateLocals {
    //reads the business index
    //generates a HashMap with Business ID as key and the state as a value.
    public static HashMap<String, String> generateBusinessAreaHashMap() {
        String indexPath = ".\\index\\business";
        IndexReader reader = null;
        HashMap<String, String> businessLocations = new HashMap<>();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            //Print the toTAl number of documents in the corpus
            int length =  reader.maxDoc();
            for(int i = 0; i < length; i++){
                businessLocations.put(reader.document(i).get("business_id"), reader.document(i).get("state"));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return businessLocations;
    }
    //reads the core index.
    //generates a HashMap with user_ids as keys and business_ids of the places each user has been to.
    public static HashMap<String, ArrayList<String>> generateUserBusinessHashMap(){
        String indexPath = ".\\index\\core";
        IndexReader reader = null;
        HashMap<String, ArrayList<String>> userReviewLocations = new HashMap<>();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            int length =  reader.maxDoc();
            for(int i = 0; i < length; i++){
                String userId = reader.document(i).get("user_id");
                String businessId = reader.document(i).get("business_id");
                if(userReviewLocations.containsKey(userId)){
                    userReviewLocations.get(userId).add(businessId);
                }else{
                    ArrayList<String> businesses = new ArrayList<>();
                    businesses.add(businessId);
                    userReviewLocations.put(userId, businesses);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userReviewLocations;
    }

    public static void writeUserBusinessDictFile(HashMap<String, ArrayList<String>> dict) throws IOException {
        FileWriter writer = new FileWriter(".\\dict\\user_business_dict.txt");
        String[] keys = dict.keySet().toArray(new String[0]);
        int s = dict.size();
        for(int i = 0; i < s; i++){
            String key = keys[i];
            writer.write(key + "\t");
            ArrayList<String> values = dict.get(key);
            for(String value: values){
                writer.write(value + "\t");
            }
            writer.write("\n");
        }
    }
    public static HashMap<String, ArrayList<String>> readUserBusinessDictFile() throws IOException {
        HashMap<String, ArrayList<String>> userBusinesses = new HashMap<>();
        File file = new File(".\\dict\\user_business_dict.txt");
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineArray = line.split("\t");
            String user = lineArray[0];
            ArrayList<String> businesses = new ArrayList<>();
            for(int i = 1; i < lineArray.length; i++){
                businesses.add(lineArray[i]);
            }
            userBusinesses.put(user, businesses);
        }
        return userBusinesses;
    }


    public static ArrayList<String[]> fileParser(String filePath, String[] fields) throws IOException, ParseException {
        ArrayList<String[]> documents = new ArrayList<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        int j = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String[] document = new String[fields.length];
            for(int i = 0; i < fields.length; i++){
                Object x = jo.get(fields[i]);
                try{
                    document[i] = (String) x;
                }
                catch(ClassCastException e1){
                    try{
                        document[i] = Double.toString((Double)x);
                    }catch(ClassCastException e2){
                        document[i] = Long.toString((Long)x);
                    }
                }

            }
            documents.add(document);
        }
        return documents;
    }
    //returns a dictionary with the user_id to the review_count
    public static HashMap<String, Integer> generateUserCountHashMap() throws IOException, ParseException {
        ArrayList<String[]> userCounts = fileParser("D:\\yelp_dataset\\yelp_dataset\\yelp_academic_dataset_user.json",
                new String[] {"user_id", "review_count"});
        HashMap<String, Integer> userCountHashMap = new HashMap<>();
        int s = userCounts.size();
        for(int i = 0; i < s; i++){
            String[] userCount = userCounts.get(i);
            userCountHashMap.put(userCount[0], Integer.parseInt(userCount[1]));
        }
        return userCountHashMap;
    }


    public static void writeUserCountDictFile(HashMap<String,Integer> dict) throws IOException {
        FileWriter writer = new FileWriter(".\\dict\\user_count_dict.txt");
        String[] keys = dict.keySet().toArray(new String[0]);
        int s = dict.size();
        for(int i = 0; i < s; i++){
            String key = keys[i];
            Integer value = dict.get(key);
            writer.write(key + "\t" + value + "\n");
        }
    }

    public static HashMap<String, Integer> readUserCountDictFile() throws IOException {
        HashMap<String, Integer> userCount = new HashMap<>();
        File file = new File(".\\dict\\user_count_dict.txt");
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineArray = line.split("\t");
            String user = lineArray[0];
            if(lineArray.length > 1){
                Integer count = Integer.parseInt(lineArray[1]);
                userCount.put(user, count);
            }else{
                userCount.put(user, 0);
            }

        }
        return userCount;
    }


    public static void calculateLocals(String state) throws IOException {
        System.out.println("generating business area hashmap");
        HashMap<String, String> businessStates = generateBusinessAreaHashMap();
        System.out.println("generating userReviewLocations hashmap");
        HashMap<String, ArrayList<String>> userReviewLocations = readUserBusinessDictFile();
        System.out.println("generating user counts hashmap");
        HashMap<String, Integer> userCounts = readUserCountDictFile();
        String[] users = userReviewLocations.keySet().toArray(new String[0]);
        for(String user: users){
            ArrayList<String> businesses = userReviewLocations.get(user);
            System.out.println(user);
            Integer count = userCounts.get(user);
            System.out.println(count);
            for(String business: businesses){
                System.out.println("\t" + businessStates.get(business));
            }
        }
    }
    public static void main(String[] args) throws IOException, ParseException {
        //System.out.println("generateBusinessAreaHashMap()");
        //HashMap<String, String> businessStates = generateBusinessAreaHashMap();
        //System.out.println("generateUserBusinessHashMap()");
        //HashMap<String, ArrayList<String>> userReviewLocations = readUserBusinessDictFile();
        System.out.println("calculateLocals()");
        calculateLocals("NV");
    }
}
