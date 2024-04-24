package net.ianfinlayson.tao;

// defines an exception for type mismatches in sloth code

public class TypeMismatchException extends RuntimeException {
    public TypeMismatchException(String mesg) {
        super(mesg);
    }
}

