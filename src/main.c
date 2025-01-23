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

    LexerState lexer = createLexerState(file, argv[1]);

    Token t = lex(&lexer);
    while (t.type != TOK_END) {
        printToken(t);
        t = lex(&lexer);
    }
    printToken(t);
    printf("\n");

    fclose(file);
    return 0;
}


