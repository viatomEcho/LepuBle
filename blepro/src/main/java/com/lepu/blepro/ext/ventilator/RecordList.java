package com.lepu.blepro.ext.ventilator;

import java.util.ArrayList;

public class RecordList {
    private long startTime;  // 列表起始时间s
    private int type;        // 1:当天统计；2:单次统计
    private int size;        // 记录size
    private ArrayList<Record> list = new ArrayList<>();

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArrayList<Record> getList() {
        return list;
    }

    public void setList(ArrayList<Record> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "RecordList{" +
                "startTime=" + startTime +
                ", type=" + type +
                ", size=" + size +
                ", list=" + list +
                '}';
    }

    public class Record {
        private String recordName;  // 文件名
        private long measureTime;   // 记录时间，对应出文件名然后下载s
        private long updateTime;    // 此记录更新时间s

        public long getMeasureTime() {
            return measureTime;
        }

        public void setMeasureTime(long measureTime) {
            this.measureTime = measureTime;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public String getRecordName() {
            return recordName;
        }

        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "recordName='" + recordName + '\'' +
                    ", measureTime=" + measureTime +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }

}
