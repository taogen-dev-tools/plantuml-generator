package com.taogen.docs2uml.sourcecode;

import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.util.CommandLineUtil;
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
        long start = System.currentTimeMillis();
        CommandOption commandOption = new CommandOption();
        commandOption.setMembersDisplayed(true);
        commandOption.setPackageName("org.springframework");
        commandOption.setTopPackageName("org.springframework");
        commandOption.setSpecifiedClass("org.springframework.beans.factory.BeanFactory");
        String rootDirPath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework";
        commandOption.setRootDirPath(rootDirPath);
        // scan files
        Predicate<Path> matchPredicate = FilePathScanner.getSpringFrameworkMatchPredicate();
        List<String> filePaths = scanFiles(commandOption.getRootDirPath(), matchPredicate);
        // parse files to myEntities
        List<MyEntity> myEntities = parseFilesToMyEntities(filePaths, commandOption);
        // filter myEntities for the specified class
        List<MyEntity> specifiedMyEntities = filterMyEntitiesForSpecifiedClass(myEntities, commandOption);
        // generate plantUML text
        String outputFilePath = generatePlantUmlText(myEntities, specifiedMyEntities, commandOption);
        CommandLineUtil.executeCommandLine("java -DPLANTUML_LIMIT_SIZE=100000 -jar plantuml.jar " + outputFilePath);
        log.info("Elapsed time: {} ms", System.currentTimeMillis() - start);
    }

    private static List<String> scanFiles(String rootDirPath, Predicate<Path> matchPredicate) throws IOException {
        Scanner scanner = new FilePathScanner();
        List<String> filePaths = scanner.scan(rootDirPath, matchPredicate);
        log.debug("filePaths: {}", filePaths.stream()
//                .map(filePath -> filePath.substring(rootDirPath.length()))
                .collect(Collectors.joining("\n")));
        log.debug("filePath Size: {}", filePaths.size());
        return filePaths;
    }

    private static List<MyEntity> parseFilesToMyEntities(List<String> filePaths, CommandOption commandOption) throws IOException {
        SourceCodeParser parser = new SourceCodeParser();
        List<MyEntity> myEntities = filePaths.stream()
                .map(filePath -> {
                    try {
                        return parser.parse(filePath, commandOption);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        writeMyEntityToFile(myEntities, "myEntities.txt");
        log.debug("myEntities size: {}", myEntities.size());
        log.info("getSourceCodeContent Elapsed time: {}", parser.getGetSourceCodeContentElapsedTime());
        return myEntities;
    }

    private static void writeMyEntityToFile(List<MyEntity> myEntities, String file) throws IOException {
        String myEntitiesString = myEntities.stream()
                .map(MyEntity::toString)
                .collect(Collectors.joining("\n"));
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(myEntitiesString);
        }
    }

    private static List<MyEntity> filterMyEntitiesForSpecifiedClass(List<MyEntity> myEntities, CommandOption commandOption) {
        if (commandOption.getSpecifiedClass() == null || commandOption.getSpecifiedClass().isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, MyEntity> classPathToEntity = myEntities.stream()
                .collect(Collectors.toMap(MyEntity::getId, Function.identity()));
        MyEntity root = classPathToEntity.get(commandOption.getSpecifiedClass());
        if (root == null) {
            return new ArrayList<>();
        }
        linkMyEntities(myEntities, classPathToEntity);
        refreshDependencies(myEntities, classPathToEntity);
        return getSpecifiedMyEntitiesFromLinkedMyEntities(root, classPathToEntity);
    }

    private static void refreshDependencies(List<MyEntity> myEntities, Map<String, MyEntity> classPathToEntity) {
        for (MyEntity myEntity : myEntities) {
            List<MyEntity> toRemove = new ArrayList<>();
            List<MyEntity> toAdd = new ArrayList<>();
            List<MyEntity> dependencies = myEntity.getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                for (MyEntity dependency : dependencies) {
                    MyEntity dependencyInMap = classPathToEntity.get(dependency.getId());
                    if (dependencyInMap != null) {
                        toAdd.add(dependencyInMap);
                        toRemove.add(dependency);
                    }
                }
                dependencies.removeAll(toRemove);
                dependencies.addAll(toAdd);
            }
        }
    }

    private static List<MyEntity> getSpecifiedMyEntitiesFromLinkedMyEntities(MyEntity root, Map<String, MyEntity> classPathToEntity) {
        List<MyEntity> specifiedMyEntities = new ArrayList<>();
        // traverse (To get specified class graph)
        Queue<MyEntity> queue = new LinkedList<>();
        queue.add(root);
        root.setVisited(true);
        while (!queue.isEmpty()) {
            MyEntity current = queue.poll();
            MyEntity myEntityInMap = classPathToEntity.get(current.getId());
            if (myEntityInMap != null && !specifiedMyEntities.contains(myEntityInMap)) {
                specifiedMyEntities.add(myEntityInMap);
            }
            enqueueParentClass(queue, current);
            enqueueParentInterfaces(queue, current);
            enqueueSubClasses(queue, current);
            enqueueSubInterfaces(queue, current);
        }
//        writeMyEntityToFile(specifiedMyEntities, "myEntities-new.txt");
        log.debug("newEntities size: {}", specifiedMyEntities.size());
        return specifiedMyEntities;
    }

    private static void enqueueSubInterfaces(Queue<MyEntity> queue, MyEntity current) {
        List<MyEntity> subInterfaces = current.getSubInterfaces();
        if (subInterfaces != null && !subInterfaces.isEmpty()) {
            for (MyEntity subInterface : subInterfaces) {
                if (!subInterface.isVisited()) {
                    queue.add(subInterface);
                    subInterface.setVisited(true);
                }
            }
        }
    }

    private static void enqueueSubClasses(Queue<MyEntity> queue, MyEntity current) {
        List<MyEntity> subClasses = current.getSubClasses();
        if (subClasses != null && !subClasses.isEmpty()) {
            for (MyEntity subClass : subClasses) {
                if (!subClass.isVisited()) {
                    queue.add(subClass);
                    subClass.setVisited(true);
                }
            }
        }
    }

    private static void enqueueParentInterfaces(Queue<MyEntity> queue, MyEntity current) {
        List<MyEntity> parentInterfaces = current.getParentInterfaces();
        if (parentInterfaces != null && !parentInterfaces.isEmpty()) {
            for (MyEntity parentInterface : parentInterfaces) {
                if (!parentInterface.isVisited()) {
                    queue.add(parentInterface);
                    parentInterface.setVisited(true);
                }
            }
        }
    }

    private static void enqueueParentClass(Queue<MyEntity> queue, MyEntity current) {
        MyEntity parentClass = current.getParentClass();
        if (parentClass != null && !parentClass.isVisited()) {
            queue.add(parentClass);
            parentClass.setVisited(true);
        }

    }

    private static void linkMyEntities(List<MyEntity> myEntities, Map<String, MyEntity> classPathToEntity) {
        // link
        for (MyEntity myEntity : myEntities) {
            log.debug("To link myEntity: {}", myEntity.getId());
            linkClasses(myEntity, classPathToEntity);
            linkInterfaces(myEntity, classPathToEntity);
        }
//                writeMyEntityToFile(myEntities, "myEntities-linked.txt");
        log.debug("myEntities size after linked: {}", myEntities.size());
    }

    private static void linkInterfaces(MyEntity myEntity, Map<String, MyEntity> classPathToEntity) {
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

    private static void linkClasses(MyEntity myEntity, Map<String, MyEntity> classPathToEntity) {
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
    }

    private static String generatePlantUmlText(List<MyEntity> myEntities,
                                             List<MyEntity> specifiedMyEntities,
                                             CommandOption commandOption) {
        Generator generator = new ClassDiagramGenerator();
        if (commandOption.getSpecifiedClass() != null) {
            return generator.generate(specifiedMyEntities, commandOption);
        } else {
            return generator.generate(myEntities, commandOption);
        }
    }
}
