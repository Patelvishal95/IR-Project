import os
import array


score = open("../../Pratik Devikar/IR-Project/Task1/BM-25_Results/BM25_scores_query_1.txt", 'r')
rel_measure = open("Measures/1.txt",'r').readlines()
newlist =[]
for relmeas in rel_measure:
    relmeas=relmeas.strip("\n")
    newlist.append(relmeas)

totalrelevant = len(newlist)

linesfromfiles = score.readlines()
a = []
for line in linesfromfiles:
    a.append(0)
    for relmeas in newlist:

        if relmeas == line.split(" ")[2]:
            a.pop()
            a.append(1)
# a is an array that has information about relevance of the mb25 output
print(a)

precision = []
recall=[]
numberrelresult = 0

i = 1
for num in a:
    if num == 1:
        numberrelresult = numberrelresult + 1
    recall.append(numberrelresult/totalrelevant)
    precision.append(numberrelresult/i)
    i = i + 1
print(precision)
print(recall)

