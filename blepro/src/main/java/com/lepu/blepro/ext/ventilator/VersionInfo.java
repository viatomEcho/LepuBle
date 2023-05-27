package com.lepu.blepro.ext.ventilator;

public class VersionInfo {
    private String hwV;    // 硬件版本 e.g. 'A' : A版
    private String fwV;   // 固件版本 e.g. 0x00010200 : V1.2.0
    private String blV;   // 引导版本 e.g. 0x00010200 : V1.2.0
    private String bleV;  // 蓝牙驱动版本 e.g. 0x00010200 : V1.2.0
    private String algV;  // 算法版本

    public String getHwV() {
        return hwV;
    }

    public void setHwV(String hwV) {
        this.hwV = hwV;
    }

    public String getFwV() {
        return fwV;
    }

    public void setFwV(String fwV) {
        this.fwV = fwV;
    }

    public String getBlV() {
        return blV;
    }

    public void setBlV(String blV) {
        this.blV = blV;
    }

    public String getBleV() {
        return bleV;
    }

    public void setBleV(String bleV) {
        this.bleV = bleV;
    }

    public String getAlgV() {
        return algV;
    }

    public void setAlgV(String algV) {
        this.algV = algV;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "hwV='" + hwV + '\'' +
                ", fwV='" + fwV + '\'' +
                ", blV='" + blV + '\'' +
                ", bleV='" + bleV + '\'' +
                ", algV='" + algV + '\'' +
                '}';
    }
}
