package ru.rompet.cloudstorage.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Credentials {
    private String login;
    private String password;

    public Credentials() {
        this.login = "";
        this.password = "";
    }

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return login.equals("") || password.equals("");
    }
}
