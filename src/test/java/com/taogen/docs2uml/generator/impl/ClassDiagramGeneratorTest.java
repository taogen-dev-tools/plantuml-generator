package com.taogen.docs2uml.generator.impl;

import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.parser.Parser;
import com.taogen.docs2uml.parser.ParserFactory;
import com.taogen.docs2uml.parser.impl.PackagesParserTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ClassDiagramGeneratorTest {

    private ClassDiagramGenerator classDiagramGenerator = new ClassDiagramGenerator();

    @Test
    public void generate() throws IOException {
        Parser parser = ParserFactory.create(ParserType.DETAILS);
        File file = new File(PackagesParserTest.class.getClassLoader().getResource("html/java-api-java-io-byteArrayInputStream.html").getFile());
        Document document = Jsoup.parse(file, "UTF-8");
        CommandOption commandOption = new CommandOption("null", "java.io");
        commandOption.setMembersDisplayed(true);
        List<MyEntity> myEntities = parser.parse(document, commandOption);
        assertTrue(classDiagramGenerator.generate(myEntities, commandOption));
    }

    @Test
    public void parseTestSuperInterfacesHaveGeneric() throws IOException {
        Parser parser = ParserFactory.create(ParserType.DETAILS);
        String packageName = "java.util";
        CommandOption commandOption = new CommandOption(null, packageName, packageName);
        commandOption.setMembersDisplayed(false);
        List<MyEntity> myEntities = parser.parse(getDocument("html/java-api-java-util-NavigableMap.html"), commandOption);
        assertTrue(classDiagramGenerator.generate(myEntities, commandOption));
    }

    private Document getDocument(String filePath) throws IOException {
        File file = new File(PackagesParserTest.class.getClassLoader().getResource(filePath).getFile());
        return Jsoup.parse(file, "UTF-8");
    }
}
