package net.ianfinlayson.chai;

//import org.antlr.v4.runtime.tree.TerminalNode;


public class TypeChecker extends ChaiParserBaseVisitor<Type> {

    @Override public Type visitProgram(ChaiParser.ProgramContext ctx) {
        System.out.println("Beginning type checking on the program!");
        return visitChildren(ctx);
    }



}
