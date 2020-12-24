# coding: utf-8
import numpy as np


def block():
    b = []
    for line in open("statics.txt"):
        if line == '\n':
            yield b
            b = []
        else:
            b.append(line)
            
def extract(bl):
    p = int(bl[0].split(":")[1])
    v = int(bl[2].split(":")[1])
    r = int(bl[5].split(":")[1])
    a = []
    for ns in bl[7:]:
        a.append(int(ns.split(":")[3]))
    details = "|".join([str(_) for _ in a])
    return str(p), str(v), str(r),str(np.std(a)),details
    
    
print("Physical Nodes, Virtual Nodes, Records, std, details")
for x in block():
    print(",".join(extract(x)))
    
