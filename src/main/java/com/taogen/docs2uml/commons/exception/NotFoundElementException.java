package com.taogen.docs2uml.commons.exception;

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
