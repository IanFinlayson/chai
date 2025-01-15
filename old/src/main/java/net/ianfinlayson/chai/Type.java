package net.ianfinlayson.chai;

import java.util.ArrayList;

public class Type {
    // what type of type this is
    private Kind kind;

    // we keep an array of sub-types
    // for lists and sets this is length 1
    // for dicts it's length 2
    // for tuples it's the length of the tuple
    // for functions the fitsr is the return value, and the following are params
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

            // they all must match
            case TUPLE:
            case FUNCTION:
                if (this.subtypes.size() != other.subtypes.size()) {
                    return false;
                } else {
                    for (int i = 0; i < this.subtypes.size(); i++) {
                        if (!this.subtypes.get(i).equals(other.subtypes.get(i))) {
                            return false;
                        }
                    }

                    return true;
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
        String t;
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
                t = "(";
                for (int i = 0; i < subtypes.size() - 1; i++) {
                    t += subtypes.get(i) + ", ";
                }
                t += subtypes.get(subtypes.size() - 1);
                t += ")";
                return t;
            case FUNCTION:
                t = "";
                for (int i = 1; i < subtypes.size(); i++) {
                    t += subtypes.get(i);
                    t += " -> ";
                }
                t += subtypes.get(0);
                return t;

            default:
                throw new RuntimeException("Unhandled type");
        }
    }
}

