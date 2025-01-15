package net.ianfinlayson.chai;

import java.util.ArrayList;
import java.util.List;

class Parameter {
    public String name;
    public Type type;
    public boolean defaultValue;
    public boolean assigned;

    public Parameter(String name, Type type, boolean defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.assigned = false;
    }

    @Override
    public String toString() {
        return "(" + name + ", " + type + ", " + defaultValue + ")";
    }
}

public class Function {
    private String name;
    private ChaiParser.StatementsContext stmts;
    private Type returnType;
    private ArrayList<Parameter> parameters;

    public Function(String name, ChaiParser.StatementsContext stmts, Type returnType) {
        this.name = name;
        this.stmts = stmts;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    // the first subtype is return, rest are params
    public Type getFullType() {
        Type t = new Type(Kind.FUNCTION);
        t.addSub(returnType);
        for (Parameter p : parameters) {
            t.addSub(p.type);
        }

        return t;
    }

    // just the return type alone w/ no subs
    public Type getReturnType() {
        return returnType;
    }

    public int getNumParams() {
        return parameters.size();
    }

    public String paramNames(int i) {
        return parameters.get(i).name;
    }

    public Type paramTypes(int i) {
        return parameters.get(i).type;
    }

    public void registerParameter(String name, Type type, boolean defaultValue) {
        parameters.add(new Parameter(name, type, defaultValue));
    }

    @Override
    public boolean equals(Object other) {
        // functions aren't equal unless LITERALY so
        return this == other;
    }

    @Override
    public String toString() {
        return "<function " + name + ">";
    }

    public Type typeCheck(List<ChaiParser.ArgumentContext> args, int line, TypeChecker tc) {
        // reset in case this was called before
        for (Parameter p : parameters) {
            p.assigned = false;
        }
        
        // step 1: go through args looking for named ones, and assign those into formals w/ same name
        for (ChaiParser.ArgumentContext arg : args) {
            if (arg.IDNAME() != null) {
                String argname = arg.IDNAME().getText();

                // find it in the list of formal params
                boolean found = false;
                for (Parameter formal : parameters) {
                    if (formal.name.equals(argname)) {
                        formal.assigned = true;
                        found = true;

                        // ensure types match
                        if (!formal.type.equals(tc.visit(arg.expression()))) {
                            throw new TypeMismatchException("Named argument '" + argname +"' does not match expected type", line);
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new TypeMismatchException("Named argument '" + argname +"' not found in formals", line);
                }
            }
        }

        // step 2: go through args looking for unnamed ones, and assign positionally to unassigned formals
        for (ChaiParser.ArgumentContext arg : args) {
            if (arg.IDNAME() == null) {
                boolean done = false;
                for (Parameter formal : parameters) {
                    if (!formal.assigned) {
                        // ensure types match
                        if (!formal.type.equals(tc.visit(arg.expression()))) {
                            throw new TypeMismatchException("Positional argument does not match expected type", line);
                        }

                        formal.assigned = true;
                        done = true;
                        break;
                    }
                }
                if (!done) {
                    throw new TypeMismatchException("Too many arguments given to function", line);
                }
            }
        }

        // step 3: go through formals and assign un-assigned ones their default params
        // if they don't ave one, that's an error (for now -- later they will be curried functions!)
        for (Parameter formal : parameters) {
            if (!formal.assigned) {
                if (formal.defaultValue) {
                    formal.assigned = true;
                } else {
                    throw new TypeMismatchException("Not enough arguments given to function", line);
                }
            }
        }

        // the type of the call is the return value of the function
        return getReturnType();
    }
}


