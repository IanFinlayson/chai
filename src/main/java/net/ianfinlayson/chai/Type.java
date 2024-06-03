package net.ianfinlayson.chai;

import java.util.ArrayList;

public class Type {
    // what type of type this is
    private Kind kind;

    // we keep an array of sub-types
    // for lists and sets this is length 1
    // for dicts it's length 2
    // for tuples it's the length of the tuple
    private ArrayList<Type> subtypes = null;

    public Type(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public void addSub(Type t) {
        if (subtypes == null) {
            subtypes = new ArrayList<>();
        }
        subtypes.add(t);
    }
}

