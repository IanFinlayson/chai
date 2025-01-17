#ifndef LEXER_H
#define LEXER_H

#include <stdio.h>
#include "token.h"

// set up input stream for lexing
void setStream(FILE* stream, const char* name);

// returns the next token in the input stream
Token lex();

#endif

