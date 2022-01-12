package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.DateUtil;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author chenyongfeng
 */
public class F5ScaleBleCmd {

    // response
    public static final int WEIGHT_DATA = 0xD5;
    public static final int IMPEDANCE_DATA = 0xD6;
    public static final int UNSTABLE_DATA = 0xD0;
    public static final int OTHER_DATA = 0xD7;
    public static final int HISTORY_DATA = 0xD8;

    // request
    public static final int SET_PARAM = 0xD0;
    public static final int SET_USER_LIST = 0xD1;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] setUserInfo(int unit, int number, int height, int weight, int age, int sex) {
        TimeZone tz = TimeZone.getDefault();
        String tzName = tz.getDisplayName(false, TimeZone.SHORT);
        String[] strings = new String[2];
        int sign = 0;
        if (tzName.contains("+")) {
            strings = tzName.split("[+]");
            strings = strings[1].split(":");
            sign = 0;
        } else if (tzName.contains("-")) {
            strings = tzName.split("-");
            strings = strings[1].split(":");
            sign = 1;
        }
        int timeZone = (Integer.parseInt(strings[0])*60 + Integer.parseInt(strings[1])) / 15;

        int time = DateUtil.getSecondTimestamp(new Date());
        int len = 16;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAC;
        cmd[1] = (byte) 0x27;
        cmd[2] = (byte) ((time >> 24) & 0xFF);
        cmd[3] = (byte) ((time >> 16) & 0xFF);
        cmd[4] = (byte) ((time >> 8) & 0xFF);
        cmd[5] = (byte) (time & 0xFF);
        cmd[6] = (byte) (sign << 7 | timeZone);
        cmd[7] = (byte) (unit & 0xFF);
        cmd[8] = (byte) (number & 0xFF);
        cmd[9] = (byte) (height & 0xFF);
        cmd[10] = (byte) ((weight >> 8) & 0xFF);
        cmd[11] = (byte) (weight & 0xFF);
        cmd[12] = (byte) (age & 0xFF);
        cmd[13] = (byte) (sex & 0xFF);
        cmd[18] = (byte) SET_PARAM;
        cmd[19] = (byte) CrcUtil.calF5ScaleCHK(cmd);
        return cmd;
    }

    public static byte[] setUserList(int number, int height, int weight, int age, int sex) {
        int len = 16;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAC;
        cmd[1] = (byte) 0x27;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) seqNo;
        cmd[4] = (byte) (number & 0xFF);
        cmd[5] = (byte) (height & 0xFF);
        cmd[6] = (byte) ((weight >> 8) & 0xFF);
        cmd[7] = (byte) (weight & 0xFF);
        cmd[8] = (byte) (age & 0xFF);
        cmd[9] = (byte) (sex & 0xFF);
        cmd[18] = (byte) SET_USER_LIST;
        cmd[19] = (byte) CrcUtil.calF5ScaleCHK(cmd);
        addNo();
        return cmd;
    }
}
