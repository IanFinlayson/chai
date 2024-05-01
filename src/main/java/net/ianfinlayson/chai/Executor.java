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

public class Executor extends ChaiParserBaseVisitor<Value> {
    private Scanner input = new Scanner(System.in);
    private Stack<HashMap<String, Value>> stack = new Stack<>();
    private HashMap<String, Value> globals = new HashMap<>();

    // this maps function name to the list of stmts comprising it
    private HashMap<String, ChaiParser.FunctiondefContext> functions = new HashMap<>();

    // call the main function for the program
    public void callMain() {
        if (functions.get("main") == null) {
            throw new RuntimeException("No main function found");
        }
        visit(functions.get("main").statements());
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
        if (!stack.empty()) {
            stack.peek().put(name, val);
        } else {
            globals.put(name, val);
        }
    }

	@Override
    public Value visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // simply put the function into the function table
        String name = ctx.IDNAME().getText();
        functions.put(name, ctx);
        return null;
    }

	@Override
    public Value visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // get the thing we are assigning
        Value val = visit(ctx.expression());

        // get the lvalue we are writing into
        ChaiParser.LvalueContext lhs = ctx.lvalue();
        
        // if it's just an id, do that
        if (lhs instanceof ChaiParser.JustIDContext) {
            String name = ((ChaiParser.JustIDContext) lhs).IDNAME().getText();

            // ensure it exists first
            if (loadVar(name) == null) {
                throw new RuntimeException("Variable '" + name + "' assigned without being declared");
            } else {
                putVar(name, val);
            }
        }
        
        // TODO also handle (possibly multiple) list/set/dict assignments!

        return null;
    }
    
	@Override
    public Value visitVarStatement(ChaiParser.VarStatementContext ctx) {
        // TODO add the type information from this!
        // TODO also make variables actually constant!

        // get the parts out
        boolean constant = ctx.LET() != null;
        String name = ctx.IDNAME().getText();
        Value val = visit(ctx.expression());

        // make sure this variable does not already exist
        if (loadVar(name) != null) {
            throw new RuntimeException("Variable '" + name + "' already exists");
        }

        // add this as a variable
        putVar(name, val);
        return null;
    }







	@Override
    public Value visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        // TODO
        return visitChildren(ctx);
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
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        ChaiParser.ExpressionContext expr = ctx.expression();
        ChaiParser.StatementsContext stmt = ctx.statements();
        
        Value condition = visit(expr);
        if (condition.getType() != Type.BOOL) {
            throw new TypeMismatchException("Type of while condition must be boolean");
        }

        while(condition.toBool() == true) {
            try {
                visit(stmt);
            } catch (ContinueException e) {
                // do nothing, we will fall down to checking the condition again
            } catch (BreakException e) {
                // this was easy
                break;
            }

            condition = visit(expr);
        }

        return null;
    }

	@Override
    public Value visitModassign(ChaiParser.ModassignContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitIfstmt(ChaiParser.IfstmtContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitElifclause(ChaiParser.ElifclauseContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitElseclause(ChaiParser.ElseclauseContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // grab the name and look up the function
        String name = ctx.IDNAME().getText();
        
        // here we just check for our library functions
        switch (name) {
            case "print": libraryPrint(ctx.arglist()); return null;
        }


        // TODO handle non-built-in functions!
        throw new RuntimeException("Error function '" + name + "' not found");
    }

	@Override
    public Value visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        Value expr = visit(ctx.expression());
        if (expr.getType() != Type.BOOL) {
            throw new TypeMismatchException("Operands to 'not' must have boolean type");
        }
        
        return new Value(!expr.toBool());
    }

	@Override
    public Value visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
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

	@Override
    public Value visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        // evaluate left hand side
        Value lhs = visit(ctx.expression(0));
        if (lhs.getType() != Type.BOOL) {
            throw new TypeMismatchException("Operands to 'or' must have boolean type");
        }
        
        // we do short-circuit eval
        if (lhs.toBool() == true) {
            return new Value(true);
        }
        
        // evaluate right hand side
        Value rhs = visit(ctx.expression(1));
        if (rhs.getType() != Type.BOOL) {
            throw new TypeMismatchException("Operands to 'or' must have boolean type");
        }
        if (rhs.toBool() == true) {
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
    public Value visitInExpression(ChaiParser.InExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        // evaluate left hand side
        Value lhs = visit(ctx.expression(0));
        if (lhs.getType() != Type.BOOL) {
            throw new TypeMismatchException("Operands to 'and' must have boolean type");
        }
        
        // we do short-circuit eval
        if (lhs.toBool() == false) {
            return new Value(false);
        }
        
        // evaluate right hand side
        Value rhs = visit(ctx.expression(1));
        if (rhs.getType() != Type.BOOL) {
            throw new TypeMismatchException("Operands to 'and' must have boolean type");
        }
        if (rhs.toBool() == false) {
            return new Value(false);
        }
        
        return new Value(true);
    }

	@Override
    public Value visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
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
        } else {
            return lhs.divide(rhs);
        }
    }

	@Override
    public Value visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        Value val = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUS:
                if (val.getType() != Type.INT && val.getType() != Type.FLOAT) {
                    throw new TypeMismatchException("Invlaid type to unary + operator");
                } else {
                    return val;
                }
            case ChaiLexer.MINUS:
                if (val.getType() == Type.INT) {
                    return new Value(-val.toInt());
                } else if (val.getType() == Type.FLOAT) {
                    return new Value(-val.toFloat());
                } else {
                    throw new TypeMismatchException("Invlaid type to unary - operator");
                }
            case ChaiLexer.COMPLEMENT:
                if (val.getType() != Type.INT) {
                    throw new TypeMismatchException("Invlaid type to unary ~ operator");
                } else {
                    return new Value(~val.toInt());
                }
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
        // TODO
        return visitChildren(ctx);
    }

	@Override
    public Value visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        // TODO
        return visitChildren(ctx);
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

        // for each argument that we are given
        for (ChaiParser.ArgumentContext arg : args.argument()) {
            // if it's a keyword argument
            if (arg.ASSIGN() != null) {
                String kwname = arg.IDNAME().getText();
                Value kwval = visit(arg.expression());

                if (kwname.equals("end")) {
                    if (kwval.getType() != Type.STRING) {
                        throw new TypeMismatchException("'end' argument must be a String");
                    } else {
                        end = kwval.toString();
                    }
                } else if (kwname.equals("sep")) {
                    if (kwval.getType() != Type.STRING) {
                        throw new TypeMismatchException("'sep' argument must be a String");
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


