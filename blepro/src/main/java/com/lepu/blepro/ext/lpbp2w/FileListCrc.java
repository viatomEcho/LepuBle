package com.lepu.blepro.ext.lpbp2w;

public class FileListCrc {
    private int fileType;
    private int crc;

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    @Override
    public String toString() {
        return "FileListCrc{" +
                "fileType=" + fileType +
                ", crc=" + crc +
                '}';
    }
}
