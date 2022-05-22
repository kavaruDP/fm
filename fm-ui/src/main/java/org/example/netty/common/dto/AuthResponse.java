package org.example.netty.common.dto;

public class AuthResponse implements BasicResponse {
    public String getResult() {
        return result;
    }

    private String result;
    private String fullClientHomeDir = "";

    public AuthResponse(String result) {
        this.result = result;
    }
    @Override
    public String getType() {
        return "AuthResponse";
    }
    public String getFullClientHomeDir() {
        return fullClientHomeDir;
    }
    public void setFullClientHomeDir(String ClientHomeDir) {
        this.fullClientHomeDir = ClientHomeDir;
    }
}
