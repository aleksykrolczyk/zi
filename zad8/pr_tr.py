import numpy as np
from nptyping import Array

# L1  = [0, 1, 1, 0, 0, 0, 0, 0, 0, 0]
L1 = [0, 1, 1, 0, 1, 0, 0, 0, 0, 0]
L2 = [1, 0, 0, 1, 0, 0, 0, 0, 0, 0]
# L3  = [0, 1, 0, 0, 0, 0, 0, 0, 0, 0]
L3 = [0, 1, 0, 0, 0, 0, 1, 0, 0, 0]
L4 = [0, 1, 1, 0, 0, 0, 0, 0, 0, 0]
L5 = [0, 0, 0, 0, 0, 1, 1, 0, 0, 0]
L6 = [0, 0, 0, 0, 0, 0, 1, 1, 0, 0]
L7 = [0, 0, 0, 0, 1, 1, 1, 1, 1, 1]
L8 = [0, 0, 0, 0, 0, 0, 1, 0, 1, 0]
L9 = [0, 0, 0, 0, 0, 0, 1, 0, 0, 1]
L10 = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

L = np.array([L1, L2, L3, L4, L5, L6, L7, L8, L9, L10])

ITERATIONS = 100
kurak = 10


def getM(L):
    # M = np.zeros([10, 10], dtype=float)
    # number of outgoing links
    # c = np.zeros([10], dtype=int)

    ## TODO 1 compute the stochastic matrix M
    M = np.array(L[0]/L[0].sum())
    for x in L[1:]:
        o = x/x.sum() if x.sum() != 0 else x
        M = np.vstack([M, o])
    M = M.transpose()

    # for i in range(0, 10):
    #     c[i] = sum(L[i])
    #
    # for i in range(0, 10):
    #     for j in range(0, 10):
    #         if L[j][i] == 0:
    #             M[i][j] = 0
    #         else:
    #             M[i][j] = 1.0 / c[j]
    return M


def pagerank(M, q):
    v = [1 / kurak] * kurak
    v2 = v[:]
    for _ in range(100):
        for i in range(kurak):
            v2[i] = q + (1 - q) * (v * M[i]).sum()
        v = v2[:]
    return v


def trustrank(M: Array, q, v: Array) -> Array:
    v2: Array = v.copy()
    for _ in range(100):
        for i in range(kurak):
            v2[i] = q * v[i] + (1 - q) * (v * M[i]).sum()
        v = v2.copy()
    return v


print("Matrix L (indices)")
print(L)

M = getM(L)

print("Matrix M (stochastic matrix)")
print(M)

### TODO 2: compute pagerank with damping factor q = 0.15
### Then, sort and print: (page index (first index = 1 add +1) : pagerank)
### (use regular array + sort method + lambda function)
print("PAGERANK")

q = 0.15
pr = list(zip(range(1,kurak + 1), pagerank(M, q)))
pr.sort(key=lambda x: x[1], reverse=True)
# Nie bardzo rozumiem zapis w jakim formacie ma być dokładnie printowane, więc jest tak jak jest (może chodziło o to) ¯\_(ツ)_/¯
print(pr)

# pr = np.zeros([10], dtype=float)

### TODO 3: compute trustrank with damping factor q = 0.15
### Documents that are good = 1, 2 (indexes = 0, 1)
### Then, sort and print: (page index (first index = 1, add +1) : trustrank)
### (use regular array + sort method + lambda function)
print("TRUSTRANK (DOCUMENTS 1 AND 2 ARE GOOD)")

q = 0.15
d = np.zeros([kurak], dtype=float)
d[0] = 1; d[1] = 1
aaa = list(zip(range(1,kurak + 1), trustrank(M, q, d)))
aaa.sort(key=lambda x: x[1], reverse=True)
print(aaa)


# tr = [v for v in d]

### TODO 4: Repeat TODO 3 but remove the connections 3->7 and 1->5 (indexes: 2->6, 0->4)
### before computing trustrank
L1  = [0, 1, 1, 0, 0, 0, 0, 0, 0, 0]
L3  = [0, 1, 0, 0, 0, 0, 0, 0, 0, 0]

L = np.array([L1, L2, L3, L4, L5, L6, L7, L8, L9, L10])
M = getM(L)
q = 0.15
d = np.zeros([kurak], dtype=float)
d[0] = 1; d[1] = 1
aaa = list(zip(range(1,kurak + 1), trustrank(M, q, d)))
aaa.sort(key=lambda x: x[1], reverse=True)
print('TRUSTRANK WITH REMOVED CONNECTIONS')
print(aaa)
