package ru.rompet.cloudstorage.common.transfer;

import ru.rompet.cloudstorage.common.transfer.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.transfer.data.ErrorInfo;
import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Response extends Message implements Serializable {
    private ErrorInfo errorInfo;
    private DirectoryStructure directoryStructure;

    private Response(){};

    public Response(Command command) {
        super(command);
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
