package net.ianfinlayson.chai;

import java.util.ArrayList;

// TODO:
// - functions
// - unions
// - generator
// - class types
// -type parameters


public class Type {
    // what type of type this is
    private Kind kind;

    // we keep an array of sub-types
    // for lists and sets this is length 1
    // for dicts it's length 2
    // for tuples it's the length of the tuple
    // for functions it's the number of args + 1 (for return type)
    private ArrayList<Type> subtypes = null;

    public Type(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean equals(Object o) {
        Type other = (Type) o;

        // if different kinds, they dont match
        if (this.kind != other.kind) {
            return false;
        }

        switch (kind) {
            // these are scalars, so they just match
            case INT:
            case FLOAT:
            case BOOL:
            case STRING:
                return true;

            // if one or both are empty they match, if both filled they must be the same
            case LIST:
            case SET:
                if  (this.subtypes == null || other.subtypes == null) {
                    return true;
                } else if (this.subtypes.get(0).equals(other.subtypes.get(0))) {
                    return true;
                } else {
                    return false;
                }

            // all of the subs must match
            case TUPLE:
                if (this.subtypes == null || other.subtypes == null) {
                    return false;
                } else if (this.subtypes.size() != other.subtypes.size()) {
                    return false;
                } else {
                    for (int i = 0; i < this.subtypes.size(); i++) {
                        if (!this.subtypes.get(i).equals(other.subtypes.get(i))) {
                            return false;
                        }
                    }
                    return true;
                }

                // if one or both are empty they match, if both filled they must be the same, but for both!
            case DICT:
                if  (this.subtypes == null || other.subtypes == null) {
                    return true;
                } else {
                    if (this.subtypes.get(0).equals(other.subtypes.get(0)) &&
                            this.subtypes.get(1).equals(other.subtypes.get(1))) {
                        return true;
                    } else {
                        return false;
                    }
                }

            default:
                throw new RuntimeException("Unhandled type in type equality method");
        }
    }

    public void addSub(Type t) {
        if (subtypes == null) {
            subtypes = new ArrayList<>();
        }
        subtypes.add(t);
    }

    public ArrayList<Type> getSubs() {
        return subtypes;
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
                if (subtypes == null) {
                    return "[]";
                } else {
                    return "[" + subtypes.get(0) + "]";
                }
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

