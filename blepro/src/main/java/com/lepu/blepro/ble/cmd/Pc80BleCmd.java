package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.Pc80TimeData;
import com.lepu.blepro.utils.CrcUtil;
import java.util.Date;

/**
 * @author chenyongfeng
 */
public class Pc80BleCmd {

    public static final int SCP_ECG_LENGTH = (2+4+76+32+30+28+9024+88+512);

    public static final int GET_INFO = 0x11;
    public static final int TIME_SET = 0x33;
    public static final int TRANS_SET = 0x55;
    public static final int VERSION_SET = 0x66;
    public static final int GET_RATE = 0x77;
    // 传输文件
    public static final int DATA_MESS = 0xAA;
    // 实时数据
    public static final int TRACK_DATA_MESS = 0xDD;
    public static final int HEARTBEAT = 0xFF;

    // 接收正确
    public static final int ACK = 0x00;
    // 接收有误
    public static final int NAK = 0x01;
    // 拒绝接收
    public static final int REJ = 0x02;

    /**
     * 用于查询下位机的版本信息
     * @return byte数组
     */
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

    /**
     * 用于下位机同步时间请求，上位机发送时间
     * @return byte数组
     */
    public static byte[] setTime() {
        Pc80TimeData timeData = new Pc80TimeData(new Date());
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

    /**
     * 查询信息(用于查询采样率等参数)
     * @return byte数组
     */
    public static byte[] getRate() {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_RATE;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) 0x01;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    /**
     * 用于建立数据传输会话应答
     * @param res int
     * @return byte数组
     */
    public static byte[] responseTransSet(int res) {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) TRANS_SET;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) res;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    /**
     * 传输协议版本设置
     * @param res int
     * @return byte数组
     */
    public static byte[] versionSet(int res) {
        byte[] cmd = new byte[5];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) VERSION_SET;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) res;
        cmd[4] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    /**
     * 用于数据传输应答
     * @param seq int
     * @param res int
     * @return byte数组
     */
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

    /**
     * 用于上位机判断下位机的连接状态，每秒1包
     * @return byte数组
     */
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
