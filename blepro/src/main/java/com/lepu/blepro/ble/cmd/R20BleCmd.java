package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class R20BleCmd {

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;

    public static final int DEVICE_BOUND = 0x01;
    public static final int DEVICE_UNBOUND = 0x02;
    public static final int SET_USER_INFO = 0x03;
    public static final int GET_USER_INFO = 0x04;
    public static final int INTO_DOCTOR_MODE = 0x05;
    public static final int GET_WIFI_LIST = 0x11;
    public static final int SET_WIFI_CONFIG = 0x12;
    public static final int GET_WIFI_CONFIG = 0x13;
    public static final int GET_VERSION_INFO = 0x15;
    public static final int GET_SYSTEM_SETTING = 0x16;
    public static final int SET_SYSTEM_SETTING = 0x17;
    public static final int GET_MEASURE_SETTING = 0x18;
    public static final int SET_MEASURE_SETTING = 0x19;
    public static final int MASK_TEST = 0x1A;
    public static final int GET_VENTILATION_SETTING = 0x20;
    public static final int SET_VENTILATION_SETTING = 0x21;
    public static final int GET_WARNING_SETTING = 0x22;
    public static final int SET_WARNING_SETTING = 0x23;
    public static final int VENTILATION_SWITCH = 0x24;
    public static final int GET_FILE_LIST = 0x31;
    public static final int READ_FILE_START = 0x32;
    public static final int READ_FILE_DATA = 0x33;
    public static final int READ_FILE_END = 0x34;

    public static int seqNo = 0;
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

        addNo();
        return cmd;
    }

    public static byte[] deviceBound(boolean bound) {
        if (bound) {
            return getReq(DEVICE_BOUND, new byte[0]);
        } else {
            return getReq(DEVICE_UNBOUND, new byte[0]);
        }
    }
    public static byte[] setUserInfo(byte[] data) {
        return getReq(SET_USER_INFO, data);
    }
    public static byte[] getUserInfo() {
        return getReq(GET_USER_INFO, new byte[0]);
    }
    public static byte[] intoDoctorMode(byte[] pin, long timestamp) {
        byte[] data = new byte[10];
        System.arraycopy(pin, 0, data, 0, pin.length);
        data[6] = (byte) timestamp;
        data[7] = (byte) (timestamp >> 8);
        data[8] = (byte) (timestamp >> 16);
        data[9] = (byte) (timestamp >> 24);
        return getReq(INTO_DOCTOR_MODE, data);
    }
    public static byte[] getWifiList() {
        return getReq(GET_WIFI_LIST, new byte[0]);
    }
    public static byte[] setWifiConfig(byte[] config) {
        return getReq(SET_WIFI_CONFIG, config);
    }
    public static byte[] getWifiConfig() {
        return getReq(GET_WIFI_CONFIG, new byte[0]);
    }
    public static byte[] getVersionInfo() {
        return getReq(GET_VERSION_INFO, new byte[0]);
    }
    public static byte[] getSystemSetting() {
        return getReq(GET_SYSTEM_SETTING, new byte[0]);
    }
    public static byte[] setSystemSetting(byte[] data) {
        return getReq(SET_SYSTEM_SETTING, data);
    }
    public static byte[] getMeasureSetting() {
        return getReq(GET_MEASURE_SETTING, new byte[0]);
    }
    public static byte[] setMeasureSetting(byte[] data) {
        return getReq(SET_MEASURE_SETTING, data);
    }
    public static byte[] maskTest(boolean start) {
        int temp = 0;
        if (start) {
            temp = 1;
        }
        return getReq(MASK_TEST, new byte[]{(byte)temp,0,0,0});
    }
    public static byte[] getVentilationSetting() {
        return getReq(GET_VENTILATION_SETTING, new byte[0]);
    }
    public static byte[] setVentilationSetting(byte[] data) {
        return getReq(SET_VENTILATION_SETTING, data);
    }
    public static byte[] getWarningSetting() {
        return getReq(GET_WARNING_SETTING, new byte[0]);
    }
    public static byte[] setWarningSetting(byte[] data) {
        return getReq(SET_WARNING_SETTING, data);
    }
    public static byte[] ventilationSwitch(boolean start) {
        int temp = 0;
        if (start) {
            temp = 1;
        }
        return getReq(VENTILATION_SWITCH, new byte[]{(byte)temp,0,0,0});
    }
    public static byte[] getFileList(long startTime, int recordType) {
        byte[] data = new byte[10];
        data[0] = (byte) startTime;
        data[1] = (byte) (startTime >> 8);
        data[2] = (byte) (startTime >> 16);
        data[3] = (byte) (startTime >> 24);
        data[4] = (byte) recordType;
        return getReq(GET_FILE_LIST, data);
    }
    public static byte[] readFileStart(byte[] fileName, int offset) {
        byte[] data = new byte[36];
        System.arraycopy(fileName, 0, data, 0, fileName.length);
        data[32] = (byte) offset;
        data[33] = (byte) (offset >> 8);
        data[34] = (byte) (offset >> 16);
        data[35] = (byte) (offset >> 24);
        return getReq(READ_FILE_START, data);
    }
    public static byte[] readFileData(int offset) {
        byte[] data = new byte[4];
        data[0] = (byte) offset;
        data[1] = (byte) (offset >> 8);
        data[2] = (byte) (offset >> 16);
        data[3] = (byte) (offset >> 24);
        return getReq(READ_FILE_DATA, data);
    }
    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
    }
}
