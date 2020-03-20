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
import java.util.List;

/**
 * @author Taogen
 */
public class ClassDetailsCrawler extends AbstractCrawler {
    private static final Logger logger = LogManager.getLogger();

    public ClassDetailsCrawler() {
    }

    public ClassDetailsCrawler(MyCommand myCommand) {
        this.myCommand = myCommand;
    }

    @Override
    public List<MyEntity> crawl() {
        Document document = getDocument(this.myCommand.getUrl());
        return getContainSingleMyEntityListByDocument(document);
    }

    private List<MyEntity> getContainSingleMyEntityListByDocument(Document document) {
        if (document == null) {
            throw new FailConnectException(String.format(FAIL_TO_CONNECT_URL, this.myCommand.getUrl()));
        }
        List<MyEntity> myEntities = new ArrayList<>(1);
        String packageName = null;
        EntityType entityType = null;
        String className = null;
        MyEntity parentClass = new MyEntity();
        List<MyEntity> parentInterfaces = new ArrayList<>();
        Boolean isAbstract = null;
        List<MyField> fields = new ArrayList<>();
        List<MyMethod> methods = new ArrayList<>();

        try {
            Element headerElement = document.getElementsByClass("header").get(0);
            Element packageElement = headerElement.getElementsByClass("subTitle").last();
            Element classElement = headerElement.getElementsByClass("title").first();
            // package name
            packageName = packageElement.text();
            String[] classElementSplit = classElement.text().split(" ");
            // entity type
            entityType = EntityType.valueOf(classElementSplit[0].toUpperCase());
            // class name
            className = classElementSplit[1];
            // parent class
            parentClass.setClassName(getParentClassName(document));

            Element descriptionElement = document.getElementsByClass("description").first();
            // parent interfaces
            Elements parentInterfacesElements = descriptionElement.getElementsByTag("dd").first().getElementsByTag("a");
            if (parentInterfacesElements != null) {
                for (Element element : parentInterfacesElements) {
                    MyEntity myEntity = new MyEntity();
                    myEntity.setClassName(element.text());
                    parentInterfaces.add(myEntity);
                }
            }
            // is abstract
            Element isAbstractElement = descriptionElement.getElementsByTag("pre").first();
            isAbstract = isAbstractElement.html().contains(DecorativeKeyword.ABSTRACT);

            // fields
            Element fieldsElement = getMemberElement(document, "field.summary");
            if (fieldsElement != null) {
                fields = getFieldsByElement(fieldsElement);
            }

            // constructors
            Element constructorsElement = getMemberElement(document, "constructor.summary");
            if (constructorsElement != null) {
                methods.addAll(getConstructorsByElement(constructorsElement));
            }

            // methods
            Element methodsElement = getMemberElement(document, "method.summary");
            if (methodsElement != null) {
                methods.addAll(getMethodsByElement(methodsElement));
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, this.myCommand.getUrl()));
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
        logger.debug("myEntity is {}", classDetailsEntity);
        myEntities.add(classDetailsEntity);
        return myEntities;
    }

    private String getParentClassName(Document document) {
        String parentClassName = null;
        try {
            Element inheritanceElement = document.getElementsByClass("inheritance").first();
            Elements inheritanceElements = inheritanceElement.getElementsByTag("ul");
            Element parentClassElement = inheritanceElements.get(inheritanceElements.size() - 2).getElementsByTag("li").first();
            parentClassName = parentClassElement.text();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            logger.info("no parent class!");
        }
        return parentClassName;
    }

    private Element getMemberElement(Document document, String summaryName) {
        Element element = null;
        try {
            element = document.getElementsByAttributeValue("name", summaryName).first().parent().getElementsByClass("memberSummary").first();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            logger.info("no " + summaryName);
        }
        return element;
    }

    private List<MyField> getFieldsByElement(Element fieldsElement) {
        Elements fieldElements = fieldsElement.getElementsByTag("tr");
        fieldElements.remove(0);

        List<MyField> myFields = new ArrayList<>(fieldElements.size());
        Visibility visibility = null;
        Boolean isStatic = null;
        Boolean isFinal = null;
        String type = null;
        String name = null;
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
                throw new NotFoundElementException(String.format(NOT_FOUND_ELEMENTS_ERROR, this.myCommand.getUrl()));
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
        Visibility visibility = null;
        String methodName = null;
        for (Element constructorElement : constructorElements) {
            Elements tdElements = constructorElement.getElementsByTag("td");
            Element codeElement = null;
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
            List<MyParameter> parameters = getParametersByCodeElement(codeElement);
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

    private List<MyParameter> getParametersByCodeElement(Element codeElement) {
        String codeElementText = codeElement.text();
        String parametersText = codeElementText.substring(codeElementText.indexOf('(') + 1, codeElementText.indexOf(')'));
        List<MyParameter> myParameters = new ArrayList<>();
        if (parametersText.length() == 0) {
            return myParameters;
        }
        String[] parameterEntries = parametersText.split(",");
        for (String s : parameterEntries) {
            String[] parameterEntrySplit = s.trim().split(" ");
            myParameters.add(new MyParameter(parameterEntrySplit[0], parameterEntrySplit[1]));
        }
        return myParameters;
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
            myMethod.setParams(getParametersByCodeElement(tdElements.get(1).getElementsByTag("code").first()));
            methods.add(myMethod);
        }
        return methods;
    }
}
