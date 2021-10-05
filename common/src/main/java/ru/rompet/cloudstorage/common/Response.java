package ru.rompet.cloudstorage.common;

public class Response {
    private Command command;
    private String filename;
    private long position;
    private byte[] file;
    private boolean isLastPart;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public boolean isLastPart() {
        return isLastPart;
    }

    public void setLastPart(boolean lastPart) {
        isLastPart = lastPart;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
