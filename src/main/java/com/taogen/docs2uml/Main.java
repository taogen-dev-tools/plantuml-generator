package com.taogen.docs2uml;

import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.generator.Generator;
import com.taogen.docs2uml.generator.impl.ClassDiagramGenerator;
import com.taogen.docs2uml.task.TaskController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author Taogen
 */
public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();
        TaskController taskController = new TaskController(new MyCommand("https://docs.oracle.com/javase/8/docs/api/index.html", "java.io"));
        List<MyEntity> myEntityList = taskController.execute();
        Generator generator = new ClassDiagramGenerator();
        generator.generate(myEntityList);
        logger.info("Elapsed time is {}" + (System.currentTimeMillis() - beginTime));
    }

}
