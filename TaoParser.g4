parser grammar TaoParser;

options {
    tokenVocab = TaoLexer;
}

// the program is a list of toplevel constructs (or blank lines)
program: (toplevel | NEWLINE)*;

// TODO other top level things like typedef
toplevel: functiondef
        ;

// a function definition line
functiondef: DEF IDNAME LPAREN paramlist RPAREN type? COLON NEWLINE INDENT statements DEDENT
           ;

// a list of 0 or more formal parameters to a function
paramlist: (IDNAME type COMMA)* IDNAME type
         |
         ;

// any data type
type: INT
    | FLOAT
    | STRING
    | BOOL
    | TYPENAME
    ;

// a list of 0 or more statements (or blank lines)
statements: (statement | NEWLINE)*;

// any valid Tao statement
statement: functioncall
         | IDNAME ASSIGN expression
         | RETURN expression
         | IF expression COLON NEWLINE INDENT statements DEDENT
         | IF expression COLON NEWLINE INDENT statements DEDENT ELSE COLON NEWLINE INDENT statements DEDENT
         | FOR IDNAME IN expression COLON NEWLINE INDENT statements DEDENT
         | WHILE expression COLON NEWLINE INDENT statements DEDENT
         ;

// a function call
functioncall: IDNAME LPAREN arglist RPAREN;

// the supplied argument list, containing 0 or more expressions, separated with commas
arglist: (expression COMMA)* expression
       |
       ;

// any valid Tao expression
expression: NOT expression
          | expression op=(TIMES | DIVIDE) expression
          | expression op=(PLUS | MINUS) expression
          | expression op=(LESS | GREATER | LESSEQ | GREATEREQ | EQUALS | NOTEQUALS) expression
          | expression AND expression
          | expression OR expression
          | expression ELIPSIS expression
          | term
          ;
          
// a term is an individual piece of an expression
term: IDNAME
    | INTVAL
    | FLOATVAL
    | STRINGVAL
    | TRUE
    | FALSE
    | term LBRACK expression RBRACK
    | LPAREN expression RPAREN
    | LBRACK (expression (COMMA expression)*)? RBRACK
    | functioncall
    ;

