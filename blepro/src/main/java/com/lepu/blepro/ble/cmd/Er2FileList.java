package com.lepu.blepro.ble.cmd;

import java.util.Arrays;
import java.util.List;

public class Er2FileList {
    private byte fileNum;
    private String[] fileNames;
    public Er2FileList(byte[] data) {
        fileNum = data[0];
        fileNames = new String[fileNum];
        for(int i = 0; i < fileNum; i++) {
            byte[] tmeData = Arrays.copyOfRange(data, (i * 16) + 1, (i + 1) * 16 + 1);
            String name = new String(tmeData).trim();
            fileNames[i] = name;
        }
    }

    public byte getFileNum() {
        return fileNum;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public List<String> listFileName() {
        return Arrays.asList(fileNames);
    }

    @Override
    public String toString() {
        return "Er2FileList{" +
                "fileNum=" + fileNum +
                ", fileNames=" + Arrays.toString(fileNames) +
                '}';
    }
}
