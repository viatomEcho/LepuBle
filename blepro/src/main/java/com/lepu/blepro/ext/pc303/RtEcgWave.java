package com.lepu.blepro.ext.pc303;

import java.util.Arrays;

public class RtEcgWave {
    private int seqNo;
    private byte[] ecgBytes;
    private int[] ecgInts;
    private float[] ecgFloats;
    private boolean isProbeOff;
    private int digit;

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public byte[] getEcgBytes() {
        return ecgBytes;
    }

    public void setEcgBytes(byte[] ecgBytes) {
        this.ecgBytes = ecgBytes;
    }

    public int[] getEcgInts() {
        return ecgInts;
    }

    public void setEcgInts(int[] ecgInts) {
        this.ecgInts = ecgInts;
    }

    public float[] getEcgFloats() {
        return ecgFloats;
    }

    public void setEcgFloats(float[] ecgFloats) {
        this.ecgFloats = ecgFloats;
    }

    public boolean isProbeOff() {
        return isProbeOff;
    }

    public void setProbeOff(boolean probeOff) {
        isProbeOff = probeOff;
    }

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    @Override
    public String toString() {
        return "RtEcgWave{" +
                "ecgBytes=" + Arrays.toString(ecgBytes) +
                ", ecgInts=" + Arrays.toString(ecgInts) +
                ", ecgFloats=" + Arrays.toString(ecgFloats) +
                ", isProbeOff=" + isProbeOff +
                ", digit=" + digit +
                '}';
    }
}
