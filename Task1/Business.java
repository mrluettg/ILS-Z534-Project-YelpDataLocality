import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Collections;


//Task1 Algorithm 1. Uses AVG star rating multiplied by total number of reviews to rank
public class Business implements Comparable<Business> {

    public String BID;
    public String Bname;
    public String Bstate;
    public String Bcity;
    public Double Bstars;
    public String Bcategory;
    public Long BreviewCount;
    public Integer Btotal;



    public Business(String ID, String name, String state, String city,String category, Double stars, Long reviewCount){
        this.BID = ID;
        this.Bname = name;
        this.Bstate = state;
        this.Bcity = city;
        this.Bcategory = category;
        this.Bstars = stars;
        this.BreviewCount = reviewCount;



        Double doubleTotal = stars * reviewCount;
        this.Btotal =  (int) Math.round(doubleTotal);

    }

    public static ArrayList<Business> fileParser(String filePath) throws IOException, ParseException {
        ArrayList<Business> documents = new ArrayList<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;

            String businessId = (String) jo.get("business_id");
            String name = (String) jo.get("name");
            String state = (String) jo.get("state");
            String city = (String) jo.get("city");
            String category = (String) jo.get("categories");
            Double stars = (Double) jo.get("stars");
            Long review_count = (Long) jo.get("review_count");

            Business B = new Business (businessId, name, state, city, category, stars, review_count);


            documents.add(B);
            //System.out.println(B.BID + ": " + B.Bname + ": " + B.Btotal);
        }
        return documents;
    }


    public static void getBusiness(){

    }

    @Override
    public int compareTo(Business b){
        return Integer.compare(this.Btotal, b.Btotal);
    }

    public static void display(Business b, Integer rank){ //prints each business based on user query and rank
        System.out.println(rank + ". " + b.Bname + ", Stars: " + Math.round(b.Bstars) + ", Review Count: " + b.BreviewCount +  ", Score: " + b.Btotal);
    }

    public static ArrayList getBusinesses()throws IOException, ParseException{
        ArrayList<Business> businesses = fileParser("D:\\yelp_academic_dataset_business.json");
        return businesses;
    }



    public static void main(String[] args)throws IOException, ParseException{

        ArrayList<Business> businessesUpdated = new ArrayList<>();

        ArrayList<Business> businesses = fileParser("D:\\yelp_academic_dataset_business.json");
        Collections.sort(businesses);
        Collections.reverse(businesses);

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter City");
        String location = scan.nextLine(); //get user input

        System.out.println("Enter category");
        String category = scan.nextLine(); //get user input but does not work

        System.out.println("Enter Minimum Star Rating (1-5):");
        String rating = scan.nextLine(); //get user input
        int intRating = Integer.parseInt(rating);

        System.out.println("Searching for: " + category + " in " + location + " with " + rating + " star(s).");



        for(int i = 0; i < businesses.size(); i++){
            Business b = businesses.get(i);
            int intStars = (int) Math.round(b.Bstars);
            if(b.Bcity.toLowerCase().equals(location.toLowerCase())){
               //if(b.Bcategory.toLowerCase().contains(category.toLowerCase())){ //doesnt work
                    if(intStars >= intRating){
                    businessesUpdated.add(b);
                   }
               //}
            }
        }


        for(int i = 0; i < businessesUpdated.size(); i++) {
            Business b = businessesUpdated.get(i);
            display(b, (i+1));;
        }



    }


}
