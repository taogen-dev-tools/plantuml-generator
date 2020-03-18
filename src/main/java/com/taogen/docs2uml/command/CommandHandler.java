package com.taogen.docs2uml.command;

import com.taogen.docs2uml.constant.CommandError;
import com.taogen.docs2uml.entity.ErrorMessage;
import com.taogen.docs2uml.entity.MyCommand;
import lombok.Data;

/**
 * @author Taogen
 */
@Data
public class CommandHandler {
    // TODO: update docs. command -> argutments
    private String[] arguments;
    private MyCommand myCommand;

    public CommandHandler() {
    }

    public CommandHandler(String[] arguments) {
        this.arguments = arguments;
    }

    // TODO: Recording. Lack design of algorithm.
    public ErrorMessage check() {
        if (arguments == null) {
            return ErrorMessage.get(CommandError.ERROR_CODE_MISS_PARAM);
        }

        for (int i = 0; i < arguments.length; i++) {
        }

        return ErrorMessage.get(CommandError.SUCCESS_CODE);
    }

}
