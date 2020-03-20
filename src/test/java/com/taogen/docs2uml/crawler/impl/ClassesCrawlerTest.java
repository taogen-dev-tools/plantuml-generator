package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClassesCrawlerTest {

    private ClassesCrawler classesCrawler = new ClassesCrawler();

    @Test
    public void crawl() {
        String url = "https://docs.oracle.com/javase/8/docs/api/java/io/package-frame.html";
        classesCrawler.setMyCommand(new MyCommand(url));
        List<MyEntity> myEntities = classesCrawler.crawl();
        assertNotNull(myEntities);
        assertTrue(myEntities.size() > 0);
    }
}