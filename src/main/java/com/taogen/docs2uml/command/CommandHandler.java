package com.taogen.docs2uml.command;

import com.taogen.docs2uml.commons.constant.CommandError;
import com.taogen.docs2uml.commons.entity.CommandOption;
import com.taogen.docs2uml.commons.entity.ErrorMessage;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taogen
 */
@Data
public class CommandHandler {
    private static final String URL_OPTION = "-u";
    private static final String URL_FULL_OPTION = "--url";
    private static final String PACKAGE_OPTION = "-p";
    private static final String PACKAGE_FULL_OPTION = "--package";
    private static final String SUB_PACKAGE_OPTION = "-s";
    private static final String SUB_PACKAGE_FULL_OPTION = "--subpackage";
    private static final String MEMBERS_OPTION = "-m";
    private static final String MEMBERS_FULL_OPTION = "--members";
    private static final String CLASS_OPTION = "-c";
    private static final String CLASS_FULL_OPTION = "--class";

    private static final Logger logger = LogManager.getLogger();

    // TODO: update docs. command -> argutments
    private String[] arguments;
    private CommandOption commandOption = new CommandOption();

    public CommandHandler() {
    }

    public CommandHandler(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * TODO: Update docs. new method
     */
    public static void showCommandUsage() {
        logger.info("Command Usage Example: java -jar docs2uml.jar -u https://example.com -p com.example");
    }

    // TODO: Recording. Lack design of algorithm.
    public ErrorMessage check() {
        if (arguments == null) {
            return CommandError.getErrorMessageByCode(com.taogen.docs2uml.commons.constant.CommandError.ERROR_CODE_MISS_PARAM);
        }
        Map<String, String> argumentsMap = new HashMap<>();
        for (int i = 0; i < arguments.length; i = i + 2) {
            argumentsMap.put(arguments[i], arguments[i + 1]);
        }
        ErrorMessage errorMessage = checkArguments(argumentsMap);
        if (CommandError.SUCCESS_CODE.equals(errorMessage.getErrorCode())) {
            setArgumentsByMap(argumentsMap);
        }
        return errorMessage;
    }

    private ErrorMessage checkArguments(Map<String, String> argumentsMap) {
        String url = getOptionByKeys(argumentsMap, URL_OPTION, URL_FULL_OPTION);
        if (url == null) {
            return CommandError.getErrorMessageByCode(CommandError.ERROR_CODE_MISS_PARAM);
        }
        if (getOptionByKeys(argumentsMap, PACKAGE_OPTION, PACKAGE_FULL_OPTION) == null) {
            return CommandError.getErrorMessageByCode(CommandError.ERROR_CODE_MISS_PARAM);
        }
        if (!isLegalUrl(url)) {
            return CommandError.getErrorMessageByCode(CommandError.ERROR_CODE_PARAM_VALUE_FORMAT_ERROR);
        }
        String subPackage = getOptionByKeys(argumentsMap, SUB_PACKAGE_OPTION, SUB_PACKAGE_FULL_OPTION);
        if (subPackage != null && !isBooleanValue(subPackage)) {
            return CommandError.getErrorMessageByCode(CommandError.ERROR_CODE_PARAM_VALUE_FORMAT_ERROR);
        }
        String members = getOptionByKeys(argumentsMap, MEMBERS_OPTION, MEMBERS_FULL_OPTION);
        if (members != null && !isBooleanValue(members)) {
            return CommandError.getErrorMessageByCode(CommandError.ERROR_CODE_PARAM_VALUE_FORMAT_ERROR);
        }
        return CommandError.getErrorMessageByCode(CommandError.SUCCESS_CODE);
    }

    private String getOptionByKeys(Map<String, String> arguments, String shortOptionKey, String fullOptionKey) {
        return arguments.get(shortOptionKey) == null ? arguments.get(fullOptionKey) : arguments.get(shortOptionKey);
    }

    private boolean isBooleanValue(String subPacakge) {
        return "true".equals(subPacakge) || "false".equals(subPacakge);
    }

    private boolean isLegalUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void setArgumentsByMap(Map<String, String> argumentsMap) {
        this.commandOption.setUrl(getOptionByKeys(argumentsMap, URL_OPTION, URL_FULL_OPTION));
        String packageName = getOptionByKeys(argumentsMap, PACKAGE_OPTION, PACKAGE_FULL_OPTION);
        this.commandOption.setPackageName(packageName);
        this.commandOption.setTopPackageName(packageName);
        String subPackage = getOptionByKeys(argumentsMap, SUB_PACKAGE_OPTION, SUB_PACKAGE_FULL_OPTION);
        if (subPackage != null) {
            this.commandOption.setSubPackage(Boolean.parseBoolean(subPackage));
        } else {
            // set subPackage default value: false
            this.commandOption.setSubPackage(false);
        }
        String members = getOptionByKeys(argumentsMap, MEMBERS_OPTION, MEMBERS_FULL_OPTION);
        if (members != null) {
            this.commandOption.setMembersDisplayed(Boolean.parseBoolean(members));
        } else {
            // default value
            this.commandOption.setMembersDisplayed(true);
        }
        this.commandOption.setSpecifiedClass(getOptionByKeys(argumentsMap, CLASS_OPTION, CLASS_FULL_OPTION));
    }
}
