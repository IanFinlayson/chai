package net.ianfinlayson.chai;

import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ChaiExecutor extends ChaiParserBaseVisitor<ChaiValue> {
    private Scanner input = new Scanner(System.in);
    private Stack<HashMap<String, ChaiValue>> stack = new Stack<>();
    private HashMap<String, ChaiValue> globals = new HashMap<>();

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
    private ChaiValue loadVar(String name) {
        if (!stack.empty()) {
            if (stack.peek().get(name) != null) {
                return stack.peek().get(name);
            }
        }

        if (globals.get(name) != null) {
            return globals.get(name);
        }

        else throw new RuntimeException("Could not load variable " + name);
    }

    // put a var -- again into the top function if there is one, or global
    private void putVar(String name, ChaiValue val) {
        if (!stack.empty()) {
            stack.peek().put(name, val);
        } else {
            globals.put(name, val);
        }
    }

	@Override
    public ChaiValue visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // simply put the function into the function table
        String name = ctx.IDNAME().getText();
        functions.put(name, ctx);
        return null;
    }






	@Override
    public ChaiValue visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // TODO make sure that the value exists already first

        // get the thing we are assigning
        ChaiValue val = visit(ctx.expression());

        // get the lvalue we are writing into
        ChaiParser.LvalueContext lhs = ctx.lvalue();
        
        // if it's just an id, do that
        if (lhs instanceof ChaiParser.JustIDContext) {
            putVar(((ChaiParser.JustIDContext) lhs).IDNAME().getText(), val);
        }
        
        // TODO also handle (possibly multiple) list/set/dict assignments!

        return null;
    }
    








	@Override
    public ChaiValue visitVarStatement(ChaiParser.VarStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitModassignStatement(ChaiParser.ModassignStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitIfstatement(ChaiParser.IfstatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitContinueStatement(ChaiParser.ContinueStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitBreakStatement(ChaiParser.BreakStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitForStatement(ChaiParser.ForStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitModassign(ChaiParser.ModassignContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitIfstmt(ChaiParser.IfstmtContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitElifclause(ChaiParser.ElifclauseContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitElseclause(ChaiParser.ElseclauseContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
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
    public ChaiValue visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitPowerExpression(ChaiParser.PowerExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitInExpression(ChaiParser.InExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitPlusMinusExpression(ChaiParser.PlusMinusExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitTermExpression(ChaiParser.TermExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitIdTerm(ChaiParser.IdTermContext ctx) {
        String name = ctx.IDNAME().getText();
        return loadVar(name);
    }

	@Override
    public ChaiValue visitListRangeTerm(ChaiParser.ListRangeTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitParensTerm(ChaiParser.ParensTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitListLiteralTerm(ChaiParser.ListLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitDicLiteralTerm(ChaiParser.DicLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitSimpleLiteralTerm(ChaiParser.SimpleLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitDictentry(ChaiParser.DictentryContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public ChaiValue visitIntLiteral(ChaiParser.IntLiteralContext ctx) {
        return new ChaiValue(Integer.parseInt(ctx.INTVAL().getText()));
    }

	@Override
    public ChaiValue visitFloatLiteral(ChaiParser.FloatLiteralContext ctx) {
        return new ChaiValue(Double.parseDouble(ctx.FLOATVAL().getText()));
    }

	@Override
    public ChaiValue visitStringLiteral(ChaiParser.StringLiteralContext ctx) {
        // get it, but ditch the quotation marks
        String whole = ctx.STRINGVAL().getText();
        return new ChaiValue(whole.substring(1, whole.length() - 1));
    }

	@Override
    public ChaiValue visitTrueLiteral(ChaiParser.TrueLiteralContext ctx) {
        return new ChaiValue(true);
    }

	@Override
    public ChaiValue visitFalseLiteral(ChaiParser.FalseLiteralContext ctx) {
        return new ChaiValue(false);
    }
    
    // standard library functions appear below
    private void libraryPrint(ChaiParser.ArglistContext args) {
        String end = "\n";
        String sep = " ";
        boolean first = true;
        
        // we put the things to print in a list so we need only go through args once
        ArrayList<ChaiValue> toprint = new ArrayList<>();

        // for each argument that we are given
        for (ChaiParser.ArgumentContext arg : args.argument()) {
            // if it's a keyword argument
            if (arg.ASSIGN() != null) {
                String kwname = arg.IDNAME().getText();
                ChaiValue kwval = visit(arg.expression());

                if (kwname.equals("end")) {
                    if (kwval.getType() != ChaiType.STRING) {
                        throw new TypeMismatchException("'end' argument must be a String");
                    } else {
                        end = kwval.toString();
                    }
                } else if (kwname.equals("sep")) {
                    if (kwval.getType() != ChaiType.STRING) {
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
        for (ChaiValue val : toprint) {
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


