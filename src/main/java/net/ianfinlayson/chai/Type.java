package net.ianfinlayson.chai;


public class Type {

}











/*

Int
Float
String
Bool

List (one subtype)
Set (one subtype)
Generator (one subtype)
Dictionary (two subtypes)
Tuple (any number of subtypes)

Function (first subtype is return val, rest are args)
Class (should store reference to the ChaiClass object it refers to...)

*/


// TODO how to do unions

// TODO how to do type parameters


/*
## Unions

Chai allows the creation of discriminated unions, which are
data types where the possible values are listed out.  This
can be used similar to an enumeration:

type Direction = North | South | East | West

Chai also allows data to be associated with each of the possible options.
For example, this type creates a binary tree structure where every tree
is either an empty leaf node, or an interior node storing a tuple with the
data value, and left and right children:

type Tree = Leaf | Node of (Int, Tree, Tree)

We can use the `match` construct to match the different options of
a union.  For example, the following function inserts into a Tree
structure given the above definition:

# insert a value into a tree
def insert(tree Tree, value Int) Tree:
    match tree:
        case Leaf:
            return Node (value, Leaf, Leaf)
        case Node (root, left, right):
            if value < root:
                return Node (root, insert(left, value), right)
            else:
                return Node (root, left, insert(right, value))
*/







/*
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
type.  For example, the following function returns an Int or None:

```
def slope(x0 Int, y0 Int, x1 Int, y1 Int) Option<Int>:
    if x0 != x1:
        return Some (y1 - y0) / (x1 - x0)
    else:
        return None
```

A function which is not declared to return an Option can not return None.  This
limitation makes code more reliable (See Tony Hoare's "Billion Dollar Mistake").
*/



