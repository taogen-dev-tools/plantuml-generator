package com.taogen.docs2uml.commons.entity;

import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class ErrorMessage {
    private String errorCode;
    private String errorMessage;

    public ErrorMessage() {
    }

    public ErrorMessage(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
