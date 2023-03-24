package com.lepu.demo.data;

public class EcnData {
    private String fileName;  // 文件名
    private byte[] data;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EcnData{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
