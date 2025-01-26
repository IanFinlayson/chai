#ifndef HASHTABLE_H
#define HASHTABLE_H

#define KEYSIZE 32

typedef struct {
    char key[KEYSIZE];
    void* value;
} HashEntry;

typedef struct {
    HashEntry* table;
    int size;
} HashTable;

HashTable hashCreate(int size);
void hashDestroy(HashTable table);

void hashInsert(HashTable table, const char* key, void* value);
void* hashLookup(HashTable table, const char* key);

#endif

