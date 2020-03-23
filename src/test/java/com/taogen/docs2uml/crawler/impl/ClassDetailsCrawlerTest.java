package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClassDetailsCrawlerTest extends CrawlerTest{

    private ClassDetailsCrawler classDetailsCrawler = new ClassDetailsCrawler();

    @Test
    public void crawl() {
        String url = "https://docs.oracle.com/javase/8/docs/api/java/io/BufferedInputStream.html";
        MyCommand myCommand = new MyCommand(url, "java.io", "java.io");
//        classDetailsCrawler.setMyCommand(myCommand);
        List<MyEntity> myEntities = classDetailsCrawler.crawl(myCommand);
        assertNotNull(myEntities);
        assertEquals(1, myEntities.size());
        checkClassDetails(myEntities.get(0), myCommand);
    }

    private void checkClassDetails(MyEntity myEntity, MyCommand myCommand) {
        assertNotNull(myEntity.getType());
        assertNotNull(myEntity.getPackageName());
        assertNotNull(myEntity.getClassName());
        assertNotNull(myEntity.getPackageName());
        assertTrue(myEntity.getPackageName().startsWith(myCommand.getPackageName()));
    }
}