package com.lepu.blepro.ble.data;


import java.util.Arrays;

public class Er2DeviceInfo {
    private String deviceName;
    private String deviceMacAddress;

    private String hwVersion;
    private String fwVersion;
    private String blVersion;
    private String branchCode;
    private int deviceType;
    private String protocolVersion;
    private DeviceTime currentTime;
    private int protocolDataMaxLen;
    private byte snLength;
    private String serialNum;

    private byte[] data;

    public Er2DeviceInfo(String deviceName, String deviceMacAddress, byte[] data) {
        this.deviceName = deviceName;
        this.deviceMacAddress = deviceMacAddress;

        this.data = data;
        char c = (char) data[0];
        hwVersion = String.valueOf(c);
        fwVersion = "".concat(String.valueOf(data[3]))
                .concat(".").concat(String.valueOf(data[2]))
                .concat(".").concat(String.valueOf(data[1]));
        blVersion = "".concat(String.valueOf(data[7]))
                .concat(".").concat(String.valueOf(data[6]))
                .concat(".").concat(String.valueOf(data[5]));
        byte[] tmpBranchCodeData = Arrays.copyOfRange(data, 9, 17);
        branchCode = new String(tmpBranchCodeData);
        deviceType = data[21] + ((data[20] >> 8) & 0xFF);
        protocolVersion = "".concat(String.valueOf(data[23]))
                .concat(".").concat(String.valueOf(data[22]));

        protocolDataMaxLen = data[32] + ((data[31] >> 8) & 0xFF);
        snLength = data[37];
        byte[] tmpSerialNumData = Arrays.copyOfRange(data, 38, 56);
        serialNum = new String(tmpSerialNumData);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public String getBlVersion() {
        return blVersion;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public DeviceTime getCurrentTime() {
        return currentTime;
    }

    public int getProtocolDataMaxLen() {
        return protocolDataMaxLen;
    }

    public byte getSnLength() {
        return snLength;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public byte[] getData() {
        return data;
    }
}
