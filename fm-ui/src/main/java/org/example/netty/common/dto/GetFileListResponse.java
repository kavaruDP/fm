package org.example.netty.common.dto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GetFileListResponse implements BasicResponse {

    private List<File> itemList;
    public List<File> getItemList() {
        return itemList;
    }
    private String clientStringDir;

    public GetFileListResponse(String clientStringDir, List<File> itemList) {
        this.itemList = itemList;
        this.clientStringDir = clientStringDir;
    }

    @Override
    public String getType() {
        return "GetFileListResponse";
    }

    public String getClientStringDir() {
        return clientStringDir;
    }
}
