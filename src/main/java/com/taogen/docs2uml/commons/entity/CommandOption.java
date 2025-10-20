package com.taogen.docs2uml.commons.entity;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class CommandOption {
    /**
     * Fetch HTML document from the Doc API URL
     */
    private String url;
    /**
     * For generating output file name. For example, v5.1.x.
     */
    private String sourceCodeVersion;
    /**
     * Scanning source code from the rootDirPath
     */
    private String rootDirPath;
    // TODO: update doc, pacakge -> pacakgeName
    /**
     * package to scan
     */
    private String topPackageName;
    /**
     * current scan package
     */
    private String packageName;
    /**
     * Whether to scan subpackages of the specified package
     */
    private Boolean subPackage;
    private boolean fieldsDisplayed = false;
    private boolean methodsDisplayed = false;
    private boolean dependenciesDisplayed = false;
    private boolean membersDisplayed = false;
    private boolean staticFieldExcluded = false;
    private boolean onlyPublicMethodsDisplayed = false;
    /**
     * Only generate classes related with the specified class
     */
    private String specifiedClass;

    public CommandOption() {
    }

    public CommandOption(String url) {
        this.url = url;
    }

    public CommandOption(String url, String topPackageName) {
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

    public String getPrefixUrl() {
        if (url != null) {
            return url.substring(0, url.lastIndexOf('/') + 1);
        }
        return null;
    }
}
