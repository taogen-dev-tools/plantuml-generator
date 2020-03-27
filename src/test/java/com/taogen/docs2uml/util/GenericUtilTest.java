package com.taogen.docs2uml.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class GenericUtilTest {

    @Test
    public void removeGeneric() {
        assertEquals("Collection c", GenericUtil.removeGeneric("Collection<? super T> c"));
        assertEquals("Map map", GenericUtil.removeGeneric("Map<String,List<String>> map"));
        assertEquals("List list", GenericUtil.removeGeneric("List<T> list"));
        assertEquals("List priorityList", GenericUtil.removeGeneric("List<Locale.LanguageRange> priorityList"));
    }
}