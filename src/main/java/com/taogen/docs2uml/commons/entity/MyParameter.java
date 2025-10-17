package com.taogen.docs2uml.commons.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taogen
 */
@Slf4j
@Data
public class MyParameter {
    private String type;
    private String name;

    public MyParameter(){}
    public MyParameter(String type, String name){
        this.type = type;
        this.name = name;
    }

    public static MyParameter getFromStr(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        MyParameter myParameter = new MyParameter();
        List<String> splitList = Arrays.stream(s.split(" "))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
        if (splitList.size() >= 2) {
            myParameter.setType(splitList.get(splitList.size() - 2));
            myParameter.setName(splitList.get(splitList.size() - 1));
        } else {
            if (s.contains("...")) {
                int index = s.indexOf("...");
                int length = "...".length();
                myParameter.setType(s.substring(0, index + length));
                myParameter.setName(s.substring(index + length));
            }
        }
        return myParameter;
    }
}
