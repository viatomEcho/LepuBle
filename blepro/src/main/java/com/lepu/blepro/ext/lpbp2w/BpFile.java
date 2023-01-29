package com.lepu.blepro.ext.lpbp2w;

public class BpFile {
    private int fileVersion;       // 文件版本 e.g.  0x01 :  V1
    private int fileType;          // 文件类型 1：血压；2：心电
    private int measureTime;       // 测量时间时间戳s
    private int measureMode;       // 测量模式 0:单次模式 1:X3模式 2:间隔模式
    private int measureInterval;   // 测量间隔单位s 仅非单次模式有效
    private boolean upload;        // 上传标识
    private int taskId;            // 所属任务id 间隔模式有效
    private int sys;               // 收缩压
    private int dia;               // 舒张压
    private int mean;              // 平均压
    private int pr;                // 心率
    private boolean arrhythmia;    // 诊断结果 bit0:心率不齐

    public BpFile(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2BpFile data = new com.lepu.blepro.ble.data.Bp2BpFile(bytes);
        fileVersion = data.getFileVersion();
        fileType = data.getFileType();
        measureTime = data.getMeasureTime();
        measureMode = data.getMeasureMode();
        measureInterval = data.getMeasureInterval();
        upload = data.getUploadTag();
        taskId = data.getTaskId();
        sys = data.getSys();
        dia = data.getDia();
        mean = data.getMean();
        pr = data.getPr();
        arrhythmia = data.getResult() == 1;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(int measureTime) {
        this.measureTime = measureTime;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public int getMeasureInterval() {
        return measureInterval;
    }

    public void setMeasureInterval(int measureInterval) {
        this.measureInterval = measureInterval;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMean() {
        return mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public boolean isArrhythmia() {
        return arrhythmia;
    }

    public void setArrhythmia(boolean arrhythmia) {
        this.arrhythmia = arrhythmia;
    }

    @Override
    public String toString() {
        return "BpFile{" +
                "fileVersion=" + fileVersion +
                ", fileType=" + fileType +
                ", measureTime=" + measureTime +
                ", measureMode=" + measureMode +
                ", measureInterval=" + measureInterval +
                ", upload=" + upload +
                ", taskId=" + taskId +
                ", sys=" + sys +
                ", dia=" + dia +
                ", mean=" + mean +
                ", pr=" + pr +
                ", arrhythmia=" + arrhythmia +
                '}';
    }
}
