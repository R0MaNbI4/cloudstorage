package ru.rompet.cloudstorage.client.handler;

import com.mysql.cj.util.StringUtils;
import ru.rompet.cloudstorage.common.FilesVisitor;
import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public boolean validate(String input) throws Exception {
        isValidCommand = isValidCredentials = isValidPath = true;
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

    public boolean parsePath() throws Exception {
//        try {
//            fromPath = Path.of(args[1 + parameters.size()]);
//            if (args.length - 1 == 2 + parameters.size()) {
//                toPath = Path.of(args[2 + parameters.size()]);
//            } else {
//                toPath = fromPath;
//            }
//        } catch (InvalidPathException | ArrayIndexOutOfBoundsException e) {
//            return false;
//        }

        fromPath = Path.of(args[1 + parameters.size()]);
        Path fileName = fromPath.getFileName();
        if (!hasParameter(Parameter.D) || !hasExtension(fromPath)) {
            Path parent = fromPath.getParent();
            FilesVisitor visitor = new FilesVisitor(1);
            Files.walkFileTree(parent, visitor);
            Path finalFileName = fileName;
            List<Path> result = visitor.getFilesStream()
                    .filter(x -> getFilenameWithoutExtension(x).equals(finalFileName))
                    .collect(Collectors.toList());

            if (result.size() > 1) {
                System.out.println("Multiple files or directories with the same name\n" +
                                   "Specify the extension if it's a file or use the -f flag if it's a directory");
                return false;
            } else if (result.size() == 0) {
                return false;
            } else {
                fileName = result.get(0);
                fromPath = fromPath.getParent().resolve(fileName);
            }
        }
        if (isToPathDefined()) {
            toPath = Path.of(args[2 + parameters.size()]);
            if (hasParameter(Parameter.CHE) && hasParameter(Parameter.DE)) {
                System.out.println("You can't use parameters -che (change extension) and -de (delete extension) together");
                return false;
            }
            if ((hasParameter(Parameter.D) || Files.isDirectory(fromPath)) && (hasParameter(Parameter.CHE) || hasParameter(Parameter.DE))) {
                System.out.println("You can't use parameters -che (change extension) or -de (delete extension) for directory");
                return false;
            }
            if (hasParameter(Parameter.CHE) && hasParameter(Parameter.CHP) && !hasParameter(Parameter.CHN)) {
                System.out.println("This combination of parameters cannot be used. Use -cha (change all) and give the full path with file name and extension");
                return false;
            }
            if (hasParameter(Parameter.CHA)) {
                addParameter(Parameter.CHP, Parameter.CHN, Parameter.CHE);
            }
            if (!hasParameter(Parameter.CHN) && !hasParameter(Parameter.CHE) && !hasParameter(Parameter.CHP)) {
                addParameter(Parameter.CHP);
            }

            if (!(hasParameter(Parameter.CHP) && hasParameter(Parameter.CHN) && hasParameter(Parameter.CHE))) {
                if (hasParameter(Parameter.CHP)) {
                    toPath = toPath.resolve(fileName);
                }
                if (!(hasParameter(Parameter.CHE) && hasParameter(Parameter.CHN))) {
                    if (hasParameter(Parameter.CHE)) {
                        toPath = changeExtension(fromPath, toPath.toString());
                    }
                    if (hasParameter(Parameter.CHN)) {
                        toPath = changeName(hasParameter(Parameter.CHP) ? toPath : fromPath, toPath.toString());
                    }
                } else {
                    toPath = fromPath.getParent() != null ? fromPath.getParent().resolve(toPath) : toPath;
                }
            }
            if (hasParameter(Parameter.DE)) {
                toPath = deleteExtension(toPath);
            }
        } else {
            toPath = fromPath;
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

    private Path getFilenameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        if (new File(path.toString()).isFile()) {
            if (name.lastIndexOf(".") > 0) {
                name = name.substring(0, name.lastIndexOf("."));
            }
        }
        return Path.of(name);
    }

    private boolean hasParameter(Parameter parameter) {
        return parameters.contains(parameter);
    }

    private void addParameter(Parameter... parameter) {
        for (int i = 0; i < parameter.length; i++) {
            if (!hasParameter(parameter[i])) {
                parameters.add(parameter[i]);
            }
        }
    }

    private boolean hasExtension(Path path) {
        if (Files.isRegularFile(path)) {
            return path.toString().contains(".");
        } else {
            return false;
        }
    }

    private boolean isToPathDefined() {
        return args.length - 1 == 2 + parameters.size();
    }

    private Path changeExtension(Path path, String newExtension) {
        String strPath = path.toString();
        if (!hasExtension(path)) {
            strPath = strPath + newExtension;
        } else {
            strPath = strPath.substring(0, strPath.lastIndexOf(".")) + newExtension;
        }
        return Path.of(strPath);
    }

    private Path deleteExtension(Path path) {
        String strPath = path.toString();
        return Path.of(strPath.substring(0, strPath.lastIndexOf(".") == -1 ? strPath.length() : strPath.lastIndexOf(".")));
    }

    private Path changeName(Path path, String newName) {
        String strPath = path.toString();
        strPath = strPath.substring(0, strPath.lastIndexOf("\\") + 1) + newName;
        if (hasExtension(path)) {
            strPath = strPath + strPath.substring(strPath.lastIndexOf("."));
        }
        return Path.of(strPath);
    }

}
