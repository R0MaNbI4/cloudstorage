package ru.rompet.cloudstorage.server.domain;

import java.nio.file.Path;

public class Test {
    public static void main(String[] args) {
        Path from = Path.of("minecraft");
        Path to = Path.of("test1\\test\\minecraft");
        String fromString = "minecraft\\config\\test1.txt";
        String toString = "test1\\test\\minecraft\\config\\test1.txt";
        System.out.println(to.getParent());
    }
}
