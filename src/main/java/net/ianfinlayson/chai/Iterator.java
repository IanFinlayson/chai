package net.ianfinlayson.chai;

import java.util.ArrayList;

// this class is used to provide iterations on data values
// such as for looping through data structures in for loops

// this may need to be more sophisticated once we get to
// generators

public class Iterator {
    private Value source;
    int index;

    public Iterator(Value source) {
        this.source = source;
        index = 0;
    }

    public boolean done() {
        switch (source.getType()) {
            case STRING:
                String sval = source.toString();
                return index >= sval.length();
            case LIST:
                ArrayList<Value> lval = source.toList();
                return index >= lval.size();
        }

        throw new RuntimeException("iteration through illegal value");
    }

    public Value next() {
        switch (source.getType()) {
            case STRING:
                String sval = source.toString();
                char letter = sval.charAt(index);
                index++;
                return new Value(String.valueOf(letter));
            case LIST:
                ArrayList<Value> lval = source.toList();
                Value v = lval.get(index);
                index++;
                return v;
        }

        throw new RuntimeException("iteration through illegal value");
    }
}

