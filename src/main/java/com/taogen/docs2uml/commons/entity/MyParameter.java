package com.taogen.docs2uml.commons.entity;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class MyParameter {
    private String type;
    private String name;

    public MyParameter(){}
    public MyParameter(String type, String name){
        this.type = type;
        this.name = name;
    }
}
