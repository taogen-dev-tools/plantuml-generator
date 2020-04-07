package com.taogen.docs2uml.parser;

import com.taogen.docs2uml.commons.exception.ParserException;
import org.jsoup.nodes.Document;

/**
 * @author Taogen
 */
public abstract class AbstractParser implements Parser {
    protected static final String NOT_FOUND_ELEMENTS_ERROR = "Not found elements form Web page. Please check your URL: %s";
    protected static final String FAIL_TO_CONNECT_URL = "Fail to connect the URL: %s";
    protected static final String GENERIC_LEFT_MARK = "<";
    protected static final String GENERIC_RIGHT_MARK = ">";

    protected void checkDocumentInstance(Object document) {
        if (!(document instanceof Document)) {
            throw new ParserException("can't parser this type of resource");
        }
    }
}
