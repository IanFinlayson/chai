package net.ianfinlayson.chai;

import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.v4.runtime.tree.TerminalNode;

// these "exceptions" are used to deal with the corresponding control flow constructs
// this is a little hacky, but the simplest way to do this during the tree-walking
class BreakException extends RuntimeException {}
class ContinueException extends RuntimeException {}
class FunctionReturn extends RuntimeException {
    private Value val;
    
    public FunctionReturn(Value val) {
        this.val = val;
    }
    
    public Value getVal() {
        return val;
    }
}

// variables can be made constant, so this combines the value with that info
class Variable {
    public Value value;
    public boolean constant;
}

public class Executor extends ChaiParserBaseVisitor<Value> {
    private Scanner input = new Scanner(System.in);
    private Stack<HashMap<String, Variable>> stack = new Stack<>();
    private HashMap<String, Variable> globals = new HashMap<>();

    // this maps function name to the list of stmts comprising it
    private HashMap<String, ChaiParser.FunctiondefContext> functions = new HashMap<>();

    // call the main function for the program
    public void callMain() {
        if (functions.get("main") == null) {
            throw new RuntimeException("No main function found");
        }
        
        // make a stack frame and call it
        stack.push(new HashMap<String, Variable>());
        visit(functions.get("main").statements());
        stack.pop();
    }

    // load a variable -- we check the top function first, then globals
    private Value loadVar(String name) {
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                return stack.peek().get(name).value;
            }
        }

        if (globals.get(name) != null) {
            return globals.get(name).value;
        }
        
        return null;
    }

    // put a var -- again into the top function if there is one, or global
    private void putVar(String name, Value val, boolean constant) {
        // check if it exists first
        Variable exiting = null;
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                exiting = stack.peek().get(name);
            }
        }
        if (exiting == null && globals.get(name) != null) {
            exiting = globals.get(name);
        }

        // if it exists and is constant, error
        if (exiting != null && exiting.constant) {
            throw new RuntimeException("Constant value being re-assigned");
        }

        // keep track of the constant information and val
        Variable v = new Variable();
        v.constant = constant;
        v.value = val;

        // actually write it into correct scope
        if (!stack.empty()) {
            stack.peek().put(name, v);
        } else {
            globals.put(name, v);
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
                putVar(name, val, false);
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
                switch (destination.getType()) {
                    // TODO when we have dicts and sets, we'll need to add those
                    case LIST:
                        ArrayList<Value> list = destination.toList();
                        Value index = indices.get(i);
                        destination = list.get(index.toInt());
                        break;
                }
            }
            
            // do the actual set now
            switch (destination.getType()) {
                // TODO when we have dicts and sets, we'll need to add those
                case LIST:
                    ArrayList<Value> list = destination.toList();
                    Value index = indices.get(0);
                    list.set(index.toInt(), val);
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
        boolean constant = ctx.LET() != null;
        String name = ctx.IDNAME().getText();
        Value val = visit(ctx.expression());

        // make sure this variable does not already exist
        if (loadVar(name) != null) {
            throw new RuntimeException("Variable '" + name + "' already exists");
        }

        // add this as a variable
        putVar(name, val, constant);
        return null;
    }







	@Override
    public Value visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        // TODO
        return visitChildren(ctx);
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

        Iterator it = new Iterator(collection);
        while (!it.done()) {
            // assign the var, run the stmts
            putVar(inductionVar, it.next(), false);
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

	@Override
    public Value visitModassign(ChaiParser.ModassignContext ctx) {
        // TODO, how to do these cleanly?

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUSASSIGN:
            case ChaiLexer.MINUSASSIGN:
            case ChaiLexer.TIMESASSIGN:
            case ChaiLexer.DIVASSIGN:
            case ChaiLexer.MODASSIGN:
            case ChaiLexer.LSHIFTASSIGN:
            case ChaiLexer.RSHIFTASSIGN:
            case ChaiLexer.BITANDASSIGN:
            case ChaiLexer.BITORASSIGN:
            case ChaiLexer.BITXORASSIGN:
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




	@Override
    public Value visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // grab the name and look up the function
        String name = ctx.IDNAME().getText();
        
        // here we just check for our library functions
        switch (name) {
            case "print": libraryPrint(ctx.arglist()); return null;
        }

        // search for user-defined function
        ChaiParser.FunctiondefContext func = functions.get(name);
        if (func == null) {
            throw new RuntimeException("Function " + name + " not found");
        }

        // this function makes a new symbol table for locals
        HashMap<String, Variable> scope = new HashMap<>();

        // next we need to evaluate expressions in args into formals

        // if one side has args and the other not, that's a problem
        if (ctx.arglist() == null && func.paramlist() != null) {
            throw new RuntimeException("Function " + name + " expects arguments, but none given");
        } else if (ctx.arglist() != null && func.paramlist() == null) {
            throw new RuntimeException("Function " + name + " does not expects arguments, but was given some");
        }

        // TODO pass the args (including keyword ones!)
        // if both sides have args, do the thing
//       if (ctx.args() != null && func.formals() != null) {
//           List<ChaiParser.ExpressionContext> exprs = ctx.args().expression();
//           List<TerminalNode> args = func.formals().IDNAME();
//
//           // check for arity mismatch
//           if (exprs.size() != args.size()) {
//               throw new RuntimeException("Error: Function " + name + " expects " + args.size()
//                       + " parameters, but was given " + exprs.size() + ".");
//           }
//
//           // go through each argument
//           for (int i = 0; i < exprs.size(); i++) {
//               // just evaluate each one and put it in
//               String vname = args.get(i).getText();
//               Value value = visit(exprs.get(i));
//               scope.put(vname, value);
//           }
//       }
        
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
            return new Value(lhs.toInt() << rhs.toInt());
        } else {
            return new Value(lhs.toInt() >> rhs.toInt());
        }
    }

	@Override
    public Value visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(lhs.toInt() | rhs.toInt());
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
        return new Value(lhs.toInt() ^ rhs.toInt());
    }

	@Override
    public Value visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(lhs.toInt() & rhs.toInt());
    }

	@Override
    public Value visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        
        // we break it down to just less and equals
        switch (ctx.op.getType()) {
            case ChaiLexer.LESS:
                return new Value(lhs.less(rhs));
            case ChaiLexer.GREATER:
                return new Value(rhs.less(lhs));
            case ChaiLexer.LESSEQ:
                return new Value(!rhs.less(lhs));
            case ChaiLexer.GREATEREQ:
                return new Value(!lhs.less(rhs));
            case ChaiLexer.EQUALS:
                return new Value(lhs.equals(rhs));
            case ChaiLexer.NOTEQUALS:
                return new Value(!lhs.equals(rhs));
        }
        throw new RuntimeException("This should not happen, no comparison op found");
    }

    // so as to not have this in both the following methods
    private boolean inCollection(Value target, Value collection) {
        // TODO add dicts sets and tuples when we get them
        switch (collection.getType()) {
            case STRING:
                String needle = target.toString();
                String haystack = collection.toString();
                return haystack.indexOf(needle) != -1;
            case LIST:
                for (Value v : collection.toList()) {
                    if (v.equals(target)) {
                        return true;
                    }
                }
                return false;
        }
    
        throw new RuntimeException("invalid type passed to inCollection");
    }

	@Override
    public Value visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(!inCollection(lhs, rhs));
    }
	
	@Override
    public Value visitInExpression(ChaiParser.InExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        return new Value(inCollection(lhs, rhs));
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
        return lhs.pow(rhs);
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
            return lhs.plus(rhs);
        } else {
            return lhs.minus(rhs);
        }
    }

	@Override
    public Value visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        Value lhs = visit(ctx.expression(0));
        Value rhs = visit(ctx.expression(1));
        
        if (ctx.op.getType() == ChaiLexer.TIMES) {
            return lhs.times(rhs);
        } else if (ctx.op.getType() == ChaiLexer.DIVIDE) {
            return lhs.divide(rhs);
        } else {
            return lhs.modulo(rhs);
        }
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

        return new Value(result);
    }

	@Override
    public Value visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        Value val = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUS:
                return val;
            case ChaiLexer.MINUS:
                if (val.getType() == Type.INT) {
                    return new Value(-val.toInt());
                } else if (val.getType() == Type.FLOAT) {
                    return new Value(-val.toFloat());
                }
            case ChaiLexer.COMPLEMENT:
                return new Value(~val.toInt());
        }
           
        return visitChildren(ctx);
    }

	@Override
    public Value visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        // TODO
        return visitChildren(ctx);
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

        return new Value(range);
    }

	@Override
    public Value visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        Value index = visit(ctx.expression());
        Value list = visit(ctx.term());

        ArrayList<Value> vals = list.toList();
        int num = index.toInt();
        if (num >= vals.size()) {
            throw new RuntimeException("List index out of range");
        }

        return vals.get(num);
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
        
        return new Value(list);
    }

	@Override
    public Value visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitDicLiteralTerm(ChaiParser.DicLiteralTermContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitDictentry(ChaiParser.DictentryContext ctx) {
        // TODO
        return visitChildren(ctx);
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
                    if (kwval.getType() != Type.STRING) {
                        throw new RuntimeException("'end' argument must be a String");
                    } else {
                        end = kwval.toString();
                    }
                } else if (kwname.equals("sep")) {
                    if (kwval.getType() != Type.STRING) {
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


