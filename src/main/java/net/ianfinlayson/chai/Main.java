package net.ianfinlayson.chai;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String args[]) {
        // get input file
        if (args.length != 1) {
            System.out.println("chai: no input files");
            return;
        }

        // set up streams
        CharStream stream = null;
        ChaiLexer lexer = null;
        try {
            stream = CharStreams.fromFileName(args[0]);
            lexer = new ChaiLexer(stream);
        } catch (Exception e) {
            System.out.println("could not open '" + args[0] + "' for reading");
            return;
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChaiParser parser = new ChaiParser(tokens);

        // setup our error listener, and remove default ones
        ErrorHandler handler = new ErrorHandler(args[0]);
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        lexer.addErrorListener(handler);
        parser.addErrorListener(handler);

        // do the parsing
        ParseTree tree = parser.program();
        if (tree == null) {
            System.out.println("internal compiler error");
            return;
        } else {
            // TODO if parsing failed, we should not type check
            // do type checking
            TypeChecker checker = new TypeChecker();
            checker.visit(tree);
        }
    }
}

