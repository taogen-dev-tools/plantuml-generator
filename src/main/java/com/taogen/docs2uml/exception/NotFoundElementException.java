package com.taogen.docs2uml.exception;

/**
 * @author Taogen
 */
public class NotFoundElementException extends CrawlerException {
    public NotFoundElementException() {
    }

    public NotFoundElementException(String message) {
        super(message);
    }
}
