package ru.rompet.cloudstorage.common;

import java.io.Serializable;

public class Response extends Message implements Serializable {
    private ErrorInfo errorInfo;

    private Response(){};

    public Response(Command command, String filename) {
        super(command, filename);
        errorInfo = new ErrorInfo();
    }

    public Response(Request request) {
        super(request);
        errorInfo = new ErrorInfo();
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
