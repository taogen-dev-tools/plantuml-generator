package com.taogen.docs2uml.sourcecode.scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author taogen
 */
public interface Scanner {
    List<String> scan(String root, Predicate<Path> matchPredicate) throws IOException;
}
