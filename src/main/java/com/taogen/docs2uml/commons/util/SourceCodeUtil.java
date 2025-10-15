package com.taogen.docs2uml.commons.util;

/**
 *
 * @author taogen
 */
public class SourceCodeUtil {
    /**
     * Remove comments
     * 1) // comments...
     * 2) int a; // comments
     * 3) /**
     * * comments
     * *
     *
     * @param s
     * @return
     */
    public static String removeComments(String s) {
        return s.replaceAll("//.*$", "")
                .replaceAll("^\\s*(/?\\*+).*$", "");
    }
}
