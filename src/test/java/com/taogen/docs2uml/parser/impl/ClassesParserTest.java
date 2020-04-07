package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import com.taogen.docs2uml.parser.Parser;
import com.taogen.docs2uml.parser.ParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ClassesParserTest {

    private static final Logger logger = LogManager.getLogger();
    private static Document document;
    private static Parser parser;

    @BeforeClass
    public static void init() throws IOException {
        parser = ParserFactory.create(ParserType.CLASSES);
        File file = new File(PackagesParserTest.class.getClassLoader().getResource("html/java-api-java-io-classes.html").getFile());
        document = Jsoup.parse(file, "UTF-8");
    }

    @Test
    public void parse() {
        String packageName = "java.io";
        List<MyEntity> myEntityList = parser.parse(document, new CommandOption(null, packageName, packageName));
        assertNotNull(myEntityList);
        logger.debug("classes size is : {}", myEntityList.size());
        assertTrue(myEntityList.size() > 0);
        assertEquals(packageName, myEntityList.get(0).getPackageName());
    }
}