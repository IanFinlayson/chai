#ifndef TOKEN_H
#define TOKEN_H

typedef enum {
    // indentation (these are fake tokens lexer adds in)
    TOK_INDENT,
    TOK_DEDENT,
    TOK_NEWLINE,

    // keywords
    TOK_AND,
    TOK_ASSERT,
    TOK_BREAK,
    TOK_CASE,
    TOK_CONTINUE,
    TOK_DEF,
    TOK_ELIF,
    TOK_ELSE,
    TOK_FOR,
    TOK_IF,
    TOK_IN,
    TOK_LAMBDA,
    TOK_LET,
    TOK_MATCH,
    TOK_NOT,
    TOK_OF,
    TOK_OR,
    TOK_PASS,
    TOK_RETURN,
    TOK_TYPE,
    TOK_VAR,
    TOK_WHILE,

    // pre-defined type names
    TOK_INT,
    TOK_FLOAT,
    TOK_STRING,
    TOK_BOOL,
    TOK_VOID,
    TOK_TRUE,
    TOK_FALSE,

    // operators and syntax
    TOK_PLUS,
    TOK_MINUS,
    TOK_DIVIDE,
    TOK_TIMES,
    TOK_MODULUS,
    TOK_POWER,
    TOK_INTDIV,
    TOK_COMPLEMENT,
    TOK_LSHIFT,
    TOK_RSHIFT,
    TOK_BITAND,
    TOK_BITXOR,
    TOK_LESS,
    TOK_GREATER,
    TOK_LESSEQ,
    TOK_GREATEREQ,
    TOK_EQUALS,
    TOK_NOTEQUALS,
    TOK_COLON,
    TOK_COMMA,
    TOK_ASSIGN,
    TOK_LPAREN,
    TOK_RPAREN,
    TOK_LBRACK,
    TOK_RBRACK,
    TOK_LBRACE,
    TOK_RBRACE,
    TOK_ELIPSIS,
    TOK_BAR,
    TOK_ARROW,
    TOK_CONS,
    TOK_USCORE,
    TOK_PLUSASSIGN,
    TOK_MINUSASSIGN,
    TOK_TIMESASSIGN,
    TOK_DIVASSIGN,
    TOK_MODASSIGN,
    TOK_POWERASSIGN,
    TOK_INTDIVASSIGN,
    TOK_LSHIFTASSIGN,
    TOK_RSHIFTASSIGN,
    TOK_BITANDASSIGN,
    TOK_BITORASSIGN,
    TOK_BITXORASSIGN,

    // chai differentiates between names for ids and types (the former start w/ lowercase and the latter uppercase)
    TOK_IDNAME,
    TOK_TYPENAME,

    // literal values
    TOK_INTVAL,
    TOK_FLOATVAL,
    TOK_STRINGVAL,

    // end of the input
    TOK_END
} TokenType;

typedef struct {
    TokenType type;
    char* lexeme;
    int line;
} Token;

Token makeToken(TokenType type, char* lexeme, int line);
void printToken(Token t);

#endif

