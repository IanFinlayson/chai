import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

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
        
        // do the parsing
        ParseTree tree = parser.program();

        // create the visitor for running (eventually we'll make a type checker one too)
        TaoExecutor executor = new TaoExecutor();
        
        try {
            executor.visit(tree);
            executor.callMain();
        } catch (TypeMismatchException e) {
            System.out.println("Type error: " + e.getMessage());
        }
    }
}

