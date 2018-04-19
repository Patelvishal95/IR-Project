import os


def create_stopwords_list():
    stop_words = []
    f = open('common_words.txt', 'r')
    for line in f:
        stop_words.append(line[:-1])
    return stop_words


# ========================================================================
def create_indexes_removing_stopwords():
    stop_words = create_stopwords_list()
    dir_tokenized_dictionaries = 'D:/IR-Project/Pratik Devikar/IR-Project/Task1/Tokenized text files/'
    index = 1
    for filename in os.listdir(dir_tokenized_dictionaries):
        print(index)
        index += 1
        f = open(dir_tokenized_dictionaries + filename, 'r')
        f_stop = open('D:\\IR-Project\\Pratik Devikar\\IR-Project\\Task3\\Tokenized text files - Stopping\\' + filename[:-4] + '-Stopping.txt', 'w')

        for line in f:
            # print(line.split(" ")[0])
            if line.split(" ")[0] not in stop_words:
                f_stop.write(line)
        f.close()
        f_stop.close()


# ========================================================================
def main():
    create_indexes_removing_stopwords()


# ========================================================================

if __name__ == "__main__":
    main()
