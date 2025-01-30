# Chai Lexical Documentation

## Overview

This document describes the lexical features of Chai.  This includes the list
of operators and keywords, rules for variable names, string constants, comments,
number formats, as well as the white-spaced based indentation rules.

## Indentation

Chai, like Python and Haskell, uses indentation to indicate the structure of
code.  The first time indentation is found, the compiler will use the number
of spaces as the size of one indentation level.

Further lines that are indented must be done is a multiple of that prior size.
For example, if the first indentation encountered is 4 spaces, then valid
indent amounts are 0, 4, 8, 12, 16, etc.  An invalid indentation amount is an
error.  Likewise, indenting by more than one level at a time (such as going from
0 to 8 spaces in this instance) is an error.

Lines can be continued using the \ character.  When two lines are continued in
this way, they are treated as one very long line.

Chai also allows the | symbol to be used as a continuation line, which is
helpful with discriminated unions.  For example:

```
type Tree<T> = Leaf |
               Node of (T, Tree<T>, Tree<T>)
```

This allows the parts of a union to be aligned on individual lines.


## Operators and Symbols

Chai includes the following operators and symbols:

| Operator    | Meaning |
| -------- | ------- |
| + | addition, concatenation |
| - | subtraction, unary negate |
| / | division |
| * | multiplication |
| % | modulus |
| ** | exponentiation |
| // | integer division |
| ~ | bitwise complement |
| << | left shift |
| >> | right shift |
| & | bitwise and |
| \| | bitwise or, separation of discriminated union types |
| ^ | bitwise exclusive or |
| < | less than |
| > | greater than |
| <= | less than or equal to |
| >= | greater than or equal to |
| == | equals to |
| != | not equals to |
| : | block beginning, dictionary key/value separator |
| , | parameter separator |
| = | assignment operator |
| ( | left parenthesis |
| ) | right parenthesis |
| [ | left bracket |
| ] | right bracket |
| { | left brace |
| } | right brace |
| . | object member access |
| .. | ellipsis |
| -> | function type separator |
| :: | cons operator |
| _ | wildcard match operator |
| += | add and assign |
| -= | subtract and assign |
| \*= | multiplication and assign |
| /= | division and assign |
| %= | modulus and assign |
| \*\*= | exponentiation and assign |
| //= | integer division and assign |
| <<= | left shift and assign |
| >>= | right shift and assign |
| &= | bitwise and and assign |
| \|= | bitwise or and assign |
| ^= | bitwise exclusive or and assign |

## Keywords

Chai includes the following reserved keywords.  They will
be documented more extensively in syntactical and semantic
documentation, but are summarized here.

| Keyword | Meaning |
| -------- | ------- |
| and | boolean and operator |
| assert | check that a condition is true or halt the program |
| break | exit a loop early |
| case | used in a match statement to denote a possible case |
| class | creates a class structure |
| continue | move on to the next loop iteration |
| def | define a function |
| elif | check a condition after a previous if (and possibly other elifs) |
| else | execute code if a previous if (and possibly elifs) were all false |
| for | begin a for loop construct |
| if | check a condition and execute code if it's true |
| implement | indicates a class has a given trait |
| in | used in a for loop, and also as an operator on collections |
| lambda | define an anonymous function |
| let | create a constant |
| match | compare a value to several possible values or patterns |
| not | a unary boolean operator |
| of | specify the subtype of part of a discriminated union |
| or | boolean or operator |
| pass | take no action (used when a statement is syntactically required) |
| return | return to the caller of a function, possibly with a value |
| self | refers to the object represented by a class |
| trait | creates a type trait |
| type | used to declare new types |
| var | create a variable |
| while | begin a while loop construct |

Chai also includes the following built-in type names.  In Chai, type names
always begin with a capital letter while identifier names for functions
and variables always begin with lower-case.

| Type |
| -------- |
| Int |
| Float |
| String |
| Bool |
| Void |
| True |
| False |


## Identifiers

As mentioned above, identifiers come in two kinds in Chai: type identifiers
begin with capital letters and all others begin with lower-case ones.  This is a
convention in many languages, but is enforced in Chai.

Following the initial letter which begins an identifier name are any number of
letters (of either case), digits, or the underscore character.  Identifiers
cannot begin with digits or underscores.

## Strings

Strings in Chai are delimited by double quotes.  They can contain the following
escape sequences:
 - \\"
 - \\\\
 - \\n
 - \\t

String literals cannot span more than one line.

## Comments

Comments begin with the \# character and extend to the end of the line.


## Number Formats

If a number begins with '0x' it will be treated as a hexadecimal one.  The
digits 0-9 as well as letters A-F (in either case) will be treated as part
of the number.  If it begins with '0b' it will be treated as binary and only
digits 0 and 1 will be treated as part of it.  Numbers beginning with '0o'
are treated as octal and contain digits 0-7.

Otherwise, numbers in Chai begin with a '.' or a digit.  They contain any number
of digits and up to one '.' total.  If there is a '.', it will be treated as
a floating-point value, otherwise an integer.

Numbers can also be written in scientific notation such as 1.4e6, or 89e-4.

