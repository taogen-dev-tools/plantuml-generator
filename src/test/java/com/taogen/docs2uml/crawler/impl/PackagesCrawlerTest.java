package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.constant.CrawlerType;
import com.taogen.docs2uml.crawler.Crawler;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.FailConnectException;
import com.taogen.docs2uml.exception.KnownException;
import com.taogen.docs2uml.exception.NotFoundElementException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PackagesCrawlerTest {


    private Crawler packagesCrawler;
    private static String url = "https://docs.oracle.com/javase/8/docs/api/";

    @Test
    public void crawl() {
        String packageName = "java.util";
        packagesCrawler = CrawlerFactory.create(CrawlerType.PACKAGES, new MyCommand(url, packageName));
        List<MyEntity> myEntityList = packagesCrawler.crawl();
        assertNotNull(myEntityList);
        assertTrue(myEntityList.size() > 0);
    }

    @Test
    public void crawlTestFailConnectUrl(){
        try {
            packagesCrawler = CrawlerFactory.create(CrawlerType.PACKAGES, new MyCommand("http://test.test", "java.test"));
            packagesCrawler.crawl();
        } catch (KnownException e) {
            assertTrue(e instanceof FailConnectException);
        }
    }

    @Test
    public void crawlTestNotFoundElement(){
        String url = "https://docs.oracle.com/javase123123/8/docs/api/";
        try {
            packagesCrawler = CrawlerFactory.create(CrawlerType.PACKAGES, new MyCommand(url, "java.test"));
            packagesCrawler.crawl();
        } catch (KnownException e) {
            assertTrue(e instanceof NotFoundElementException);
        }
    }
}