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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author taogen
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        CommandOption commandOption = new CommandOption();
        commandOption.setPackageName("org.springframework");
        commandOption.setTopPackageName("org.springframework");
        commandOption.setSpecifiedClass("org.springframework.beans.factory.BeanFactory");
        // scan
        Scanner scanner = new FilePathScanner();
        String rootDirPath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework";
        Predicate<Path> matchPredicate = FilePathScanner.getSpringFrameworkMatchPredicate();
        List<String> filePaths = scanner.scan(rootDirPath, matchPredicate);
        System.out.println(filePaths.stream()
//                .map(filePath -> filePath.substring(rootDirPath.length()))
                .collect(Collectors.joining("\n")));
        System.out.println(filePaths.size());
        // parse
        SourceCodeParser parser = new SourceCodeParser();
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
        writeMyEntityToFile(myEntities, "myEntities.txt");
        log.debug("myEntities size: {}", myEntities.size());
        // filter
        List<MyEntity> newEntities = new ArrayList<>();
        if (commandOption.getSpecifiedClass() != null && !commandOption.getSpecifiedClass().isEmpty()) {
            Map<String, MyEntity> classPathToEntity = myEntities.stream()
                    .collect(Collectors.toMap(myEntity -> myEntity.getId(), Function.identity()));
            MyEntity root = classPathToEntity.get(commandOption.getSpecifiedClass());
            if (root != null) {
                // link
                for (MyEntity myEntity : myEntities) {
                    log.debug("To link myEntity: {}", myEntity);
                    MyEntity parentClass = myEntity.getParentClass();
                    if (parentClass != null) {
                        log.debug("Parent class path: {}", parentClass.getId());
                        MyEntity parentClassInMap = classPathToEntity.get(parentClass.getId());
                        // Link subclasses: add current entity to the parent class's subclass list
                        if (parentClassInMap != null) {
                            // Link parentClass
                            myEntity.setParentClass(parentClassInMap);
                            List<MyEntity> subClasses = parentClassInMap.getSubClasses();
                            if (subClasses == null) {
                                subClasses = new ArrayList<>();
                                parentClassInMap.setSubClasses(subClasses);
                            }
                            if (!subClasses.contains(myEntity)) {
                                subClasses.add(myEntity);
                            }
                        }
                    }
                    // Link subinterfaces: add current entity to the parent interface's subinterface list
                    if (myEntity.getParentInterfaces() != null && !myEntity.getParentInterfaces().isEmpty()) {
                        // Link parentInterfaces (Not a good choice. Too many top interfaces cause too many classes)
//                        List<MyEntity> toRemoveParentInterfaces = new ArrayList<>();
//                        List<MyEntity> toAddParentInterfaces = new ArrayList<>();
                        for (MyEntity parentInterfaceEntity : myEntity.getParentInterfaces()) {
                            MyEntity parentInterfaceInMap = classPathToEntity.get(parentInterfaceEntity.getId());
                            if (parentInterfaceInMap != null) {
                                // Link parentInterfaces (Not a good choice. Too many top interfaces cause too many classes)
//                                toAddParentInterfaces.add(parentInterfaceInMap);
//                                toRemoveParentInterfaces.add(parentInterfaceEntity);
                                List<MyEntity> subInterfaces = parentInterfaceInMap.getSubInterfaces();
                                if (subInterfaces == null) {
                                    subInterfaces = new ArrayList<>();
                                    parentInterfaceInMap.setSubInterfaces(subInterfaces);
                                }
                                if (!subInterfaces.contains(myEntity)) {
                                    subInterfaces.add(myEntity);
                                }
                            }
                        }
                        // Link parentInterfaces (Not a good choice. Too many top interfaces cause too many classes)
//                        myEntity.getParentInterfaces().addAll(toAddParentInterfaces);
//                        myEntity.getParentInterfaces().removeAll(toRemoveParentInterfaces);
                    }
                }
//                writeMyEntityToFile(myEntities, "myEntities-linked.txt");
                log.debug("myEntities size: {}", myEntities.size());
                // traverse (To get specified class graph)
                Queue<MyEntity> queue = new LinkedList<>();
                queue.add(root);
                root.setVisited(true);
                while (!queue.isEmpty()) {
                    MyEntity current = queue.poll();
                    MyEntity myEntityInMap = classPathToEntity.get(current.getId());
                    if (myEntityInMap != null && !newEntities.contains(myEntityInMap)) {
                        newEntities.add(myEntityInMap);
                    }
                    MyEntity parentClass = current.getParentClass();
                    if (parentClass != null && !parentClass.getVisited()) {
                        queue.add(parentClass);
                        parentClass.setVisited(true);
                    }
                    List<MyEntity> parentInterfaces = current.getParentInterfaces();
                    if (parentInterfaces != null && !parentInterfaces.isEmpty()) {
                        for (MyEntity parentInterface : parentInterfaces) {
                            if (!parentInterface.getVisited()) {
                                queue.add(parentInterface);
                                parentInterface.setVisited(true);
                            }
                        }
                    }
                    List<MyEntity> subClasses = current.getSubClasses();
                    if (subClasses != null && !subClasses.isEmpty()) {
                        for (MyEntity subClass : subClasses) {
                            if (!subClass.getVisited()) {
                                queue.add(subClass);
                                subClass.setVisited(true);
                            }
                        }
                    }
                    List<MyEntity> subInterfaces = current.getSubInterfaces();
                    if (subInterfaces != null && !subInterfaces.isEmpty()) {
                        for (MyEntity subInterface : subInterfaces) {
                            if (!subInterface.getVisited()) {
                                queue.add(subInterface);
                                subInterface.setVisited(true);
                            }
                        }
                    }
                }
            }
        }
//        writeMyEntityToFile(newEntities, "myEntities-new.txt");
        log.debug("newEntities size: {}", newEntities.size());
        // generate
        Generator generator = new ClassDiagramGenerator();
        if (commandOption.getSpecifiedClass() != null) {
            generator.generate(newEntities, commandOption);
        } else {
            generator.generate(myEntities, commandOption);
        }
    }

    private static void writeMyEntityToFile(List<MyEntity> myEntities, String file) throws IOException {
        String myEntitiesString = myEntities.stream()
                .map(MyEntity::toString)
                .collect(Collectors.joining("\n"));
        FileWriter fw = new FileWriter(file);
        fw.write(myEntitiesString);
        fw.close();
    }
}
