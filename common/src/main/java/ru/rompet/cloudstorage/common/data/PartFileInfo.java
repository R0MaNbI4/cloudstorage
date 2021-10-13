package ru.rompet.cloudstorage.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.rompet.cloudstorage.common.Message;

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
        setPosition(message.getPartFileInfo().getPosition());
    }

    public void addPosition(Message message, int add) {
        setPosition(message.getPartFileInfo().getPosition() + add);
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

    @JsonIgnore
    public boolean isFirstPart() {
        return position == 0;
    }
}
