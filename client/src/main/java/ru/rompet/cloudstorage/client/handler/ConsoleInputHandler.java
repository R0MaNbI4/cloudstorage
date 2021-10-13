package ru.rompet.cloudstorage.client.handler;

import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ConsoleInputHandler {
    private String[] args;
    private Command command;
    private Path fromPath;
    private Path toPath;
    private ArrayList<String> parameters;

    public ConsoleInputHandler() {
        parameters = new ArrayList<>();
    }

    public boolean validate(String input) {
        this.args = input.split("\\s");
        tryGetParameters();
        return isValidCommand() && isValidPath();
    }

    public Command getCommand() {
        return command;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getFromPath() {
        return fromPath.toString();
    }

    public String getToPath() {
        return toPath.toString();
    }

    public boolean isValidCommand() {
        try {
            command = Command.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public boolean isValidPath() {
        try {
            fromPath = Path.of(args[1 + parameters.size()]);
            if (args.length - 1 == 2 + parameters.size()) {
                toPath = Path.of(args[2 + parameters.size()]);
            } else {
                toPath = fromPath;
            }
        } catch (InvalidPathException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean hasParameters() {
        return parameters.size() != 0;
    }

    private void tryGetParameters() {
        parameters.clear();
        for (int i = 1; i < args.length; i++) {
            if (Parameter.has(args[i].substring(1))) {
                parameters.add(args[i]);
            } else {
                break;
            }
        }
    }
}
