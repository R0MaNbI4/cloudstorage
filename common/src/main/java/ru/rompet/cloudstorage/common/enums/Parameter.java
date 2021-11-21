package ru.rompet.cloudstorage.common.enums;

import java.util.Arrays;

public enum Parameter {
    RW, // rewrite
    RN, // rename
    NR, // recursion
    CD, // create directories

    // parameters below only used for console input
    D, // directory
    CHN, // change name
    CHP; // change path

    public static boolean has(String value) {
        return Arrays.stream(Parameter.values()).anyMatch(e -> e.name().equals(value));
    }

    public static Parameter[] getConsoleInputParameters() {
        return new Parameter[]{
                Parameter.D,
                Parameter.CHN,
                Parameter.CHP,
        };
    }
}
