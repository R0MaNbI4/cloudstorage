package ru.rompet.cloudstorage.common;

import java.io.Serializable;

public class Request extends Message implements Serializable
{
    private Request(){
    }

    public Request(Command command, String filename) {
        super(command, filename);
    }

    public Request(Response response) {
        super(response);
    }

    public boolean hasData() {
        return partFileInfo().getFile() == null || partFileInfo().getFile().length == 0;
    }
}