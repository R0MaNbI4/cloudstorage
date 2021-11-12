package ru.rompet.cloudstorage.client.handler;

import ru.rompet.cloudstorage.common.Command;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConsoleInputHandler {
    private String[] args;
    private Command command;
    private Path path;
    private ArrayList<String> parameters;

    public ConsoleInputHandler() {
        parameters = new ArrayList<>();
    }

    public boolean validate(String input) {
        this.args = input.split("\\s");
        return isValidCommand() && isValidPath() && isValidParameters();
    }

    public Command getCommand() {
        return command;
    }

    public String getFilename() {
        return path.getFileName().toString();
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getPath() {
        return path.toString();
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
            path = Paths.get(args[args.length - 1]);
        } catch (InvalidPathException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean isValidParameters() {
        parameters.clear();
        for (int i = 1; i <= args.length - 2; i++) {
            if (args[i].matches("\\s-[a-zA-Z]*\\s")) {
                parameters.add(args[i]);
            } else {
                return false;
            }
        }
        return true;
    }
}
