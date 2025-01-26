#include <assert.h>
#include <string.h>
#include <stdlib.h>

#include "hashtable.h"

HashTable hashCreate(int size) {
    HashTable ht;
    ht.table = malloc(size * sizeof(HashEntry));
    assert(size > 0);
    ht.size = size;
    for (int i = 0; i < size; i++) {
        ht.table[i].value = NULL;
    }

    return ht;
}

void hashDestroy(HashTable ht) {
    free(ht.table);
}

int hash(const char* key) {
    int length = strlen(key);
    assert(length < KEYSIZE);
    int code = 0;
    for (int i = 0; i < length; i++) {
        code += key[i] * 31;
    }
    return code;
}

void hashInsert(HashTable ht, const char* key, void* value) {
    int index = hash(key) % ht.size;
    while (ht.table[index].value != NULL) {
        index = (index + 1) % ht.size;
    }

    strcpy(ht.table[index].key, key);
    ht.table[index].value = value;
}

void* hashLookup(HashTable ht, const char* key) {
    int index = hash(key) % ht.size;
    while (ht.table[index].value != NULL && strcmp(ht.table[index].key, key)) {
        index = (index + 1) % ht.size;
    }

    return ht.table[index].value;
}

