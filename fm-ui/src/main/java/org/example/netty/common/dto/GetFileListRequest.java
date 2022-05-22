package org.example.netty.common.dto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GetFileListRequest implements BasicRequest {
    private String clientDir;
    public String getClientCurrentDir() {
        return clientDir;
    }
    public GetFileListRequest(String clientCurrentDir) {
        this.clientDir = clientCurrentDir;
    }


    @Override
    public String getType() {
        return "GetFileListRequest";
    }
}

