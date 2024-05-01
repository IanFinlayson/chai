x = 0
while True:
    x = x + 1
    if x > 25:
        break
    if x % 2 == 0:
        continue
    print(x)
print("---")

i = 0
j = 0
while i < 10:
    j = 0
    while j < 10:
        if j == 5:
            break
        print(i, j)
        j = j + 1
    i = i + 1
print("---")
        
i = 0
j = 0
while i < 10:
    j = 0
    while j < 10:
        j = j + 1
        if j == 5:
            continue
        print(i, j)
    i = i + 1
