package utilitySet;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ranker.FileUtility;

public class SnippetGenerator {

  private static int SLIDING_WINDOW_LENGTH = 30;

  private void generateSnippetSummaries(String directoryPath) {

  }

  private String extractMostSignificantSentence(String content) {

    return null;
  }

  private String getQueryHighlightedHTMLSnippet(String docID, List<String> querySet) {

    return null;
  }


  /**
   * parse the document to extract only the required content. All xml tags and trailing numbers in the
   * doc is cleaned out.
   * 
   * @param docContent
   * @return
   */
  public static String cleanDocContent(String docContent) {
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

  public static void main(String args[]) {
    FileUtility fu = new FileUtility();
    String CACM_DOC_LOCATION = "./input/CACM/CACM-1811.html";
    String content = fu.textFileToString(CACM_DOC_LOCATION);

    System.out.println(cleanDocContent(content));

  }

}


