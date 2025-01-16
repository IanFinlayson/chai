#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>

#include "lexer.h"
#include "token.h"

// global lexer state, mostly for indentation haha
FILE* stream;
int line_number = 1;

bool start_of_line = true;
int spaces_per_indent = 0;
int indent_level = 0;
int dedents_remaining = 0;


void setStream(FILE* file) {
    stream = file;
}

// checks if next character of input is something, if not put it back
bool match(char expected) {
    char next = fgetc(stream);
    if (next == expected) {
        return true;
    } else {
        ungetc(next, stream);
        return false;
    }
}

void lexerror(const char* mesg) {
    // TODO
    printf("error occured in lexer: %s\n", mesg);
    exit(-1);

}

// TODO put an error if the 1024 is not big enough

// we saw the start of a number, lex it!
// TODO allow for nested \" characters
Token lexString() {
    // grab the whole thang
    char buffer[1024];

    int i = 0;
    bool done = false;
    while (!done) {
        char next = fgetc(stream);
        if (next == '"') {
            done = true;
        } else {
            buffer[i] = next;
            i++;
        }
    }
    buffer[i] = '\0';

    return STRINGVAL;
}


// we saw the start of a number, lex it!
// TODO add in more for these (hex numbers, scientific notation, etc.
Token lexNumber(char start) {
    // grab the whole thang
    char buffer[1024];
    buffer[0] = start;
    bool seendot = start == '.';

    int i = 1;
    bool done = false;
    while (!done) {
        char next = fgetc(stream);
        if (next == '.' && !seendot) {
            buffer[i] = next;
            i++;
            seendot = true;
        }
        else if (next == '.') {
            ungetc(next, stream);
            done = true;
        }
        else if (!isdigit(next)) {
            ungetc(next, stream);
            done = true;
        } else {
            buffer[i] = next;
            i++;
        }
    }
    buffer[i] = '\0';

    if (seendot) return FLOATVAL;
    else return INTVAL;
}

// we saw the start of a id or keyword, lex it!
Token lexWord(char start) {
    // grab the whole thang
    char buffer[1024];
    buffer[0] = start;

    int i = 1;
    bool done = false;
    while (!done) {
        char next = fgetc(stream);
        if (!isalnum(next) && next != '_') {
            ungetc(next, stream);
            done = true;
        } else {
            buffer[i] = next;
            i++;
        }
    }
    buffer[i] = '\0';

    // TODO use a hash table instead to make this faster!
    if (!strcmp(buffer, "and")) return AND;
    if (!strcmp(buffer, "assert")) return ASSERT;
    if (!strcmp(buffer, "break")) return BREAK;
    if (!strcmp(buffer, "case")) return CASE;
    if (!strcmp(buffer, "continue")) return CONTINUE;
    if (!strcmp(buffer, "def")) return DEF;
    if (!strcmp(buffer, "elif")) return ELIF;
    if (!strcmp(buffer, "else")) return ELSE;
    if (!strcmp(buffer, "for")) return FOR;
    if (!strcmp(buffer, "if")) return IF;
    if (!strcmp(buffer, "in")) return IN;
    if (!strcmp(buffer, "lambda")) return LAMBDA;
    if (!strcmp(buffer, "let")) return LET;
    if (!strcmp(buffer, "match")) return MATCH;
    if (!strcmp(buffer, "not")) return NOT;
    if (!strcmp(buffer, "of")) return OF;
    if (!strcmp(buffer, "or")) return OR;
    if (!strcmp(buffer, "pass")) return PASS;
    if (!strcmp(buffer, "return")) return RETURN;
    if (!strcmp(buffer, "type")) return TYPE;
    if (!strcmp(buffer, "var")) return VAR;
    if (!strcmp(buffer, "while")) return WHILE;
    if (!strcmp(buffer, "Int")) return INT;
    if (!strcmp(buffer, "Float")) return FLOAT;
    if (!strcmp(buffer, "String")) return STRING;
    if (!strcmp(buffer, "Bool")) return BOOL;
    if (!strcmp(buffer, "Void")) return VOID;
    if (!strcmp(buffer, "True")) return TRUE;
    if (!strcmp(buffer, "False")) return FALSE;

    if (isupper(buffer[0])) return TYPENAME;
    else return IDNAME;
}

// returns the next token in the input stream
Token lex() {
    do {
        // if we need dedents, do that first
        if (dedents_remaining > 0) {
            dedents_remaining--;
            indent_level--;
            return DEDENT;
        }

        int current = fgetc(stream);

        // if this is not a space character, this is not the start of the line
        if (current != ' ') {
            start_of_line = false;
        }

        switch (current) {
            // we handle operators first, in order of complexity
            case '~': return COMPLEMENT;
            case ',': return COMMA;
            case '(': return LPAREN;
            case ')': return RPAREN;
            case '[': return LBRACK;
            case ']': return RBRACK;
            case '{': return LBRACE;
            case '}': return RBRACE;
            case '_': return USCORE;

            case '+': return match('=') ? PLUSASSIGN : PLUS;
            case '%': return match('=') ? MODASSIGN : MODULUS;
            case '&': return match('=') ? BITANDASSIGN : BITAND;
            case '^': return match('=') ? BITXORASSIGN : BITXOR;
            case '=': return match('=') ? EQUALS : ASSIGN;
            case '|': return match('=') ? BITORASSIGN : BAR;
            case ':': return match(':') ? CONS : COLON;

            case '!':
                if (match('=')) return NOTEQUALS;
                else lexerror("! given as an operator without =");

            case '.':
                if (match('.')) return ELIPSIS;
                else {
                    // it might be the start of a number
                    char next = fgetc(stream);
                    ungetc(next, stream);
                    if (isdigit(next)) return lexNumber(current);
                    else lexerror(". alone is not an operator");
                }

            case '-':
                if (match('=')) return MINUSASSIGN;
                else if (match('>')) return ARROW;
                else return MINUS;

            case '*':
                if (match('=')) return TIMESASSIGN;
                else if (match('*')) {
                if (match('=')) return POWERASSIGN;
                    else return POWER;
                } else return TIMES;

            case '/':
                if (match('='))
                    return DIVASSIGN;
                else if (match('/')) {
                    if (match('=')) return INTDIVASSIGN;
                    else return INTDIV;

                } else return DIVIDE;

            case '<':
               if (match('=')) return LESSEQ;
               else if (match('<')) {
                   if (match('=')) return LSHIFTASSIGN;
                   else return LSHIFT;
               } else return LESS;

            case '>':
               if (match('=')) return GREATEREQ;
               else if (match('>')) {
                   if (match('=')) return RSHIFTASSIGN;
                   else return RSHIFT;
               } else return GREATER;


            // comments
            case '#':
               while (current != '\n') {
                   current = fgetc(stream);
               }
               continue;

            // string literals
            case '"':
               return lexString();

            // we do not allow tabs or carriage returns, for now anyway
            case '\t':
            case '\r':
               lexerror("tabs or carriage returns not allowed in chai source");

            // spaces are tough...
            case ' ':
               if (start_of_line) {
                   // no longer start of line
                   start_of_line = false;

                   // count the number of spaces here
                   int spaces = 0;
                   while (current == ' ') {
                       spaces++;
                       current = fgetc(stream);
                   }

                   // if this was ALL spaces, or a comment is hit, skip
                   if (current == '\n' || current == '#') {
                       ungetc(current, stream);
                       continue;
                   }

                   // otherwise put the thing after the spaces back in
                   ungetc(current, stream);

                   // if this is the FIRST indent, this is the level of one
                   if (spaces_per_indent == 0) {
                       spaces_per_indent = spaces;
                   }

                   // compute indentation level
                   int level = spaces / spaces_per_indent;
                   if ((spaces % spaces_per_indent) != 0) {
                       lexerror("Indentation level inconsistent");
                   }

                   // if we indented, do that
                   if (level == (indent_level + 1)) {
                       indent_level++;
                       return INDENT;
                   }

                   // if we indented too much, that's an error
                   if (level > indent_level) {
                       lexerror("Too much indentation");
                   }

                   // if we are de-denting, trigger that
                   if (level < indent_level) {
                       dedents_remaining = indent_level - level;
                       continue;
                   }

                   // otherwise, the level is equal, so just carry on
                   continue;

               } else {
                   // whitespace not at start of line is skipped
                   continue;
               }

            // ding!
            case '\n':
                start_of_line = true;
                line_number++;
                continue;

            case EOF:
                // if we were indented, we need to trigger dedents on subsequent lex calls
                if (indent_level != 0) {
                    dedents_remaining = indent_level;
                    continue;
                } else return END;
        }

        // now we need to handle things that don't start with a specifc character
        if (isalpha(current)) {
            return lexWord(current);
        }

        // start of a number
        if (isdigit(current)) {
            return lexNumber(current);
        }

        printf("Invalid token: '%c' (%d)\n", current, current);
        lexerror("invalid token given");

    } while (true); // keep looping over skippables
}

