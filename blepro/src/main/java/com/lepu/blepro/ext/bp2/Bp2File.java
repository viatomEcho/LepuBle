package com.lepu.blepro.ext.bp2;

import java.util.Arrays;

public class Bp2File {
    private String fileName;
    private int type;
    private byte[] content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Bp2File{" +
                "fileName='" + fileName + '\'' +
                ", type=" + type +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
