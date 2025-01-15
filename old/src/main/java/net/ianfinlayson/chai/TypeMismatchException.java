package net.ianfinlayson.chai;

public class TypeMismatchException extends RuntimeException {
    private int line;

    public TypeMismatchException(String message, int line) {
        super(message);
        this.line = line;
    }

    @Override
    public String getMessage() {
        return "Type error on line " + line + ": " + super.getMessage();
    }
}

