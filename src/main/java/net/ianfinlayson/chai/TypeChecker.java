package net.ianfinlayson.chai;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

public class TypeChecker extends ChaiParserBaseVisitor<Type> {
    // each of these methods performs type checking in their part of the
    // tree, and returns they type of it (for expressions) or null (for stmts)
    // it throws exceptions for type errors that are encountered


    // variables can be made constant, so this combines the value with that info
    class Variable {
        public Type type;
        public boolean constant;
        public int declared_line;

        public Variable(Type type, boolean constant, int line) {
            this.type = type;
            this.constant = constant;
            this.declared_line = line;
        }
    }

    // we keep track of the types of variables in functions and globals
    private Stack<HashMap<String, Variable>> stack = new Stack<>();
    private HashMap<String, Variable> globals = new HashMap<>();

    private Variable loadVar(String name) {
        // first look to see if it is in the top of the stack
        if (!stack.empty()) {
            Variable local = stack.peek().get(name);
            if (local != null) {
                return local;
            }
        }

        // otherwise check globals
        Variable global = globals.get(name);
        if (global != null) {
            return global;
        }

        return null;
    }

    private void putVar(String name, Type type, boolean constant, int line) {
        // check if it exists first
        // this prevents locals shadowing globals, which i think is good
        if ((!stack.empty() && stack.peek().get(name) != null) || globals.get(name) != null) {
            throw new TypeMismatchException("Variable " + name + " was already declared", line);
        }

        // actually write it into correct scope
        if (!stack.empty()) {
            stack.peek().put(name, new Variable(type, constant, line));
        } else {
            globals.put(name, new Variable(type, constant, line));
        }
    }

    private void nixVar(String name) {
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                stack.peek().remove(name);
                return;
            }
        }

        if (globals.get(name) != null) {
            globals.remove(name);
        }
    }

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

    // we keep track of all the types for functions
    class Function {
        public String name;
        public Type returnType;
        public ArrayList<Parameter> parameters;

        public Function(String name) {
            this.name = name;
            this.returnType = null;
            this.parameters = new ArrayList<>();
        }

        // reset all params to un-assigned so we can type check the next call
        public void reset() {
            for (Parameter p : parameters) {
                p.assigned = false;
            }
        }
    }

    // this is used to keep track of function types so we can check the calls
    HashMap<String, Function> functions = new HashMap<>();

    // this is used so we know what function we're in so we can check returns
    Stack<Function> currentFunctions = new Stack<>();

    @Override
    public Type visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // grab the name and setup a function for it
        String name = ctx.IDNAME().getText();
        Function func = new Function(name);
        currentFunctions.push(func);

        // get the parameters
        if (ctx.paramlist() != null) {
            List<ChaiParser.ParamContext> params = ctx.paramlist().param();
            for (ChaiParser.ParamContext param : params) {
                if (param instanceof ChaiParser.NamedParamContext) {
                    ChaiParser.NamedParamContext np = (ChaiParser.NamedParamContext) param;

                    // put in the parameter with no default value
                    func.parameters.add(new Parameter(np.IDNAME().getText(), visit(np.type()), false));
                } else {
                    ChaiParser.DefaultParamContext dp = (ChaiParser.DefaultParamContext) param;
                    
                    // get the pieces
                    String pname = dp.IDNAME().getText();
                    Type inferred = visit(dp.term());
                    
                    // if given a type, we type-check it
                    if (dp.type() != null) {
                        if (!inferred.equals(visit(dp.type()))) {
                            throw new TypeMismatchException("Given type of paramter '" + pname +"' does not match actual type",
                                    ctx.getStart().getLine());
                        }
                    }

                    // put it in
                    func.parameters.add(new Parameter(pname, inferred, true));
                }
            }
        }

        // get the return type for it (maybe null)
        if (ctx.type() == null) {
            func.returnType = null;
        } else {
            func.returnType = visit(ctx.type());
        }

        // add it to the map of functions
        functions.put(name, func);

        // make a scope for this function
        stack.push(new HashMap<String, Variable>());

        // add the parameters into the scope
        for (Parameter param : func.parameters) {
            putVar(param.name, param.type, false, ctx.getStart().getLine());
        }

        // type check the function body
        visit(ctx.statements());

        // get rid of the stack frames
        stack.pop();
        currentFunctions.pop();
        return null;
    }

    @Override
    public Type visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        // make sure we are in a function
        if (currentFunctions.empty()) {
            throw new TypeMismatchException("Return encountered outside of a function", ctx.getStart().getLine());
        }

        // if there is no return type, make sure the function is void
        if (ctx.expression() == null) {
            if (currentFunctions.peek().returnType != null) {
                throw new TypeMismatchException("Function returns a value but none given", ctx.getStart().getLine());
            }
        }

        // if there is a return type, make sure it matches the function type
        else {
            Type givenValue = visit(ctx.expression());
            if (currentFunctions.peek().returnType == null || !givenValue.equals(currentFunctions.peek().returnType)) {
                throw new TypeMismatchException("Return value does match the function type", ctx.getStart().getLine());
            }

        }

        return null;
    }

    @Override
    public Type visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // grab the function name
        String name = ctx.IDNAME().getText();

        // get the arguments
        List<ChaiParser.ArgumentContext> args;
        if (ctx.arglist() != null) {
           args = ctx.arglist().argument();
        } else {
            args = new ArrayList<>();
        }

        // handle library functions
        switch (name) {
            case "print": return checkPrint(args, ctx.getStart().getLine());
            case "input": return checkInput(args, ctx.getStart().getLine());
            case "len": return checkLen(args, ctx.getStart().getLine());
        }

        // get the function that we are calling
        Function callee = functions.get(name);
        if (callee == null) {
            throw new TypeMismatchException("Function '" + name +"' not found", ctx.getStart().getLine());
        }
        callee.reset();
        
        // step 1: go through args looking for named ones, and assign those into formals w/ same name
        for (ChaiParser.ArgumentContext arg : args) {
            if (arg.IDNAME() != null) {
                String argname = arg.IDNAME().getText();

                // find it in the list of formal params
                boolean found = false;
                for (Parameter formal : callee.parameters) {
                    if (formal.name.equals(argname)) {
                        formal.assigned = true;
                        found = true;

                        // ensure types match
                        if (!formal.type.equals(visit(arg.expression()))) {
                            throw new TypeMismatchException("Named argument '" + argname +"' does not match expected type", ctx.getStart().getLine());
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new TypeMismatchException("Named argument '" + argname +"' not found in formals", ctx.getStart().getLine());
                }
            }
        }

        // step 2: go through args looking for unnamed ones, and assign positionally to unassigned formals
        for (ChaiParser.ArgumentContext arg : args) {
            if (arg.IDNAME() == null) {
                boolean done = false;
                for (Parameter formal : callee.parameters) {
                    if (!formal.assigned) {
                        // ensure types match
                        if (!formal.type.equals(visit(arg.expression()))) {
                            throw new TypeMismatchException("Positional argument does not match expected type", ctx.getStart().getLine());
                        }

                        formal.assigned = true;
                        done = true;
                        break;
                    }
                }
                if (!done) {
                    throw new TypeMismatchException("Too many arguments given to function", ctx.getStart().getLine());
                }
            }
        }

        // step 3: go through formals and assign un-assigned ones their default params
        // if they don't ave one, that's an error (for now -- later they will be curried functions!)
        for (Parameter formal : callee.parameters) {
            if (!formal.assigned) {
                if (formal.defaultValue) {
                    formal.assigned = true;
                } else {
                    throw new TypeMismatchException("Not enough arguments given to function", ctx.getStart().getLine());
                }
            }
        }

        // the type of the call is the return value of the function
        return callee.returnType;
    }

    private Type checkPrint(List<ChaiParser.ArgumentContext> args, int line)  {
        // scan for the end and sep keywords, other than that, fair game
        for (ChaiParser.ArgumentContext arg : args) {
            if (arg.IDNAME() != null) {
                if (arg.IDNAME().getText().equals("end") || arg.IDNAME().getText().equals("sep")) {
                    if (visit(arg.expression()).getKind() != Kind.STRING) {
                        throw new TypeMismatchException("Key word argument '" + arg.IDNAME().getText() + "' must be of type string", line);
                    }
                }
            }
        }

        return null;
    }
    private Type checkInput(List<ChaiParser.ArgumentContext> args, int line) {
        if (args.size() != 1) {
            throw new TypeMismatchException("Too " + (args.size() == 0 ? "few" : "many") + " arguments given to input", line);
        }
        if (visit(args.get(0).expression()).getKind() != Kind.STRING) {
            throw new TypeMismatchException("Argument to input must be of type String", line);
        }

        return new Type(Kind.STRING);
    }
    private Type checkLen(List<ChaiParser.ArgumentContext> args, int line) {
        if (args.size() != 1) {
            throw new TypeMismatchException("Too " + (args.size() == 0 ? "few" : "many") + " arguments given to len", line);
        }
        switch (visit(args.get(0)).getKind()) {
            case STRING:
            case LIST:
            case DICT:
            case TUPLE:
            case SET:
                // these are OK
                return new Type(Kind.INT);
            default:
                throw new TypeMismatchException("Cannot take length of scalar value", line);
        }
    }

    @Override
    public Type visitFloatType(ChaiParser.FloatTypeContext ctx) {
        return new Type(Kind.FLOAT);
    }

    @Override
    public Type visitIntType(ChaiParser.IntTypeContext ctx) {
        return new Type(Kind.INT);
    }

    @Override
    public Type visitStringType(ChaiParser.StringTypeContext ctx) {
        return new Type(Kind.STRING);
    }

    @Override
    public Type visitVoidType(ChaiParser.VoidTypeContext ctx) {
        return null;
    }

    @Override
    public Type visitTupleType(ChaiParser.TupleTypeContext ctx) {
        Type tups = new Type(Kind.TUPLE);

        for (ChaiParser.TypeContext t : ctx.type()) {
            tups.addSub(visit(t));
        }
        return tups;
    }

    @Override
    public Type visitSetType(ChaiParser.SetTypeContext ctx) {
        Type set = new Type(Kind.SET);
        set.addSub(visit(ctx.type()));
        return set;
    }

    @Override
    public Type visitListType(ChaiParser.ListTypeContext ctx) {
        Type list = new Type(Kind.LIST);
        list.addSub(visit(ctx.type()));
        return list;
    }

    @Override
    public Type visitBoolType(ChaiParser.BoolTypeContext ctx) {
        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitDictType(ChaiParser.DictTypeContext ctx) {
        Type dict = new Type(Kind.DICT);
        dict.addSub(visit(ctx.type(0)));
        dict.addSub(visit(ctx.type(1)));
        return dict;
    }

    @Override
    public Type visitVarStatement(ChaiParser.VarStatementContext ctx) {
        String name = ctx.IDNAME().getText();

        // get the type of the expression being assigned
        Type inferred = visit(ctx.expression());
        if (ctx.type() != null) {
            Type declared = visit(ctx.type());
            if (!declared.equals(inferred)) {
                throw new TypeMismatchException("Declared type '" + declared + "' does not match assigned type '" + inferred, ctx.getStart().getLine());
            }
        }

        // check if it's declared as a constant
        boolean constant = ctx.LET() != null;

        // put it into the scope
        putVar(name, inferred, constant, ctx.getStart().getLine());
        return null;
    }

    @Override
    public Type visitIdTerm(ChaiParser.IdTermContext ctx) {
        String name = ctx.IDNAME().getText();

        // grab the variable
        Variable vari = loadVar(name);

        if (vari == null) {
            throw new TypeMismatchException("Variable " + name + " was not declared in this scope", ctx.getStart().getLine());
        } else {
            return vari.type;
        }
    }

    @Override
    public Type visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // get the types for the destination and expression and make sure they match
        Type dest = visit(ctx.lvalue());
        Type result = visit(ctx.expression());

        // make sure the types match up
        if (!dest.equals(result)) {
            throw new TypeMismatchException("Types in assignment do not match", ctx.getStart().getLine());
        }

        return null;
    }

    @Override
    public Type visitNestedLvalue(ChaiParser.NestedLvalueContext ctx) {
        Type base = visit(ctx.lvalue());
        Type index = visit(ctx.expression());
        switch (base.getKind()) {
            case STRING:
                // make sure the index is an integer
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("String index must be of integer type", ctx.getStart().getLine());
                }
                // return string
                return base;
            case LIST:
                // make sure the index is an integer
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("String index must be of integer type", ctx.getStart().getLine());
                }
                // return subtype
                return base.getSubs().get(0);
            case DICT:
                // make sure index is sub0
                if (!index.equals(base.getSubs().get(0))) {
                    throw new TypeMismatchException("Incorrect index type on dictionary", ctx.getStart().getLine());
                }

                // return sub1
                return base.getSubs().get(1);
            default:
                throw new TypeMismatchException("Cannot apply index operator to type", ctx.getStart().getLine());
        }
    }

	@Override
    public Type visitJustID(ChaiParser.JustIDContext ctx) {
        String name = ctx.IDNAME().getText();
        Variable vari = null;

        // find it on the stack or as a global
        if (!stack.empty()) {
            vari = stack.peek().get(name);
        }
        if (vari == null) {
            vari = globals.get(name);
        }

        // if not found, that's an error
        if (vari == null) {
            throw new TypeMismatchException("Variable " + name + " was not declared in this scope", ctx.getStart().getLine());
        }

        // if it's constant, that's an error (this chain is only for assignment statements)
        if (vari.constant) {
            throw new TypeMismatchException("Attempt to reassign constat " + name, ctx.getStart().getLine());
        }

        return vari.type;
    }

    // helper function for numeric type checks
    private boolean numberType(Type t) {
        return t.getKind() == Kind.INT || t.getKind() == Kind.FLOAT;
    }

    @Override
    public Type visitModassign(ChaiParser.ModassignContext ctx) {
        Type dest = visit(ctx.lvalue());
        Type rhs = visit(ctx.expression());

        switch (ctx.op.getType()) {
            // both numbers, both strings, both matching lists
            case ChaiLexer.PLUSASSIGN:
                if (dest.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    // this is cool
                } else if (dest.getKind() == Kind.FLOAT && numberType(rhs)) {
                    // this is ok
                } else if (dest.getKind() == Kind.STRING && rhs.getKind() == Kind.STRING) {
                    // fine too
                } else if (dest.getKind() == Kind.LIST && dest.equals(rhs)) {
                    // also cool
                } else {
                    throw new TypeMismatchException("Unsupported types for += operation", ctx.getStart().getLine());
                }
                break;

            // both numbers, dest list/string and rhs int
            case ChaiLexer.TIMESASSIGN:
                if (dest.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    // this is cool
                } else if (dest.getKind() == Kind.FLOAT && numberType(rhs)) {
                    // this is ok
                } else if ((dest.getKind() == Kind.STRING || dest.getKind() == Kind.LIST) && rhs.getKind() == Kind.INT) {
                    // this is ok too
                } else {
                    throw new TypeMismatchException("Unsupported types for *= operation", ctx.getStart().getLine());
                }
                break;

            // both numbers
            case ChaiLexer.MINUSASSIGN:
            case ChaiLexer.DIVASSIGN:
            case ChaiLexer.MODASSIGN:
            case ChaiLexer.INTDIVASSIGN:
                if (dest.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    // this is cool
                } else if (dest.getKind() == Kind.FLOAT && numberType(rhs)) {
                    // this is ok
                } else {
                    throw new TypeMismatchException("Unsupported types for assignment operation", ctx.getStart().getLine());
                }
                break;

            // these ones must all be ints
            case ChaiLexer.LSHIFTASSIGN:
            case ChaiLexer.RSHIFTASSIGN:
            case ChaiLexer.BITANDASSIGN:
            case ChaiLexer.BITORASSIGN:
            case ChaiLexer.BITXORASSIGN:
                if (dest.getKind() != Kind.INT || rhs.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Bitwise operator ony applies to integer type", ctx.getStart().getLine());
                }
                break;
        }

        return null;
    }

    @Override
    public Type visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        // get the type of the expression, it must be boolean
        Type exprType = visit(ctx.expression());
        if (exprType.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("assert must be a boolean type", ctx.getStart().getLine());
        }

        return null;
    }

    @Override
    public Type visitForStatement(ChaiParser.ForStatementContext ctx) {
        // we need to make the induction variable with the right type, then type check the body
        // and then remove the induction variable again
        Type it = visit(ctx.expression());

        // get the type of the induction variable
        Type indType = null;
        switch (it.getKind()) {
            case STRING:
                indType = new Type(Kind.STRING);
                break;
            case LIST:
                if (it.getSubs() != null) {
                    indType = it.getSubs().get(0);
                } else {
                    // why would someone loop through []?
                    indType = new Type(Kind.LIST);
                }
                break;
            case DICT:
                indType = new Type(Kind.TUPLE);
                indType.addSub(it.getSubs().get(0));
                indType.addSub(it.getSubs().get(1));
                break;
            case SET:
                if (it.getSubs() != null) {
                    indType = it.getSubs().get(0);
                } else {
                    // why would someone loop through {}?
                    indType = new Type(Kind.SET);
                }
                break;
            default:
                throw new TypeMismatchException("Value in for loop is not iterable", ctx.getStart().getLine());
        }

        // make sure the variable does not already exist
        String indVar = ctx.IDNAME().getText();
        if (loadVar(indVar) != null) {
            throw new TypeMismatchException("For loop variable " + indVar + " already exists", ctx.getStart().getLine());
        }

        // make it
        putVar(indVar, indType, false, ctx.getStart().getLine());

        // go through all of the statements
        visit(ctx.statements());

        // remove it
        nixVar(indVar);

        return null;
    }

    @Override
    public Type visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("While condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitIfstmt(ChaiParser.IfstmtContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("If condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());

        // if there is elif clauses and an else clause, visit them
        for (ChaiParser.ElifclauseContext elif : ctx.elifclause()) {
            visit(elif);
        }
        if (ctx.elseclause() != null) {
            visit(ctx.elseclause());
        }

        return null;
    }

    @Override
    public Type visitElifclause(ChaiParser.ElifclauseContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("If condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitElseclause(ChaiParser.ElseclauseContext ctx) {
        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Shifts only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise or only operates on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise xor only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise and only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        if (visit(ctx.expression()).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Not only applies to booleans", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.BOOL ||
            visit(ctx.expression(1)).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Arguments to or operator must be boolean", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.BOOL ||
            visit(ctx.expression(1)).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Arguments to and operator must be boolean", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        // we do equality separate from order
        if (ctx.op.getType() == ChaiLexer.EQUALS || ctx.op.getType() == ChaiLexer.NOTEQUALS) {
            // they must match, except float/int is fine
            if (lhs.getKind() != rhs.getKind()) {
                if (!numberType(lhs) || !numberType(rhs)) {
                    throw new TypeMismatchException("Unsupported types to equality operator", ctx.getStart().getLine());
                }
            }
        } else {
            // we only allow these for numbers and strings
            if (lhs.getKind() == Kind.STRING && rhs.getKind() == Kind.STRING) {
                // this is ok
            } else if (numberType(lhs) && numberType(rhs)) {
                // this is ok
            } else {
                throw new TypeMismatchException("Unsupported types to comparison operator", ctx.getStart().getLine());
            }
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitPlusMinusExpression(ChaiParser.PlusMinusExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        if (ctx.op.getType() == ChaiLexer.PLUS) {
            if (lhs.getKind() == Kind.LIST && rhs.getKind() == Kind.LIST) {
                // must be compatible types
                if (!lhs.equals(rhs)) {
                    throw new TypeMismatchException("List types for + operator do not match", ctx.getStart().getLine());
                }

                // one could be empty (or both i guess...) so we find the one that's not (if any)
                if (lhs.getSubs() == null) {
                    return rhs;
                } else {
                    return lhs;
                }
            } else if (lhs.getKind() == Kind.STRING && rhs.getKind() == Kind.STRING) {
                return new Type(Kind.STRING);
            } else if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                return new Type(Kind.INT);
            } else if (numberType(lhs) && numberType(rhs)) {
                return new Type(Kind.FLOAT);
            } else {
                throw new TypeMismatchException("Unsupported types to + operator", ctx.getStart().getLine());
            }
        } else {
            // subtraction
            if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                return new Type(Kind.INT);
            } else if (numberType(lhs) && numberType(rhs)) {
                return new Type(Kind.FLOAT);
            } else {
                throw new TypeMismatchException("Unsupported types to - operator", ctx.getStart().getLine());
            }
        }
    }

    @Override
    public Type visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case ChaiLexer.TIMES:
                // we can do list * num, or string * num
                if ((lhs.getKind() == Kind.LIST || lhs.getKind() == Kind.STRING) && rhs.getKind() == Kind.INT) {
                    return lhs;
                } else if (lhs.getKind() == Kind.INT && (rhs.getKind() == Kind.LIST || rhs.getKind() == Kind.STRING)) {
                    return rhs;
                } else if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to * operator", ctx.getStart().getLine());
                }

            case ChaiLexer.DIVIDE:
                if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to / operator", ctx.getStart().getLine());
                }
            case ChaiLexer.INTDIV:
                if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.INT);
                } else {
                    throw new TypeMismatchException("Unsupported types to // operator", ctx.getStart().getLine());
                }
            case ChaiLexer.MODULUS:
                if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
                }
            default:
                throw new RuntimeException("Unhandled multiplicative operator in type checker");
        }
    }

    @Override
    public Type visitPowerExpression(ChaiParser.PowerExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
            return new Type(Kind.INT);
        } else if (numberType(lhs) && numberType(rhs)) {
            return new Type(Kind.FLOAT);
        } else {
            throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
        }
    }

    @Override
    public Type visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        Type operand = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUS:
            case ChaiLexer.MINUS:
                if (operand.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (operand.getKind() == Kind.FLOAT) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
                }
            case ChaiLexer.COMPLEMENT:
                if (operand.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Unsupported type to ~ operator", ctx.getStart().getLine());
                } else {
                    return new Type(Kind.INT);
                }
            default:
                throw new RuntimeException("Unhandled unary operator in type checker");
        }
    }

    // private method used for 'in' and 'not in' checks
    private Type checkInExpression(Type item, Type collection, int line) {
        switch (collection.getKind()) {
            case STRING:
                // the item must be a string
                if (item.getKind() != Kind.STRING) {
                    throw new TypeMismatchException("Unsupported types in 'in' expression", line);
                }
                break;
            case LIST:
            case SET:
            case DICT:
                // the item must match the (first) subtype, or the subtype can be empty
                if (collection.getSubs() != null && !item.equals(collection.getSubs().get(0))) {
                    throw new TypeMismatchException("Search type does not match collection type in 'in' expression", line);
                }
                break;

            default:
                throw new TypeMismatchException("Unsupported types in 'in' expression", line);
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        return checkInExpression(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.getStart().getLine());
    }

    @Override
    public Type visitInExpression(ChaiParser.InExpressionContext ctx) {
        return checkInExpression(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.getStart().getLine());
    }

    @Override
    public Type visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        // grab the three expressions here
        Type ifpart = visit(ctx.expression(0));
        Type cond = visit(ctx.expression(1));
        Type elsepart = visit(ctx.expression(2));

        // the condition must be boolean
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Condition of if expression must be boolean", ctx.getStart().getLine());
        }

        // the if and then parts must match
        if (!ifpart.equals(elsepart)) {
            throw new TypeMismatchException("If and ele clauses of if expression must be of same type", ctx.getStart().getLine());
        }

        // the result is the type of the parts
        return ifpart;
    }

    @Override
    public Type visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        // get the two expression types
        Type elem = visit(ctx.expression(0));
        Type existing = visit(ctx.expression(1));

        // the existing must be a list
        if (existing.getKind() != Kind.LIST) {
            throw new TypeMismatchException("Cons operator can only apply to list type", ctx.getStart().getLine());
        }

        // either the existing list is empty or the types match
        if (existing.getSubs() == null || elem.equals(existing.getSubs().get(0))) {
            // the type is the same as the existing list
            return existing;
        } else {
            throw new TypeMismatchException("Cannot cons item to list of different type", ctx.getStart().getLine());
        }
    }








    @Override
    public Type visitListRangeTerm(ChaiParser.ListRangeTermContext ctx) {
        // grab the start and end, both must be ints
        Type start = visit(ctx.expression(0));
        Type end = visit(ctx.expression(1));

        if (start.getKind() != Kind.INT || end.getKind() != Kind.INT) {
            throw new TypeMismatchException("Endpoints of range expression must be integers", ctx.getStart().getLine());
        }

        Type range = new Type(Kind.LIST);
        range.addSub(new Type(Kind.INT));
        return range;
    }

    @Override
    public Type visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        Type list = visit(ctx.term());
        List<ChaiParser.ExpressionContext> indices = ctx.expression();

        // the list must be...a list
        if (list.getKind() != Kind.LIST) {
            throw new TypeMismatchException("Slices can only be applied to list types", ctx.getStart().getLine());
        }

        // any indices given must be integers (could be 0, 1, or 2)
        for (ChaiParser.ExpressionContext index : indices) {
            if(visit(index).getKind() != Kind.INT) {
                throw new TypeMismatchException("Index in slice expression must be of integer type", ctx.getStart().getLine());
            }
        }

        // the type is the same as the list itself
        return list;
    }

    @Override
    public Type visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        // LBRACK expression FOR IDNAME IN expression (IF expression)? RBRACK    # listcompTerm
        // find the name of the induction var and the list being pulled from
        String indVar = ctx.IDNAME().getText();
        Type list = visit(ctx.expression(1));

        // find the type of the induction variable based on the range which is given
        if (list.getKind() != Kind.LIST) {
            throw new TypeMismatchException("Source of list comprehension must be list type", ctx.getStart().getLine());
        }

        // add the induction variable into this scope
        putVar(indVar, list.getSubs().get(0), false, ctx.getStart().getLine());

        // type check the generating expression
        Type generator = visit(ctx.expression(0));
        
        // if there is a predicate, type check it, and make sure it's of bool type
        if (ctx.expression(2) != null) {
            Type predicate = visit(ctx.expression(2));
            if (predicate.getKind() != Kind.BOOL) {
                throw new TypeMismatchException("The predicate of a list comprehension must be boolean type", ctx.getStart().getLine());
            }
        }

        // remove the induction variable
        nixVar(indVar);

        // the type is a list of whatever the generating exprssion type is
        Type listcomp = new Type(Kind.LIST);
        listcomp.addSub(generator);
        return listcomp;
    }

    @Override
    public Type visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        Type collection = visit(ctx.term());
        Type index = visit(ctx.expression());

        switch (collection.getKind()) {
            case STRING:
                // the index must be an int
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Index into string must be integer type", ctx.getStart().getLine());
                }
                // it returns a string
                return new Type(Kind.STRING);

            case LIST:
                // the index must be an int
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Index into string must be integer type", ctx.getStart().getLine());
                }
                // it returns the type of the list
                return collection.getSubs().get(0);
            case DICT:
                // the index must be the first subtype
                if (!index.equals(collection.getSubs().get(0))) {
                    throw new TypeMismatchException("Type of dictionary key does not match", ctx.getStart().getLine());
                }
                // it returns the second subtype
                return collection.getSubs().get(1);

            default:
                throw new TypeMismatchException("Unsupported collection type in index expression", ctx.getStart().getLine());
        }
    }

    @Override
    public Type visitEmptydictLiteralTerm(ChaiParser.EmptydictLiteralTermContext ctx) {
        return new Type(Kind.DICT);
    }

    @Override
    public Type visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        // get the items in the set
        List<ChaiParser.ExpressionContext> entries = ctx.expression();

        // get the type for the first entry
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("Set literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // it's a set of whatever the thing is then
        Type set = new Type(Kind.SET);
        set.addSub(first);
        return set;
    }

    @Override
    public Type visitListLiteralTerm(ChaiParser.ListLiteralTermContext ctx) {
        // get the items in the list
        List<ChaiParser.ExpressionContext> entries = ctx.expression();

        // if the list is empty return list w/o subtype
        if (entries.size() == 0) {
            return new Type(Kind.LIST);
        }

        // get the type for the first entry
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("List literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // it's a list of whatever the thing is then
        Type lst = new Type(Kind.LIST);
        lst.addSub(first);
        return lst;
    }

    @Override
    public Type visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        // make a tuple type
        Type tups = new Type(Kind.TUPLE);

        // add all the things into it
        for (ChaiParser.ExpressionContext expr : ctx.expression()) {
            tups.addSub(visit(expr));
        }

        return tups;
    }

    @Override
    public Type visitDictLiteralTerm(ChaiParser.DictLiteralTermContext ctx) {
        List<ChaiParser.DictentryContext> entries = ctx.dictentry();

        // get the dict type for the first entry here
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("Dictionary literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // return dictionary type
        return first;
    }

    @Override
    public Type visitDictentry(ChaiParser.DictentryContext ctx) {
        // visit both of the expressions
        Type dict = new Type(Kind.DICT);
        dict.addSub(visit(ctx.expression(0)));
        dict.addSub(visit(ctx.expression(1)));
        return dict;
    }

    @Override
    public Type visitIntLiteral(ChaiParser.IntLiteralContext ctx) {
        return new Type(Kind.INT);
    }

    @Override
    public Type visitFloatLiteral(ChaiParser.FloatLiteralContext ctx) {
        return new Type(Kind.FLOAT);
    }

    @Override
    public Type visitStringLiteral(ChaiParser.StringLiteralContext ctx) {
        return new Type(Kind.STRING);
    }

    @Override
    public Type visitTrueLiteral(ChaiParser.TrueLiteralContext ctx) {
        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitFalseLiteral(ChaiParser.FalseLiteralContext ctx) {
        return new Type(Kind.BOOL);
    }
	@Override public Type visitParensTerm(ChaiParser.ParensTermContext ctx) {
        return visit(ctx.expression());
    }
}
