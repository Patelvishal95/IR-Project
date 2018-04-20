
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrecisionRecall {

    //file f is the file whose evaluation you need to do
    ArrayList[] measures = new ArrayList[65];
    double[] AP,Rank;
    public static void main(String[] args) {
        File f = new File("../../Pratik Devikar/IR-Project/Task1/BM-25_Results");//First run
        
        //add all such file with paths like this
        PrecisionRecall obj = new PrecisionRecall();
        obj.init();//need to be runned once
        //supply folder of evaluation here program will take care for all queries
        //Second argument is the format of the file of run
        obj.startevaluation(f, "BM25_scores_query_","");//reusable part
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task1/SQLM_Results"), "SQLM_scores_query_","");
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task1/TFIDF_Results"), "TFIDF_scores_query_","");

        //query enrichment run
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task2/BM25_Results_Query_Enrichment"), "BM25_scores_query_enriched_query_","");
        
        //lucene run
        obj.startevaluation(new File("../../SaranPrasad/output/lucene_indexer/RankedResults_cacm"), "queryID_","lucene");
        obj.startevaluation(new File("../../SaranPrasad/output/lucene_indexer/RankedResults_cacm_with_stopping"), "queryID_","lucenestopped");
        
        //stopping 2 runs
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task3/BM25_Results_Stopping"), "BM25_scores_stopping_query_","");
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task3/SQLM_Results_Stopping"), "SQLM_scores_stopping_query_","");
        
        
    }
//tempflag is introduced because lucene query has same name format program will just overwrite to the same folder
    private void startevaluation(File toevaluate, String Filenameformat,String tempflag) {
        File[] listoffiles = toevaluate.listFiles();
        AP=new double[65];
        Rank = new double[65];
        for (File queryrun : listoffiles) {
            String name = (queryrun.getName());
            name = name.replace(Filenameformat, "");
            int number = Integer.valueOf(name.replaceAll(".txt", ""));
            Calculation(queryrun, number, Filenameformat,tempflag);
        }
        WriteToFile writer = new WriteToFile(new File("Evaluation/"+tempflag+Filenameformat+"/"+"MAPandMRR.txt"));
        writer.write("MAP is -> "+MAP()+"\n");
        writer.write("MRR is -> "+MAR()+"\n");
        writer.close();
    }

    private void init() {
        //intitialize the relevance measures here
        File measures = new File("Measures/");
        //this has list of all measure files load it to an arrayList
        File[] listmeasures = measures.listFiles();
        //first index is the query number of the relevance measures
        for (File fileinmeasure : listmeasures) {
            ReadFiletoarrayList(fileinmeasure);
        }
        //printmeasures();
    }

    private void ReadFiletoarrayList(File f) {

        ArrayList<String> toplace = new ArrayList<>();
        int pos = Integer.valueOf(f.getName().replaceAll(".txt", ""));

        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(f));
            String read = b.readLine();
            while (read != null) {
                toplace.add(read);
                read = b.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                b.close();
            } catch (IOException ex) {
                Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        measures[pos] = toplace;
    }

    private void Calculation(File queryrun, int querynumber, String Filenameformat,String tempflag) {
            if(measures[querynumber]==null){return;}
        double[] precision = new double[101];
        double[] recall = new double[101];
        int[] match = GetMatch(queryrun,querynumber);
        int numbermatch = 0;
        for(int i=1;i<=100;i++){
            if(match[i]==1){
                numbermatch++;
            }
            precision[i]=(double)numbermatch / (double) i;
            recall[i]= (double)numbermatch/(double)measures[querynumber].size();
        }
        //writing to this file
        File cretdir = new File("Evaluation/"+tempflag+Filenameformat);
        cretdir.mkdirs();
        WriteToFile writer = new WriteToFile(new File("Evaluation/"+tempflag+Filenameformat+"/"+Filenameformat+querynumber+"_"+"evaluation.txt"));
        writer.write("Ranking  DocID      Precision               Recall\n");
        try {
            BufferedReader b = new BufferedReader(new FileReader(queryrun));
            String read = b.readLine();int i=1;
            while (read != null) {
                if(read.equalsIgnoreCase("")){break;}
                writer.write("  "+String.format("%03d", i)+"    "+read.split(" ")[2]+"  "+String.format("%1.20f", precision[i])+"  "+String.format("%1.20f", recall[i])+"\n");
                read=b.readLine();i++;
                if(i==101)break;
            }
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        }
        double map = AP(precision,match);
        double rank = Rank(match);
        AP[querynumber]=map;
        Rank[querynumber]=rank;
        writer.write("P@k = 5 -> "+precision[5]+"\n");
        writer.write("P@k = 20 -> "+precision[20]+"\n");
        writer.write("Average precision is -> "+map+"\n");
        writer.write("Reciprocal Rank is -> "+rank+"\n");
        writer.close();
    }

    private int[] GetMatch(File queryrun,int querypointer) {
        int[] toret = new int[101];
        try {
            BufferedReader b = new BufferedReader(new FileReader(queryrun));
            ArrayList<String> tocheck = measures[querypointer];
            String read = b.readLine();int i=1;
            while (read != null) {
                if(read.equalsIgnoreCase("")){read=b.readLine();toret[i]=0;continue;}
                toret[i] = findinmeasures(tocheck,(read.split(" ")[2]));
                read = b.readLine();i++;
                if(i==101){return toret;}
            }
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        }
        return toret;
    }

    private int findinmeasures(ArrayList<String> tocheck, String string) {
        for(String tomatch:tocheck){
            if(tomatch.equalsIgnoreCase(string)){
                return 1; 
            }
        }return 0;
    }

    private double Rank(int[] match) {
       for(int i=1;i<=100;i++) {
       if(match[i]==1){
           return 1/(double) i;
       }
       }
       return 0;
    }

    private double AP(double[] precision, int[] match) {
    double toret=0.0;int count=0;
        for(int i=1;i<=100;i++) {
        if(match[i]==1){
            count++;
            toret+=precision[i];
        }
    }
        return toret/(double)count;
    }

    private String MAP() {
        double toret=0.0;
        for(double toadd : AP){
            toret+=toadd;
        }
        
        return String.valueOf(toret/52.0);
    }

    private String MAR() {
        double toret=0.0;
        for(double toadd : Rank){
            toret+=toadd;
        }
        
        return String.valueOf(toret/52.0);
    }

}
