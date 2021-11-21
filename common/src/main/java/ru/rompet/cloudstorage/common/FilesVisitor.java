package ru.rompet.cloudstorage.common;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FilesVisitor implements FileVisitor<Path> {
    int maxDepth;
    int currentDepth;
    ArrayList<Path> filesList;

    public FilesVisitor(int depth) {
        this.maxDepth = depth - 1;
        currentDepth = -1;
        filesList = new ArrayList<>();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (currentDepth != -1) {
            filesList.add(dir);
        }
        currentDepth++;
        if (currentDepth > maxDepth) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        filesList.add(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        currentDepth--;
        return FileVisitResult.CONTINUE;
    }

    public Stream<Path> getFilesStream () {
        return filesList.stream();
    }
}
