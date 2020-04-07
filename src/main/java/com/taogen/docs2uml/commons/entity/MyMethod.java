package com.taogen.docs2uml.commons.entity;

import com.taogen.docs2uml.commons.constant.Visibility;
import lombok.Data;

import java.util.List;

/**
 * @author Taogen
 */
@Data
public class MyMethod {
    private Visibility visibility;
    private Boolean isStatic;
    private Boolean isAbstract;
    private String returnType;
    private String name;
    private List<MyParameter> params;
}
