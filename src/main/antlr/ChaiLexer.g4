lexer grammar ChaiLexer;

/* setup the indentation-based lexing */
@lexer::members {
	private int INDENT_TOKEN = ChaiParser.INDENT;
	private int DEDENT_TOKEN = ChaiParser.DEDENT;
}
import DentLexer;

/* our keywords */
AND:        'and';
ASSERT:     'assert';
BREAK:      'break';
CASE:       'case';
CONTINUE:   'continue';
DEF:        'def';
ELIF:       'elif';
ELSE:       'else';
FOR:        'for';
IF:         'if';
IN:         'in';
LAMBDA:     'lambda';
LET:        'let';
MATCH:      'match';
NOT:        'not';
OF:         'of';
OR:         'or';
PASS:       'pass';
RETURN:     'return';
TYPE:       'type';
VAR:        'var';
WHILE:      'while';

/* pre-defined type names */
INT:        'Int';
FLOAT:      'Float';
STRING:     'String';
BOOL:       'Bool';
VOID:       'Void';
TRUE:       'True';
FALSE:      'False';

PLUS:       '+';
MINUS:      '-';
DIVIDE:     '/';
TIMES:      '*';
MODULUS:    '%';
POWER:      '**';
INTDIV:     '//';

COMPLEMENT: '~';
LSHIFT:     '<<';
RSHIFT:     '>>';
BITAND:     '&';
BITXOR:     '^';

LESS:       '<';
GREATER:    '>';
LESSEQ:     '<=';
GREATEREQ:  '>=';
EQUALS:     '==';
NOTEQUALS:  '!=';
COLON:      ':';
COMMA:      ',';
ASSIGN:     '=';
LPAREN:     '(';
RPAREN:     ')';
LBRACK:     '[';
RBRACK:     ']';
LBRACE:     '{';
RBRACE:     '}';
ELIPSIS:    '..';
BAR:        '|';
ARROW:      '->';
CONS:       '::';
USCORE:     '_';

PLUSASSIGN:     '+=';
MINUSASSIGN:    '-=';
TIMESASSIGN:    '*=';
DIVASSIGN:      '/=';
MODASSIGN:      '%=';
POWERASSIGN:    '**=';
INTDIVASSIGN:   '//=';

LSHIFTASSIGN:   '<<=';
RSHIFTASSIGN:   '>>=';
BITANDASSIGN:   '&=';
BITORASSIGN:    '|=';
BITXORASSIGN:   '^=';


/* chai differentiates between id (start lower case) and type (upper case) names */
IDNAME:     [a-z][_0-9a-zA-Z]*;
TYPENAME:   [A-Z][_0-9a-zA-Z]*;

/* TODO add in more for these (hex numbers, scientific notation, etc. */
INTVAL:     [0-9]+;
FLOATVAL:   [0-9]*'.'[0-9]+;

/* thank you antlr book */
STRINGVAL: '"' ( ~[\\"\r\n] | ESC)* '"';
fragment ESC: '\\' [tn"\\];


/* comments */
COMMENT: '#'~[\n\r]* -> channel(HIDDEN);

