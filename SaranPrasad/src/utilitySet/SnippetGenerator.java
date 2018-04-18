package utilitySet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SnippetGenerator {

  private static int SLIDING_WINDOW_LENGTH = 30;
  private static String SNIPPET_SUMMARIES_PARENT_FOLDER = "./output/snippet_summaries/";

  private void generateSnippetSummaries(String directoryPath) {
    File directory = new File(directoryPath);
    if (!directory.isDirectory()) {
      System.out.println("Given path does not correspond to a directory");
      return;
    }

    String outputPath = SNIPPET_SUMMARIES_PARENT_FOLDER + directory.getName() + "/";

    for (File file : directory.listFiles()) {
      FileUtility fu = new FileUtility();
      String fileContent = cleanDocContent(fu.textFileToString(file));
      String snippetText = extractMostSignificantSentence(fileContent);
      fu.writeStringToFile(snippetText, outputPath + file.getName() + ".txt");
    }

  }

  /**
   * Assign a significance factor to words in text and extract sentences which have most number of
   * such significant words
   * 
   * @param content
   * @return
   */
  private String extractMostSignificantSentence(String content) {
    String contentCaseFolded = content.toLowerCase();
    String words[] = contentCaseFolded.split(" ");
    // number of sentences in document d
    int sd = words.length / SLIDING_WINDOW_LENGTH;

    // record term frequencies
    HashMap<String, Integer> freq = new HashMap<String, Integer>();
    for (String word : words) {
      if (freq.containsKey(word))
        freq.put(word, freq.get(word) + 1);
      else
        freq.put(word, 1);
    }

    // flags corresponding to significant words in the content
    boolean sigWords[] = new boolean[words.length];
    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      int fdw = freq.get(word); // frequency of the word in the document
      boolean cond = (sd < 25) && (fdw >= 7 - (0.1 * (25 - sd)));
      cond = cond || (sd >= 25 && sd <= 40) && (fdw >= 7);
      cond = cond || (fdw >= (7 + 0.1 * (sd - 40)));
      sigWords[i] = cond ? true : false;
    }

    // finding window with maximum significant words
    int maxSigLen = -1;
    int maxWindowId = -1;
    for (int i = 0; i < sd; i++) {
      int start = i * sd;
      int end = start + SLIDING_WINDOW_LENGTH;

      // find significant words count in current window
      int sigLen = 0;
      for (int j = start; j < end; j++)
        if (sigWords[j])
          sigLen++;

      if (sigLen > maxSigLen) {
        maxSigLen = sigLen;
        maxWindowId = i;
      }
    }

    if (maxWindowId == -1)
      return "";
    else
      return content.substring(maxWindowId * sd,
          maxWindowId * sd + SLIDING_WINDOW_LENGTH);
  }

  private String getQueryHighlightedHTMLSnippet(String docID, List<String> querySet) {

    return null;
  }


  /**
   * parse the document to extract only the required content. All xml tags and trailing numbers in the
   * doc are cleaned out.
   * 
   * @param docContent
   * @return
   */
  private String cleanDocContent(String docContent) {
    Document doc = Jsoup.parse(docContent);
    // extract content from the pre tag
    String mainContent = doc.select("pre").text();

    // clean trailing numbers
    String regex = "([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9] ([AaPp][Mm])";
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(mainContent);

    if (matcher.find())
      mainContent = mainContent.substring(0, matcher.start());

    return mainContent;
  }


  // Test
  public static void main(String args[]) {
    FileUtility fu = new FileUtility();
    String CACM_DOC_LOCATION = "./input/CACM/CACM-1811.html";
    String content = fu.textFileToString(CACM_DOC_LOCATION);

    System.out.println((new SnippetGenerator()).cleanDocContent(content));

  }

}


