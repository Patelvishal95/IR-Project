import operator
import string
from math import log
import os
import glob
from bs4 import BeautifulSoup as bs


# =====================================================================#
# Get unigram inverted index list
def get_unigrams_tf_list(dir_tokenized_dictionaries):
    list_of_term_freq = []
    term_freq_dict = {}
    # counter = 1
    for filename in os.listdir(dir_tokenized_dictionaries):
        f = open(dir_tokenized_dictionaries + filename, 'r')
        filename = filename[:-4]
        filename = filename + ".txt"
        # Read the rows in a file and split the row into term and frequency
        for term in f:
            term = term[:-1]
            term = term.split(" ")
            term_fq = [t for t in term]
            term_fq.insert(2, filename)
            list_of_term_freq.append(term_fq)

            del term
            del term_fq

        # print(counter)
        # counter += 1
        del filename
        f.close()

    return list_of_term_freq


# =====================================================================#
# Calculate document length
def calculate_dl(dir_tokenized_dictionaries):
    # list_of_term_freq = []
    dl = {}
    # counter = 0
    for filename in os.listdir(dir_tokenized_dictionaries):
        f = open(dir_tokenized_dictionaries + filename, 'r')
        # filename = filename[:-4]
        # Read the rows in a file and split the row into term and frequency
        for term in f:
            term = term[:-1]
            term = term.split(" ")
            term_fq = [t for t in term if t != '']
            # list_of_term_freq.append(term_fq)
            if filename in dl:
                dl[filename] += int(term_fq[1])
            else:
                dl[filename] = int(term_fq[1])
            del term
            del term_fq
            # del list_of_term_freq
        # print(counter)
        # counter += 1
    return dl


# =====================================================================#
# Create unigrams
def get_unigrams(dir_tokenized_dictionaries):
    ni = {}
    term_freq_dict = {}
    list_of_term_freq = []
    for filename in os.listdir(dir_tokenized_dictionaries):
        f = open(dir_tokenized_dictionaries + filename, 'r')
        filename = filename[:-4]
        filename = filename + ".txt"
        # Split the row into term and frequency
        for term in f:
            term = term[:-1]
            term = term.split(" ")
            term = term[0]
            # term_fq = [t for t in term]
            # term_fq.insert(2, filename)
            # list_of_term_freq.append(term_fq)

            if term in ni:
                ni[term] += 1.0
            else:
                ni[term] = 1.0

            del term
            # del term_fq
        f.close()

    return ni


# =====================================================================#
# Calculate the BM25 score for a single query term
def calculate_bm25_score(dl, avdl, fi, qfi, N, ni):
    k1 = 1.2
    b = 0.75
    k2 = 100.0
    ri = 0.0
    R = 0.0
    K = k1 * ((1 - b) + b * (dl / avdl))
    score = (((k1 + 1) * fi) / (K + fi)) * (((k2 + 1) * qfi) / (k2 + qfi))
    score *= log(((ri + 0.5) / (R - ri + 0.5)) / ((ni - ri + 0.5) / (N - ni - R + ri + 0.5)))
    return score


# =====================================================================#
# Calculate document length of all files and their average length
def calculate_dl_and_avgdl(dir_tokenized_dictionaries):
    avgdl = 0
    dl = calculate_dl(dir_tokenized_dictionaries)
    for d in dl.items():
        avgdl += d[1]
    avgdl = avgdl / 3024.0
    return dl, avgdl


# =====================================================================#
# Create query dictionary
def create_queries_dict(row):
    row = row[:-1]
    query_terms = row.split(" ")
    queries_dict = {}
    for q in query_terms:
        if q in queries_dict:
            queries_dict[q] += 1
        else:
            queries_dict[q] = 1
    return queries_dict


# =====================================================================#
# Write the document into files
def write_scores_into_files(index, doc_score):
    f = open('D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\BM25_Results_Stopping\\BM25_scores_stemmed_corpus_query_' + str(index) + '.txt', 'w')
    number_of_lines = min(100, len(doc_score))
    for i in range(number_of_lines):
        f.write(str(index) + " " + "Q0 " + doc_score[i][0][:-13] + " " + str(i + 1) + " " + str(
            doc_score[i][1]) + " " + "BM25_model_on_stemmed_corpus" + '\n')
    f.close()


# =====================================================================#
# Convert xml files to text files
def convert_xml_to_txt():
    all_indexed_xml_files = glob.glob('D:\\IR-Project\\Stemmed_Index\\*.xml')
    counter = 1
    for file in all_indexed_xml_files:
        terms = []
        freq = []
        infile = open(file, 'r')
        contents = infile.read()
        soup = bs(contents, 'xml')
        term_result_set = soup.findAll('Term')
        freq_result_set = soup.findAll('Count')

        for t in term_result_set:
            if ' ' in t.getText():
                t = t.getText().replace(' ', '')
                terms.append(t)
            else:
                terms.append(t.getText())
        for f in freq_result_set:
            freq.append(f.getText())

        # Create a dictionary of term and freq of a token
        tf_dict = dict(zip(terms, freq))

        f = open('D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\Stemmed Tokenized text '
                 'files\\' + file[-13:-4] + '.txt', 'w')
        for d in tf_dict.items():
            f.write(str(d[0]) + ' ' + str(d[1]) + '\n')
        f.close()

        print(counter)
        counter += 1


# =====================================================================#
# Retrieve query from cacm query file and refine it
def retrieve_queries_from_cacm_query():
    file = 'D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task1\\Query.txt'
    f = open(file, 'r')
    queries = []
    for query in f:
        query = str(query)
        exclude = set(string.punctuation)
        # Lower case and remove punctuations
        query = ''.join(ch.lower() for ch in query if ch not in exclude)
        queries.append(query)
    f.close()
    f = open('Refined_Query.txt', 'w')
    for q in queries:
        f.write(q)
    f.close()


# =====================================================================#
# Create a dictionary of unigram
def get_unigram_inverted_dict(list_of_term_freq):
    unigram_inverted_dict = {}
    for l in list_of_term_freq:
        if l[0] in unigram_inverted_dict:
            unigram_inverted_dict[l[0]].append([l[2], l[1]])
        else:
            unigram_inverted_dict[l[0]] = [[l[2], l[1]]]
    return unigram_inverted_dict


# =====================================================================#
# Algorithm to calculate document scores by BM25 algorithm
def bm25_algorithm(unigram_inverted_dict, dl, avgdl, N, ni):
    query_index = 1
    f = open('Refined_Query.txt', 'r')
    counter = 1
    for row in f:
        queries_dict = create_queries_dict(row)
        doc_score = {}

        # Calculate score for every query with respect to every document in its inverted list
        for query_word in queries_dict.items():
            try:  # For keyerror when a particular query word doesnt belong to any of the tokens
                for inv_list in unigram_inverted_dict[query_word[0]]:
                    # Calculate BM25 score
                    bm25_score = calculate_bm25_score(dl[inv_list[0]], avgdl, int(inv_list[1]), query_word[1], N, ni[query_word[0]])
                    if inv_list[0] in doc_score:
                        doc_score[inv_list[0]] += bm25_score
                    else:
                        doc_score[inv_list[0]] = bm25_score

                del bm25_score

            except KeyError:
                pass

        # Sort the dictionary based on scores
        doc_score = (sorted(doc_score.items(), key=operator.itemgetter(1), reverse=True))

        # Write the document into files
        write_scores_into_files(query_index, doc_score)
        query_index += 1

        # print(counter)
        # counter += 1
    del queries_dict
    del doc_score
    f.close()


# =====================================================================#
# MAIN Function
def main():
    # Initialize the parameters
    N = 3204.0  # Number of documents
    dir_tokenized_dictionaries = 'D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\Tokenized text files - Stopping\\'

    # convert_xml_to_txt()

    # Calculate document length of all files and their average length
    print("Calculating dl and avdl")
    dl, avgdl = calculate_dl_and_avgdl(dir_tokenized_dictionaries)

    # # Calculate ni
    print("Calculating ni")
    ni = get_unigrams(dir_tokenized_dictionaries)

    # retrieve_queries_from_cacm_query()

    # Generate tf list
    print("Calculating list of tf")
    list_of_term_freq = get_unigrams_tf_list(dir_tokenized_dictionaries)

    # Generate a dictionary for the term freq table
    print("Calculating unigram inverted list")
    unigram_inverted_dict = get_unigram_inverted_dict(list_of_term_freq)

    bm25_algorithm(unigram_inverted_dict, dl, avgdl, N, ni)

# =====================================================================#

if __name__ == "__main__":
    main()
