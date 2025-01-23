#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

#include "lexer.h"
#include "errors.h"
#include "token.h"

// used to make tokens without typing line number all the time
#define TOK(type) makeToken(type, NULL, state->line_number)

// used for reading in strings, ids, numbers...
#define BUFFER_SIZE 1024
char buffer[BUFFER_SIZE];

// checks if next character of input is something, if not put it back
bool match(char expected, LexerState* state) {
    char next = fgetc(state->stream);
    if (next == expected) {
        return true;
    } else {
        ungetc(next, state->stream);
        return false;
    }
}

// we create a hash table of reserved words so when we see an id we can look it up
#define KEYWORD_SIZE 16
#define HASHTABLE_SIZE 255

typedef struct {
    char keyword[KEYWORD_SIZE];
    TokenType token;
} KeywordTableEntry;

// this is global, but all lexers can share this table
KeywordTableEntry keywords[HASHTABLE_SIZE];
bool table_innited = false;

// get a hash code of a keyword
int hashKeyword(const char* keyword) {
    int length = strlen(keyword);
    int code = 0;
    for (int i = 0; i < length; i++) {
        code += keyword[i] * 31;
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

// TODO call this ONE time
void setupKeywords() {
    for (int i = 0; i < HASHTABLE_SIZE; i++) {
        strcpy(keywords[i].keyword, "");
        keywords[i].token = TOK_END;
    }

    insertKeyword("and", TOK_AND);
    insertKeyword("assert", TOK_ASSERT);
    insertKeyword("break", TOK_BREAK);
    insertKeyword("case", TOK_CASE);
    insertKeyword("class", TOK_CLASS);
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

TokenType lookupKeyword(const char* keyword) {
    // ensure table is built one time
    if (!table_innited) {
        setupKeywords();
        table_innited = true;
    }

    int index = hashKeyword(keyword);

    while (keywords[index].token != TOK_END && strcmp(keywords[index].keyword, keyword)) {
        index = (index + 1) % HASHTABLE_SIZE;
    }

    return keywords[index].token;
}

LexerState createLexerState(FILE* file, const char* name) {
    LexerState state;
    state.stream = file;
    state.file_name = name;
    state.line_number = 1;
    state.start_of_line = true;
    state.spaces_per_indent = 0;
    state.indent_level = 0;
    state.dedents_remaining = 0;
    return state;
}

// we saw the start of a number, lex it!
Token lexString(LexerState* state) {
    // grab the whole thang
    int i = 0;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (next == '"') {
            done = true;
        } else if (next == '\\') {
            next = fgetc(state->stream);
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
                    issue(state->file_name, state->line_number, "invalid escape sequence in string literal: %c", next);
            }
        } else {
            buffer[i] = next;
            i++;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "string literal too long");
            // consume the rest of it
            while (next != '"') next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';
    return makeToken(TOK_STRINGVAL, strdup(buffer), state->line_number);
}

// lex a hex constant
Token lexHex(LexerState* state) {
    // grab the whole thang
    buffer[0] = '0';
    buffer[1] = 'x';

    int i = 2;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (isxdigit(next)) {
            buffer[i] = next;
            i++;
        } else if (isalpha(next)) {
            issue(state->file_name, state->line_number, "illegal '%c' found in hexadecimal literal", next);
            ungetc(next, state->stream);
            done = true;
        }
        else {
            ungetc(next, state->stream);
            done = true;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "hexadecimal literal too long");
            // consume the rest of it
            while (isxdigit(next)) next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';
    return makeToken(TOK_INTVAL, strdup(buffer), state->line_number);
}

Token lexBinary(LexerState* state) {
    // grab the whole thang
    buffer[0] = '0';
    buffer[1] = 'b';

    int i = 2;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (next == '0' || next == '1') {
            buffer[i] = next;
            i++;
        } else if (isalnum(next)) {
            issue(state->file_name, state->line_number, "illegal '%c' found in binary literal", next);
            ungetc(next, state->stream);
            done = true;
        }
        else {
            ungetc(next, state->stream);
            done = true;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "binary literal too long");
            // consume the rest of it
            while (next == '0' || next == '1') next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';
    return makeToken(TOK_INTVAL, strdup(buffer), state->line_number);
}

Token lexOctal(LexerState* state) {
    // grab the whole thang
    buffer[0] = '0';
    buffer[1] = 'o';

    int i = 2;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (next >= '0' && next <= '7') {
            buffer[i] = next;
            i++;
        } else if (isalnum(next)) {
            issue(state->file_name, state->line_number, "illegal '%c' found in octal literal", next);
            ungetc(next, state->stream);
            done = true;
        }
        else {
            ungetc(next, state->stream);
            done = true;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "octal literal too long");
            // consume the rest of it
            while (next >= '0' && next <= '7') next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';
    return makeToken(TOK_INTVAL, strdup(buffer), state->line_number);


}

// we saw the start of a number, lex it!
Token lexNumber(char start, LexerState* state) {
    // check for hex, binary, octal
    if (start == '0') {
        char next = fgetc(state->stream);
        if (next == 'x' || next == 'X') return lexHex(state);
        else if (next == 'b' || next == 'B') return lexBinary(state);
        else if (next == 'o' || next == 'O') return lexOctal(state);
        else ungetc(next, state->stream);
    }

    // grab the whole thang
    buffer[0] = start;
    bool seendot = start == '.';
    bool seene = false;

    int i = 1;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (next == '.' && !seendot) {
            buffer[i] = next;
            i++;
            seendot = true;
        }
        else if (next == '.') {
            issue(state->file_name, state->line_number, "multiple '.' found in number literal");
            ungetc(next, state->stream);
            done = true;
        }
        // scientific notation can have an e
        else if ((next == 'e' || next == 'E') && !seene) {
            buffer[i] = next;
            i++;
            seene = true;

            next = fgetc(state->stream);
            if (next == '-') {
                buffer[i] = next;
                i++;
            } else {
                ungetc(next, state->stream);
            }
        }
        else if (next == 'e' || next == 'E') {
            issue(state->file_name, state->line_number, "multiple 'E' found in number literal");
            ungetc(next, state->stream);
            done = true;
        }
        else if (isdigit(next)) {
            buffer[i] = next;
            i++;
        }
        else if (isalpha(next)) {
            issue(state->file_name, state->line_number, "'%c' found in number literal", next);
            ungetc(next, state->stream);
            done = true;
        } else {
            ungetc(next, state->stream);
            done = true;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "numeric literal too long");
            // consume the rest of it
            while (isdigit(next) || next == '.') next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';

    if (seendot || seene) return makeToken(TOK_FLOATVAL, strdup(buffer), state->line_number);
    else return makeToken(TOK_INTVAL, strdup(buffer), state->line_number);
}

// we saw the start of a id or keyword, lex it!
Token lexWord(char start, LexerState* state) {
    // grab the whole thang
    buffer[0] = start;

    int i = 1;
    bool done = false;
    while (!done) {
        char next = fgetc(state->stream);
        if (!isalnum(next) && next != '_') {
            ungetc(next, state->stream);
            done = true;
        } else {
            buffer[i] = next;
            i++;
        }
        if (i >= BUFFER_SIZE) {
            issue(state->file_name, state->line_number, "identifier name too long");
            // consume the rest of it
            while (isalnum(next) || next == '_') next = fgetc(state->stream);
            break;
        }
    }
    buffer[i] = '\0';

    // look this up in the keyword hash table and return if found
    TokenType keytoken = lookupKeyword(buffer);
    if (keytoken != TOK_END) return TOK(keytoken);

    // it's an id or type name based on first letter's capitaization
    if (isupper(buffer[0])) return makeToken(TOK_TYPENAME, strdup(buffer), state->line_number);
    else return makeToken(TOK_IDNAME, strdup(buffer), state->line_number);
}

// returns the next token in the input stream
Token lex(LexerState* state) {
    do {
        // if we need dedents, do that first
        if (state->dedents_remaining > 0) {
            state->dedents_remaining--;
            state->indent_level--;
            return TOK(TOK_DEDENT);
        }

        int current = fgetc(state->stream);

        // if this is not a space character, and it was the start of the line, dedent all the way
        if (current != ' ' && current != '\n' && state->start_of_line) {
            state->start_of_line = false;
            ungetc(current, state->stream);

            if (state->indent_level > 0) {
                state->dedents_remaining = state->indent_level;
            }

            continue;
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

            case '+': return TOK(match('=', state) ? TOK_PLUSASSIGN : TOK_PLUS);
            case '%': return TOK(match('=', state) ? TOK_MODASSIGN : TOK_MODULUS);
            case '&': return TOK(match('=', state) ? TOK_BITANDASSIGN : TOK_BITAND);
            case '^': return TOK(match('=', state) ? TOK_BITXORASSIGN : TOK_BITXOR);
            case '=': return TOK(match('=', state) ? TOK_EQUALS : TOK_ASSIGN);
            case ':': return TOK(match(':', state) ? TOK_CONS : TOK_COLON);

            case '!':
                if (match('=', state)) return TOK(TOK_NOTEQUALS);
                else {
                    issue(state->file_name, state->line_number, "stray '!' in program");
                    continue;
                }

            case '.':
                if (match('.', state)) return TOK(TOK_ELIPSIS);
                else {
                    // it might be the start of a number
                    char next = fgetc(state->stream);
                    ungetc(next, state->stream);
                    if (isdigit(next)) return lexNumber(current, state);
                    else {
                        issue(state->file_name, state->line_number, "stray '.' in program");
                        continue;
                    }
                }

            case '-':
                if (match('=', state)) return TOK(TOK_MINUSASSIGN);
                else if (match('>', state)) return TOK(TOK_ARROW);
                else return TOK(TOK_MINUS);

            case '*':
                if (match('=', state)) return TOK(TOK_TIMESASSIGN);
                else if (match('*', state)) {
                if (match('=', state)) return TOK(TOK_POWERASSIGN);
                    else return TOK(TOK_POWER);
                } else return TOK(TOK_TIMES);

            case '/':
                if (match('=', state))
                    return TOK(TOK_DIVASSIGN);
                else if (match('/', state)) {
                    if (match('=', state)) return TOK(TOK_INTDIVASSIGN);
                    else return TOK(TOK_INTDIV);

                } else return TOK(TOK_DIVIDE);

            case '<':
               if (match('=', state)) return TOK(TOK_LESSEQ);
               else if (match('<', state)) {
                   if (match('=', state)) return TOK(TOK_LSHIFTASSIGN);
                   else return TOK(TOK_LSHIFT);
               } else return TOK(TOK_LESS);

            case '>':
               if (match('=', state)) return TOK(TOK_GREATEREQ);
               else if (match('>', state)) {
                   if (match('=', state)) return TOK(TOK_RSHIFTASSIGN);
                   else return TOK(TOK_RSHIFT);
               } else return TOK(TOK_GREATER);


            // comments
            case '#':
               while (current != '\n') {
                   current = fgetc(state->stream);
               }
               continue;

            // string literals
            case '"':
               return lexString(state);

            // we do not allow tabs or carriage returns, for now anyway
            case '\t':
               issue(state->file_name, state->line_number, "stray '\\t' in program");
               continue;
            case '\r':
               issue(state->file_name, state->line_number, "stray '\\r' in program");
               continue;

            // spaces are tough...
            case ' ':
               if (state->start_of_line) {
                   // no longer start of line
                   state->start_of_line = false;

                   // count the number of spaces here
                   int spaces = 0;
                   while (current == ' ') {
                       spaces++;
                       current = fgetc(state->stream);
                   }

                   // if this was ALL spaces, or a comment is hit, skip
                   if (current == '\n' || current == '#') {
                       ungetc(current, state->stream);
                       continue;
                   }

                   // otherwise put the thing after the spaces back in
                   ungetc(current, state->stream);

                   // if this is the FIRST indent, this is the level of one
                   if (state->spaces_per_indent == 0) {
                       state->spaces_per_indent = spaces;
                   }

                   // compute indentation level
                   int level = spaces / state->spaces_per_indent;
                   if ((spaces % state->spaces_per_indent) != 0) {
                       issue(state->file_name, state->line_number, "indentation of %d spaces not consistent with previous indent width of %d",
                               spaces, state->spaces_per_indent);
                       continue;
                   }

                   // if we indented, do that
                   if (level == (state->indent_level + 1)) {
                       state->indent_level++;
                       return TOK(TOK_INDENT);
                   }

                   // if we indented too much, that's an error
                   if (level > state->indent_level) {
                       issue(state->file_name, state->line_number, "indentation of more than one level encountered");
                       continue;
                   }

                   // if we are de-denting, trigger that
                   if (level < state->indent_level) {
                       state->dedents_remaining = state->indent_level - level;
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
                state->start_of_line = true;
                state->line_number++;
                return TOK(TOK_NEWLINE);

            // this is the line extender character we DONT say start of line
            case '\\':
                if (match('\n', state)) {
                    state->line_number++;
                    continue;
                } else {
                    issue(state->file_name, state->line_number, "unexpected character after line break");
                    // consume rest of this line
                    char next = fgetc(state->stream);
                    while (next != '\n') next = fgetc(state->stream);
                    continue;
                }

            // we also allow | to be a line ender because it's nice for discriminated unions
            case '|': 
                if (match('=', state)) return TOK(TOK_BITORASSIGN);
                else if (match('\n', state)) {
                    // consume the \n w/o setting start of line to true
                    state->line_number++;
                    return TOK(TOK_BAR);
                } else return TOK(TOK_BAR);

            case EOF:
                // if we were indented, we need to trigger dedents on subsequent lex calls
                if (state->indent_level != 0) {
                    state->dedents_remaining = state->indent_level;
                    continue;
                } else return TOK(TOK_END);
        }

        // now we need to handle things that don't start with a specifc character
        if (isalpha(current)) {
            return lexWord(current, state);
        }

        // start of a number
        if (isdigit(current)) {
            return lexNumber(current, state);
        }

        issue(state->file_name, state->line_number, "stray '%c' found in program", current);
    } while (true); // keep looping over skippables
}

