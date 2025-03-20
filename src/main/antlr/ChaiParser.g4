parser grammar ChaiParser;

options {
    tokenVocab = ChaiLexer;
}

// a program is a collection of units
program:
    (unit | imprt | NEWLINE)* EOF;

// imports can only exist at top level
imprt: IMPORT IDNAME NEWLINE;

// units can be top-level things, or also inside a class/def
unit: 
    (VAR | LET) IDNAME type? (ASSIGN expression)? NEWLINE
    | functiondef
    | typedef
    | classdef
    ;

// a def statement
functiondef: DEF IDNAME typeparams? LPAREN paramlist? RPAREN functype? COLON NEWLINE INDENT statements DEDENT;
functype: type | VOID;
paramlist: (param COMMA)* param;

// a single parameter, with a default value and/or a type
param: IDNAME type
     | IDNAME ASSIGN term type?
     ;

// a class declaration (pass is allowed, or else must be units
classdef: CLASS TYPENAME COLON NEWLINE INDENT ((unit | NEWLINE)+ | PASS) DEDENT;

// one or more type parameters for generic code
typeparams: LESS (TYPENAME COMMA)+ TYPENAME GREATER;

// a type definition such as a discriminated union (but really anything)
typedef: TYPE TYPENAME typeparams? ASSIGN type NEWLINE;

// any type usable in
type: INT
    | FLOAT
    | STRING
    | BOOL
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

    // a generator type
    | type STAR
    ;

// filling in type params with real types
typeparamfills: LESS (type COMMA)+ type GREATER;

// piece of a discriminated union
unionpart: TYPENAME (OF type)?;

// a list of 1 or more statements (or blank lines)
statements: (statement | NEWLINE)+;

// any valid Chai statement
statement:
         lvalue ASSIGN expression
         | modassign
         | ASSERT expression
         | RETURN expression?
         | YIELD expression
         | PASS
         | CONTINUE
         | BREAK
         | ifstmt
         | FOR IDNAME IN expression COLON NEWLINE INDENT statements DEDENT
         | WHILE expression COLON NEWLINE INDENT statements DEDENT
         | MATCH expression COLON NEWLINE INDENT caseline+ DEDENT
         | unit
         | expression
         ;

// something that can be assigned into -- basically name or list/dict/tuple/set reference, or class reference
lvalue: (SELF DOT)? IDNAME lvaluecont*;
lvaluecont: DOT IDNAME
          | LBRACK expression RBRACK
          ;

// an operator assign type statement like `i += 1`
modassign: lvalue op=(PLUSASSIGN | MINUSASSIGN | TIMESASSIGN | DIVASSIGN | MODASSIGN | INTDIVASSIGN
                    | LSHIFTASSIGN | RSHIFTASSIGN | BITANDASSIGN | BITORASSIGN | BITXORASSIGN) expression;

// a case in a match statement
caseline: CASE destructure COLON NEWLINE INDENT statements DEDENT;

// a thing that can be used as part of a destructured match statement
destructure: IDNAME
           | literal
           | USCORE
           | LPAREN (destructure COMMA)+ destructure RPAREN
           | destructure (CONS destructure)+
           | LBRACK (destructure (COMMA destructure)*)? RBRACK
           | TYPENAME destructure?
           ;

// we don't get else if for free
ifstmt: IF expression COLON NEWLINE INDENT statements DEDENT elifclause* elseclause?;
elifclause: ELIF expression COLON NEWLINE INDENT statements DEDENT;
elseclause: ELSE COLON NEWLINE INDENT statements DEDENT;

// a function call
functioncall: term LPAREN arglist? RPAREN;

// the supplied argument list, containing 0 or more expression, separated with commas
arglist: (argument COMMA)* argument;

// a single argument, maybe a keyword one
argument: expression
        | IDNAME ASSIGN expression
        ;

// any valid Chai expression
expression: expression DOT IDNAME
          | expression DOT functioncall
          | <assoc=right> expression POWER expression
          | op=(COMPLEMENT | PLUS | MINUS) expression
          | <assoc=right> expression CONS expression
          | expression op=(STAR | DIVIDE | INTDIV | MODULUS) expression
          | expression op=(PLUS | MINUS) expression
          | expression op=(LSHIFT | RSHIFT) expression
          | expression BITAND expression
          | expression BITXOR expression
          | expression BAR expression
          | expression op=(LESS | GREATER | LESSEQ | GREATEREQ | EQUALS | NOTEQUALS) expression
          | expression IN expression
          | expression NOT IN expression
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
    // list index like nums[i]
    | term LBRACK expression RBRACK

    // self as used in classes
    | SELF

    // list slice
    | term LBRACK expression? COLON expression? RBRACK

    // sth in parens
    | LPAREN expression RPAREN

    // a list literal like [3, 4, 5]
    | LBRACK (expression (COMMA expression)*)? RBRACK
    
    // a list range like [1 .. 10] [1, 3 .. 10] [1 .. ] or [10, 20 .. ]
    | LBRACK expression (COMMA expression)? ELIPSIS expression? RBRACK

    // a list comprehension
    | LBRACK expression FOR IDNAME IN expression (IF expression)? RBRACK

    // tuple literal like (3, 4, 5) (we don't allow 0 or 1 length tuples)
    | LPAREN (expression COMMA)+ expression RPAREN

    // a set literal
    | LBRACE (expression (COMMA expression)*)? RBRACE

    // a dictionary literal
    | LBRACE (dictentry (COMMA dictentry)*) RBRACE
    | LBRACE COLON RBRACE
    
    // a discriminated union
    | TYPENAME expression?

    // a class reference
    | SELF

    // an actual value from the lexer
    | literal
    ;

dictentry: expression COLON expression;

// any straight up value from the lexer
literal: INTVAL
       | FLOATVAL
       | STRINGVAL
       | TRUE
       | FALSE
       ;

