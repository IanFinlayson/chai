package net.ianfinlayson.chai;

import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.CharStream;


// these "exceptions" are used to deal with the corresponding control flow constructs
// this is a little hacky, but the simplest way to do this during the tree-walking
class BreakException extends RuntimeException {}

class ContinueException extends RuntimeException {}

class AssertException extends RuntimeException {
    private String reason;
    public AssertException(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

class FunctionReturn extends RuntimeException {
    private Value val;

    public FunctionReturn(Value val) {
        this.val = val;
    }

    public Value getVal() {
        return val;
    }
}

public class Executor extends ChaiParserBaseVisitor<Value> {
    //private Scanner input = new Scanner(System.in);
    private Stack<HashMap<String, Value>> stack = new Stack<>();
    private HashMap<String, Value> globals = new HashMap<>();
    private CharStream stream;

    // we keep a reference to the char stream for reporting the text that produces
    // errors and assertion failures
    public Executor(CharStream stream) {
        this.stream = stream;
    }

    // this maps function name to the list of stmts comprising it
    private HashMap<String, ChaiParser.FunctiondefContext> functions = new HashMap<>();

    // call the main function for the program
    public void callMain() {
        if (functions.get("main") == null) {
            throw new RuntimeException("No main function found");
        }

        // make a stack frame and call it
        stack.push(new HashMap<String, Value>());
        try {
            visit(functions.get("main").statements());
        } catch (AssertException e) {
            System.out.println("Asserton failed: " + e.getReason());
        }
        stack.pop();
    }

    // load a variable -- we check the top function first, then globals
    private Value loadVar(String name) {
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                return stack.peek().get(name);
            }
        }

        if (globals.get(name) != null) {
            return globals.get(name);
        }

        return null;
    }

    // put a var -- again into the top function if there is one, or global
    private void putVar(String name, Value val) {
        // check if it exists first
        Value exiting = null;
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                exiting = stack.peek().get(name);
            }
        }
        if (exiting == null && globals.get(name) != null) {
            exiting = globals.get(name);
        }

        // actually write it into correct scope
        if (!stack.empty()) {
            stack.peek().put(name, val);
        } else {
            globals.put(name, val);
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

	@Override
    public Value visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // simply put the function into the function table
        String name = ctx.IDNAME().getText();
        functions.put(name, ctx);
        return null;
    }

    // assign a chai value into an l-value
    private void doAssign(ChaiParser.LvalueContext lhs, Value val) {
        // if it's just an id, do that
        if (lhs instanceof ChaiParser.JustIDContext) {
            String name = ((ChaiParser.JustIDContext) lhs).IDNAME().getText();

            // ensure it exists first
            if (loadVar(name) == null) {
                throw new RuntimeException("Variable '" + name + "' assigned without being declared");
            } else {
                putVar(name, val);
            }
        } else {
            // it's a list/set/dict assignment, and is possibly nested

            // loop until the lvalue is just an id, collecting the values
            // used as indices in a list
            ArrayList<Value> indices = new ArrayList<>();
            while (!(lhs instanceof ChaiParser.JustIDContext)) {
                ChaiParser.NestedLvalueContext n = (ChaiParser.NestedLvalueContext) lhs;

                indices.add(visit(n.expression()));
                lhs = n.lvalue();
            }

            String name = ((ChaiParser.JustIDContext) lhs).IDNAME().getText();

            // apply all but FIRST one
            Value destination = loadVar(name);
            for (int i = indices.size() - 1; i > 0; i--) {
                switch (destination.getKind()) {
                    case LIST:
                    case TUPLE:
                        ArrayList<Value> list = destination.toList();
                        Value index = indices.get(i);
                        destination = list.get(index.toInt());
                        break;
                    case DICT:
                        HashMap<Value, Value> dict = destination.toDict();
                        index = indices.get(i);
                        destination = dict.get(index);
                        break;
                }
            }

            // do the actual set now
            switch (destination.getKind()) {
                case LIST:
                case TUPLE:
                    ArrayList<Value> list = destination.toList();
                    Value index = indices.get(0);
                    list.set(index.toInt(), val);
                    break;
                case DICT:
                    HashMap<Value, Value> dict = destination.toDict();
                    index = indices.get(0);
                    dict.put(index, val);
                    break;
            }
        }
    }

	@Override
    public Value visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // get the thing we are assigning
        Value val = visit(ctx.expression());

        // get the lvalue we are writing into
        ChaiParser.LvalueContext lhs = ctx.lvalue();

        // do the assign
        doAssign(lhs, val);
        return null;
    }

	@Override
    public Value visitVarStatement(ChaiParser.VarStatementContext ctx) {
        // get the parts out
        String name = ctx.IDNAME().getText();
        Value val = visit(ctx.expression());

        // add this as a variable (or set it again if we're in a loop)
        putVar(name, val);
        return null;
    }

	@Override
    public Value visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        Value val = visit(ctx.expression());
        if (!val.toBool()) {
            int a = ctx.expression().start.getStartIndex();
            int b = ctx.expression().stop.getStopIndex();
            Interval interval = new Interval(a,b);
            throw new AssertException(stream.getText(interval));
        }

        return null;
    }

	@Override
    public Value visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        Value retVal = null;
        if (ctx.expression() != null) {
            retVal = visit(ctx.expression());
        }

        throw new FunctionReturn(retVal);
    }

	@Override
    public Value visitContinueStatement(ChaiParser.ContinueStatementContext ctx) {
        throw new ContinueException();
    }

	@Override
    public Value visitBreakStatement(ChaiParser.BreakStatementContext ctx) {
        throw new BreakException();
    }

	@Override
    public Value visitForStatement(ChaiParser.ForStatementContext ctx) {
        String inductionVar = ctx.IDNAME().getText();
        Value collection = visit(ctx.expression());

        // make sure the var doesn't already exist
        if (loadVar(inductionVar) != null) {
            throw new RuntimeException("Induction variable exists in outer scope");
        }

        Stepper it = new Stepper(collection);
        while (!it.done()) {
            // assign the var, run the stmts
            putVar(inductionVar, it.next());
            visit(ctx.statements());
        }

        // remove the variable from the scope
        nixVar(inductionVar);

        return null;
    }

	@Override
    public Value visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        // evaluate the condition
        Value condition = visit(ctx.expression());

        // while it's true
        while (condition.toBool()) {
            try {
                // visit the statments
                visit(ctx.statements());
            } catch (ContinueException e) {
                // do nothing, we will fall down to checking the condition again
            } catch (BreakException e) {
                // this was easy
                break;
            }

            // re-eval condition
            condition = visit(ctx.expression());
        }

        return null;
    }


    // this recurses through the destructure expression and returns if we do it or not
    // and builds out new variables being introduced
    private boolean walkDestructures(ChaiParser.DestructureContext destr, ArrayList<String> newvars, Value expr) {
        // we need to see what kind of destr it is
        // we check the types against expr and also introduce variables if needed
        if (destr instanceof ChaiParser.LiteralDestrContext) {
            // literal, check if it's equal
            ChaiParser.LiteralDestrContext lit = (ChaiParser.LiteralDestrContext) destr;
            return expr.equals(visit(lit.literal()));
        } else if (destr instanceof ChaiParser.TupleDestrContext) {
            // LPAREN (destructure COMMA)+ destructure RPAREN
            // check for equality of tuple positions, recursively

            ChaiParser.TupleDestrContext tups = (ChaiParser.TupleDestrContext) destr;
            List<ChaiParser.DestructureContext> terms = tups.destructure();

            boolean allMatch = true;
            ArrayList<String> trynewvars = new ArrayList<>();
            for (int i = 0; i < terms.size(); i++) {
                allMatch = allMatch && walkDestructures(terms.get(i), trynewvars, expr.toList().get(i));
            }

            // if all tuple terms are equal, we got a match
            // otherwise, we need to remove any vars we created in the process
            if (allMatch) {
                for (String nv : trynewvars) {
                    newvars.add(nv);
                }
                return true;
            } else {
                for (String scrub : trynewvars) {
                    nixVar(scrub);
                }
                return false;
            }

        } else if (destr instanceof ChaiParser.ListDestrContext) {
            // LBRACK (destructure (COMMA destructure)*)? RBRACK    # listDestr

            ChaiParser.ListDestrContext ls = (ChaiParser.ListDestrContext) destr;
            List<ChaiParser.DestructureContext> terms = ls.destructure();

            // if different size, it can't match
            if (terms.size() != expr.toList().size()) {
                return false;
            }

            // check if all the thingies match
            boolean allMatch = true;
            ArrayList<String> trynewvars = new ArrayList<>();
            for (int i = 0; i < terms.size(); i++) {
                allMatch = allMatch && walkDestructures(terms.get(i), trynewvars, expr.toList().get(i));
            }

            // if all list terms are equal, we got a match
            // otherwise, we need to remove any vars we created in the process
            if (allMatch) {
                for (String nv : trynewvars) {
                    newvars.add(nv);
                }
                return true;
            } else {
                for (String scrub : trynewvars) {
                    nixVar(scrub);
                }
                return false;
            }

        } else if (destr instanceof ChaiParser.IdDestrContext) {
            // ID: introduce it with this given value
            String name = ((ChaiParser.IdDestrContext) destr).IDNAME().getText();
            putVar(name, expr);
            newvars.add(name);
            return true;
        } else if (destr instanceof ChaiParser.UscoreDestrContext) {
            // nothing to do here
            return true;
        } else if (destr instanceof ChaiParser.UnionDestrContext) {
            // TYPENAME destructure?
            // TODO add this when we actually get unions!
            return false;
        } else if (destr instanceof ChaiParser.ConsDestrContext) {
            // destructure (CONS destructure)+

            // grab the things being destructured
            ChaiParser.ConsDestrContext ls = (ChaiParser.ConsDestrContext) destr;
            List<ChaiParser.DestructureContext> terms = ls.destructure();

            // we loop through all but the last
            // each must match the subsequent value in the expr
            boolean allMatch = true;
            ArrayList<String> trynewvars = new ArrayList<>();
            int consies = 0;
            for (; consies < (terms.size() - 1); consies++) {
                // if there's nothin to match with, we have no match
                if (expr.toList().size() <= consies) {
                    allMatch = false;
                } else {
                    allMatch = allMatch && walkDestructures(terms.get(consies), trynewvars, expr.toList().get(consies));
                }
            }

            // we get the remainders of the list without the stuff cons'd in
            ArrayList<Value> remainders = new ArrayList<>();
            for (int j = consies; j < expr.toList().size(); j++) {
                remainders.add(expr.toList().get(j));
            }

            // now the LAST one must be matched against the remainder of the list...
            allMatch = allMatch && walkDestructures(terms.get(terms.size() - 1), trynewvars, new Value(remainders, false));

            // if we are good, go with it... else bail out
            if (allMatch) {
                for (String nv : trynewvars) {
                    newvars.add(nv);
                }
                return true;
            } else {
                for (String scrub : trynewvars) {
                    nixVar(scrub);
                }
                return false;
            }

        } else {
            throw new RuntimeException("Unhandled destructure type in match statement");
        }
    }

	@Override
    public Value visitMatchStatment(ChaiParser.MatchStatmentContext ctx) {
        // get the type of the expression
        Value expr = visit(ctx.expression());

        // for each case line
        for (ChaiParser.CaselineContext caseline : ctx.caseline()) {
            ChaiParser.DestructureContext destr = caseline.destructure();

            // we make a list of new variables we introduce in destructures,
            // so that we can remove them again at the end
            ArrayList<String> newvars = new ArrayList<>();

            // walk the destructures for this case line, and see if it can be matched
            if (walkDestructures(destr, newvars, expr)) {
                // type check the statements under it
                visit(caseline.statements());
                // remove variable destructures we've added
                for (String vari : newvars) {
                    nixVar(vari);
                }

                // only one case can be matched for each match
                break;
            }
        }

        return null;
    }










    // get the current value of an L-value, for modassign purposes
    private Value readLvalue(ChaiParser.LvalueContext lvalue) {
        if (lvalue instanceof ChaiParser.NestedLvalueContext) {
            Value collection = readLvalue(((ChaiParser.NestedLvalueContext) lvalue).lvalue());
            Value index = visit(((ChaiParser.NestedLvalueContext) lvalue).expression());

            if ((collection.getKind() == Kind.LIST) || (collection.getKind() == Kind.TUPLE)) {
                ArrayList<Value> vals = collection.toList();
                int num = index.toInt();
                if (num >= vals.size()) {
                    throw new RuntimeException("List index out of range");
                }

                return vals.get(num);
            } else if (collection.getKind() == Kind.DICT) {
                HashMap<Value, Value> dict = collection.toDict();
                if (dict.get(index) == null) {
                    throw new RuntimeException("Value not found in dictionary");
                }

                return dict.get(index);
            } else throw new RuntimeException("Unhandled indexable type");

        } else if (lvalue instanceof ChaiParser.JustIDContext) {
            return loadVar(((ChaiParser.JustIDContext) lvalue).IDNAME().getText());
        } else throw new RuntimeException("Unhadnlded l-value case");
    }

	@Override
    public Value visitModassign(ChaiParser.ModassignContext ctx) {
        // get the current value of the lvalue we need to modify, and thing to modify in
        Value current = readLvalue(ctx.lvalue());
        Value change = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUSASSIGN:
                doAssign(ctx.lvalue(), Operators.plus(current, change));
                break;
            case ChaiLexer.MINUSASSIGN:
                doAssign(ctx.lvalue(), Operators.minus(current, change));
                break;
            case ChaiLexer.TIMESASSIGN:
                doAssign(ctx.lvalue(), Operators.times(current, change));
                break;
            case ChaiLexer.DIVASSIGN:
                doAssign(ctx.lvalue(), Operators.divide(current, change));
                break;
            case ChaiLexer.MODASSIGN:
                doAssign(ctx.lvalue(), Operators.modulo(current, change));
                break;
            case ChaiLexer.INTDIVASSIGN:
                doAssign(ctx.lvalue(), Operators.intdivide(current, change));
                break;
            case ChaiLexer.LSHIFTASSIGN:
                doAssign(ctx.lvalue(), Operators.lshift(current, change));
                break;
            case ChaiLexer.RSHIFTASSIGN:
                doAssign(ctx.lvalue(), Operators.rshift(current, change));
                break;
            case ChaiLexer.BITANDASSIGN:
                doAssign(ctx.lvalue(), Operators.bitand(current, change));
                break;
            case ChaiLexer.BITORASSIGN:
                doAssign(ctx.lvalue(), Operators.bitor(current, change));
                break;
            case ChaiLexer.BITXORASSIGN:
                doAssign(ctx.lvalue(), Operators.bitxor(current, change));
                break;
        }

        return null;
    }

	@Override
    public Value visitIfstmt(ChaiParser.IfstmtContext ctx) {
        // evaluate the initial expression
        Value condition = visit(ctx.expression());

        // if it's true, do this stmt and bail
        if (condition.toBool()) {
            visit(ctx.statements());
            return null;
        }

        // now we go through any elif clauses there might be here
        for (ChaiParser.ElifclauseContext elif : ctx.elifclause()) {
            // check this one's condition
            condition = visit(elif.expression());

            // if it's true, do THIS stmt and bail
            if (condition.toBool()) {
                visit(elif.statements());
                return null;
            }
        }

        // now we check if there is an else, and if so, do that
        if (ctx.elseclause() != null) {
            visit(ctx.elseclause().statements());
        }

        return null;
    }

    // assign parameters for a function
    // this takes into account keyword style parameters and default values
    private void assignArgs(ChaiParser.ArglistContext args, ChaiParser.ParamlistContext formals, HashMap<String, Value> scope) {

        // step 1: go through args looking for named ones, and assign those into formals w/ same name
        // if name does not exist, that's an error
        if (args != null) {
            for (ChaiParser.ArgumentContext arg : args.argument()) {
                if (arg.IDNAME() != null) {
                    // get the value for it
                    Value val = visit(arg.expression());

                    // loop through formals looking for one with matching name
                    String formalName = "";
                    boolean done = false;
                    for (ChaiParser.ParamContext param : formals.param()) {
                        if (param instanceof ChaiParser.DefaultParamContext) {
                            formalName = ((ChaiParser.DefaultParamContext) (param)).IDNAME().getText();
                        } else if (param instanceof ChaiParser.NamedParamContext) {
                            formalName = ((ChaiParser.NamedParamContext) (param)).IDNAME().getText();
                        } else throw new RuntimeException("Unhandled formal type");

                        if (formalName.equals(arg.IDNAME().getText())) {
                            // assign it
                            scope.put(formalName, val);
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        throw new RuntimeException("Parameter named '" + arg.IDNAME().getText() + "' does not exist.");
                    }
                }
            }
        }

        // step 2: go through args looking for unnamed ones, and assign positionally to unassigned formals
        // if we have no unassigned formal, that's an error
        if (args != null) {
            for (ChaiParser.ArgumentContext arg : args.argument()) {
                if (arg.IDNAME() == null) {
                    // get the value for it
                    Value val = visit(arg.expression());

                    // find the first un-assigned parameter
                    String formalName = "";
                    for (ChaiParser.ParamContext param : formals.param()) {
                        if (param instanceof ChaiParser.DefaultParamContext) {
                            formalName = ((ChaiParser.DefaultParamContext) (param)).IDNAME().getText();
                        } else if (param instanceof ChaiParser.NamedParamContext) {
                            formalName = ((ChaiParser.NamedParamContext) (param)).IDNAME().getText();
                        } else throw new RuntimeException("Unhandled formal type");

                        if (scope.get(formalName) == null) {
                            break;
                        }
                    }

                    if (formalName.equals("")) {
                        throw new RuntimeException("Too many parameters given");
                    }

                    // do the assignment
                    scope.put(formalName, val);
                }
            }
        }

        // step 3: go through formals and assign un-assigned ones their default params
        for (ChaiParser.ParamContext param : formals.param()) {
            String formalName = "";
            if (param instanceof ChaiParser.DefaultParamContext) {
                formalName = ((ChaiParser.DefaultParamContext) (param)).IDNAME().getText();
                // if not already assigned
                if (scope.get(formalName) == null) {
                    // eval the default param and stick it into the scope
                    Value val = visit(((ChaiParser.DefaultParamContext) (param)).term());
                    scope.put(formalName, val);
                }
            }
        }

        // step 4: look for unassigned params.  For now this is an error, later we will make them
        // curried functions!
        String formalName = "";
        for (ChaiParser.ParamContext param : formals.param()) {
            if (param instanceof ChaiParser.DefaultParamContext) {
                formalName = ((ChaiParser.DefaultParamContext) (param)).IDNAME().getText();
            } else if (param instanceof ChaiParser.NamedParamContext) {
                formalName = ((ChaiParser.NamedParamContext) (param)).IDNAME().getText();
            } else throw new RuntimeException("Unhandled formal type");

            if (scope.get(formalName) == null) {
                throw new RuntimeException("Unassigned parameter '" + formalName + "' in function call");
            }
        }
    }

	@Override
    public Value visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // grab the name and look up the function
        String name = ctx.IDNAME().getText();

        // here we just check for our library functions
        switch (name) {
            case "print": libraryPrint(ctx.arglist()); return null;
            case "input": return libraryInput(ctx.arglist());
            case "len": return libraryLen(ctx.arglist());
        }

        // search for user-defined function
        ChaiParser.FunctiondefContext func = functions.get(name);
        if (func == null) {
            throw new RuntimeException("Function " + name + " not found");
        }

        // this function makes a new symbol table for locals
        HashMap<String, Value> scope = new HashMap<>();

        // next we need to evaluate expressions in args into formals, if there are formals
        if (func.paramlist() != null) {
            assignArgs(ctx.arglist(), func.paramlist(), scope);
        }

        // add this scope to the stack
        stack.push(scope);

        // now run the stmts for it, until a return
        Value returnVal = null;
        try {
            visit(func.statements());
        } catch (FunctionReturn ret) {
            returnVal = ret.getVal();
        }

        // finally pop off this scope and return
        stack.pop();
        return returnVal;
    }

	@Override
    public Value visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        if (ctx.op.getType() == ChaiLexer.LSHIFT) {
            return Operators.lshift(lhs, rhs);
        } else {
            return Operators.rshift(lhs, rhs);
        }
    }

	@Override
    public Value visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return Operators.bitor(lhs, rhs);
    }

	@Override
    public Value visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        Value expr = visit(ctx.expression());
        return new Value(!expr.toBool());
    }

	@Override
    public Value visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return Operators.bitxor(lhs, rhs);
    }

	@Override
    public Value visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return Operators.bitand(lhs, rhs);
    }

	@Override
    public Value visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        // we break it down to just less and equals
        switch (ctx.op.getType()) {
            case ChaiLexer.LESS:
                return new Value(Operators.less(lhs, rhs));
            case ChaiLexer.GREATER:
                return new Value(Operators.less(rhs, lhs));
            case ChaiLexer.LESSEQ:
                return new Value(!Operators.less(rhs, lhs));
            case ChaiLexer.GREATEREQ:
                return new Value(!Operators.less(lhs, rhs));
            case ChaiLexer.EQUALS:
                return new Value(Operators.equals(lhs, rhs));
            case ChaiLexer.NOTEQUALS:
                return new Value(!Operators.equals(lhs, rhs));
        }
        throw new RuntimeException("This should not happen, no comparison op found");
    }


	@Override
    public Value visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(!Operators.in(lhs, rhs));
    }

	@Override
    public Value visitInExpression(ChaiParser.InExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(Operators.in(lhs, rhs));
    }

	@Override
    public Value visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        // evaluate left hand side (we do short-circuit)
        Value lhs = visit(ctx.expression(0));
        if (lhs.toBool()) {
            return new Value(true);
        }

        // evaluate right hand side
        Value rhs = visit(ctx.expression(1));
        if (rhs.toBool()) {
            return new Value(true);
        }

        return new Value(false);
    }

	@Override
    public Value visitPowerExpression(ChaiParser.PowerExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return Operators.pow(lhs, rhs);
    }

	@Override
    public Value visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        // evaluate left hand side (short-circuit style)
        Value lhs = visit(ctx.expression(0));
        if (!lhs.toBool()) {
            return new Value(false);
        }

        // evaluate right hand side
        Value rhs = visit(ctx.expression(1));
        if (!rhs.toBool()) {
            return new Value(false);
        }

        return new Value(true);
    }

	@Override
    public Value visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        // evaluate the condition, which is the middle expression
        Value condition = visit(ctx.expression(1));

        // if true, evaluate first, else third
        if (condition.toBool()) {
            return visit(ctx.expression(0));
        } else {
            return visit(ctx.expression(2));
        }
    }

	@Override
    public Value visitPlusMinusExpression(ChaiParser.PlusMinusExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        if (ctx.op.getType() == ChaiLexer.PLUS) {
            return Operators.plus(lhs, rhs);
        } else {
            return Operators.minus(lhs, rhs);
        }
    }

	@Override
    public Value visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        if (ctx.op.getType() == ChaiLexer.TIMES) {
            return Operators.times(lhs, rhs);
        } else if (ctx.op.getType() == ChaiLexer.DIVIDE) {
            return Operators.divide(lhs, rhs);
        } else if (ctx.op.getType() == ChaiLexer.INTDIV) {
            return Operators.intdivide(lhs, rhs);
        } else if (ctx.op.getType() == ChaiLexer.MODULUS) {
            return Operators.modulo(lhs, rhs);
        } else throw new RuntimeException("Unhandled times/div operator");
    }

	@Override
    public Value visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        ArrayList<Value> result = new ArrayList<>();
        result.add(lhs);

        for (Value v : rhs.toList()) {
            result.add(v);
        }

        return new Value(result, false);
    }

	@Override
    public Value visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        Value val = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUS:
                return val;
            case ChaiLexer.MINUS:
                if (val.getKind() == Kind.INT) {
                    return new Value(-val.toInt());
                } else if (val.getKind() == Kind.FLOAT) {
                    return new Value(-val.toFloat());
                }
            case ChaiLexer.COMPLEMENT:
                return new Value(~val.toInt());
        }

        return visitChildren(ctx);
    }

	@Override
    public Value visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        // make the list we will be generating
        ArrayList<Value> list = new ArrayList<>();

        // get the variable and make sure it does not already exist
        String genVar = ctx.IDNAME().getText();
        if (loadVar(genVar) != null) {
            throw new RuntimeException("Induction variable exists in outer scope");
        }

        // there are 2 or 3 expressions in this
        // first is the thing we are generating
        // second is list we pull from
        // third (if there) is the condition test
        List<ChaiParser.ExpressionContext> exprs = ctx.expression();

        // first we get the list of all the starting values
        ArrayList<Value> starters = visit(exprs.get(1)).toList();

        // next we go through each thing
        for (Value val : starters) {
            // make this variable and give it the value
            putVar(genVar, val);

            // check the condition, if it exists
            if (exprs.size() == 3) {
                if (!visit(exprs.get(2)).toBool()) {
                    // skip this one!
                    nixVar(genVar);
                    continue;
                }
            }

            // evaluate the first expression with this var
            Value thing = visit(exprs.get(0));

            // add it to the list
            list.add(thing);

            // remove the variable from the scope
            nixVar(genVar);
        }

        return new Value(list, false);
    }

	@Override
    public Value visitIdTerm(ChaiParser.IdTermContext ctx) {
        String name = ctx.IDNAME().getText();
        Value val = loadVar(name);
        if (val == null) {
            throw new RuntimeException("Could not load variable " + name);
        } else {
            return val;
        }
    }

    @Override
    public Value visitListRangeTerm(ChaiParser.ListRangeTermContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));

        ArrayList<Value> range = new ArrayList<>();
        int a = lhs.toInt(), b = rhs.toInt();
        if (a > b) {
            for (int i = a; i >= b; i--) {
                range.add(new Value(i));
            }

        } else {
            for (int i = a; i <= b; i++) {
                range.add(new Value(i));
            }
        }

        return new Value(range, false);
    }

	@Override
    public Value visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        Value index = visit(ctx.expression());
        Value collection = visit(ctx.term());

        if (collection.getKind() == Kind.STRING) {
            String str = collection.toString();
            int num = index.toInt();

            if (num >= str.length()) {
                throw new RuntimeException("String index out of range");
            }

            return new Value(Character.toString(str.charAt(num)));
        } else if (collection.getKind() == Kind.LIST) {
            ArrayList<Value> vals = collection.toList();
            int num = index.toInt();
            if (num >= vals.size()) {
                throw new RuntimeException("List index out of range");
            }

            return vals.get(num);
        } else if (collection.getKind() == Kind.DICT) {
            HashMap<Value, Value> dict = collection.toDict();
            Value result = dict.get(index);
            if (result == null) {
                throw new RuntimeException("Value " + index + " not found in dictionary");
            } else {
                return result;
            }
        } else throw new RuntimeException("Illegal type in index operation");
    }

	@Override public Value visitParensTerm(ChaiParser.ParensTermContext ctx) {
        return visit(ctx.expression());
    }

	@Override
    public Value visitListLiteralTerm(ChaiParser.ListLiteralTermContext ctx) {
        // this represents a list literal like [1, 2, 3] etc.
        ArrayList<Value> list = new ArrayList<>();

        for (ChaiParser.ExpressionContext expr : ctx.expression()) {
            list.add(visit(expr));
        }

        return new Value(list, false);
    }

	@Override
    public Value visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        // this represents a tuple literal like (1, "hey", True) etc.
        // these are pretty much identical to lists from this interprerters pov
        // (type checker only thing that cares about the diff)
        ArrayList<Value> tuple = new ArrayList<>();

        for (ChaiParser.ExpressionContext expr : ctx.expression()) {
            tuple.add(visit(expr));
        }

        return new Value(tuple, true);
    }

	@Override
    public Value visitDictLiteralTerm(ChaiParser.DictLiteralTermContext ctx) {
        // make a dictionary with all of the entries
        HashMap<Value, Value> dict = new HashMap<>();

        for (ChaiParser.DictentryContext entry : ctx.dictentry()) {
            dict.put(visit(entry.expression(0)), visit(entry.expression(1)));
        }

        return new Value(dict);
    }

	@Override public Value visitEmptydictLiteralTerm(ChaiParser.EmptydictLiteralTermContext ctx) {
        return new Value(new HashMap<Value, Value>());
    }

	@Override
    public Value visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        // grab the list, if not a list type checker failed
        ArrayList<Value> list = visit(ctx.term()).toList();

        // grab the expressions, there are 0, 1, or 2
        List<ChaiParser.ExpressionContext> exprs = ctx.expression();

        // pull out the beginning and ending indices, these are defualts
        int start = 0;
        int end = list.size();

        // if both are here, grab both
        if (exprs.size() == 2) {
            start = visit(exprs.get(0)).toInt();
            end = visit(exprs.get(1)).toInt();
        }
        // if one is here, we must figure out which...
        else if (exprs.size() == 1) {
            // we check if the expr is before or after : so we know which side it is
            int expr_index = exprs.get(0).start.getStartIndex();
            int colon_index = + ctx.COLON().getSymbol().getStartIndex();

            if (expr_index < colon_index) {
                start = visit(exprs.get(0)).toInt();
            } else {
                end = visit(exprs.get(0)).toInt();
            }
        }

        // build our new list made out of what's in original
        ArrayList<Value> slice = new ArrayList<>();
        for (int i = start; i < end; i++) {
            slice.add(list.get(i));
        }

        return new Value(slice, false);
    }

	@Override
    public Value visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        HashSet<Value> set = new HashSet<>();

        for (ChaiParser.ExpressionContext expr : ctx.expression()) {
            set.add(visit(expr));
        }

        return new Value(set);
    }

	@Override
    public Value visitIntLiteral(ChaiParser.IntLiteralContext ctx) {
        return new Value(Integer.parseInt(ctx.INTVAL().getText()));
    }

	@Override
    public Value visitFloatLiteral(ChaiParser.FloatLiteralContext ctx) {
        return new Value(Double.parseDouble(ctx.FLOATVAL().getText()));
    }

	@Override
    public Value visitStringLiteral(ChaiParser.StringLiteralContext ctx) {
        // get it, but ditch the quotation marks
        String whole = ctx.STRINGVAL().getText();
        return new Value(whole.substring(1, whole.length() - 1));
    }

	@Override
    public Value visitTrueLiteral(ChaiParser.TrueLiteralContext ctx) {
        return new Value(true);
    }

	@Override
    public Value visitFalseLiteral(ChaiParser.FalseLiteralContext ctx) {
        return new Value(false);
    }

    // standard library functions appear below
    private Value libraryInput(ChaiParser.ArglistContext args) {
        String prompt = "";

        if (args != null && args.argument() != null) {
            if (args.argument().size() != 1) {
                throw new RuntimeException("input called with incorrect number of arguments");
            }

            ChaiParser.ArgumentContext arg = args.argument().get(0);
            if (arg.ASSIGN() != null) throw new RuntimeException("input has no keyword argument");

            Value p = visit(arg.expression());
            if (p.getKind() != Kind.STRING) throw new RuntimeException("Prompt to input must be a string");
            prompt = p.toString();
        }

        System.out.print(prompt);
        Scanner input = new Scanner(System.in);
        return new Value(input.nextLine());
    }

    private Value libraryLen(ChaiParser.ArglistContext args) {
        if (args == null || args.argument() == null || args.argument().size() != 1) {
            throw new RuntimeException("len takes exactly 1 argument");
        }

        ChaiParser.ArgumentContext arg = args.argument().get(0);
        if (arg.ASSIGN() != null) throw new RuntimeException("len has no keyword argument");

        Value collection = visit(arg.expression());

        int size = 0;
        switch (collection.getKind()) {
            case LIST:
            case TUPLE:
                size = collection.toList().size();
                break;
            case STRING:
                size = collection.toString().length();
                break;
            case DICT:
                size = collection.toDict().size();
                break;
            case SET:
                size = collection.toSet().size();
                break;
            default:
                throw new RuntimeException("Cannot take length of scalar");
        }

        return new Value(size);
    }

    private void libraryPrint(ChaiParser.ArglistContext args) {
        String end = "\n";
        String sep = " ";
        boolean first = true;

        // we put the things to print in a list so we need only go through args once
        ArrayList<Value> toprint = new ArrayList<>();

        // if there are no arguments, just print a \n and return!
        if (args == null || args.argument() == null) {
            System.out.print("\n");
            return;
        }

        // for each argument that we are given
        for (ChaiParser.ArgumentContext arg : args.argument()) {
            // if it's a keyword argument
            if (arg.ASSIGN() != null) {
                String kwname = arg.IDNAME().getText();
                Value kwval = visit(arg.expression());

                if (kwname.equals("end")) {
                    if (kwval.getKind() != Kind.STRING) {
                        throw new RuntimeException("'end' argument must be a String");
                    } else {
                        end = kwval.toString();
                    }
                } else if (kwname.equals("sep")) {
                    if (kwval.getKind() != Kind.STRING) {
                        throw new RuntimeException("'sep' argument must be a String");
                    } else {
                        sep = kwval.toString();
                    }
                }
            } else {
                // add to our printing list
                toprint.add(visit(arg.expression()));
            }
        }

        // now actually print the things
        for (Value val : toprint) {
            // print the separator after first one
            if (!first) {
                System.out.print(sep);
            } else {
                first = false;
            }

            System.out.print(val);
        }

        // print the ending
        System.out.print(end);
    }
}

