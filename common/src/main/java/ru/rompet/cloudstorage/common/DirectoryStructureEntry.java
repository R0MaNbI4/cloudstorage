package ru.rompet.cloudstorage.common;


public class DirectoryStructureEntry {
    private String name;
    private long sizeInBytes;
    private boolean directory;

    DirectoryStructureEntry(){};

    public DirectoryStructureEntry(String name, long sizeInBytes, boolean isDirectory) {
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.directory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public boolean isDirectory() {
        return directory;
    }
}
