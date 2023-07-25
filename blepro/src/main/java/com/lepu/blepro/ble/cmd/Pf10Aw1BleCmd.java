package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class Pf10Aw1BleCmd {

    private static final int TYPE_NORMAL_SEND = 0x00;
    public static final int TYPE_NORMAL_RECEIVE = 0x01;
    private static final int HEAD = 0xA5;
    public static final int GET_CONFIG = 0x00;
    public static final int SET_CONFIG = 0x01;
    public static final int ENABLE_PARAM = 0x02;
    public static final int ENABLE_WAVE = 0x03;
    public static final int RT_PARAM = 0x04;
    public static final int RT_WAVE = 0x05;
    public static final int WORK_STATUS_DATA = 0x06;

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
        public static final int SPO2_LOW = 1;      // 血氧阈值提醒
        public static final int PR_HIGH = 2;       // 脉率高阈值
        public static final int PR_LOW = 3;        // 脉率低阈值
        public static final int ALARM_SWITCH = 4;  // 阈值提醒开关
        public static final int MEASURE_MODE = 5;  // 测量模式
        public static final int BEEP_SWITCH = 6;   // 蜂鸣器开关
        public static final int LANGUAGE = 7;      // 语言包
        public static final int BLE_SWITCH = 8;    // 蓝牙开关
        public static final int ES_MODE = 9;       // 定时熄屏
    }

    /**
     * 0：使能血氧参数
     * 1：使能血氧波形
     */
    public static class EnableType {
        public static final int OXY_PARAM = 0;
        public static final int OXY_WAVE = 1;
    }

    /**
     * 使能发送开关
     * @param type EnableType
     * @param enable boolean
     * @return byte数组
     */
    public static byte[] enableSwitch(int type, boolean enable) {
        if (enable) {
            if (type == EnableType.OXY_PARAM) {
                return getReq(ENABLE_PARAM, new byte[]{1});
            } else {
                return getReq(ENABLE_WAVE, new byte[]{1});
            }
        } else {
            if (type == EnableType.OXY_PARAM) {
                return getReq(ENABLE_PARAM, new byte[]{0});
            } else {
                return getReq(ENABLE_WAVE, new byte[]{0});
            }
        }
    }

    public static byte[] setConfig(byte[] data) {
        return getReq(SET_CONFIG, data);
    }
    public static byte[] getConfig() {
        return getReq(GET_CONFIG, new byte[0]);
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
