import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Collections;


//Algorithm 2. Uses total number of useful, cool, and funny reviews divided by review stars


public class Review implements Comparable<Review> {

    public String bID;
    public String rID;
    public Long useful;
    public Long funny;
    public Long cool;
    public Integer commentTotal;
    public Double stars;


    public Review(String bisID, String revID, Long usefulCount, Long funnyCount, Long coolCount, Double stars){
        this.bID = bisID;
        this.rID = revID;
        this.useful = usefulCount;
        this.funny = funnyCount;
        this.cool = coolCount;
        this.stars = stars;

        this.commentTotal = (int) ((usefulCount + funnyCount + coolCount)/stars);


    }

    public static ArrayList<Review> fileParser(String filePath) throws IOException, ParseException {
        ArrayList<Review> reviews = new ArrayList<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;

            String businessId = (String) jo.get("business_id");
            String reviewId = (String) jo.get("review_id");
            Long useful = (Long) jo.get("useful");
            Long funny = (Long) jo.get("funny");
            Long cool = (Long) jo.get("cool");
            Double stars = (Double) jo.get("stars");


            Review r = new Review (businessId, reviewId, useful, funny, cool, stars);


            reviews.add(r);
        }
        return reviews;
    }



    @Override
    public int compareTo(Review r){
        return Integer.compare(this.commentTotal, r.commentTotal);
    }

    public static void display(int commentTotal, Business b, Integer rank){

        System.out.println(rank + ". " + b.Bname + ", Stars: " + Math.round(b.Bstars) + ", Review Count: " + b.BreviewCount +  ", Score: " + commentTotal);
    }
    public static void main(String[] args)throws IOException, ParseException{

        ArrayList<Business> businessesUpdated = new ArrayList<>();
        ArrayList<Review> reviewsUpdated = new ArrayList<>();

        System.out.println("Loading Reviews...");
        ArrayList<Review> reviews = fileParser("D:\\yelp_academic_dataset_review.json");
        System.out.println("Loading Businesses...");
        ArrayList<Business> businesses = new ArrayList<>();
        businesses = Business.getBusinesses();


        System.out.println("Sorting...");
        Collections.sort(reviews);
        Collections.reverse(reviews);
        Collections.sort(businesses);
        Collections.reverse(businesses);
        System.out.println();
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter City");
        String location = scan.nextLine(); //get user input

        System.out.println("Enter category");
        String category = scan.nextLine(); //get user input


        System.out.println("Searching for: " + category + " in " + location);



        for(int i = 0; i < businesses.size(); i++){
            Business b = businesses.get(i);
            if(b.Bcity.toLowerCase().equals(location.toLowerCase())){
                //if(r.Bcategory.toLowerCase().contains(category.toLowerCase())){
                    businessesUpdated.add(b);

                //}
            }
        }

        for(int i = 0; i < businessesUpdated.size(); i++){
            Business b = businesses.get(i);
            long c = 0;
            long f = 0;
            long u = 0;
            Integer rStars = 0;


            //System.out.println(r);
            for(int j = 0; j < reviews.size(); j++){
                Review r = reviews.get(j);
                c += r.cool;
                f += r.funny;
                u += r.useful;
                rStars += (int) Math.round(r.stars);
                if(r.bID.equals(b.BID)){
                    reviewsUpdated.add(r);

                }

            }
            int total = (int) ((c + f + u)/ rStars);
            display(total, b, (i + 1));
        }
        System.out.println(reviewsUpdated);





    }


}
