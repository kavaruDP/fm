package org.example.common.dto;

public class GetFileRequest implements BasicRequest {

    private String filename;
    private String destFilepath;
    private String srcFilepath;
    public String getFilename() {
        return filename;
    }
    public String getDestFilepath() {
        return destFilepath;
    }
    public String getSrcFilepath() {
        return srcFilepath;
    }

    public GetFileRequest(String filename, String srcFilepath, String destFilepath) {
        this.filename = filename;
        this.srcFilepath = srcFilepath;
        this.destFilepath = destFilepath;
    }

    @Override
    public String getType() {
        return "GetFileRequest";
    }
}
