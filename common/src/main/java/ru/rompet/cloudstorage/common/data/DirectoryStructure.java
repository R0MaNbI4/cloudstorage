package ru.rompet.cloudstorage.common.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DirectoryStructure implements Iterable<DirectoryStructureEntry> {
    private final ArrayList<DirectoryStructureEntry> directoryStructure;

    public DirectoryStructure() {
        this.directoryStructure = new ArrayList<>();
    }

    public void scan(String path) throws IOException {
        List<Path> paths = listFiles(Path.of(path));
        for (Path path1 : paths) {
            DirectoryStructureEntry directoryStructureEntry = new DirectoryStructureEntry(
                    path1.getFileName().toString(),
                    Files.size(path1),
                    Files.isDirectory(path1)
            );
            directoryStructure.add(directoryStructureEntry);
        }
    }

    private List<Path> listFiles(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path, 1)) {
            result = walk
                    .filter(x -> Files.isRegularFile(x) || Files.isDirectory(x))
                    .filter(x -> x.getParent() != null) // exclude parent directory
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public Iterator<DirectoryStructureEntry> iterator() {
        return new CustomIterator();
    }

    class CustomIterator implements Iterator<DirectoryStructureEntry> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index != directoryStructure.size();
        }

        @Override
        public DirectoryStructureEntry next() {
            return directoryStructure.get(index++);
        }
    }
}
