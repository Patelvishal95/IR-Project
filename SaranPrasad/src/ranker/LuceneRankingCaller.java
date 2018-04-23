package ranker;

public class LuceneRankingCaller {
  /**
   * Indexes the given cacm and cacm_stem corpus by calling utility methods from Lucene
   * 
   * @param args
   */
  public static void main(String[] args) {

    // =========================================================
    // Perform Indexing and Retrieval - Lucene
    // =========================================================
    LuceneIndexerAndSearch lucene = new LuceneIndexerAndSearch();

    // Task 1
    lucene.indexRankAndQuery(LuceneIndexerAndSearch.RUN_TYPE.CACM);

    // Task 3 A
    lucene.indexRankAndQuery(LuceneIndexerAndSearch.RUN_TYPE.CACM_WITH_STOPPING);

    // Task 3 B
    lucene.insdexRankAndQuery(LuceneIndexerAndSearch.RUN_TYPE.CACM_STEMMED_CORPUS);

    // Phase 2
    // Snippet generation and Query term highlighting for Lucene Indexed Cacm corpus
    lucene.rankWithSnippetsAndQueryHighlighting(LuceneIndexerAndSearch.RUN_TYPE.CACM);

  }
}
