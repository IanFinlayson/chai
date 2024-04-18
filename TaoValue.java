
import java.util.ArrayList;

public class TaoValue {
    private TaoType type;
    private Object value;

    public TaoValue(int intVal) {
        type = TaoType.INT;
        value = Integer.valueOf(intVal);
    }

    public TaoValue(double realVal) {
        type = TaoType.REAL;
        value = Double.valueOf(realVal);
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
    public String toString() {
        switch (type) {
            case INT:
                return ((Integer) value).toString();
            case REAL:
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
            case REAL:
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
            case REAL:
                return ((Double) value).doubleValue();
            case BOOL:
                throw new TypeMismatchException("Cannot convert boolean to real");
            case STRING:
                throw new TypeMismatchException("Cannot convert string to real");
            case ARRAY:
                throw new TypeMismatchException("Cannot convert array to real");
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
    }

    public boolean toBool() {
        switch (type) {
            case INT:
                throw new TypeMismatchException("Cannot convert integer to boolean");
            case REAL:
                throw new TypeMismatchException("Cannot convert real to boolean");
            case BOOL:
                return ((Boolean) value).booleanValue();
            case STRING:
                throw new TypeMismatchException("Cannot convert string to boolean");
            case ARRAY:
                throw new TypeMismatchException("Cannot convert array to boolean");
        }

        throw new RuntimeException("Unhandled type ni swtich/case");
    }

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


