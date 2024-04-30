package net.ianfinlayson.chai;

import java.util.ArrayList;

public class ChaiValue {
    private ChaiType type;
    private Object value;

    public ChaiValue(int intVal) {
        type = ChaiType.INT;
        value = Integer.valueOf(intVal);
    }

    public ChaiValue(double floatVal) {
        type = ChaiType.FLOAT;
        value = Double.valueOf(floatVal);
    }

    public ChaiValue(boolean boolVal) {
        type = ChaiType.BOOL;
        value = Boolean.valueOf(boolVal);
    }

    public ChaiValue(String stringVal) {
        type = ChaiType.STRING;
        value = stringVal;
    }

    public ChaiValue(ArrayList<ChaiValue> array) {
        type = ChaiType.LIST;
        value = array;
    }

    public ChaiType getType() {
        return type;
    }

    // used to simplify logic of math functions
    private boolean numberType() {
        return type == ChaiType.INT || type == ChaiType.FLOAT;
    }

    public ChaiValue plus(ChaiValue other) {
        if (type == ChaiType.STRING && other.type == ChaiType.STRING) {
            // string concatenation
            return new ChaiValue(toString() + other.toString());
        } else if (type == ChaiType.LIST && other.type == ChaiType.LIST) {
            // array concatenation
            ArrayList<ChaiValue> combined = new ArrayList<>();

            for (ChaiValue v : toArray()) {
                combined.add(v);
            }
            for (ChaiValue v : other.toArray()) {
                combined.add(v);
            }
            return new ChaiValue(combined);
        } else if (type == ChaiType.INT && other.type == ChaiType.INT) {
            // integer math
            return new ChaiValue(toInt() + other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new ChaiValue(toReal() + other.toReal());
        } else {
            throw new RuntimeException("Illegal types in + operation");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        switch (type) {
            case INT:
                return ((Integer) value).toString();
            case FLOAT:
                return ((Double) value).toString();
            case BOOL:
                return ((Boolean) value).toString();
            case STRING:
                return ((String) value).toString();
            case LIST:
                return ((ArrayList<ChaiValue>) value).toString();
        }

        throw new RuntimeException("Unhandled type in swtich/case");
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

    public double toReal() {
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
    public ArrayList<ChaiValue> toArray() {
        if (type == ChaiType.LIST) {
            return (ArrayList<ChaiValue>) value;
        }
       
        throw new TypeMismatchException("Cannot convert type to array");
    }
}


