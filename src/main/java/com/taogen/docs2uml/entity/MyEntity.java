package com.taogen.docs2uml.entity;

import com.taogen.docs2uml.constant.EntityType;
import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
// TODO: update doc. Add new fields.
public class MyEntity {
    private EntityType type;
    // TODO: update doc package -> packageName
    private String packageName;
    private Boolean isAbstract;
    private String className;
    private String url;
    private MyEntity parentClass;
    private List<MyEntity> parentInterfaces;
    private List<MyField> fields;
    private List<MyMethod> methods;
}
