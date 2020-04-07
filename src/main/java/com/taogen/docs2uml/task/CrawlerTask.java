package com.taogen.docs2uml.task;

import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.constant.RequestMethod;
import com.taogen.docs2uml.commons.vo.HttpRequest;
import com.taogen.docs2uml.crawler.Crawler;
import com.taogen.docs2uml.crawler.CrawlerFactory;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.parser.Parser;
import com.taogen.docs2uml.parser.ParserFactory;
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
    private CommandOption commandOption;
    private Parser parser;
    private ParserType parserType;

    public CrawlerTask(CrawlerType crawlerType, ParserType parserType, CommandOption commandOption){
        this.crawler = CrawlerFactory.create(crawlerType);
        this.crawlerType = crawlerType;
        this.parser = ParserFactory.create(parserType);
        this.parserType = parserType;
        this.commandOption = commandOption;
    }

    @Override
    public List<MyEntity> call() {
        HttpRequest httpRequest = new HttpRequest(commandOption.getUrl(), RequestMethod.GET);
        return parser.parse(crawler.crawl(httpRequest), commandOption);
    }
}
