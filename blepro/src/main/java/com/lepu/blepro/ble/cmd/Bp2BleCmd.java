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
        public final static int MSG_TYPE_GET_INFO = 0x03;
        public final static int MSG_TYPE_SET_TIME = 0x04;
        public final static int MSG_TYPE_START_BP = 0x15;
        public final static int MSG_TYPE_STOP_BP = 0x16;
        public final static int MSG_TYPE_GET_BP_STATE = 0x18;//实施波形状态
        public final static int MSG_TYPE_GET_BP_FILE_LIST = 0xF1;//获取文件列表




        public final static byte CMD_FILE_READ_START = (byte) 0xF2;//开始
        public final static byte CMD_FILE_READ_PKG = (byte) 0xF3;//读取中
        public final static byte CMD_FILE_READ_END = (byte) 0xF4;//结束

        public static byte[] getCmd(int msgType) {
            switch (msgType) {
                case BPMCmd.MSG_TYPE_GET_INFO:
                    return getInfo();//信息
                case BPMCmd.MSG_TYPE_SET_TIME:
                    return setTime();//设置时间
                case BPMCmd.MSG_TYPE_GET_BP_FILE_LIST:
                    return getFileList();//获取文件列表
                case BPMCmd.MSG_TYPE_START_BP:
                    return startBp();//开始血压测量
                case BPMCmd.MSG_TYPE_STOP_BP:
                    return stopBp();//停止血压测量
                default:
                    return new byte[0];
            }
        }


        public static byte[] getInfo() {
            int len = 0;

            byte[] cmd = new byte[8 + len];

            cmd[0] = HEAD;
            cmd[1] = CMD_INFO;
            cmd[2] = ~CMD_INFO;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len >> 8);
            cmd[7 + len] = CrcUtil.calCRC8(cmd);

            serial++;

            return cmd;
        }

        public static byte[] getConfig() {
            int len = 0;

            byte[] cmd = new byte[8 + len];

            cmd[0] = HEAD;
            cmd[1] = CMD_BP2_CONFIG;
            cmd[2] = ~CMD_BP2_CONFIG;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len >> 8);
            cmd[7 + len] = CrcUtil.calCRC8(cmd);

            serial++;

            return cmd;


        }

        public static byte[] setConfig(boolean switchState) {
            int len = 40;

            byte[] cmd = new byte[8 + len];

            cmd[0] = HEAD;
            cmd[1] = CMD_BP2_SET_SWITCHER_STATE;
            cmd[2] = ~CMD_BP2_SET_SWITCHER_STATE;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;

            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len >> 8);

            if (switchState) {
                cmd[31] = 1;
            }

            cmd[7 + len] = CrcUtil.calCRC8(cmd);

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

            byte[] cmd = new byte[8 + len];

            cmd[0] = HEAD;
            cmd[1] = CMD_SET_TIME;
            cmd[2] = ~CMD_SET_TIME;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len >> 8);

            cmd[7] = (byte) (c.get(Calendar.YEAR));
            cmd[8] = (byte) (c.get(Calendar.YEAR) >> 8);
            cmd[9] = (byte) (c.get(Calendar.MONTH) + 1);
            cmd[10] = (byte) (c.get(Calendar.DATE));
            cmd[11] = (byte) (c.get(Calendar.HOUR_OF_DAY));
            cmd[12] = (byte) (c.get(Calendar.MINUTE));
            cmd[13] = (byte) (c.get(Calendar.SECOND));

            cmd[7 + len] = CrcUtil.calCRC8(cmd);

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
        //文件下载开始
        public static byte[] getFileStart(byte[] fileName,byte offset){
            // filename = 16, offset = 4
            int len = 20;

            byte[] cmd = new byte[8+len];

            cmd[0] = HEAD;
            cmd[1] = CMD_FILE_READ_START;
            cmd[2] = ~CMD_FILE_READ_START;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len>>8);

            // file name > 16 : cut
            int l = fileName.length > 20 ? 20 : fileName.length;

            System.arraycopy(fileName, 0, cmd, 7, l);

            cmd[7+len-4] = (byte) (offset >> 24);
            cmd[7+len-3] = (byte) (offset >> 16);
            cmd[7+len-2] = (byte) (offset >> 8);
            cmd[7+len-1] = (byte) offset;

            cmd[7+len] = CrcUtil.calCRC8(cmd);

            serial++;

            return cmd;
        }
        //文件下载结束
        public static byte[] fileReadEnd() {
                int len = 0;

                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_FILE_READ_END;
                cmd[2] = ~CMD_FILE_READ_END;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;
                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);
                cmd[7+len] = CrcUtil.calCRC8(cmd);

                serial++;

                return cmd;

        }
        //文件下载中途
        public static byte[] fileReadPkg(int addrOffset) {
                int len = 4;
                byte[] cmd = new byte[8+len];

                cmd[0] = HEAD;
                cmd[1] = CMD_FILE_READ_PKG;
                cmd[2] = ~CMD_FILE_READ_PKG;
                cmd[3] = TYPE_NORMAL_SEND;
                cmd[4] = (byte) serial;
                // length
                cmd[5] = (byte) len;
                cmd[6] = (byte) (len>>8);

                cmd[7] = (byte) addrOffset;
                cmd[8] = (byte) (addrOffset >> 8);
                cmd[9] = (byte) (addrOffset >> 16);
                cmd[10] = (byte) (addrOffset >> 24);

                cmd[11] = CrcUtil.calCRC8(cmd);

                serial++;

                return cmd;

        }
        public static byte[] getFileList() {
            int len = 0;

            byte[] cmd = new byte[8 + len];

            cmd[0] = HEAD;
            cmd[1] = CMD_FILE_LIST;
            cmd[2] = ~CMD_FILE_LIST;
            cmd[3] = TYPE_NORMAL_SEND;
            cmd[4] = (byte) serial;
            // length
            cmd[5] = (byte) len;
            cmd[6] = (byte) (len >> 8);
            cmd[7 + len] = CrcUtil.calCRC8(cmd);

            serial++;

            return cmd;

        }
    }

    public static byte calcNum(byte[] cmd) {
        int result = BPMCmd.HEAD;

        return (byte) (result);
    }
}
