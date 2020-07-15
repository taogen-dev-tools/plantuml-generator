package com.taogen.docs2uml;

import com.taogen.docs2uml.command.CommandHandler;
import com.taogen.docs2uml.commons.constant.CommandError;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.ErrorMessage;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.KnownException;
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
        if (!CommandError.SUCCESS_CODE.equals(errorMessage.getErrorCode())) {
            logger.error(errorMessage.getErrorMessage());
            CommandHandler.showCommandUsage();
            return;
        }
        CommandOption commandOption = commandHandler.getCommandOption();
        try {
            long beginTime = System.currentTimeMillis();
            TaskController taskController = new TaskController(commandOption);
            taskController.execute();
            List<MyEntity> myEntityList = taskController.getMyEntities();
            List<MyEntity> specifiedMyEntityList = taskController.getSpecifiedMyEntities();
            String classPath = commandOption.getTopPackageName();
            if (myEntityList == null || myEntityList.size() == 0) {
                logger.info("can't find classes from path {}", classPath);
                return;
            } else {
                if (commandOption.getSpecifiedClass() != null && (specifiedMyEntityList == null || specifiedMyEntityList.size() == 0)) {
                    classPath += "." + commandOption.getSpecifiedClass();
                    logger.info("can't find classes from path {}", classPath);
                    return;
                }
            }
            generateDiagrams(myEntityList, specifiedMyEntityList, commandOption);
            logger.info("Elapsed time: {}ms", (System.currentTimeMillis() - beginTime));
        } catch (KnownException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("System internal error!", e);
        }
    }

    private static void generateDiagrams(List<MyEntity> myEntityList, List<MyEntity> specifiedMyEntityList, CommandOption commandOption) {
        Generator generator = new ClassDiagramGenerator();
        logger.debug("commandOption is {}", commandOption.toString());
        if (doesSpecifyClass(commandOption)) {
            generator.generate(specifiedMyEntityList, commandOption);
        } else {
            generator.generate(myEntityList, commandOption);
        }
    }

    private static boolean doesSpecifyClass(CommandOption commandOption) {
        return commandOption.getSpecifiedClass() != null && !commandOption.getSpecifiedClass().isEmpty();
    }
}
