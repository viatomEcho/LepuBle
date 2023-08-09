package com.lepu.blepro.ble.data;

public class TmbInfo {
    private String manufacturer;
    private String model;
    private String serial;
    private String hv;
    private String fv;
    private String sv;
    private String id;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getHv() {
        return hv;
    }

    public void setHv(String hv) {
        this.hv = hv;
    }

    public String getFv() {
        return fv;
    }

    public void setFv(String fv) {
        this.fv = fv;
    }

    public String getSv() {
        return sv;
    }

    public void setSv(String sv) {
        this.sv = sv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TmbInfo{" +
                "manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", serial='" + serial + '\'' +
                ", hv='" + hv + '\'' +
                ", fv='" + fv + '\'' +
                ", sv='" + sv + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
