package org.example.common.dto;

public class PutFileResponse implements BasicResponse {
    public String getResult() {
        return result;
    }
    public String getDestPath() {
        return destPath;
    }
    private String result;
    private String destPath;
    public PutFileResponse(String result, String destPath) {
        this.result = result;
        this.destPath = destPath;
    }
    @Override
    public String getType() {
        return "PutFileResponse";
    }
}
