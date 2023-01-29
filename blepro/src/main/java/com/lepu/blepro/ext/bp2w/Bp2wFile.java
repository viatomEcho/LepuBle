package com.lepu.blepro.ext.bp2w;

public class Bp2wFile {
    private String fileName;
    private int type;  // 1:BP血压, 2:ECG心电
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
        return "Bp2wFile{" +
                "fileName='" + fileName + '\'' +
                ", type=" + type +
                '}';
    }
}
