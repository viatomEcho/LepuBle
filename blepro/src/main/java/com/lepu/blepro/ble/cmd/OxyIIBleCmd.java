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
    public static int GET_FILE_LIST = 0x06;
    public static int READ_FILE_START = 0x07;
    public static int READ_FILE_DATA = 0x08;
    public static int READ_FILE_END = 0x09;

    public static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static class FileType {
        public static final int OXY = 0;
        public static final int PPG = 1;
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

    public static byte[] getFileList() {
        return getReq(GET_FILE_LIST, new byte[0]);
    }
    public static byte[] readFileStart(byte[] fileName, byte offset) {
        // filename = 16, offset = 4
        int len = 20;

        byte[] data = new byte[len];
        int l = Math.min(fileName.length, 16);
        System.arraycopy(fileName, 0, data, 0, l);

        data[len-4] = offset;
        data[len-3] = (byte) (offset >> 8);
        data[len-2] = (byte) (offset >> 16);
        data[len-1] = (byte) (offset >> 24);
        return getReq(READ_FILE_START, data);
    }
    public static byte[] readFileData(int addrOffset) {
        int len = 4;
        byte[] data = new byte[len];
        data[0] = (byte) addrOffset;
        data[1] = (byte) (addrOffset >> 8);
        data[2] = (byte) (addrOffset >> 16);
        data[3] = (byte) (addrOffset >> 24);
        return getReq(READ_FILE_DATA, data);
    }
    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
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
