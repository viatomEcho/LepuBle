package com.lepu.blepro.ext.ventilator;

public class RtState {
    private int ventilationMode;   // 通气模式 0:CPAP  1:APAP  2:S   3:S/T   4:T
    private boolean isVentilated;  // 是否通气 0:0ff;1:on
    private int deviceMode;        // 0:患者模式；1：设备端医生模式；2：BLE端医生模式；3：Socket端医生模式
    private int standard;          // CE/FDA, 1是CFDA, 2是CE, 3是FDA

    public int getVentilationMode() {
        return ventilationMode;
    }

    public void setVentilationMode(int ventilationMode) {
        this.ventilationMode = ventilationMode;
    }

    public boolean isVentilated() {
        return isVentilated;
    }

    public void setVentilated(boolean ventilated) {
        isVentilated = ventilated;
    }

    public int getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(int deviceMode) {
        this.deviceMode = deviceMode;
    }

    public int getStandard() {
        return standard;
    }

    public void setStandard(int standard) {
        this.standard = standard;
    }

    @Override
    public String toString() {
        return "RtState{" +
                "ventilationMode=" + ventilationMode +
                ", isVentilated=" + isVentilated +
                ", deviceMode=" + deviceMode +
                ", standard=" + standard +
                '}';
    }
}
