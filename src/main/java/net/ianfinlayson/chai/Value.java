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
        // replace the escape sequences! this was annoying!
        stringVal = stringVal.replaceAll("\\\\n", "\n");
        stringVal = stringVal.replaceAll("\\\\t", "\t");
        stringVal = stringVal.replaceAll("\\\\\"", "\"");

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
    public boolean numberType() {
        return type == Type.INT || type == Type.FLOAT;
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
            case STRING:
            case LIST:
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
            case BOOL:
            case STRING:
            case LIST:
            default:
                throw new RuntimeException("type error slipped past type checker");
        }

    }

    public boolean toBool() {
        switch (type) {
            case BOOL:
                return ((Boolean) value).booleanValue();
            case INT:
            case FLOAT:
            case STRING:
            case LIST:
            default:
                throw new RuntimeException("type error slipped past type checker");
        }

    }

    @SuppressWarnings("unchecked")
    public ArrayList<Value> toList() {
        if (type == Type.LIST) {
            return (ArrayList<Value>) value;
        }

        throw new RuntimeException("type error slipped past type checker");
    }
}


