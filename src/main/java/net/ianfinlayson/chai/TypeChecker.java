package net.ianfinlayson.chai;

public class TypeChecker extends ChaiParserBaseVisitor<Type> {
    // each of these methods performs type checking in their part of the
    // tree, and returns they type of it (for expressions) or null (for stmts)
    // it throws exceptions for type errors that are encountered

    @Override
    public Type visitProgram(ChaiParser.ProgramContext ctx) {
        // TODO
    }

    @Override
    public Type visitToplevel(ChaiParser.ToplevelContext ctx) {
        // TODO
    }

    @Override
    public Type visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // TODO
    }

    @Override
    public Type visitParamlist(ChaiParser.ParamlistContext ctx) {
        // TODO
    }

    @Override
    public Type visitNamedParam(ChaiParser.NamedParamContext ctx) {
        // TODO
    }

    @Override
    public Type visitDefaultParam(ChaiParser.DefaultParamContext ctx) {
        // TODO
    }

    @Override
    public Type visitTypedef(ChaiParser.TypedefContext ctx) {
        // TODO
    }

    @Override
    public Type visitTypeparams(ChaiParser.TypeparamsContext ctx) {
        // TODO
    }

    @Override
    public Type visitNamedType(ChaiParser.NamedTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitFloatType(ChaiParser.FloatTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitIntType(ChaiParser.IntTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitStringType(ChaiParser.StringTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitVoidType(ChaiParser.VoidTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitTupleType(ChaiParser.TupleTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitFunctionType(ChaiParser.FunctionTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitSetType(ChaiParser.SetTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitListType(ChaiParser.ListTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitBoolType(ChaiParser.BoolTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitDictType(ChaiParser.DictTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitUnionType(ChaiParser.UnionTypeContext ctx) {
        // TODO
    }

    @Override
    public Type visitTypeparamfills(ChaiParser.TypeparamfillsContext ctx) {
        // TODO
    }

    @Override
    public Type visitUnionpart(ChaiParser.UnionpartContext ctx) {
        // TODO
    }

    @Override
    public Type visitStatements(ChaiParser.StatementsContext ctx) {
        // TODO
    }

    @Override
    public Type visitFuncallStatement(ChaiParser.FuncallStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitVarStatement(ChaiParser.VarStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitModassignStatement(ChaiParser.ModassignStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitIfstatement(ChaiParser.IfstatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitPassStatement(ChaiParser.PassStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitContinueStatement(ChaiParser.ContinueStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitBreakStatement(ChaiParser.BreakStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitForStatement(ChaiParser.ForStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitToplevelStatement(ChaiParser.ToplevelStatementContext ctx) {
        // TODO
    }

    @Override
    public Type visitNestedLvalue(ChaiParser.NestedLvalueContext ctx) {
        // TODO
    }

    @Override
    public Type visitJustID(ChaiParser.JustIDContext ctx) {
        // TODO
    }

    @Override
    public Type visitModassign(ChaiParser.ModassignContext ctx) {
        // TODO
    }

    @Override
    public Type visitIfstmt(ChaiParser.IfstmtContext ctx) {
        // TODO
    }

    @Override
    public Type visitElifclause(ChaiParser.ElifclauseContext ctx) {
        // TODO
    }

    @Override
    public Type visitElseclause(ChaiParser.ElseclauseContext ctx) {
        // TODO
    }

    @Override
    public Type visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // TODO
    }

    @Override
    public Type visitArglist(ChaiParser.ArglistContext ctx) {
        // TODO
    }

    @Override
    public Type visitArgument(ChaiParser.ArgumentContext ctx) {
        // TODO
    }

    @Override
    public Type visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitFuncallExpression(ChaiParser.FuncallExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitPowerExpression(ChaiParser.PowerExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitInExpression(ChaiParser.InExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitPlusMinusExpression(ChaiParser.PlusMinusExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        // TODO
    }

    @Override
    public Type visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        // TODO
    }








    @Override
    public Type visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        // TODO
    }




    @Override
    public Type visitEmptydictLiteralTerm(ChaiParser.EmptydictLiteralTermContext ctx) {
        // TODO
    }





    @Override
    public Type visitListRangeTerm(ChaiParser.ListRangeTermContext ctx) {
        // TODO
    }





    @Override
    public Type visitParensTerm(ChaiParser.ParensTermContext ctx) {
        return visit(ctx.expression());
    }






    @Override
    public Type visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        // TODO
    }





    @Override
    public Type visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        // TODO
    }









    @Override
    public Type visitIdTerm(ChaiParser.IdTermContext ctx) {
        // TODO
    }




    @Override
    public Type visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        // TODO
    }






    @Override
    public Type visitListLiteralTerm(ChaiParser.ListLiteralTermContext ctx) {
        // TODO
    }




    @Override
    public Type visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        // TODO
    }




    @Override
    public Type visitDictLiteralTerm(ChaiParser.DictLiteralTermContext ctx) {
        // TODO
    }

    @Override
    public Type visitDictentry(ChaiParser.DictentryContext ctx) {
        // TODO
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
}



