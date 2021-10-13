package ru.rompet.cloudstorage.common.enums;

import java.util.Arrays;

public enum Parameter {
    RW;

    public static boolean has(String value) {
        return Arrays.stream(Parameter.values()).anyMatch(e -> e.name().equals(value));
    }
}
