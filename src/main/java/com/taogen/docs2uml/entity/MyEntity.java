package com.taogen.docs2uml.entity;

import com.taogen.docs2uml.constant.EntityType;
import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
public class MyEntity {
    private EntityType type;
    // TODO: update doc package -> packageName
    private String packageName;
    private String className;
    private String url;
    private MyEntity parentClass;
    private List<MyEntity> parentInterfaces;
    private List<MyField> fields;
    private List<MyMethod> methods;
}
