package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.FailConnectException;
import com.taogen.docs2uml.exception.KnownException;
import com.taogen.docs2uml.exception.NotFoundElementException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PackagesCrawlerTest extends CrawlerTest {


    private static String url = "https://docs.oracle.com/javase/8/docs/api/";
    private PackagesCrawler packagesCrawler = new PackagesCrawler();

    @Test
    public void crawl() {
        String packageName = "java.util";
        packagesCrawler.setMyCommand(new MyCommand(url, packageName));
        List<MyEntity> myEntityList = packagesCrawler.crawl();
        assertNotNull(myEntityList);
        assertTrue(myEntityList.size() > 0);
        checkUrlOfMyEntities(myEntityList);
    }

    @Test
    public void crawlTestFailConnectUrl() {
        try {
            packagesCrawler.setMyCommand(new MyCommand("http://000.com", "java.test"));
            packagesCrawler.crawl();
        } catch (KnownException e) {
            assertTrue(e instanceof FailConnectException);
        }
    }

    @Test
    public void crawlTestNotFoundElement() {
        String url = "https://docs.oracle.com/javase123123/8/docs/api/";
        try {
            packagesCrawler.setMyCommand(new MyCommand(url, "java.test"));
            packagesCrawler.crawl();
        } catch (KnownException e) {
            assertTrue(e instanceof NotFoundElementException);
        }
    }
}