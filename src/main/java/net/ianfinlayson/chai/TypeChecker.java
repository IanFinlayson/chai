package net.ianfinlayson.chai;


public class TypeChecker extends ChaiParserBaseVisitor<Type> {

    @Override public Type visitProgram(ChaiParser.ProgramContext ctx) {
        System.out.println("Beginning type checking on the program!");
        return visitChildren(ctx);
    }

	@Override public Type visitImprt(ChaiParser.ImprtContext ctx) { return visitChildren(ctx); }
	@Override public Type visitUnit(ChaiParser.UnitContext ctx) { return visitChildren(ctx); }
	@Override public Type visitFunctiondef(ChaiParser.FunctiondefContext ctx) { return visitChildren(ctx); }
	@Override public Type visitFunctype(ChaiParser.FunctypeContext ctx) { return visitChildren(ctx); }
	@Override public Type visitParamlist(ChaiParser.ParamlistContext ctx) { return visitChildren(ctx); }
	@Override public Type visitParam(ChaiParser.ParamContext ctx) { return visitChildren(ctx); }
	@Override public Type visitVariadic(ChaiParser.VariadicContext ctx) { return visitChildren(ctx); }
	@Override public Type visitClassdef(ChaiParser.ClassdefContext ctx) { return visitChildren(ctx); }
	@Override public Type visitTypeparams(ChaiParser.TypeparamsContext ctx) { return visitChildren(ctx); }
	@Override public Type visitTypedef(ChaiParser.TypedefContext ctx) { return visitChildren(ctx); }
	@Override public Type visitType(ChaiParser.TypeContext ctx) { return visitChildren(ctx); }
	@Override public Type visitTypeparamfills(ChaiParser.TypeparamfillsContext ctx) { return visitChildren(ctx); }
	@Override public Type visitUnionpart(ChaiParser.UnionpartContext ctx) { return visitChildren(ctx); }
	@Override public Type visitStatements(ChaiParser.StatementsContext ctx) { return visitChildren(ctx); }
	@Override public Type visitStatement(ChaiParser.StatementContext ctx) { return visitChildren(ctx); }
	@Override public Type visitLvalue(ChaiParser.LvalueContext ctx) { return visitChildren(ctx); }
	@Override public Type visitLvaluecont(ChaiParser.LvaluecontContext ctx) { return visitChildren(ctx); }
	@Override public Type visitModassign(ChaiParser.ModassignContext ctx) { return visitChildren(ctx); }
	@Override public Type visitCaseline(ChaiParser.CaselineContext ctx) { return visitChildren(ctx); }
	@Override public Type visitDestructure(ChaiParser.DestructureContext ctx) { return visitChildren(ctx); }
	@Override public Type visitIfstmt(ChaiParser.IfstmtContext ctx) { return visitChildren(ctx); }
	@Override public Type visitElifclause(ChaiParser.ElifclauseContext ctx) { return visitChildren(ctx); }
	@Override public Type visitElseclause(ChaiParser.ElseclauseContext ctx) { return visitChildren(ctx); }
	@Override public Type visitFunctioncall(ChaiParser.FunctioncallContext ctx) { return visitChildren(ctx); }
	@Override public Type visitArglist(ChaiParser.ArglistContext ctx) { return visitChildren(ctx); }
	@Override public Type visitExpression(ChaiParser.ExpressionContext ctx) { return visitChildren(ctx); }
	@Override public Type visitLambda(ChaiParser.LambdaContext ctx) { return visitChildren(ctx); }
	@Override public Type visitLambdaParams(ChaiParser.LambdaParamsContext ctx) { return visitChildren(ctx); }
	@Override public Type visitTerm(ChaiParser.TermContext ctx) { return visitChildren(ctx); }
	@Override public Type visitDictentry(ChaiParser.DictentryContext ctx) { return visitChildren(ctx); }
	@Override public Type visitLiteral(ChaiParser.LiteralContext ctx) { return visitChildren(ctx); }
}

