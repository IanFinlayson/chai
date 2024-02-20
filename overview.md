# Tao Programming Language Overview

## Overview

Tao aims to be a high-level, general-purpose programming language.  It combines
the elegant syntax of Python with the powerful type system of languages in the
ML family.  Tao supports features including algebraic types, first-class
functions, powerful built-in data structures and more.

The language is in early development and will likely change as the initial
interpreter for it is developed.


## Basic Syntax

The basic syntax borrows from Python, including significant whitespace.  Unlike
Python, programs begin execution in a main function.  Below is "Hello World!" in
Tao:

```
# a simple program
def main():
    print("Hello World!")
```

Tao includes the basic Python control structures including loops and if/else
statements.  Below is a program to simulate the Collatz conjecture.

```
def main():
    var number = Int(input("Starting number: "))
    
    while number != 1:
        print(number)
        if number % 2 == 0:
            number /= 2
        else:
            number = number * 3 + 1
    
    print("Done")
```

Note that variables are introduced with the `var` keyword.  This prevents the
creation of variables accidentally by misspelling an existing variable name,
as can happen in Python.

We can also use `let` instead to introduce constant values:

```
let size = 100   # cannot change
var name = "Joe" # can
```

## Type Declarations

Tao will automatically infer the types of local variables based on
what value they are assigned.  We can also explicitly give type to
these variables like so:

```
var name String
var num Float = 0
```

In Tao all types begin with capital letters while variables, constants and
functions always begin with lower-case letters.  This is a convention in some
languages, but is enforced in Tao.

Type declarations are required for the parameters and return value of functions.
This following example shows how functions can be defined in Tao:

```
def fact(num int) int:
    if num == 0:
        return 1
    else:
        return num * fact(num - 1)

def main():
    for i in [1 .. 10]:
        print(i, "! = ", fact(i))
```

By providing local type inference but requiring declarations on function
headers, Tao attempts to strike a balance between clarity, reliability
and ease of use.


## Lists

Tao supports lists which are implemented with arrays and not linked lists (just
as Python lists are).  We can create lists using the typical bracket syntax:

```
nums = [10, 20, 30, 40, 50]
```

Tao also has a `..` operator which creates a range of numbers:

```
nums = [1 .. 100]
```

Like Python, for loops can be used to iterate over elements in a list:

```
for num in nums:
    print(num)
```

Unlike Python, lists are homogeneous -- all elements must be of the same
data type.


## Tuples

Tuples on the other hand are heterogeneous --  you can create a tuple containing
multiple different types of data.  But also unlike a list, a tuple is a of a fixed
size.  Here we make a tuple of size three:

```
userInfo = ("Joe", 100, true)
```

Tao allows for the definition of new named types using the `type` keyword.  For
instance, here we define a specific type of tuple:

```
type Date = (Int, Int, Int)

def getBirthDay() Date:
    return (10, 26, 1984)
```

Unlike Python, tuples in Tao require parentheses in all contexts.  (Python has
them being optional sometimes but not others).  In Tao, there is no such thing
as a tuple with 0 or 1 elements.  There is no reason to create such a thing.


## Dictionaries

Dictionaries in Tao work much as they do in Python.  The following code creates
a dictionary:

```
sentiments = {"good": 2, "awesome": 4, "bad": -2, "awful": -3}
```

We can then add or modify dictionary elements with this syntax:

```
sentiments["great"] = 3
```

And read values using the same syntax:
```
print(sentiments["awesome"])
```

## Sets



## Unions

```
type Tree = Leaf
          | Node of (Int, Tree, Tree)
```





## Functions

first class functions
currying
closures


## Type Parameters


## None

In Tao, `None` works differently from how it does in Python.  Taking a cue
from languages like Haskell, we include an Option type which is declared like
this:

```
type Option<T> = None
               | Some of T
```

Any function that may not return a good value is declared as returning an Option
type.  For example, the following function returns a String or None:

```
def slope(x0 Int, y0 Int, x1 Int, y1 Int) Option<Int>:
    if x0 != x1:
        return Some (y1 - y0) / (x1 - x0)
    else:
        return None
```




## Classes


## Libraries



