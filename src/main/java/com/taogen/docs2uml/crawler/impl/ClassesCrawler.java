package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.crawler.AbstractCrawler;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;

import java.util.List;

/**
 * @author Taogen
 */
public class ClassesCrawler extends AbstractCrawler {

    public ClassesCrawler() {
    }

    public ClassesCrawler(MyCommand myCommand) {
        this.myCommand = myCommand;
    }

    @Override
    public List<MyEntity> crawl() {
        return null;
    }
}
