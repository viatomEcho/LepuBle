package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class R20BleCmd {

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;
    public static final int ECHO = 0xE0;

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

    public static byte[] echo(byte[] data) {
        return getReq(ECHO, data);
    }

}
