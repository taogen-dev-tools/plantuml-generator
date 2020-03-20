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

    public ClassesCrawler() {
    }

    public ClassesCrawler(MyCommand myCommand) {
        this.myCommand = myCommand;
    }

    @Override
    public List<MyEntity> crawl() {
        Document document = getDocument(this.myCommand.getUrl());
        Elements elements = getElementsByDocument(document);
        return getEntitiesByElements(elements);
    }

    private Elements getElementsByDocument(Document document) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, this.myCommand.getUrl()));
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

    private List<MyEntity> getEntitiesByElements(List<Element> elements) {
        if (elements == null) {
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, this.myCommand.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>();
        for (Element element : elements) {
            String url = element.attr("href");
            String className = element.text();
            MyEntity myEntity = new MyEntity();
            myEntity.setUrl(this.myCommand.getPrefixUrl() + url);
            myEntity.setClassName(className);
            myEntities.add(myEntity);
        }
        logger.debug("myEntities size is {}", myEntities.size());
        return myEntities;
    }
}
