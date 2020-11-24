import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GenerateIndexReview {
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
                document[i] = (String) jo.get(fields[i]);
            }
            documents.add(document);
        }
        return documents;
    }


    public static void createLuceneIndex(String dataset, String[] fields, boolean textFields[]) throws IOException, ParseException {
        String filePath = "D:\\yelp_dataset\\yelp_dataset\\yelp_academic_dataset_" + dataset + ".json";
        int n = fields.length;
        ArrayList<HashMap<String, String>> documents = new ArrayList();
        File file = new File(filePath);
        if(file.exists()){
            ArrayList<String[]> fileDocs = fileParser(filePath, fields);
            for(String[] fileDoc: fileDocs) {
                HashMap<String, String> document = new HashMap<>();
                for (int i = 0; i < fields.length; i++) {
                    document.put(fields[i], fileDoc[i]);
                }
                documents.add(document);
            }
        }else{
            System.out.println("it no exist");
        }
        Analyzer analyzer  = new StandardAnalyzer();
        String indexPath = "./index/" + dataset;
        try{
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);
            for (HashMap<String, String> document : documents) {
                indexDoc(writer, document, fields, textFields);
            }
            writer.close();
            System.out.println("Done ...");
        }catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }

    }

}
