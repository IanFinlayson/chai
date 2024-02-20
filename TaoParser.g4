parser grammar TaoParser;

options {
    tokenVocab = TaoLexer;
}

// the program is a list of toplevel constructs (or blank lines)
program: (toplevel | NEWLINE)*;

// TODO other top level things like classes
toplevel: functiondef
        | typedef
        ;

// a function definition line
functiondef: DEF IDNAME LPAREN paramlist RPAREN type? COLON NEWLINE INDENT statements DEDENT;

// a list of 0 or more formal parameters to a function
paramlist: (IDNAME type COMMA)* IDNAME type
         |
         ;

typedef: TYPE TYPENAME ASSIGN type;

// any data type
type: INT
    | FLOAT
    | STRING
    | BOOL
    | TYPENAME

    // a tuple type
    | LPAREN (type COMMA)+ type RPAREN

    // a list type
    | LBRACK type RBRACK

    // a dict type
    | LBRACE type COLON type RBRACE

    // discriminated union
    | unionpart BAR unionpart (BAR unionpart)*
    ;
    
// piece of a discriminated union
unionpart: TYPENAME (OF type)?;

// a list of 0 or more statements (or blank lines)
statements: (statement | NEWLINE)*;

// any valid Tao statement
statement: functioncall
         | lvalue ASSIGN expression
         | (VAR | LET) IDNAME type? (ASSIGN expression)?
         | modassign
         | RETURN expression
         | ifstmt
         | FOR IDNAME IN expression COLON NEWLINE INDENT statements DEDENT
         | WHILE expression COLON NEWLINE INDENT statements DEDENT
         ;

// something that can be assigned into -- basically name or list/dict/tuple/set reference
lvalue: IDNAME
      | lvalue LBRACK expression RBRACK
      ;

// an operator assign type statement like `i += 1`
modassign: lvalue op=(PLUSASSIGN | MINUSASSIGN | TIMESASSIGN | DIVASSIGN | MODASSIGN) expression;

// we don't get else if for free
ifstmt: IF expression COLON NEWLINE INDENT statements DEDENT elifclause* elseclause?;
elifclause: ELIF expression COLON NEWLINE INDENT statements DEDENT;
elseclause: ELSE COLON NEWLINE INDENT statements DEDENT;

// a function call
functioncall: IDNAME LPAREN arglist RPAREN;

// the supplied argument list, containing 0 or more expressions, separated with commas
arglist: (expression COMMA)* expression
       |
       ;

// any valid Tao expression
expression: NOT expression
          | op=(PLUS | MINUS) expression
          | expression op=(TIMES | DIVIDE | MODULUS) expression
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

    // list index like nums[i]
    | term LBRACK expression RBRACK

    // sth in parens
    | LPAREN expression RPAREN

    // a list literal like [3, 4, 5]
    | LBRACK (expression (COMMA expression)*)? RBRACK
    | functioncall

    // tuple literal like (3, 4, 5) (we don't allow 0 or 1 length tuples)
    | LPAREN (expression COMMA)+ expression RPAREN

    // a dictionary literal
    | LBRACE (dictentry (COMMA dictentry)*)? RBRACE
    ;

dictentry: expression COLON expression;

