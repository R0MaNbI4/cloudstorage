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
    private String login;
    private String password;
    private ArrayList<Parameter> parameters;
    boolean isValidCommand;
    boolean isValidCredentials;
    boolean isValidPath;

    public ConsoleInputHandler() {
        parameters = new ArrayList<>();
    }

    public boolean validate(String input) {
        isValidCommand = isValidCredentials = isValidPath = true;
        this.args = input.split("\\s");
        if (!parseCommand()) {
            isValidCommand = false;
            return false;
        }
        parseParameters();
        if (command == Command.DIR && args.length == 1) {
            fromPath = toPath = Path.of("");
            return true;
        }
        if (command == Command.AUTH || command == Command.REGISTER) {
            if (!parseCredentials()) {
                isValidCredentials = false;
                return false;
            }
        } else {
            if (!parsePath()) {
                isValidPath = false;
                return false;
            }
        }
        return true;
    }

    public Command getCommand() {
        return command;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public String getFromPath() {
        return fromPath.toString();
    }

    public String getToPath() {
        return toPath.toString();
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasParameters() {
        return parameters.size() != 0;
    }

    public boolean isValidCommand() {
        return isValidCommand;
    }

    public boolean isValidCredentials() {
        return isValidCredentials;
    }

    public boolean isValidPath() {
        return isValidPath;
    }

    private boolean parseCommand() {
        try {
            command = Command.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private void parseParameters() {
        parameters.clear();
        for (int i = 1; i < args.length; i++) {
            String enumString = args[i].substring(1).toUpperCase();
            if (Parameter.has(enumString)) {
                parameters.add(Parameter.valueOf(enumString));
            } else {
                break;
            }
        }
    }

    public boolean parsePath() {
        try {
            fromPath = Path.of(args[1 + parameters.size()]);
            if (args.length - 1 == 2 + parameters.size()) {
                toPath = Path.of(args[2 + parameters.size()]);
            } else {
                toPath = fromPath;
            }
        } catch (InvalidPathException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    private boolean parseCredentials() {
        try {
            login = args[1 + parameters.size()];
            password = args[2 + parameters.size()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
}
