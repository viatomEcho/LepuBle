package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class OxyIIBleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;
    public static final int TYPE_NORMAL_RECEIVE = 0x01;
    public static int GET_CONFIG = 0x00;
    public static int SET_CONFIG = 0x01;
    public static int RT_PARAM = 0x02;
    public static int RT_WAVE = 0x03;
    public static int RT_DATA = 0x04;
    public static int RT_PPG = 0x05;

    public static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    /**
     * 设置
     */
    public static class ConfigType {
        public static final int ALL = 0;
        public static final int SPO2_SWITCH = 1;   // 血氧提醒开关
        public static final int SPO2_LOW = 2;      // 血氧阈值提醒
        public static final int HR_SWITCH = 3;     // 心率提醒开关
        public static final int HR_LOW = 4;        // 心率低阈值
        public static final int HR_HIGH = 5;       // 心率高阈值
        public static final int MOTOR = 6;         // 震动强度
        public static final int BUZZER = 7;        // 声音强度
        public static final int DISPLAY_MODE = 8;  // 显示模式
        public static final int BRIGHTNESS = 9;    // 屏幕亮度
        public static final int INTERVAL = 10;     // 存储间隔
    }

    public static byte[] setConfig(byte[] data) {
        return getReq(SET_CONFIG, data);
    }
    public static byte[] getConfig() {
        return getReq(GET_CONFIG, new byte[0]);
    }

    public static byte[] getRtParam() {
        return getReq(RT_PARAM, new byte[0]);
    }
    public static byte[] getRtWave() {
        return getReq(RT_WAVE, new byte[0]);
    }
    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[]{125});
    }

    public static byte[] getRtPpg() {
        return getReq(RT_PPG, new byte[]{0x07, 0x00});
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

        addNo();
        return cmd;
    }


}
