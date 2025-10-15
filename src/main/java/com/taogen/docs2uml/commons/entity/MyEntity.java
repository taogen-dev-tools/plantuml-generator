package com.taogen.docs2uml.commons.entity;

import com.taogen.docs2uml.commons.constant.EntityType;
import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
// TODO: update doc. Add new fields.
public class MyEntity {
    /**
     * id: class path
     */
    private String id;
    private EntityType type;
    // TODO: update doc package -> packageName
    private String packageName;
    private Boolean isAbstract;
    private String className;
    private String classNameWithoutGeneric;
    private String url;
    private MyEntity parentClass;
    private List<MyEntity> parentInterfaces;
    private List<MyField> fields;
    private List<MyMethod> methods;
    private List<MyEntity> subClasses;
    private List<MyEntity> subInterfaces;
    private Boolean visited = false;
}
