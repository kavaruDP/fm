package org.example.common.dto;

import org.example.client.ServerDirInfo;

import java.io.File;
import java.util.List;

public class GetFileListResponse implements BasicResponse {

    private List<ServerDirInfo> itemList;
    public List<ServerDirInfo> getItemList() {
        return itemList;
    }
    private String clientStringDir;

    public GetFileListResponse(String clientStringDir, List<ServerDirInfo> itemList) {
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
