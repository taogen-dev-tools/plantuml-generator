package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.ParserException;
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

public class PackagesParserTest {

    private static final Logger logger = LogManager.getLogger();
    private static Document document;
    private static Parser parser;

    @BeforeClass
    public static void init() throws IOException {
        File file = new File(PackagesParserTest.class.getClassLoader().getResource("html/java-api-home.html").getFile());
        document = Jsoup.parse(file, "UTF-8");
        parser = ParserFactory.create(ParserType.PACKAGES);
    }

    @Test
    public void parse() {
        String packageName = "java.io";
        String url = "https://docs.oracle.com/javase/8/docs/api/";
        CommandOption commandOption = new CommandOption(url, packageName, packageName, false);
        List<MyEntity> myEntityList = parser.parse(document, commandOption);
        assertNotNull(myEntityList);
        logger.debug("packages start with {} MyEntity list size is: {}", packageName, myEntityList.size());
        assertEquals(packageName, myEntityList.get(0).getPackageName());
    }

    @Test
    public void parseTestPackagesSizeGreaterThanOne() {
        String url = "https://docs.oracle.com/javase/8/docs/api/";
        String packageName = "java.util";
        CommandOption commandOption = new CommandOption(url, packageName, packageName, true);
        List<MyEntity> utilPackages = parser.parse(document, commandOption);
        assertNotNull(utilPackages);
        logger.debug("packages start with {} MyEntity list size is: {}", packageName, utilPackages.size());
        assertTrue(utilPackages.size() > 1);
        assertEquals(packageName, utilPackages.get(0).getPackageName());
    }

    @Test
    public void parseTestIllegalDocumentType() {
        Parser parser = ParserFactory.create(ParserType.PACKAGES);
        try {
            parser.parse("test string", new CommandOption());
        } catch (Exception e) {
            assertTrue(e instanceof ParserException);
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
        }
    }
}