package net.ianfinlayson.tao;

import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TaoExecutor extends TaoParserBaseVisitor<TaoValue> {
    private Scanner input = new Scanner(System.in);
    private Stack<HashMap<String, TaoValue>> stack = new Stack<>();
    private HashMap<String, TaoValue> globals = new HashMap<>();

    // this maps function name to the list of stmts comprising it
    private HashMap<String, TaoParser.FunctiondefContext> functions = new HashMap<>();

    // call the main function for the program
    public void callMain() {
        if (functions.get("main") == null) {
            throw new RuntimeException("No main function found");
        }
        visit(functions.get("main").statements());
    }

    // load a variable -- we check the top function first, then globals
    private TaoValue loadVar(String name) {
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
    private void putVar(String name, TaoValue val) {
        if (!stack.empty()) {
            stack.peek().put(name, val);
        } else {
            globals.put(name, val);
        }
    }

	@Override
    public TaoValue visitFunctiondef(TaoParser.FunctiondefContext ctx) {
        // simply put the function into the function table
        String name = ctx.IDNAME().getText();
        functions.put(name, ctx);
        return null;
    }






	@Override
    public TaoValue visitAssignStatement(TaoParser.AssignStatementContext ctx) {
        // TODO make sure that the value exists already first

        // get the thing we are assigning
        TaoValue val = visit(ctx.expression());

        // get the lvalue we are writing into
        TaoParser.LvalueContext lhs = ctx.lvalue();
        
        // if it's just an id, do that
        if (lhs instanceof TaoParser.JustIDContext) {
            putVar(((TaoParser.JustIDContext) lhs).IDNAME().getText(), val);
        }
        
        // TODO also handle (possibly multiple) list/set/dict assignments!

        return null;
    }
    








	@Override
    public TaoValue visitVarStatement(TaoParser.VarStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitModassignStatement(TaoParser.ModassignStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitAssertStatement(TaoParser.AssertStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitReturnStatement(TaoParser.ReturnStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitIfstatement(TaoParser.IfstatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitContinueStatement(TaoParser.ContinueStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitBreakStatement(TaoParser.BreakStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitForStatement(TaoParser.ForStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitWhileStatement(TaoParser.WhileStatementContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitModassign(TaoParser.ModassignContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitIfstmt(TaoParser.IfstmtContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitElifclause(TaoParser.ElifclauseContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitElseclause(TaoParser.ElseclauseContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitFunctioncall(TaoParser.FunctioncallContext ctx) {
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
    public TaoValue visitShiftExpression(TaoParser.ShiftExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitBitorExpression(TaoParser.BitorExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitNotExpression(TaoParser.NotExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitBitxorExpression(TaoParser.BitxorExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitBitandExpression(TaoParser.BitandExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitCompareExpression(TaoParser.CompareExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitNotinExpression(TaoParser.NotinExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitOrExpression(TaoParser.OrExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitPowerExpression(TaoParser.PowerExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitInExpression(TaoParser.InExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitAndExpression(TaoParser.AndExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitIfelseExpression(TaoParser.IfelseExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitPlusMinusExpression(TaoParser.PlusMinusExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitTimesdivExpression(TaoParser.TimesdivExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitConsExpression(TaoParser.ConsExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitUnaryExpression(TaoParser.UnaryExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitTermExpression(TaoParser.TermExpressionContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitListcompTerm(TaoParser.ListcompTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitIdTerm(TaoParser.IdTermContext ctx) {
        String name = ctx.IDNAME().getText();
        return loadVar(name);
    }

	@Override
    public TaoValue visitListRangeTerm(TaoParser.ListRangeTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitListIndexTerm(TaoParser.ListIndexTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitParensTerm(TaoParser.ParensTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitListLiteralTerm(TaoParser.ListLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitTupleLiteralTerm(TaoParser.TupleLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitDicLiteralTerm(TaoParser.DicLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitListSliceTerm(TaoParser.ListSliceTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitSetLiteralTerm(TaoParser.SetLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitSimpleLiteralTerm(TaoParser.SimpleLiteralTermContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitDictentry(TaoParser.DictentryContext ctx) {
        return visitChildren(ctx);
    }

	@Override
    public TaoValue visitIntLiteral(TaoParser.IntLiteralContext ctx) {
        return new TaoValue(Integer.parseInt(ctx.INTVAL().getText()));
    }

	@Override
    public TaoValue visitFloatLiteral(TaoParser.FloatLiteralContext ctx) {
        return new TaoValue(Double.parseDouble(ctx.FLOATVAL().getText()));
    }

	@Override
    public TaoValue visitStringLiteral(TaoParser.StringLiteralContext ctx) {
        // get it, but ditch the quotation marks
        String whole = ctx.STRINGVAL().getText();
        return new TaoValue(whole.substring(1, whole.length() - 1));
    }

	@Override
    public TaoValue visitTrueLiteral(TaoParser.TrueLiteralContext ctx) {
        return new TaoValue(true);
    }

	@Override
    public TaoValue visitFalseLiteral(TaoParser.FalseLiteralContext ctx) {
        return new TaoValue(false);
    }
    
    // standard library functions appear below
    private void libraryPrint(TaoParser.ArglistContext args) {
        String end = "\n";
        String sep = " ";
        boolean first = true;
        
        // we put the things to print in a list so we need only go through args once
        ArrayList<TaoValue> toprint = new ArrayList<>();

        // for each argument that we are given
        for (TaoParser.ArgumentContext arg : args.argument()) {
            // if it's a keyword argument
            if (arg.ASSIGN() != null) {
                String kwname = arg.IDNAME().getText();
                TaoValue kwval = visit(arg.expression());

                if (kwname.equals("end")) {
                    if (kwval.getType() != TaoType.STRING) {
                        throw new TypeMismatchException("'end' argument must be a String");
                    } else {
                        end = kwval.toString();
                    }
                } else if (kwname.equals("sep")) {
                    if (kwval.getType() != TaoType.STRING) {
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
        for (TaoValue val : toprint) {
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


