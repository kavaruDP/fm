package org.example.client;

import java.io.File;
import java.io.Serializable;

public class ServerDirInfo implements Serializable {
    private static final long serialVersionUID = 145673298473543546L;

    @Override
    public String toString() {
        return "filename='" + filename + '\'' +
                ", size='" + size;
    }


    public ServerDirInfo(File file) {
        this.filename = file.getName();
        this.size = file.length();
    }

    private String filename;
    private long size;
    public String getFilename() {
        return filename;
    }
    public long getSize() {
        return size;
    }


}
