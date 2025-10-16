package com.taogen.docs2uml.commons.constant;

/**
 * @author Taogen
 */
public enum Visibility {
    /**
     * public
     */
    PUBILC,
    /**
     * protected
     */
    PROTECTED,
    /**
     * default
     */
    DEFAULT,
    /**
     * private
     */
    PRIVATE;

    public static Visibility getVisibilityByContainsText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Visibility.DEFAULT;
        }
        if (text.contains(String.valueOf(Visibility.PROTECTED).toLowerCase())) {
            return Visibility.PROTECTED;
        } else if (text.contains(String.valueOf(Visibility.PRIVATE).toLowerCase())) {
            return Visibility.PRIVATE;
        } else if (text.contains(String.valueOf(Visibility.PUBILC).toLowerCase())) {
            return Visibility.PUBILC;
        } else {
            return Visibility.DEFAULT;
        }
    }

}
