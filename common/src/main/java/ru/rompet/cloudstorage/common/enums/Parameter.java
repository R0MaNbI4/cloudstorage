package ru.rompet.cloudstorage.common.enums;

import java.util.Arrays;

public enum Parameter {
    RW, // rewrite
    RN, // rename
    NR, // recursion
    CD, // create directories

    // parameters below only used for console input and don't transferred to the server (removed from request before sending)
    D, // directory
    CHN, // change name
    CHE, // change extension
    CHP, // change path
    CHA, // change all
    DE; // delete extension

    public static boolean has(String value) {
        return Arrays.stream(Parameter.values()).anyMatch(e -> e.name().equals(value));
    }
}
