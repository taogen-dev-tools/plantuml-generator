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
import java.util.List;

/**
 * @author Taogen
 */
public class PackagesCrawler extends AbstractCrawler {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<MyEntity> crawl(MyCommand myCommand) {
        Document document = getDocument(myCommand.getUrl());
        String packageListFrameUrl = getPackageListDocumentUrl(document, myCommand);
        Document packageListDocument = getDocument(packageListFrameUrl);
        Elements packageElements = getPackageElements(packageListDocument, packageListFrameUrl, myCommand);
        return getEntityListByElements(packageElements, myCommand);
    }

    private String getPackageListDocumentUrl(Document document, MyCommand myCommand) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, myCommand.getUrl()));
        }
        String packageListFrameUrl;
        try {
            Element element = document.getElementsByAttributeValue("name", "packageListFrame").get(0);
            packageListFrameUrl = element.attr("src");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
        }
        return new StringBuilder().append(myCommand.getPrefixUrl()).append("/").append(packageListFrameUrl).toString();
    }

    private Elements getPackageElements(Document packageListDocument, String packageListFrameUrl, MyCommand myCommand) {
        if (packageListDocument == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, packageListFrameUrl));
        }
        Elements elements;
        try {
            elements = packageListDocument.getElementsByClass("indexContainer").get(0).getElementsByAttributeValue("target", "PackageFrame");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
        }
        return elements;
    }

    private List<MyEntity> getEntityListByElements(Elements packageElements, MyCommand myCommand) {
        if (packageElements == null) {
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>();
        for (Element element : packageElements) {
            String packageName = element.text();
            boolean filterCondition;
            if (myCommand.getSubPackage()) {
                filterCondition = packageName.startsWith(myCommand.getTopPackageName());
            } else {
                filterCondition = packageName.trim().equals(myCommand.getTopPackageName());
            }
            if (filterCondition) {
                String packageHref = element.attr("href");
                MyEntity myEntity = new MyEntity();
                myEntity.setPackageName(packageName);
                myEntity.setUrl(new StringBuilder().append(myCommand.getPrefixUrl()).append("/").append(packageHref).toString());
                myEntities.add(myEntity);
            }
        }
        return myEntities;
    }
}
