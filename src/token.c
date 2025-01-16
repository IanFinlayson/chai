#include <stdio.h>

#include "token.h"

Token makeToken(TokenType type, char* lexeme, int line) {
    Token tok = {type, lexeme, line};
    return tok;
}

// print out the text of a token, for debugging porpoises
void printToken(Token t) {
    switch (t.type) {
        case TOK_INDENT: printf("{\n"); break;
        case TOK_DEDENT: printf("\n}\n"); break;
        case TOK_NEWLINE: printf("\n"); break;
        case TOK_AND: printf(" and "); break;
        case TOK_ASSERT: printf("assert "); break;
        case TOK_BREAK: printf("break"); break;
        case TOK_CASE: printf("case "); break;
        case TOK_CONTINUE: printf("continue"); break;
        case TOK_DEF: printf("def "); break;
        case TOK_ELIF: printf("elif "); break;
        case TOK_ELSE: printf("else"); break;
        case TOK_FOR: printf("for "); break;
        case TOK_IF: printf("if "); break;
        case TOK_IN: printf(" in "); break;
        case TOK_LAMBDA: printf("lambda "); break;
        case TOK_LET: printf("let "); break;
        case TOK_MATCH: printf("match "); break;
        case TOK_NOT: printf("not "); break;
        case TOK_OF: printf("of "); break;
        case TOK_OR: printf(" or "); break;
        case TOK_PASS: printf("pass"); break;
        case TOK_RETURN: printf("return "); break;
        case TOK_TYPE: printf("type"); break;
        case TOK_VAR: printf("var "); break;
        case TOK_WHILE: printf("while "); break;
        case TOK_INT: printf("Int"); break;
        case TOK_FLOAT: printf("Float"); break;
        case TOK_STRING: printf("String"); break;
        case TOK_BOOL: printf("Bool"); break;
        case TOK_VOID: printf("Void"); break;
        case TOK_TRUE: printf("True"); break;
        case TOK_FALSE: printf("False"); break;
        case TOK_PLUS: printf(" + "); break;
        case TOK_MINUS: printf(" - "); break;
        case TOK_DIVIDE: printf(" / "); break;
        case TOK_TIMES: printf(" * "); break;
        case TOK_MODULUS: printf(" %% "); break;
        case TOK_POWER: printf(" ** "); break;
        case TOK_INTDIV: printf(" // "); break;
        case TOK_COMPLEMENT: printf(" ~ "); break;
        case TOK_LSHIFT: printf(" << "); break;
        case TOK_RSHIFT: printf(" >> "); break;
        case TOK_BITAND: printf(" & "); break;
        case TOK_BITXOR: printf(" ^ "); break;
        case TOK_LESS: printf(" < "); break;
        case TOK_GREATER: printf(" > "); break;
        case TOK_LESSEQ: printf(" <= "); break;
        case TOK_GREATEREQ: printf(" >= "); break;
        case TOK_EQUALS: printf(" == "); break;
        case TOK_NOTEQUALS: printf(" != "); break;
        case TOK_COLON: printf(":"); break;
        case TOK_COMMA: printf(", "); break;
        case TOK_ASSIGN: printf(" = "); break;
        case TOK_LPAREN: printf(" ("); break;
        case TOK_RPAREN: printf(") "); break;
        case TOK_LBRACK: printf("["); break;
        case TOK_RBRACK: printf("] "); break;
        case TOK_LBRACE: printf("{"); break;
        case TOK_RBRACE: printf("} "); break;
        case TOK_ELIPSIS: printf(" .. "); break;
        case TOK_BAR: printf(" | "); break;
        case TOK_ARROW: printf(" -> "); break;
        case TOK_CONS: printf(" :: "); break;
        case TOK_USCORE: printf("_"); break;
        case TOK_PLUSASSIGN: printf(" += "); break;
        case TOK_MINUSASSIGN: printf(" -= "); break;
        case TOK_TIMESASSIGN: printf(" *= "); break;
        case TOK_DIVASSIGN: printf(" /= "); break;
        case TOK_MODASSIGN: printf(" %%= "); break;
        case TOK_POWERASSIGN: printf(" **= "); break;
        case TOK_INTDIVASSIGN: printf(" //= "); break;
        case TOK_LSHIFTASSIGN: printf(" <<= "); break;
        case TOK_RSHIFTASSIGN: printf(" >>= "); break;
        case TOK_BITANDASSIGN: printf(" &= "); break;
        case TOK_BITORASSIGN: printf(" |= "); break;
        case TOK_BITXORASSIGN: printf(" ^= "); break;
        case TOK_IDNAME: printf("%s", t.lexeme); break;
        case TOK_TYPENAME: printf("%s", t.lexeme); break;
        case TOK_INTVAL: printf("%s", t.lexeme); break;
        case TOK_FLOATVAL: printf("%s", t.lexeme); break;
        case TOK_STRINGVAL: printf("\"%s\"", t.lexeme); break;
        case TOK_END: printf("END"); break;
    }
}


