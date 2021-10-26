package ru.rompet.cloudstorage.common.data;

import java.io.Serializable;

public class ErrorInfo implements Serializable {
    private boolean isSuccessful;
    private boolean isFileAlreadyExists;
    private boolean isFileNotExists;
    private boolean isFileUnableToDelete;
    private String ErrorDetails;

    public ErrorInfo() {
        setSuccessful(true);
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public boolean isFileAlreadyExists() {
        return isFileAlreadyExists;
    }

    public void setFileAlreadyExists(boolean fileAlreadyExists) {
        isFileAlreadyExists = fileAlreadyExists;
    }

    public boolean isFileNotExists() {
        return isFileNotExists;
    }

    public void setFileNotExists(boolean fileNotExists) {
        isFileNotExists = fileNotExists;
    }

    public boolean isFileUnableToDelete() {
        return isFileUnableToDelete;
    }

    public void setFileUnableToDelete(boolean fileUnableToDelete) {
        isFileUnableToDelete = fileUnableToDelete;
    }

    public String getErrorDetails() {
        return ErrorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        ErrorDetails = errorDetails;
    }
}
