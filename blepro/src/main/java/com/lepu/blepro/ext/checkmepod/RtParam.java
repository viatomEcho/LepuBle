package com.lepu.blepro.ext.checkmepod;

public class RtParam {
    private int pr;
    private int spo2;
    private float pi;
    private float temp;
    private int oxyState;
    private int tempState;
    private int batteryState;
    private int battery;
    private int runStatus;

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public int getOxyState() {
        return oxyState;
    }

    public void setOxyState(int oxyState) {
        this.oxyState = oxyState;
    }

    public int getTempState() {
        return tempState;
    }

    public void setTempState(int tempState) {
        this.tempState = tempState;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(int runStatus) {
        this.runStatus = runStatus;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "pr=" + pr +
                ", spo2=" + spo2 +
                ", pi=" + pi +
                ", temp=" + temp +
                ", oxyState=" + oxyState +
                ", tempState=" + tempState +
                ", batteryState=" + batteryState +
                ", battery=" + battery +
                ", runStatus=" + runStatus +
                '}';
    }
}
