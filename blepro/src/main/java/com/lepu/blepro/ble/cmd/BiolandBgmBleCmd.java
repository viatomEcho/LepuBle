package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;

import java.util.Date;

public class BiolandBgmBleCmd {

    public static final int HEAD = 0x5A;

    public static final int HAND_SHAKE = 0x09;

    public static final int GET_INFO = 0x00;
    public static final int MSG_ING = 0x02;
    public static final int GET_DATA = 0x03;
    public static final int MSG_END = 0x05;

    public static byte[] setTime() {
        return getReq(HAND_SHAKE, new byte[0]);
    }

    public static byte[] getInfo() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = new byte[6];
        data[0] = (byte) (timeData.getYear()-2000);
        data[1] = (byte) timeData.getMonth();
        data[2] = (byte) timeData.getDate();
        data[3] = (byte) timeData.getHour();
        data[4] = (byte) timeData.getMinute();
        data[5] = (byte) timeData.getSecond();
        return getReq(GET_INFO, data);
    }

    public static byte[] getData() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = new byte[6];
        data[0] = (byte) (timeData.getYear()-2000);
        data[1] = (byte) timeData.getMonth();
        data[2] = (byte) timeData.getDate();
        data[3] = (byte) timeData.getHour();
        data[4] = (byte) timeData.getMinute();
        data[5] = (byte) timeData.getSecond();
        return getReq(GET_DATA, data);
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[4+len];
        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) cmd.length;
        cmd[2] = (byte) sendCmd;

        System.arraycopy(data, 0, cmd, 3, data.length);

        cmd[cmd.length-1] = getLastByte(cmd);
        return cmd;
    }

    public static byte getLastByte(byte[] srcBytes) {
        byte tmpByte = 0;
        int i;
        for(i = 0; i < srcBytes.length-1; i++) {
            tmpByte = (byte)((tmpByte + srcBytes[i]) & 0xff);
        }
        return (byte)(tmpByte+2);
    }

}
