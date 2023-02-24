package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Calendar;

public class LpBleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    public static final int ECHO = 0xE0;
    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int GET_BATTERY = 0xE4;
    public static final int FW_UPDATE_START = 0xE5;
    public static final int FW_UPDATE_CONTENT = 0xE6;
    public static final int FW_UPDATE_END = 0xE7;
    public static final int BURN_FACTORY_INFO = 0xEA;
    public static final int BURN_LOCK_FLASH = 0xEB;
    public static final int SET_TIME = 0xEC;
    public static final int GET_TEMPERATURE = 0xED;
    public static final int FACTORY_RESET_ALL = 0xEE;
    public static final int GET_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int WRITE_FILE_START = 0xF5;
    public static final int WRITE_FILE_DATA = 0xF6;
    public static final int WRITE_FILE_END = 0xF7;
    public static final int DELETE_FILE = 0xF8;
    public static final int GET_USER_LIST = 0xF9;
    public static final int DFU_UPDATE_MODE = 0xFA;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
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

    public static byte[] getInfo() {
        return getReq(GET_INFO, new byte[0]);
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

    public static byte[] burnFactoryInfo(byte[] config) {
        return getReq(BURN_FACTORY_INFO, config);
    }

    //文件下载开始
    public static byte[] readFileStart(byte[] fileName,byte offset){
        // filename = 16, offset = 4
        int len = 20;

        byte[] data = new byte[len];
        int l = Math.min(fileName.length, 16);
        System.arraycopy(fileName, 0, data, 0, l);

        data[len-4] = (byte) offset;
        data[len-3] = (byte) (offset >> 8);
        data[len-2] = (byte) (offset >> 16);
        data[len-1] = (byte) (offset >> 24);
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

    public static byte[] deleteFile() {
        return getReq(DELETE_FILE, new byte[]{0});
    }

    /**
     * 写文件开始
     */
    public static byte[] writeFileStart(byte[] fileName, int offset, int fileSize) {
        // filename = 16, offset = 4, fileSize = 4
        int len = 24;

        byte[] data = new byte[len];
        int l = Math.min(fileName.length, 16);
        System.arraycopy(fileName, 0, data, 0, l);

        data[len-8] = (byte) offset;
        data[len-7] = (byte) (offset >> 8);
        data[len-6] = (byte) (offset >> 16);
        data[len-5] = (byte) (offset >> 24);

        data[len-4] = (byte) fileSize;
        data[len-3] = (byte) (fileSize >> 8);
        data[len-2] = (byte) (fileSize >> 16);
        data[len-1] = (byte) (fileSize >> 24);

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
        cmd[cmd.length-1] = CrcUtil.calCRC8(cmd);

        addNo();

        return cmd;
    }

}
