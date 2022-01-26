package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Calendar;

/**
 * @author chenyongfeng
 */
public class Bp2wBleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    public static final int ECHO = 0xE0;
    public static final int GET_BATTERY = 0xE4;
    public static final int FW_UPDATE_START = 0xE5;
    public static final int FW_UPDATE_CONTENT = 0xE6;
    public static final int FW_UPDATE_END = 0xE7;
    public static final int BURN_FACTORY_INFO = 0xEA;
    public static final int BURN_LOCK_FLASH = 0xEB;
    public static final int GET_TEMPERATURE = 0xED;
    public static final int WRITE_FILE_START = 0xF5;
    public static final int WRITE_FILE_DATA = 0xF6;
    public static final int WRITE_FILE_END = 0xF7;
    public static final int DELETE_FILE = 0xF8;
    public static final int GET_USER_LIST = 0xF9;
    public static final int DFU_UPDATE_MODE = 0xFA;

    public static final int GET_CONFIG = 0x00;
    public static final int SET_CONFIG = 0x0B;
    public static final int SET_TIME = 0xEC;
    public static final int GET_INFO = 0xE1;
    public static final int GET_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int FACTORY_RESET_ALL = 0xEE;
    /**
     * data = state + wave
     */
    public static final int RT_DATA = 0x08;
    public static final int RT_STATE = 0x06;

    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    public static final int SWITCH_STATE = 0x09;

    public static final int GET_PHY_STATUS = 0x0E;
    public static final int SET_PHY_STATUS = 0x0F;
    public static final int SET_TIMING_MEASURE = 0x10;
    public static final int GET_WIFI_ROUTE = 0x11;
    public static final int SET_WIFI_CONFIG = 0x12;
    public static final int GET_WIFI_CONFIG = 0x13;
    public static final int GET_USER_LIST_CRC = 0x20;
    public static final int GET_ECG_LIST_CRC = 0x21;
    public static final int GET_BP_LIST_CRC = 0x22;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static class SwitchState {
        public static final int ENTER_BP = 0;
        public static final int ENTER_ECG = 1;
        public static final int ENTER_HISTORY = 2;
        public static final int ENTER_ON = 3;
        public static final int ENTER_OFF = 4;
        public static final int ENTER_PHYSIOTHERAPY = 5;
    }

    public static byte[] switchState(int state) {
        return getReq(SWITCH_STATE, new byte[]{(byte)state});
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

    public static byte[] factoryResetAll() {
        return getReq(FACTORY_RESET_ALL, new byte[0]);
    }

    public static byte[] setConfig(boolean switchState, int volume) {
        int len = 40;
        byte[] data = new byte[len];
        if(switchState) {
            data[24] = 1;
        }
        data[26] = (byte) volume;
        return getReq(SET_CONFIG, data);
    }

    public static byte[] setTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int len = 7;
        byte[] data = new byte[len];
        data[0] = (byte) (c.get(Calendar.YEAR));
        data[1] = (byte) (c.get(Calendar.YEAR) >> 8);
        data[2] = (byte) (c.get(Calendar.MONTH) + 1);
        data[3] = (byte) (c.get(Calendar.DATE));
        data[4] = (byte) (c.get(Calendar.HOUR_OF_DAY));
        data[5] = (byte) (c.get(Calendar.MINUTE));
        data[6] = (byte) (c.get(Calendar.SECOND));

        LepuBleLog.d("setTime===");
        return getReq(SET_TIME, data);
    }

    //文件下载开始
    public static byte[] readFileStart(byte[] fileName,byte offset){
        // filename = 16, offset = 4
        int len = 20;

        byte[] data = new byte[len];
        // file name > 16 : cut
        int l = fileName.length > 20 ? 20 : fileName.length;

        System.arraycopy(fileName, 0, data, 0, l);

        data[len-4] = (byte) (offset >> 24);
        data[len-3] = (byte) (offset >> 16);
        data[len-2] = (byte) (offset >> 8);
        data[len-1] = (byte) offset;
        return getReq(READ_FILE_START, data);
    }
    //文件下载结束
    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
    }
    //文件下载中途
    public static byte[] readFileData(int addrOffset) {
        int len = 4;
        byte[] data = new byte[len];
        data[0] = (byte) addrOffset;
        data[1] = (byte) (addrOffset >> 8);
        data[2] = (byte) (addrOffset >> 16);
        data[3] = (byte) (addrOffset >> 24);
        return getReq(READ_FILE_DATA, data);
    }

    public static byte[] getFileList() {
        return getReq(GET_FILE_LIST, new byte[0]);
    }
    public static byte[] getFileListCrc(int cmd) {
        return getReq(cmd, new byte[0]);
    }

    public static byte[] getRtBpState() {
        return getReq(RT_STATE, new byte[0]);
    }
    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }

    public static byte[] getPhyStatus() {
        return getReq(GET_PHY_STATUS, new byte[0]);
    }

    public static byte[] setPhyStatus(int mode, int intensity, int time) {
        int len = 12;
        byte[] data = new byte[len];
        data[0] = (byte) mode;
        data[1] = (byte) intensity;
        data[2] = (byte) (time & 0xFF);
        data[3] = (byte) ((time >> 8) & 0xFF);
        return getReq(SET_PHY_STATUS, data);
    }

    public static byte[] setTimingMeasure(int period, int interval, boolean autoContinue, boolean autoDisplay) {
        int len = 12;
        byte[] data = new byte[len];
        data[0] = (byte) (period & 0xFF);
        data[1] = (byte) ((period >> 8) & 0xFF);
        data[2] = (byte) (interval & 0xFF);
        data[3] = (byte) ((interval >> 8) & 0xFF);
        data[4] = (byte) (autoContinue?1:0);
        data[5] = (byte) (autoDisplay?1:0);
        return getReq(SET_TIMING_MEASURE, data);
    }

    public static byte[] getWifiRoute(int deviceNum) {
        return getReq(GET_WIFI_ROUTE, new byte[]{(byte)deviceNum});
    }

    public static byte[] setWifiConfig(byte[] config) {
        return getReq(SET_WIFI_CONFIG, config);
    }
    public static byte[] getWifiConfig() {
        return getReq(GET_WIFI_CONFIG, new byte[]{0});
    }

    /**
     * 写文件开始
     */
    public static byte[] writeFileStart(byte[] fileName, int offset, int fileSize) {
        // filename = fileName.length, offset = 4, fileSize = 4
        int len = fileName.length + 8;

        byte[] data = new byte[len];
        System.arraycopy(fileName, 0, data, 0, fileName.length);

        data[len-8] = (byte) (offset >> 24);
        data[len-7] = (byte) (offset >> 16);
        data[len-6] = (byte) (offset >> 8);
        data[len-5] = (byte) offset;

        data[len-4] = (byte) (fileSize >> 24);
        data[len-3] = (byte) (fileSize >> 16);
        data[len-2] = (byte) (fileSize >> 8);
        data[len-1] = (byte) fileSize;

        return getReq(WRITE_FILE_START, data);
    }

    /**
     * 写文件内容
     */
    public static byte[] writeFileData(byte[] data) {
        return getReq(WRITE_FILE_DATA, data);
    }

    /**
     * 写文件结束
     */
    public static byte[] writeFileEnd() {
        return getReq(WRITE_FILE_END, new byte[0]);
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
