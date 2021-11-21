package ru.rompet.cloudstorage.common.exception;

public class ImpossibleUniquelyIdentifyFileException extends Exception {
    public ImpossibleUniquelyIdentifyFileException(String message) {
        super(message);
    }

    public ImpossibleUniquelyIdentifyFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
