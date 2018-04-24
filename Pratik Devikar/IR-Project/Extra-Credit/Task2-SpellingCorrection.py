import operator
import random
from collections import OrderedDict

from nltk.corpus import brown


# =====================================================================#
# Dynamic Programming algorithm for calculating the edit distance between two strings
def editDistance_algorithm(str1, str2, m, n):
    # Create a table to store results of subproblems
    edit_distance = [[0 for x in range(n + 1)] for x in range(m + 1)]

    # Fill edit_distance[][] in bottom up manner
    for i in range(m + 1):
        for j in range(n + 1):
            # If first string is empty, only option is to
            # isnert all characters of second string
            if i == 0:
                edit_distance[i][j] = j  # Min. operations = j

            # If second string is empty, only option is to
            # remove all characters of second string
            elif j == 0:
                edit_distance[i][j] = i  # Min. operations = i

            # If last characters are same, ignore last char
            # and recur for remaining string
            elif str1[i - 1] == str2[j - 1]:
                edit_distance[i][j] = edit_distance[i - 1][j - 1]

            # If last character are different, consider all
            # possibilities and find minimum
            else:
                edit_distance[i][j] = 1 + min(edit_distance[i][j - 1],  # Insert
                                              edit_distance[i - 1][j],  # Remove
                                              edit_distance[i - 1][j - 1])  # Replace

    return edit_distance[m][n]


# =====================================================================#
def create_term_dq_master_dict():
    term_fq_dict = {}
    f = open('master_xml.txt', 'r')
    for row in f:
        term_fq = row.split(" ")
        term_fq_dict[term_fq[0]] = term_fq[1][:-1]
    return term_fq_dict


# =====================================================================#
def closest_word(dct, term_fq_dict):
    flag = 0

    if bool(dct):
        lowest_edit_distance = min(list(dct.values()))
        words_with_lowest_ed = []

        for key, value in dct.items():
            if dct[key] == lowest_edit_distance:
                words_with_lowest_ed.append(key)

        scores_dict = {}

        # for key, value in dct.items():
        for key in words_with_lowest_ed:
            if key not in term_fq_dict:
                scores_dict[key] = 0.0
            else:
                scores_dict[key] = int(term_fq_dict[key])
                flag = 1

        if flag == 0:
            return (list(scores_dict.keys())[random.randint(0, len(scores_dict) - 1)])
        else:
            return max(scores_dict.items(), key=operator.itemgetter(1))[0]
    else:
        return None


# =====================================================================#
def main():
    term_fq_dict = create_term_dq_master_dict()
    # Read the error queries from Task 1
    f = open('Refined_Query_error.txt', 'r')
    # f = open('Q.txt', 'r')
    index = 1
    for row in f:
        print(index)
        row = row[:-1]
        terms = row.split(" ")
        corrected_words = []
        error_terms = []
        normal_terms = []

        # Make a list of terms with spelling errors
        for t in terms:
            if t not in brown.words():
                error_terms.append(t)
            else:
                normal_terms.append(t)

        print(error_terms)
        # Correct the spelling errors using the edit distance formula
        for o in error_terms:
            dct = {}
            # CHeck for every word in english dictionary
            for w in brown.words():
                if len(w) == len(o):  # Assuming same length for both the words
                    ed = editDistance_algorithm(o, w, len(o), len(w))
                    if ed >= len(o):
                        pass
                    else:
                        dct[w] = ed

            corrected_word = closest_word(dct, term_fq_dict)

            if corrected_word is None:
                corrected_word = o

            corrected_words.append(corrected_word)
            print(corrected_word)
            # print(dct)

        index += 1
        # REMEMBER TO REMOVE SPACE FROM THE END OF EVERY LINE ?????????????????????????????????????????
        f1 = open('Query_with_spelling_errors_corrected.txt', 'a')
        for n in normal_terms:
            f1.write(n + ' ')
        for c in corrected_words:
            f1.write(c + ' ')
        f1.write('\n')
        f1.close()
    f.close()


# =====================================================================#

if __name__ == "__main__":
    main()
