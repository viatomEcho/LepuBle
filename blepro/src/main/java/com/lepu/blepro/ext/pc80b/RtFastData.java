package com.lepu.blepro.ext.pc80b;

public class RtFastData {
    private int seqNo;
    private float gain;
    private int channel;
    private int measureMode;
    private int measureStage;
    private int hr;
    private boolean leadOff;
    private int dataType;
    private RtEcgData ecgData;
    private RtEcgResult ecgResult;

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public int getMeasureStage() {
        return measureStage;
    }

    public void setMeasureStage(int measureStage) {
        this.measureStage = measureStage;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public boolean isLeadOff() {
        return leadOff;
    }

    public void setLeadOff(boolean leadOff) {
        this.leadOff = leadOff;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public RtEcgData getEcgData() {
        return ecgData;
    }

    public void setEcgData(RtEcgData ecgData) {
        this.ecgData = ecgData;
    }

    public RtEcgResult getEcgResult() {
        return ecgResult;
    }

    public void setEcgResult(RtEcgResult ecgResult) {
        this.ecgResult = ecgResult;
    }

    @Override
    public String toString() {
        return "RtFastData{" +
                "seqNo=" + seqNo +
                ", gain=" + gain +
                ", channel=" + channel +
                ", measureMode=" + measureMode +
                ", measureStage=" + measureStage +
                ", leadOff=" + leadOff +
                ", dataType=" + dataType +
                ", ecgData=" + ecgData +
                ", ecgResult=" + ecgResult +
                '}';
    }
}
