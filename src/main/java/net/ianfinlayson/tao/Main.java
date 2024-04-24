package net.ianfinlayson.tao;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// we want to quit the program on the first error message, which is not the default behavior
class TaoErrorListener extends BaseErrorListener {
    private String filename;

    public TaoErrorListener(String filename) {
        this.filename = filename;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String message, RecognitionException e) {
        System.out.println(filename + ":" + line + ":" + column + " " + message);
        System.exit(1);
    }
}

public class Main {
    public static void main(String args[]) {
        // get input file
        if (args.length != 1) {
            System.out.println("Please pass the Tao file as input.");
            return;
        }

        // set up streams
        TaoLexer lexer = null;
        try {
            lexer = new TaoLexer(CharStreams.fromFileName(args[0]));
        } catch (Exception e) {
            System.out.println("Could not open '" + args[0] + "' for reading.");
            return;
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TaoParser parser = new TaoParser(tokens);

        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        TaoErrorListener el = new TaoErrorListener(args[0]);
        lexer.addErrorListener(el);
        parser.addErrorListener(el);

        // do the parsing
        ParseTree tree = parser.program();
        if (tree == null) {
            System.out.println("Couldn't parse at all!");
        }

        System.out.println("Done parsing!");

        // create the visitor for running (eventually we'll make a type checker one too)
        TaoExecutor executor = new TaoExecutor();

        try {
            executor.visit(tree);
            executor.callMain();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}

