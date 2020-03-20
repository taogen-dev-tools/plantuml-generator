package com.taogen.docs2uml.task;

import com.taogen.docs2uml.constant.CrawlerType;
import com.taogen.docs2uml.crawler.Crawler;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Taogen
 */
@Data
public class CrawlerTask implements Callable<List<MyEntity>> {
    private Crawler crawler;
    private CrawlerType crawlerType;
    private MyCommand myCommand;

    public CrawlerTask(CrawlerType crawlerType, MyCommand myCommand){
        this.crawler = CrawlerFactory.create(crawlerType);
        this.crawlerType = crawlerType;
        this.myCommand = myCommand;
    }

    @Override
    public List<MyEntity> call() {
        return crawler.crawl(myCommand);
    }
}
