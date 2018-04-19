
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QueryEnrichment {

    String query;
    int number;
    int k = 10;//number of queries to be selected for pseudo relevance feedback 
    int notoken = 10; //number of tokens to expand the query with in relevance feedback

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //new QueryEnrichment("Queries is this a about above");
        File ftoread = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query.txt");
        File ftowrite = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query_Enriched.txt");
        BufferedReader b = new BufferedReader(new FileReader(ftoread));
        BufferedWriter w = new BufferedWriter(new FileWriter(ftowrite));
        String read = b.readLine();
        int i = 1;
        while (read != null) {
            w.write(new QueryEnrichment(read, i).getQuery() + "\n");
            read = b.readLine();
            i++;
        }
        w.close();
    }

    public QueryEnrichment(String query, int number) {
        this.query = query;
        this.number = number;
        this.run();
    }

    public String getQuery() {
        return query;
    }

    private void run() {
        System.out.println("Query before->" + query);
        Stopping();
        Stemming();
        String[] tokens = PseudoRelevanceFeedback();
        System.out.println(Arrays.toString(tokens));
        query = query.concat(" ");
        query = query.concat(String.join(" ", tokens));
        System.out.println("Query after->" + query);
    }

    //Stopping from the list of stop words provided
    private void Stopping() {
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(new File("./src/common_words")));
            String read = b.readLine();
            ArrayList<String> querysplit = new ArrayList<String>(Arrays.asList(query.split(" ")));
            ArrayList<String> toremove = new ArrayList<String>();
            while (read != null) {
                for (String tocmp : querysplit) {
                    if (tocmp.equalsIgnoreCase(read)) {
                        toremove.add(read);
                    }
                }
                read = b.readLine();
            }
            querysplit.removeAll(toremove);
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

        for (int i = 0; i < split.length; i++) {
            String append = s.StemWordWithWordNet(split[i]);
            if (append == null) {
                continue;
            }
            if (append.equalsIgnoreCase(split[i])) {
                continue;
            }
            query = query.concat(" " + append);
        }
        s.Unload();
    }

    private String[] PseudoRelevanceFeedback() {
        String files[] = GetFileNames();
        ArrayList<IndexEntry> total = new ArrayList<>();
        for (String file : files) {
            ArrayList<IndexEntry> toadd = GetTokens(file);
            total = AddArrayList(total, toadd);
        }
        //now total has all the tags we need sort it to get top "notoken" 
        Collections.sort(total, new Comparator<IndexEntry>() {
            @Override
            public int compare(IndexEntry o1, IndexEntry o2) {
            if(o1.getCount()>o2.getCount()){return -1;}
            else if(o1.getCount()==o2.getCount()){return 0;}
            else{return 1;}
            }
        });
        String[] tokens = new String[10];
        for(int i=0;i<notoken;i++){
           tokens[i] = total.get(i).getTerm();
        }
        return tokens;
    }
    private String[] GetFileNames() {
        String[] filelist = new String[10];
        try {

            File f = new File("./../../Pratik Devikar/IR-Project/Task1/BM-25_Results/BM25_scores_query_" + number + ".txt");
            BufferedReader b = new BufferedReader(new FileReader(f));
            String read = null;//b.readLine();
            int i = 0;
            while (i != k) {
                read = b.readLine();
                filelist[i] = read.split(" ")[2];
                read = b.readLine();
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filelist;
    }

    private ArrayList<IndexEntry> GetTokens(String file) {
        ArrayList<IndexEntry> tokensofthisfile = new ArrayList<>();
        try {
            File f = new File("./../../Index/" + file + ".xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);

            NodeList nList = doc.getElementsByTagName("Entry");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    tokensofthisfile.add(new IndexEntry(eElement.getElementsByTagName("Term").item(0).getTextContent(), Integer.valueOf(eElement.getElementsByTagName("Count").item(0).getTextContent())));
                }
            }
            tokensofthisfile = StoppingforPseudoRelevance(tokensofthisfile);
            
        } catch (SAXException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(QueryEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tokensofthisfile;
    }

    ArrayList<IndexEntry> StoppingforPseudoRelevance(ArrayList<IndexEntry> listoftokens) {
        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(new File("./src/common_words")));
            String read = b.readLine();
            ArrayList<IndexEntry> toremove = new ArrayList<>();
            while (read != null) {
                for (IndexEntry tocmp : listoftokens) {
                    if (tocmp.getTerm().equalsIgnoreCase(read)) {
                        toremove.add(tocmp);
                    }
                }
                read = b.readLine();
            }
            listoftokens.removeAll(toremove);
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
        return listoftokens;
    }

    private ArrayList<IndexEntry> AddArrayList(ArrayList<IndexEntry> total, ArrayList<IndexEntry> toadd) {
        if (total.size() == 0) {
            return (ArrayList<IndexEntry>) toadd.clone();
        }
        for (IndexEntry tocmp : toadd) {
            int addflag = 0;//0mean  add 1 meand dont add
            for (int i = 0; i < total.size(); i++) {//IndexEntry intotal:total){
                IndexEntry intotal = total.get(i);
                if (intotal.getTerm().equalsIgnoreCase(tocmp.getTerm())) {
                    total.set(i, intotal.updateCount(tocmp.getCount()));
                    addflag = 1;
                    break;
                }
            }
            if (addflag == 0) {
                total.add(tocmp);
            }
        }
        return (ArrayList<IndexEntry>) total.clone();
    }

}
