package com.taogen.docs2uml.commons.util.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * @author taogen
 */
@Data
public class SourceCodeContent {
    private String packageDeclaration;
    private List<String> imports;
    private String classDeclaration;
    /**
     * Fields
     * - Basic fields: `Integer i;` `User user;`
     * - Array fields: `User[] users;`
     * - Generic fields: `List<User> userList;`
     * - Generic array fields: `MyField<String>[]`
     * - Constants: `public static final Integer COUNT = 0;`
     */
    private List<String> fields;
    private List<String> constructors;
    private List<String> methods;
    private List<String> nestedClasses;

}
