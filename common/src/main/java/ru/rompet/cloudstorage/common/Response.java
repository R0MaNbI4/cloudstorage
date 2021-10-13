package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.data.ErrorInfo;
import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Response extends Message implements Serializable {
    private ErrorInfo errorInfo;
    private DirectoryStructure directoryStructure;

    private Response(){};

    public Response(Command command, String fromPath, String toPath) {
        super(command, fromPath, toPath);
        errorInfo = new ErrorInfo();
        directoryStructure = new DirectoryStructure();
    }

    public Response(Request request) {
        super(request);
        errorInfo = new ErrorInfo();
        directoryStructure = new DirectoryStructure();
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public DirectoryStructure getDirectoryStructure() {
        return directoryStructure;
    }
}
