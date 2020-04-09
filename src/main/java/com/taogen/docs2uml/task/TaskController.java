package com.taogen.docs2uml.task;

import com.taogen.docs2uml.commons.constant.CrawlerType;
import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.TaskExecuteException;
import com.taogen.docs2uml.commons.util.GenericUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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
    private final List<MyEntity> myEntities = new ArrayList<>();
    private final List<MyEntity> specifiedMyEntities = new ArrayList<>();
    private CommandOption commandOption;

    public TaskController(CommandOption commandOption) {
        this.commandOption = commandOption;
        if (commandOption.getSubPackage() == null) {
            commandOption.setSubPackage(false);
        }
        queue.add(new CrawlerTask(CrawlerType.DOCUMENT, ParserType.PACKAGES, commandOption));
    }

    public void execute() {
        logger.info("Tasks starting...");
        String specifiedClass = this.commandOption.getSpecifiedClass();
        Map<String, MyEntity> entityMap = null;
        if (specifiedClass != null && !specifiedClass.isEmpty()) {
            entityMap = new HashMap<>();
        }
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
                if (specifiedClass != null && !specifiedClass.isEmpty()) {
                    MyEntity myEntity = resultEntities.get(0);
                    entityMap.put(GenericUtil.removeGeneric(myEntity.getClassName()), myEntity);
                }
            }
        }
        this.pool.shutdown();
        logger.debug("myEntity list size is {}", this.myEntities.size());
        if (specifiedClass != null && !specifiedClass.isEmpty()) {
            logger.debug("entity map size is {}", entityMap.size());
            setSpecifiedMyEntityListByMap(entityMap, specifiedClass);
            logger.info("specified myEntity list size is {}", this.specifiedMyEntities.size());
        }
        logger.info("Task end.");
    }

    private void setSpecifiedMyEntityListByMap(Map<String, MyEntity> entityMap, String specifiedClass) {
        Set<MyEntity> myEntitySet = new HashSet<>();
        Queue<MyEntity> myEntityQueue = new LinkedList<>();
        MyEntity specifiedEntity = entityMap.get(GenericUtil.removeGeneric(specifiedClass));
        if (specifiedEntity == null) {
            logger.debug("specified class is null");
        }
        addEntityToSetAndQueue(specifiedEntity, myEntitySet, myEntityQueue);
        while (myEntityQueue.peek() != null) {
            MyEntity myEntity = myEntityQueue.poll();
            if (myEntity.getParentClass() != null && entityMap.get(myEntity.getParentClass().getClassNameWithoutGeneric()) != null) {
                addEntityToSetAndQueue(entityMap.get(myEntity.getParentClass().getClassNameWithoutGeneric()), myEntitySet, myEntityQueue);
            }
            List<MyEntity> superInterfaces = myEntity.getParentInterfaces();
            if (superInterfaces != null) {
                for (MyEntity e : superInterfaces) {
                    if (entityMap.get(e.getClassNameWithoutGeneric()) != null) {
                        addEntityToSetAndQueue(entityMap.get(e.getClassNameWithoutGeneric()), myEntitySet, myEntityQueue);
                    }
                }
            }
            List<MyEntity> subClasses = myEntity.getSubClasses();
            if (myEntity.getSubClasses() != null) {
                for (MyEntity e : subClasses) {
                    if (entityMap.get(e.getClassNameWithoutGeneric()) != null) {
                        addEntityToSetAndQueue(entityMap.get(e.getClassNameWithoutGeneric()), myEntitySet, myEntityQueue);
                    }
                }
            }
            List<MyEntity> subInterfaces = myEntity.getSubInterfaces();
            if (myEntity.getSubInterfaces() != null) {
                for (MyEntity e : subInterfaces) {
                    if (entityMap.get(e.getClassNameWithoutGeneric()) != null) {
                        addEntityToSetAndQueue(entityMap.get(e.getClassNameWithoutGeneric()), myEntitySet, myEntityQueue);
                    }
                }
            }
        }
        this.specifiedMyEntities.addAll(myEntitySet);
    }

    private void addEntityToSetAndQueue(MyEntity myEntity, Set<MyEntity> myEntitySet, Queue<MyEntity> myEntityQueue) {
        if (myEntity != null && myEntitySet != null && myEntityQueue != null) {
            if (!myEntitySet.contains(myEntity)) {
                myEntityQueue.add(myEntity);
            }
            myEntitySet.add(myEntity);
        }
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

    public List<MyEntity> getMyEntities(){
        return this.myEntities;
    }

    public List<MyEntity> getSpecifiedMyEntities(){
        return this.specifiedMyEntities;
    }
}
