
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexerStemmed {

    public static void main(String[] args) {
        File pathtocorppus = new File("./../../SaranPrasad/input/CACM_STEM/Docs_Separated");
        File[] list = pathtocorppus.listFiles();
        TreeMap<String, Integer> total = new TreeMap<>();
        for (File toopen : list) {
            TreeMap<String, Integer> tm = new TreeMap<>();
            try {
                BufferedReader b = new BufferedReader(new FileReader(toopen));
                String read = b.readLine();
                String textoffile="";
                while(read!=null){
                textoffile = textoffile.concat(read);
                read = b.readLine();
                }
                String[] split = textoffile.split("[ ]");
                for (String yxz : split) {
//                    System.out.println("Before->" + yxz);
                    //starting punctuation handling
                    //remove all numbers from the index
                    if (yxz.matches("^\\d+$")) {
                        continue;
                    }
                    yxz = yxz.toLowerCase();
                    yxz = yxz.replaceAll("[	]{1,}", " ");
                    yxz = yxz.replaceAll("[`~#()\"'/]{0,}", "");
                    if (yxz.matches("^\\d+-\\d+$")) {//2013-2014
                        //contains year sequence then do not remove -
                        continue;
                    } else {

                        yxz = yxz.replaceAll("-", " ");
                    }
                    yxz = yxz.replaceAll("\\.$", "");

                    yxz = yxz.replaceAll("\\[\\d+\\]", "");

                    yxz = yxz.replaceAll("'s$", "s");// replacing moon's to moon
                    if (yxz.matches("^\\w+:\\w+$")) {// for punc word some punctuation
                        yxz = yxz.replaceAll(":", " ");
                    }
                    if (yxz.matches("^\\w+\\[\\w+$")) {// for punc word some punctuation
                        yxz = yxz.replaceAll("\\[", " ");
                    }
                    yxz = yxz.replaceAll("[\\n]{1,}", " ");
                    yxz = yxz.replaceAll("[:]{0,}", "");
                    if (yxz.matches("^\\w+.\\w+$")) {// for punc word some punctuation
                        yxz = yxz.replaceAll("\\.", " ");
                    }
                    if (yxz.matches("^[\\p{P}]{0,}\\w+[\\p{P}]{0,}$")) {// for punc word some punctuation
                        yxz = yxz.replaceAll("[\\p{P}]{0,}", "");
                    }
                    if (yxz.matches("^[\\p{P}]{0,}[\\w+.]{0,}[\\p{P}]{0,}$")) {// short forms like U.S.A
                        yxz = yxz.replaceAll("^[\\p{P}]{0,}", "");
                        yxz = yxz.replaceAll("[\\p{P}]{0,}$", "");
                    }
                    if (!yxz.matches("\\d")) {//no digits remove everything except words
                        //System.out.println(yxz);
                        yxz = yxz.replaceAll("[\\p{P}]{0,}", "");
                    }
                    yxz = yxz.replaceAll("—", " ");
                    //if(yxz.matches("[\\w+]\\)")){yxz=yxz.replaceAll(")", "");}
                    //split based on space
                    String[] splitstr = yxz.split(" ");
//                    System.out.println("After->");
//                    System.out.println(Arrays.toString(splitstr));
                    if (splitstr.length == 0) {
                        continue;
                    }
                    if (splitstr.length == 1) {
                        if (yxz.matches("^[ ]{0,}$")) {
                            continue;
                        }
                        if (yxz.matches("^\\d+$")) {
                        continue;
                    }
                        if (tm.containsKey(yxz)) {
                            tm.put(yxz, tm.get(yxz) + 1);
                        } else {
                            tm.put(yxz, 1);
                        }
                        if (total.containsKey(yxz)) {
                            total.put(yxz, total.get(yxz) + 1);
                        } else {
                            total.put(yxz, 1);
                        }
                        //System.out.println(yxz +" ");
                    } else if (splitstr.length > 1) {
                        for (String xyz : splitstr) {
                            if (xyz.matches("^[ ]{0,}$")) {
                                continue;
                            }
                            if (xyz.matches("[ ]{0,}")) {
                                continue;
                            }
                            if (xyz.matches("^\\d+$")) {
                        continue;
                    }
                            if (tm.containsKey(xyz)) {
                                tm.put(xyz, tm.get(xyz) + 1);
                            } else {
                                tm.put(xyz, 1);
                            }
                            if (total.containsKey(xyz)) {
                                total.put(xyz, total.get(xyz) + 1);
                            } else {
                                total.put(xyz, 1);
                            }
                            //System.out.println(xyz +" ");
                        }
                    }
                }
                //System.out.println(Arrays.toString(split));
                //write tm to a xml file
                //System.out.println(tm.toSring());
                String num = toopen.getName().replaceAll(".txt", "");
                WriteToXMLStemmed.Write(tm, "CACM-"+String.format("%04d", Integer.valueOf(num)));

            } catch (IOException ex) {
                System.out.println("ERROR:Unable to parse the file");
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        WriteToXMLStemmed.Write(total, "/Master/master");

    }

}
