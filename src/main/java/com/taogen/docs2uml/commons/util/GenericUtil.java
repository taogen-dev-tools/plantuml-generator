package com.taogen.docs2uml.commons.util;

/**
 * @author Taogen
 */
public class GenericUtil {
    private GenericUtil() {
        throw new IllegalStateException("utility class");
    }

    public static String removeGeneric(String src) {
        if (src != null) {
            src = src.replaceAll("<[a-zA-Z,?. _<>]+>", "");
        }
        return src;
    }
}
