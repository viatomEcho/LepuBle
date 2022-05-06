package com.lepu.blepro.ext.pc60fw;

import java.util.Arrays;

public class PpgData {
    private byte[] redFrqBytes;
    private int redFrqInt;
    private byte[] irFrqBytes;
    private int irFrqInt;

    public byte[] getRedFrqBytes() {
        return redFrqBytes;
    }

    public void setRedFrqBytes(byte[] redFrqBytes) {
        this.redFrqBytes = redFrqBytes;
    }

    public int getRedFrqInt() {
        return redFrqInt;
    }

    public void setRedFrqInt(int redFrqInt) {
        this.redFrqInt = redFrqInt;
    }

    public byte[] getIrFrqBytes() {
        return irFrqBytes;
    }

    public void setIrFrqBytes(byte[] irFrqBytes) {
        this.irFrqBytes = irFrqBytes;
    }

    public int getIrFrqInt() {
        return irFrqInt;
    }

    public void setIrFrqInt(int irFrqInt) {
        this.irFrqInt = irFrqInt;
    }

    @Override
    public String toString() {
        return "PpgData{" +
                "redFrqBytes=" + Arrays.toString(redFrqBytes) +
                ", redFrqInt=" + redFrqInt +
                ", irFrqBytes=" + Arrays.toString(irFrqBytes) +
                ", irFrqInt=" + irFrqInt +
                '}';
    }
}
