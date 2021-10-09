package ru.rompet.cloudstorage.common;

import java.io.Serializable;

public class Message implements Serializable {
    private Command command;
    private String filename;
    private PartFileInfo partFileInfo;

    protected Message(Command command, String filename) {
        setCommand(command);
        setFilename(filename);
        partFileInfo = new PartFileInfo();
    }

    protected Message(Message message) {
        setCommand(message.getCommand());
        setFilename(message.getFilename());
        partFileInfo = new PartFileInfo();
    }

    protected Message(){}

    public Command getCommand() {
        return command;
    }

    public String getFilename() {
        return filename;
    }

    protected void setCommand(Command command) {
        this.command = command;
    }

    protected void setFilename(String filename) {
        this.filename = filename;
    }

    public PartFileInfo partFileInfo() {
        return partFileInfo;
    }
}
