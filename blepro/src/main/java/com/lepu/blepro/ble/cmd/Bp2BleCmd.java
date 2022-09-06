package com.lepu.blepro.ble.cmd;

import java.util.Calendar;

/**
 * @author chenyongfeng
 */
public class Bp2BleCmd {
    public static final int MSG_TYPE_INVALID = -1;
    private static int seqNo = 0;

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;
    public static final int SET_TIME = 0xEC;
    public static final int GET_INFO = 0xE1;
    public static final int GET_FILE_LIST = 0xF1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int FACTORY_RESET_ALL = 0xEE;

    public static final int RT_DATA = 0x08;
    public static final int RT_STATE = 0x06;
    public static final int SWITCH_STATE = 0x09;
    public static final int GET_PHY_STATE = 0x0E;
    public static final int SET_PHY_STATE = 0x0F;

    public static final int SET_CONFIG = 0x0B;
    public static final int GET_CONFIG = 0x00;

    public static final int FILE_READ_START = 0xF2;
    public static final int FILE_READ_PKG = 0xF3;
    public static final int FILE_READ_END = 0xF4;

    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    public static class SwitchState {
        public static final int ENTER_BP = 0;
        public static final int ENTER_ECG = 1;
        public static final int ENTER_HISTORY = 2;
        public static final int ENTER_ON = 3;
        public static final int ENTER_OFF = 4;
        public static final int ENTER_PHY = 5;
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
        byte[] cmd = new byte[40];
        if(switchState) {
            cmd[24] = (byte) 0x01;
        }
        cmd[26] = (byte) volume;
        return getReq(SET_CONFIG, cmd);
    }

    public static byte[] setConfig(byte[] data) {
        return getReq(SET_CONFIG, data);
    }

    public static byte[] setTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        byte[] cmd = new byte[7];
        cmd[0] = (byte) (c.get(Calendar.YEAR));
        cmd[1] = (byte) (c.get(Calendar.YEAR) >> 8);
        cmd[2] = (byte) (c.get(Calendar.MONTH) + 1);
        cmd[3] = (byte) (c.get(Calendar.DATE));
        cmd[4] = (byte) (c.get(Calendar.HOUR_OF_DAY));
        cmd[5] = (byte) (c.get(Calendar.MINUTE));
        cmd[6] = (byte) (c.get(Calendar.SECOND));
        return getReq(SET_TIME, cmd);
    }
    //文件下载开始
    public static byte[] getFileStart(byte[] fileName,byte offset){
        byte[] cmd = new byte[20];
        System.arraycopy(fileName, 0, cmd, 0, fileName.length);
        cmd[16] = (byte) offset;
        cmd[17] = (byte) (offset >> 8);
        cmd[18] = (byte) (offset >> 16);
        cmd[19] = (byte) (offset >> 24);
        return getReq(FILE_READ_START, cmd);
    }
    //文件下载结束
    public static byte[] fileReadEnd() {
        return getReq(FILE_READ_END, new byte[0]);
    }
    //文件下载中途
    public static byte[] fileReadPkg(int addrOffset) {
        byte[] cmd = new byte[4];
        cmd[0] = (byte) addrOffset;
        cmd[1] = (byte) (addrOffset >> 8);
        cmd[2] = (byte) (addrOffset >> 16);
        cmd[3] = (byte) (addrOffset >> 24);
        return getReq(FILE_READ_PKG, cmd);
    }
    public static byte[] getFileList() {
        return getReq(GET_FILE_LIST, new byte[0]);
    }
    public static byte[] getRtState() {
        return getReq(RT_STATE, new byte[0]);
    }
    public static byte[] getPhyState() {
        return getReq(GET_PHY_STATE, new byte[0]);
    }
    public static byte[] setPhyState(byte[] data) {
        return getReq(SET_PHY_STATE, data);
    }
    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }
}
