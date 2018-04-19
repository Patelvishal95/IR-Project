rel = open("Rel.txt",'r')
read = rel.readlines()
for line in read:
    write = open("Measures/"+line.split(" ")[0]+".txt",'a')
    write.write(line.split(" ")[2])
    write.write("\n")