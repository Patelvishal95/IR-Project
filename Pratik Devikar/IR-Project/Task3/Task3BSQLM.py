import operator
from Task1 import Task1TFID as tfidf_model
import os
from math import log


# =====================================================================#
# Create a term freq dictionary
def get_tf_dict(filename):
    if filename == 'master_xml.txt':
        dir_tokenized_dictionaries = ''
    else:
        dir_tokenized_dictionaries = 'D:/IR-Project/Pratik Devikar/IR-Project/Task3/Stemmed Tokenized text files/'
    list_of_term_freq = []
    term_freq_dict = {}
    f = open(dir_tokenized_dictionaries + filename, 'r')
    filename = filename[:-4]
    filename = filename + ".txt"
    # Read the rows in a file and split the row into term and frequency
    for term in f:
        term = term[:-1]
        term = term.split(" ")
        term_fq = [t for t in term if t != '']
        # term_fq.insert(2, filename)
        list_of_term_freq.append(term_fq)

        del term
        del term_fq

    # print(counter)
    # counter += 1
    del filename
    f.close()

    for tq in list_of_term_freq:
        term_freq_dict[tq[0]] = int(tq[1])

    return term_freq_dict


# =====================================================================#
def write_scores_into_files(query_index, doc_score):
    f = open('D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\SQLM_Results_Stemmed\\SQLM_scores_stemmed_corpus_query_' + str(query_index) + '.txt', 'w')
    number_of_lines = min(100, len(doc_score))
    for i in range(number_of_lines):
        f.write(str(query_index) + " " + "Q0 " + doc_score[i][0][:-13] + " " + str(i + 1) + " " + str(
            doc_score[i][1]) + " " + "Smoothed_Query_Likelihood_Model_on_stemmed_corpus_" + '\n')
    f.close()


# =====================================================================#
# Algorithm to calculate document scores base on smoothened query likelihood model
def smoothed_query_likelihood_algorithm(all_docs, master_tf_dict, C, alpha):
    query_index = 1
    f = open('cacm_stem.query.txt', 'r')
    for row in f:
        # print(query_index)
        doc_score = {}
        # Separate individual query terms
        query_terms = tfidf_model.create_query_terms_list(row)
        for query_word in query_terms:
            for doc in all_docs:
                # Create a term freq dictionary
                term_freq_dict = get_tf_dict(doc)
                D = float(sum((term_freq_dict.values())))
                try:
                    # Calculate the probabilities
                    prob_qi_given_d = (term_freq_dict[query_word] / D)
                    prob_qi_given_c = (master_tf_dict[query_word] / C)

                    first_term = (1 - alpha) * prob_qi_given_d
                    second_term = alpha * prob_qi_given_c

                    # Calculate document scores and store it in a dictionary
                    if doc in doc_score:
                        doc_score[doc] += log(first_term + second_term, 10)
                    else:
                        doc_score[doc] = log(first_term + second_term, 10)
                except KeyError:
                    pass

        # Sort the dictionary based on scores
        doc_score = (sorted(doc_score.items(), key=operator.itemgetter(1), reverse=True))

        # Write the document into files
        write_scores_into_files(query_index, doc_score)
        print(query_index)
        query_index += 1
    f.close()


# =====================================================================#
# MAIN Function
def main():
    alpha = 0.35
    dir_tokenized_dictionaries = 'D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\Stemmed Tokenized text files\\'

    # Load the master xml file
    master_tf_dict = get_tf_dict('master_xml.txt')
    C = float(sum(master_tf_dict.values()))

    # List of all the tokenized files
    all_docs = []
    for filename in os.listdir(dir_tokenized_dictionaries):
        all_docs.append(filename)

    smoothed_query_likelihood_algorithm(all_docs, master_tf_dict, C, alpha)


# =====================================================================#
if __name__ == "__main__":
    main()
