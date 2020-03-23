package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.constant.DecorativeKeyword;
import com.taogen.docs2uml.constant.EntityType;
import com.taogen.docs2uml.constant.Visibility;
import com.taogen.docs2uml.crawler.AbstractCrawler;
import com.taogen.docs2uml.entity.*;
import com.taogen.docs2uml.exception.FailConnectException;
import com.taogen.docs2uml.exception.NotFoundElementException;
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
public class ClassDetailsCrawler extends AbstractCrawler {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<MyEntity> crawl(MyCommand myCommand) {
        logger.info("Begin to parse {}", myCommand.getUrl());
        Document document = getDocument(myCommand.getUrl());

        List<MyEntity> myEntities = getContainSingleMyEntityListByDocument(document, myCommand);
        logger.info("End to parse {}", myEntities.get(0).getClassName());
        return myEntities;
    }

    private List<MyEntity> getContainSingleMyEntityListByDocument(Document document, MyCommand myCommand) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, myCommand.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>(1);
        String packageName;
        EntityType entityType;
        String className;
        MyEntity parentClass = new MyEntity();
        List<MyEntity> parentInterfaces = new ArrayList<>();
        boolean isAbstract;
        List<MyField> fields = new ArrayList<>();
        List<MyMethod> methods = new ArrayList<>();

        try {
            Element headerElement = document.getElementsByClass("header").get(0);
            Element packageElement = headerElement.getElementsByClass("subTitle").last();
            Element classElement = headerElement.getElementsByClass("title").first();
            Element descriptionElement = document.getElementsByClass("description").first();
            String[] classElementSplit = classElement.text().split(" ");
            // class name
            className = classElementSplit[1];
            // entity type
            entityType = EntityType.valueOf(classElementSplit[0].toUpperCase());
            // parent class
            parentClass.setClassName(getParentClassName(document, className));
            // package name
            packageName = packageElement.text();
            // super interfaces
            parentInterfaces.addAll(getSuperInterfaces(descriptionElement, className));
            // is abstract
            Element isAbstractElement = descriptionElement.getElementsByTag("pre").first();
            isAbstract = isAbstractElement.html().contains(DecorativeKeyword.ABSTRACT);
            // fields
            Element fieldsElement = getMemberElement(document, "field.summary", className);
            if (fieldsElement != null) {
                fields = getFieldsByElement(fieldsElement, myCommand);
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
        } catch (IndexOutOfBoundsException | NullPointerException | NotFoundElementException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
        }

        MyEntity classDetailsEntity = new MyEntity();
        classDetailsEntity.setPackageName(packageName);
        classDetailsEntity.setType(entityType);
        classDetailsEntity.setClassName(className);
        classDetailsEntity.setParentClass(parentClass);
        classDetailsEntity.setParentInterfaces(parentInterfaces);
        classDetailsEntity.setIsAbstract(isAbstract);
        classDetailsEntity.setFields(fields);
        classDetailsEntity.setMethods(methods);
        myEntities.add(classDetailsEntity);
        return myEntities;
    }

    private String getParentClassName(Document document, String className) {
        String parentClassName = null;
        try {
            Element inheritanceElement = document.getElementsByClass("inheritance").first();
            Elements inheritanceElements = inheritanceElement.getElementsByTag("ul");
            Element parentClassElement = inheritanceElements.get(inheritanceElements.size() - 2).getElementsByTag("li").first();
            parentClassName = parentClassElement.text();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.debug("No parent class in {}", className);
        }
        return parentClassName;
    }

    private List<MyEntity> getSuperInterfaces(Element descriptionElement, String className) {
        List<MyEntity> superInterfaces = new ArrayList<>();
        Elements dlElemnets = descriptionElement.getElementsByTag("dl");
        Elements parentInterfacesElements = null;
        for (Element dl : dlElemnets) {
            String title = dl.getElementsByTag("dt").first().text();
            if (title.contains("Superinterfaces") || title.contains("Implemented Interfaces")) {
                parentInterfacesElements = dl.getElementsByTag("dd").first().getElementsByTag("a");
                break;
            }
        }
        if (parentInterfacesElements != null) {
            for (Element element : parentInterfacesElements) {
                MyEntity myEntity = new MyEntity();
                myEntity.setClassName(element.text());
                superInterfaces.add(myEntity);
            }
        } else {
            logger.debug("No parent interfaces in {}", className);
        }
        return superInterfaces;
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

    private List<MyField> getFieldsByElement(Element fieldsElement, MyCommand myCommand) {
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
                throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, myCommand.getUrl()));
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
            parameterEntries = handleDifficultParameterList(parametersText);
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
     * parameters string test case:
     * - replaceAll(List<T> list, T oldVal, T newVal)
     * - addAll(Collection<? super T> c, T... elements)
     * - mapEquivalents(List<Locale.LanguageRange> priorityList, Map<String,List<String>> map)
     * @return
     */
    private List<String> handleDifficultParameterList(String parametersText) {
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
            parameterEntries.add(parametersText.substring(indexBegin));
        }
        return  parameterEntries;
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
