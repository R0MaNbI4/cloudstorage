package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.data.Credentials;
import ru.rompet.cloudstorage.common.data.PartFileInfo;
import ru.rompet.cloudstorage.common.enums.Command;

import java.io.Serializable;

public class Message implements Serializable {
    private boolean authenticated;
    private Command command;
    private PartFileInfo partFileInfo;
    private Credentials credentials;
    private String fromPath;
    private String toPath;

    protected Message(Command command) {
        this();
        this.command = command;
    }

    protected Message(Message message) {
        this();
        this.command = message.getCommand();
        this.fromPath = message.getFromPath();
        this.toPath = message.getToPath();
        this.getCredentials().setLogin(message.getCredentials().getLogin());
    }

    protected Message(){
        partFileInfo = new PartFileInfo();
        credentials = new Credentials();
    }

    public Command getCommand() {
        return command;
    }

    public PartFileInfo getPartFileInfo() {
        return partFileInfo;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
