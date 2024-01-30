parser grammar TaoParser;

options {
    tokenVocab = TaoLexer;
}

program: (toplevel | NEWLINE)*;

// TODO other top level things
toplevel: functiondef
        ;

functiondef: DEF IDNAME LPAREN paramlist RPAREN COLON NEWLINE INDENT statements DEDENT
           ;

// TODO
paramlist:
         ;

statements: (statement | NEWLINE)*;

// TODO more options
statement: functioncall
         | IDNAME ASSIGN expression
         ;


functioncall: IDNAME LPAREN arglist RPAREN;

arglist: (expression COMMA)* expression
       |
       ;


// TODO more here
expression: NOT expression
          | expression op=(TIMES | DIVIDE) expression
          | expression op=(PLUS | MINUS) expression
          | expression op=(LESS | GREATER | LESSEQ | GREATEREQ | EQUALS | NOTEQUALS) expression
          | expression AND expression
          | expression OR expression
          | term LBRACK expression RBRACK
          | term
          ;

term: IDNAME
    | INTVAL
    | FLOATVAL
    | STRINGVAL
    | TRUE
    | FALSE
    | LPAREN expression RPAREN
    | LBRACK (expression (COMMA expression)*)? RBRACK
    ;


/*
statement: lvalue ASSIGN expression SEMI                # assign
         | PRINT LPAREN expression RPAREN SEMI          # print
         | PRINTLN LPAREN expression RPAREN SEMI        # println
         | BEGIN statement* END                         # sequence
         | IF expression THEN statement                 # ifthen
         | IF expression THEN statement ELSE statement  # ifthenelse
         | WHILE expression DO statement                # whiledo
         ;
*/
