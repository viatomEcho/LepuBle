package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;

import java.util.Date;

/**
 * @author chenyongfeng
 */
public class Lew3BleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    public static final int ECHO = 0xE0;
    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int GET_BATTERY = 0xE4;
    public static final int FW_UPDATE_START = 0xE5;
    public static final int FW_UPDATE_DATA = 0xE6;
    public static final int FW_UPDATE_END = 0xE7;
    public static final int BURN_FACTORY_INFO = 0xEA;
    public static final int SET_TIME = 0xEC;
    public static final int FACTORY_RESET_ALL = 0xEE;
    public static final int GET_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int DELETE_FILE = 0xF8;

    public static final int GET_CONFIG = 0x00;
    public static final int SYSTEM_SETTINGS = 0x01;
    public static final int SET_SERVER = 0x02;
    public static final int BOUND_DEVICE = 0x03;
    public static final int RT_DATA = 0x11;


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

    public static byte[] getBattery() {
        return getReq(GET_BATTERY, new byte[0]);
    }

    public static byte[] getDeviceInfo() {
        return getReq(GET_INFO, new byte[0]);
    }

    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        return getReq(SET_TIME, timeData.convert2Data());
    }

    public static byte[] listFiles() {
        return getReq(GET_FILE_LIST, new byte[0]);
    }

    public static byte[] deleteFile(byte[] fileName) {
        return getReq(DELETE_FILE, fileName);
    }

    public static byte[] factoryReset() {
        return getReq(FACTORY_RESET, new byte[0]);
    }

    public static byte[] factoryResetAll() {
        return getReq(FACTORY_RESET_ALL, new byte[0]);
    }

    public static byte[] reset() {
        return getReq(RESET, new byte[0]);
    }

    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }

    public static byte[] boundDevice() {
        return getReq(BOUND_DEVICE, new byte[0]);
    }

    public static byte[] readFileStart(byte[] fileName, int offset) {
        byte[] data = new byte[fileName.length + 4];

        System.arraycopy(fileName, 0, data, 0, fileName.length);

        data[data.length - 4] = (byte) (offset & 0xFF);
        data[data.length - 3] = (byte) ((offset >> 8) & 0xFF);
        data[data.length - 2] = (byte) ((offset >> 16) & 0xFF);
        data[data.length - 1] = (byte) ((offset >> 24) & 0xFF);
        return getReq(READ_FILE_START, data);
    }

    public static byte[] readFileData(int offset) {
        byte[] offsetData = new byte[4];
        offsetData[0] = (byte) (offset & 0xFF);
        offsetData[1] = (byte) ((offset >> 8) & 0xFF);
        offsetData[2] = (byte) ((offset >> 16) & 0xFF);
        offsetData[3] = (byte) ((offset >> 24) & 0xFF);

        return getReq(READ_FILE_DATA, offsetData);
    }

    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
    }

    public static byte[] setServer(byte[] data) {
        return getReq(SET_SERVER, data);
    }
    public static byte[] systemSettings(boolean on) {
        if (on) {
            return getReq(SYSTEM_SETTINGS, new byte[]{0});
        } else {
            return getReq(SYSTEM_SETTINGS, new byte[]{1});
        }
    }

    public static byte[] fwUpdateStart(byte[] data) {
        return getReq(FW_UPDATE_START, data);
    }
    public static byte[] fwUpdateData(byte[] data) {
        return getReq(FW_UPDATE_DATA, data);
    }
    public static byte[] fwUpdateEnd() {
        return getReq(FW_UPDATE_END, new byte[0]);
    }

    public static byte[] burnFactoryInfo(byte[] data) {
        return getReq(BURN_FACTORY_INFO, data);
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

        cmd[cmd.length-1] = CrcUtil.calCRC8(cmd);

        addNo();

        return cmd;
    }
}
