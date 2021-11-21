package ru.rompet.cloudstorage.common.exception;

public class FileNotExistsException extends Exception {
    public FileNotExistsException(String message) {
        super(message);
    }

    public FileNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}