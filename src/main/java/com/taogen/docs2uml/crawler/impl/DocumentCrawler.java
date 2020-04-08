package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.vo.HttpRequest;
import com.taogen.docs2uml.crawler.AbstractCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            connection.headers(getBasicHeaders());
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

    protected Map<String, String> getBasicHeaders()
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.5");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0");
        return headers;
    }
}
