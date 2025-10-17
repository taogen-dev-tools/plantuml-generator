package com.taogen.docs2uml.sourcecode.parser;

import com.taogen.docs2uml.commons.constant.DecorativeKeyword;
import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.constant.Visibility;
import com.taogen.docs2uml.commons.entity.*;
import com.taogen.docs2uml.commons.util.GenericUtil;
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
    public static final Map<String, EntityType> STRING_TO_ENTITY_TYPE = new HashMap<>();

    static {
        STRING_TO_ENTITY_TYPE.put("public interface", EntityType.INTERFACE);
        STRING_TO_ENTITY_TYPE.put("public class", EntityType.CLASS);
        STRING_TO_ENTITY_TYPE.put("public enum", EntityType.ENUM);
        STRING_TO_ENTITY_TYPE.put("public @interface", EntityType.ANNOTATION);
        STRING_TO_ENTITY_TYPE.put("public abstract class", EntityType.ABSTRACT);
    }


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
                    .map(SourceCodeUtil::removeComments)
                    .filter(item -> item != null && !item.trim().isEmpty())
                    .collect(Collectors.joining(System.lineSeparator()));
//            log.debug("sourceCodeStr:\n{}", sourceCodeStr);
            entity = parseSourceCodeByRegex(sourceCodeStr, filePath, commandOption);
        }
        if (entity == null) {
            return null;
        }
        return entity;
    }

    private MyEntity parseSourceCodeByRegex(String sourceCodeStr, String filePath, CommandOption commandOption) {
        MyEntity entity = new MyEntity();
        entity.setUrl(filePath);
        String[] lines = sourceCodeStr.split("\n");
        // type
        entity.setType(getEntityType(sourceCodeStr));
        if (entity.getType() == null) {
            log.warn("No entity type found for file {}", filePath);
            return null;
        }
        SourceCodeContent sourceCodeContent = null;
        if (commandOption.isMembersDisplayed()) {
            long start = System.currentTimeMillis();
            sourceCodeContent = SourceCodeUtil.getSourceCodeContent(sourceCodeStr);
            long elapsedTime = System.currentTimeMillis() - start;
            getSourceCodeContentElapsedTime += elapsedTime;
        }
//            log.info("getSourceCodeContent Elapsed time: {}", elapsedTime);
        // package name
        entity.setPackageName(getPackageName(sourceCodeStr));
        if (entity.getPackageName() == null) {
            log.warn("No package name found for file {}", filePath);
        }
        // is abstract
        entity.setIsAbstract(EntityType.ABSTRACT.equals(entity.getType()));
        // class name
        Matcher classNameMatcher = SourceCodeUtil.CLASS_DECLARATION_PATTERN.matcher(sourceCodeStr);
        if (classNameMatcher.find()) {
            log.debug("match: {}", classNameMatcher.group());
            for (int i = 1; i <= classNameMatcher.groupCount(); i++) {
                log.trace("group {}: {}", i, classNameMatcher.group(i));
            }
            entity.setClassName(classNameMatcher.group(SourceCodeUtil.CLASS_NAME_WITH_GENERIC_GROUP));
            // parent class and interfaces
            if (classNameMatcher.group(SourceCodeUtil.CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP) != null) {
                setParentClassOrInterfaces(lines, entity, classNameMatcher, SourceCodeUtil.CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP);
            }
            if (classNameMatcher.group(SourceCodeUtil.CLASS_SECOND_EXTENDS_OR_IMPLEMENTS_GROUP) != null) {
                setParentClassOrInterfaces(lines, entity, classNameMatcher, SourceCodeUtil.CLASS_SECOND_EXTENDS_OR_IMPLEMENTS_GROUP);
            }
        } else {
            log.warn("Cannot parse file {}", filePath);
            return null;
        }
        entity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(entity.getClassName()));
        entity.setId(entity.getPackageName() + "." + entity.getClassNameWithoutGeneric());
        if (commandOption.isMembersDisplayed()) {
            // fields
            String fieldStrings = sourceCodeContent.getFields().stream().collect(Collectors.joining("\n"));
            entity.setFields(getFieldList(fieldStrings));
            // methods
            String methodStrings = sourceCodeContent.getMethods().stream().collect(Collectors.joining("\n"));
            if (entity.getClassNameWithoutGeneric().equals("BeanFactory")) {
                log.debug("method size: {}", sourceCodeContent.getMethods().size());
                log.debug("methodStrings: \n{}", methodStrings);
            }
            entity.setMethods(getMethodList(methodStrings));
        }
        log.debug("myEntity: {}", entity);
        return entity;
    }

    private List<MyMethod> getMethodList(String methodStrings) {
        Matcher methodMatcher = SourceCodeUtil.METHOD_DECLARATION_PATTERN.matcher(methodStrings);
        List<MyMethod> methodList = new ArrayList<>();
        while (methodMatcher.find()) {
            MyMethod myMethod = new MyMethod();
            myMethod.setName(methodMatcher.group(SourceCodeUtil.METHOD_NAME_GROUP));
            myMethod.setReturnType(methodMatcher.group(SourceCodeUtil.METHOD_RETURN_TYPE_GROUP));
            String parameterStr = methodMatcher.group(SourceCodeUtil.METHOD_PARAMETER_GROUP);
            List<String> parameterStrList = SourceCodeUtil.splitParametersFromStr(parameterStr);
            List<MyParameter> parameterList = parameterStrList.stream()
                    .map(MyParameter::getFromStr)
                    .collect(Collectors.toList());
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
        return methodList;
    }

    private List<MyField> getFieldList(String sourceCodeStr) {
        Matcher matcher = SourceCodeUtil.FIELD_PATTERN.matcher(sourceCodeStr);
        List<MyField> fieldList = new ArrayList<>();
        while (matcher.find()) {
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
            fieldList.add(field);
        }
        return fieldList;
    }

    private EntityType getEntityType(String sourceCodeStr) {
        for (Map.Entry<String, EntityType> entry : STRING_TO_ENTITY_TYPE.entrySet()) {
            if (sourceCodeStr.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getPackageName(String sourceCodeStr) {
        Matcher packageMatcher = SourceCodeUtil.PACKAGE_PATTERN.matcher(sourceCodeStr);
        if (packageMatcher.find()) {
            log.debug("match: {}", packageMatcher.group());
            for (int i = 1; i < packageMatcher.groupCount(); i++) {
                log.trace("group {}: {}", i, packageMatcher.group(i));
            }
            return packageMatcher.group(1);
        } else {
            return null;
        }
    }

    private void setParentClassOrInterfaces(String[] lines, MyEntity entity, Matcher classNameMatcher, int keywordGroup) {
        String stringValue = classNameMatcher.group(keywordGroup + 2).trim();
        log.debug("stringValue: {}", stringValue);
        if ("extends".equals(classNameMatcher.group(keywordGroup)) &&
                !EntityType.INTERFACE.equals(entity.getType())) {
            MyEntity parentClass = new MyEntity();
            parentClass.setClassName(stringValue);
            parentClass.setClassNameWithoutGeneric(GenericUtil.removeGeneric(parentClass.getClassName()));
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].contains("." + parentClass.getClassNameWithoutGeneric() + ";")) {
                    parentClass.setPackageName(lines[i].substring(lines[i].indexOf("import ") + "import ".length(), lines[i].indexOf(parentClass.getClassNameWithoutGeneric()) - 1));
                }
            }
            if (parentClass.getPackageName() == null) {
                parentClass.setPackageName(entity.getPackageName());
            }
            parentClass.setId(parentClass.getPackageName() + "." + parentClass.getClassNameWithoutGeneric());
            entity.setParentClass(parentClass);
        } else {
            List<MyEntity> parentInterfaces = GenericUtil.getClassListFromContainsGenericString(stringValue).stream()
                    .map(String::trim)
                    .map(name -> {
                        MyEntity parentInterface = new MyEntity();
                        parentInterface.setClassName(name);
                        parentInterface.setClassNameWithoutGeneric(GenericUtil.removeGeneric(parentInterface.getClassName()));
                        for (int i = 1; i < lines.length; i++) {
                            if (lines[i].contains("." + parentInterface.getClassNameWithoutGeneric() + ";")) {
                                parentInterface.setPackageName(lines[i].substring(lines[i].indexOf("import ") + "import ".length(), lines[i].indexOf(parentInterface.getClassNameWithoutGeneric()) - 1));
                            }
                        }
                        if (parentInterface.getPackageName() == null) {
                            parentInterface.setPackageName(entity.getPackageName());
                        }
                        parentInterface.setId(parentInterface.getPackageName() + "." + parentInterface.getClassNameWithoutGeneric());
                        return parentInterface;
                    })
                    .collect(Collectors.toList());
            entity.setParentInterfaces(parentInterfaces);
        }
    }
}
