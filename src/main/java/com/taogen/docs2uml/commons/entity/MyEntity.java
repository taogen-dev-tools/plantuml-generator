package com.taogen.docs2uml.commons.entity;

import com.taogen.docs2uml.commons.constant.EntityType;
import com.taogen.docs2uml.commons.util.SourceCodeUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taogen
 */
@Slf4j
@Data
// TODO: update doc. Add new fields.
public class MyEntity {
    /**
     * id: class path
     */
    private String id;
    private EntityType type;
    // TODO: update doc package -> packageName
    private String packageName;
    private Boolean isAbstract;
    private String className;
    private String classNameWithoutGeneric;
    private String url;
    private MyEntity parentClass;
    private List<MyEntity> parentInterfaces;
    private List<MyField> fields;
    private List<MyMethod> methods;
    private List<MyEntity> subClasses;
    private List<MyEntity> subInterfaces;
    private boolean visited = false;

    public static MyEntity getFromClassDeclaration(SourceCodeUtil.ClassDeclaration classDeclaration) {
        if (classDeclaration == null) {
            return null;
        }
        MyEntity entity = new MyEntity();
        entity.setId(classDeclaration.getPackageName() + "." + classDeclaration.getClassNameWithoutGeneric());
        entity.setType(classDeclaration.getType());
        entity.setPackageName(classDeclaration.getPackageName());
        entity.setIsAbstract(classDeclaration.isAbstract());
        entity.setClassName(classDeclaration.getClassName());
        entity.setClassNameWithoutGeneric(classDeclaration.getClassNameWithoutGeneric());
        // parent class
        SourceCodeUtil.ClassDeclaration parentClassDeclaration = classDeclaration.getParentClass();
        if (parentClassDeclaration != null) {
            MyEntity parentClass = new MyEntity();
            parentClass.setId(parentClassDeclaration.getPackageName() + "." + parentClassDeclaration.getClassNameWithoutGeneric());
            parentClass.setPackageName(parentClassDeclaration.getPackageName());
            parentClass.setClassName(parentClassDeclaration.getClassName());
            parentClass.setClassNameWithoutGeneric(parentClassDeclaration.getClassNameWithoutGeneric());
            entity.setParentClass(parentClass);
        }
        // parent interfaces
        List<SourceCodeUtil.ClassDeclaration> parentInterfaceDeclarations = classDeclaration.getParentInterfaces();
        if  (parentInterfaceDeclarations != null && parentInterfaceDeclarations.size() > 0) {
            List<MyEntity> parentInterfaces = new ArrayList<>();
            if (parentInterfaceDeclarations != null) {
                for (SourceCodeUtil.ClassDeclaration interfaceDeclaration : parentInterfaceDeclarations) {
                    MyEntity interfaceEntity = new MyEntity();
                    interfaceEntity.setId(interfaceDeclaration.getPackageName() + "." + interfaceDeclaration.getClassNameWithoutGeneric());
                    interfaceEntity.setPackageName(interfaceDeclaration.getPackageName());
                    interfaceEntity.setClassName(interfaceDeclaration.getClassName());
                    interfaceEntity.setClassNameWithoutGeneric(interfaceDeclaration.getClassNameWithoutGeneric());
                    parentInterfaces.add(interfaceEntity);
                }
            }
            entity.setParentInterfaces(parentInterfaces);
        }
        return entity;
    }
}
