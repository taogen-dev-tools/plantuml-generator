package com.taogen.docs2uml.commons.util;

import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.util.vo.SourceCodeContent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author taogen
 */
@Slf4j
public class SourceCodeUtil {
    public static final Map<String, EntityType> STRING_TO_ENTITY_TYPE = new HashMap<>();

    static {
        STRING_TO_ENTITY_TYPE.put("public interface", EntityType.INTERFACE);
        STRING_TO_ENTITY_TYPE.put("public class", EntityType.CLASS);
        STRING_TO_ENTITY_TYPE.put("public enum", EntityType.ENUM);
        STRING_TO_ENTITY_TYPE.put("public @interface", EntityType.ANNOTATION);
        STRING_TO_ENTITY_TYPE.put("public abstract class", EntityType.ABSTRACT);
    }

    public static final String MAX_THREE_KEYWORDS_PATTERN_STR = "(\\w+[ ]+)?(\\w+[ ]+)?(\\w+[ ]+)?";
    public static final String IDENTIFIER_PATTERN_STR = "([a-zA-Z0-9$_]+)";
    /**
     * Package
     */
    public static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+(([a-zA-Z0-9$_]+\\.?)+);");
    //    public static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+((\\w\\.?)+);");
    /**
     * Import
     */
    public static final Pattern IMPORT_PATTERN = Pattern.compile("import(\\s+static)?\\s+[a-zA-Z0-9$_]+(\\.[a-zA-Z0-9$_]+)*;");
    /**
     * Class declaration
     */
    // public static final Pattern CLASS_NAME_PATTERN = Pattern.compile("public(\\s+abstract)?\\s+(class|interface|@interface|enum)\\s+((.+?)(<.+?>)?)(\\s+(extends|implements)\\s+((.+?)(<.+?>)?))?(\\s+(extends|implements)\\s+((.+?)(<.+?>)?))?\\s*\\{");
    public static final String CLASS_NAME_WITH_GENERIC_PATTERN_STR = "(([A-Z][a-zA-Z0-9$_]*)(<.+?>)?)";
    public static final String PARENT_CLASS_OR_INTERFACES_PATTERN_STR = "(\\s+(extends|implements)\\s+(((.|[\\n])+?)(<.+?>)?))?";
    //    public static final String PARENT_CLASS_OR_INTERFACES = "(\\s+(extends|implements)\\s+(([.\t\n]+?)(<.+?>)?))?";
    public static final String ANNOTATION_PATTERN_STR = "(@" + CLASS_NAME_WITH_GENERIC_PATTERN_STR + "(\\([a-zA-Z0-9$_\"=,.{} ]+\\))?\\s*)*";
    public static final Pattern CLASS_DECLARATION_PATTERN = Pattern.compile(
            ANNOTATION_PATTERN_STR +
                    "public(\\s+abstract)?\\s+(class|interface|@interface|enum)\\s+" +
                    CLASS_NAME_WITH_GENERIC_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    "\\s+\\{"); // Pattern.DOTALL
    public static final int CLASS_NAME_WITH_GENERIC_GROUP = 8;
    public static final int CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP = 12;
    public static final int CLASS_SECOND_EXTENDS_OR_IMPLEMENTS_GROUP = CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP + 6;
    /**
     * Field declaration
     */
    public static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\s*" +
                    ANNOTATION_PATTERN_STR +
                    MAX_THREE_KEYWORDS_PATTERN_STR +
                    IDENTIFIER_PATTERN_STR +
                    "(<.+?>)?(\\[\\])?\\s+" +
                    IDENTIFIER_PATTERN_STR +
                    "(\\s*=\\s*.+)?;");
    //    public static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(ANNOTATION_PATTERN_STR + "(\\w+[ ]+)?(\\w+[ ]+)?(\\w+[ ]+)?(<.+?>[ ]+)?([a-zA-Z0-9$_.]+)(\\[\\])?(<.+?>)?\\s+([a-zA-Z0-9$_]+)\\(\\s*.*\\)(\\s+throws\\s+.+)?\\s*\\{");
    public static final int FIELD_VISIBILITY_GROUP = 6;
    public static final int FIELD_FIRST_KEYWORD_GROUP = 7;
    public static final int FIELD_TYPE_GROUP = 9;
    public static final int FIELD_NAME_GROUP = 12;
    /**
     * Method declaration
     */
    public static final String RETURN_TYPE_PATTERN_STR = "(<.+?>[ ]+)?([a-zA-Z0-9$_.]+)(<.+?>)?(\\[\\])?";
    public static final String METHOD_NAME_PATTERN_STR = "\\s+([a-zA-Z0-9$_]+)";
    public static final String DO_NOT_MATCH_IN_METHOD_PATTERN_STR = "(?<![{};])\\s*\\n*\\s*";
    public static final String METHOD_PARAMS_PATTERN_STR = "\\(([a-zA-Z0-9$_.,?<>@\\[\\] \\n\\t]*)\\)";
    public static final String THROWS_PATTERN_STR = "(\\s+throws\\s+[A-Z][a-zA-Z0-9$_, ]+)?";
    public static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            DO_NOT_MATCH_IN_METHOD_PATTERN_STR +
                    ANNOTATION_PATTERN_STR +
                    MAX_THREE_KEYWORDS_PATTERN_STR +
                    RETURN_TYPE_PATTERN_STR +
                    "(?<!return)(?<!new)(?<!throw)" +
                    METHOD_NAME_PATTERN_STR +
                    METHOD_PARAMS_PATTERN_STR +
                    THROWS_PATTERN_STR +
                    "(\\s*\\{|\\s*;)");
    public static final int METHOD_VISIBILITY_GROUP = 6;
    public static final int METHOD_FIRST_KEYWORD_GROUP = 7;
    public static final int METHOD_RETURN_TYPE_GROUP = 10;
    public static final int METHOD_NAME_GROUP = 13;
    public static final int METHOD_PARAMETER_GROUP = 14;
    /**
     * Constructor declaration
     */
    public static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile("");
    /**
     * Nested class declaration
     */
    public static final Pattern NESTED_CLASS_PATTERN = Pattern.compile(
            ANNOTATION_PATTERN_STR +
                    MAX_THREE_KEYWORDS_PATTERN_STR +
                    "(class|interface|@interface|enum)\\s+" +
                    CLASS_NAME_WITH_GENERIC_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    "\\s+\\{"); // Pattern.DOTALL

//    public static void main(String[] args) {
//        System.out.println("class pattern: " + CLASS_DECLARATION_PATTERN.pattern());
//        System.out.println("field pattern: " + FIELD_PATTERN.pattern());
//        System.out.println("method pattern: " + METHOD_DECLARATION_PATTERN.pattern());
//        System.out.println("nested class pattern: " + NESTED_CLASS_PATTERN.pattern());
//    }

    /**
     * Remove comments
     * 1) // comments...
     * 2) int a; // comments
     * 3) /**
     * * xxxx
     * \*\/
     * 4) /*
     * xxx
     * \*\/
     *
     * @param s
     * @return
     */
    public static String removeComments(String s) {
        s = s.replaceAll("(?s)\\/\\*.*?\\*\\/", "")
                .replaceAll("//.*", "");
        return s;
    }

    public static int getPairedEndedParenthesesIndex(String sourceCodeStr, int startIndex) {
        if (sourceCodeStr == null || sourceCodeStr.trim().isEmpty()) {
            return -1;
        }
        int i = startIndex;
        LinkedList<String> stack = new LinkedList<>();
        stack.push("{");
        while (!stack.isEmpty() && i < sourceCodeStr.length()) {
            if (sourceCodeStr.charAt(i) == '{') {
                stack.push("{");
            }
            if (sourceCodeStr.charAt(i) == '}') {
                stack.pop();
            }
            i++;
        }
        return i;
    }

    public static SourceCodeContent getSourceCodeContent(String sourceCodeStr) {
        if (sourceCodeStr == null) {
            return null;
        }
        SourceCodeContent sourceCodeContent = new SourceCodeContent();

        // packageDeclaration
        Matcher packageMatcher = SourceCodeUtil.PACKAGE_PATTERN.matcher(sourceCodeStr);
        if (packageMatcher.find()) {
            String packageMatch = packageMatcher.group();
            sourceCodeContent.setPackageDeclaration(packageMatch);
        }
        sourceCodeStr = sourceCodeStr.replaceAll(SourceCodeUtil.PACKAGE_PATTERN.pattern(), "");

        // imports
        Matcher importMatcher = SourceCodeUtil.IMPORT_PATTERN.matcher(sourceCodeStr);
        List<String> imports = new ArrayList<>();
        while (importMatcher.find()) {
            imports.add(importMatcher.group());
        }
        sourceCodeContent.setImports(imports);
        sourceCodeStr = sourceCodeStr.replaceAll(SourceCodeUtil.IMPORT_PATTERN.pattern(), "");

        // class
        Matcher classDeclarationMatcher = SourceCodeUtil.CLASS_DECLARATION_PATTERN.matcher(sourceCodeStr);
        if (classDeclarationMatcher.find()) {
            log.debug("class declaration match: {}", classDeclarationMatcher.group());
            sourceCodeContent.setClassDeclaration(classDeclarationMatcher.group());
        } else {
            return null;
        }
        sourceCodeStr = sourceCodeStr.replaceFirst(SourceCodeUtil.CLASS_DECLARATION_PATTERN.pattern(), "");
        sourceCodeStr = sourceCodeStr.substring(0, sourceCodeStr.lastIndexOf('}'));

        // Nested classes: 先定位内部类的声明行，然后通过括号匹配{}来获取完整的内部类。
        Matcher nestedClassMatcher = SourceCodeUtil.NESTED_CLASS_PATTERN.matcher(sourceCodeStr);
        List<String> nestedClasses = new ArrayList<>();
        while (nestedClassMatcher.find()) {
            String nestedClassMatch = nestedClassMatcher.group();
            log.debug("nestedClass match: {}", nestedClassMatch);
            int startIndex = sourceCodeStr.indexOf(nestedClassMatch);
            int i = startIndex + nestedClassMatch.length();
            int endIndex = SourceCodeUtil.getPairedEndedParenthesesIndex(sourceCodeStr, i);
            String nestedClassStr = sourceCodeStr.substring(startIndex, endIndex);
            nestedClasses.add(nestedClassStr);
        }
        sourceCodeContent.setNestedClasses(nestedClasses);
        for (String nestedClassStr : nestedClasses) {
            sourceCodeStr = sourceCodeStr.replace(nestedClassStr, "");
        }

        // Methods/Constructors: 先定位方法的声明行，然后通过括号匹配{}来获取完整的方法体。
        Matcher methodMatcher = SourceCodeUtil.METHOD_DECLARATION_PATTERN.matcher(sourceCodeStr);
        List<String> methods = new ArrayList<>();
        while (methodMatcher.find()) {
            String methodDeclarationMatch = methodMatcher.group();
            if (!isValidTopMethodMatch(sourceCodeStr, methodMatcher.start())) {
                log.debug("Not valid top method match: {}", methodDeclarationMatch);
                continue;
            }
            if (methodDeclarationMatch.trim().endsWith(";")) {
                methods.add(methodDeclarationMatch);
            } else {
                int startIndex = sourceCodeStr.indexOf(methodDeclarationMatch);
                int i = startIndex + methodDeclarationMatch.length();
                int endIndex = SourceCodeUtil.getPairedEndedParenthesesIndex(sourceCodeStr, i);
                String methodStr = sourceCodeStr.substring(startIndex, endIndex);
                methods.add(methodStr);
            }
        }
        for (String methodStr : methods) {
            sourceCodeStr = sourceCodeStr.replace(methodStr, "");
        }
        sourceCodeContent.setMethods(methods);

        // Fields：去除方法和内部类后，通过正则匹配。
        Matcher fieldMatcher = SourceCodeUtil.FIELD_PATTERN.matcher(sourceCodeStr);
        List<String> fields = new ArrayList<>();
        while (fieldMatcher.find()) {
            String fieldDeclarationMatch = fieldMatcher.group();
            fields.add(fieldDeclarationMatch);
        }
        sourceCodeContent.setFields(fields);
        for (String fieldStr : fields) {
            sourceCodeStr = sourceCodeStr.replace(fieldStr, "");
        }

        // sourceCodeStr after parsing is an empty string
        if (!sourceCodeStr.trim().isEmpty()) {
            log.debug("sourceCodeStr after parsing: {}", sourceCodeStr.trim());
        }
        return sourceCodeContent;
    }

    private static boolean isValidTopMethodMatch(String sourceCodeStr, int methodBeginIndex) {
        LinkedList<String> stack = new LinkedList<>();
        for (int i = 0; i < methodBeginIndex; i++) {
            if (sourceCodeStr.charAt(i) == '{') {
                stack.push("{");
            } else if (sourceCodeStr.charAt(i) == '}') {
                if (stack.peek().equals("{")) {
                    stack.pop();
                }
            }
            if (sourceCodeStr.charAt(i) == '"') {
                if (!stack.isEmpty() && stack.peek().equals("\"")) {
                    stack.pop();
                } else {
                    stack.push("\"");
                }
            }
        }
        return stack.isEmpty();
    }

    public static String replaceStringWithRange(String s, int startIndex, int endIndex) {
        char[] charArray = s.toCharArray();
        for (int i = startIndex; i < endIndex; i++) {
            charArray[i] = ' ';
        }
        return new String(charArray);
    }

    public static String replaceMatchContent(String s, Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();
        char[] charArray = s.toCharArray();
        for (int i = start; i < end; i++) {
            charArray[i] = ' ';
        }
        return new String(charArray);
    }

//    public static void main(String[] args) {
//        Pattern pattern = Pattern.compile("abc");
//        String s = "---abc---";
//        Matcher matcher = pattern.matcher(s);
//        matcher.find();
//        String s1 = replaceMatchContent(s, matcher);
//        System.out.println(s1);
//        System.out.println(replaceStringWithRange(s, 3, 6));
//    }

    public static List<String> splitParametersFromStr(String parameterStr) {
        if (parameterStr == null || parameterStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        LinkedList<String> stack = new LinkedList<>();
        List<Integer> separatorIndexes = new ArrayList<>();
        separatorIndexes.add(-1);
        for (int i = 0; i < parameterStr.length(); i++) {
            char c = parameterStr.charAt(i);
            if (c == '<') {
                stack.push("<");
            } else if (c == '>') {
                stack.pop();
            } else if (c == ',') {
                if (stack.isEmpty()) {
                    separatorIndexes.add(i);
                }
            }
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < separatorIndexes.size(); i++) {
            if (i < separatorIndexes.size() - 1) {
                result.add(parameterStr.substring(separatorIndexes.get(i) + 1, separatorIndexes.get(i + 1)).trim());
            } else {
                result.add(parameterStr.substring(separatorIndexes.get(i) + 1).trim());
            }
        }
        return result;
    }

//    public static void main(String[] args) {
//        System.out.println(splitParametersFromStr("Class<T> requiredType, Object... args"));
//        System.out.println(splitParametersFromStr("Class<T> requiredType"));
//        System.out.println(splitParametersFromStr("String name, @Nullable Class<T> requiredType"));
//        System.out.println(splitParametersFromStr("String name, Object bean, String name2, Object bean2"));
//        List<String> parameterStrList = splitParametersFromStr("String name, Object bean, String name2, Object bean2");
//        List<MyParameter> parameterList = parameterStrList.stream()
//                .map(MyParameter::getFromStr)
//                .collect(Collectors.toList());
//        System.out.println(parameterList);
//    }

    public static String getPackageNameFromStrByRegex(String sourceCodeStr) {
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

    public static ClassDeclaration getClassDeclarationFromStr(String sourceCodeStr) {
        if (sourceCodeStr == null || sourceCodeStr.trim().isEmpty()) {
            return null;
        }
        ClassDeclaration entity = new ClassDeclaration();
        // file type
        entity.setType(getEntityType(sourceCodeStr));
        // package name
        entity.setPackageName(SourceCodeUtil.getPackageNameFromStrByRegex(sourceCodeStr));
        // is abstract
        entity.setAbstract(EntityType.ABSTRACT.equals(entity.getType()));
        Matcher classNameMatcher = SourceCodeUtil.CLASS_DECLARATION_PATTERN.matcher(sourceCodeStr);
        if (!classNameMatcher.find()) {
            return null;
        }
        log.debug("match: {}", classNameMatcher.group());
        for (int i = 1; i <= classNameMatcher.groupCount(); i++) {
            log.trace("group {}: {}", i, classNameMatcher.group(i));
        }
        entity.setClassName(classNameMatcher.group(SourceCodeUtil.CLASS_NAME_WITH_GENERIC_GROUP));
        entity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(entity.getClassName()));
        // parent class and interfaces
        String[] lines = sourceCodeStr.split("\n");
        if (classNameMatcher.group(SourceCodeUtil.CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP) != null) {
            setParentClassOrInterfaces(lines, entity, classNameMatcher, SourceCodeUtil.CLASS_FIRST_EXTENDS_OR_IMPLEMENTS_GROUP);
        }
        if (classNameMatcher.group(SourceCodeUtil.CLASS_SECOND_EXTENDS_OR_IMPLEMENTS_GROUP) != null) {
            setParentClassOrInterfaces(lines, entity, classNameMatcher, SourceCodeUtil.CLASS_SECOND_EXTENDS_OR_IMPLEMENTS_GROUP);
        }
        return entity;
    }

    public static void setParentClassOrInterfaces(String[] lines, ClassDeclaration entity, Matcher classNameMatcher, int keywordGroup) {
        String stringValue = classNameMatcher.group(keywordGroup + 2).trim();
        log.debug("stringValue: {}", stringValue);
        if ("extends".equals(classNameMatcher.group(keywordGroup)) &&
                !EntityType.INTERFACE.equals(entity.getType())) {
            ClassDeclaration parentClass = new ClassDeclaration();
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
            entity.setParentClass(parentClass);
        } else {
            List<ClassDeclaration> parentInterfaces = GenericUtil.getClassListFromContainsGenericString(stringValue).stream()
                    .map(String::trim)
                    .map(name -> {
                        ClassDeclaration parentInterface = new ClassDeclaration();
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
                        return parentInterface;
                    })
                    .collect(Collectors.toList());
            entity.setParentInterfaces(parentInterfaces);
        }
    }

    public static EntityType getEntityType(String sourceCodeStr) {
        for (Map.Entry<String, EntityType> entry : STRING_TO_ENTITY_TYPE.entrySet()) {
            if (sourceCodeStr.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Data
    public static class ClassDeclaration {
        private EntityType type;
        private String packageName;
        private boolean isAbstract = false;
        private String className;
        private String classNameWithoutGeneric;
        private ClassDeclaration parentClass;
        private List<ClassDeclaration> parentInterfaces;
    }
}
