package com.taogen.docs2uml.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taogen
 */
@Slf4j
public class GenericUtil {
    protected static final String GENERIC_LEFT_MARK = "<";
    protected static final String GENERIC_RIGHT_MARK = ">";

    private GenericUtil() {
        throw new IllegalStateException("utility class");
    }

    /**
     * Remove generic from a string
     *
     * @param src - "DynamicFileAssert<A extends DynamicFileAssert<A, F>, F extends DynamicFile>"
     * @return
     */
    public static String removeGeneric(String src) {
        if (src != null) {
            src = src.replaceAll("<[a-zA-Z,?. _<>\\[\\]]+>", "");
        }
        return src;
    }

    /**
     * Convert a string that contains generic to a list
     *
     * @param parametersText parameters string
     *                       1. extends/implements:
     *                       - "FactoryBean<Map<String, Object>>, InitializingBean"
     *                       2. methods parameters:
     *                       - "List<T> list, T oldVal, T newVal"
     *                       - "Collection<? super T> c, T... elements"
     *                       - "List<Locale.LanguageRange> priorityList, Map<String,List<String>> map"
     * @return [List<T>, T]
     */
    public static List<String> getClassListFromContainsGenericString(String parametersText) {
        log.debug("Convert a string that contains generic to a list: {}", parametersText);
        List<String> parameterEntries = new ArrayList<>();
        int indexBegin = 0;
        int indexSplit = parametersText.indexOf(',', indexBegin);
        while (indexSplit != -1) {
            int indexLeft = parametersText.indexOf('<', indexBegin);
            int indexLeft2 = indexLeft;
            int indexRight = parametersText.indexOf('>', indexBegin);
            if (indexLeft != -1 && indexRight != -1) {
                while (parametersText.substring(indexLeft2 + 1, indexRight).contains(GENERIC_LEFT_MARK)) {
                    indexLeft2 = parametersText.indexOf('<', indexLeft2 + 1);
                    indexRight = parametersText.indexOf('>', indexRight + 1);
                }
                if (indexSplit > indexLeft) {
                    indexSplit = parametersText.indexOf(',', indexRight + 1);
                    if (indexSplit == -1) {
                        indexSplit = parametersText.length();
                    }
                }
            }
            parameterEntries.add(parametersText.substring(indexBegin, indexSplit).trim());
            indexBegin = indexSplit + 1;
            indexSplit = parametersText.indexOf(',', indexBegin);
        }
        if (indexBegin < parametersText.length() - 1) {
            parameterEntries.add(parametersText.substring(indexBegin).trim());
        }
        return parameterEntries;
    }

    public static void main(String[] args) {
//        ClassDetailsParser classDetailsParser = new ClassDetailsParser();
//        List<String> values = GenericUtil.getClassListFromContainsGenericString("Collection<? super T> c, T... elements");
//        System.out.println(values);
//        System.out.println(values.size());
        String s = removeGeneric("DynamicFileAssert<A extends DynamicFileAssert<A, F>, F extends DynamicFile>");
        System.out.println(s);
    }
}
