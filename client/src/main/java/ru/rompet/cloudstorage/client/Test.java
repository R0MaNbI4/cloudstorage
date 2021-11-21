package ru.rompet.cloudstorage.client;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import ru.rompet.cloudstorage.common.FilesVisitor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws Exception {
        // если нет параметра -f и расширения, то
        String path = "clientFiles\\folder\\test1";
        Path parent = Path.of(path).getParent();
        Path name = Path.of(path).getFileName();
        System.out.println(name);
        System.out.println(parent);
        List<Path> result;
        FilesVisitor visitor = new FilesVisitor(1);
        Files.walkFileTree(parent, visitor);
        result = visitor.getFilesStream()
                .filter(x -> getFilenameWithoutExtension(x).equals(name)) // если файл, то удалить расширение, иначе не трогать расширение (папка с точкой в названии)
                .collect(Collectors.toList());
        System.out.println(result);
        if (result.size() > 1) {
            System.out.println("расширение или -f");
            // Несколько файлов или директорий с таким именем. Введите расширение, если это файл или параметр -f, если это директория66
        } else {
            System.out.println("всё норм");
        }
        System.out.println(result);
        File file = new File(path);
        String strPath = "folder\\test.txt";
        System.out.println(strPath.substring(0, strPath.lastIndexOf(".") == -1 ? strPath.length() : strPath.lastIndexOf(".")));
//        System.out.println(strPath.substring(0, strPath.lastIndexOf("\\") + 1));
//        System.out.println(strPath.substring(strPath.lastIndexOf(".")));
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
}
