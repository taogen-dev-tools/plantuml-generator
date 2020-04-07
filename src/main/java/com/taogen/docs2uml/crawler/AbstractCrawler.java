package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.commons.constant.RequestMethod;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * @author Taogen
 */
@Data
public abstract class AbstractCrawler implements Crawler {
    private static final Logger logger = LogManager.getLogger();

    protected static Connection.Method getJsoupConnectionMethod(RequestMethod requestMethod) {
        Connection.Method method = Connection.Method.GET;
        if (RequestMethod.POST.equals(requestMethod)) {
            method = Connection.Method.POST;
        }
        return method;
    }

    protected Document getDocument(String url) {
        Document document = null;
        Connection connection = Jsoup.connect(url);
        try {
            document = connection.timeout(10 * 1000).get();
        } catch (IOException e) {
            logger.error("Fail to connect!", e);
        }
        return document;
    }
}
