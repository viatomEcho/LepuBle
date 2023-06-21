package com.lepu.blepro.ble.data;

import com.lepu.blepro.utils.Convertible;

import java.util.Arrays;

public class FwUpdate implements Convertible {

    private int totalMask;
    private int curMask;
    private int deviceType;
    private int version;
    private int size;
    private byte[] data;

    public void setMask(boolean bootloader, boolean app, boolean language) {
        if(bootloader) {
            totalMask = totalMask | 0x01;
        }

        if(app) {
            totalMask = totalMask | 0x02;
        }

        if(language) {
            totalMask = totalMask | 0x04;
        }
    }

    public int getTotalMask() {
        return totalMask;
    }

    public void setTotalMask(int totalMask) {
        this.totalMask = totalMask;
    }

    public int getCurMask() {
        return curMask;
    }

    public void setCurMask(int curMask) {
        this.curMask = curMask;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] convert2Data() {
        byte[] data = new byte[11];
        data[0] = (byte) deviceType;
        data[1] = (byte) (deviceType >> 8);
        data[2] = (byte) curMask;
        data[3] = (byte) totalMask;
        data[4] = (byte) version;
        data[5] = (byte) (version >> 8);
        data[6] = (byte) (version >> 16);
        data[7] = (byte) size;
        data[8] = (byte) (size >> 8);
        data[9] = (byte) (size >> 16);
        data[10] = (byte) (size >> 24);
        return data;
    }

    @Override
    public String toString() {
        return "FwUpdate{" +
                "totalMask=" + totalMask +
                ", curMask=" + curMask +
                ", deviceType=" + deviceType +
                ", version=" + version +
                ", size=" + size +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
