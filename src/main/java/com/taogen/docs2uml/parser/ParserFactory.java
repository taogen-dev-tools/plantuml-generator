package com.taogen.docs2uml.parser;

import com.taogen.docs2uml.commons.constant.ParserType;
import com.taogen.docs2uml.parser.impl.ClassDetailsParser;
import com.taogen.docs2uml.parser.impl.ClassesParser;
import com.taogen.docs2uml.parser.impl.PackagesParser;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Taogen
 */
public class ParserFactory {
    private static Map<ParserType, Parser> parserMap = new EnumMap<>(ParserType.class);

    static {
        parserMap.put(ParserType.PACKAGES, new PackagesParser());
        parserMap.put(ParserType.CLASSES, new ClassesParser());
        parserMap.put(ParserType.DETAILS, new ClassDetailsParser());
    }

    private ParserFactory() {
        throw new IllegalStateException("factory class");
    }

    public static Parser create(ParserType parserType) {
        return parserMap.get(parserType);
    }
}
