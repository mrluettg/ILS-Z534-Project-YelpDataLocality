import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Collections;

//Timothy Niles
//Review Object class


public class Review {

    public String bID;
    public String rID;
    public Long useful;
    public Long funny;
    public Long cool;
    public Integer commentTotal;


    public Review(String bisID, String revID, Long usefulCount, Long funnyCount, Long coolCount){
        this.bID = bisID;
        this.rID = revID;
        this.useful = usefulCount;
        this.funny = funnyCount;
        this.cool = coolCount;

        this.commentTotal = (int) ((usefulCount + funnyCount + coolCount));


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


            Review r = new Review (businessId, reviewId, useful, funny, cool);


            reviews.add(r);
        }
        return reviews;
    }



    public static ArrayList getReviews()throws IOException, ParseException{
        System.out.println("Loading Reviews...");
        ArrayList<Review> reviews = fileParser("D:\\yelp_academic_dataset_review.json");
        return reviews;
    }


    public static void main(String[] args){
    }


}
