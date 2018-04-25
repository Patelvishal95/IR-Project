import operator
from Task1 import Task1BM25 as bm25_model
from math import log
import os


# =====================================================================#
# Separate individual query terms
def create_query_terms_list(row):
    row = row[:-1]
    query = row.split(" ")
    query_terms = []
    for q in query:
        query_terms.append(q)
    return query_terms


# =====================================================================#
# Create a term freq dictionary
def get_tff_list(filename):
    dir_tokenized_dictionaries = 'D:/IR-Project/Pratik Devikar/IR-Project/Task3/Tokenized text files - Stopping/'
    list_of_term_freq = []
    term_freq_dict = {}
    f = open(dir_tokenized_dictionaries + filename, 'r')
    filename = filename[:-4]
    filename = filename + ".txt"
    # Read the rows in a file and split the row into term and frequency
    for term in f:
        term = term[:-1]
        term = term.split(" ")
        term_fq = [t for t in term]
        # term_fq.insert(2, filename)
        list_of_term_freq.append(term_fq)

        del term
        del term_fq

    del filename
    f.close()

    for tq in list_of_term_freq:
        term_freq_dict[tq[0]] = tq[1]

    return term_freq_dict


# =====================================================================#
def write_scores_into_files(query_index, doc_score):
    f = open('D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\TFIDF_Results_Stopping\\TFIDF_scores_stopping_query_' + str(query_index) + '.txt', 'w')
    number_of_lines = min(100, len(doc_score))
    for i in range(number_of_lines):
        f.write(str(query_index) + " " + "Q0 " + doc_score[i][0][:-13] + " " + str(i + 1) + " " + str(
            doc_score[i][1]) + " " + "TFIDF_model_stopping" + '\n')
    f.close()


# =====================================================================#
# Algorithm to calculate the document scores by tfidf model
def tf_idf_algorithm(N, ni, idf, all_docs):
    query_index = 1
    f = open('Refined_Query.txt', 'r')
    for row in f:
        print(query_index)
        # Separate individual query terms
        query_terms = create_query_terms_list(row)
        doc_score = {}

        for query_word in query_terms:
            try:  # For keyerror when a particular query word doesnt belong to any of the tokens
                # calculate idf
                idf[query_word] = 1 + log((N / ni[query_word] + 1), 10)
                tf = {}
                for doc in all_docs:
                    try:
                        # Create a term freq dictionary of that document's terms
                        term_freq_dict = get_tff_list(doc)
                        tf[query_word] = log(1 + int(term_freq_dict[query_word]), 10)

                        if doc in doc_score:
                            doc_score[doc] += idf[query_word] * tf[query_word]
                        else:
                            doc_score[doc] = idf[query_word] * tf[query_word]
                    except:
                        pass
            except KeyError:
                pass

        # Sort the dictionary based on scores
        doc_score = (sorted(doc_score.items(), key=operator.itemgetter(1), reverse=True))

        # Write the document into files
        write_scores_into_files(query_index, doc_score)
        query_index += 1
    f.close()


# =====================================================================#
# MAIN Function
def main():
    dir_tokenized_dictionaries = 'D:/IR-Project/Pratik Devikar/IR-Project/Task3/Tokenized text files - Stopping/'
    N = 3204.0
    ni = bm25_model.get_unigrams(dir_tokenized_dictionaries)

    idf = {}

    # List of all the tokenized files
    all_docs = []
    for filename in os.listdir(dir_tokenized_dictionaries):
        all_docs.append(filename)

    tf_idf_algorithm(N, ni, idf, all_docs)


# =====================================================================#
if __name__ == "__main__":
    main()
