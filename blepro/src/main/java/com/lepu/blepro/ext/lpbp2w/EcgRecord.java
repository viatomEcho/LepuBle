package com.lepu.blepro.ext.lpbp2w;

public class EcgRecord {
    private long startIime;          // 测量时间戳s
    private String fileName;         // 文件名
    private int uid;                 // 用户id
    private int recordingTime;       // 记录时长 单位s
    private int result;
    private EcgDiagnosis diagnosis;  // 诊断结果
    private int hr;                  // 单位bpm
    private int qrs;                 // 单位ms
    private int pvcs;                // 单位个
    private int qtc;                 // 单位ms

    public long getStartIime() {
        return startIime;
    }

    public void setStartIime(long startIime) {
        this.startIime = startIime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public EcgDiagnosis getDiagnosis() {
        return new EcgDiagnosis(result);
    }

    public void setDiagnosis(EcgDiagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getQrs() {
        return qrs;
    }

    public void setQrs(int qrs) {
        this.qrs = qrs;
    }

    public int getPvcs() {
        return pvcs;
    }

    public void setPvcs(int pvcs) {
        this.pvcs = pvcs;
    }

    public int getQtc() {
        return qtc;
    }

    public void setQtc(int qtc) {
        this.qtc = qtc;
    }

    @Override
    public String toString() {
        return "EcgRecord{" +
                "startIime=" + startIime +
                ", fileName='" + fileName + '\'' +
                ", uid=" + uid +
                ", recordingTime=" + recordingTime +
                ", result=" + result +
                ", diagnosis=" + diagnosis +
                ", hr=" + hr +
                ", qrs=" + qrs +
                ", pvcs=" + pvcs +
                ", qtc=" + qtc +
                '}';
    }
}
