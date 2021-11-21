package ru.rompet.cloudstorage.common.transfer;

import ru.rompet.cloudstorage.common.Utils;
import ru.rompet.cloudstorage.common.transfer.data.Credentials;
import ru.rompet.cloudstorage.common.transfer.data.PartFileInfo;
import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable, Cloneable {
    private boolean authenticated;
    private Command command;
    private PartFileInfo partFileInfo;
    private Credentials credentials;
    private String fromPath;
    private String toPath;
    private ArrayList<Parameter> parameters;

    protected Message(Command command) {
        this();
        this.command = command;
    }

    protected Message(Message message) {
        this();
        this.command = message.getCommand();
        //Collections.copy(this.parameters, message.getParameters());
        this.parameters = message.getParameters();
        this.fromPath = message.getFromPath();
        this.toPath = message.getToPath();
        this.getCredentials().setLogin(message.getCredentials().getLogin());
    }

    protected Message(){
        partFileInfo = new PartFileInfo();
        credentials = new Credentials();
        parameters = new ArrayList<>();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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

    public boolean hasParameter(Parameter parameter) {
        return parameters.contains(parameter);
    }

    public boolean removeParameters(Parameter... parameter) {
        return Utils.removeParameters(parameters, parameter);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addToPaths(String str) {
        fromPath = fromPath + str;
        toPath = toPath + str;
    }
}
