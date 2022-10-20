package com.lepu.blepro.ext.er1;

public class RtWave {

    private byte[] ecgBytes;
    private short[] ecgShorts;
    private float[] ecgFloats;

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
