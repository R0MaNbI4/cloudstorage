package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Request extends Message implements Serializable, Cloneable
{
    private Request(){}

    public Request(Command command) {
        super(command);
    }

    public Request(Response response) {
        super(response);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean hasData() {
        return !(getPartFileInfo().getFile() == null || getPartFileInfo().getFile().length == 0);
    }
}