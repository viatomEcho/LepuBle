package com.lepu.blepro.ext.pc60fw;

public class WorkingStatus {
    private int mode;
    private int step;
    private int para1;
    private int para2;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getPara1() {
        return para1;
    }

    public void setPara1(int para1) {
        this.para1 = para1;
    }

    public int getPara2() {
        return para2;
    }

    public void setPara2(int para2) {
        this.para2 = para2;
    }

    @Override
    public String toString() {
        return "WorkingStatus{" +
                "mode=" + mode +
                ", step=" + step +
                ", para1=" + para1 +
                ", para2=" + para2 +
                '}';
    }
}
