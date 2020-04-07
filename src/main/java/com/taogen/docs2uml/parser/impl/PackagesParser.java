package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.constant.CrawlerError;
import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.commons.constant.ParserError;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.exception.NotFoundElementException;
import com.taogen.docs2uml.commons.vo.HttpRequest;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import com.taogen.docs2uml.parser.AbstractParser;
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
public class PackagesParser extends AbstractParser {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<MyEntity> parse(Object document, CommandOption commandOption) {
        checkDocumentInstance(document);
        String packageListFrameUrl = getPackageListDocumentUrl((Document) document, commandOption);
        Document packageListDocument = (Document) CrawlerFactory.create(CrawlerType.DOCUMENT).crawl(new HttpRequest(packageListFrameUrl));
        Elements packageElements = getPackageElements(packageListDocument, packageListFrameUrl, commandOption);
        return getEntityListByElements(packageElements, commandOption);
    }

    private String getPackageListDocumentUrl(Document document, CommandOption commandOption) {
        if (document == null) {
            throw new FailConnectException(String.format(CrawlerError.FAIL_TO_CONNECT_URL, commandOption.getUrl()));
        }
        String packageListFrameUrl;
        try {
            Element element = document.getElementsByAttributeValue("name", "packageListFrame").get(0);
            packageListFrameUrl = element.attr("src");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(ParserError.NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
        }
        return new StringBuilder().append(commandOption.getPrefixUrl()).append("/").append(packageListFrameUrl).toString();
    }

    private Elements getPackageElements(Document packageListDocument, String packageListFrameUrl, CommandOption commandOption) {
        if (packageListDocument == null) {
            throw new FailConnectException(String.format(CrawlerError.FAIL_TO_CONNECT_URL, packageListFrameUrl));
        }
        Elements elements;
        try {
            elements = packageListDocument.getElementsByClass("indexContainer").get(0).getElementsByAttributeValue("target", "PackageFrame");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(ParserError.NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
        }
        return elements;
    }

    private List<MyEntity> getEntityListByElements(Elements packageElements, CommandOption commandOption) {
        if (packageElements == null) {
            throw new NotFoundElementException(String.format(ParserError.NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>();
        for (Element element : packageElements) {
            String packageName = element.text();
            boolean filterCondition;
            if (commandOption.getSubPackage() != null && commandOption.getSubPackage()) {
                filterCondition = packageName.trim().startsWith(commandOption.getTopPackageName());
            } else {
                filterCondition = packageName.trim().equals(commandOption.getTopPackageName());
            }
            if (filterCondition) {
                String packageHref = element.attr("href");
                MyEntity myEntity = new MyEntity();
                myEntity.setPackageName(packageName);
                myEntity.setUrl(new StringBuilder().append(commandOption.getPrefixUrl()).append("/").append(packageHref).toString());
                myEntities.add(myEntity);
            }
        }
        return myEntities;
    }
}
