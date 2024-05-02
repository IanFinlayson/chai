package net.ianfinlayson.chai;

import java.util.ArrayList;

public class Value {
    private Type type;
    private Object value;

    public Value(int intVal) {
        type = Type.INT;
        value = Integer.valueOf(intVal);
    }

    public Value(double floatVal) {
        type = Type.FLOAT;
        value = Double.valueOf(floatVal);
    }

    public Value(boolean boolVal) {
        type = Type.BOOL;
        value = Boolean.valueOf(boolVal);
    }

    public Value(String stringVal) {
        type = Type.STRING;
        value = stringVal;
    }

    public Value(ArrayList<Value> array) {
        type = Type.LIST;
        value = array;
    }

    public Object getRaw() {
        return value;
    }

    public Type getType() {
        return type;
    }

    // used to simplify logic of math functions
    private boolean numberType() {
        return type == Type.INT || type == Type.FLOAT;
    }

    public Value plus(Value other) {
        if (type == Type.STRING && other.type == Type.STRING) {
            // string concatenation
            return new Value(toString() + other.toString());
        } else if (type == Type.LIST && other.type == Type.LIST) {
            // array concatenation
            ArrayList<Value> combined = new ArrayList<>();

            for (Value v : toList()) {
                combined.add(v);
            }
            for (Value v : other.toList()) {
                combined.add(v);
            }
            return new Value(combined);
        } else if (type == Type.INT && other.type == Type.INT) {
            // integer math
            return new Value(toInt() + other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new Value(toFloat() + other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in + operation");
        }
    }

    public Value minus(Value other) {
        if (type == Type.INT && other.type == Type.INT) {
            // integer math
            return new Value(toInt() - other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new Value(toFloat() - other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in - operation");
        }
    }

    public Value times(Value other) {
        // "hi " * 3 gives us "hi hi hi "
        if (type == Type.STRING && other.type == Type.INT) {
            String result = "";
            for (int i = 0; i < other.toInt(); i++) {
                result += toString();
            }
            return new Value(result);
        } else if (type == Type.INT && other.type == Type.STRING) {
            return other.times(this);
        }

        // [1,2] * 2 gives us [1,2,1,2]
        else if (type == Type.LIST && other.type == Type.INT) {
            ArrayList<Value> result = new ArrayList<>();

            ArrayList<Value> from = toList();
            for (int i = 0; i < other.toInt(); i++) {
                for (int j = 0; j < from.size(); j++) {
                    result.add(from.get(j));
                }
            }
            return new Value(result);
        } else if (type == Type.INT && other.type == Type.LIST) {
            return other.times(this);
        }

        // otherwise do number stuff
        else if (type == Type.INT && other.type == Type.INT) {
            // integer math
            return new Value(toInt() * other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new Value(toFloat() * other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in * operation");
        }
    }

    public Value divide(Value other) {
        if (numberType() && other.numberType()) {
            // float math
            return new Value(toFloat() / other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in / operation");
        }
    }
    
    public Value modulo(Value other) {
        if (type == Type.INT && other.type == Type.INT) {
            // integer math
            return new Value(toInt() % other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new Value(toFloat() % other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in % operation");
        }
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Value other) {
        if (type == Type.INT && other.type == Type.INT)
            return toInt() == other.toInt();
        
        else if (numberType() && other.numberType())
            return toFloat() == other.toFloat();
            
        else if (type == Type.BOOL && other.type == Type.BOOL)
            return toBool() == other.toBool();

        else if (type == Type.STRING && other.type == Type.STRING)
            return toString().equals(other.toString());

        else if (type == Type.LIST && other.type == Type.LIST) {
            ArrayList<Value> lhs = (ArrayList<Value>) value;
            ArrayList<Value> rhs = (ArrayList<Value>) other.value;

            if (lhs.size() != rhs.size()) {
                return false;
            }

            for (int i = 0; i < lhs.size(); i++) {
                if (!lhs.get(i).equals(rhs.get(i))) {
                    return false;
                }
            }
            
            return true;
        } else {
            throw new TypeMismatchException("Invalid operands to comparison operator.");
        }
    }

    public boolean less(Value other) {
        if (type == Type.INT && other.type == Type.INT)
            return toInt() < other.toInt();
        
        else if (numberType() && other.numberType())
            return toFloat() < other.toFloat();
            
        else if (type == Type.STRING && other.type == Type.STRING) {
            return toString().compareTo(other.toString()) < 0;
        }
        
        // TODO, python allows for these comparions with lists
        // it goes element by element and when one is less, it returns
        // should Chai also support this??
        
        else {
            throw new TypeMismatchException("Invalid operands to comparison operator.");
        }

    }
    
    public Value pow(Value other) {
        // TODO should we do integer ones separately?  actually we should consider bignum anyway...
        if (numberType() && other.numberType()) {
            return new Value(Math.pow(toFloat(), other.toFloat()));
        } else {
            throw new RuntimeException("Illegal types in ** operation");
        }

    }

    // we need to print "" around an strings that may be in this list/set/dict
    // the argument is this basically -- are we in a nested structure that
    // will require that should we get down to a stirng?
    @SuppressWarnings("unchecked")
    public String toString(boolean printQuotes) {
        switch (type) {
            case INT:
                return ((Integer) value).toString();
            case FLOAT:
                return ((Double) value).toString();
            case BOOL:
                if ((Boolean) value) {
                    return "True";
                } else {
                    return "False";
                }
            case STRING:
                if (printQuotes) {
                    return "\"" + ((String) value) + "\"";
                } else {
                    return ((String) value);
                }

            case LIST:
                String result = "[";
                boolean first = true;

                for (Value val : ((ArrayList<Value>) value)) {
                    if (first) {
                        first = false;
                    } else {
                        result += ", ";
                    }

                    result += val.toString(true);
                }

                return result + "]";
        }

        throw new RuntimeException("Unhandled type in swtich/case");
    }

    @Override
    public String toString() {
        // by default we do not print nested quotations on strings
        return toString(false);
    }

    public int toInt() {
        switch (type) {
            case INT:
                return ((Integer) value).intValue();
            case FLOAT:
                return ((Double) value).intValue();
            case BOOL:
                throw new TypeMismatchException("Cannot convert boolean to integer");
            case STRING:
                throw new TypeMismatchException("Cannot convert string to integer");
            case LIST:
                throw new TypeMismatchException("Cannot convert array to integer");
        }

        throw new RuntimeException("Unhandled type in swtich/case");
    }

    public double toFloat() {
        switch (type) {
            case INT:
                return ((Integer) value).doubleValue();
            case FLOAT:
                return ((Double) value).doubleValue();
            case BOOL:
                throw new TypeMismatchException("Cannot convert boolean to float");
            case STRING:
                throw new TypeMismatchException("Cannot convert string to float");
            case LIST:
                throw new TypeMismatchException("Cannot convert array to float");
        }

        throw new RuntimeException("Unhandled type in swtich/case");
    }

    public boolean toBool() {
        switch (type) {
            case INT:
                throw new TypeMismatchException("Cannot convert integer to boolean");
            case FLOAT:
                throw new TypeMismatchException("Cannot convert float to boolean");
            case BOOL:
                return ((Boolean) value).booleanValue();
            case STRING:
                throw new TypeMismatchException("Cannot convert string to boolean");
            case LIST:
                throw new TypeMismatchException("Cannot convert array to boolean");
        }

        throw new RuntimeException("Unhandled type in swtich/case");
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Value> toList() {
        if (type == Type.LIST) {
            return (ArrayList<Value>) value;
        }

        throw new TypeMismatchException("Cannot convert type to array");
    }
}


