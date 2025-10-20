package com.taogen.docs2uml.generator;

import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;

import java.util.List;

/**
 * @author Taogen
 */
public interface Generator {
    /**
     * generate PlantUML text
     *
     * @param myEntities
     * @param commandOption
     * @return The output text file path
     */
    String generate(List<MyEntity> myEntities, CommandOption commandOption);
}
