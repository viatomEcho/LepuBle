package com.lepu.demo.data;

public class WirelessData {

    // 开始时间
    private long startTime;
    // 测量时长 00:00
    private int recordTime;
    // 数据总量 kb
    private int totalBytes;
    // 实际数据
    private int receiveBytes;
    // 总包数
    private int totalSize;
    // 丢包数
    private int missSize;
    // 错误字节
    private int errorBytes;
    // 丢包率
    private double missPercent;
    // 误码率
    private double errorPercent;
    // 数据传输速度
    private double speed;
    // 吞吐量 kb/h
    private double throughput;
    // 单次时延
    private long oneDelay;
    // 总时延
    private long totalDelay;

    public int getReceiveBytes() {
        return receiveBytes;
    }

    public void setReceiveBytes(int receiveBytes) {
        this.receiveBytes = receiveBytes;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getErrorBytes() {
        return errorBytes;
    }

    public void setErrorBytes(int errorBytes) {
        this.errorBytes = errorBytes;
    }

    public int getMissSize() {
        return missSize;
    }

    public void setMissSize(int missSize) {
        this.missSize = missSize;
    }

    public double getErrorPercent() {
        return errorPercent;
    }

    public void setErrorPercent(double errorPercent) {
        this.errorPercent = errorPercent;
    }

    public double getMissPercent() {
        return missPercent;
    }

    public void setMissPercent(double missPercent) {
        this.missPercent = missPercent;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public long getOneDelay() {
        return oneDelay;
    }

    public void setOneDelay(long oneDelay) {
        this.oneDelay = oneDelay;
    }

    public long getTotalDelay() {
        return totalDelay;
    }

    public void setTotalDelay(long totalDelay) {
        this.totalDelay = totalDelay;
    }

    @Override
    public String toString() {
        return "WirelessData{" +
                "\"recordTime\":" + recordTime +
                ",\"startTime\":" + startTime +
                ",\"receiveBytes\":" + receiveBytes +
                ",\"totalBytes\":" + totalBytes +
                ",\"totalSize\":" + totalSize +
                ",\"errorBytes\":" + errorBytes +
                ",\"missSize\":" + missSize +
                ",\"errorPercent\":" + errorPercent +
                ",\"missPercent\":" + missPercent +
                ",\"speed\":" + speed +
                ",\"throughput\":" + throughput +
                ",\"oneDelay\":" + oneDelay +
                ",\"totalDelay\":" + totalDelay +
                "}";
    }

}

