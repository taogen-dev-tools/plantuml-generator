package com.taogen.docs2uml.commons.util;

import com.taogen.docs2uml.commons.util.vo.SourceCodeContent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author taogen
 */
@Slf4j
public class SourceCodeUtil {
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
            "^\\s*" +
                    ANNOTATION_PATTERN_STR +
                    "(\\w+[ ]+)?(\\w+[ ]+)?(\\w+[ ]+)?([a-zA-Z0-9$_]+)(<.+?>)?(\\[\\])?\\s+([a-zA-Z0-9$_]+)(\\s*=\\s*.+)?;");
    //    public static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(ANNOTATION_PATTERN_STR + "(\\w+[ ]+)?(\\w+[ ]+)?(\\w+[ ]+)?(<.+?>[ ]+)?([a-zA-Z0-9$_.]+)(\\[\\])?(<.+?>)?\\s+([a-zA-Z0-9$_]+)\\(\\s*.*\\)(\\s+throws\\s+.+)?\\s*\\{");
    public static final int FIELD_VISIBILITY_GROUP = 6;
    public static final int FIELD_FIRST_KEYWORD_GROUP = 7;
    public static final int FIELD_TYPE_GROUP = 9;
    public static final int FIELD_NAME_GROUP = 12;
    /**
     * Method declaration
     */
    public static final String RETURN_TYPE_PATTERN_STR = "(<.+?>[ ]+)?([a-zA-Z0-9$_.]+)(<.+?>)?(\\[\\])?";
    public static final String DO_NOT_MATCH_IN_METHOD_PATTERN_STR = "(?<![{};])\\s*\\n+\\s*";
    public static final String METHOD_PARAMS_PATTERN_STR = "\\([a-zA-Z0-9$_.,?<>@\\[\\] \\n\\t]*\\)";
    public static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            DO_NOT_MATCH_IN_METHOD_PATTERN_STR +
                    ANNOTATION_PATTERN_STR +
                    "(\\w+[ ]+)?(\\w+[ ]+)?(\\w+[ ]+)?" +
                    RETURN_TYPE_PATTERN_STR +
                    "\\s+([a-zA-Z0-9$_]+)" +
                    METHOD_PARAMS_PATTERN_STR +
                    "(\\s+throws\\s+[A-Z][a-zA-Z0-9$_]+)?(\\s*\\{|\\s*;)");
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
                    "([a-zA-Z0-9$_]+\\s+)?([a-zA-Z0-9$_]+\\s+)?([a-zA-Z0-9$_]+\\s+)?(class|interface|@interface|enum)\\s+" +
                    CLASS_NAME_WITH_GENERIC_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    PARENT_CLASS_OR_INTERFACES_PATTERN_STR +
                    "\\s+\\{"); // Pattern.DOTALL

    public static void main(String[] args) {
        System.out.println("class pattern: " + CLASS_DECLARATION_PATTERN.pattern());
        System.out.println("field pattern: " + FIELD_PATTERN.pattern());
        System.out.println("method pattern: " + METHOD_DECLARATION_PATTERN.pattern());
        System.out.println("nested class pattern: " + NESTED_CLASS_PATTERN.pattern());
    }

    /**
     * Remove comments
     * 1) // comments...
     * 2) int a; // comments
     * 3) /**
     * * comments
     * *
     *
     * @param s
     * @return
     */
    public static String removeComments(String s) {
        return s.replaceAll("//.*$", "")
                .replaceAll("^\\s*(/?\\*+).*$", "");
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
        }
        sourceCodeStr = sourceCodeStr.replaceAll(SourceCodeUtil.CLASS_DECLARATION_PATTERN.pattern(), "");
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
}
