package org.example.common.dto;

import org.example.client.FileMessage;

public class PutFileRequest implements BasicRequest {
    private FileMessage fileMessage;
    private String srcPath;
    private String destPath;
    public FileMessage getFileMessage() {
        return fileMessage;
    }
    public String getDestPath() { return destPath; }
    public String getSrcPath() { return srcPath; }
    public PutFileRequest(FileMessage fileMessage, String srcPath, String destPath) {
        this.fileMessage = fileMessage;
        this.destPath = destPath;
        this.srcPath = srcPath;
    }

    @Override
    public String getType() {
        return "PutFileRequest";
    }
}
