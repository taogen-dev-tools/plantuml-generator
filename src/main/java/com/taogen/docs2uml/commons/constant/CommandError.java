package com.taogen.docs2uml.commons.constant;

import com.taogen.docs2uml.commons.entity.ErrorMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taogen
 * // TODO: Recording. Lock design of constant.
 */
public class CommandError {
    public static final String SUCCESS_CODE = "0";
    public static final String SUCCESS_MESSAGE = "OK";

    public static final String ERROR_CODE_MISS_PARAM = "01";
    public static final String ERROR_MESSAGE_MISS_PARAM = "Missing required parameters!";

    public static final String ERROR_CODE_PARAM_VALUE_FORMAT_ERROR = "02";
    public static final String ERROR_MESSAGE_PARAM_VALUE_FORMAT_ERROR = "Error format of parameter value!";

    private static final Map<String, Object> ERROR_MAP = new HashMap<>();

    static {
        ERROR_MAP.put(SUCCESS_CODE, new ErrorMessage(SUCCESS_CODE, SUCCESS_MESSAGE));
        ERROR_MAP.put(ERROR_CODE_MISS_PARAM, new ErrorMessage(ERROR_CODE_MISS_PARAM, ERROR_MESSAGE_MISS_PARAM));
        ERROR_MAP.put(ERROR_CODE_PARAM_VALUE_FORMAT_ERROR, new ErrorMessage(ERROR_CODE_PARAM_VALUE_FORMAT_ERROR, ERROR_MESSAGE_PARAM_VALUE_FORMAT_ERROR));
    }

    public static ErrorMessage getErrorMessageByCode(String errorCode){
        return (ErrorMessage) ERROR_MAP.get(errorCode);
    }

    private CommandError() {
        throw new IllegalStateException("Constant class");
    }
}
