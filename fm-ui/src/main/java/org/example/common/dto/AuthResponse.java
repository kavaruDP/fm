package org.example.common.dto;

public class AuthResponse implements BasicResponse {
    public String getResult() {
        return result;
    }

    private String result;
    private String clientHomeDir = "";

    public AuthResponse(String result) {
        this.result = result;
    }
    @Override
    public String getType() {
        return "AuthResponse";
    }
    public String getClientHomeDir() {
        return clientHomeDir;
    }
    public void setClientHomeDir(String clientHomeDir) {
        this.clientHomeDir = clientHomeDir;
    }
}
