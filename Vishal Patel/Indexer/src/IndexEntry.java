
/**
 *
 * Custom Index Entry Object Used in Pseudo relevance feedback
 */
public class IndexEntry {
    private String Term;
    private int count;

    public IndexEntry(String Term, int count) {
        this.Term = Term;
        this.count = count;
    }

    public String getTerm() {
        return Term;
    }

    public void setTerm(String Term) {
        this.Term = Term;
    }

    public int getCount() {
        return count;
    }
    public IndexEntry updateCount(int addcount){
        this.count+=addcount;
        return this;
    }
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "IndexEntry{" + "Term=" + Term + ", count=" + count + '}';
    }
    
}
