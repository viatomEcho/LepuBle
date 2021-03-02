package com.lepu.blepro.ble.cmd;


import java.util.Arrays;

public class Er2BleResponse {

    private byte head;
    private byte cmd;
    private byte _cmd;
    private byte pkgType;
    private byte pkgNo;
    private int length;
    private byte[] data;
    private byte crc8;

    private byte[] buf;

    public Er2BleResponse(byte[] buf) {
        this.buf = buf;

        head = buf[0];
        cmd = buf[1];
        _cmd = buf[2];
        pkgType = buf[3];
        pkgNo = buf[4];

        length = (buf[5] & 0xFF) + ((buf[6] & 0xFF) << 8);

        data = Arrays.copyOfRange(buf, Er2BleCmd.COMMON_PKG_HEAD_LENGTH, Er2BleCmd.COMMON_PKG_HEAD_LENGTH + length);

        crc8 = buf[Er2BleCmd.COMMON_PKG_HEAD_LENGTH + length];
    }

    public byte getCmd() {
        return cmd;
    }

    public byte get_cmd() {
        return _cmd;
    }

    public byte getPkgType() {
        return pkgType;
    }

    public byte getPkgNo() {
        return pkgNo;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getBuf() {
        return buf;
    }

    public byte getCrc8() {
        return crc8;
    }

    public static boolean isValidResp(byte[] input) {
        if(input == null) {
            return false;
        }

        if(input.length < Er2BleCmd.MIN_PKG_LENGTH) {
            return false;
        }

        if((input[0] & 0xFF) != 0xA5) {
            return false;
        }

        if(input[1] != (~input[2])) {
            return false;
        }

        int inputDataLength = (input[5] & 0xFF) + ((input[6] & 0xFF) << 8);
        if(inputDataLength == input.length - Er2BleCmd.MIN_PKG_LENGTH) {
            return true;
        }

        return BleCRC.calCRC8(input) != input[input.length - 1];
    }
}
