package com.taogen.docs2uml.generator.impl;

import com.taogen.docs2uml.entity.MyCommand;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.task.TaskController;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClassDiagramGeneratorTest {

    private ClassDiagramGenerator classDiagramGenerator = new ClassDiagramGenerator();

    @Test
    public void generate() {
        MyCommand myCommand = new MyCommand("https://docs.oracle.com/javase/8/docs/api/index.html", "java.lang.invoke");
        TaskController taskController = new TaskController(myCommand);
        List<MyEntity> myEntities = taskController.execute();
        assertTrue(classDiagramGenerator.generate(myEntities));
    }
}