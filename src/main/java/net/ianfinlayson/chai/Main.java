package net.ianfinlayson.chai;

import java.io.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static String printSyntaxTree(Parser parser, ParseTree root) {
        StringBuilder buf = new StringBuilder();
        recursive(root, buf, 0, Arrays.asList(parser.getRuleNames()));
        return buf.toString();
    }

    private static void recursive(ParseTree aRoot, StringBuilder buf, int offset, List<String> ruleNames) {
        for (int i = 0; i < offset; i++) {
            buf.append("  ");
        }
        buf.append(Trees.getNodeText(aRoot, ruleNames)).append("\n");
        if (aRoot instanceof ParserRuleContext) {
            ParserRuleContext prc = (ParserRuleContext) aRoot;
            if (prc.children != null) {
                for (ParseTree child : prc.children) {
                    recursive(child, buf, offset + 1, ruleNames);
                }
            }
        }
    }

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
            // optionally we can print out the tree to stdout
            //System.out.println(printSyntaxTree(parser, tree));


            // TODO if parsing failed, we should not type check
            // do type checking
            TypeChecker checker = new TypeChecker();
            checker.visit(tree);
        }
    }
}

