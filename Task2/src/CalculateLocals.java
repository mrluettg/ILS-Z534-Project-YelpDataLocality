//Matt Luettgen
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;

public class CalculateLocals {
    //where everything is stored.
    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";


    public static ArrayList<String[]> fileParser(String filePath, String[] fields) throws IOException, ParseException {
        ArrayList<String[]> documents = new ArrayList<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
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
    //reads the business index
    //generates a HashMap with Business ID as key and the state as a value.
    public static HashMap<String, String> generateBusinessAreaHashMap() {
        String indexPath = FILEPATH + "index\\business";
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

    //trim this hashmap so that it only contains businesses of a particular state.
    public static void trimBusinessAreaHashmap(HashMap<String, String> businessAreaHashMap, String state){
        String[] businesses = businessAreaHashMap.keySet().toArray(new String[0]);
        for(String business: businesses){
            if(!businessAreaHashMap.get(business).equals(state)){
                businessAreaHashMap.remove(business);
            }
        }
    }


    public static HashMap<String, ArrayList<String[]>> generateUserBusinessDateHashMap() throws IOException, ParseException {
        String filePath = FILEPATH + "yelp_academic_dataset_review.json";
        HashMap<String, ArrayList<String[]>> userBusinessDate = new HashMap<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String userId = (String) jo.get("user_id");
            String businessId = (String) jo.get("business_id");
            String date = (String) jo.get("date");
            date = date.substring(0, date.indexOf(" "));
            String[] bd = new String[]{businessId, date}; //bd for business-date
            if(userBusinessDate.containsKey(userId)){
                userBusinessDate.get(userId).add(bd);
            }else{
                ArrayList<String[]> bds = new ArrayList<>();
                bds.add(bd);
                userBusinessDate.put(userId, bds);
            }
        }

        return userBusinessDate;
    }
    public static void writeUserBusinessDateDictFile(HashMap<String, ArrayList<String[]>> dict) throws IOException {
        FileWriter writer = new FileWriter(FILEPATH + "dict\\user_business_date_dict.txt");
        String[] keys = dict.keySet().toArray(new String[0]);
        int s = dict.size();
        for(int i = 0; i < s; i++){
            String key = keys[i];
            writer.write(key + "\t");
            ArrayList<String[]> bds = dict.get(key); //bd for business-date
            for(String[] bd: bds) {
                writer.write(bd[0] + " " + bd[1] + "\t");
            }
            writer.write("\n");
        }
    }

    public static HashMap<String, ArrayList<String[]>> readUserBusinessDateDictFile() throws IOException {
        HashMap<String, ArrayList<String[]>> userBusinessDate = new HashMap<>();
        File file = new File(FILEPATH +  "dict\\user_business_dict.txt");
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineArray = line.split("\t");
            String user = lineArray[0];
            ArrayList<String[]> businessDate = new ArrayList<>();
            for(int i = 1; i < lineArray.length; i++){
                String[] bd = lineArray[i].split(" ");
                businessDate.add(bd);
            }
            userBusinessDate.put(user, businessDate);
        }
        return userBusinessDate;
    }

    //trim the hashmaps so it only has businesses within a particular state.
    public static void trimUserBusinessDateHashMap(HashMap<String, ArrayList<String[]>> userBusinessDateHashMap, HashMap<String, String> businessStateHashMap){
        String[] users = userBusinessDateHashMap.keySet().toArray(new String[0]);
        for(String user: users){
            ArrayList<String[]> bds = userBusinessDateHashMap.get(user);
            ArrayList<String[]> bdsNew = new ArrayList<>();
            boolean hasReviewInArea = false;
            for(String[] bd: bds){
                String businessId = bd[0];
                if(businessStateHashMap.containsKey(businessId)){
                    hasReviewInArea = true;
                    bdsNew.add(bd);
                }
            }
            if(!hasReviewInArea){
                userBusinessDateHashMap.remove(user);
            }else{
                userBusinessDateHashMap.replace(user, bdsNew);
            }
        }
    }
    //for the business-date arraylist of every user (user visited business b at date d, all business in the same metro area))
    //subtracts the newest from the oldest date.
    public static boolean minMaxDateThreshold(ArrayList<String[]> bds){
        if(bds.size() < 2){return false;}
        LocalDate firstDate = LocalDate.parse(bds.get(0)[1], DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate min = firstDate;
        LocalDate max = firstDate;
        for(String[] bd: bds) {
            LocalDate date = LocalDate.parse(bd[1], DateTimeFormatter.ISO_LOCAL_DATE);
            if (date.isBefore(min)) {
                min = date;
            }
            if (date.isAfter(max)) {
                max = date;
            }
        }
        LocalDate threshold = LocalDate.parse("0001-01-01", DateTimeFormatter.ISO_LOCAL_DATE);
        int year = min.getYear();
        int day = min.getDayOfYear();
        max = max.minusDays(day);
        max = max.minusYears(year);
        return max.isAfter(threshold);
    }
    //returns a dictionary with the user_id to the review_count
    public static HashMap<String, Integer> generateUserCountHashMap() throws IOException, ParseException {
        ArrayList<String[]> userCounts = fileParser(FILEPATH + "yelp_academic_dataset_user.json",
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
        FileWriter writer = new FileWriter(FILEPATH + "dict\\user_count_dict.txt");
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
        File file = new File(FILEPATH + "dict\\user_count_dict.txt");
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


    public static HashMap<String, Integer> calculateLocals(String state) throws IOException {
        System.out.println("generating business area hashmap");
        HashMap<String, String> businessStates = generateBusinessAreaHashMap();
        System.out.println("trimming business area hashmap");
        trimBusinessAreaHashmap(businessStates, state);
        System.out.println("generating userReviewLocations hashmap");
        HashMap<String, ArrayList<String[]>> userBusinessDateHashMap = readUserBusinessDateDictFile();
        System.out.println(userBusinessDateHashMap.size());
        System.out.println("Trimming userBusinessDateHashMap");
        trimUserBusinessDateHashMap(userBusinessDateHashMap, businessStates);
        System.out.println(userBusinessDateHashMap.size());
        System.out.println("generating user counts hashmap");
        HashMap<String, Integer> userCounts = readUserCountDictFile();
        String[] users = userBusinessDateHashMap.keySet().toArray(new String[0]);
        HashMap<String, Integer> output = new HashMap<>();
        FileWriter writer = new FileWriter("D:\\yelp_dataset\\yelp_dataset\\calculateLocals_output\\" + state+ "\\calculateLocals_output_" + state +  ".txt");
        int incrementer = 0;
        for(String user: users){
            ArrayList<String[]> bds = userBusinessDateHashMap.get(user);
            boolean isLongTime = minMaxDateThreshold(bds);
            int count;
            if(userCounts.get(user) != null){
                count = userCounts.get(user);
            }else{
                continue;
            }
            int localCount = bds.size();
            boolean isOften = ((double)localCount / (double)count) > .5;
            if(isOften && isLongTime){
                output.put(user, 1);
                writer.write(incrementer + "\t" + user + "\t" + 1 + "\n");
            }else{
                output.put(user, 0);
                writer.write(incrementer + "\t" + user + "\t" + 0 + "\n");
            }
            incrementer++;
        }
        writer.close();
        return output;
    }
    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("generateUserBusinessHashMap()");
        writeUserBusinessDateDictFile(generateUserBusinessDateHashMap());
        System.out.println("generating user review-count HashMap");
        writeUserCountDictFile(generateUserCountHashMap());
        System.out.println("calculateLocals()");
        calculateLocals("NV");

    }
}
