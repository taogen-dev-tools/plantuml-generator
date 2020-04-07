package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.vo.HttpRequest;
import com.taogen.docs2uml.crawler.Crawler;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DocumentCrawlerTest {

    @Test
    public void crawl() {
        Crawler crawler = CrawlerFactory.create(CrawlerType.DOCUMENT);
        Document document = (Document) crawler.crawl(new HttpRequest("https://github.com"));
        assertNotNull(document);
    }

    @Test
    public void crawlTestUrlIsNull() {
        Crawler crawler = CrawlerFactory.create(CrawlerType.DOCUMENT);
        try {
            Document document = (Document) crawler.crawl(new HttpRequest(null));
        } catch (Exception e) {
            assertTrue(e instanceof FailConnectException);
        }
    }
}