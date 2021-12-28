package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.FscaleUserInfo;
import com.lepu.blepro.utils.CrcUtil;
import com.lepu.blepro.utils.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class F4ScaleBleCmd {

    // response
    public final static int A0 = 0xA0;
    public final static int A1 = 0xA1;
    public final static int A2 = 0xA2;
    public final static int A3 = 0xA3;
    public final static int A4 = 0xA4;

    // request
    public final static int B0 = 0xB0;
    public final static int B1 = 0xB1;
    public final static int B2 = 0xB2;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] responsePackage(int packageNo) {
        int len = 16;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) seqNo;
        cmd[1] = (byte) cmd.length;
        cmd[2] = (byte) 0x00;
        cmd[3] = (byte) B0;
        cmd[4] = (byte) packageNo;
        cmd[5] = (byte) 0x00;
        cmd[19] = (byte) CrcUtil.calF5ScaleCHK(cmd);
        addNo();
        return cmd;
    }

    public static byte[] updateUserInfo(int index, int height, int weight, int age, int sex) {
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
        cmd[0] = (byte) seqNo;
        cmd[1] = (byte) cmd.length;
        cmd[2] = (byte) 0x00;
        cmd[3] = (byte) B1;
        cmd[4] = (byte) ((time >> 24) & 0xFF);
        cmd[5] = (byte) ((time >> 16) & 0xFF);
        cmd[6] = (byte) ((time >> 8) & 0xFF);
        cmd[7] = (byte) (time & 0xFF);
        cmd[8] = (byte) ((sign << 7 | (timeZone >> 8)) & 0xFF);
        cmd[9] = (byte) (timeZone & 0xFF);
        cmd[10] = (byte) (index & 0xFF);
        cmd[11] = (byte) (height & 0xFF);
        cmd[12] = (byte) ((weight >> 8) & 0xFF);
        cmd[13] = (byte) (weight & 0xFF);
        cmd[14] = (byte) ((sex==1?1:0) << 7 | age);
        cmd[15] = (byte) 0x07;
        cmd[19] = (byte) CrcUtil.calF5ScaleCHK(cmd);
        addNo();
        return cmd;
    }

    public static byte[] setUserList(List<FscaleUserInfo> userInfos) {
        int count = userInfos.size();
        int len = 2 + count*4;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) seqNo;
        cmd[1] = (byte) cmd.length;
        cmd[2] = (byte) 0x00;
        cmd[3] = (byte) B2;
        cmd[4] = (byte) (count & 0xFF);
        for (int i=0; i<count; i++) {
            cmd[5+i*4] = (byte) (userInfos.get(i).height & 0xFF);
            cmd[6+i*4] = (byte) (((int)(userInfos.get(i).weight*100) >> 8) & 0xFF);
            cmd[7+i*4] = (byte) ((int)(userInfos.get(i).weight*100) & 0xFF);
            cmd[8+i*4] = (byte) ((userInfos.get(i).sex.getValue()==1?1:0) << 7 | userInfos.get(i).age);
        }
        cmd[cmd.length-1] = (byte) CrcUtil.calF5ScaleCHK(cmd);
        addNo();
        return cmd;
    }

}
