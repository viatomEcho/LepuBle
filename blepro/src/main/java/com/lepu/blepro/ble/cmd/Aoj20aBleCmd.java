package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import java.util.Date;

/**
 * @author chenyongfeng
 */
public class Aoj20aBleCmd {

    public static final int HEAD = 0xAA;
    /**
     * 0x01 红外温度计
     */
    public static final int DEVICE_TYPE = 0x01;

    public static final int CMD_TEMP_MEASURE = 0xD1;
    public static final int MSG_TEMP_MEASURE = 0xC1;
    public static final int CMD_SET_TIME = 0xD2;
    public static final int MSG_SET_TIME = 0xC2;
    public static final int CMD_GET_HISTORY_DATA = 0xD3;
    public static final int MSG_GET_HISTORY_DATA = 0xC3;
    public static final int CMD_DELETE_HISTORY_DATA = 0xD4;
    public static final int MSG_DELETE_HISTORY_DATA = 0xC4;
    public static final int CMD_GET_DEVICE_DATA = 0xD5;
    public static final int MSG_GET_DEVICE_DATA = 0xC5;

    public static final int MSG_ERROR_CODE = 0xCE;

    /**
     * 时间数据同步指令
     * @return byte数组
     */
    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = new byte[5];
        data[0] = (byte) (timeData.getYear()-2000);
        data[1] = (byte) timeData.getMonth();
        data[2] = (byte) timeData.getDate();
        data[3] = (byte) timeData.getHour();
        data[4] = (byte) timeData.getMinute();
        return getReq(CMD_SET_TIME, data);
    }

    /**
     * 测温数据同步指令（单次开始测量指令）
     * @return byte数组
     */
    public static byte[] tempMeasure() {
        return getReq(CMD_TEMP_MEASURE, new byte[]{0,0,0});
    }

    /**
     * 设备状态查询指令
     * @return byte数组
     */
    public static byte[] getDeviceData() {
        return getReq(CMD_GET_DEVICE_DATA, new byte[0]);
    }

    /**
     * 历史数据同步指令（多次离线数据同步指令）
     * @return byte数组
     */
    public static byte[] getHistoryData() {
        return getReq(CMD_GET_HISTORY_DATA, new byte[0]);
    }

    /**
     * 擦除历史数据指令
     * @return
     */
    public static byte[] deleteHistoryData() {
        return getReq(CMD_DELETE_HISTORY_DATA, new byte[0]);
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) DEVICE_TYPE;
        cmd[2] = (byte) sendCmd;
        cmd[3] = (byte) len;

        System.arraycopy(data, 0, cmd, 4, data.length);

        cmd[cmd.length-1] = getLastByte(cmd);
        return cmd;
    }

    public static byte getLastByte(byte[] srcBytes) {
        byte tmpByte = srcBytes[1];
        int i;
        for(i = 2; i < srcBytes.length - 1; i++) {
            tmpByte = (byte)((tmpByte ^ srcBytes[i]) & 0xff);
        }
        return tmpByte;
    }

}
