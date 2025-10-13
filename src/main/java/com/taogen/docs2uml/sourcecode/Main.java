package com.taogen.docs2uml.sourcecode;

import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.generator.Generator;
import com.taogen.docs2uml.generator.impl.ClassDiagramGenerator;
import com.taogen.docs2uml.sourcecode.parser.SourceCodeParser;
import com.taogen.docs2uml.sourcecode.scanner.FilePathScanner;
import com.taogen.docs2uml.sourcecode.scanner.Scanner;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author taogen
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new FilePathScanner();
        SourceCodeParser parser = new SourceCodeParser();
        String rootDirPath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework";
        Predicate<Path> matchPredicate = FilePathScanner.getSpringFrameworkMatchPredicate();
        List<String> filePaths = scanner.scan(rootDirPath, matchPredicate);
        System.out.println(filePaths.stream()
//                .map(filePath -> filePath.substring(rootDirPath.length()))
                .collect(Collectors.joining("\n")));
        System.out.println(filePaths.size());
        List<MyEntity> myEntities = filePaths.stream()
                .map(filePath -> {
                    try {
                        return parser.parse(filePath, null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String myEntitiesString = myEntities.stream()
                .map(MyEntity::toString)
                .collect(Collectors.joining("\n"));
        FileWriter fw = new FileWriter("myEntities.txt");
        fw.write(myEntitiesString);
        fw.close();
        Generator generator = new ClassDiagramGenerator();
        CommandOption commandOption = new CommandOption();
        commandOption.setPackageName("org.springframework");
        commandOption.setTopPackageName("com.taogen.docs2uml");
        generator.generate(myEntities, commandOption);
    }
}
