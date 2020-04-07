package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.exception.NotFoundElementException;
import com.taogen.docs2uml.parser.AbstractParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Taogen
 */
public class ClassesParser extends AbstractParser {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<MyEntity> parse(Object document, CommandOption commandOption) {
        checkDocumentInstance(document);
        Elements elements = getElementsByDocument((Document) document, commandOption);
        return getEntitiesByElements(elements, commandOption);
    }

    private Elements getElementsByDocument(Document document, CommandOption commandOption) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, commandOption.getUrl()));
        }
        Elements elements = document.getElementsByAttributeValue("target", "classFrame");
        filterElements(elements);
        return elements;
    }

    private void filterElements(Elements elements) {
        if (elements != null) {
            Set<Integer> removeIndexSet = new HashSet<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                String titleAttr = elements.get(i).attr("title");
                if (titleAttr == null || "".equals(titleAttr.trim())) {
                    removeIndexSet.add(i);
                }
            }
            for (int index : removeIndexSet) {
                elements.remove(index);
            }
        }
    }

    private List<MyEntity> getEntitiesByElements(List<Element> elements, CommandOption commandOption) {
        if (elements == null) {
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>();
        for (Element element : elements) {
            String url = element.attr("href");
            String className = element.text();
            MyEntity myEntity = new MyEntity();
            myEntity.setUrl(commandOption.getPrefixUrl() + url);
            myEntity.setClassName(className);
            myEntity.setPackageName(commandOption.getPackageName());
            myEntities.add(myEntity);
        }
        logger.debug("Package {} classes size is {}", commandOption.getPackageName(), myEntities.size());
        return myEntities;
    }
}
