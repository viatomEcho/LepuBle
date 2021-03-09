package com.lepu.blepro.utils;

/**
 * author: wujuan
 * created on: 2021/3/9 11:12
 * description:
 */
public class DfuUtil {
    public static final String BOOTLOADER_NAME_KEYWORD = "Updater";
    public static boolean isDfuDevice(final String deviceName) {
        return deviceName != null && deviceName.contains(BOOTLOADER_NAME_KEYWORD);
    }
    public static String getNewMac(String mac) {
        int type = 1;
        String newMac = "";
        String oneMac = mac.substring(0, mac.lastIndexOf(":") + 1);
        String twoMac = mac.substring(mac.lastIndexOf(":") + 1);
        int newTwoMac = Integer.parseInt(twoMac, 16);
        if (type == 1) {
            if (newTwoMac == 0xff) {
                newTwoMac = 0;
            } else {
                newTwoMac = newTwoMac + 1;
            }
        } else if (type == 2) {
            if (newTwoMac == 0) {
                newTwoMac = 0xff;
            } else {
                newTwoMac = newTwoMac - 1;
            }
        }
        String last = Integer.toHexString(newTwoMac);
        newMac = oneMac + (last.length() == 1 ? (0 + last) : last);
//        newMac = oneMac + last;

        LepuBleLog.e("DfuUtil", "old mac====>" + mac + "new mac" + newMac);
        return newMac.toUpperCase();
    }
}
