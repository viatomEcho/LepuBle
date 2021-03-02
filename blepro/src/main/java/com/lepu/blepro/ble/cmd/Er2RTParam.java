package com.lepu.blepro.ble.cmd;

public class Er2RTParam {

    public static final int LENGTH = 20;

    private int hr;
    private byte sysFlag;
    private byte percent;
    private int recordTime;
    private byte runStatus;

    public Er2RTParam(byte[] data) {
        if(data == null || data.length != LENGTH) {
            return;
        }

        hr = data[0] & 0xFF + ((data[1] & 0xFF) << 8);
        sysFlag = data[2];
        percent = data[3];

        recordTime = (data[4] & 0xFF) + ((data[5] & 0xFF) << 8)
                + ((data[6] & 0xFF) << 16) + ((data[7] & 0xFF) << 24);

        runStatus = data[8];

    }

    public int getHr() {
        return hr;
    }

    public byte getSysFlag() {
        return sysFlag;
    }

    public boolean isrFlag() {
        byte sysFlag = getSysFlag();
        return (sysFlag & 0x01) > 0;
    }

    public boolean isSignalPoor() {
        byte sysFlag = getSysFlag();
        return (sysFlag & 0x04) > 0;
    }

    public int getBatteryState() {
        byte sysFlag = getSysFlag();
        return (sysFlag >> 6 ) & 0x03;
    }

    public byte getPercent() {
        return percent;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public byte getRunStatus() {
        return runStatus;
    }

    public int getCurrentState() {
        byte runStatus = getRunStatus();
        return runStatus & 0x0F;
    }

    public int getLastState() {
        byte runStatus = getRunStatus();
        return (runStatus >> 4) & 0x0F;
    }
}
