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
functiondef: DEF IDNAME typeparams? LPAREN paramlist? RPAREN type? COLON NEWLINE INDENT statements DEDENT;

// a list of 0 or more formal parameters to a function
paramlist: (IDNAME type COMMA)* IDNAME type;

// a type definition such as a discriminated union (but really anything)
typedef: TYPE TYPENAME typeparams? ASSIGN type;

// one or more type parameters for generic code
typeparams: LESS (TYPENAME COMMA)* TYPENAME GREATER;

// any data type
type: INT
    | FLOAT
    | STRING
    | BOOL
    | VOID
    | TYPENAME typeparamfills?

    // a tuple type
    | LPAREN (type COMMA)+ type RPAREN

    // a list type
    | LBRACK type RBRACK

    // a dict type
    | LBRACE type COLON type RBRACE
    
    // a set type
    | LBRACE type RBRACE

    // discriminated union
    | unionpart BAR unionpart (BAR unionpart)*
    
    // a function type
    | type ARROW type
    ;

// filling in type params with real types
typeparamfills: LESS (type COMMA)* type GREATER;
    
// piece of a discriminated union
unionpart: TYPENAME (OF type)?;

// a list of 1 or more statements (or blank lines)
statements: (statement | NEWLINE)+;

// any valid Tao statement
statement: functioncall
         | lvalue ASSIGN expression
         | (VAR | LET) IDNAME type? (ASSIGN expression)?
         | modassign
         | ASSERT expression
         | RETURN expression?
         | ifstmt
         | PASS | CONTINUE | BREAK
         | FOR IDNAME IN expression COLON NEWLINE INDENT statements DEDENT
         | WHILE expression COLON NEWLINE INDENT statements DEDENT
         | MATCH expression COLON NEWLINE INDENT caseline+ DEDENT
         | toplevel
         ;

// something that can be assigned into -- basically name or list/dict/tuple/set reference
lvalue: IDNAME
      | lvalue LBRACK expression RBRACK
      ;

// an operator assign type statement like `i += 1`
modassign: lvalue op=(PLUSASSIGN | MINUSASSIGN | TIMESASSIGN | DIVASSIGN | MODASSIGN
                    | LSHIFTASSIGN | RSHIFTASSIGN | BITANDASSIGN | BITORASSIGN | BITXORASSIGN)
                 expression;

// a case in a match statement -- TODO do more pattern matches!
caseline: CASE TYPENAME destructure? COLON NEWLINE INDENT statements DEDENT;

// a thing that can be used as part of a destructure
destructure: IDNAME
           | LPAREN (destructure COMMA)+ destructure RPAREN    // tuple destructure
           ;

// we don't get else if for free
ifstmt: IF expression COLON NEWLINE INDENT statements DEDENT elifclause* elseclause?;
elifclause: ELIF expression COLON NEWLINE INDENT statements DEDENT;
elseclause: ELSE COLON NEWLINE INDENT statements DEDENT;

// a function call
functioncall: term LPAREN arglist? RPAREN;

// the supplied argument list, containing 0 or more expressions, separated with commas
arglist: (expression COMMA)* expression;

// any valid Tao expression
expression: <assoc=right> expression POWER expression
          | op=(COMPLEMENT | PLUS | MINUS) expression
          | expression op=(TIMES | DIVIDE | MODULUS) expression
          | expression op=(PLUS | MINUS) expression
          | expression op=(LSHIFT | RSHIFT) expression
          | expression BITAND expression
          | expression BITXOR expression
          | expression BAR expression
          | expression op=(LESS | GREATER | LESSEQ | GREATEREQ | EQUALS | NOTEQUALS) expression
          | expression IN expression
          | NOT expression
          | expression AND expression
          | expression OR expression
          | expression IF expression ELSE expression
          | lambda
          | functioncall
          | term
          ;

// a lambda function
lambda: LAMBDA lambdaParams? COLON expression;
lambdaParams: (IDNAME type COMMA)* IDNAME type;

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
          
    // a list range
    | LBRACK expression ELIPSIS expression RBRACK
    
    // a list comprehension
    | LBRACK expression FOR IDNAME IN expression (IF expression)? RBRACK

    // tuple literal like (3, 4, 5) (we don't allow 0 or 1 length tuples)
    | LPAREN (expression COMMA)+ expression RPAREN

    // a set literal
    | LBRACE (expression (COMMA expression)*)? RBRACE

    // a dictionary literal
    | LBRACE (dictentry (COMMA dictentry)*)? RBRACE
    
    // a discriminated union
    | TYPENAME expression?
    ;

dictentry: expression COLON expression;

