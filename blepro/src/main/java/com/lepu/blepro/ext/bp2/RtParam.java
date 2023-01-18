package com.lepu.blepro.ext.bp2;

public class RtParam {
    private int paramDataType;
    private byte[] paramData;
    private byte[] waveData;

    public int getParamDataType() {
        return paramDataType;
    }

    public void setParamDataType(int paramDataType) {
        this.paramDataType = paramDataType;
    }

    public byte[] getParamData() {
        return paramData;
    }

    public void setParamData(byte[] paramData) {
        this.paramData = paramData;
    }

    public byte[] getWaveData() {
        return waveData;
    }

    public void setWaveData(byte[] waveData) {
        this.waveData = waveData;
    }
}
