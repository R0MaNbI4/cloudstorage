package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Request extends Message implements Serializable
{
    private Request(){
    }

    public Request(Command command, String fromPath, String toPath) {
        super(command, fromPath, toPath);
    }

    public Request(Response response) {
        super(response);
    }

    public boolean hasData() {
        return !(getPartFileInfo().getFile() == null || getPartFileInfo().getFile().length == 0);
    }
}