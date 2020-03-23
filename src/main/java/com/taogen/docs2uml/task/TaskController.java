package com.taogen.docs2uml.task;

import com.taogen.docs2uml.constant.CrawlerType;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.TaskExecuteException;
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
    private MyCommand myCommand;

    public TaskController(MyCommand myCommand) {
        this.myCommand = myCommand;
        queue.add(new CrawlerTask(CrawlerType.PACKAGES, myCommand));
    }

    public List<MyEntity> execute() {
        logger.info("Tasks starting...");
        while (!queue.isEmpty()) {
            CrawlerTask task = queue.poll();
            Future<List<MyEntity>> future = this.pool.submit(task);
            List<MyEntity> resultEntities = getResultFromFuture(future, task.getMyCommand().getUrl());
            if (CrawlerType.PACKAGES.equals(task.getCrawlerType())) {
                for (MyEntity myEntity : resultEntities) {
                    queue.add(new CrawlerTask(CrawlerType.CLASSES, new MyCommand(myEntity.getUrl(), myCommand.getTopPackageName(), myEntity.getPackageName())));
                }
            }
            if (CrawlerType.CLASSES.equals(task.getCrawlerType())) {
                for (MyEntity myEntity : resultEntities){
                    queue.add(new CrawlerTask(CrawlerType.DETAILS, new MyCommand(myEntity.getUrl(), myCommand.getTopPackageName(), myEntity.getPackageName())));
                }
            }
            if (CrawlerType.DETAILS.equals(task.getCrawlerType())){
                this.myEntities.addAll(resultEntities);
            }
        }
        this.pool.shutdown();
        logger.debug("return entity size: {}", this.myEntities.size());
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
