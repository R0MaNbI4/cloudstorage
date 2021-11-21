package ru.rompet.cloudstorage.common.transfer;

import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Request extends Message implements Serializable
{
    private Request(){}

    public Request(Command command) {
        super(command);
    }

    public Request(Response response) {
        super(response);
    }

    public boolean hasData() {
        return !(getPartFileInfo().getFile() == null
                || (getPartFileInfo().getFile().length == 0 && !getPartFileInfo().isLastPart()));
    }
}