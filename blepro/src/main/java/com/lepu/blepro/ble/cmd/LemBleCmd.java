package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

/**
 * 护颈仪设备指令
 * @author chenyongfeng
 */
public class LemBleCmd {

    public static final int HEAD_0 = 0x55;
    public static final int HEAD_1 = 0xAA;

    public static final int DEVICE_SWITCH = 0x01;
    public static final int DEVICE_BATTERY = 0x02;
    public static final int HEAT_MODE = 0x03;
    public static final int MASSAGE_MODE = 0x04;
    public static final int MASSAGE_LEVEL = 0x05;
    public static final int MASSAGE_TIME = 0x06;

    public static final int CMD_DEVICE_STATE = 0x80;
    public static final int MSG_DEVICE_STATE = 0x81;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 9999) {
            seqNo = 0;
        }
    }

    public static byte[] deviceSwitch(boolean on) {
        if (on) {
            return getReq(DEVICE_SWITCH, new byte[]{1});
        } else {
            return getReq(DEVICE_SWITCH, new byte[]{0});
        }
    }
    public static byte[] getBattery() {
        return getReq(DEVICE_BATTERY, new byte[]{0});
    }
    public static byte[] heatMode(boolean on) {
        if (on) {
            return getReq(HEAT_MODE, new byte[]{1});
        } else {
            return getReq(HEAT_MODE, new byte[]{0});
        }
    }
    public static class MassageMode {
        public static final int VITALITY = 0;   // 活力模式
        public static final int DYNAMIC = 1;    // 动感模式
        public static final int HAMMERING = 2;  // 捶击模式
        public static final int SOOTHING = 3;   // 舒缓模式
        public static final int AUTOMATIC = 4;  // 自动模式
    }
    public static byte[] massageMode(int mode) {
        return getReq(MASSAGE_MODE, new byte[]{(byte)mode});
    }
    public static byte[] massageLevel(int level) {
        return getReq(MASSAGE_LEVEL, new byte[]{(byte)level});
    }
    public static class MassageTime {
        public static final int MIN_15 = 0;   // 活力模式
        public static final int MIN_10 = 1;   // 动感模式
        public static final int MIN_5 = 2;    // 捶击模式
    }
    public static byte[] massageTime(int time) {
        return getReq(MASSAGE_TIME, new byte[]{(byte)time});
    }
    public static byte[] getDeviceState() {
        return getReq(CMD_DEVICE_STATE, new byte[0]);
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[9+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[5] = (byte) sendCmd;
        cmd[6] = (byte) len;
        System.arraycopy(data, 0, cmd, 7, len);
        cmd[cmd.length-2] = CrcUtil.calCRC8(cmd);
        cmd[cmd.length-1] = (byte) 0xFE;
        return cmd;
    }

}
