#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>

#include "lexer.h"
#include "errors.h"
#include "token.h"

// global lexer state, mostly for indentation haha
const char* file_name;
FILE* stream;
int line_number = 1;

bool start_of_line = true;
int spaces_per_indent = 0;
int indent_level = 0;
int dedents_remaining = 0;

// used to make tokens without typing line number all the time
#define TOK(type) makeToken(type, NULL, line_number)

// used for reading in strings, ids, numbers...
#define BUFFER_SIZE 1024
char buffer[BUFFER_SIZE];

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

// we create a hash table of reserved words so when we see an id we can look it up
#define KEYWORD_SIZE 16
#define HASHTABLE_SIZE 127

typedef struct {
    char keyword[KEYWORD_SIZE];
    TokenType token;
} KeywordTableEntry;

KeywordTableEntry keywords[HASHTABLE_SIZE];

// we tweaked this until there was only 1 collision
int hashKeyword(const char* keyword) {
    int length = strlen(keyword);
    int code = 0;
    for (int i = 0; i < length; i++) {
        code += keyword[i] * 37;
    }
    return code % HASHTABLE_SIZE;
}

void insertKeyword(const char* keyword, TokenType token) {
    int index = hashKeyword(keyword);
    while (keywords[index].token != TOK_END) {
        index = (index + 1) % HASHTABLE_SIZE;
    }

    strcpy(keywords[index].keyword, keyword);
    keywords[index].token = token;
}

TokenType lookupKeyword(const char* keyword) {
    int index = hashKeyword(keyword);

    while (keywords[index].token != TOK_END && strcmp(keywords[index].keyword, keyword)) {
        index = (index + 1) % HASHTABLE_SIZE;
    }

    return keywords[index].token;
}

void setupKeywords() {
    for (int i = 0; i < HASHTABLE_SIZE; i++) {
        strcpy(keywords[i].keyword, "");
        keywords[i].token = TOK_END;
    }

    insertKeyword("and", TOK_AND);
    insertKeyword("assert", TOK_ASSERT);
    insertKeyword("break", TOK_BREAK);
    insertKeyword("case", TOK_CASE);
    insertKeyword("continue", TOK_CONTINUE);
    insertKeyword("def", TOK_DEF);
    insertKeyword("elif", TOK_ELIF);
    insertKeyword("else", TOK_ELSE);
    insertKeyword("for", TOK_FOR);
    insertKeyword("if", TOK_IF);
    insertKeyword("in", TOK_IN);
    insertKeyword("lambda", TOK_LAMBDA);
    insertKeyword("let", TOK_LET);
    insertKeyword("match", TOK_MATCH);
    insertKeyword("not", TOK_NOT);
    insertKeyword("of", TOK_OF);
    insertKeyword("or", TOK_OR);
    insertKeyword("pass", TOK_PASS);
    insertKeyword("return", TOK_RETURN);
    insertKeyword("type", TOK_TYPE);
    insertKeyword("var", TOK_VAR);
    insertKeyword("while", TOK_WHILE);
    insertKeyword("Int", TOK_INT);
    insertKeyword("Float", TOK_FLOAT);
    insertKeyword("String", TOK_STRING);
    insertKeyword("Bool", TOK_BOOL);
    insertKeyword("Void", TOK_VOID);
    insertKeyword("True", TOK_TRUE);
    insertKeyword("False", TOK_FALSE);
}

void setStream(FILE* file, const char* name) {
    stream = file;
    file_name = name;
    setupKeywords();
}


// we saw the start of a number, lex it!
Token lexString() {
    // grab the whole thang
    int i = 0;
    bool done = false;
    while (!done) {
        char next = fgetc(stream);
        if (next == '"') {
            done = true;
        } else if (next == '\\') {
            next = fgetc(stream);
            switch (next) {
                case '"':
                    buffer[i++] = '\"';
                    break;
                case '\\':
                    buffer[i++] = '\\';
                    break;
                case 'n':
                    buffer[i++] = '\n';
                    break;
                case 't':
                    buffer[i++] = '\t';
                    break;
                default:
                    issue(file_name, line_number, "invalid escape sequence in string literal: %c", next);
            }
        } else {
            buffer[i] = next;
            i++;
        }
        if (i >= BUFFER_SIZE) {
            issue(file_name, line_number, "string literal too long");
            // consume the rest of it
            while (next != '"') next = fgetc(stream);
            break;
        }
    }
    buffer[i] = '\0';
    return makeToken(TOK_STRINGVAL, strdup(buffer), line_number);
}



// we saw the start of a number, lex it!
// TODO add in more for these (hex numbers, scientific notation, etc.
Token lexNumber(char start) {
    // grab the whole thang
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
        if (i >= BUFFER_SIZE) {
            issue(file_name, line_number, "numeric literal too long");
            // consume the rest of it
            while (isdigit(next) || next == '.') next = fgetc(stream);
            break;
        }
    }
    buffer[i] = '\0';

    if (seendot) return makeToken(TOK_FLOATVAL, strdup(buffer), line_number);
    else return makeToken(TOK_INTVAL, strdup(buffer), line_number);
}

// we saw the start of a id or keyword, lex it!
Token lexWord(char start) {
    // grab the whole thang
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
        if (i >= BUFFER_SIZE) {
            issue(file_name, line_number, "identifier name too long");
            // consume the rest of it
            while (isalnum(next) || next == '_') next = fgetc(stream);
            break;
        }
    }
    buffer[i] = '\0';

    // look this up in the keyword hash table and return if found
    TokenType keytoken = lookupKeyword(buffer);
    if (keytoken != TOK_END) return TOK(keytoken);

    // it's an id or type name based on first letter's capitaization
    if (isupper(buffer[0])) return makeToken(TOK_TYPENAME, strdup(buffer), line_number);
    else return makeToken(TOK_IDNAME, strdup(buffer), line_number);
}

// returns the next token in the input stream
Token lex() {
    do {
        // if we need dedents, do that first
        if (dedents_remaining > 0) {
            dedents_remaining--;
            indent_level--;
            return TOK(TOK_DEDENT);
        }

        int current = fgetc(stream);

        // if this is not a space character, and it was the start of the line, dedent all the way
        if (current != ' ' && start_of_line) {
            start_of_line = false;

            if (indent_level > 0) {
                dedents_remaining = indent_level;
                continue;
            }
        }

        switch (current) {
            // we handle operators first, in order of complexity
            case '~': return TOK(TOK_COMPLEMENT);
            case ',': return TOK(TOK_COMMA);
            case '(': return TOK(TOK_LPAREN);
            case ')': return TOK(TOK_RPAREN);
            case '[': return TOK(TOK_LBRACK);
            case ']': return TOK(TOK_RBRACK);
            case '{': return TOK(TOK_LBRACE);
            case '}': return TOK(TOK_RBRACE);
            case '_': return TOK(TOK_USCORE);

            case '+': return TOK(match('=') ? TOK_PLUSASSIGN : TOK_PLUS);
            case '%': return TOK(match('=') ? TOK_MODASSIGN : TOK_MODULUS);
            case '&': return TOK(match('=') ? TOK_BITANDASSIGN : TOK_BITAND);
            case '^': return TOK(match('=') ? TOK_BITXORASSIGN : TOK_BITXOR);
            case '=': return TOK(match('=') ? TOK_EQUALS : TOK_ASSIGN);
            case ':': return TOK(match(':') ? TOK_CONS : TOK_COLON);

            case '!':
                if (match('=')) return TOK(TOK_NOTEQUALS);
                else {
                    issue(file_name, line_number, "stray '!' in program");
                    continue;
                }

            case '.':
                if (match('.')) return TOK(TOK_ELIPSIS);
                else {
                    // it might be the start of a number
                    char next = fgetc(stream);
                    ungetc(next, stream);
                    if (isdigit(next)) return lexNumber(current);
                    else {
                        issue(file_name, line_number, "stray '.' in program");
                        continue;
                    }
                }

            case '-':
                if (match('=')) return TOK(TOK_MINUSASSIGN);
                else if (match('>')) return TOK(TOK_ARROW);
                else return TOK(TOK_MINUS);

            case '*':
                if (match('=')) return TOK(TOK_TIMESASSIGN);
                else if (match('*')) {
                if (match('=')) return TOK(TOK_POWERASSIGN);
                    else return TOK(TOK_POWER);
                } else return TOK(TOK_TIMES);

            case '/':
                if (match('='))
                    return TOK(TOK_DIVASSIGN);
                else if (match('/')) {
                    if (match('=')) return TOK(TOK_INTDIVASSIGN);
                    else return TOK(TOK_INTDIV);

                } else return TOK(TOK_DIVIDE);

            case '<':
               if (match('=')) return TOK(TOK_LESSEQ);
               else if (match('<')) {
                   if (match('=')) return TOK(TOK_LSHIFTASSIGN);
                   else return TOK(TOK_LSHIFT);
               } else return TOK(TOK_LESS);

            case '>':
               if (match('=')) return TOK(TOK_GREATEREQ);
               else if (match('>')) {
                   if (match('=')) return TOK(TOK_RSHIFTASSIGN);
                   else return TOK(TOK_RSHIFT);
               } else return TOK(TOK_GREATER);


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
               issue(file_name, line_number, "stray '\\t' in program");
               continue;
            case '\r':
               issue(file_name, line_number, "stray '\\r' in program");
               continue;

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
                       issue(file_name, line_number, "indentation of %d spaces not consistent with previous indent width of %d",
                               spaces, spaces_per_indent);
                       continue;
                   }

                   // if we indented, do that
                   if (level == (indent_level + 1)) {
                       indent_level++;
                       return TOK(TOK_INDENT);
                   }

                   // if we indented too much, that's an error
                   if (level > indent_level) {
                       issue(file_name, line_number, "indentation of more than one level encountered");
                       continue;
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
                return TOK(TOK_NEWLINE);

            // this is the line extender character we DONT say start of line
            case '\\':
                if (match('\n')) {
                    line_number++;
                    continue;
                } else {
                    issue(file_name, line_number, "unexpected character after line break");
                    // consume rest of this line
                    char next = fgetc(stream);
                    while (next != '\n') next = fgetc(stream);
                    continue;
                }

            // we also allow | to be a line ender because it's nice for discriminated unions
            case '|': 
                if (match('=')) return TOK(TOK_BITORASSIGN);
                else if (match('\n')) {
                    // consume the \n w/o setting start of line to true
                    line_number++;
                    return TOK(TOK_BAR);
                } else return TOK(TOK_BAR);

            case EOF:
                // if we were indented, we need to trigger dedents on subsequent lex calls
                if (indent_level != 0) {
                    dedents_remaining = indent_level;
                    continue;
                } else return TOK(TOK_END);
        }

        // now we need to handle things that don't start with a specifc character
        if (isalpha(current)) {
            return lexWord(current);
        }

        // start of a number
        if (isdigit(current)) {
            return lexNumber(current);
        }

        issue(file_name, line_number, "stray '%c' found in program", current);
    } while (true); // keep looping over skippables
}

