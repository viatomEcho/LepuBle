package com.lepu.blepro.ble.cmd;


import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.ByteArrayKt;
import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.LepuBleLog;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Bp2BleCmd {
    public static final int MSG_TYPE_INVALID = -1;
    public final static int SET_TIME = 0xEC;
    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }
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
        private final static byte HEAD = (byte) 0xA5;
        public final static byte CMD_SET_TIME = (byte) 0xEC;
        private final static byte TYPE_NORMAL_SEND = (byte) 0x00;
        public final static byte CMD_FILE_LIST = (byte) 0xF1;
        // heartbeat sound
        public final static byte CMD_BP2_SET_SWITCHER_STATE = (byte) 0x0B;
        public final static byte CMD_INFO = (byte) 0xE1;
        public final static byte CMD_BP2_CONFIG = (byte) 0x00;
        private static int serial = 0;
        public final static byte CMD_WRITE = (byte) 0xDC;
        public final static byte CMD_READ = (byte) 0xDD;
        public final static int MSG_TYPE_GET_INFO = 0x03;
        public final static int MSG_TYPE_SET_TIME = 0x04;
        public final static int MSG_TYPE_START_BP = 0x15;
        public final static int MSG_TYPE_STOP_BP = 0x16;
        public final static int MSG_TYPE_GET_RECORDS = 0x17; // 获取历史记录列表
        public final static int MSG_TYPE_GET_BP_STATE = 0x18;
        public final static int MSG_TYPE_GET_BP_DATA = 0x19; // 实时广播血压的值
        public final static int MSG_TYPE_GET_RESULT = 0x1A; // 测量结束返回测量的结果

        public final static byte CMD_TYPE_START_BP = (byte) 0xA1;
        public final static byte CMD_TYPE_STOP = (byte) 0xA2;
        public final static byte CMD_TYPE_GET_INFO = (byte) 0xAB;
        public final static byte CMD_TYPE_SET_TIME = (byte) 0xB0;
        public final static byte CMD_TYPE_GET_RECORDS = (byte) 0xB1;
        public final static byte CMD_TYPE_GET_RECORDS_N = (byte) 0xB3;
        public final static byte CMD_TYPE_GET_BP_STATE = (byte) 0xB2;
        public final static byte CMD_TYPE_GET_BP_DATA = (byte) 0x00;
        public final static byte CMD_TYPE_GET_RESULT = (byte) 0x1C;

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
            switch (msgType) {
                case BPMCmd.MSG_TYPE_GET_RECORDS:
                {
                    int storeIdA = (int) map.get("storeIdA");
                    int storeIdB = (int) map.get("storeIdB");
                    return getRecords(storeIdA, storeIdB);
                }
                default:
                    return new byte[0];
            }
        }

        public static int getMsgType(byte[] response) {
            LepuBleLog.d("getMsgType===", response[4]+"");
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
            }
            return MSG_TYPE_INVALID;
        }

        public static byte[] getInfo() {
            int len = 0;

            byte[] cmd = new byte[8+len];

            cmd[0] = HEAD;
            cmd[1] = CMD_INFO;
            cmd[2] = ~CMD_INFO;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len>>8);
            cmd[7+len] = CrcUtil.calCRC8(cmd);

            serial++;

            return cmd;
        }
        public static byte[] getConfig() {
                int len = 0;

                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_BP2_CONFIG;
                cmd[2] = ~CMD_BP2_CONFIG;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;
                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);
                cmd[7+len] = CrcUtil.calCRC8(cmd);

                serial++;

                return cmd;


        }

        public static byte[] setConfig(boolean switchState) {
                int len = 40;

                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_BP2_SET_SWITCHER_STATE;
                cmd[2] = ~CMD_BP2_SET_SWITCHER_STATE;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;

                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);

                if(switchState) {
                    cmd[31] = 1;
                }

                cmd[7+len] = CrcUtil.calCRC8(cmd);

                serial++;

                return cmd;

        }
        public static byte[] startBp() {
            byte[] cmd = new byte[6];


            return cmd;
        }

        public static byte[] stopBp() {
            byte[] cmd = new byte[6];


            return cmd;
        }

        public static byte[] setTime() {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());

                int len = 7;

                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_SET_TIME;
                cmd[2] = ~CMD_SET_TIME;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;
                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);

                cmd[7] = (byte) (c.get(Calendar.YEAR));
                cmd[8] = (byte) (c.get(Calendar.YEAR) >> 8);
                cmd[9] = (byte) (c.get(Calendar.MONTH)+1);
                cmd[10] = (byte) (c.get(Calendar.DATE));
                cmd[11] = (byte) (c.get(Calendar.HOUR_OF_DAY));
                cmd[12] = (byte) (c.get(Calendar.MINUTE));
                cmd[13] = (byte) (c.get(Calendar.SECOND));

                cmd[7+len] = CrcUtil.calCRC8(cmd);

                serial++;
                LepuBleLog.d("setTime===");

                return cmd;
        }

        public static byte[] getRecords() {
            byte[] cmd = new byte[10];

            return cmd;
        }

        public static byte[] getRecords(int storeIdA, int storeIdB) {
            byte[] cmd = new byte[10];

            return cmd;
        }

        public static byte[] getBpState() {
            byte[] cmd = new byte[10];
            return cmd;
        }


        public static byte[] getFileList() {
                int len = 0;

                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_FILE_LIST;
                cmd[2] = ~CMD_FILE_LIST;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;
                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);
                cmd[7+len] = CrcUtil.calCRC8(cmd);

                serial++;

                return cmd;


        }
    }

    public static byte calcNum(byte[] cmd) {
        int result = BPMCmd.HEAD;

        return (byte) (result);
    }
}
