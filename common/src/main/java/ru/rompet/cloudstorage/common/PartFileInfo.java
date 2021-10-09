package ru.rompet.cloudstorage.common;

import java.io.Serializable;

public class PartFileInfo implements Serializable {
    private long position;
    private byte[] file;
    private boolean isLastPart;

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setPosition(Message message) {
        setPosition(message.partFileInfo().getPosition());
    }

    public void addPosition(Message message, int add) {
        setPosition(message.partFileInfo().getPosition() + add);
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
}
