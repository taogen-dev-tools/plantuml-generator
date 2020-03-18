package com.taogen.docs2uml.entity;

import com.taogen.docs2uml.constant.Visibility;

import java.util.List;

/**
 * @author Taogen
 */
public class MyMethod {
    private Visibility visibility;
    private Boolean isStatic;
    private Boolean isAbstract;
    private String returnType;
    private String name;
    private List<MyParameter> params;
}
