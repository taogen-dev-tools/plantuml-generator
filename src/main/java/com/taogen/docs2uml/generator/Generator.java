package com.taogen.docs2uml.generator;

import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.exception.GeneratorException;

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
    Boolean generate(List<MyEntity> myEntities);
}
