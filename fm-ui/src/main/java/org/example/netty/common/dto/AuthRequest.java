package org.example.netty.common.dto;

public class AuthRequest implements BasicRequest {

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    private String login;
    private String password;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;

    }
    @Override
    public String getType() {
        return "AuthRequest";
    }

}
