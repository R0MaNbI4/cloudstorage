package ru.rompet.cloudstorage.client.handler;

import ru.rompet.cloudstorage.common.Settings;
import ru.rompet.cloudstorage.common.Utils;
import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;
import ru.rompet.cloudstorage.common.exception.FileNotExistsException;
import ru.rompet.cloudstorage.common.exception.ImpossibleUniquelyIdentifyFileException;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.rompet.cloudstorage.common.Utils.*;

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
    boolean isRootDefined;
    private final String[] restrictedDirectoryName = {
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    private final List<Command> commandsWithoutPath = Arrays.asList(
            Command.DIR, Command.HELP, Command.SHROOT);

    public ConsoleInputHandler() {
        parameters = new ArrayList<>();
    }

    public boolean validate(String input) throws Exception {
        isValidCommand = isValidCredentials = isValidPath = isRootDefined = true;
        if (Settings.getRoot().equals("")) {
            isRootDefined = false;
            return false;
        }
        // split by space, but words in quotation marks keep together
        // Example: save -r "directory name\test.txt" -> ["save","-r","directory name\test.txt"]
        this.args = getMatches(input, "(?<=\")[^\\s]+.+?(?=\")|\\-?[^\\s\"]+");
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].replaceAll("\\/","\\");
        }
        if (!parseCommand()) {
            isValidCommand = false;
            return false;
        }
        parseParameters();
        if (commandsWithoutPath.contains(command) && args.length == 1) {
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

    public boolean isRootDefined() {
        return isRootDefined;
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

    public boolean parsePath() throws Exception {
        try {
            fromPath = Path.of(args[1 + parameters.size()]);
            if (isToPathDefined()) {
                toPath = Path.of(args[2 + parameters.size()]);
            } else {
                toPath = fromPath;
            }
        } catch (InvalidPathException | ArrayIndexOutOfBoundsException e) {
            return false;
        }

        if (toPath.toString().equals(".")) {
            toPath = Path.of(".");
        }

        if (command == Command.SAVE) {
            return parsePathForSave();
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

    private boolean parsePathForSave() {
        try {
            fromPath = findFileByIncompleteName(Settings.getRoot(), parameters, fromPath);
        } catch (FileNotExistsException | ImpossibleUniquelyIdentifyFileException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            throw new RuntimeException("SWW", e);
        }
        if (isToPathDefined()) {
            toPath = configurePathAccordingParameters(Settings.getRoot(), parameters, fromPath, toPath);
        } else {
            toPath = fromPath;
        }
        removeParameters(Parameter.getConsoleInputParameters());
        return true;
    }

    private void removeParameters(Parameter... parameter) {
        Utils.removeParameters(parameters, parameter);
    }

    private boolean isToPathDefined() {
        return args.length - 1 == 2 + parameters.size();
    }

    private boolean hasParameter(Parameter parameter) {
        return Utils.hasParameters(parameters, parameter);
    }
}