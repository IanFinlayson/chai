package net.ianfinlayson.chai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

// this class is used to provide iterations on data values
// such as for looping through data structures in for loops

// this may need to be more sophisticated once we get to
// generators

public class Stepper {
    private Value source;
    int index;
    private Iterator<Value> set_it; // used only for sets which have no other way

    public Stepper(Value source) {
        this.source = source;
        index = 0;
    
        if (source.getType() == Type.SET) {
            set_it = source.toSet().iterator();
        }
    }

    public boolean done() {
        // TODO add dict and set to this
        switch (source.getType()) {
            case STRING:
                String sval = source.toString();
                return index >= sval.length();
            case LIST:
                ArrayList<Value> lval = source.toList();
                return index >= lval.size();
            case SET:
                return !set_it.hasNext();
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
            case SET:
                return set_it.next();
        }

        throw new RuntimeException("iteration through illegal value");
    }
}

