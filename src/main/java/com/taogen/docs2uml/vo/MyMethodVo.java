package com.taogen.docs2uml.vo;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class MyMethodVo {
    private String visibility;
    private String isStatic;
    private String isAbstract;
    private String returnType;
    private String name;
    private String params;
}
