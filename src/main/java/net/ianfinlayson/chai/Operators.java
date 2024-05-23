package net.ianfinlayson.chai;

import java.util.ArrayList;

// this file contains methods for all operators across chai values

public class Operators {

    public static Value plus(Value lhs, Value rhs) {
        if (lhs.getType() == Type.STRING && rhs.getType() == Type.STRING) {
            // string concatenation
            return new Value(lhs.toString() + rhs.toString());
        } else if (lhs.getType() == Type.LIST && rhs.getType() == Type.LIST) {
            // array concatenation
            ArrayList<Value> combined = new ArrayList<>();

            for (Value v : lhs.toList()) {
                combined.add(v);
            }
            for (Value v : rhs.toList()) {
                combined.add(v);
            }
            return new Value(combined, false);
        } else if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            // integer math
            return new Value(lhs.toInt() + rhs.toInt());
        } else if (lhs.numberType() && rhs.numberType()) {
            // float math
            return new Value(lhs.toFloat() + rhs.toFloat());
        } else {
            throw new RuntimeException("Illegal types in + operation");
        }
    }

    public static Value minus(Value lhs, Value rhs) {
        if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            // integer math
            return new Value(lhs.toInt() - rhs.toInt());
        } else if (lhs.numberType() && rhs.numberType()) {
            // float math
            return new Value(lhs.toFloat() - rhs.toFloat());
        } else {
            throw new RuntimeException("Illegal types in - operation");
        }
    }

    public static Value times(Value lhs, Value rhs) {
        // "hi " * 3 gives us "hi hi hi "
        if (lhs.getType() == Type.STRING && rhs.getType() == Type.INT) {
            String result = "";
            for (int i = 0; i < rhs.toInt(); i++) {
                result += lhs.toString();
            }
            return new Value(result);
        } else if (lhs.getType() == Type.INT && rhs.getType() == Type.STRING) {
            return times(rhs, lhs);
        }

        // [1,2] * 2 gives us [1,2,1,2]
        else if (lhs.getType() == Type.LIST && rhs.getType() == Type.INT) {
            ArrayList<Value> result = new ArrayList<>();

            ArrayList<Value> from = lhs.toList();
            for (int i = 0; i < rhs.toInt(); i++) {
                for (int j = 0; j < from.size(); j++) {
                    result.add(from.get(j));
                }
            }
            return new Value(result, false);
        } else if (lhs.getType() == Type.INT && rhs.getType() == Type.LIST) {
            return times(rhs, lhs);
        }

        // otherwise do number stuff
        else if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            // integer math
            return new Value(lhs.toInt() * rhs.toInt());
        } else if (lhs.numberType() && rhs.numberType()) {
            // float math
            return new Value(lhs.toFloat() * rhs.toFloat());
        } else {
            throw new RuntimeException("Illegal types in * operation");
        }
    }

    public static Value divide(Value lhs, Value rhs) {
        if (lhs.numberType() && rhs.numberType()) {
            // float math
            return new Value(lhs.toFloat() / rhs.toFloat());
        } else {
            throw new RuntimeException("Illegal types in / operation");
        }
    }

    public static Value intdivide(Value lhs, Value rhs) {
        return new Value(lhs.toInt() / rhs.toInt());
    }


    public static Value modulo(Value lhs, Value rhs) {
        if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            // integer math
            return new Value(lhs.toInt() % rhs.toInt());
        } else if (lhs.numberType() && rhs.numberType()) {
            // float math
            return new Value(lhs.toFloat() % rhs.toFloat());
        } else {
            throw new RuntimeException("Illegal types in % operation");
        }
    }
    
    public static boolean in(Value target, Value collection) {
        switch (collection.getType()) {
            case STRING:
                String needle = target.toString();
                String haystack = collection.toString();
                return haystack.indexOf(needle) != -1;
            case LIST:
            case TUPLE:
                for (Value v : collection.toList()) {
                    if (Operators.equals(v, target)) {
                        return true;
                    }
                }
                return false;
            case SET:
                return collection.toSet().contains(target);
            case DICT:
                return collection.toDict().get(target) != null;
        }

        throw new RuntimeException("invalid type passed to inCollection");
    }

    @SuppressWarnings("unchecked")
    public static boolean equals(Value lhs, Value rhs) {
        if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            return lhs.toInt() == rhs.toInt();
        }

        else if (lhs.numberType() && rhs.numberType()) {
            return lhs.toFloat() == rhs.toFloat();
        }

        else if (lhs.getType() == Type.BOOL && rhs.getType() == Type.BOOL) {
            return lhs.toBool() == rhs.toBool();
        }

        else if (lhs.getType() == Type.STRING && rhs.getType() == Type.STRING) {
            return lhs.toString().equals(rhs.toString());
        }

        else if ((lhs.getType() == Type.LIST && rhs.getType() == Type.LIST) &&
                 (lhs.getType() == Type.TUPLE && rhs.getType() == Type.TUPLE)) {
            ArrayList<Value> left = lhs.toList();
            ArrayList<Value> right = rhs.toList();

            if (left.size() != right.size()) {
                return false;
            }

            for (int i = 0; i < left.size(); i++) {
                if (!left.get(i).equals(right.get(i))) {
                    return false;
                }
            }

            return true;
        }
        else {
            throw new RuntimeException("Invalid operands to comparison operator.");
        }
    }

    public static boolean less(Value lhs, Value rhs) {
        if (lhs.getType() == Type.INT && rhs.getType() == Type.INT)
            return lhs.toInt() < rhs.toInt();

        else if (lhs.numberType() && rhs.numberType())
            return lhs.toFloat() < rhs.toFloat();

        else if (lhs.getType() == Type.STRING && rhs.getType() == Type.STRING) {
            return lhs.toString().compareTo(rhs.toString()) < 0;
        }

        // TODO, python allows for these comparions with lists
        // it goes element by element and when one is less, it returns
        // should Chai also support this??

        else {
            throw new RuntimeException("Invalid operands to comparison operator.");
        }

    }

    // why does java have no int pow function??
    private static int intpow(int base, int exponent) {
        if (exponent == 0) return 1;

        int sub = intpow(base, exponent >> 1);
        if ((exponent & 1) == 0) {
            return sub * sub;
        } else {
            return sub * sub * base;
        }
    }

    public static Value pow(Value lhs, Value rhs) {
        if (lhs.getType() == Type.INT && rhs.getType() == Type.INT) {
            return new Value(intpow(lhs.toInt(), rhs.toInt()));
        } else {
            return new Value(Math.pow(lhs.toFloat(), rhs.toFloat()));
        }
    }

    public static Value lshift(Value lhs, Value rhs) {
        return new Value(lhs.toInt() << rhs.toInt());
    }

    public static Value rshift(Value lhs, Value rhs) {
        return new Value(lhs.toInt() >> rhs.toInt());
    }

    public static Value bitand(Value lhs, Value rhs) {
        return new Value(lhs.toInt() & rhs.toInt());
    }

    public static Value bitor(Value lhs, Value rhs) {
        return new Value(lhs.toInt() | rhs.toInt());
    }

    public static Value bitxor(Value lhs, Value rhs) {
        return new Value(lhs.toInt() ^ rhs.toInt());
    }
}

