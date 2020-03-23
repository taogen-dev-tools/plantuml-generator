package com.taogen.docs2uml.generator.impl;

import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.GeneratorException;
import com.taogen.docs2uml.generator.AbstractGenerator;
import com.taogen.docs2uml.vo.MyEntityVo;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
public class ClassDiagramGenerator extends AbstractGenerator {

    @Override
    public Boolean generate(List<MyEntity> myEntities) throws GeneratorException {
        ensureGenerateDirExists();
        Map<String, Object> params = getParameters(myEntities);
        String templateFilename = "classDiagramTemplate.ftl";
        String generateFilename = "classDiagram.txt";
        generateTemplate(templateFilename, generateFilename, params);
        return true;
    }

    private void generateTemplate(String templateFilename, String generateFilename, Map<String, Object> params) throws GeneratorException {
        try (
                Writer consoleWriter = new OutputStreamWriter(System.out);
                Writer fileWriter = new FileWriter(new File(new StringBuilder().append(GENERATE_DIRECTORY).append("/").append(generateFilename).toString()))
        ) {

            Template template = configuration.getTemplate(templateFilename);
            template.process(params, consoleWriter);
            template.process(params, fileWriter);
        } catch (TemplateException | IOException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new GeneratorException(e.getMessage());
        }
    }

    private Map<String, Object> getParameters(List<MyEntity> myEntities) {
        Map<String, Object> root = new HashMap<>();
        List<MyEntityVo> myEntityVos = convertEntityToVo(myEntities);
        root.put("entities", myEntityVos);
        return root;
    }
}
