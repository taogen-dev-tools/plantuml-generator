package com.taogen.docs2uml.generator;

import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.exception.GeneratorException;

import java.util.List;

/**
 * @author Taogen
 */
public interface Generator {
    /**
     * generate PlantUML text
     * @param myEntities
     * @return
     * @throws GeneratorException
     */
    Boolean generate(List<MyEntity> myEntities) throws GeneratorException;
}
