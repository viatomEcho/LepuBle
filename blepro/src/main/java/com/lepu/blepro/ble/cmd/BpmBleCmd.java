package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.ByteArrayKt;
import java.util.Calendar;
import java.util.Map;

/**
 * @author wujuan
 */
public class BpmBleCmd {
    public static final int MSG_TYPE_INVALID = -1;

    public static byte[] getCmd(int msgType) {
        return BPMCmd.getCmd(msgType);
    }

    public static byte[] getCmd(int msgType, Map<String, Object> map) {
        return BPMCmd.getCmd(msgType, map);
    }

    public static int getMsgType(byte[] response) {
        return BPMCmd.getMsgType(response);
    }

    public static class BPMCmd {
        public static final byte HEAD_0 = (byte) 0x02;
        public static final byte HEAD_1 = (byte) 0x40;
        public static final byte CMD_WRITE = (byte) 0xDC;
        public static final byte CMD_READ = (byte) 0xDD;

        public static final int MSG_TYPE_GET_INFO = 0x03;
        public static final int MSG_TYPE_SET_TIME = 0x04;
        public static final int MSG_TYPE_START_BP = 0x15;
        public static final int MSG_TYPE_STOP_BP = 0x16;
        public static final int MSG_TYPE_GET_RECORDS = 0x17; // 获取历史记录列表
        public static final int MSG_TYPE_GET_BP_STATE = 0x18;
        public static final int MSG_TYPE_GET_BP_DATA = 0x19; // 实时广播血压的值
        public static final int MSG_TYPE_GET_RESULT = 0x1A; // 测量结束返回测量的结果

        public static final byte CMD_TYPE_START_BP = (byte) 0xA1;
        public static final byte CMD_TYPE_STOP = (byte) 0xA2;
        public static final byte CMD_TYPE_GET_INFO = (byte) 0xAB;
        public static final byte CMD_TYPE_SET_TIME = (byte) 0xB0;
        public static final byte CMD_TYPE_GET_RECORDS = (byte) 0xB1;
        public static final byte CMD_TYPE_GET_RECORDS_N = (byte) 0xB3;
        public static final byte CMD_TYPE_GET_BP_STATE = (byte) 0xB2;
        public static final byte CMD_TYPE_GET_BP_DATA = (byte) 0x00;
        public static final byte CMD_TYPE_GET_RESULT = (byte) 0x1C;

        public static byte[] getCmd(int msgType) {
            switch (msgType) {
                case BPMCmd.MSG_TYPE_GET_INFO:
                    return getInfo();
                case BPMCmd.MSG_TYPE_START_BP:
                    return startBp();
                case BPMCmd.MSG_TYPE_STOP_BP:
                    return stopBp();
                case BPMCmd.MSG_TYPE_SET_TIME:
                    return setTime();
                case BPMCmd.MSG_TYPE_GET_RECORDS:
                    return getRecords();
                case BPMCmd.MSG_TYPE_GET_BP_STATE:
                    return getBpState();
                default:
                    return new byte[0];
            }
        }

        public static byte[] getCmd(int msgType, Map<String, Object> map) {
            if (msgType == BPMCmd.MSG_TYPE_GET_RECORDS) {
                int storeIdA = (int) map.get("storeIdA");
                int storeIdB = (int) map.get("storeIdB");
                return getRecords(storeIdA, storeIdB);
            }
            return new byte[0];
        }

        public static int getMsgType(byte[] response) {
            switch(response[4]) {
                case CMD_TYPE_GET_INFO:
                    return MSG_TYPE_GET_INFO;
                case CMD_TYPE_SET_TIME:
                    return MSG_TYPE_SET_TIME;
                case CMD_TYPE_GET_BP_STATE:
                    return MSG_TYPE_GET_BP_STATE;
                case CMD_TYPE_GET_BP_DATA:
                    return MSG_TYPE_GET_BP_DATA;
                case CMD_TYPE_GET_RESULT:
                    return MSG_TYPE_GET_RESULT;
                case CMD_TYPE_GET_RECORDS:
                case CMD_TYPE_GET_RECORDS_N:
                    return MSG_TYPE_GET_RECORDS;
                default:
                    return MSG_TYPE_INVALID;
            }
        }

        public static byte[] getInfo() {
            byte[] cmd = new byte[6];

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x01; // length
            cmd[4] = CMD_TYPE_GET_INFO; // cmd value
            cmd[5] = calcNum(cmd);
            return cmd;
        }

        public static byte[] startBp() {
            byte[] cmd = new byte[6];

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x01; // length
            cmd[4] = CMD_TYPE_START_BP; // cmd value
            cmd[5] = calcNum(cmd);
            return cmd;
        }

        public static byte[] stopBp() {
            byte[] cmd = new byte[6];

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x01; // length
            cmd[4] = CMD_TYPE_STOP; // cmd value
            cmd[5] = calcNum(cmd);
            return cmd;
        }

        public static byte[] setTime() {
            byte[] cmd = new byte[12];

            Calendar calendar = Calendar.getInstance();
            byte year = (byte) (calendar.get(Calendar.YEAR) - 2000);
            byte month = (byte) (calendar.get(Calendar.MONTH) + 1);
            byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            byte hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            byte minute = (byte) calendar.get(Calendar.MINUTE);
            byte second = (byte) calendar.get(Calendar.SECOND);

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x07; // length
            cmd[4] = CMD_TYPE_SET_TIME; // cmd value
            cmd[5] = year; // cmd value
            cmd[6] = month; // cmd value
            cmd[7] = day; // cmd value
            cmd[8] = hour; // cmd value
            cmd[9] = minute; // cmd value
            cmd[10] = second; // cmd value
            cmd[11] = calcNum(cmd);
            return cmd;
        }

        public static byte[] getRecords() {
            byte[] cmd = new byte[10];

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x05; // length
            cmd[4] = CMD_TYPE_GET_RECORDS; // cmd value
            cmd[5] = 0x00; // cmd value
            cmd[6] = 0x00; // cmd value
            cmd[7] = 0x00; // cmd value
            cmd[8] = 0x00; // cmd value
            cmd[9] = calcNum(cmd);
            return cmd;
        }

        public static byte[] getRecords(int storeIdA, int storeIdB) {
            byte[] cmd = new byte[10];

            byte[] storeIdArray4A = ByteArrayKt.shortToByteArray(storeIdA);
            byte[] storeIdArray4B = ByteArrayKt.shortToByteArray(storeIdB);

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x05; // length
            cmd[4] = CMD_TYPE_GET_RECORDS; // cmd value

            cmd[5] = storeIdArray4A[0]; // cmd value
            cmd[6] = storeIdArray4A[1]; // cmd value
            cmd[7] = storeIdArray4B[0]; // cmd value
            cmd[8] = storeIdArray4B[1];  // cmd value

            cmd[9] = calcNum(cmd);
            return cmd;
        }

        public static byte[] getBpState() {
            byte[] cmd = new byte[6];

            cmd[0] = HEAD_0;
            cmd[1] = HEAD_1;
            cmd[2] = CMD_WRITE;
            cmd[3] = (byte) 0x01; // length
            cmd[4] = CMD_TYPE_GET_BP_STATE; // cmd value
            cmd[5] = calcNum(cmd);
            return cmd;
        }
    }

    public static byte calcNum(byte[] cmd) {
        int result = BPMCmd.HEAD_1;
        for(int i = 2; i < (cmd.length - 1); i++) {
            result = cmd[i] ^ result;
        }
        return (byte) (result);
    }
}
