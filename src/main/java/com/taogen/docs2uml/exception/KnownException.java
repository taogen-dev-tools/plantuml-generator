package com.taogen.docs2uml.exception;

/**
 * @author Taogen
 */
public class KnownException extends RuntimeException {

    public KnownException() {
    }

    public KnownException(String message) {
        super(message);
    }
}
