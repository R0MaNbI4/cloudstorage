package ru.rompet.cloudstorage.client.handler;

import com.mysql.cj.util.StringUtils;
import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final String[] restrictedDirectoryName = {
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    public ConsoleInputHandler() {
        parameters = new ArrayList<>();
    }

    public boolean validate(String input) {
        isValidCommand = isValidCredentials = isValidPath = true;
        this.args = getMatches(input, "(?<=\")[^\\s]+.+?(?=\")|\\-?[^\\s\"]+");
        if (!parseCommand()) {
            isValidCommand = false;
            return false;
        }
        parseParameters();
        if ((command == Command.DIR || command == Command.HELP) && args.length == 1) {
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
        return isValidDirectoryName(login);
    }

    private boolean isValidDirectoryName(String name) {
        try {
            Path.of(name);
        } catch (InvalidPathException e) {
            return false;
        }
        for (int i = 0; i < restrictedDirectoryName.length; i++) {
            if (name.toUpperCase().equals(restrictedDirectoryName[i])) {
                return false;
            }
        }
        if (name.endsWith(".") || name.endsWith(" ")) {
            return false;
        }
        return true;
    }

    private String[] getMatches(String input, String regex) {
        ArrayList<String> argsArrayList = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(input);
        while (m.find()) {
            argsArrayList.add(m.group());
        }
        return argsArrayList.toArray(String[]::new);
    }
}
