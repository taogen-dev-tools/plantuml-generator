package com.taogen.docs2uml.entity;

import com.taogen.docs2uml.constant.Visibility;
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

    public static Visibility getVisibilityByContainsText(String text) {
        if (text.contains(String.valueOf(Visibility.PROTECTED).toLowerCase())) {
            return Visibility.PROTECTED;
        } else if (text.contains(String.valueOf(Visibility.PRIVATE).toLowerCase())) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PUBILC;
        }
    }
}
