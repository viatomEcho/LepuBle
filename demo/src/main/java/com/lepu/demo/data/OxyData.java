package com.lepu.demo.data;

import com.lepu.blepro.ble.data.OxyBleFile;

public class OxyData {

    private String fileName;
    private OxyBleFile oxyBleFile;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public OxyBleFile getOxyBleFile() {
        return oxyBleFile;
    }

    public void setOxyBleFile(OxyBleFile oxyBleFile) {
        this.oxyBleFile = oxyBleFile;
    }

    @Override
    public String toString() {
        return "OxyData{" +
                "fileName='" + fileName + '\'' +
                ", oxyBleFile=" + oxyBleFile +
                '}';
    }
}
