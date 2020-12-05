/*
Matt Luettgen
Modified version of IndexFiles.java to read all those in the list.
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class GenerateIndexCore {
    static void indexDoc(IndexWriter writer, HashMap<String, String> document, String[] fields, boolean[] textFields) throws IOException {
        // make a new, empty document
        Document lDoc = new Document();
        for(int i = 0; i < fields.length; i++){
            String field = fields[i];
            boolean isTextField = textFields[i];
            if(isTextField){
                lDoc.add(new TextField(field, document.get(field), Field.Store.YES));
            }else{
                lDoc.add(new StringField(field, document.get(field), Field.Store.YES));
            }
        }
        writer.addDocument(lDoc);
    }

    //file path
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


    public static void createLuceneIndex(String dataset, String[] fields, boolean textFields[]) throws IOException, ParseException {
        String filePath = "D:\\yelp_dataset\\yelp_dataset\\yelp_academic_dataset_" + dataset + ".json";
        File file = new File(filePath);
        Analyzer analyzer  = new StandardAnalyzer();
        String indexPath = "D:\\yelp_dataset\\yelp_dataset\\index\\core";
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);
        if(file.exists()){
            ArrayList<String[]> fileDocs = fileParser(filePath, fields);
            for(String[] fileDoc: fileDocs) {
                HashMap<String, String> document = new HashMap<>();
                for (int i = 0; i < fields.length; i++) {
                    document.put(fields[i], fileDoc[i]);
                }
                indexDoc(writer, document, fields, textFields);
            }
        }else{
            System.out.println("it no exist");
        }

        writer.close();
    }
        //ArrayList<HashMap<String, String>> documents = new ArrayList();
    /** Index all text files under a directory. */
    public static void main(String[] args) throws IOException, ParseException {
        //read through the corpus and add everything to documents.
        String srcPath = "D:\\yelp_dataset\\yelp_dataset\\yelp-academic_dataset_";
        String[] stringFields = new String[] {"review_id", "user_id", "business_id"};
        boolean[] isTextFields = new boolean[] {false, false, false};
        String dataset = "review";
        createLuceneIndex(dataset, stringFields, isTextFields);

        String indexPath = "D:\\yelp_dataset\\yelp_dataset\\index\\core";
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            //Print the toTAl number of documents in the corpus
            System.out.println("Total number of documents in the corpus: " + reader.maxDoc());
            System.out.println(reader.document(0).get("review_id"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}