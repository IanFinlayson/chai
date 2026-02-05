
/* this file incorporates the DentLexer developed by Mike Weaver:
   https://github.com/wevrem/wry
*/

lexer grammar ChaiLexer;

@lexer::members {

	// Initializing `pendingDent` to true means any whitespace at the beginning
	// of the file will trigger an INDENT, which will probably be a syntax error,
	// as it is in Python.
	private boolean pendingDent = true;

	private int indentCount = 0;

	private java.util.LinkedList<Token> tokenQueue = new java.util.LinkedList<>();

	private java.util.Stack<Integer> indentStack = new java.util.Stack<>();

	private Token initialIndentToken = null;

	private int getSavedIndent() { return indentStack.isEmpty() ? 0 : indentStack.peek(); }

	private int INDENT_TOKEN = ChaiParser.INDENT;
	private int DEDENT_TOKEN = ChaiParser.DEDENT;

	private CommonToken createToken(int type, String text, Token next) {
		CommonToken token = new CommonToken(type, text);
		if (null != initialIndentToken) {
			token.setStartIndex(initialIndentToken.getStartIndex());
			token.setLine(initialIndentToken.getLine());
			token.setCharPositionInLine(initialIndentToken.getCharPositionInLine());
			token.setStopIndex(next.getStartIndex()-1);
		}
		return token;
	}

	@Override
	public Token nextToken() {
		// Return tokens from the queue if it is not empty.
		if (!tokenQueue.isEmpty()) { return tokenQueue.poll(); }

		// Grab the next token and if nothing special is needed, simply return it.
		// Initialize `initialIndentToken` if needed.
		Token next = super.nextToken();
		//NOTE: This could be an appropriate spot to count whitespace or deal with
		//NEWLINES, but it is already handled with custom actions down in the
		//lexer rules.
		if (pendingDent && null == initialIndentToken && NEWLINE != next.getType()) { initialIndentToken = next; }
		if (null == next || HIDDEN == next.getChannel() || NEWLINE == next.getType()) { return next; }

		// Handle EOF. In particular, handle an abrupt EOF that comes without an
		// immediately preceding NEWLINE.
		if (next.getType() == EOF) {
			indentCount = 0;
			// EOF outside of `pendingDent` state means input did not have a final
			// NEWLINE before end of file.
			if (!pendingDent) {
				initialIndentToken = next;
				tokenQueue.offer(createToken(NEWLINE, "NEWLINE", next));
			}
		}

		// Before exiting `pendingDent` state queue up proper INDENTS and DEDENTS.
		while (indentCount != getSavedIndent()) {
			if (indentCount > getSavedIndent()) {
				indentStack.push(indentCount);
				tokenQueue.offer(createToken(INDENT, "INDENT" + indentCount, next));
			} else {
				indentStack.pop();
				tokenQueue.offer(createToken(DEDENT, "DEDENT"+getSavedIndent(), next));
			}
		}
		pendingDent = false;
		tokenQueue.offer(next);
		return tokenQueue.poll();
	}

}

/* we allow a \ character to continue a line to the next one */
LINECONT: '\\' NEWLINE WS {
	setChannel(HIDDEN);
};

NEWLINE: ( '\r'? '\n' | '\r' ) {
	if (pendingDent) { setChannel(HIDDEN); }
	pendingDent = true;
	indentCount = 0;
	initialIndentToken = null;
};

WS: [ \t]+ {
	setChannel(HIDDEN);
	if (pendingDent) { indentCount += getText().length(); }
};

INDENT: 'INDENT' { setChannel(HIDDEN); };
DEDENT: 'DEDENT' { setChannel(HIDDEN); };

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
OPEN:       'open';
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

/* symbols */
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

/* we also allow | to do line continuations, for use in
   discriminated unions */
BARCONT: '|' WS? NEWLINE WS {
    setType(BAR);
    setText("|");
};

