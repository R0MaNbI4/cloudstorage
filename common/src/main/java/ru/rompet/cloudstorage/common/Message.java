package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.data.PartFileInfo;
import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Message implements Serializable {
    private Command command;
    private String fromPath;
    private String toPath;
    private PartFileInfo partFileInfo;

    protected Message(Command command, String fromPath, String toPath) {
        this.command = command;
        this.fromPath = fromPath;
        this.toPath = toPath;
        partFileInfo = new PartFileInfo();
    }

    protected Message(Message message) {
        this.command = message.getCommand();
        this.fromPath = message.getFromPath();
        this.toPath = message.getToPath();
        partFileInfo = new PartFileInfo();
    }

    protected Message(){}

    public Command getCommand() {
        return command;
    }

    public String getFromPath() {
        return fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public PartFileInfo getPartFileInfo() {
        return partFileInfo;
    }
}
