package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.PC80TimeData;
import com.lepu.blepro.utils.CrcUtil;

import java.util.Date;

/**
 * universal command for Viatom devices
 */
public class Pc80BleCmd {

    public final static int SCP_ECG_LENGTH = (2+4+76+32+30+28+9024+88+512);

    public final static int GET_INFO = 0x11;
    public final static int TIME_SET = 0x33;
    public final static int TRANS_SET = 0x55;
    public final static int VERSION_SET = 0x66;
    public final static int GET_RATE = 0x77;
    public final static int DATA_MESS = 0xAA;
    public final static int TRACK_DATA_MESS = 0xDD;
    public final static int HEARTBEAT = 0xFF;

    public final static int ACK = 0x00;
    public final static int NAK = 0x01;
    public final static int REJ = 0x02;

    // 用于查询下位机的版本信息
    public static byte[] getInfo() {
        int versionLen = 6;
        int cmdLength = 4 + versionLen;
        byte[] cmd = new byte[cmdLength];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_INFO;
        cmd[2] = (byte) (6 & 0xFF);
        cmd[cmdLength - 1] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 用于下位机同步时间请求，上位机发送时间
    public static byte[] setTime() {
        PC80TimeData timeData = new PC80TimeData(new Date());
        byte[] data = timeData.convert2Data();
        int cmdLength = 4 + data.length;
        byte[] cmd = new byte[cmdLength];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) TIME_SET;
        cmd[2] = (byte) (data.length & 0xFF);
        for(int i = 0; i < data.length; i++) {
            cmd[i + 3] = data[i];
        }
        cmd[cmdLength - 1] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 查询信息(用于查询采样率等参数)
    public static byte[] getRate() {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_RATE;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) 0x01;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 用于建立数据传输会话应答
    // ACK 0x00 接收正确
    // NAK 0x01 接收有误
    // REJ 0x02 拒绝接收
    public static byte[] responseTransSet(int res) {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) TRANS_SET;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) res;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    //传输协议版本设置
    public static byte[] versionSet(int res) {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) VERSION_SET;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) res;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 用于数据传输应答
    // ACK 0x00 接收正确
    // NAK 0x01 接收有误
    // REJ 0x02 拒绝接收
    public static byte[] responseDataMess(int seq, int res) {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) DATA_MESS;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) seq;
        cmd[4] = (byte) res;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 用于上位机判断下位机的连接状态，每秒1包
    public static byte[] sendHeartbeat() {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) HEARTBEAT;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) 0x00;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

}
