package com.taogen.docs2uml.task;

import com.taogen.docs2uml.commons.entity.CommandOption;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskControllerTest {

    @Test
    public void execute() {
        CommandOption commandOption = new CommandOption("https://docs.oracle.com/javase/8/docs/api/index.html", "java.lang.invoke");
        TaskController taskController = new TaskController(commandOption);
        assertNotNull(taskController.execute());
    }
}