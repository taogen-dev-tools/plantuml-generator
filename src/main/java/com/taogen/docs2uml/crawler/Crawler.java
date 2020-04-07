package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.commons.vo.HttpRequest;

/**
 * @author Taogen
 */
public interface Crawler<T> {
    /**
     * crawling information from Web page
     *
     * @param httpRequest HTTP request information
     * @return crawling result
     */
    T crawl(HttpRequest httpRequest);
}
