parser grammar ChaiParser;

options {
    tokenVocab = ChaiLexer;
}

// the program is a list of toplevel constructs (or blank lines)
program: (toplevel | NEWLINE)* EOF;

// things that can exist outside of anything
toplevel: functiondef
        | typedef
        ;

// a function definition line
functiondef: DEF IDNAME typeparams? LPAREN paramlist? RPAREN type? COLON NEWLINE INDENT statements DEDENT;

// a list of 0 or more formal parameters to a function
paramlist: (param COMMA)* param;

// a single parameter, with a default value and/or a type
param: IDNAME type                  # namedParam
     | IDNAME ASSIGN term type?     # defaultParam
     ;

// a type definition such as a discriminated union (but really anything)
typedef: TYPE TYPENAME typeparams? ASSIGN type;

// one or more type parameters for generic code
typeparams: LESS (TYPENAME COMMA)* TYPENAME GREATER;

// any data type
type: INT                           # intType
    | FLOAT                         # floatType
    | STRING                        # stringType
    | BOOL                          # boolType
    | VOID                          # voidType
    | TYPENAME typeparamfills?      # namedType

    // a tuple type
    | LPAREN (type COMMA)+ type RPAREN  # tupleType

    // a list type
    | LBRACK type RBRACK                # listType

    // a dict type
    | LBRACE type COLON type RBRACE     # dictType
    
    // a set type
    | LBRACE type RBRACE                # setType

    // discriminated union
    | unionpart BAR unionpart (BAR unionpart)*  # unionType
    
    // a function type
    | type ARROW type                           # functionType
    ;

// filling in type params with real types
typeparamfills: LESS (type COMMA)* type GREATER;
    
// piece of a discriminated union
unionpart: TYPENAME (OF type)?;

// a list of 1 or more statements (or blank lines)
statements: (statement | NEWLINE)+;

// any valid Chai statement
statement: functioncall                                     # funcallStatement
         | lvalue ASSIGN expression                         # assignStatement
         | (VAR | LET) IDNAME type? ASSIGN expression       # varStatement
         | modassign                                        # modassignStatement
         | ASSERT expression                                # assertStatement
         | RETURN expression?                               # returnStatement
         | ifstmt                                           # ifstatement
         | PASS                                             # passStatement
         | CONTINUE                                         # continueStatement
         | BREAK                                            # breakStatement
         | FOR IDNAME IN expression COLON NEWLINE INDENT statements DEDENT  # forStatement
         | WHILE expression COLON NEWLINE INDENT statements DEDENT          # whileStatement
         | MATCH expression COLON NEWLINE INDENT caseline+ DEDENT           # matchStatment
         | toplevel                                                         # toplevelStatement
         ;

// something that can be assigned into -- basically name or list/dict/tuple/set reference
lvalue: IDNAME                              # justID
      | lvalue LBRACK expression RBRACK     # nestedLvalue
      ;

// an operator assign type statement like `i += 1`
modassign: lvalue op=(PLUSASSIGN | MINUSASSIGN | TIMESASSIGN | DIVASSIGN | MODASSIGN | INTDIVASSIGN
                    | LSHIFTASSIGN | RSHIFTASSIGN | BITANDASSIGN | BITORASSIGN | BITXORASSIGN)
                 expression;

// a case in a match statement
caseline: CASE destructure COLON NEWLINE INDENT statements DEDENT;

// a thing that can be used as part of a destructured match statement
destructure: IDNAME                                             # idDestr
           | literal                                            # literalDestr
           | USCORE                                             # uscoreDestr
           | LPAREN (destructure COMMA)+ destructure RPAREN     # tupleDestr
           | destructure (CONS destructure)+                    # consDestr
           | LBRACK (destructure COMMA)* RBRACK                 # listDestr
           | TYPENAME destructure?                              # unionDestr
           ;

// we don't get else if for free
ifstmt: IF expression COLON NEWLINE INDENT statements DEDENT elifclause* elseclause?;
elifclause: ELIF expression COLON NEWLINE INDENT statements DEDENT;
elseclause: ELSE COLON NEWLINE INDENT statements DEDENT;

// a function call TODO make this a term, so we can call functions through data structures
functioncall: IDNAME LPAREN arglist? RPAREN;

// the supplied argument list, containing 0 or more expression, separated with commas
arglist: (argument COMMA)* argument;

// a single argument, maybe a keyword one
argument: expression
        | IDNAME ASSIGN expression
        ;

// any valid Chai expression
expression: <assoc=right> expression POWER expression                       # powerExpression
          | op=(COMPLEMENT | PLUS | MINUS) expression                       # unaryExpression
          | <assoc=right> expression CONS expression                        # consExpression
          | expression op=(TIMES | DIVIDE | INTDIV | MODULUS) expression    # timesdivExpression
          | expression op=(PLUS | MINUS) expression                         # plusMinusExpression
          | expression op=(LSHIFT | RSHIFT) expression                      # shiftExpression
          | expression BITAND expression                                    # bitandExpression
          | expression BITXOR expression                                    # bitxorExpression
          | expression BAR expression                                       # bitorExpression
          | expression op=(LESS | GREATER | LESSEQ | GREATEREQ | EQUALS | NOTEQUALS) expression     # compareExpression
          | expression IN expression                                        # inExpression
          | expression NOT IN expression                                    # notinExpression
          | NOT expression                                                  # notExpression
          | expression AND expression                                       # andExpression
          | expression OR expression                                        # orExpression
          | expression IF expression ELSE expression                        # ifelseExpression
          | lambda                                                          # lambdaExpression
          | functioncall                                                    # funcallExpression
          | term                                                            # termExpression
          ;


// a lambda function
lambda: LAMBDA lambdaParams? COLON expression;
lambdaParams: (IDNAME type COMMA)* IDNAME type;

// a term is an individual piece of an expression
term: IDNAME                                                        # idTerm
    // list index like nums[i]
    | term LBRACK expression RBRACK                                 # listIndexTerm

    // list slice
    | term LBRACK expression? COLON expression? RBRACK              # listSliceTerm

    // sth in parens
    | LPAREN expression RPAREN                                      # parensTerm

    // a list literal like [3, 4, 5]
    | LBRACK (expression (COMMA expression)*)? RBRACK               # listLiteralTerm
    
    // a list range
    | LBRACK expression ELIPSIS expression RBRACK                   # listRangeTerm
          
    // a list comprehension
    | LBRACK expression FOR IDNAME IN expression (IF expression)? RBRACK    # listcompTerm

    // tuple literal like (3, 4, 5) (we don't allow 0 or 1 length tuples)
    | LPAREN (expression COMMA)+ expression RPAREN                  # tupleLiteralTerm

    // a set literal
    | LBRACE (expression (COMMA expression)*)? RBRACE               # setLiteralTerm

    // a dictionary literal
    | LBRACE (dictentry (COMMA dictentry)*)? RBRACE                 # dicLiteralTerm
    
    // a discriminated union
    | TYPENAME expression?                                          # unionTerm

    // an actual value from the lexer
    | literal                                                       # simpleLiteralTerm
    ;

dictentry: expression COLON expression;

// any straight up value from the lexer
literal: INTVAL         # intLiteral
       | FLOATVAL       # floatLiteral
       | STRINGVAL      # stringLiteral
       | TRUE           # trueLiteral
       | FALSE          # falseLiteral
       ;


