package ranker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

/**
 * To create Apache Lucene index in a folder and add files into this index based on the input of the
 * user.
 */
public class LuceneIndexerAndSearch {
  private static Analyzer sAnalyzer;
  private static int NUM_OF_RESULTS_TO_RETURN = 100;

  private static String CACM_INDEX_LOCATION = "./output/lucene_indexer/Indexer_cacm/";
  private static String CACM_DOCS_LOCATION = "./input/CACM";
  private static String CACM_QUERY_FILE_PATH = "./input/cacm.query.txt";
  private static String CACM_RANKED_RESULTS_PATH =
      "./output/lucene_indexer/RankedResults_cacm/";

  private static String CACM_WITH_STOPPING_INDEX_LOCATION =
      "./output/lucene_indexer/Indexer_cacm_with_stopping/";
  private static String STOP_LIST_PATH = "./input/common_words.txt";
  private static String CACM_WITH_STOPPING_RANKED_RESULTS_PATH =
      "./output/lucene_indexer/RankedResults_cacm_with_stopping/";

  private static String CACM_STEMMED_INDEX_LOCATION =
      "./output/lucene_indexer/Indexer_cacm_stem/";
  private static String CACM_STEMMED_DOCS_LOCATION = "./input/CACM_STEM/cacm_stem.txt";
  private static String CACM_STEMMED_QUERY_FILE_PATH = "./input/cacm_stem.query.txt";
  private static String CACM_STEMMED_RANKED_RESULTS_PATH =
      "./output/lucene_indexer/RankedResults_cacm_stem/";

  public static enum RUN_TYPE {
    CACM, CACM_WITH_STOPPING, CACM_STEMMED_CORPUS
  }

  private IndexWriter writer;
  private ArrayList<File> queue;

  /** default constructor */
  LuceneIndexerAndSearch() {
    sAnalyzer = new SimpleAnalyzer();
    queue = new ArrayList<File>();
  }

  /**
   * Perform index, rank and query tasks based on the run type
   * 
   * @param runType
   */
  public void indexRankAndQuery(RUN_TYPE runType) {
    String indexLocation = CACM_INDEX_LOCATION;
    String docsLocation = CACM_DOCS_LOCATION;
    String queryFilePath = CACM_QUERY_FILE_PATH;
    String rankedResultsPath = CACM_RANKED_RESULTS_PATH;
    String systemRunName = "Lucene_Query_Parser_CACM";
    switch (runType) {
      case CACM_STEMMED_CORPUS:
        indexLocation = CACM_STEMMED_INDEX_LOCATION;
        docsLocation = CACM_STEMMED_DOCS_LOCATION;
        queryFilePath = CACM_STEMMED_QUERY_FILE_PATH;
        rankedResultsPath = CACM_STEMMED_RANKED_RESULTS_PATH;
        systemRunName = "Lucene_Query_Parser_CACM_stemmed_corpus";
        break;
      case CACM_WITH_STOPPING:
        indexLocation = CACM_WITH_STOPPING_INDEX_LOCATION;
        rankedResultsPath = CACM_WITH_STOPPING_RANKED_RESULTS_PATH;
        systemRunName = "Lucene_Query_Parser_CACM_with_stopwords";
        // adding stop words from file
        Path stopwordsFile = Paths.get(STOP_LIST_PATH);
        try {
          sAnalyzer = new StopAnalyzer(stopwordsFile);
        } catch (IOException e2) {
          e2.printStackTrace();
        }
        break;
      default:
        break;
    }

    // ===================================================
    // clean up old index files and prepare for new ones
    // ===================================================
    try {
      File dir = new File(indexLocation);
      for (File file : dir.listFiles())
        file.delete();
      createIndex(indexLocation);
    } catch (Exception ex) {
      System.out.println("Cannot create index..." + ex.getMessage());
      System.exit(-1);
    }

    // ===================================================
    // read and parse raw documents from raw docs location
    // ===================================================
    try {
      if (runType.equals(RUN_TYPE.CACM_STEMMED_CORPUS))
        indexStemmedCorpus(docsLocation);
      else
        indexFileOrDirectory(docsLocation);
    } catch (Exception e) {
      System.out.println("Error indexing " + docsLocation + " : " + e.getMessage());
      System.exit(-1);
    }

    // ===================================================
    // after adding, we always have to call the
    // closeIndex, otherwise the index is not created
    // ===================================================
    try {
      closeIndex();
    } catch (Exception e) {
      System.out.println("Error closing index");
      System.exit(-1);
    }

    // =========================================================
    // Now search
    // =========================================================
    IndexReader reader = null;
    try {
      reader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(
          indexLocation)));
    } catch (IOException e1) {
      System.out.println("Error reading index");
      System.exit(-1);
    }

    // =========================================================
    // Write query results to output
    // =========================================================
    FileUtility fu = new FileUtility();
    List<String> queryList = new ArrayList<String>();

    // Load queries
    if (runType.equals(RUN_TYPE.CACM_STEMMED_CORPUS))
      queryList = fu.textFileToList(queryFilePath);
    else
      queryList = fu.parseQueryDocToList(queryFilePath);

    for (int queryID = 0; queryID < queryList.size(); queryID++) {
      String query = queryList.get(queryID);
      try {
        Query q = new QueryParser("contents", sAnalyzer).parse(QueryParser.escape(query));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector =
            TopScoreDocCollector.create(NUM_OF_RESULTS_TO_RETURN);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        String outputFilePath =
            rankedResultsPath + "queryID_" + (queryID + 1) + ".txt";
        StringBuilder toOutput = new StringBuilder();
        // display results in console
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
          String resultLine = (queryID + 1) + " Q0 " + getDocIDFromDocName(d.get("path"))
              + " " + (i + 1) + " " + hits[i].score + " " + systemRunName;
          toOutput.append(resultLine);
          toOutput.append(System.getProperty("line.separator"));
          System.out.println(resultLine);
        }
        fu.writeStringToFile(toOutput.toString(), outputFilePath);

      } catch (Exception e) {
        System.out.println("Error searching " + query + " : " + e.getMessage());
        System.exit(-1);
      }
    }
  }

  private void createIndex(String indexDir) throws IOException {
    FSDirectory dir = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
    IndexWriterConfig config = new IndexWriterConfig(sAnalyzer);
    writer = new IndexWriter(dir, config);
  }

  /**
   * Returns docID sans extension
   */
  private String getDocIDFromDocName(String name) {
    if (name.indexOf('\\') > -1)
      name = name.substring(name.lastIndexOf('\\') + 1);
    if (name.indexOf('.') > -1)
      return name.substring(0, name.lastIndexOf('.'));
    else
      return name;
  }


  /**
   * Parses the given stemmed corpus into multiple files and then indexes them
   * 
   * @param the path of a text file representing the stemmed corpus
   * @throws java.io.IOException when exception
   */
  public void indexStemmedCorpus(String fileName) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      // ===================================================
      // parse contents of the file and add them to
      // individual doc file
      // ===================================================
      File f = new File(fileName);
      fr = new FileReader(f);
      br = new BufferedReader(fr);
      String readLine = null;
      StringBuilder sb = new StringBuilder();
      String docName = null;
      while ((readLine = br.readLine()) != null) {
        // new file line
        if (readLine.startsWith("#")) {
          if (docName != null)
            addDocToIndexWriter(writer, sb, f.getPath(), docName);
          docName = readLine.replace("# ", "");
          sb = new StringBuilder();
        } else {
          sb.append(readLine);
          sb.append(System.getProperty("line.separator"));
        }
      }
      // if there is content left in the string builder, write it as the last doc
      if (sb.length() > 0)
        addDocToIndexWriter(writer, sb, f.getPath(), docName);
    } catch (Exception e) {
      System.out.println("Could not add: " + fileName);
    } finally {
      br.close();
      fr.close();
    }
    queue.clear();
  }

  /**
   * write contents to doc
   * 
   * @param writer
   * @param sb
   * @param filePath
   * @param docName
   * @throws IOException
   */
  private void addDocToIndexWriter(IndexWriter writer, StringBuilder sb, String filePath,
      String docName) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("contents", sb.toString(), Field.Store.NO));
    doc.add(new StringField("path", getDocIDFromDocName(filePath) + "_" + docName,
        Field.Store.YES));
    doc.add(new StringField("filename", docName,
        Field.Store.YES));
    writer.addDocument(doc);
  }

  /**
   * Indexes a file or directory
   * 
   * @param fileName the name of a text file or a folder we wish to add to the index
   * @throws java.io.IOException when exception
   */
  public void indexFileOrDirectory(String fileName) throws IOException {
    // ===================================================
    // gets the list of files in a folder (if user has submitted
    // the name of a folder) or gets a single file name (is user
    // has submitted only the file name)
    // ===================================================
    addFiles(new File(fileName));

    int originalNumDocs = writer.numDocs();
    for (File f : queue) {
      FileReader fr = null;
      try {
        Document doc = new Document();
        System.out.println("Adding file: " + f.getName());
        // ===================================================
        // add contents of file
        // ===================================================
        fr = new FileReader(f);
        doc.add(new TextField("contents", fr));
        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
        doc.add(new StringField("filename", f.getName(),
            Field.Store.YES));

        writer.addDocument(doc);
      } catch (Exception e) {
        System.out.println("Could not add: " + f);
      } finally {
        fr.close();
      }
    }

    int newNumDocs = writer.numDocs();
    System.out.println("");
    System.out.println("************************");
    System.out
        .println((newNumDocs - originalNumDocs) + " documents added.");
    System.out.println("************************");

    queue.clear();
  }

  private void addFiles(File file) {

    if (!file.exists()) {
      System.out.println(file + " does not exist.");
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        addFiles(f);
      }
    } else {
      String filename = file.getName().toLowerCase();
      // ===================================================
      // Only index text files
      // ===================================================
      if (filename.endsWith(".htm") || filename.endsWith(".html")
          || filename.endsWith(".xml") || filename.endsWith(".txt")) {
        queue.add(file);
      } else {
        System.out.println("Skipped " + filename);
      }
    }
  }

  /**
   * Close the index.
   * 
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
}
