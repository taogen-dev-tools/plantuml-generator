package com.taogen.docs2uml.exception;

/**
 * @author Taogen
 */
public class FailConnectException extends CrawlerException {
    public FailConnectException() {
    }

    public FailConnectException(String message) {
        super(message);
    }
}
