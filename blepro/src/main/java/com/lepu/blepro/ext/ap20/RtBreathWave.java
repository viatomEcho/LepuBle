package com.lepu.blepro.ext.ap20;

import java.util.Arrays;

public class RtBreathWave {
    private byte[] flowBytes;
    private int flowInt;
    private byte[] snoreBytes;
    private int snoreInt;

    public byte[] getFlowBytes() {
        return flowBytes;
    }

    public void setFlowBytes(byte[] flowBytes) {
        this.flowBytes = flowBytes;
    }

    public int getFlowInt() {
        return flowInt;
    }

    public void setFlowInt(int flowInt) {
        this.flowInt = flowInt;
    }

    public byte[] getSnoreBytes() {
        return snoreBytes;
    }

    public void setSnoreBytes(byte[] snoreBytes) {
        this.snoreBytes = snoreBytes;
    }

    public int getSnoreInt() {
        return snoreInt;
    }

    public void setSnoreInt(int snoreInt) {
        this.snoreInt = snoreInt;
    }

    @Override
    public String toString() {
        return "RtBreathWave{" +
                "flowBytes=" + Arrays.toString(flowBytes) +
                ", flowInt=" + flowInt +
                ", snoreBytes=" + Arrays.toString(snoreBytes) +
                ", snoreInt=" + snoreInt +
                '}';
    }
}
