package com.taogen.docs2uml.sourcecode.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author taogen
 */
public class FilePathScanner implements Scanner {
    public static void main(String[] args) throws IOException {
        FilePathScanner filePathScanner = new FilePathScanner();
        String rootDirPath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework";
        Predicate<Path> matchPredicate = getSpringFrameworkMatchPredicate();
        List<String> filePaths = filePathScanner.scan(rootDirPath, matchPredicate);
        System.out.println(filePaths.stream()
//                .map(filePath -> filePath.substring(rootDirPath.length()))
                .collect(Collectors.joining("\n")));
        System.out.println(filePaths.size());
    }

    public static Predicate<Path> getSpringFrameworkMatchPredicate() {
        String sourceCodePathPart = "src/main/java/org/springframework";
        Set<String> exclusiveFileNames = new HashSet<>(
                Arrays.asList("package-info.java"));
        Set<String> includedPath = new HashSet<>(
                Arrays.asList(sourceCodePathPart)
        );
        Set<String> exclusivePath = new HashSet<>(
                Arrays.asList("org/springframework/build/",
                        "org/springframework/docs/",
                        "org/springframework/mail/",
                        "org/springframework/scheduling/",
                        "org/springframework/ui/",
                        "org/springframework/jmx/",
                        "org/springframework/jndi/",
                        "org/springframework/scripting/",
                        "spring-framework/spring-webflux/",
                        "org/springframework/aot/",
                        "org/springframework/expression/",
                        "org/springframework/jdbc/",
                        "org/springframework/jms/",
                        "org/springframework/messaging/",
                        "org/springframework/orm/",
                        "org/springframework/oxm/",
                        "org/springframework/r2dbc/",
                        "org/springframework/test/",
                        "org/springframework/mock/",
                        "org/springframework/dao/",
                        "org/springframework/transaction/",
                        "/org/springframework/web/",
                        "org/springframework/validation/",
                        "org/springframework/http/",
                        "org/springframework/jca/",
                        "org/springframework/cache/",
                        "org/springframework/aop/",
                        "org/springframework/format/"));
        Predicate<Path> matchPredicate = path -> {
            String fileName = path.getFileName().toString();
            String filePath = path.toAbsolutePath().toString();
            return fileName.endsWith(".java") &&
                    !exclusiveFileNames.contains(fileName) &&
                    includedPath.stream().anyMatch(filePath::contains) &&
                    exclusivePath.stream().noneMatch(filePath::contains);
        };
        return matchPredicate;
    }

    @Override
    public List<String> scan(String rootDirPath, Predicate<Path> matchPredicate) throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get(rootDirPath))) {
            return files.filter(matchPredicate::test)
                    .map(file -> file.toAbsolutePath().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
