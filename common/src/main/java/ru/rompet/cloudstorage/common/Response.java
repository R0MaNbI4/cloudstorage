package ru.rompet.cloudstorage.common;

import java.io.Serializable;
import java.util.ArrayList;

public class Response extends Message implements Serializable {
    private ErrorInfo errorInfo;
    private DirectoryStructure directoryStructure;

    private Response(){};

    public Response(Command command, String filename) {
        super(command, filename);
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
