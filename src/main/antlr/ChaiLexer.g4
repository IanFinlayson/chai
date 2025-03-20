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
CLASS:      'class';
DEF:        'def';
ELIF:       'elif';
ELSE:       'else';
FOR:        'for';
IF:         'if';
IN:         'in';
IMPORT:     'import';
LAMBDA:     'lambda';
LET:        'let';
MATCH:      'match';
NOT:        'not';
OF:         'of';
OR:         'or';
PASS:       'pass';
RETURN:     'return';
SELF:       'self';
TYPE:       'type';
VAR:        'var';
WHILE:      'while';
YIELD:      'yield';

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
STAR:       '*';
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
DOT:        '.';

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

/* we take ints in 4 bases */
INTVAL: DECINT | OCTINT | BININT | HEXINT;
DECINT: '0' | [1-9][0-9]*;
OCTINT: '0' [oO] [0-7]+;
BININT: '0' [bB] [01]+;
HEXINT: '0' [xX] [0-9a-fA-F]+;

/* floats can be regular or in scientific notation */
FLOATVAL: REGFLOAT | SCIFLOAT;
REGFLOAT: [0-9]* '.' [0-9]+;
SCIFLOAT: (DECINT | REGFLOAT) [eE] [+-]? DECINT;

/* thank you antlr book */
STRINGVAL: '"' ( ~[\\"\r\n] | ESC)* '"';
fragment ESC: '\\' [tn"\\];

/* comments */
COMMENT: '#'~[\n\r]* -> channel(HIDDEN);

