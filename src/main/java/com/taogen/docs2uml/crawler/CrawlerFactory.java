package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.constant.CrawlerType;
import com.taogen.docs2uml.crawler.impl.ClassDetailsCrawler;
import com.taogen.docs2uml.crawler.impl.ClassesCrawler;
import com.taogen.docs2uml.crawler.impl.PackagesCrawler;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Taogen
 * TODO: Update doc. Add new class
 */
public class CrawlerFactory {
    private static Map<CrawlerType, Crawler> crawlerMap = new EnumMap<>(CrawlerType.class);

    static {
        crawlerMap.put(CrawlerType.PACKAGES, new PackagesCrawler());
        crawlerMap.put(CrawlerType.CLASSES, new ClassesCrawler());
        crawlerMap.put(CrawlerType.DETAILS, new ClassDetailsCrawler());
    }

    private CrawlerFactory() {
        throw new IllegalStateException("factory class");
    }

    public static Crawler create(CrawlerType crawlerType) {
        return crawlerMap.get(crawlerType);
    }
}
