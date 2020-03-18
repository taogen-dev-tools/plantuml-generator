package com.taogen.docs2uml.command;

import com.taogen.docs2uml.constant.CommandError;
import com.taogen.docs2uml.entity.ErrorMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommandHandlerTest {

    private CommandHandler commandHandler = new CommandHandler();

    @Test
    public void check() {
        commandHandler.setArguments(null);
        ErrorMessage errorMessage = commandHandler.check();
        assertEquals(CommandError.ERROR_CODE_MISS_PARAM, errorMessage.getErrorCode());
    }
}