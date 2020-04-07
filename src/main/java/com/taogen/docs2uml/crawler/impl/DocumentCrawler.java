package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.vo.HttpRequest;
import com.taogen.docs2uml.crawler.AbstractCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * @author Taogen
 */
public class DocumentCrawler extends AbstractCrawler {

    @Override
    public Document crawl(HttpRequest httpRequest) {
        Document document = null;
        Connection connection = null;
        try {
            connection = Jsoup.connect(httpRequest.getUrl());
        } catch (IllegalArgumentException e) {
            throw new FailConnectException(e.getMessage());
        }
        if (httpRequest.getHeaders() != null) {
            connection.headers(httpRequest.getHeaders());
        }
        connection.method(getJsoupConnectionMethod(httpRequest.getRequestMethod()));
        try {
            document = connection.timeout(10 * 1000).get();
        } catch (IOException e) {
            throw new FailConnectException(e.getMessage());
        }
        return document;
    }
}
