package com.lepu.blepro.ble.data.ventilator;

/**
 * @author chenyongfeng
 */
public class VentilatorModel {

    private boolean supportCpap;
    private boolean supportApap;
    private boolean supportS;
    private boolean supportST;
    private boolean supportT;

    public VentilatorModel(String branchCode) {
        // BranchCode 第三位为机型码，取值范围A-H其中ABCD是单水平，EFGH是双水平
        // 单水平默认只有CPAP、APAP模式，双水平默认支持CPAP、APAP、S、T、S/T，特殊的ABEF不支持APAP
        if (branchCode == null || branchCode.length() < 3) {
            return;
        }
        char c = branchCode.charAt(2);
        switch (c) {
            case 'A':
            case 'B':
                supportCpap = true;
                supportApap = false;
                supportS = false;
                supportST = false;
                supportT = false;
                break;
            case 'C':
            case 'D':
                supportCpap = true;
                supportApap = true;
                supportS = false;
                supportST = false;
                supportT = false;
                break;
            case 'E':
            case 'F':
                supportCpap = true;
                supportApap = false;
                supportS = true;
                supportST = true;
                supportT = true;
                break;
            case 'G':
            case 'H':
                supportCpap = true;
                supportApap = true;
                supportS = true;
                supportST = true;
                supportT = true;
                break;
            default:
                supportCpap = false;
                supportApap = false;
                supportS = false;
                supportST = false;
                supportT = false;
        }
    }

    public boolean isSupportCpap() {
        return supportCpap;
    }

    public void setSupportCpap(boolean supportCpap) {
        this.supportCpap = supportCpap;
    }

    public boolean isSupportApap() {
        return supportApap;
    }

    public void setSupportApap(boolean supportApap) {
        this.supportApap = supportApap;
    }

    public boolean isSupportS() {
        return supportS;
    }

    public void setSupportS(boolean supportS) {
        this.supportS = supportS;
    }

    public boolean isSupportST() {
        return supportST;
    }

    public void setSupportST(boolean supportST) {
        this.supportST = supportST;
    }

    public boolean isSupportT() {
        return supportT;
    }

    public void setSupportT(boolean supportT) {
        this.supportT = supportT;
    }

    @Override
    public String toString() {
        return "VentilatorModel{" +
                "supportCpap=" + supportCpap +
                ", supportApap=" + supportApap +
                ", supportS=" + supportS +
                ", supportST=" + supportST +
                ", supportT=" + supportT +
                '}';
    }
}
