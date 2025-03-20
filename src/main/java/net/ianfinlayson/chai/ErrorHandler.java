package net.ianfinlayson.chai;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// eventually we want may want to do something more complex here
class ErrorHandler extends BaseErrorListener {
    private String filename;

    public ErrorHandler(String filename) {
        this.filename = filename;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String message, RecognitionException e) {
        // TODO eventually prettify these syntax errors some
        System.out.println(filename + ":" + line + ":" + column + " " + message);
    }
}

