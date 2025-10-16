package com.taogen.docs2uml.commons.entity;

import com.taogen.docs2uml.commons.constant.Visibility;
import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class MyField {
    private Visibility visibility;
    private Boolean isStatic;
    private Boolean isFinal;
    private String type;
    private String name;
}
