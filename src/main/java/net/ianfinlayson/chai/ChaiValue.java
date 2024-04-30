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

            for (ChaiValue v : toList()) {
                combined.add(v);
            }
            for (ChaiValue v : other.toList()) {
                combined.add(v);
            }
            return new ChaiValue(combined);
        } else if (type == ChaiType.INT && other.type == ChaiType.INT) {
            // integer math
            return new ChaiValue(toInt() + other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new ChaiValue(toFloat() + other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in + operation");
        }
    }

    public ChaiValue minus(ChaiValue other) {
        if (type == ChaiType.INT && other.type == ChaiType.INT) {
            // integer math
            return new ChaiValue(toInt() - other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new ChaiValue(toFloat() - other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in - operation");
        }
    }
    
    public ChaiValue times(ChaiValue other) {
        // "hi " * 3 gives us "hi hi hi "
        if (type == ChaiType.STRING && other.type == ChaiType.INT) {
            String result = "";
            for (int i = 0; i < other.toInt(); i++) {
                result += toString();
            }
            return new ChaiValue(result);
        } else if (type == ChaiType.INT && other.type == ChaiType.STRING) {
            return other.times(this);
        }
        
        // [1,2] * 2 gives us [1,2,1,2]
        else if (type == ChaiType.LIST && other.type == ChaiType.INT) {
            ArrayList<ChaiValue> result = new ArrayList<>();
            
            ArrayList<ChaiValue> from = toList();
            for (int i = 0; i < other.toInt(); i++) {
                for (int j = 0; j < from.size(); j++) {
                    result.add(from.get(j));
                }
            }
            return new ChaiValue(result);
        } else if (type == ChaiType.INT && other.type == ChaiType.LIST) {
            return other.times(this);
        }
        
        // otherwise do number stuff
        else if (type == ChaiType.INT && other.type == ChaiType.INT) {
            // integer math
            return new ChaiValue(toInt() * other.toInt());
        } else if (numberType() && other.numberType()) {
            // float math
            return new ChaiValue(toFloat() * other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in * operation");
        }
    }
    
    public ChaiValue divide(ChaiValue other) {
        if (numberType() && other.numberType()) {
            // float math
            return new ChaiValue(toFloat() / other.toFloat());
        } else {
            throw new RuntimeException("Illegal types in / operation");
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
                return ((Boolean) value).toString();
            case STRING:
                if (printQuotes) {
                    return "\"" + ((String) value) + "\"";
                } else {
                    return ((String) value);
                }

            case LIST:
                String result = "[";
                boolean first = true;
                
                for (ChaiValue val : ((ArrayList<ChaiValue>) value)) {
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
    public ArrayList<ChaiValue> toList() {
        if (type == ChaiType.LIST) {
            return (ArrayList<ChaiValue>) value;
        }
       
        throw new TypeMismatchException("Cannot convert type to array");
    }
}


