#include "token.h"
#include "lexer.h"

int main(int argc, char* argv[]) {
    FILE* f = fopen("test.chai", "r");
    setStream(f);

    Token t = lex();
    while (t != END) {
        printf("%s\n", tokenString(t));
        t = lex();
    }
    printf("%s\n", tokenString(t));

    return 0;
}


