package com.taogen.docs2uml.entity;

import com.taogen.docs2uml.constant.CommandError;
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

    public static ErrorMessage get(String errorCode) {
        return (ErrorMessage) CommandError.ERROR_MAP.get(errorCode);
    }
}
