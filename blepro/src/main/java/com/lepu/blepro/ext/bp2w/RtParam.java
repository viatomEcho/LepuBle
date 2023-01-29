package com.lepu.blepro.ext.bp2w;

public class RtParam {
    private int paramDataType;
    private byte[] paramData;
    private byte[] ecgBytes;
    private short[] ecgShorts;
    private float[] ecgFloats;

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

    public byte[] getEcgBytes() {
        return ecgBytes;
    }

    public void setEcgBytes(byte[] ecgBytes) {
        this.ecgBytes = ecgBytes;
    }

    public short[] getEcgShorts() {
        return ecgShorts;
    }

    public void setEcgShorts(short[] ecgShorts) {
        this.ecgShorts = ecgShorts;
    }

    public float[] getEcgFloats() {
        return ecgFloats;
    }

    public void setEcgFloats(float[] ecgFloats) {
        this.ecgFloats = ecgFloats;
    }
}
