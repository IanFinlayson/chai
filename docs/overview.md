# Chai Programming Language Overview

## Overview

Chai aims to be a high-level, general-purpose programming language.  It combines
the elegant syntax of Python with the powerful type system of languages in the
ML family.  It is a pragmatic language that allows multiple styles of
programming.  Chai supports features including algebraic types, first-class
functions, built-in data structures, an object system and more.

The language is in early development and will likely change as the initial
interpreter for it is developed.


## Basic Syntax

The basic syntax borrows from Python, including significant whitespace.  Unlike
Python, programs begin execution in a main function.  Below is "Hello World!" in
Chai:

```
# a simple program
def main():
    print("Hello World!")
```

Chai includes the basic Python control structures including loops and if/else
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

Chai will automatically infer the types of local variables based on
what value they are assigned.  We can also explicitly give type to
these variables like so:

```
var name String
var num Float = 0
```

In Chai all types begin with capital letters while variables, constants and
functions always begin with lower-case letters.  This is a convention in some
languages, such as Java, but is enforced in Chai.

Type declarations are required for the parameters and return value of functions.
This following example shows how functions can be defined in Chai:

```
def fact(num Int) Int:
    if num == 0:
        return 1
    else:
        return num * fact(num - 1)

def main():
    for i in [1 .. 10]:
        print(i, "! = ", fact(i))
```

By providing local type inference but requiring declarations on function
headers, Chai attempts to strike a balance between clarity, reliability
and ease of use.


## Lists

Chai supports lists which are implemented with arrays and not linked lists (just
as Python lists are).  We can create lists using the typical bracket syntax:

```
nums = [10, 20, 30, 40, 50]
```

Chai also has a `..` operator which creates a range of numbers:

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


## List Comprehensions

Chai supports list comprehensions to initialize lists as well.  These consist
of an expression using a variable which is generated by the following for
expression.  They can optionally include an if expression to filter out
options.

For example:

```
let evens = [x for x in [0 .. 100] if x % 2 == 0]
    
let squares  = [x ** 2 for x in [1 .. 10]]
```




## Tuples

Tuples on the other hand are heterogeneous --  you can create a tuple containing
multiple different types of data.  But also unlike a list, a tuple is a of a fixed
size.  Here we make a tuple of size three:

```
userInfo = ("Joe", 100, true)
```

Chai allows for the definition of new named types using the `type` keyword.  For
instance, here we define a specific type of tuple:

```
type Date = (Int, Int, Int)

def getBirthDay() Date:
    return (10, 26, 1984)
```

Unlike Python, tuples in Chai require parentheses in all contexts.  (Python has
them being optional sometimes but not others).  In Chai, there is no such thing
as a tuple with 0 or 1 elements.  There is no reason to create such a thing.


## Dictionaries

Dictionaries in Chai work much as they do in Python.  The following code creates
a dictionary:

```
let sentiments = {"good": 2, "awesome": 4, "bad": -2, "awful": -3}
```

We can then add or modify dictionary elements with this syntax:

```
sentiments["great"] = 3
```

And read values using the same syntax:
```
print(sentiments["awesome"])
```

If we want to make an empty dictionary, we do so like this:

```
let phonebook {String:String} = {:}
```

This is different from Python, where {} indicates an empty dictionary, and
there is no syntax for an empty set at all.


## Sets

Sets are also introduced with curly-braces:

```
let villains = {"Darth Vader", "Voldemort", "Sauron"}
```

An empty set is introduced with `{}`.  We can use the `in` operator to test if
something is in a set:

```
if "Sauron" in villains:
    print("Author is a LOTR fan!")
```

Set elements are unordered and do not allow duplicate values.


## Unions

Chai allows the creation of discriminated unions, which are
data types where the possible values are listed out.  This
can be used similar to an enumeration:

```
type Direction = North | South | East | West

```

Chai also allows data to be associated with each of the possible options.
For example, this type creates a binary tree structure where every tree
is either an empty leaf node, or an interior node storing a tuple with the
data value, and left and right children:

```
type Tree = Leaf | Node of (Int, Tree, Tree)
```

We can use the `match` construct to match the different options of
a union.  For example, the following function inserts into a Tree
structure given the above definition:

```
# insert a value into a tree
def insert(tree Tree, value T) Tree:
    match tree:
        case Leaf:
            return Node (value, Leaf, Leaf)
        case Node (root, left, right):
            if value < root:
                return Node (root, insert(left, value), right)
            else:
                return Node (root, left, insert(right, value))
```

## More Matches!

The match statement can also do pattern matching on tuples and lists.
For example, we can use this to match values from a tuple:

```
def printInfo(info (String, Int)):
    match info:
        case ("", _):
            print("No name given")
        case (_, 0):
            print("No age given")
        case (name, age):
            print(name, "is", age, "years old!")
```

The match statement _destructures_ its arguments.  Here the third
case splits the tuple values into named values.  The first two match
for specific values, with the underscore matching values we are not
interested in capturing.

This same syntax can be used with lists by replacing the parentheses with
square brackets.  Lists can also be matched by pulling values out of the front
using the cons operator:

```
def count(nums [Int], accum Int) Int:
    match nums:
        case []:
            return accum
        case next :: rest:
            return count(rest, accum + next)
```

These destructuring matches can be nested of course:

```
def printInfo(info [(String, Int)]):
    match info:
        case []:
            print("Done!")
        case (name, 0) :: rest:
            print(name, "is missing their age!")
            printInfo(rest)
        case ("Hammy", _) :: rest:
            print("If your name is Hammy, I don't care your age!")
            printInfo(rest)
        case (name, age) :: rest:
            print(name, "is", age, "years old.")
            printInfo(rest)
```


## Functions

Functions in Chai are first class values and can be passed into functions as
arguments, returned back from functions, put into data structures, etc.

Chai also supports partial function application, or "currying", in which
calling a function with less than all of its arguments results in a new
function with the provided arguments "baked in".  For example:

```
def add(a Int, b Int) Int:
    return a + b


def main():
    # make a function with 7 pre-filled in for argument 1
    let add7 = add(7)

    # should print 12
    print(add7(5))
```

This can be helpful, especially when used with higher-order functions.  For
example, we can use the `reduce` and `map` functions (which exist in Python as
well) to form new functions by pre-supplying the function argument:

```
let sum = reduce(add)
let capitalize = map(toupper)
```


## Lambda Functions

Like Python, Chai supports lambda functions, which are anonymous functions
created inline.  Lambdas can be directly applied or passed into functions,
while regular function definitions cannot be.

Here is an example of a simple lambda:

```
def main():
    let func = lambda x Int: x + 1
    print(func(5))
```

This should print 6.  Lambda's are often used with higher-order functions such
as map and reduce.  Here is how we can use reduce to compute a factorial, with
a lambda function:

```
def main():
    let x = Int(input())
    let factorial = reduce(lambda x Int, y Int: x * y, [1 .. x])
```

This lambda demonstrates the syntax for one with multiple parameters.  Like
Python, lambda functions cannot span more than one line of code.  The return
type of a lambda is inferred from the expression that is produced.


## Type Parameters

Chai supports type parameters on functions, type declarations, and classes.
This allows code to be parameterized on a data type which will be supplied
on call, leading to generic code.

For example, the following simple sort function works for lists of any type:

```
def sort<T>(list [T]):
    var sorted = false
    while not sorted:
        sorted = true
        for i in [0 .. len(list) - 2]:
            if list[i] > list[i + 1]:
                var temp = list[i]
                list[i] = list[i + 1]
                list[i + 1] = temp
                sorted = false
```

Here, `T` is a type parameter.  Type parameter names begin with capital letters
(as all type names do), and are enclosed within angle brackets.  If there are
more than one, they are separated with commas.  Inside the parameterized code,
the type parameter can be used where any other type can (e.g. parameter and
return values, local variables, etc.)

This also allows for the creation of higher-order functions such as map, which
can be written in Chai like this:

```
def map<From, To>(f From -> To, list [From]) [To]:
    var result [To] = []
    for item in list:
        result += f(item)
    return result

def main():
    let perfect_squares = map(lambda x: x * x, [1 .. 10])
```

Here, we have two type parameters, called From and To.  This map function takes
a list and a function.  It applies the function to every element of the list,
giving us a new list with the results.  The From type is the type of the
elements of the list being passed in.  The To type is the type of the resulting
list.  Here the function f has type `From -> To` which is a function which
takes one From type parameter and returns one To type value.

We do not need to supply the types of map when calling it, since the compiler
infers them from the parameters.  If we wanted to, we could though:

```
let perfect_squares = map<Int, Int>(lambda x: x * x, [1 .. 10])
```

## None

In Chai, `None` works differently from how it does in Python.  Taking a cue
from languages like Haskell, we include an Option type which is declared like
this:

```
type Option<T> = None | Some of T
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

A function which is not declared to return an Option can not return None.  This
limitation makes code more reliable (See Tony Hoare's "Billion Dollar Mistake").

