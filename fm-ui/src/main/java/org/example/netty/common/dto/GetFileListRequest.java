package org.example.netty.common.dto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GetFileListRequest implements BasicRequest {

    @Override
    public String getType() {
        return "GetFileListRequest";
    }
}
