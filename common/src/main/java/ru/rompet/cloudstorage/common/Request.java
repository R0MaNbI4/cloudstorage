package ru.rompet.cloudstorage.common;

public class Request {
    private String filename;
    private Command command;
    private byte[] file;
    private long position;
    private boolean isLastPart;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public boolean isLastPart() {
        return isLastPart;
    }

    public void setLastPart(boolean lastPart) {
        isLastPart = lastPart;
    }

    public boolean hasData() {
        return getFile() != null;
    }
}
