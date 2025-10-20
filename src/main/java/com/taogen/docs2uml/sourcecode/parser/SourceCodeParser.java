package com.taogen.docs2uml.sourcecode.parser;

import com.taogen.docs2uml.commons.constant.DecorativeKeyword;
import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.constant.Visibility;
import com.taogen.docs2uml.commons.entity.*;
import com.taogen.docs2uml.commons.util.SourceCodeUtil;
import com.taogen.docs2uml.commons.util.vo.SourceCodeContent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 *
 * @author taogen
 */
@Slf4j
@Data
public class SourceCodeParser {
    private int getSourceCodeContentElapsedTime = 0;


    public static void main(String[] args) throws IOException {
        SourceCodeParser sourceCodeParser = new SourceCodeParser();
//        String filePath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework/spring-context/src/main/java/org/springframework/context/support/ClassPathXmlApplicationContext.java";
//        String filePath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework/spring-context/src/main/java/org/springframework/context/support/GenericApplicationContext.java";
//        String filePath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultListableBeanFactory.java";
        //===================================================
        // TODO
        // 1. normal. If use DOTALL mode, it will match to the last {
//        String filePath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework/spring-beans/src/main/java/org/springframework/beans/factory/config/YamlMapFactoryBean.java";
        // 2. contains tab -> fix: using DOTALL
        String filePath = "/Users/taogen/var/cs/repositories/personal/dev/taogen-code-source-learning/spring-framework/spring-context/src/main/java/org/springframework/context/ApplicationContext.java";
        //===================================================
        sourceCodeParser.parse(filePath, null);
    }

    public MyEntity parse(String filePath, CommandOption commandOption) throws IOException {
        log.debug("Parsing file {}", filePath);
        MyEntity entity = null;
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {
            String sourceCodeStr = br.lines()
                    .filter(item -> item != null && !item.trim().isEmpty())
                    .collect(Collectors.joining(System.lineSeparator()));
            sourceCodeStr = SourceCodeUtil.removeComments(sourceCodeStr);
//            log.debug("parse() sourceCodeStr:\n{}", sourceCodeStr);
            entity = parseSourceCodeByRegex(sourceCodeStr, filePath, commandOption);
        }
        if (entity == null) {
            return null;
        }
        return entity;
    }

    private MyEntity parseSourceCodeByRegex(String sourceCodeStr, String filePath, CommandOption commandOption) {
        SourceCodeContent sourceCodeContent = null;
        long start = System.currentTimeMillis();
        sourceCodeContent = SourceCodeUtil.getSourceCodeContent(sourceCodeStr);
        if  (sourceCodeContent == null) {
            log.warn("Failed to parse file: {}", filePath);
        }
        long elapsedTime = System.currentTimeMillis() - start;
        getSourceCodeContentElapsedTime += elapsedTime;
//            log.info("getSourceCodeContent Elapsed time: {}", elapsedTime);
        // class declaration
        SourceCodeUtil.ClassDeclaration classDeclaration = SourceCodeUtil.getClassDeclarationFromStr(sourceCodeStr);
        MyEntity entity = MyEntity.getFromClassDeclaration(classDeclaration);
        if (entity == null || entity.getType() == null ||
                entity.getPackageName() == null || entity.getClassName() == null) {
            log.warn("Failed to parse file: {}", filePath);
            return null;
        }
        if (commandOption.isMembersDisplayed() || commandOption.isFieldsDisplayed() || commandOption.isDependenciesDisplayed()) {
            // fields
//            String fieldStrings = sourceCodeContent.getFields().stream().collect(Collectors.joining("\n"));
            log.debug("field size: {}", sourceCodeContent.getFields().size());
            entity.setFields(getFieldList(sourceCodeContent.getFields(), commandOption, entity));
        }
        if  (commandOption.isMembersDisplayed() || commandOption.isMethodsDisplayed()) {
            // methods
//            String methodStrings = sourceCodeContent.getMethods().stream().collect(Collectors.joining("\n"));
            log.debug("method size: {}", sourceCodeContent.getMethods().size());
            entity.setMethods(getMethodList(sourceCodeContent.getMethods(), commandOption));
        }
        // dependencies
        if (commandOption.isDependenciesDisplayed()) {
            entity.setDependencies(getDependencies(entity.getFields(), sourceCodeContent.getImports(), commandOption));
        }

        log.debug("myEntity: {}", entity);
        return entity;
    }

    private List<MyEntity> getDependencies(List<MyField> fields, List<String> imports, CommandOption commandOption) {
        List<MyEntity> dependencies = new ArrayList<>();
        Set<String> existingDependencies = new HashSet<>();
        Set<String> exclusiveTypes = new HashSet<>(Arrays.asList("byte", "int", "long", "short", "double", "float", "boolean", "char", "String"));
        String topPackageName = commandOption.getTopPackageName();
        String[] split = topPackageName.split(".");
        if (split.length > 2) {
            topPackageName = split[0] +  "." + split[1];
        }
        for  (MyField field : fields) {
            String type = field.getType();
            if (exclusiveTypes.contains(type)) {
                continue;
            }
            for (String importStr : imports) {
                String classPath = importStr.replace("import ", "").replace(";", "").trim();
                String className = classPath.substring(classPath.lastIndexOf(".") + 1);
                if (className.equals(type) && classPath.startsWith(topPackageName) &&
                        !existingDependencies.contains(classPath)) {
                    MyEntity dependency = new MyEntity();
                    dependency.setId(classPath);
                    dependency.setType(EntityType.CLASS);
                    dependency.setPackageName(classPath.substring(0, classPath.lastIndexOf(".")));
                    dependency.setClassName(className);
                    dependency.setClassNameWithoutGeneric(dependency.getClassName());
                    dependencies.add(dependency);
                    existingDependencies.add(classPath);
                }
            }
        }
        return dependencies;
    }

    private List<MyMethod> getMethodList(List<String> methodStrings, CommandOption commandOption) {
        List<MyMethod> methodList = new ArrayList<>();
        for (String methodString : methodStrings) {
            Matcher methodMatcher = SourceCodeUtil.METHOD_DECLARATION_PATTERN.matcher(methodString);
            if (methodMatcher.find()) {
                MyMethod myMethod = new MyMethod();
                myMethod.setName(methodMatcher.group(SourceCodeUtil.METHOD_NAME_GROUP));
                myMethod.setReturnType(methodMatcher.group(SourceCodeUtil.METHOD_RETURN_TYPE_GROUP));
                String parameterStr = methodMatcher.group(SourceCodeUtil.METHOD_PARAMETER_GROUP);
                List<MyParameter> parameterList = MyParameter.getParaListFromStr(parameterStr);
                myMethod.setParams(parameterList);
                myMethod.setVisibility(Visibility.getVisibilityByContainsText(
                        methodMatcher.group(SourceCodeUtil.METHOD_VISIBILITY_GROUP)));
                Set<String> keywords = new HashSet<>();
                if (methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP) != null) {
                    keywords.add(methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP).trim());
                }
                if (methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP + 1) != null) {
                    keywords.add(methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP + 1).trim());
                }
                if (methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP + 2) != null) {
                    keywords.add(methodMatcher.group(SourceCodeUtil.METHOD_FIRST_KEYWORD_GROUP + 2).trim());
                }
                myMethod.setIsStatic(keywords.contains("static"));
                myMethod.setIsAbstract(keywords.contains("abstract"));
                methodList.add(myMethod);
            }
        }
        if (commandOption.isOnlyPublicMethodsDisplayed()) {
            methodList.removeIf(method -> !Visibility.PUBILC.equals(method.getVisibility()));
        }
        return methodList;
    }

    private List<MyField> getFieldList(List<String> fieldStrings, CommandOption commandOption, MyEntity entity) {
        List<MyField> fieldList = new ArrayList<>();
        for  (String fieldString : fieldStrings) {
            Matcher matcher = SourceCodeUtil.FIELD_PATTERN.matcher(fieldString);
            if (matcher.find()) {
                MyField field = new MyField();
                String type = matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP);
                if (matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 1) != null && !matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 1).trim().isEmpty()) {
                    type += matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 1);
                }
                if (matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 2) != null && !matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 2).trim().isEmpty()) {
                    type += matcher.group(SourceCodeUtil.FIELD_TYPE_GROUP + 2);
                }
                field.setType(type);
                field.setName(matcher.group(SourceCodeUtil.FIELD_NAME_GROUP));
                if (EntityType.INTERFACE.equals(entity.getType())) {
                    field.setVisibility(Visibility.PUBILC);
                    field.setIsStatic(true);
                    field.setIsFinal(true);
                } else {
                    field.setVisibility(Visibility.getVisibilityByContainsText(matcher.group(SourceCodeUtil.FIELD_VISIBILITY_GROUP)));
                    Set<String> keywords = new HashSet<>();
                    if (matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP) != null) {
                        keywords.add(matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP).trim());
                    }
                    if (matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP + 1) != null) {
                        keywords.add(matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP + 1).trim());
                    }
                    if (matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP + 2) != null) {
                        keywords.add(matcher.group(SourceCodeUtil.FIELD_FIRST_KEYWORD_GROUP + 2).trim());
                    }
                    field.setIsStatic(keywords.contains(DecorativeKeyword.STATIC));
                    field.setIsFinal(keywords.contains(DecorativeKeyword.FINAL));
                }
                fieldList.add(field);
            }
        }
        if (commandOption.isStaticFieldExcluded()) {
            fieldList.removeIf(MyField::getIsStatic);
        }
        return fieldList;
    }
}
