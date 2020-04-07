package com.taogen.docs2uml.command;

import com.taogen.docs2uml.commons.constant.CommandError;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommandHandlerTest {

    private CommandHandler commandHandler = new CommandHandler();

    @Test
    public void check() {
        commandHandler.setArguments(null);
        assertEquals(CommandError.ERROR_CODE_MISS_PARAM, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-u", "http://test.com"});
        assertEquals(CommandError.ERROR_CODE_MISS_PARAM, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-u", "h://test.com", "-p", "com.test"});
        assertEquals(CommandError.ERROR_CODE_PARAM_VALUE_FORMAT_ERROR, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-u", "https://test.com", "-s", "t", "-p", "com.test"});
        assertEquals(CommandError.ERROR_CODE_PARAM_VALUE_FORMAT_ERROR, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-u", "https://test.com", "-p", "com.test"});
        assertEquals(CommandError.SUCCESS_CODE, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-p", "com.test", "-u", "https://test.com"});
        assertEquals(CommandError.SUCCESS_CODE, commandHandler.check().getErrorCode());

        commandHandler.setArguments(new String[]{"-u", "https://test.com", "-p", "com.test", "-s", "true"});
        assertEquals(CommandError.SUCCESS_CODE, commandHandler.check().getErrorCode());
    }
}