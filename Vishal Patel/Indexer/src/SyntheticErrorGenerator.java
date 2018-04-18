
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SyntheticErrorGenerator {

    public static void main(String[] args) {
        try {
            File ftoread = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query.txt");
            File ftowrite = new File("./../../Pratik Devikar/IR-Project/Task1/Refined_Query_error.txt");
            
            BufferedReader b = new BufferedReader(new FileReader(ftoread));
            BufferedWriter w = new BufferedWriter(new FileWriter(ftowrite));
            String read = b.readLine();
            while(read!=null){
                w.write(Shuffle(read)+"\n");
                read = b.readLine();
            }
            w.close();
            System.out.println();
        } catch (IOException ex) {
            Logger.getLogger(SyntheticErrorGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static String Shuffle(String query){
        Random rand = new Random();
            List<String> split = Arrays.asList( query.split(" "));
            int length = split.size();
            //It is given that error rate is 40%
            int maxnoerror = (int) ((double)length * 0.4);
            System.out.println(maxnoerror +" for query is " +query +" length is "+length );
            //once we get max number of possible error choose between 0 to that number
            if(maxnoerror==0){return query;}
            maxnoerror = rand.nextInt(Math.abs(maxnoerror))+1;
            
            //shuffeling the list
            //Shuffeling avoids repetations
            Collections.shuffle(split);
            for(int j=0;j<maxnoerror;j++){
                //pick a random term from the query
                if(j>=split.size()){break;}
                if(split.get(j).length()<=3){maxnoerror++;continue;}
                //System.out.println("Term is "+split.get(j)+"Shuffle is "+Shuffle(split.get(j)));
                split.set(j, ShuffleTerm(split.get(j)));
            }
            return String.join(" ", split);
    }
    //Tested function works correctly
//    Testing funciton
//        System.out.println("abdsvkajbreg->"+Shuffle("abdsvkajbreg"));
    public static String ShuffleTerm(String toshuffle){
        char[] toshuffletochar = toshuffle.toCharArray();
        String toret = String.valueOf(toshuffletochar[0]);
       ArrayList<Character> listofotherchar = new ArrayList<Character>();
        for(int i=1;i<toshuffletochar.length-1;i++){
            listofotherchar.add(toshuffletochar[i]);
        }
        Collections.shuffle(listofotherchar);
        for(char toapeend:listofotherchar){
        toret = toret.concat(String.valueOf(toapeend));}
        return toret.concat(String.valueOf(toshuffletochar[toshuffle.length()-1]));
    }
}
