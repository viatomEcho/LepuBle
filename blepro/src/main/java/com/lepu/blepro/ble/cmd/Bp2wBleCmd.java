package com.lepu.blepro.ble.cmd;

import static com.lepu.blepro.ble.cmd.LpBleCmd.getReq;

import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Calendar;

/**
 * @author chenyongfeng
 */
public class Bp2wBleCmd {

    public static final int GET_CONFIG = 0x00;
    public static final int SET_CONFIG = 0x0B;
    /**
     * data = state + wave
     */
    public static final int RT_DATA = 0x08;
    public static final int RT_STATE = 0x06;

    public static final int SWITCH_STATE = 0x09;

    public static final int GET_PHY_STATUS = 0x0E;
    public static final int SET_PHY_STATUS = 0x0F;
    public static final int SET_TIMING_MEASURE = 0x10;
    public static final int GET_WIFI_ROUTE = 0x11;
    public static final int SET_WIFI_CONFIG = 0x12;
    public static final int GET_WIFI_CONFIG = 0x13;
    public static final int GET_USER_LIST_CRC = 0x30;
    public static final int GET_ECG_LIST_CRC = 0x31;
    public static final int GET_BP_LIST_CRC = 0x32;

    public static byte[] switchState(int state, byte[] key) {
        return getReq(SWITCH_STATE, new byte[]{(byte)state}, key);
    }

    public static byte[] getConfig(byte[] key) {
        return getReq(GET_CONFIG, new byte[0], key);
    }

    public static byte[] setConfig(byte[] data, byte[] key) {
        return getReq(SET_CONFIG, data, key);
    }

    public static byte[] getRtState(byte[] key) {
        return getReq(RT_STATE, new byte[0], key);
    }
    public static byte[] getRtData(byte[] key) {
        return getReq(RT_DATA, new byte[0], key);
    }

    public static byte[] getPhyStatus(byte[] key) {
        return getReq(GET_PHY_STATUS, new byte[0], key);
    }

    public static byte[] setPhyStatus(int mode, int intensity, int time, byte[] key) {
        int len = 12;
        byte[] data = new byte[len];
        data[0] = (byte) mode;
        data[1] = (byte) intensity;
        data[2] = (byte) (time & 0xFF);
        data[3] = (byte) ((time >> 8) & 0xFF);
        return getReq(SET_PHY_STATUS, data, key);
    }

    public static byte[] setTimingMeasure(int period, int interval, boolean autoContinue, boolean autoDisplay, byte[] key) {
        int len = 12;
        byte[] data = new byte[len];
        data[0] = (byte) (period & 0xFF);
        data[1] = (byte) ((period >> 8) & 0xFF);
        data[2] = (byte) (interval & 0xFF);
        data[3] = (byte) ((interval >> 8) & 0xFF);
        data[4] = (byte) (autoContinue?1:0);
        data[5] = (byte) (autoDisplay?1:0);
        return getReq(SET_TIMING_MEASURE, data, key);
    }

    public static byte[] getWifiRoute(int deviceNum, byte[] key) {
        return getReq(GET_WIFI_ROUTE, new byte[]{(byte)deviceNum}, key);
    }

    public static byte[] setWifiConfig(byte[] config, byte[] key) {
        return getReq(SET_WIFI_CONFIG, config, key);
    }
    public static byte[] getWifiConfig(byte[] key) {
        return getReq(GET_WIFI_CONFIG, new byte[]{0}, key);
    }

}
