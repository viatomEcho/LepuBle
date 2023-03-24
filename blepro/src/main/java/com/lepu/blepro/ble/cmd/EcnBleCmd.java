package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

public class EcnBleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    public static final int GET_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    //文件下载开始
    public static byte[] readFileStart(byte[] fileName,byte offset) {
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
