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
        type = ChaiType.ARRAY;
        value = array;
    }

    public ChaiType getType() {
        return type;
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
            case ARRAY:
                return ((ArrayList<ChaiValue>) value).toString();
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
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
            case ARRAY:
                throw new TypeMismatchException("Cannot convert array to integer");
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
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
            case ARRAY:
                throw new TypeMismatchException("Cannot convert array to float");
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
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
            case ARRAY:
                throw new TypeMismatchException("Cannot convert array to boolean");
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ChaiValue> toArray() {
        if (type == ChaiType.ARRAY) {
            return (ArrayList<ChaiValue>) value;
        } else {
            ArrayList<ChaiValue> stuff = new ArrayList<>();
            stuff.add(this);
            return stuff;
        }
    }
}


