import operator
from math import log
import os
import glob
from bs4 import BeautifulSoup as bs


# =====================================================================#
# Get unigram inverted index
def get_unigrams_inverted_index(dir_tokenized_dictionaries):
    term_freq_dict = {}
    list_of_term_freq = []
    for filename in os.listdir(dir_tokenized_dictionaries):
        f = open(dir_tokenized_dictionaries + filename, 'r')
        filename = filename[:-9]
        filename = filename + ".txt"
        # Read the rows in a file and split the row into term and frequency
        for term in f:
            term = term[:-1]
            term = term.split(" ")
            term_fq = [t for t in term]
            term_fq.insert(2, filename)
            list_of_term_freq.append(term_fq)

    # Generate a dictionary for the term freq table
    for l in list_of_term_freq:
        if l[0] in term_freq_dict:
            term_freq_dict[l[0]].append([l[2], l[1]])
        else:
            term_freq_dict[l[0]] = [[l[2], l[1]]]

    return term_freq_dict


# =====================================================================#
# Calculate document length
def calculate_dl(dir_tokenized_dictionaries):
    # list_of_term_freq = []
    dl = {}
    counter=0
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
        print(counter)
        counter+=1
    return dl


# =====================================================================#
# Create unigrams
def get_unigrams(dir_tokenized_dictionaries):
    fi = {}
    term_freq_dict = {}
    list_of_term_freq = []
    for filename in os.listdir(dir_tokenized_dictionaries):
        f = open(dir_tokenized_dictionaries + filename, 'r')
        filename = filename[:-9]
        filename = filename + ".txt"
        # Split the row into term and frequency
        for term in f:
            term = term[:-1]
            term = term.split(" ")
            term_fq = [t for t in term]
            term_fq.insert(2, filename)
            list_of_term_freq.append(term_fq)

            del term
            del term_fq
        del filename
        f.close()

    # Create a dictionary containing term and document id
    for l in list_of_term_freq:
        if l[0] in term_freq_dict:
            term_freq_dict[l[0]].extend([l[2]])
        else:
            term_freq_dict[l[0]] = [l[2]]

    for key, value in term_freq_dict.items():
        fi[key] = (len(term_freq_dict[key]))
    return fi


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
    f = open('BM25_scores_query_' + str(index) + '.txt', 'w')
    number_of_lines = min(100, len(doc_score))
    for i in range(number_of_lines):
        f.write(str(index) + " " + "Q0 " + doc_score[i][0][:-9] + " " + str(i + 1) + " " + str(
            doc_score[i][1]) + " " + "BM25_model" + '\n')
    f.close()


# =====================================================================#
def convert_xml_to_txt():
    tf_dict = {}
    all_indexed_xml_files = glob.glob('C:\\Users\\prati\\Documents\\GitHub\\IR-Project\\Vishal '
                                      'Patel\\Indexer\\src\\index\\*.xml')
    # print(len(all_files))
    terms = []
    freq = []
    counter = 1
    for file in all_indexed_xml_files:
        infile = open(file, 'r')
        contents = infile.read()
        soup = bs(contents, 'xml')
        term_result_set = soup.findAll('Term')
        freq_result_set = soup.findAll('Count')

        for t in term_result_set:
            terms.append(t.getText())
        for f in freq_result_set:
            freq.append(f.getText())

        tf_dict = dict(zip(terms, freq))

        f = open('C:\\Users\\prati\\Documents\\GitHub\\IR-Project\\Pratik Devikar\\IR-Project\\Task1\\Tokenized text '
                 'files\\' + file[-13:-4] + '.txt', 'w')
        for d in tf_dict.items():
            f.write(str(d[0]) + ' ' + str(d[1]) + '\n')
        f.close()

        print(counter)
        counter += 1


# =====================================================================#
# MAIN Function
def main():
    N = 3204.0  # Number of documents
    dir_tokenized_dictionaries = 'C:/Users/prati/Documents/GitHub/IR-Project/Pratik Devikar/IR-Project/Task1/Tokenized text files/'

    convert_xml_to_txt()

    # Calculate document length of all files and their average length
    # dl, avgdl = calculate_dl_and_avgdl(dir_tokenized_dictionaries)

    # # Calculate ni
    # ni = get_unigrams(dir_tokenized_dictionaries)

    # index = 1
    # f = open('cacm_stem.query.txt', 'r')
    # for row in f:
    #     queries_dict = create_queries_dict(row)
    #
    #     # Get unigram inverted index
    #     unigram_inverted_dict = get_unigrams_inverted_index(dir_tokenized_dictionaries)
    #     doc_score = {}
    #
    #     # Calculate score for every query with respect to every document in its inverted list
    #     for query in queries_dict.items():
    #         for inv_list in unigram_inverted_dict[query[0]]:
    #             inv_list[0] = inv_list[0].replace('.txt', '_dict.txt')
    #             # Calculate BM25 score
    #             bm25_score = calculate_bm25_score(dl[inv_list[0]], avgdl, int(inv_list[1]), query[1], N, ni[query[0]])
    #             if inv_list[0] in doc_score:
    #                 doc_score[inv_list[0]] += bm25_score
    #             else:
    #                 doc_score[inv_list[0]] = bm25_score
    #
    #     # Sort the dictionary based on scores
    #     doc_score = (sorted(doc_score.items(), key=operator.itemgetter(1), reverse=True))
    #
    #     # Write the document into files
    #     write_scores_into_files(index, doc_score)
    #     index += 1
    # f.close()


# =====================================================================#

if __name__ == "__main__":
    main()
