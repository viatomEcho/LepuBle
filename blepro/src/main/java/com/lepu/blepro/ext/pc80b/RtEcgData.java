package com.lepu.blepro.ext.pc80b;

import java.util.Arrays;

public class RtEcgData {
    private byte[] ecgBytes;
    private int[] ecgInts;
    private float[] ecgFloats;

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

    @Override
    public String toString() {
        return "RtEcgData{" +
                "ecgBytes=" + Arrays.toString(ecgBytes) +
                "ecgInts=" + Arrays.toString(ecgInts) +
                ", ecgFloats=" + Arrays.toString(ecgFloats) +
                '}';
    }
}
