package net.ianfinlayson.chai;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// we want to quit the program on the first error message, which is not the default behavior
class ChaiErrorListener extends BaseErrorListener {
    private String filename;

    public ChaiErrorListener(String filename) {
        this.filename = filename;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String message, RecognitionException e) {
        System.out.println(filename + ":" + line + ":" + column + " " + message);
        System.exit(0);
    }
}

public class Main {
    public static void main(String args[]) {
        // get input file
        if (args.length != 1) {
            System.out.println("Please pass the Chai file as input.");
            return;
        }

        // set up streams
        CharStream stream = null;
        ChaiLexer lexer = null;
        try {
            stream = CharStreams.fromFileName(args[0]);
            lexer = new ChaiLexer(stream);
        } catch (Exception e) {
            System.out.println("Could not open '" + args[0] + "' for reading.");
            return;
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChaiParser parser = new ChaiParser(tokens);

        // setup our error listener, that prints it and then bails
        ChaiErrorListener el = new ChaiErrorListener(args[0]);
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        lexer.addErrorListener(el);
        parser.addErrorListener(el);

        // do the parsing
        ParseTree tree = parser.program();
        if (tree == null) {
            System.out.println("Couldn't parse at all!");
            return;
        }

        // print the tree to a text file for debugging porpoises
        try (PrintWriter dbgfile = new PrintWriter("parsetree.txt")) {
            dbgfile.println(tree.toStringTree(parser));
        } catch (FileNotFoundException e) {}

        // create the visitor for running (eventually we'll make a type checker one too)
        Executor executor = new Executor(stream);

        try {
            executor.visit(tree);
            executor.callMain();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}

