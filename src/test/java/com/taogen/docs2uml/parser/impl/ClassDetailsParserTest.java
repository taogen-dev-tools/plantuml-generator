package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.parser.Parser;
import com.taogen.docs2uml.parser.ParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.nio.cs.ext.PCK;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ClassDetailsParserTest {

    private static final Logger logger = LogManager.getLogger();
    private static Parser parser;

    @BeforeClass
    public static void init() {
        parser = ParserFactory.create(ParserType.DETAILS);
    }

    @Test
    public void parse() throws IOException {
        String packageName = "java.io";
        List<MyEntity> myEntities = parser.parse(getDocument("html/java-api-java-io-byteArrayInputStream.html"),
                new CommandOption(null, packageName, packageName));
        checkMyEntity(myEntities, packageName);
    }

    private Document getDocument(String filePath) throws IOException {
        File file = new File(PackagesParserTest.class.getClassLoader().getResource(filePath).getFile());
        return Jsoup.parse(file, "UTF-8");
    }

    private void checkMyEntity(List<MyEntity> myEntities, String packageName) {
        assertNotNull(myEntities);
        assertEquals(1, myEntities.size());
        assertNotNull(myEntities.get(0));
        assertNotNull(myEntities.get(0).getClassName());
        assertNotNull(myEntities.get(0).getType());
        assertNotNull(myEntities.get(0).getPackageName());
        assertEquals(packageName, myEntities.get(0).getPackageName());
        logger.debug("class name is {}", myEntities.get(0).getClassName());
    }
}