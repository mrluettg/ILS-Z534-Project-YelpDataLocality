import java.io.*;
import java.nio.channels.ScatteringByteChannel;
import java.util.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.FilePathProcessor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.lang.Math;


public class CosineSimilarity {
    //finds the tf-idf vector for each review.
    //finds the average tf-idf vector for each business.
    //finds the cosine difference between the average and each review.
    //finds the average cosine difference of all the reviews of a single user.
        //outputs that to a file.

    public static String FILEPATH = "D:\\yelp_dataset\\yelp_dataset\\";
    public static LinkedHashSet<String> getUsers(String state) throws FileNotFoundException {
        File userFile = new File(FILEPATH + "calculateLocals_output\\" + state+ "\\" + state + "_calculateLocals_output_short.txt");
        LinkedHashSet<String> users = new LinkedHashSet<>();
        Scanner userScanner = new Scanner(userFile);
        while(userScanner.hasNextLine()) {
            String line = userScanner.nextLine();
            String userId = line.split("\t")[1];
            users.add(userId);
        }
        return users;
    }
    public static void writeStringIntegerHashMap(HashMap<String, Integer> hm, String filePath) throws IOException{
        FileWriter writer = new FileWriter(filePath);
        String[] keys = hm.keySet().toArray(new String[0]);
        for(String key: keys){
            writer.write(key + "\t" + hm.get(key) + "\n");
        }
        writer.close();
    }
    public static HashMap<String, Integer> readStringIntegerHashMap(String filePath) throws IOException {
        HashMap<String, Integer> hm = new HashMap<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineArray = line.split("\t");
            String key = lineArray[0];
            Integer value = Integer.parseInt(lineArray[1]);
            hm.put(key, value);
        }
        scanner.close();
        return hm;
    }

    public static void writeStringIntegerLhsHashMap(HashMap<String, LinkedHashSet<Integer>> hm, String filePath) throws IOException{
        FileWriter writer = new FileWriter(filePath);
        String[] keys = hm.keySet().toArray(new String[0]);
        for(String key: keys){
            writer.write(key + "\t");
            //System.out.println(key);
            LinkedHashSet<Integer> lhs = hm.get(key); //bd for business-date
            for(int integer: lhs) {
                //System.out.print(integer + "\t");
                writer.write(integer + "\t");
            }
            //System.out.println();
            writer.write("\n");
        }
        writer.close();
    }
    public static HashMap<String, LinkedHashSet<Integer>> readStringIntegerLhsHashMap(String filePath) throws IOException {
        HashMap<String, LinkedHashSet<Integer>> stringIntegerLhs = new HashMap<>();
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineLst = line.split("\t");
            String key = lineLst[0];
            LinkedHashSet<Integer> lhs = new LinkedHashSet<>();
            for(int i = 1; i < lineLst.length; i++){
                int integer = Integer.parseInt(lineLst[i]);
                lhs.add(integer);
            }
            stringIntegerLhs.put(key, lhs);
        }
        scanner.close();
        return stringIntegerLhs;
    }
    //generates a lookup for reviewIds to save on space.
    //HashMap from a String representing the old String id to an Integer representing the new Integer id.
    public static HashMap<String, Integer> generateIntegerReviewIds(String state) throws FileNotFoundException, ParseException {
        LinkedHashSet<String> users = getUsers(state);
        File reviewFile = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(reviewFile);
        HashMap<String, Integer> lookup = new HashMap<>();
        int incrementer = 0;
        while(reviewScanner.hasNext()) {
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String userId = (String) jo.get("user_id");
            if(users.contains(userId)){
                String reviewId = (String) jo.get("review_id");
                lookup.put(reviewId, incrementer++);
            }
        }
        reviewScanner.close();
        return lookup;
    }
    public static void writeIntegerReviewIds(HashMap<String, Integer> frequencies, String state) throws IOException {
        String outputFile = FILEPATH + "dict\\" + state +  "_review_lookup.txt";
        writeStringIntegerHashMap(frequencies, outputFile);
    }
    public static HashMap<String, Integer> readIntegerReviewIds(String state) throws IOException {
        HashMap<String, Integer> frequencies = new HashMap<>();
        String file = (FILEPATH + "dict\\" + state +  "_review_lookup.txt");
        return readStringIntegerHashMap(file);
    }

    public static HashMap<Integer, String> reverseLookup(HashMap<String, Integer> lookup){
        HashMap<Integer, String> reverse = new HashMap<>();
        String[] keys = lookup.keySet().toArray(new String[0]);
        for(String key: keys){
            reverse.put(lookup.get(key), key);
        }
        return reverse;
    }

    public static HashMap<String, Integer> getFrequencies(String state) throws FileNotFoundException, ParseException {
        System.out.println("finding frequencies for " + state + "...");
        LinkedHashSet<String> users = getUsers(state);
        HashMap<String, Integer> frequencies = new HashMap<>();
        File reviewFile = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(reviewFile);
        while(reviewScanner.hasNext()){
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String text = (String) jo.get("text");
            String userId = (String) jo.get("user_id");
            if(users.contains(userId)){
                PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(text),
                        new CoreLabelTokenFactory(), "");
                while (ptbt.hasNext()) {
                    String label = ptbt.next().value();
                    label = label.toLowerCase();
                    if(! frequencies.containsKey(label)){
                        frequencies.put(label, 1);
                    }else{
                        frequencies.put(label, frequencies.get(label) + 1);
                    }
                }
            }

        }
        return frequencies;
    }
    public static void writeFrequencyHashMap(HashMap<String, Integer> frequencies, String state) throws IOException {
        String outputFile = FILEPATH + "dict\\" + state +  "_frequencies.txt";
        writeStringIntegerHashMap(frequencies, outputFile);
    }
    public static HashMap<String, Integer> readFrequencyHashMap(String state) throws IOException {
        HashMap<String, Integer> frequencies = new HashMap<>();
        String filePath = FILEPATH +  "dict\\" + state +  "_frequencies.txt";
        return readStringIntegerHashMap(filePath);
    }

    public static HashMap<String, LinkedHashSet<Integer>> generateBusinessReviewsHashMap(String state) throws ParseException, IOException {
        LinkedHashSet<String> users = getUsers(state);
        HashMap<String, Integer> reviewLookup = readIntegerReviewIds(state);
        HashMap<String, LinkedHashSet<Integer>> businessReviewsHashMap = new HashMap<>();
        File reviewFile = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(reviewFile);
        while(reviewScanner.hasNextLine()) {
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String userId = (String) jo.get("user_id");
            if(users.contains(userId)){
                String businessId = (String) jo.get("business_id");
                String reviewId = (String) jo.get("review_id");
                Integer intReviewId = reviewLookup.get(reviewId);
                if(businessReviewsHashMap.containsKey(businessId)){
                    businessReviewsHashMap.get(businessId).add(intReviewId);
                }else{
                    LinkedHashSet<Integer> reviews = new LinkedHashSet<>();
                    reviews.add(intReviewId);
                    businessReviewsHashMap.put(businessId, reviews);
                }
            }
        }
        return businessReviewsHashMap;
    }
    public static void writeBusinessReviewsHashMap(HashMap<String, LinkedHashSet<Integer>> brs, String state) throws IOException {
        String file = FILEPATH + "dict\\" + state + "_business_reviews.txt";
        writeStringIntegerLhsHashMap(brs, file);
    }
    public static HashMap<String, LinkedHashSet<Integer>> readBusinessReviewsHashMap(String state) throws IOException {
        String filePath = FILEPATH + "dict\\"+ state + "_business_reviews.txt";
        return readStringIntegerLhsHashMap(filePath);
    }


    //Structure of HashMap<String term, HashMap<String review_id, Integer term_count>>
    //this way k(t) (number of documents have term t) can be found by tdf.get(term).size()
    //and c(t, doc) (number of term t in document d) can be found by tdf.get(term).get(doc)
    //particular to a state.
    public static HashMap<String, HashMap<Integer, Integer>> generateTermDocumentFreqHashMap(String state) throws IOException, ParseException {
        System.out.println("Finding c(t, doc) and k(t) for " + state + "...");
        LinkedHashSet<String> users = getUsers(state);
        HashMap<String, Integer> reviewLookup = readIntegerReviewIds("NV");
        HashMap<String, HashMap<Integer, Integer>> terms = new HashMap<>();
        //HashMap<String, Integer> frequencies = readFrequencyHashMap("NV");
        File reviewFile = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(reviewFile);
        while(reviewScanner.hasNext()){
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String text = (String) jo.get("text");
            String userId = (String) jo.get("user_id");
            String reviewId = (String) jo.get("review_id");
            if(users.contains(userId)){
                int intReviewId = reviewLookup.get(reviewId);
                PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(text),
                        new CoreLabelTokenFactory(), "");
                while (ptbt.hasNext()) {
                    String label = ptbt.next().value();
                    label = label.toLowerCase();
                    //if label in the hashmap.
                    if(! terms.containsKey(label)){
                        HashMap<Integer, Integer> reviewFreq = new HashMap<>();
                        reviewFreq.put(intReviewId, 1);
                        terms.put(label, reviewFreq);
                    }else {
                        HashMap<Integer, Integer> reviewFreq = terms.get(label);
                        if (!reviewFreq.containsKey(intReviewId)) {
                            reviewFreq.put(intReviewId, 1);
                            terms.put(label, reviewFreq);
                        } else {
                            reviewFreq.put(intReviewId, reviewFreq.get(intReviewId) + 1);
                            terms.put(label, reviewFreq);
                        }
                    }
                }
            }
        }
        reviewScanner.close();
        System.out.println(terms.size());
        return terms;
    }
    public static void writeTermDocumentFreqHashMap(HashMap<String, HashMap<Integer, Integer>> terms, String state) throws IOException {
        System.out.println("writing TermDocumentFreqHashMap");
        FileWriter writer = new FileWriter(FILEPATH + "dict\\" + state + "_term_doc_freq.txt ");
        String[] termLst = terms.keySet().toArray(new String[0]);
        int s = termLst.length;
        for(int i = 0; i < s; i++){
            String term = termLst[i];
            writer.write(term + "\t");
            HashMap<Integer, Integer> reviewFreq = terms.get(term);
            Integer[] reviews = reviewFreq.keySet().toArray(new Integer[0]);
            for(int review: reviews) {
                writer.write(review + " " + reviewFreq.get(review) + "\t");
            }
            writer.write("\n");
        }
        writer.close();
    }

    public static HashMap<String, HashMap<Integer, Integer>> readTermDocumentFreqHashMap(String state) throws IOException {
        HashMap<String, Integer> frequencies = readFrequencyHashMap("NV");
        int numTerms = frequencies.size();
        frequencies = null;
        File file = new File(FILEPATH + "dict\\" + state + "_term_doc_freq.txt ");
        Scanner scanner = new Scanner(file);
        HashMap<String, HashMap<Integer, Integer>> termDocFreq = new HashMap<>();
        int incrementer = 0;
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] items = line.split("\t");
            String term = items[0];
            HashMap<Integer, Integer> rfs = new HashMap<>();
            for(int i = 1; i < items.length; i++){
                String item = items[i];
                String[] reviewFreq = item.split(" ");
                Integer review = Integer.parseInt(reviewFreq[0]);
                Integer freq = Integer.parseInt(reviewFreq[1]);
                rfs.put(review, freq);
            }
            termDocFreq.put(term, rfs);
            if(incrementer % 10000 == 0){
                System.out.println(incrementer + "/" + numTerms);
            }
            incrementer ++;
        }
        scanner.close();
        return termDocFreq;

    }

    public static HashMap<Integer, Integer> generateReviewLengthHashMap(String state) throws IOException, ParseException {
        HashMap<String, Integer> reviewLookup = readIntegerReviewIds(state);
        File file = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(file);
        HashMap<Integer, Integer> lengths = new HashMap<>();
        while(reviewScanner.hasNext()) {
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String reviewId = (String) jo.get("review_id");
            if(reviewLookup.containsKey(reviewId)){
                String text = (String) jo.get("text");
                PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(text),
                        new CoreLabelTokenFactory(), "");
                int length = 0;
                while(ptbt.hasNext()){
                    ptbt.next();
                    length++;
                }
                lengths.put(reviewLookup.get(reviewId), length);
            }
        }
        reviewScanner.close();
        return lengths;
    }
    public static void writeReviewLengthHashMap(HashMap<Integer, Integer> lengths, String state) throws IOException {
        FileWriter writer = new FileWriter(FILEPATH + "dict\\"+ state + "_review_lengths.txt");
        Integer[] keys = lengths.keySet().toArray(new Integer[0]);
        for(Integer key: keys){
            writer.write(key + "\t" + lengths.get(key) + "\n");
        }
        writer.close();
    }
    public static HashMap<Integer, Integer> readReviewLengthHashMap(String state) throws FileNotFoundException {
        File file = new File(FILEPATH + "dict\\"+ state + "_review_lengths.txt");
        Scanner scanner = new Scanner(file);
        HashMap<Integer, Integer> lengths = new HashMap<>();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineStr = line.split("\t");
            Integer review = Integer.parseInt(lineStr[0]);
            Integer length = Integer.parseInt(lineStr[1]);
            lengths.put(review, length);
        }
        scanner.close();
        return lengths;
    }

    //so most of the value in these tf-idf vectors will be zero.
    //so we need a way to compress vector, because of space constraints.
    //so it's not HashMap<business-id, double[]>
    //instead of double[], HashMap<Integer, double>>
    //where the Integer represents the index of the vector.
    //and the double represents the value.
    public static HashMap<String, HashMap<Integer, Double>> generateBusinessAvgTFIDFVectors(String state) throws IOException {
        System.out.println("reading business-review hashmap");
        HashMap<String, LinkedHashSet<Integer>> brs = readBusinessReviewsHashMap(state);
        System.out.println("reading termDocumentFreqHashMap");
        HashMap<String, HashMap<Integer, Integer>> tDFs  = readTermDocumentFreqHashMap(state); //term -> (document->frequency)
        String[] terms = tDFs.keySet().toArray(new String[0]);
        System.out.println("reading lengths");
        HashMap<Integer, Integer> lengths = readReviewLengthHashMap(state);
        int N = lengths.size();
        String[] businesses = brs.keySet().toArray(new String[0]);
        HashMap<Integer, String> reviewBusinessHashMap = new HashMap<>();
        System.out.println("reversing business hashmap");
        for(String business: businesses){
            LinkedHashSet<Integer> reviews = brs.get(business);
            for(Integer review: reviews){
                reviewBusinessHashMap.put(review, business);
            }
        }
        System.out.println("actually doing the stuff");
        HashMap<String, HashMap<Integer, Double>> businessAvgTFIDFVectors = new HashMap<>();
        for(String business: businesses){
            businessAvgTFIDFVectors.put(business, new HashMap<>());
        }
        for(int i = 0; i < terms.length; i++){
            String term = terms[i];
            HashMap<Integer, Integer> reviewFreq = tDFs.get(term);
            Integer[] reviews = reviewFreq.keySet().toArray(new Integer[0]);
            double k = reviews.length;
            for(Integer review: reviews){
                double length = lengths.get(review);
                double c = reviewFreq.get(review);
                String business = reviewBusinessHashMap.get(review);
                double numReviewsBusiness = brs.get(business).size();
                double tfidf = c/length + Math.log(1 + N/k);
                HashMap<Integer, Double> termAvgTfidf = businessAvgTFIDFVectors.get(business);
                if(termAvgTfidf.containsKey(i)){
                    termAvgTfidf.put(i, termAvgTfidf.get(i) + tfidf/numReviewsBusiness);
                }else{
                    termAvgTfidf.put(i, tfidf/numReviewsBusiness);
                }
                businessAvgTFIDFVectors.put(business, termAvgTfidf);
            }
            //save on memory
            tDFs.remove(term);
        }
        return businessAvgTFIDFVectors;
    }
    public static void writeBusinessAvgTFIDFVectors(HashMap<String, HashMap<Integer, Double>>  batv, String state) throws IOException {
        System.out.println("writing TermDocumentFreqHashMap");
        FileWriter writer = new FileWriter(FILEPATH +  "dict\\" + state + "_business_avg_tfidf_vectors.txt");
        String[] businesses = batv.keySet().toArray(new String[0]);
        int s = businesses.length;
        for(int i = 0; i < s; i++){
            String b = businesses[i];
            writer.write(b + "\t");
            HashMap<Integer, Double> tfidfs = batv.get(b);
            Integer[] terms = tfidfs.keySet().toArray(new Integer[0]);
            for(int term: terms) {
                writer.write(term + " " + tfidfs.get(term) + "\t");
            }
            writer.write("\n");
        }
        writer.close();
    }
    public static HashMap<String, HashMap<Integer, Double>> readBusinessAvgTFIDFVectors(String state) throws IOException{
        File file = new File(FILEPATH +  "dict\\" + state + "_business_avg_tfidf_vectors.txt");
        Scanner scanner = new Scanner(file);
        HashMap<String, HashMap<Integer, Double>> batv = new HashMap<>();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineLst = line.split("\t");
            String business = lineLst[0];
            HashMap<Integer, Double> tfidfs = new HashMap<>();
            for(int i = 1; i < lineLst.length; i++){
                String[] items = lineLst[i].split(" ");
                tfidfs.put(Integer.parseInt(items[0]), Double.parseDouble(items[1]));
            }
            batv.put(business, tfidfs);
        }
        return batv;
    }
    public static HashMap<String, LinkedHashSet<Integer>> generateUserReviewsHashMap(String state) throws ParseException, IOException {
        LinkedHashSet<String> users = getUsers(state);
        HashMap<String, Integer> reviewLookup = readIntegerReviewIds(state);
        HashMap<String, LinkedHashSet<Integer>> userReviewsHashMap = new HashMap<>();
        File reviewFile = new File(FILEPATH + "yelp_academic_dataset_review.json");
        Scanner reviewScanner = new Scanner(reviewFile);
        while(reviewScanner.hasNextLine()) {
            String line = reviewScanner.nextLine();
            Object obj = new JSONParser().parse(line);
            JSONObject jo = (JSONObject) obj;
            String userId = (String) jo.get("user_id");
            if(users.contains(userId)){
                String reviewId = (String) jo.get("review_id");
                Integer intReviewId = reviewLookup.get(reviewId);
                if(userReviewsHashMap.containsKey(userId)){
                    userReviewsHashMap.get(userId).add(intReviewId);
                }else{
                    LinkedHashSet<Integer> reviews = new LinkedHashSet<>();
                    reviews.add(intReviewId);
                    userReviewsHashMap.put(userId, reviews);
                }
            }
        }
        return userReviewsHashMap;
    }

    public static void writeUserReviewsHashMap(HashMap<String, LinkedHashSet<Integer>> lhs, String state) throws IOException {
        String file = FILEPATH + "dict\\" + state + "_user_reviews.txt";
        writeStringIntegerLhsHashMap(lhs, file);
    }
    public static HashMap<String, LinkedHashSet<Integer>> readUserReviewsHashMap(String state) throws IOException {
        String filePath = FILEPATH + "dict\\"+ state + "_user_reviews.txt";
        return readStringIntegerLhsHashMap(filePath);
    }

    public static double cosineSimilarity(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2){
        double v1EuclideanNorm = 0;
        double v2EuclideanNorm = 0;
        double dotProduct = 0;
        LinkedHashSet<Integer> terms = new LinkedHashSet<>();
        Integer[] v1Terms = v1.keySet().toArray(new Integer[0]);
        terms.addAll(Arrays.asList(v1Terms));
        Integer[] v2Terms = v2.keySet().toArray(new Integer[0]);
        terms.addAll(Arrays.asList(v2Terms));
        for(Integer i: terms){
            double x1;
            double x2;
            if(v1.containsKey(i)){ x1 = v1.get(i); }
            else{ x1 = 0; }
            if(v2.containsKey(i)){ x2 = v2.get(i); }
            else{ x2 = 0; }
            v1EuclideanNorm += x1 * x1;
            v2EuclideanNorm += x2 * x2;
            dotProduct += x1 * x2;
        }
        v1EuclideanNorm = Math.sqrt(v1EuclideanNorm);
        v2EuclideanNorm = Math.sqrt(v2EuclideanNorm);
        return dotProduct/(v1EuclideanNorm + v2EuclideanNorm);
    }

    public static void generateUserAvgCosineSimilarityHashMap(String state) throws IOException{
        System.out.println("reading user-reviews hashmap");
        HashMap<String, LinkedHashSet<Integer>> userReviews = readUserReviewsHashMap(state);
        System.out.println("reading lengths hashmap");
        HashMap<Integer, Integer> lengths = readReviewLengthHashMap(state);
        double N = lengths.size();
        String[] users = userReviews.keySet().toArray(new String[0]);
        int userSize = users.length;
        System.out.println("reading business-review hashmap");
        HashMap<String, LinkedHashSet<Integer>> brs = readBusinessReviewsHashMap(state);
        String[] businesses = brs.keySet().toArray(new String[0]);;
        System.out.println("reversing business hashmap");
        HashMap<Integer, String> reviewBusinessHashMap = new HashMap<>();
        for(String business: businesses){
            LinkedHashSet<Integer> reviews = brs.get(business);
            for(Integer review: reviews){
                reviewBusinessHashMap.put(review, business);
            }
        }
        System.out.println("reading term document frequency hashmap");
        HashMap<String, HashMap<Integer, Integer>> tdfs = readTermDocumentFreqHashMap("NV");
        System.out.println("reading business-avg-tfidf-vector HashMap");
        HashMap<String, HashMap<Integer, Double>> batv = readBusinessAvgTFIDFVectors("NV");
        System.out.println("generating user-avgCosineSimilarityHashMap");
        FileWriter writer = new FileWriter(FILEPATH +  "dict\\" + state + "_user_avg_cosine_similarity.txt");
        int userIncrementer = 0;
        for(String user: users){
            LinkedHashSet<Integer> reviews = userReviews.get(user);
            HashMap<String, HashMap<Integer, Double>> batvUser = new HashMap<>();
            for(Integer review: reviews){
                String business = reviewBusinessHashMap.get(review);
                batvUser.put(business, batv.get(business));
            }
            double numReviews = reviews.size();
            //scan through every reiew
            double reviewSum = 0;
            double csSum = 0;
            for(Integer review: reviews){
                //don't count the review if there's only one review in a business (that being the said user's review).
                //make sure there is at least one review for that business.
                if(brs.get(reviewBusinessHashMap.get(review)).size() > 1){
                    HashMap<Integer, Double> tfidfVector = new HashMap<>();
                    double length = lengths.get(review);
                    int incrementer = 0;
                    String[] terms = tdfs.keySet().toArray(new String[0]);
                    for(String term: terms){
                        HashMap<Integer, Integer> reviewFreq = tdfs.get(term);
                        if(reviewFreq.containsKey(review)){
                            double k = reviewFreq.get(review);
                            double c = reviewFreq.size();
                            double tfidf = c/length*(1+Math.log(N/k));
                            tfidfVector.put(incrementer, tfidf);
                        }
                        incrementer ++;
                    }
                    HashMap<Integer, Double> avgBusiness = batvUser.get(reviewBusinessHashMap.get(review));
                    double cs = cosineSimilarity(tfidfVector, avgBusiness);
                    csSum += cs;
                    reviewSum += 1;
                }
            }
            double avgCs = csSum/reviewSum;
            if(reviewSum == 0){ avgCs = 0; }
            writer.write(user + "\t" + avgCs + "\n");
            if(userIncrementer % 1000 == 0){
                System.out.println(userIncrementer + "/" + userSize);
            }
            userIncrementer  += 1;
        }
        writer.close();
    }
    public static void writeUserAvgCosineSimilarityHashMap(HashMap<String, Double> hm, String state) throws IOException {
        FileWriter writer = new FileWriter(FILEPATH +  "dict\\" + state + "_user_avg_cosine_similarity.txt");
        String[] keys = hm.keySet().toArray(new String[0]);
        for(String key: keys){
            writer.write(key + "\t" + hm.get(key) + "\n");
        }
        writer.close();
    }
    public static void main(String[] args) throws IOException, ParseException {
        //System.out.println("generating integer review ids");
        //HashMap<String, Integer> lookup = generateIntegerReviewIds("NV");
        //writeIntegerReviewIds(lookup, "NV");
        //System.out.println("Writing business-Reviews HashMap");
        //writeBusinessReviewsHashMap(generateBusinessReviewsHashMap("NV"), "NV");
        //System.out.println("generating term-frequency hashmap");
        //writeFrequencyHashMap(getFrequencies("NV"), "NV") ;
        //System.out.println("generating term-(document-frequency)) HashMaps");
        //writeTermDocumentFreqHashMap(generateTermDocumentFreqHashMap("NV"), "NV");
        //System.out.println("generating review-length HashMap");
        //writeReviewLengthHashMap(generateReviewLengthHashMap("NV"), "NV");
        //System.out.println("generating Business-AvgTFIDFVectors" );
        //writeBusinessAvgTFIDFVectors(generateBusinessAvgTFIDFVectors("NV"), "NV");
        //System.out.println("generating user-review Vectors");
        //HashMap<String, LinkedHashSet<Integer>> asdf = generateUserReviewsHashMap("NV");
        //writeUserReviewsHashMap(generateUserReviewsHashMap("NV"), "NV");
        System.out.println("writing avg cosineSimilarity hashmap");
        generateUserAvgCosineSimilarityHashMap("NV");
    }




}
