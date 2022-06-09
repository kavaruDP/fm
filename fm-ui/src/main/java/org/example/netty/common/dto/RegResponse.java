package org.example.netty.common.dto;

public class RegResponse implements BasicResponse {
    public String getResult() {
        return result;
    }

    private String result;

    public RegResponse(String result) {
        this.result = result;
    }
    @Override
    public String getType() {
        return "AuthResponse";
    }
}
