import math


def prime_sieve(max: int):

    highest_tester = math.floor(max / 2)
    sieve = [True] * (max + 1)

    for i in range(2, highest_tester):
        n = 2
        while i * n <= len(sieve) - 1:
            x = i * n
            sieve[x] = False
            n += 1

    for m in range(len(sieve)):
        if sieve[m]:
            print(m)


prime_sieve(25)
