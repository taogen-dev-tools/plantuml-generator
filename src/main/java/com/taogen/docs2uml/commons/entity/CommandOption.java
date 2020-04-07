package com.taogen.docs2uml.commons.entity;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class CommandOption {
    private String url;
    // TODO: update doc, pacakge -> pacakgeName
    private String topPackageName;
    private String packageName;
    private Boolean subPackage;

    public CommandOption() {
    }

    public CommandOption(String url){
        this.url = url;
    }

    public CommandOption(String url, String topPackageName){
        this.url = url;
        this.topPackageName = topPackageName;
    }

    public CommandOption(String url, String topPackageName, String packageName) {
        this.url = url;
        this.topPackageName = topPackageName;
        this.packageName = packageName;
    }

    public CommandOption(String url, String topPackageName, String packageName, Boolean subPackage) {
        this.url = url;
        this.topPackageName = topPackageName;
        this.packageName = packageName;
        this.subPackage = subPackage;
    }

    public String getPrefixUrl(){
        if (url != null){
            return url.substring(0, url.lastIndexOf('/') + 1);
        }
        return null;
    }
}
