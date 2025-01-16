#ifndef TOKEN_H
#define TOKEN_H

typedef enum {
    // indentation (these are fake tokens lexer adds in)
    INDENT,
    DEDENT,

    // keywords
    AND,
    ASSERT,
    BREAK,
    CASE,
    CONTINUE,
    DEF,
    ELIF,
    ELSE,
    FOR,
    IF,
    IN,
    LAMBDA,
    LET,
    MATCH,
    NOT,
    OF,
    OR,
    PASS,
    RETURN,
    TYPE,
    VAR,
    WHILE,

    // pre-defined type names
    INT,
    FLOAT,
    STRING,
    BOOL,
    VOID,
    TRUE,
    FALSE,

    // operators and syntax
    PLUS,
    MINUS,
    DIVIDE,
    TIMES,
    MODULUS,
    POWER,
    INTDIV,
    COMPLEMENT,
    LSHIFT,
    RSHIFT,
    BITAND,
    BITXOR,
    LESS,
    GREATER,
    LESSEQ,
    GREATEREQ,
    EQUALS,
    NOTEQUALS,
    COLON,
    COMMA,
    ASSIGN,
    LPAREN,
    RPAREN,
    LBRACK,
    RBRACK,
    LBRACE,
    RBRACE,
    ELIPSIS,
    BAR,
    ARROW,
    CONS,
    USCORE,
    PLUSASSIGN,
    MINUSASSIGN,
    TIMESASSIGN,
    DIVASSIGN,
    MODASSIGN,
    POWERASSIGN,
    INTDIVASSIGN,
    LSHIFTASSIGN,
    RSHIFTASSIGN,
    BITANDASSIGN,
    BITORASSIGN,
    BITXORASSIGN,

    // chai differentiates between names for ids and types (the former start w/ lowercase and the latter uppercase)
    IDNAME,
    TYPENAME,

    // literal values
    INTVAL,
    FLOATVAL,
    STRINGVAL,

    // end of the input
    END
} Token;

const char* tokenString(Token t);

#endif

