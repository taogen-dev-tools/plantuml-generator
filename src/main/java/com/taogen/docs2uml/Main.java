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
            List<MyEntity> myEntityList = taskController.execute();
            Generator generator = new ClassDiagramGenerator();
            generator.generate(myEntityList, commandOption);
            generateMore(generator, myEntityList, commandOption);
            logger.info("Elapsed time: {}ms", (System.currentTimeMillis() - beginTime));
        } catch (KnownException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("System internal error!", e);
        }
    }

    private static void generateMore(Generator generator, List<MyEntity> myEntityList, CommandOption commandOption) {
        if (commandOption.getMembers() != null && !commandOption.getMembers()) {
            commandOption.setMembers(true);
            generator.generate(myEntityList, commandOption);
        }
        if (commandOption.getSpecifiedClass() != null && !commandOption.getSpecifiedClass().isEmpty()){
            commandOption.setSpecifiedClass(null);
            commandOption.setMembers(true);
            generator.generate(myEntityList, commandOption);
            commandOption.setMembers(false);
            generator.generate(myEntityList, commandOption);
        }
    }
}
