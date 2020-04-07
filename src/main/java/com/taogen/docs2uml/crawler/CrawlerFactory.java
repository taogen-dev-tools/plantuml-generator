package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.crawler.impl.DocumentCrawler;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Taogen
 * TODO: Update doc. Add new class
 */
public class CrawlerFactory {
    private static Map<CrawlerType, Crawler> crawlerMap = new EnumMap<>(CrawlerType.class);

    static {
        crawlerMap.put(CrawlerType.DOCUMENT, new DocumentCrawler());
    }

    private CrawlerFactory() {
        throw new IllegalStateException("factory class");
    }

    public static Crawler create(CrawlerType crawlerType) {
        return crawlerMap.get(crawlerType);
    }
}
