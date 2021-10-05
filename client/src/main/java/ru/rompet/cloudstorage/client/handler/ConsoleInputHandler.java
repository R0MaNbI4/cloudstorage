package ru.rompet.cloudstorage.client.handler;

import io.netty.util.internal.StringUtil;
import ru.rompet.cloudstorage.common.Command;

import java.io.File;

public class ConsoleInputHandler {
    String input;

    public ConsoleInputHandler(String input) {
        this.input = input;
    }

    public Command getCommand() {
        return Command.valueOf(input.split("\\s")[0].toUpperCase());
    }

    public String getFilename() {
        String path = input.split("\\s")[1];
        return new File(path).getName();
    }

    public String getPath() {
        String path = input.split("\\s")[1];
        return new File(path).getPath();
    }
}
