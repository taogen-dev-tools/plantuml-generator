package com.taogen.docs2uml.parser;

import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;

import java.util.List;

/**
 * @author Taogen
 */
public interface Parser {
    /**
     * parsing document to My Entity
     * @param resource
     * @param commandOption
     * @return
     */
    List<MyEntity> parse(Object resource, CommandOption commandOption);
}
