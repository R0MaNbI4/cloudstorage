package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.enums.Parameter;
import ru.rompet.cloudstorage.common.exception.FileNotExistsException;
import ru.rompet.cloudstorage.common.exception.ImpossibleUniquelyIdentifyFileException;
import ru.rompet.cloudstorage.common.transfer.Message;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static void createParentDirectories(Message message, String rootDirectory) throws Exception {
        Path path = Path.of(rootDirectory + message.getToPath()).getParent();
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }

    public static boolean isPathExists(Message message, String rootDirectory) throws Exception {
        Path path = Path.of(rootDirectory + message.getToPath()).getParent();
        return Files.exists(path);
    }

    public static String rename(String name, boolean isFile) {
        StringBuilder sb = new StringBuilder();
        int number;
        boolean hasNumber = isFile && name.matches("^.+\\s\\(\\d+\\)\\..+$"); // check number if is file
        hasNumber = hasNumber || name.matches("^.+\\s\\(\\d+\\)$"); // check number anyway
        boolean hasExtension = isFile && name.matches("^.+\\..+$");
        number = hasNumber ? incrementNameNumber(getNameNumber(name, hasExtension)) : 1;
        sb.append(getNameWithoutNumber(name, hasNumber, hasExtension))
                .append(" (").append(number).append(")")
                .append(getExtension(name, hasExtension));
        return sb.toString();
    }

    public static Path findFileByIncompleteName(String root, ArrayList<Parameter> parameters, Path fromPath) throws Exception {
        Path fileName = getFilenameWithoutExtension(Path.of(root + fromPath));
        Path parent = Path.of(root + fromPath).getParent();
        FilesVisitor visitor = new FilesVisitor(1);
        Files.walkFileTree(parent, visitor);
        List<Path> result = visitor.getFilesStream()
                .filter(x -> getFilenameWithoutExtension(x).equals(fileName))
                .collect(Collectors.toList());

        if (result.size() > 1) {
            boolean isAllFiles = true;
            for (int i = 0; i < result.size(); i++) {
                if (!new File(result.get(i).toString()).isFile()) {
                    isAllFiles = false;
                }
            }
            if (isAllFiles) {
                if (!hasExtension(root, fromPath)) {
                    for (int i = 0; i < result.size(); i++) {
                        if (!hasExtension("", result.get(i))) {
                            return getParentPath(fromPath, result.get(i));
                        }
                    }
                } else {
                    for (int i = 0; i < result.size(); i++) {
                        if (fromPath.equals(result.get(i).getFileName())) {
                            return getParentPath(fromPath, result.get(i));
                        }
                    }
                }
            } else {
                for (int i = 0; i < result.size(); i++) {
                    boolean isDirectory = new File(result.get(i).toString()).isDirectory();
                    if ((isDirectory && hasParameters(parameters, Parameter.D)) ||
                            (!isDirectory && hasExtension(root, fromPath))) {
                        return getParentPath(fromPath, result.get(i));
                    }
                }
            }
            throw new ImpossibleUniquelyIdentifyFileException("Multiple files or directories with the same name\n" +
                    "Specify the extension if it's a file or use the -f flag if it's a directory");
        } else if (result.size() == 0) {
            throw new FileNotExistsException("File or directory \"" + root + fromPath.toString() + "\" not exists");
        } else {
            return getParentPath(fromPath, result.get(0));
        }
    }

    private static Path getParentPath(Path fromPath, Path fileName) {
        if (fromPath.getParent() == null) {
            return fileName.getFileName();
        } else {
            return fromPath.getParent().resolve(fileName.getFileName());
        }
    }

    public static Path configurePathAccordingParameters(String root, ArrayList<Parameter> parameters, Path fromPath, Path toPath) {
        if (!hasParameters(parameters, Parameter.CHN) && !hasParameters(parameters, Parameter.CHP)) {
            addParameters(parameters, Parameter.CHP);
        }
        if (hasParameters(parameters, Parameter.CHP) && !hasParameters(parameters, Parameter.CHN)) {
            toPath = toPath.resolve(fromPath.getFileName());
        } else if (hasParameters(parameters, Parameter.CHN) && !hasParameters(parameters, Parameter.CHP)) {
            toPath = changeName(root, fromPath, toPath.toString());
        }  else if (hasParameters(parameters, Parameter.CHN, Parameter.CHP)) {
            toPath = Path.of(toPath + getExtension(fromPath.toString(), hasExtension(root, fromPath)));
        }
        return toPath;
    }

    public static boolean hasParameters(ArrayList<Parameter> parameters, Parameter... parameter) {
        for (int i = 0; i < parameter.length; i++) {
            if (!parameters.contains(parameter[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean removeParameters(ArrayList<Parameter> parameters, Parameter... parameter) {
        for (int i = 0; i < parameter.length; i++) {
            if (hasParameters(parameters, parameter[i])) {
                if (!parameters.remove(parameter[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean hasExtension(String root, Path path) {
        if (Files.isRegularFile(Path.of(root + path))) {
            return path.toString().contains(".");
        } else {
            return false;
        }
    }

    private static void addParameters(ArrayList<Parameter> parameters, Parameter... parameter) {
        for (int i = 0; i < parameter.length; i++) {
            if (!hasParameters(parameters, parameter[i])) {
                parameters.add(parameter[i]);
            }
        }
    }

    private static int getNameNumber(String name, boolean hasExtension) {
        String number;
        if (hasExtension) {
            number = name.substring(name.lastIndexOf(" ") + 1, name.lastIndexOf(".") - 1);
        } else {
            number = name.substring(name.lastIndexOf(" ") + 1);
        }
        number = number.replaceAll("[(,)]","");
        return Integer.parseInt(number);
    }

    private static int incrementNameNumber(int number) {
        if (number == Integer.MAX_VALUE) {
            return 1;
        } else {
            return ++number;
        }
    }

    private static String getNameWithoutNumber(String name, boolean hasNumber, boolean hasExtension) {
        if (hasNumber) {
            return name.substring(0, name.lastIndexOf(" ")); // test (1) or test (1).txt
        } else {
            if (hasExtension) {
                return name.substring(0, name.lastIndexOf(".")); // test.txt
            } else {
                return name; // test
            }
        }
    }

    private static String getExtension(String name, boolean hasExtension) {
        return hasExtension ? name.substring(name.lastIndexOf(".")) : ""; // can be directory with "." in name
    }

    private static Path getFilenameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        if (new File(path.toString()).isFile()) {
            if (name.lastIndexOf(".") > 0) {
                name = name.substring(0, name.lastIndexOf("."));
            }
        }
        return Path.of(name);
    }

    private static Path changeName(String root, Path path, String newName) {
        String strPath = path.toString();
        newName = Path.of(newName).getParent() == null ? newName : Path.of(newName).getFileName().toString();
        String extension = hasExtension(root, path) ? strPath.substring(strPath.lastIndexOf(".")) : "";
        strPath = strPath.substring(0, strPath.lastIndexOf("\\") + 1) + newName + extension;
        return Path.of(strPath);
    }
}
