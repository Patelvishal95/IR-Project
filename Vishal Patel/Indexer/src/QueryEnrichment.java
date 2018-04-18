
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryEnrichment {
    String query;
    public static void main(String[] args) throws FileNotFoundException, IOException {
   new QueryEnrichment("Queries is this a about above");
   File ftoread = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query.txt");
   File ftowrite = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query_Enriched.txt");
   BufferedReader b = new BufferedReader(new FileReader(ftoread));
            BufferedWriter w = new BufferedWriter(new FileWriter(ftowrite));
            String read = b.readLine();
            while(read!=null){
                w.write(new QueryEnrichment(read).getQuery()+"\n");
                read = b.readLine();
            }
            w.close();
}
    public QueryEnrichment(String query){
        this.query=query;
        this.run();
    }

    public String getQuery() {
        return query;
    }

    private void run()  {
        System.out.println("Query before->"+query);
        Stopping();
        Stemming();
        System.out.println("Query after->"+query); 
    }
    //Stopping from the list of stop words provided
    private void Stopping()  {
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(new File("./src/common_words")));
            String read=b.readLine();
            ArrayList<String> querysplit = new ArrayList<String>(Arrays.asList(query.split(" ")));
            ArrayList<String> toremove = new ArrayList<String>();
            while(read!=null){
                for(String tocmp:querysplit){
                    if (tocmp.equalsIgnoreCase(read)){
                        toremove.add(read);
                    }
                }
                read=b.readLine();
            }   querysplit.removeAll(toremove);
            query = String.join(" ", querysplit);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                b.close();
            } catch (IOException ex) {
                Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void Stemming() {
        Stemmer s = new Stemmer();
        String[] split = query.split(" ");
        
        for(int i=0;i<split.length;i++){
            String append =s.StemWordWithWordNet(split[i]);
            if(append==null){continue;}
            if(append.equalsIgnoreCase(split[i])){continue;}
            query = query.concat(" "+append);
        }
    s.Unload();
    }
}
