package com.taogen.docs2uml.commons.util;

/**
 * @author Taogen
 */
public class GenericUtil {
    private GenericUtil(){
        throw new IllegalStateException("utility class");
    }

    public static String removeGeneric(String src){
        return src.replaceAll("<[a-zA-Z,?. <>]+>", "");
    }
}
