package com.lepu.blepro.ble.cmd;

import static com.lepu.blepro.utils.ByteArrayKt.int2ByteArray;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author chenyongfeng
 */
public class BtpBleCmd {

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;
    public static final int SET_TIME = 0xEC;
    public static final int SET_UTC_TIME = 0xC0;
    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int GET_BATTERY = 0xE4;
    public static final int BURN_FACTORY_INFO = 0xEA;
    public static final int FACTORY_RESET_ALL = 0xEE;

    public static final int GET_CONFIG = 0x00;
    public static final int RT_DATA = 0x02;
    public static final int SET_LOW_HR = 0x03;
    public static final int SET_HIGH_HR = 0x04;
    public static final int SET_SYSTEM_SWITCH = 0x05;
    public static final int SET_LOW_TEMP = 0x06;
    public static final int SET_HIGH_TEMP = 0x07;
    public static final int SET_TEMP_UNIT = 0x0C;

//    public static final int GET_FILE_LIST = 0x08;  // 0xF1
//    public static final int FILE_READ_START = 0x09;  // 0xF2
//    public static final int FILE_READ_PKG = 0x0A;  // 0xF3
//    public static final int FILE_READ_END = 0x0B;  // 0xF4

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) sendCmd;
        cmd[2] = (byte) ~sendCmd;
        cmd[3] = (byte) TYPE_NORMAL_SEND;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len << 8);
        System.arraycopy(data, 0, cmd, 7, len);
        cmd[cmd.length-1] = BleCRC.calCRC8(cmd);
        byte[] temp = new byte[4];
        int totalLen = cmd.length+temp.length;
        temp[0] = (byte) totalLen;
        temp[1] = (byte) (totalLen << 8);
        temp[2] = (byte) 0;
        temp[3] = (byte) BleCRC.calCRC8(temp);
        byte[] total = new byte[totalLen];
        System.arraycopy(temp, 0, total, 0, temp.length);
        System.arraycopy(cmd, 0, total, temp.length, cmd.length);

        addNo();
        return total;
    }

    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }

    public static byte[] setLowHr(int lowHr) {
        return getReq(SET_LOW_HR, new byte[]{(byte)lowHr});
    }
    public static byte[] setHighHr(int highHr) {
        return getReq(SET_HIGH_HR, new byte[]{(byte)highHr});
    }
    public static byte[] setLowTemp(int lowTemp) {
        return getReq(SET_LOW_TEMP, int2ByteArray(lowTemp*100));
    }
    public static byte[] setHighTemp(int highTemp) {
        return getReq(SET_HIGH_TEMP, int2ByteArray(highTemp*100));
    }
    public static byte[] setSystemSwitch(boolean hrSwitch, boolean lightSwitch, boolean tempSwitch) {
        int data = 0;
        if(hrSwitch) {
            data = data | 0x01;
        }

        if(lightSwitch) {
            data = data | 0x02;
        }

        if(tempSwitch) {
            data = data | 0x04;
        }
        return getReq(SET_SYSTEM_SWITCH, new byte[]{(byte)data});
    }
    public static byte[] setTempUnit(int unit) {
        return getReq(SET_TEMP_UNIT, new byte[]{(byte)unit});
    }

    public static byte[] getInfo() {
        return getReq(GET_INFO, new byte[0]);
    }

    public static byte[] getConfig() {
        return getReq(GET_CONFIG, new byte[0]);
    }
    public static byte[] getBattery() {
        return getReq(GET_BATTERY, new byte[0]);
    }
    public static byte[] burnFactoryInfo(byte[] config) {
        return getReq(BURN_FACTORY_INFO, config);
    }

    public static byte[] reset() {
        return getReq(RESET, new byte[0]);
    }
    public static byte[] factoryReset() {
        return getReq(FACTORY_RESET, new byte[0]);
    }

    public static byte[] factoryResetAll() {
        return getReq(FACTORY_RESET_ALL, new byte[0]);
    }

    public static byte[] setTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        byte[] cmd = new byte[7];
        cmd[0] = (byte) (c.get(Calendar.YEAR));
        cmd[1] = (byte) (c.get(Calendar.YEAR) >> 8);
        cmd[2] = (byte) (c.get(Calendar.MONTH) + 1);
        cmd[3] = (byte) (c.get(Calendar.DATE));
        cmd[4] = (byte) (c.get(Calendar.HOUR_OF_DAY));
        cmd[5] = (byte) (c.get(Calendar.MINUTE));
        cmd[6] = (byte) (c.get(Calendar.SECOND));
        return getReq(SET_TIME, cmd);
    }
    public static byte[] setUtcTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int len = 8;
        byte[] data = new byte[len];
        data[0] = (byte) (c.get(Calendar.YEAR));
        data[1] = (byte) (c.get(Calendar.YEAR) >> 8);
        data[2] = (byte) (c.get(Calendar.MONTH) + 1);
        data[3] = (byte) (c.get(Calendar.DATE));
        data[4] = (byte) (c.get(Calendar.HOUR_OF_DAY));
        data[5] = (byte) (c.get(Calendar.MINUTE));
        data[6] = (byte) (c.get(Calendar.SECOND));

        int timeZone = (int) (TimeZone.getDefault().getRawOffset()/360000f);
        LepuBleLog.d("setUtcTime===" + timeZone);

        data[7] = (byte) timeZone;

        return getReq(SET_UTC_TIME, data);
    }
}
