//Matt Luettgen
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

//to be done after CosineSimilarity.java
    //uses same data.
public class OtherFeatures {
    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";
    public static HashMap<String, Double> getUserAvgCosine(String state) throws FileNotFoundException {
        File file = new File(FILEPATH + "dict\\" + state + "_user_avg_cosine_similarity.txt");
        Scanner scanner = new Scanner(file);
        HashMap<String, Double> hm = new HashMap<>();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineLst = line.split("\t");
            String user = lineLst[0];
            double tfidf = Double.parseDouble(lineLst[1]);
            hm.put(user, tfidf);
        }
        return hm;
    }

    //we want the average latitude/longitude (business)
    //the average stars(business)
    //
    //returns a list of double.
    /**
     *     [review count    0
     *     stars     1
     *     latitude                                2
     *     longitude                                    3
     *     ]
     */

    public static HashMap<String, Double[]> gatherBusinessInformation(String[] users, String state) throws IOException, org.json.simple.parser.ParseException {
        System.out.println("gathering information from business file");
        HashMap<String, LinkedHashSet<Integer>> userReviews = CosineSimilarity.readUserReviewsHashMap(state);
        HashMap<String, LinkedHashSet<Integer>> businessReviews = CosineSimilarity.readBusinessReviewsHashMap(state);
        HashMap<Integer, String> reviewBusinesses = new HashMap<>();
        String[] businesses = businessReviews.keySet().toArray(new String[0]);
        for (String business : businesses) {
            LinkedHashSet<Integer> reviews = businessReviews.get(business);
            for (Integer review : reviews) {
                reviewBusinesses.put(review, business);
            }
        }
        LinkedHashSet<String> allBusinessIds = new LinkedHashSet<>();
        for (String user : users) {
            LinkedHashSet<Integer> ints = userReviews.get(user);
            for (Integer i : ints) {
                String businessID = reviewBusinesses.get(i);
                allBusinessIds.add(businessID);
            }
        }
        //double[] as described in green above.
        HashMap<String, Double[]> businessInformation = new HashMap<>();
        String path = FILEPATH + "yelp_academic_dataset_business.json";
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String businessId = (String) jo.get("business_id");
            if (allBusinessIds.contains(businessId)) {
                double reviewCount = (Long) jo.get("review_count");
                double stars = (Double) jo.get("stars");
                double latitude = (Double) jo.get("latitude");
                double longitude = (Double) jo.get("longitude");
                Double[] information = new Double[]{reviewCount, stars, latitude, longitude};
                businessInformation.put(businessId, information);
            }
        }
        return businessInformation;
    }

    /**
     *double[] is
     * {stars 0,
     * useful 1,
     * funny 2,
     * cool 3}
     *
     * @param users
     * @param state
     * @return
     */

    public static HashMap<Integer, Double[]> gatherReviewInformation(String[] users, String state) throws IOException, ParseException {
        System.out.println("gathering information review business file");
        HashMap<String, LinkedHashSet<Integer>> userReviews = CosineSimilarity.readUserReviewsHashMap(state);
        LinkedHashSet<String> allReviewIds = new LinkedHashSet<>();
        HashMap<String, Integer> reviewLookup = CosineSimilarity.readIntegerReviewIds(state);
        HashMap<Integer, String> reverseReviewLookup = CosineSimilarity.reverseLookup(reviewLookup);
        for(String user: users) {
            LinkedHashSet<Integer> reviews = userReviews.get(user);
            for(Integer review: reviews){
                allReviewIds.add(reverseReviewLookup.get(review));
            }
        }
        HashMap<Integer, Double[]> reviewInformation = new HashMap<>();
        String path = FILEPATH + "yelp_academic_dataset_review.json";
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String reviewId = (String) jo.get("review_id");
            if (allReviewIds.contains(reviewId)) {
                double reviewCount = (Double) jo.get("stars");
                double stars = (Long) jo.get("useful");
                double latitude = (Long) jo.get("funny");
                double longitude = (Long) jo.get("cool");
                Double[] information = new Double[]{reviewCount, stars, latitude, longitude};
                reviewInformation.put(reviewLookup.get(reviewId), information);
            }
        }
        return reviewInformation;
    }

    /**
     * double[] as in:
     * 0    avg cosine difference
     * 1    avg reviewed business stars
     * 2    avg difference b/t rating user gave and business stars
     * 3    avg useful/reviewCount(business)
     * 4    avg funny/reviewCount(business)
     * 5    avg cool/reviewCount(business)
     * 6    avg reviewed business longitude
     * 7    avg reviewed business latitude
     */

    public static HashMap<String, Double[]> calculateFeatures(String state) throws IOException, ParseException {
        System.out.println("calculating features");
        HashMap<String, Double[]> featureSet = new HashMap<>();
        HashMap<String, Double> avgCosineDifference = getUserAvgCosine(state);
        HashMap<String, LinkedHashSet<Integer>> userReviews = CosineSimilarity.readUserReviewsHashMap(state);
        String[] users = avgCosineDifference.keySet().toArray(new String[0]);
        HashMap<Integer, Double[]> reviewInformation = gatherReviewInformation(users, state);
        HashMap<String, Double[]> businessInformation = gatherBusinessInformation(users, state);
        HashMap<String, LinkedHashSet<Integer>> businessReviews = CosineSimilarity.readBusinessReviewsHashMap(state);
        HashMap<Integer, String> reviewBusinesses = new HashMap<>();
        String[] businesses = businessReviews.keySet().toArray(new String[0]);
        for (String business : businesses) {
            LinkedHashSet<Integer> reviews = businessReviews.get(business);
            for (Integer review : reviews) {
                reviewBusinesses.put(review, business);
            }
        }
        for(String user: users){
            Double[] features = new Double[8];
            features[0] = avgCosineDifference.get(user);
            LinkedHashSet<Integer> reviews = userReviews.get(user);
            double userNumReviews = reviews.size();
            double avgReviewedBusinessStars = 0;
            double avgRatingDifference = 0;
            double avgUseful = 0;
            double avgFunny = 0;
            double avgCool = 0;
            double avgLatitude = 0;
            double avgLongitude = 0;
            for(Integer review: reviews){
                //{stars 0, useful 1, funny 2, cool 3}
                Double[] reviewInfo = reviewInformation.get(review);
                //review_count 0, stars 1, longitude 2, latitude 3
                Double[] businessInfo = businessInformation.get(reviewBusinesses.get(review));
                avgUseful += reviewInfo[1]/userNumReviews;
                avgFunny += reviewInfo[2]/userNumReviews;
                avgCool += reviewInfo[3]/userNumReviews;

                avgReviewedBusinessStars += businessInfo[1]/userNumReviews;
                avgRatingDifference += (businessInfo[1] - reviewInfo[0]);
                avgLatitude += businessInfo[2]/userNumReviews;
                avgLongitude += businessInfo[3]/userNumReviews;
            }
            features[1] = avgReviewedBusinessStars;
            features[2] = avgRatingDifference;
            features[3] = avgUseful;
            features[4] = avgFunny;
            features[5] = avgCool;
            features[6] = avgLatitude;
            features[7] = avgLongitude;
            featureSet.put(user, features);
        }

        return featureSet;
    }

    public static void writeDataSets(String state) throws IOException, ParseException {
        System.out.println("writing datasets");
        String[] users = getUserAvgCosine(state).keySet().toArray(new String[0]);
        HashMap<String, Double[]> featureSet = calculateFeatures(state);
        LinkedHashSet<String> allUsers = new LinkedHashSet<>(Arrays.asList(users));
        HashMap<String, Integer> answers = new HashMap<>();
        File file = new File(FILEPATH + "calculateLocals_output\\" + state + "\\calculateLocals_output_" + state + ".txt");
        Scanner scanner = new Scanner(file);
        int incrementer = 0;
        FileWriter trainingWriter = new FileWriter(FILEPATH + "\\data\\" + state + "_training.txt");
        FileWriter trainingAnswersWriter =  new FileWriter(FILEPATH + "\\data\\" + state + "_training_answers.txt");
        FileWriter testingWriter = new FileWriter(FILEPATH + "\\data\\" + state + "_testing.txt");
        FileWriter testingAnswersWriter =  new FileWriter(FILEPATH + "\\data\\" + state + "_testing_answers.txt");
        FileWriter trecEvalAnswers = new FileWriter(FILEPATH + "\\evaluation\\" + "state_qrels");
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineLst = line.split("\t");
            String userId = lineLst[1];
            if(allUsers.contains(userId)){
                String answer = lineLst[2];
                Double[] features = featureSet.get(userId);
                StringBuilder out = new StringBuilder(userId);
                for(Double feature: features){
                    out.append("\t").append(feature);
                }
                if(incrementer % 10 >= 7){
                    //testing
                    testingAnswersWriter.write(userId + "\t" + answer + "\n");
                    testingWriter.write(out.toString() + "\n");
                }
                else{
                    trainingAnswersWriter.write(userId + "\t" + answer + "\n");
                    trainingWriter.write(out.toString() + "\n");
                }
                trecEvalAnswers.write("1 0 " + state + "-" + userId + " " + answer + "\n");
                incrementer += 1;
            }

        }
        trainingWriter.close();
        testingWriter.close();
        trainingAnswersWriter.close();
        testingAnswersWriter.close();

    }
    public static void main(String[] args) throws IOException, ParseException {
        writeDataSets("NV");
    }
}
