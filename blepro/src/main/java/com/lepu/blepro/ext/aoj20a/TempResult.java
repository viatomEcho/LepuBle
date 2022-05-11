package com.lepu.blepro.ext.aoj20a;

public class TempResult {

    private float temp;
    private int mode;
    private String modeMess;

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getModeMess() {
        return modeMess;
    }

    public void setModeMess(String modeMess) {
        this.modeMess = modeMess;
    }

    @Override
    public String toString() {
        return "TempResult{" +
                "temp=" + temp +
                ", mode=" + mode +
                ", modeMess='" + modeMess + '\'' +
                '}';
    }
}
