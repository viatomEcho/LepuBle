package com.lepu.blepro.ext.pc80b;

public class DeviceInfo {
    private String softwareV;
    private String hardwareV;
    private String algorithmV;

    public String getSoftwareV() {
        return softwareV;
    }

    public void setSoftwareV(String softwareV) {
        this.softwareV = softwareV;
    }

    public String getHardwareV() {
        return hardwareV;
    }

    public void setHardwareV(String hardwareV) {
        this.hardwareV = hardwareV;
    }

    public String getAlgorithmV() {
        return algorithmV;
    }

    public void setAlgorithmV(String algorithmV) {
        this.algorithmV = algorithmV;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "softwareV='" + softwareV + '\'' +
                ", hardwareV='" + hardwareV + '\'' +
                ", algorithmV='" + algorithmV + '\'' +
                '}';
    }
}
