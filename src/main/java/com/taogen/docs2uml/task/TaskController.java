package com.taogen.docs2uml.task;

import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.TaskExecuteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Taogen
 */
public class TaskController {
    private static final Logger logger = LogManager.getLogger();
    /**
     * Thread pool size.
     * Warning: Too big too fast can lead to request of destination server be refused. It will throw java.net.SocketTimeoutException: Read timed out
     */
    private static final Integer THREAD_POOL_COUNT = 5;
    private ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
    private ConcurrentLinkedQueue<CrawlerTask> queue = new ConcurrentLinkedQueue();
    private List<MyEntity> myEntities = new ArrayList<>();
    private CommandOption commandOption;

    public TaskController(CommandOption commandOption) {
        this.commandOption = commandOption;
        if (commandOption.getSubPackage() == null) {
            commandOption.setSubPackage(false);
        }
        queue.add(new CrawlerTask(CrawlerType.DOCUMENT, ParserType.PACKAGES, commandOption));
    }

    public List<MyEntity> execute() {
        logger.info("Tasks starting...");
        while (!queue.isEmpty()) {
            CrawlerTask task = queue.poll();
            Future<List<MyEntity>> future = this.pool.submit(task);
            List<MyEntity> resultEntities = getResultFromFuture(future, task.getCommandOption().getUrl());
            if (ParserType.PACKAGES.equals(task.getParserType())) {
                for (MyEntity myEntity : resultEntities) {
                    queue.add(new CrawlerTask(CrawlerType.DOCUMENT, ParserType.CLASSES, new CommandOption(myEntity.getUrl(), commandOption.getTopPackageName(), myEntity.getPackageName())));
                }
            }
            if (ParserType.CLASSES.equals(task.getParserType())) {
                for (MyEntity myEntity : resultEntities) {
                    queue.add(new CrawlerTask(CrawlerType.DOCUMENT, ParserType.DETAILS, new CommandOption(myEntity.getUrl(), commandOption.getTopPackageName(), myEntity.getPackageName())));
                }
            }
            if (ParserType.DETAILS.equals(task.getParserType())) {
                this.myEntities.addAll(resultEntities);
            }
        }
        this.pool.shutdown();
        logger.info("Task end.");
        logger.info("return entity size is {}", this.myEntities.size());
        return this.myEntities;
    }

    private List<MyEntity> getResultFromFuture(Future<List<MyEntity>> future, String url) {
        List<MyEntity> resultEntities;
        try {
            resultEntities = future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            this.pool.shutdown();
            throw new TaskExecuteException(e.getMessage() + "\r\nPlease check the URL: " + url);
        }
        return resultEntities;
    }
}
