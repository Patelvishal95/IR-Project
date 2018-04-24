package utilitySet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtility {
  /**
   * Returns the file contents as line delimeted String list
   */
  public List<String> textFileToList(String filePath) {
    List<String> lines = new ArrayList<String>();
    try {
      Scanner sc = new Scanner(new File(filePath));
      while (sc.hasNextLine())
        lines.add(sc.nextLine());
      sc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines;
  }


  /**
   * Returns contents of the text file
   * 
   * @param filePath - Path to file
   * @return - String - file content
   */
  public String textFileToString(String filePath) {
    return textFileToString(new File(filePath));
  }


  /**
   * Returns contents of the text file
   * 
   * @param file - File handle
   * @return - String - file content
   */
  public String textFileToString(File file) {
    StringBuilder lines = new StringBuilder();
    try {
      Scanner sc = new Scanner(file);
      while (sc.hasNextLine()) {
        lines.append(sc.nextLine());
        if (sc.hasNextLine())
          lines.append(System.getProperty("line.separator"));
      }
      sc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines.toString();
  }

  /**
   * Parse and return next document from the cached document store
   * 
   * @param sc - io handle
   * @return - String pair - 1. URL of the document 2. Document content
   */
  public String[] getNextDocText(Scanner sc) {
    String[] titleDocStringPair = new String[2];
    try {
      String url = new String();
      StringBuilder lines = new StringBuilder();

      String nextLine = sc.nextLine();
      // get article id from url
      while (sc.hasNextLine() && !nextLine.equals("<DOCHDR>"))
        nextLine = sc.nextLine();
      // url-decoding using UTF-8
      url = java.net.URLDecoder.decode(sc.nextLine(), "UTF-8");

      // skip the Trec Headers
      while (sc.hasNextLine() && !nextLine.startsWith("<html"))
        nextLine = sc.nextLine();
      // read doc contents
      while (sc.hasNextLine() && !nextLine.equals("</DOC>")) {
        lines.append(nextLine);
        nextLine = sc.nextLine();
      }

      titleDocStringPair[0] = url;
      titleDocStringPair[1] = lines.toString();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return titleDocStringPair;
  }

  /**
   * Prints string to the given handle one line at a time
   * 
   * @param output - output handle
   * @param string - output string
   */
  public void println(PrintWriter output, String string) {
    output.println(string);
    output.flush();
  }


  /**
   * Prints string to the given handle one string at a time
   * 
   * @param output - output handle
   * @param string - output string
   */
  public void print(PrintWriter output, String string) {
    output.print(string);
    output.flush();
  }

  /**
   * Writes string into the file path specified
   * 
   * @param outputString
   * @param outputPath
   */
  public void writeStringToFile(String outputString, String outputPath) {
    try {
      PrintWriter outputHandle = new PrintWriter(outputPath);
      outputHandle.println(outputString);
      outputHandle.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends string into the existing file path specified
   * 
   * @param outputString
   * @param outputPath
   */
  public void appendStringToFile(String outputString, String outputPath) {
    try {
      PrintWriter outputHandle =
          new PrintWriter(new FileOutputStream(new File(outputPath), true));
      outputHandle.println(outputString);
      outputHandle.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  public List<String> parseQueryDocToList(String queryFilePath) {
    List<String> lines = new ArrayList<String>();
    try {
      Scanner sc = new Scanner(new File(queryFilePath));
      StringBuilder sb = new StringBuilder();
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        if (line.startsWith("<DOC>"))
          sb = new StringBuilder();
        else if (line.startsWith("<DOCNO")) {
          // do nothing
        } else if (line.startsWith("</DOC>")) {
          lines.add(sb.toString().trim().replaceAll(" +", " "));
        } else {
          sb.append(line + " ");
        }
      }
      sc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines;
  }


  public String setupHTMLResultsDoc(String query, int resultCount, long timeDiff) {
    String template_header_path =
        "./output/snippet_results/htmlRef/custom_search_header.html";
    String source = textFileToString(template_header_path);
    source = source.replace("~query~", query);
    source = source.replace("~timeDiff~", String.valueOf(timeDiff));
    source = source.replace("~resultCount~", String.valueOf(resultCount));
    return source;
  }


  public String insertResult(String articleTitle, String url, String snippet) {
    String template_path =
        "./output/snippet_results/htmlRef/custom_search_result.html";
    String source = textFileToString(template_path);
    source = source.replace("~article_title~", articleTitle);
    source = source.replace("~url~", url);
    url = "..\\..\\" + url;
    source = source.replace("~href~", url);
    source = source.replace("~snippet~", snippet);
    return source;
  }


  public String finishHTMLResultsDoc() {
    String template_path =
        "./output/snippet_results/htmlRef/custom_search_footer.html";
    String source = textFileToString(template_path);
    return source;
  }

}
