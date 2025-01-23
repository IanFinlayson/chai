#ifndef LEXER_H
#define LEXER_H

#include <stdbool.h>
#include <stdio.h>
#include "token.h"

// lexer state, mostly for indentation haha
typedef struct {
    const char* file_name;
    FILE* stream;
    int line_number;

    bool start_of_line;
    int spaces_per_indent;
    int indent_level;
    int dedents_remaining;
} LexerState;

// set up input stream for lexing
LexerState createLexerState(FILE* file, const char* name);

// returns the next token in the input stream
Token lex(LexerState* state);

#endif

