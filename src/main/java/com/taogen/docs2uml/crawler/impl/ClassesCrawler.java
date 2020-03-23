package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.crawler.AbstractCrawler;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.FailConnectException;
import com.taogen.docs2uml.exception.NotFoundElementException;
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
public class ClassesCrawler extends AbstractCrawler {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<MyEntity> crawl(MyCommand myCommand) {
        Document document = getDocument(myCommand.getUrl());
        Elements elements = getElementsByDocument(document, myCommand);
        return getEntitiesByElements(elements, myCommand);
    }

    private Elements getElementsByDocument(Document document, MyCommand myCommand) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, myCommand.getUrl()));
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
            for (int index : removeIndexSet){
                elements.remove(index);
            }
        }
    }

    private List<MyEntity> getEntitiesByElements(List<Element> elements, MyCommand myCommand) {
        if (elements == null) {
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>();
        for (Element element : elements) {
            String url = element.attr("href");
            String className = element.text();
            MyEntity myEntity = new MyEntity();
            myEntity.setUrl(myCommand.getPrefixUrl() + url);
            myEntity.setClassName(className);
            myEntity.setPackageName(myCommand.getPackageName());
            myEntities.add(myEntity);
        }
        logger.debug("Package {} classes size is {}", myCommand.getPackageName(), myEntities.size());
        return myEntities;
    }
}
