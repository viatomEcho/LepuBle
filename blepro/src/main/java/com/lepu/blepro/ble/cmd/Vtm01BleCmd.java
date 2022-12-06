package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class Vtm01BleCmd {
    private static int seqNo = 0;

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;
    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;

    public static final int GET_CONFIG = 0x00;
    public static final int RT_PARAM = 0x01;
    public static final int RT_DATA = 0x02;
    public static final int GET_ORIGINAL_DATA = 0x03;
    public static final int SLEEP_MODE_ON = 0x04;
    public static final int SLEEP_MODE_OFF = 0x05;

    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) sendCmd;
        cmd[2] = (byte) ~sendCmd;
        cmd[3] = (byte) TYPE_NORMAL_SEND;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len << 8);

        System.arraycopy(data, 0, cmd, 7, len);

        cmd[cmd.length - 1] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        return getReq(GET_INFO, new byte[0]);
    }
    public static byte[] getConfig() {
        return getReq(GET_CONFIG, new byte[0]);
    }

    public static byte[] reset() {
        return getReq(RESET, new byte[0]);
    }
    public static byte[] factoryReset() {
        return getReq(FACTORY_RESET, new byte[0]);
    }

    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }
    public static byte[] getRtParam() {
        return getReq(RT_PARAM, new byte[0]);
    }
    public static byte[] getOriginalData() {
        return getReq(GET_ORIGINAL_DATA, new byte[]{0x03});
    }

    public static byte[] sleepModeOn() {
        return getReq(SLEEP_MODE_ON, new byte[0]);
    }
    public static byte[] sleepModeOff() {
        return getReq(SLEEP_MODE_OFF, new byte[0]);
    }
}
