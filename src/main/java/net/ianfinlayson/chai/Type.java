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

    public boolean equals(Object o) {
        Type other = (Type) o;

        if (this.kind != other.kind) {
            return false;
        }

        if (this.subtypes == null && other.subtypes == null) {
            // if neither has subtypes, they match
            return true;
        } else if (this.subtypes == null || other.subtypes == null) {
            // if one is missing subtypes, they don't match
            return false;
        } else if (this.subtypes.size() != other.subtypes.size()) {
            // if different amount of subtypes, they don't match
            return false;
        }

        // go through subtypes, if mismatch false
        for (int i = 0; i < this.subtypes.size(); i++) {
            if (!this.subtypes.get(i).equals(other.subtypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    public void addSub(Type t) {
        if (subtypes == null) {
            subtypes = new ArrayList<>();
        }
        subtypes.add(t);
    }

    @Override
    public String toString() {
        switch (kind) {
            case INT: return "Int";
            case FLOAT: return "Float";
            case BOOL: return "Bool";
            case STRING: return "String";
            case SET:
                return "{" + subtypes.get(0) + "}";
            case LIST:
                return "[" + subtypes.get(0) + "]";
            case DICT:
                return "{" + subtypes.get(0) + ": " + subtypes.get(1) + "}";
            case TUPLE:
                String t = "(";
                for (int i = 0; i < subtypes.size() - 1; i++) {
                    t += subtypes.get(i) + ", ";
                }
                t += subtypes.get(subtypes.size() - 1);
                t += ")";
                return t;
            default:
                throw new RuntimeException("Unhandled type");
        }
    }
}

