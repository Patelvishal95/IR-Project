# IR-Project

#### Using Jsoup - for cleaning the document for snippet generation
#### Query independent snippet generation. using a sliding window technique
#### formula from page 216
#### Apache commons lang - commons-lang-2.6.jar
#### screenshot results to documentation. And add links to path.


. Created three different run-types for Lucene indexing and retrieval
	1. CACM
	2. CACM_WITH_STOPPING
	3. CACM_STEMMED_CORPUS


Snippet generation - Literature
https://nlp.stanford.edu/IR-book/html/htmledition/results-snippets-1.html
https://nlp.stanford.edu/IR-book/pdf/irbookprint.pdf
Basic explanation

http://www.cs.pomona.edu/~dkauchak/ir_project/whitepapers/Snippet-IL.pdf
A real time Dynamic Programing approach to Snippet Generation for HTML Search Engines


https://people.eng.unimelb.edu.au/jzobel/fulltext/ecir09.pdf
However this quality comes at the cost of increased processing load. Because the search engine does not know a priori the set of queries for which a document may be fetched, it must retain each document in some form, to be searched and processed with respect to the query each time the document is highly ranked.


https://web.njit.edu/~ychen/sigmod08_snippet.pdf
Query Biased snipped generation in XML search

Definition 2.4: We define dominance score of a feature
f = (e, a, v) as follows:
DS(f, r) = N(e, a, v)
N(e,a)
D(e,a)
(1)
where R is a query result, N(x) denotes the number of
occurrences of x in R, D(e, a) denotes the domain size of
(e, a) in R.
A feature is dominant if its dominance score is larger than
1, in other words, its number of occurrences is more than
the average number of occurrences of the feature values of
the same type: N(e, a)/D(e, a). There is one exception: if
the domain size is 1, D(e, a) = 1, then there is only one
feature value of this type, which is trivially considered to be
dominant even though its dominance score is 1.
Definition 2.5: A feature f is dominant in a query result R
if one of the following holds: (a) DS(f, R) > 1 if D(e, a) > 1;
or (b) DS(f, R) = 1 if D(e, a) = 1.
We include dominant features into the snippet information
list in the decreasing order of their dominance scores.
Example 2.3: Continuing our example, we compute the
dominance scores for features in the query results. In the
following the corresponding feature types are omitted for
conciseness.
DS(Houston) = 6 / (10 / 5) = 3.0
DS(men) = 600 / (1000 / 3) = 1.8
DS(women) = 360 /(1000 / 3) = 1.08
Similarly, we get DS(casual) = 1.4, DS(outwear) = 2.2,
and DS(suit) = 1.2.



