package com.lepu.blepro.ext.ecn;

import java.util.ArrayList;

public class FileList {
    private int leftSize;
    private ArrayList<EachFile> list = new ArrayList<>();

    public int getLeftSize() {
        return leftSize;
    }

    public void setLeftSize(int leftSize) {
        this.leftSize = leftSize;
    }

    public ArrayList<EachFile> getList() {
        return list;
    }

    public void setList(ArrayList<EachFile> list) {
        this.list = list;
    }

    public class EachFile {
        private long startTime;
        private String fileName;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return "EachFile{" +
                    "startTime=" + startTime +
                    ", fileName='" + fileName + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FileList{" +
                "leftSize=" + leftSize +
                ", list=" + list +
                '}';
    }
}
