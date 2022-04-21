package com.lepu.blepro.ble.cmd;

import org.json.JSONException;
import org.json.JSONObject;

import static com.lepu.blepro.utils.StringUtilsKt.makeTimeStr;

/**
 * @author wujuan
 */
public class CheckmeLeBleCmd {

    public static int OXY_CMD_READ_START = 0x03;
    public static int OXY_CMD_READ_CONTENT = 0x04;
    public static int OXY_CMD_READ_END = 0x05;

    public static int OXY_CMD_READ_LIST_START = 0x07;
    public static int OXY_CMD_READ_LIST_CONTENT = 0x08;
    public static int OXY_CMD_READ_LIST_END = 0x09;

    public static int OXY_CMD_INFO = 0x14;
    public static int OXY_CMD_PARA_SYNC = 0x16;

    public static final String SYNC_TYPE_TIME = "SetTIME";

    public static class ListType {
        public static final int ECG_TYPE = 0;
        public static final int OXY_TYPE = 1;
        public static final int DLC_TYPE = 2;
        public static final int TEMP_TYPE = 3;
    }

    /**
     * O2 系列不使用SeqNo, 仅在下载数据时作为数据偏移
     */
    private static int seqNo = 0;

    public static byte[] syncTime() {
        JSONObject j = new JSONObject();
        try {
            j.put(SYNC_TYPE_TIME, makeTimeStr());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sync(j);
    }
    private static byte[] sync(JSONObject j) {
        char[] chars = j.toString().toCharArray();
        int size = chars.length;
        byte[] buf = new byte[8 + size];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_PARA_SYNC;
        buf[2] = (byte) ~OXY_CMD_PARA_SYNC;
        buf[5] = (byte) size;
        buf[6] = (byte) (size >> 8);
        for (int i = 0; i < size; i++) {
            buf[7 + i] = (byte) chars[i];
        }
        buf[8 + size - 1] = BleCRC.calCRC8(buf);
        return buf;
    }

    public static byte[] getInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_INFO;
        buf[2] = (byte) ~OXY_CMD_INFO;
        buf[7] = BleCRC.calCRC8(buf);
        return buf;
    }

    public static byte[] readFileStart(String fileName) {
        char[] name = fileName.toCharArray();
        int len = name.length + 1;
        // filename最后一位补0
        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_START;
        buf[2] = (byte) ~OXY_CMD_READ_START;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);
        for (int i = 0; i < len - 1; i++) {
            buf[7 + i] = (byte) name[i];
        }
        buf[buf.length - 1] = BleCRC.calCRC8(buf);
        seqNo = 0;
        return buf;
    }

    public static byte[] readFileContent() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_CONTENT;
        buf[2] = (byte) ~OXY_CMD_READ_CONTENT;
        buf[3] = (byte) seqNo;
        buf[4] = (byte) (seqNo >> 8);
        buf[7] = BleCRC.calCRC8(buf);
        seqNo++;
        return buf;
    }

    public static byte[] readFileEnd() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_END;
        buf[2] = (byte) ~OXY_CMD_READ_END;
        buf[7] = BleCRC.calCRC8(buf);
        seqNo = 0;
        return buf;
    }

    public static byte[] readListStart() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_LIST_START;
        buf[2] = (byte) ~OXY_CMD_READ_LIST_START;
        buf[7] = BleCRC.calCRC8(buf);
        return buf;
    }
    public static byte[] readListContent() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_LIST_CONTENT;
        buf[2] = (byte) ~OXY_CMD_READ_LIST_CONTENT;
        buf[7] = BleCRC.calCRC8(buf);
        return buf;
    }
    public static byte[] readListEnd() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_LIST_END;
        buf[2] = (byte) ~OXY_CMD_READ_LIST_END;
        buf[7] = BleCRC.calCRC8(buf);
        return buf;
    }


}
