package com.taogen.docs2uml.generator.impl;

import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.GeneratorException;
import com.taogen.docs2uml.generator.AbstractGenerator;
import com.taogen.docs2uml.vo.MyEntityVo;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taogen
 */
public class ClassDiagramGenerator extends AbstractGenerator {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Boolean generate(List<MyEntity> myEntities) {
        logger.info("Start generating...");
        Map<String, Object> params = getParameters(myEntities);
        String templateFilename = "classDiagramTemplate.ftl";
        String generateFilename = "classDiagram.txt";
        generateTemplate(templateFilename, generateFilename, params);
        logger.info("PlantUML text have generated to {}", Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + generateFilename);
        logger.info("End generating.");
        return true;
    }

    private void generateTemplate(String templateFilename, String generateFilename, Map<String, Object> params) {
        try (
                Writer fileWriter = new FileWriter(new File(generateFilename))
        ) {
            Template template = getTemplate(templateFilename);
            template.process(params, fileWriter);
            fileWriter.flush();
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
