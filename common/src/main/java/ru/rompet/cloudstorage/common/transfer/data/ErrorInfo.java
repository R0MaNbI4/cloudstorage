package ru.rompet.cloudstorage.common.transfer.data;

import java.io.Serializable;

public class ErrorInfo implements Serializable {
    private boolean isSuccessful;
    private boolean isFileAlreadyExists;
    private boolean isFileNotExists;
    private boolean isPathNotExists;
    private boolean isWrongPath;
    private boolean isFileUnableToDelete;
    private boolean isFileLock;
    private boolean isImpossibleUniquelyIdentifyFileException;
    private boolean IncompatibleParametersException;
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
        if (fileAlreadyExists) {
            isSuccessful = false;
        }
        isFileAlreadyExists = fileAlreadyExists;
    }

    public boolean isFileNotExists() {
        return isFileNotExists;
    }

    public void setFileNotExists(boolean fileNotExists) {
        if (fileNotExists) {
            isSuccessful = false;
        }
        isFileNotExists = fileNotExists;
    }

    public boolean isPathNotExists() {
        return isPathNotExists;
    }

    public void setPathNotExists(boolean pathNotExists) {
        if (pathNotExists) {
            isSuccessful = false;
        }
        isPathNotExists = pathNotExists;
    }

    public boolean isWrongPath() {
        return isWrongPath;
    }

    public void setWrongPath(boolean wrongPath) {
        if (wrongPath) {
            isSuccessful = false;
        }
        isWrongPath = wrongPath;
    }

    public boolean isFileUnableToDelete() {
        return isFileUnableToDelete;
    }

    public void setFileUnableToDelete(boolean fileUnableToDelete) {
        if (fileUnableToDelete) {
            isSuccessful = false;
        }
        isFileUnableToDelete = fileUnableToDelete;
    }

    public String getErrorDetails() {
        return ErrorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        ErrorDetails = errorDetails;
    }

    public boolean isFileLock() {
        return isFileLock;
    }

    public void setFileLock(boolean fileLock) {
        if (fileLock) {
            isSuccessful = false;
        }
        isFileLock = fileLock;
    }

    public boolean isImpossibleUniquelyIdentifyFileException() {
        return isImpossibleUniquelyIdentifyFileException;
    }

    public void setImpossibleUniquelyIdentifyFileException(boolean impossibleUniquelyIdentifyFileException) {
        if (impossibleUniquelyIdentifyFileException) {
            isSuccessful = false;
        }
        isImpossibleUniquelyIdentifyFileException = impossibleUniquelyIdentifyFileException;
    }

    public boolean isIncompatibleParametersException() {
        return IncompatibleParametersException;
    }

    public void setIncompatibleParametersException(boolean incompatibleParametersException) {
        if (incompatibleParametersException) {
            isSuccessful = false;
        }
        IncompatibleParametersException = incompatibleParametersException;
    }
}
