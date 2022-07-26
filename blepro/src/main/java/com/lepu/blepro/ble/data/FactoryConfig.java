package com.lepu.blepro.ble.data;

import com.lepu.blepro.utils.Convertible;

/**
 * @author chenyongfeng
 * 烧录出厂信息
 */
public class FactoryConfig implements Convertible {

    private int burnFlag;
    private char hwVersion = 'A';
    private String branchCode = "40010000";
    private int snLength = 18;
    private String snCode = "2017022211";
    private int snLengthO2 = 11;

    public void setBurnFlag(boolean enableSN, boolean enableHw, boolean enableBranchCode) {
        if(enableSN) {
            burnFlag = burnFlag | 0x01;
        }

        if(enableHw) {
            burnFlag = burnFlag | 0x02;
        }

        if(enableBranchCode) {
            burnFlag = burnFlag | 0x04;
        }
    }

    public void setHwVersion(char hwVersion) {
        this.hwVersion = hwVersion;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public void setSnCode(String snCode) {
        this.snCode = snCode;
    }

    public boolean isValidConfigCmd() {
        return true;
    }

    @Override
    public byte[] convert2Data() {
        char[] branchCodeCharArray = branchCode.toCharArray();
        char[] snCodeCharArray = snCode.toCharArray();

        byte[] data = new byte[branchCodeCharArray.length + snLength + 3];
        data[0] = (byte) burnFlag;
        data[1] = (byte) hwVersion;

        for(int i = 0; i < branchCodeCharArray.length; i++) {
            data[i + 2] = (byte) branchCodeCharArray[i];
        }

        data[2 + branchCodeCharArray.length] = (byte) snCode.length();

        for(int i = 0; i < snLength; i++) {
            if(i > snCodeCharArray.length -1) {
                data[i + 2 + branchCodeCharArray.length + 1] = (byte) 0x00;
            } else {
                data[i + 2 + branchCodeCharArray.length + 1] = (byte) snCodeCharArray[i];
            }

        }

        return data;
    }

    public byte[] convert2DataO2() {
        char[] branchCodeCharArray = branchCode.toCharArray();
        char[] snCodeCharArray = snCode.toCharArray();

        byte[] data = new byte[branchCodeCharArray.length + snLengthO2 + 2];
        data[0] = (byte) burnFlag;

        for(int i = 0; i < snLengthO2; i++) {
            if(i > snCodeCharArray.length -1) {
                data[i + 1] = (byte) 0x00;
            } else {
                data[i + 1] = (byte) snCodeCharArray[i];
            }

        }
        data[12] = (byte) hwVersion;

        for(int i = 0; i < branchCodeCharArray.length; i++) {
            data[i + 2 + snLengthO2] = (byte) branchCodeCharArray[i];
        }

        return data;
    }
}
