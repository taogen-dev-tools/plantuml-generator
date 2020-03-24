package com.taogen.docs2uml.generator;

import com.taogen.docs2uml.constant.DecorativeKeyword;
import com.taogen.docs2uml.constant.Visibility;
import com.taogen.docs2uml.entity.MyEntity;
import com.taogen.docs2uml.entity.MyField;
import com.taogen.docs2uml.entity.MyMethod;
import com.taogen.docs2uml.entity.MyParameter;
import com.taogen.docs2uml.exception.GeneratorException;
import com.taogen.docs2uml.vo.MyEntityVo;
import com.taogen.docs2uml.vo.MyFieldVo;
import com.taogen.docs2uml.vo.MyMethodVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taogen
 */
public abstract class AbstractGenerator implements Generator {
    protected static final Logger logger = LogManager.getLogger();
    protected static final String TEMPLATE_DIRECTORY_NAME = "template";
    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);

    protected static void ensureTemplateDirExists() {
        ensureDirExists(TEMPLATE_DIRECTORY_NAME);
    }

    private static void ensureDirExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    protected Configuration getConfiguration() {
        configuration.setClassForTemplateLoading(this.getClass(), "/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLogTemplateExceptions(false);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
        return configuration;
    }

    protected Template getTemplate(String templateFilename) {
        try {
            return getConfiguration().getTemplate(TEMPLATE_DIRECTORY_NAME + "/" + templateFilename);
        } catch (IOException e) {
            logger.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new GeneratorException(e.getMessage());
        }
    }

    protected List<MyEntityVo> convertEntityToVo(List<MyEntity> myEntities) {
        List<MyEntityVo> myEntityVos = new ArrayList<>();
        if (myEntities != null) {
            myEntityVos = new ArrayList<>(myEntities.size());
            for (MyEntity myEntity : myEntities) {
                MyEntityVo myEntityVo = new MyEntityVo();
                // entity
                myEntityVo.setClassName(myEntity.getClassName());
                if (myEntity.getIsAbstract()) {
                    myEntityVo.setType(DecorativeKeyword.ABSTRACT);
                } else {
                    myEntityVo.setType(myEntity.getType().toString().toLowerCase());
                }
                myEntityVo.setIsAbstract(getDecorativeGenerate(DecorativeKeyword.ABSTRACT, myEntity.getIsAbstract()));
                // entity fields
                List<MyField> myFields = myEntity.getFields();
                if (myFields != null) {
                    List<MyFieldVo> myFieldVos = new ArrayList<>(myFields.size());
                    for (MyField myField : myFields) {
                        MyFieldVo fieldVo = new MyFieldVo();
                        fieldVo.setIsFinal(getDecorativeGenerate(DecorativeKeyword.FINAL, myField.getIsFinal()));
                        fieldVo.setIsStatic(getDecorativeGenerate(DecorativeKeyword.STATIC, myField.getIsStatic()));
                        fieldVo.setVisibility(getGenerateVisibility(myField.getVisibility()));
                        fieldVo.setType(myField.getType());
                        fieldVo.setName(myField.getName());
                        myFieldVos.add(fieldVo);
                    }
                    myEntityVo.setFields(myFieldVos);
                }
                // entity methods
                List<MyMethod> myMethods = myEntity.getMethods();
                if (myMethods != null) {
                    List<MyMethodVo> myMethodVos = new ArrayList<>(myMethods.size());
                    for (MyMethod myMethod : myMethods) {
                        MyMethodVo methodVo = new MyMethodVo();
                        methodVo.setVisibility(getGenerateVisibility(myMethod.getVisibility()));
                        methodVo.setIsAbstract(getDecorativeGenerate(DecorativeKeyword.ABSTRACT, myMethod.getIsAbstract()));
                        methodVo.setIsStatic(getDecorativeGenerate(DecorativeKeyword.STATIC, myMethod.getIsStatic()));
                        methodVo.setReturnType(myMethod.getReturnType());
                        methodVo.setName(myMethod.getName());
                        List<MyParameter> myParameters = myMethod.getParams();
                        if (myParameters != null) {
                            StringBuilder params = new StringBuilder();
                            for (int i = 0; i < myParameters.size(); i++) {
                                params.append(myParameters.get(i).getType()).append(" ").append(myParameters.get(i).getName());
                                if (i != myParameters.size() - 1) {
                                    params.append(", ");
                                }
                            }
                            methodVo.setParams(params.toString());
                        }
                        myMethodVos.add(methodVo);
                    }
                    myEntityVo.setMethods(myMethodVos);
                }
                // entity parent class
                if (myEntity.getParentClass() != null) {
                    myEntityVo.setParentClass(myEntity.getParentClass().getClassName());
                }
                // entity parent interfaces
                List<MyEntity> parentInterfaces = myEntity.getParentInterfaces();
                if (parentInterfaces != null) {
                    List<String> parentInterfaceNames = new ArrayList<>(parentInterfaces.size());
                    for (MyEntity myInterface : parentInterfaces) {
                        parentInterfaceNames.add(myInterface.getClassName());
                    }
                    myEntityVo.setParentInterfaces(parentInterfaceNames);
                }
                myEntityVos.add(myEntityVo);
            }
        }
        return myEntityVos;
    }

    private String getDecorativeGenerate(String keyword, boolean isExists) {
        return isExists ? new StringBuilder().append("{").append(keyword).append("}").toString() : "";
    }

    private String getGenerateVisibility(Visibility visibility) {
        if (Visibility.PRIVATE.equals(visibility)) {
            return "-";
        } else if (Visibility.PROTECTED.equals(visibility)) {
            return "#";
        } else {
            return "+";
        }
    }
}
