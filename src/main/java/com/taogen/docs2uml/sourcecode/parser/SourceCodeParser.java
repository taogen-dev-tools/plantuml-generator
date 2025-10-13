package com.taogen.docs2uml.sourcecode.parser;

import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.MyEntity;
import com.taogen.docs2uml.commons.util.GenericUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author taogen
 */
@Slf4j
public class SourceCodeParser {
    public static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+((\\w\\.?)+);");
    // public static final Pattern CLASS_NAME_PATTERN = Pattern.compile("public(\\s+abstract)?\\s+(class|interface|@interface|enum)\\s+((.+?)(<.+?>)?)(\\s+(extends|implements)\\s+((.+?)(<.+?>)?))?(\\s+(extends|implements)\\s+((.+?)(<.+?>)?))?\\s*\\{");
    public static final String CLASS_NAME_PATTERN_STR = "(class|interface|@interface|enum)\\s+((.+?)(<.+?>)?)";
    public static final String PARENT_CLASS_OR_INTERFACES_PATTERN_STR = "(\\s+(extends|implements)\\s+((.+?)(<.+?>)?))?";
    //    public static final String PARENT_CLASS_OR_INTERFACES = "(\\s+(extends|implements)\\s+(([.\t\n]+?)(<.+?>)?))?";
    public static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "public(\\s+abstract)?\\s+" +
                    CLASS_NAME_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    "\\s+\\{"); // Pattern.DOTALL
    public static final int CLASS_NAME_GROUP = 4;
    public static final int FIRST_PARENT_OR_INTERFACE = 7;
    public static final int SECOND_PARENT_OR_INTERFACE = 12;

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
        MyEntity entity = new MyEntity();
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {
            String s = br.lines().map(str -> str.replaceAll("//.*", "")).collect(Collectors.joining(System.lineSeparator()));
            log.debug(s);
            // type
            Map<String, EntityType> stringToEntityType = new HashMap<>();
            stringToEntityType.put("public interface", EntityType.INTERFACE);
            stringToEntityType.put("public class", EntityType.CLASS);
            stringToEntityType.put("public enum", EntityType.ENUM);
            stringToEntityType.put("public @interface", EntityType.ANNOTATION);
            stringToEntityType.put("public abstract class", EntityType.ABSTRACT);
            for (String key : stringToEntityType.keySet()) {
                if (s.contains(key)) {
                    entity.setType(stringToEntityType.get(key));
                }
            }
            // package name
            Matcher packageMatcher = PACKAGE_PATTERN.matcher(s);
            if (packageMatcher.find()) {
                entity.setPackageName(packageMatcher.group(1));
                log.debug(packageMatcher.group());
                for (int i = 1; i < packageMatcher.groupCount(); i++) {
                    log.debug(packageMatcher.group(i));
                }
            }
            // is abstract
            entity.setIsAbstract(EntityType.ABSTRACT.equals(entity.getType()));
            // class name
            Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(s);
            if (classNameMatcher.find()) {
                log.debug("match: {}", classNameMatcher.group());
                for (int i = 1; i <= classNameMatcher.groupCount(); i++) {
                    log.debug("group {}: {}", i, classNameMatcher.group(i));
                }
                entity.setClassName(classNameMatcher.group(CLASS_NAME_GROUP));
                // parent class and interfaces
                if (classNameMatcher.group(FIRST_PARENT_OR_INTERFACE) != null) {
                    setParentClassOrInterfaces(entity, classNameMatcher, FIRST_PARENT_OR_INTERFACE);
                }
                if (classNameMatcher.group(SECOND_PARENT_OR_INTERFACE) != null) {
                    setParentClassOrInterfaces(entity, classNameMatcher, SECOND_PARENT_OR_INTERFACE);
                }
            } else {
                log.warn("Cannot parse file {}", filePath);
                return null;
            }
            entity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(entity.getClassName()));
            // fields
            // methods
            // subClasses
            // subInterfaces
            log.debug("myEntity: {}", entity);
        }
        return entity;
    }

    private void setParentClassOrInterfaces(MyEntity entity, Matcher classNameMatcher, int keywordGroup) {
        String stringValue = classNameMatcher.group(keywordGroup + 1).trim();
        log.debug("stringValue: {}", stringValue);
        if ("extends".equals(classNameMatcher.group(keywordGroup)) &&
                !EntityType.INTERFACE.equals(entity.getType())) {
            MyEntity parentClass = new MyEntity();
            parentClass.setClassName(stringValue);
            parentClass.setClassNameWithoutGeneric(GenericUtil.removeGeneric(parentClass.getClassName()));
            entity.setParentClass(parentClass);
        } else {
            List<MyEntity> parentInterfaces = GenericUtil.getClassListFromContainsGenericString(stringValue).stream()
                    .map(String::trim)
                    .map(name -> {
                        MyEntity parentInterface = new MyEntity();
                        parentInterface.setClassName(name);
                        parentInterface.setClassNameWithoutGeneric(GenericUtil.removeGeneric(parentInterface.getClassName()));
                        return parentInterface;
                    })
                    .collect(Collectors.toList());
            entity.setParentInterfaces(parentInterfaces);
        }
    }
}
