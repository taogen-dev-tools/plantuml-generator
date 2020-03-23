package com.taogen.docs2uml.task;

import com.taogen.docs2uml.entity.MyCommand;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskControllerTest {

    @Test
    public void execute() {
        MyCommand myCommand = new MyCommand("https://docs.oracle.com/javase/8/docs/api/index.html", "java.lang.invoke");
        TaskController taskController = new TaskController(myCommand);
        assertNotNull(taskController.execute());
    }
}