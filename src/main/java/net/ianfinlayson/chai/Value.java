package net.ianfinlayson.chai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
        // replace the escape sequences! this was annoying!
        stringVal = stringVal.replaceAll("\\\\n", "\n");
        stringVal = stringVal.replaceAll("\\\\t", "\t");
        stringVal = stringVal.replaceAll("\\\\\"", "\"");

        type = Type.STRING;
        value = stringVal;
    }

    public Value(ArrayList<Value> array, boolean tuple) {
        type = tuple ? Type.TUPLE : Type.LIST;
        value = array;
    }

    public Value(HashMap<Value, Value> map) {
        type = Type.DICT;
        value = map;
    }

    public Value(HashSet<Value> set) {
        type = Type.SET;
        value = set;
    }

    public Object getRaw() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        // required to use these as keys for dictionaries
        // this seems like it'll work pretty well
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        Value otherv = (Value) other;

        if (this.type != otherv.type) return false;
        switch (this.type) {
            case INT:
                return toInt() == otherv.toInt();
            case FLOAT:
                return toFloat() == otherv.toFloat();
            case BOOL:
                return toBool() == otherv.toBool();
            case STRING:
                return toString().equals(otherv.toString());
            case LIST:
            case TUPLE:
                return toList().equals(otherv.toList());
            case DICT:
                return toDict().equals(otherv.toDict());
            case SET:
                return toSet().equals(otherv.toSet());
            default:
                throw new RuntimeException("Unhandled type in equals");
        }
    }

    // used to simplify logic of math functions
    public boolean numberType() {
        return type == Type.INT || type == Type.FLOAT;
    }

    // we need to print "" around an strings that may be in this list/set/dict
    // the argument is this basically -- are we in a nested structure that
    // will require that should we get down to a stirng?
    @SuppressWarnings("unchecked")
    public String toString(boolean printQuotes) {
        String result = "";
        boolean first = false;

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
                result = "[";
                first = true;

                for (Value val : ((ArrayList<Value>) value)) {
                    if (first) {
                        first = false;
                    } else {
                        result += ", ";
                    }

                    result += val.toString(true);
                }

                return result + "]";
            case TUPLE:
                result = "(";
                first = true;

                for (Value val : ((ArrayList<Value>) value)) {
                    if (first) {
                        first = false;
                    } else {
                        result += ", ";
                    }

                    result += val.toString(true);
                }

                return result + ")";
            case DICT:
                result = "{";
                first = true;

                for (Value key : ((HashMap<Value, Value>) value).keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        result += ", ";
                    }

                    result += key.toString(true);
                    result += ": ";
                    result += ((HashMap<Value, Value>) value).get(key);
                }

                return result + "}";
                
            case SET:
                result = "{";
                first = true;

                for (Value val : ((HashSet<Value>) value)) {
                    if (first) {
                        first = false;
                    } else {
                        result += ", ";
                    }

                    result += val.toString(true);
                }

                return result + "}";
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
            default:
                throw new RuntimeException("type error slipped past type checker");
        }

    }

    public double toFloat() {
        switch (type) {
            case INT:
                return ((Integer) value).doubleValue();
            case FLOAT:
                return ((Double) value).doubleValue();
            default:
                throw new RuntimeException("type error slipped past type checker");
        }

    }

    public boolean toBool() {
        switch (type) {
            case BOOL:
                return ((Boolean) value).booleanValue();
            default:
                throw new RuntimeException("type error slipped past type checker");
        }

    }

    @SuppressWarnings("unchecked")
    public ArrayList<Value> toList() {
        if (type == Type.LIST || type == Type.TUPLE) {
            return (ArrayList<Value>) value;
        }

        throw new RuntimeException("type error slipped past type checker");
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Value, Value> toDict() {
        if (type == Type.DICT) {
            return (HashMap<Value, Value>) value;
        }

        throw new RuntimeException("type error slipped past type checker");
    }
    
    @SuppressWarnings("unchecked")
    public HashSet<Value> toSet() {
        if (type == Type.SET) {
            return (HashSet<Value>) value;
        }

        throw new RuntimeException("type error slipped past type checker");
    }
}


