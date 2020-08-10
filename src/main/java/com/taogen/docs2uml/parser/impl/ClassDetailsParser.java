package com.taogen.docs2uml.parser.impl;

import com.taogen.docs2uml.commons.constant.DecorativeKeyword;
import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.constant.Visibility;
import com.taogen.docs2uml.commons.entity.*;
import com.taogen.docs2uml.commons.exception.FailConnectException;
import com.taogen.docs2uml.commons.exception.NotFoundElementException;
import com.taogen.docs2uml.commons.util.GenericUtil;
import com.taogen.docs2uml.parser.AbstractParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Taogen
 */
public class ClassDetailsParser extends AbstractParser {
    private static final Logger logger = LogManager.getLogger();
    private static final String OBJECT_CLASS_PATH = "java.lang.Object";

    @Override
    public List<MyEntity> parse(Object document, CommandOption commandOption) {
        checkDocumentInstance(document);
        logger.info("Parsing {}", commandOption.getUrl());
        List<MyEntity> myEntities = getContainSingleMyEntityListByDocument((Document) document, commandOption);
        return myEntities;
    }

    private List<MyEntity> getContainSingleMyEntityListByDocument(Document document, CommandOption commandOption) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, commandOption.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>(1);
        String packageName;
        EntityType entityType;
        String className;
        String classNameWithoutGeneric;
        MyEntity parentClass = new MyEntity();
        List<MyEntity> parentInterfaces = new ArrayList<>();
        boolean isAbstract;
        List<MyField> fields = new ArrayList<>();
        List<MyMethod> methods = new ArrayList<>();
        List<MyEntity> subClasses = new ArrayList<>();
        List<MyEntity> subInterfaces = new ArrayList<>();

        try {
            Element headerElement = document.getElementsByClass("header").get(0);
            Element packageElement = headerElement.getElementsByClass("subTitle").last();
            Element classElement = headerElement.getElementsByClass("title").first();
            Element descriptionElement = document.getElementsByClass("description").first();
            String classElementText = classElement.text();
            // entity type
            entityType = EntityType.valueOf(classElementText.split(" ")[0].toUpperCase());
            logger.debug("entity type: {}", entityType);
            // class name
            className = classElementText.substring(classElementText.indexOf(' ') + 1);
            if (EntityType.ANNOTATION.equals(entityType)) {
                int index = className.indexOf(' ');
                className = className.substring(index + 1);
            }
            if (className.contains(GENERIC_LEFT_MARK) && className.contains("extends")) {
                className = className.substring(0, className.indexOf("extends") - 1) + GENERIC_RIGHT_MARK;
            }
            classNameWithoutGeneric = GenericUtil.removeGeneric(className);
            logger.debug("class name: {}", className);
            // parent class
            parentClass = getParentClassFromDescriptionPre(entityType, descriptionElement);
            // package name
            packageName = packageElement.text();
            logger.debug("package name: {}", packageName);
            // super interfaces
            parentInterfaces.addAll(getSuperInterfaces(entityType, descriptionElement));
            // is abstract
            Element isAbstractElement = descriptionElement.getElementsByTag("pre").first();
            isAbstract = isAbstractElement.html().contains(DecorativeKeyword.ABSTRACT);
            // fields
            Element fieldsElement = getMemberElement(document, "field.summary", className);
            if (fieldsElement != null) {
                fields = getFieldsByElement(fieldsElement, commandOption);
            }
            // constructors
            Element constructorsElement = getMemberElement(document, "constructor.summary", className);
            if (constructorsElement != null) {
                methods.addAll(getConstructorsByElement(constructorsElement));
            }
            // methods
            Element methodsElement = getMemberElement(document, "method.summary", className);
            if (methodsElement != null) {
                methods.addAll(getMethodsByElement(methodsElement));
            }
            // subClasses
            subClasses.addAll(getSubClasses(descriptionElement));
            // subInterfaces
            subInterfaces.addAll(getSubInterfaces(descriptionElement));
        } catch (IndexOutOfBoundsException | NullPointerException | NotFoundElementException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
        }

        MyEntity classDetailsEntity = new MyEntity();
        classDetailsEntity.setPackageName(packageName);
        classDetailsEntity.setType(entityType);
        classDetailsEntity.setClassName(className);
        classDetailsEntity.setClassNameWithoutGeneric(classNameWithoutGeneric);
        classDetailsEntity.setParentClass(parentClass);
        classDetailsEntity.setParentInterfaces(parentInterfaces);
        classDetailsEntity.setIsAbstract(isAbstract);
        classDetailsEntity.setFields(fields);
        classDetailsEntity.setMethods(methods);
        classDetailsEntity.setSubClasses(subClasses);
        classDetailsEntity.setSubInterfaces(subInterfaces);
        myEntities.add(classDetailsEntity);
        return myEntities;
    }

    /**
     * Deprecated
     * @deprecated
     * @param document
     * @param className
     * @param commandOption
     * @return
     */
    private String getParentClassName(Document document, String className, CommandOption commandOption) {
        String parentClassName = null;
        try {
            Element inheritanceElement = document.getElementsByClass("inheritance").first();
            Elements inheritanceElements = inheritanceElement.getElementsByTag("a");
            Element parentClassElement = inheritanceElements.get(inheritanceElements.size() - 1);
            parentClassName = parentClassElement.text();
            if (parentClassName.startsWith(commandOption.getTopPackageName())) {
                int packageIndex = parentClassName.lastIndexOf('.');
                parentClassName = parentClassName.substring(packageIndex + 1);
            } else {
                if (parentClassName.contains(OBJECT_CLASS_PATH)) {
                    return null;
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.debug("No parent class in {}", className);
        }
        return parentClassName;
    }

    private List<MyEntity> getSuperInterfaces(EntityType entityType, Element descriptionElement) {
//        return getClassListFromDescription(descriptionElement, Arrays.asList("Superinterfaces", "Implemented Interfaces"));
        return getSuperInterfacesFromDescriptionPre(entityType, descriptionElement);
    }

    private List<MyEntity> getSubClasses(Element descriptionElement) {
        return getClassListFromDescription(descriptionElement, Arrays.asList("Implementing Classes", "Subclasses"));
    }

    private List<MyEntity> getSubInterfaces(Element descriptionElement) {
        return getClassListFromDescription(descriptionElement, Arrays.asList("Subinterfaces"));
    }

    private MyEntity getParentClassFromDescriptionPre(EntityType entityType, Element descriptionElement) {
        Element preElement = descriptionElement.getElementsByTag("pre").first();
        String preElementText = preElement.text();
        String[] preElementSplit = preElementText.split("\n");
        if (EntityType.CLASS.equals(entityType) && preElementSplit.length > 1) {
            String target = "extends";
            int indexOfExtends = preElementSplit[1].indexOf(target);
            if (indexOfExtends != -1) {
                String extendsText = preElementSplit[1].substring(indexOfExtends + target.length());
                List<String> classNames = getClassListFromContainsGenericString(extendsText);
                if (classNames != null && classNames.size() >= 1 && !"Object".equals(classNames.get(0))) {
                    MyEntity myEntity = new MyEntity();
                    myEntity.setClassName(classNames.get(0));
                    myEntity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(myEntity.getClassName()));
                    return myEntity;
                }
            }
        }
        return null;
    }

    private List<MyEntity> getSuperInterfacesFromDescriptionPre(EntityType entityType, Element descriptionElement) {
        List<MyEntity> classList = new ArrayList<>();
        Element preElement = descriptionElement.getElementsByTag("pre").first();
        String preElementText = preElement.text();
        String interfaceTarget = "implements";
        if (EntityType.INTERFACE.equals(entityType)) {
            interfaceTarget = "extends";
        }
        int indexOfSuperInterface = preElementText.indexOf(interfaceTarget);
        if (indexOfSuperInterface != -1) {
            String superInterfacesText = preElementText.substring(indexOfSuperInterface + interfaceTarget.length());
            List<String> classNames = getClassListFromContainsGenericString(superInterfacesText);
            for (String name : classNames) {
                MyEntity myEntity = new MyEntity();
                myEntity.setClassName(name);
                myEntity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(myEntity.getClassName()));
                classList.add(myEntity);
            }
        }
        return classList;
    }

    private List<MyEntity> getClassListFromDescription(Element descriptionElement, List<String> descriptions) {
        List<MyEntity> classList = new ArrayList<>();
        Elements dlElements = descriptionElement.getElementsByTag("dl");
        String classesText = null;
        for (Element dl : dlElements) {
            String title = dl.getElementsByTag("dt").first().text();
            for (String description : descriptions) {
                if (title.contains(description)) {
                    classesText = dl.getElementsByTag("dd").first().text();
                    break;
                }
            }
        }
        if (classesText != null) {
            List<String> classNames = getClassListFromContainsGenericString(classesText);
            for (String name : classNames) {
                MyEntity myEntity = new MyEntity();
                myEntity.setClassName(name);
                myEntity.setClassNameWithoutGeneric(GenericUtil.removeGeneric(myEntity.getClassName()));
                classList.add(myEntity);
            }
        }
        return classList;
    }

    private Element getMemberElement(Document document, String summaryName, String className) {
        Element element = null;
        try {
            element = document.getElementsByAttributeValue("name", summaryName).first().parent().getElementsByClass("memberSummary").first();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.debug(new StringBuilder().append("No ").append(summaryName.substring(0, summaryName.indexOf('.'))).append(" in ").append(className));
        }
        return element;
    }

    private List<MyField> getFieldsByElement(Element fieldsElement, CommandOption commandOption) {
        Elements fieldElements = fieldsElement.getElementsByTag("tr");
        fieldElements.remove(0);

        List<MyField> myFields = new ArrayList<>(fieldElements.size());
        Visibility visibility;
        boolean isStatic;
        boolean isFinal;
        String type;
        String name;
        for (Element fieldElement : fieldElements) {
            MyField myField = new MyField();
            try {
                Element decorativeElement = fieldElement.getElementsByTag("td").first().getElementsByTag("code").first();
                String decorativeText = decorativeElement.text();
                isStatic = decorativeText.contains(DecorativeKeyword.STATIC);
                isFinal = decorativeText.contains(DecorativeKeyword.FINAL);
                visibility = MyField.getVisibilityByContainsText(decorativeText);
                String[] decorativeTextSplit = decorativeText.split(" ");
                type = decorativeTextSplit[decorativeTextSplit.length - 1];
                Element nameElement = fieldElement.getElementsByClass("memberNameLink").first().getElementsByTag("a").first();
                name = nameElement.text();
                myField.setVisibility(visibility);
                myField.setType(type);
                myField.setName(name);
                myField.setIsFinal(isFinal);
                myField.setIsStatic(isStatic);
                myFields.add(myField);
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
                throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, commandOption.getUrl()));
            }
        }
        return myFields;
    }

    private List<MyMethod> getConstructorsByElement(Element constructorsElement) {
        Elements constructorElements = constructorsElement.getElementsByTag("tr");
        constructorElements.remove(0);

        List<MyMethod> constructors = new ArrayList<>(constructorElements.size());
        boolean isStatic = false;
        boolean isAbstract = false;
        Visibility visibility;
        String methodName;
        for (Element constructorElement : constructorElements) {
            Elements tdElements = constructorElement.getElementsByTag("td");
            Element codeElement;
            // TODO: Unverified. We assume all parameters are in `<a>` tag. It's an uncertain result.
            if (tdElements.size() > 1) {
                String decorativeText = tdElements.first().getElementsByTag("code").first().text();
                visibility = MyField.getVisibilityByContainsText(decorativeText);
                isStatic = decorativeText.contains(DecorativeKeyword.STATIC);
                isAbstract = decorativeText.contains(DecorativeKeyword.ABSTRACT);
                codeElement = tdElements.get(1).getElementsByTag("code").first();
            } else {
                visibility = Visibility.PUBILC;
                codeElement = tdElements.first().getElementsByTag("code").first();
            }
            methodName = codeElement.getElementsByTag("a").first().text();
            List<MyParameter> parameters = getParametersByCodeElement(codeElement, methodName);
            MyMethod constructor = new MyMethod();
            constructor.setIsAbstract(isAbstract);
            constructor.setIsStatic(isStatic);
            constructor.setName(methodName);
            constructor.setVisibility(visibility);
            constructor.setParams(parameters);
            constructor.setReturnType(null);
            constructors.add(constructor);
        }
        return constructors;
    }

    private List<MyParameter> getParametersByCodeElement(Element codeElement, String methodName) {
        String codeElementText = codeElement.text();
        String parametersText = codeElementText.substring(codeElementText.indexOf('(') + 1, codeElementText.indexOf(')'));
        List<MyParameter> myParameters = new ArrayList<>();
        if (parametersText.length() == 0) {
            return myParameters;
        }
        List<String> parameterEntries = new ArrayList<>();
        if (!parametersText.contains(GENERIC_LEFT_MARK)) {
            parameterEntries = Arrays.asList(parametersText.split(","));
        } else {
            parameterEntries = getClassListFromContainsGenericString(parametersText);
        }
        for (String entry : parameterEntries) {
            int splitIndex = entry.lastIndexOf(' ');
            try {
                myParameters.add(new MyParameter(entry.substring(0, splitIndex), entry.substring(splitIndex + 1)));
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.debug("error parameter of {} is {}", methodName, entry);
            }
        }
        return myParameters;
    }

    /**
     * @param parametersText parameters string
     * @return
     * @testcase - replaceAll(List<T> list, T oldVal, T newVal)
     * - addAll(Collection<? super T> c, T... elements)
     * - mapEquivalents(List<Locale.LanguageRange> priorityList, Map<String,List<String>> map)
     */
    private List<String> getClassListFromContainsGenericString(String parametersText) {
        List<String> parameterEntries = new ArrayList<>();
        int indexBegin = 0;
        int indexSplit = parametersText.indexOf(',', indexBegin);
        while (indexSplit != -1) {
            int indexLeft = parametersText.indexOf('<', indexBegin);
            int indexLeft2 = indexLeft;
            int indexRight = parametersText.indexOf('>', indexBegin);
            if (indexLeft != -1 && indexRight != -1) {
                while (parametersText.substring(indexLeft2 + 1, indexRight).contains(GENERIC_LEFT_MARK)) {
                    indexLeft2 = parametersText.indexOf('<', indexLeft2 + 1);
                    indexRight = parametersText.indexOf('>', indexRight + 1);
                }
                if (indexSplit > indexLeft) {
                    indexSplit = parametersText.indexOf(',', indexRight + 1);
                    if (indexSplit == -1) {
                        indexSplit = parametersText.length();
                    }
                }
            }
            parameterEntries.add(parametersText.substring(indexBegin, indexSplit).trim());
            indexBegin = indexSplit + 1;
            indexSplit = parametersText.indexOf(',', indexBegin);
        }
        if (indexBegin < parametersText.length() - 1) {
            parameterEntries.add(parametersText.substring(indexBegin).trim());
        }
        return parameterEntries;
    }

    private List<MyMethod> getMethodsByElement(Element methodsElement) {
        Elements methodElements = methodsElement.getElementsByTag("tr");
        methodElements.remove(0);
        List<MyMethod> methods = new ArrayList<>(methodElements.size());
        for (Element methodElement : methodElements) {
            Elements tdElements = methodElement.getElementsByTag("td");
            String decorativeText = tdElements.first().getElementsByTag("code").first().text();
            String[] decorativeTextSplit = decorativeText.trim().split(" ");
            MyMethod myMethod = new MyMethod();
            myMethod.setIsStatic(decorativeText.contains(DecorativeKeyword.STATIC));
            myMethod.setIsAbstract(decorativeText.contains(DecorativeKeyword.ABSTRACT));
            myMethod.setVisibility(MyField.getVisibilityByContainsText(decorativeText));
            myMethod.setReturnType(decorativeTextSplit[decorativeTextSplit.length - 1]);
            myMethod.setName(tdElements.get(1).getElementsByTag("code").first().getElementsByTag("a").first().text());
            myMethod.setParams(getParametersByCodeElement(tdElements.get(1).getElementsByTag("code").first(), myMethod.getName()));
            methods.add(myMethod);
        }
        return methods;
    }
}
