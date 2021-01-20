package com.lepu.blepro.ble.cmd;



import com.lepu.blepro.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OxyBleCmd {

    public static int OXY_CMD_INFO = 0x14;
    public static int OXY_CMD_PARA_SYNC = 0x16;
    public static int OXY_CMD_RT_DATA = 0x1B;
    public static int OXY_CMD_RESET = 0x18;
    public static int OXY_CMD_READ_START = 0x03;
    public static int OXY_CMD_READ_CONTENT = 0x04;
    public static int OXY_CMD_READ_END = 0x05;

    /*************************同步相关**************************************/
    public final static String OXY_CMD_SYNC_TIME = "SetTIME";
    public final static String OXY_CMD_SYNC_OXI_SWITCH = "SetOxiSwitch";
    public final static String OXY_CMD_SYNC_OXI_THR = "SetOxiThr";
    public final static String OXY_CMD_SYNC_HR_SWITCH = "SetHRSwitch";
    public final static String OXY_CMD_SYNC_HR_LOW_THR = "SetHRLowThr";
    public final static String OXY_CMD_SYNC_HR_HIGH_THR = "SetHRHighThr";
    public final static String OXY_CMD_SYNC_MOTOR = "SetMotor";
    /*************************同步相关**************************************/
    public final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");



    // O2 系列不使用SeqNo, 仅在下载数据时作为数据偏移
    private static int seqNo = 0;

    private static void addNo() {
        seqNo++;
        if (seqNo >= 65535) {
            seqNo = 0;
        }
    }

    public static byte[] getInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_INFO;
        buf[2] = (byte) ~OXY_CMD_INFO;

        buf[7] = Er1BleCRC.calCRC8(buf);


        return buf;
    }

    public static byte[] syncData(String type, int value) {
        long time = System.currentTimeMillis();
        JSONObject j = new JSONObject();
        try {
            switch (type) {
                case OXY_CMD_SYNC_TIME:
//                    j.put(OXY_CMD_SYNC_TIME, DateFormatUtils.format(new Date(), "yyyy-MM-dd,HH:mm:ss", Locale.getDefault()));
                    j.put(OXY_CMD_SYNC_TIME, df.format(new Date()));
                    break;
                default:
                    LogUtils.d("syncData type="+type+"value="+value);
                    j.put(type, value+"");
                    break;
//                case OXY_CMD_SYNC_OXI_SWITCH:
//                    j.put(OXY_CMD_SYNC_OXI_SWITCH, value);
//                    break;
//                case OXY_CMD_SYNC_OXI_THR:
//                    j.put(OXY_CMD_SYNC_OXI_THR, value);
//                    break;
//                case OXY_CMD_SYNC_HR_SWITCH:
//                    j.put(OXY_CMD_SYNC_HR_SWITCH, value);
//                    break;
//                case OXY_CMD_SYNC_HR_LOW_THR:
//                    j.put(OXY_CMD_SYNC_HR_LOW_THR, value);
//                    break;
//                case OXY_CMD_SYNC_HR_HIGH_THR:
//                    j.put(OXY_CMD_SYNC_HR_HIGH_THR, value);
//                    break;
//                case OXY_CMD_SYNC_MOTOR:
//                    j.put(OXY_CMD_SYNC_MOTOR, value);
//                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        buf[8 + size - 1] = Er1BleCRC.calCRC8(buf);


        return buf;
    }

    public static byte[] getRtWave() {
        int len = 1;

        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_RT_DATA;
        buf[2] = (byte) ~OXY_CMD_RT_DATA;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);

        buf[7] = (byte) 0;  // 0 -> 125hz;  1-> 62.5hz

        buf[8] = Er1BleCRC.calCRC8(buf);


        return buf;

    }

    public static byte[] readFileStart(String fileName) {
        char[] name = fileName.toCharArray();
        int len = name.length + 1;

        byte[] buf = new byte[8 + len];  // filename最后一位补0
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_START;
        buf[2] = (byte) ~OXY_CMD_READ_START;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);

        for (int i = 0; i < len - 1; i++) {
            buf[7 + i] = (byte) name[i];
        }

        buf[buf.length - 1] = Er1BleCRC.calCRC8(buf);

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

        buf[7] = Er1BleCRC.calCRC8(buf);

        seqNo++;

        return buf;
    }

    public static byte[] readFileEnd() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_END;
        buf[2] = (byte) ~OXY_CMD_READ_END;

        buf[7] = Er1BleCRC.calCRC8(buf);

        seqNo = 0;

        return buf;
    }

    public static byte[] resetDeviceInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_RESET;
        buf[2] = (byte) ~OXY_CMD_RESET;

        buf[7] = Er1BleCRC.calCRC8(buf);


        return buf;
    }


}
