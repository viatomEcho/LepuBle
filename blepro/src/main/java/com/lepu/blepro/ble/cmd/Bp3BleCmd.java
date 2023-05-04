package com.lepu.blepro.ble.cmd;

import static com.lepu.blepro.utils.ByteArrayKt.int2ByteArray;
import com.lepu.blepro.utils.CrcUtil;

/**
 * @author chenyongfeng
 */
public class Bp3BleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    public static final int GET_CONFIG = 0x00;
    public static final int CALIBRATION_ZERO = 0x01;
    public static final int CALIBRATION_SLOPE = 0x02;
    public static final int RT_PRESSURE = 0x05;
    public static final int RT_WAVE = 0x07;
    public static final int RT_DATA = 0x08;
    public static final int PRESSURE_TEST = 0x0A;
    public static final int SET_CONFIG = 0x0B;
    public static final int SWITCH_VALVE = 0x0C;
    public static final int CURRENT_PRESSURE = 0x0D;
    public static final int SWITCH_TEST_MODE = 0x0E;
    public static final int SWITCH_BP_UNIT = 0x10;

    public static final int GET_WIFI_LIST = 0x11;
    public static final int SET_WIFI_CONFIG = 0x12;
    public static final int GET_WIFI_CONFIG = 0x13;
    public static final int SWITCH_WIFI_4G = 0x14;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getConfig() {
        return getReq(GET_CONFIG, new byte[0]);
    }
    public static byte[] calibrationZero() {
        return getReq(CALIBRATION_ZERO, new byte[0]);
    }
    public static byte[] calibrationSlope(int pressure) {
        return getReq(CALIBRATION_SLOPE, int2ByteArray(pressure));
    }
    public static byte[] getRtPressure(int rate) {
        return getReq(RT_PRESSURE, new byte[]{(byte)rate});
    }
    public static byte[] getRtWave() {
        return getReq(RT_WAVE, new byte[0]);
    }
    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }
    public static byte[] pressureTest(int pressure) {
        return getReq(PRESSURE_TEST, int2ByteArray(pressure));
    }
    public static byte[] setConfig(byte[] data) {
        return getReq(SET_CONFIG, data);
    }
    public static byte[] switchValve(boolean on) {
        int state = 0;
        if (on) {
            state = 1;
        }
        return getReq(SWITCH_VALVE, new byte[]{(byte)state});
    }
    public static byte[] getCurPressure() {
        return getReq(CURRENT_PRESSURE, new byte[0]);
    }
    public static byte[] switchTestMode(int mode) {
        return getReq(SWITCH_TEST_MODE, new byte[]{(byte)mode});
    }
    public static byte[] switchBpUnit(int unit) {
        return getReq(SWITCH_BP_UNIT, new byte[]{(byte)unit});
    }
    public static byte[] getWifiList(int deviceNum) {
        return getReq(GET_WIFI_LIST, new byte[]{(byte)deviceNum});
    }
    public static byte[] setWifiConfig(byte[] config) {
        return getReq(SET_WIFI_CONFIG, config);
    }
    public static byte[] getWifiConfig(int option) {
        return getReq(GET_WIFI_CONFIG, new byte[]{(byte)option});
    }
    public static byte[] switchWifi4g(boolean on) {
        int state = 0;
        if (on) {
            state = 1;
        }
        return getReq(SWITCH_WIFI_4G, new byte[]{(byte)state});
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[8+len];

        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) sendCmd;
        cmd[2] = (byte) ~sendCmd;
        cmd[3] = (byte) TYPE_NORMAL_SEND;
        cmd[4] = (byte) seqNo;
        // length
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len>>8);

        System.arraycopy(data, 0, cmd, 7, data.length);
        cmd[7+len] = CrcUtil.calCRC8(cmd);

        addNo();

        return cmd;
    }

}
