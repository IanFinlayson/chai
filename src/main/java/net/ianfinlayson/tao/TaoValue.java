package net.ianfinlayson.tao;

import java.util.ArrayList;

public class TaoValue {
    private TaoType type;
    private Object value;

    public TaoValue(int intVal) {
        type = TaoType.INT;
        value = Integer.valueOf(intVal);
    }

    public TaoValue(double floatVal) {
        type = TaoType.FLOAT;
        value = Double.valueOf(floatVal);
    }

    public TaoValue(boolean boolVal) {
        type = TaoType.BOOL;
        value = Boolean.valueOf(boolVal);
    }

    public TaoValue(String stringVal) {
        type = TaoType.STRING;
        value = stringVal;
    }

    public TaoValue(ArrayList<TaoValue> array) {
        type = TaoType.ARRAY;
        value = array;
    }

    public TaoType getType() {
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
                return ((ArrayList<TaoValue>) value).toString();
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
    public ArrayList<TaoValue> toArray() {
        if (type == TaoType.ARRAY) {
            return (ArrayList<TaoValue>) value;
        } else {
            ArrayList<TaoValue> stuff = new ArrayList<>();
            stuff.add(this);
            return stuff;
        }
    }
}


