package com.taogen.docs2uml.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
public class MyEntityVo {
    private String type;
    private String isAbstract;
    private String className;
    private String parentClass;
    private List<String> parentInterfaces;
    private List<MyFieldVo> fields;
    private List<MyMethodVo> methods;
}
