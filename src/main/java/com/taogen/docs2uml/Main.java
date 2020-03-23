package com.taogen.docs2uml;

import com.taogen.docs2uml.command.CommandHandler;
import com.taogen.docs2uml.constant.CommandError;
import com.taogen.docs2uml.entity.ErrorMessage;
import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.KnownException;
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
        CommandHandler commandHandler = new CommandHandler(args);
        ErrorMessage errorMessage = commandHandler.check();
        if (! CommandError.SUCCESS_CODE.equals(errorMessage.getErrorCode())){
            logger.error(errorMessage.getErrorMessage());
            CommandHandler.showCommandUsage();
            return;
        }
        MyCommand myCommand = commandHandler.getMyCommand();
        try {
            long beginTime = System.currentTimeMillis();
            TaskController taskController = new TaskController(myCommand);
            List<MyEntity> myEntityList = taskController.execute();
            Generator generator = new ClassDiagramGenerator();
            generator.generate(myEntityList);
            logger.info("Elapsed time: {}ms", (System.currentTimeMillis() - beginTime));
        }catch (Exception e){
            if (e instanceof KnownException){
                logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            }else{
                logger.error("System internal error!", e);
            }
        }
    }

}
