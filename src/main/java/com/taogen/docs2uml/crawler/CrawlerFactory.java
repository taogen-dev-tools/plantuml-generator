package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.constant.CrawlerType;
import com.taogen.docs2uml.crawler.impl.ClassDetailsCrawler;
import com.taogen.docs2uml.crawler.impl.ClassesCrawler;
import com.taogen.docs2uml.crawler.impl.PackagesCrawler;
import com.taogen.docs2uml.entity.MyCommand;

/**
 * @author Taogen
 * TODO: Update doc. Add new class
 */
public class CrawlerFactory {
    private CrawlerFactory(){
        throw new IllegalStateException("factory class");
    }

    public static Crawler create(CrawlerType crawlerType, MyCommand myCommand){
        if (CrawlerType.PACKAGES.equals(crawlerType)){
            return new PackagesCrawler(myCommand);
        }else if (CrawlerType.CLASSES.equals(crawlerType)){
            return new ClassesCrawler(myCommand);
        }else if (CrawlerType.DETAILS.equals(crawlerType)){
            return new ClassDetailsCrawler(myCommand);
        }
        return null;
    }
}
