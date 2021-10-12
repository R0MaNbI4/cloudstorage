package ru.rompet.cloudstorage.client;

import io.netty.channel.unix.IovArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("clientFiles\\");
        List<Path> paths = listFiles(path);
        paths.forEach(System.out::println);
//        for (Path path1 : paths) {
//            System.out.println(path1.getFileName());
//            System.out.println(Files.size(path1));
//            System.out.println(Files.isDirectory(path1));
//        }
    }

    public static List<Path> listFiles(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(x -> Files.isRegularFile(x) || Files.isDirectory(x))
                    .filter(x -> x.getParent() != null)
                    .collect(Collectors.toList());
        }
        return result;
    }
}
