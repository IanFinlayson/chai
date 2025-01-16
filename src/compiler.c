#include "token.h"
#include "lexer.h"

int main(int argc, char* argv[]) {
    if (argc != 2) {
        printf("Usage:\nchaic file.chai\n");
        return -1;
    }

    FILE* file = fopen(argv[1], "r");
    if (file == NULL) {
        printf("File '%s' could not be opened for reading\n", argv[1]);
        return -1;
    }

    setStream(file);

    Token t = lex();
    while (t != END) {
        printf("%s\n", tokenString(t));
        t = lex();
    }
    printf("%s\n", tokenString(t));

    fclose(file);
    return 0;
}


