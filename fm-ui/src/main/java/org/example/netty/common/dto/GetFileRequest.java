package org.example.netty.common.dto;

public class GetFileRequest implements BasicRequest {

    private String filename;
    private String path;

    public String getFilename() {
        return filename;
    }
    public String getFilepath() {
        return path;
    }

    public GetFileRequest(String filename, String path) {
        this.filename = filename;
        this.path = path;
    }

    @Override
    public String getType() {
        return "GetFileRequest";
    }
}
