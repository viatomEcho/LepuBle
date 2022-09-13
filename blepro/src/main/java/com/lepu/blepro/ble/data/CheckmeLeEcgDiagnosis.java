package com.lepu.blepro.ble.data;

public class CheckmeLeEcgDiagnosis {
    // 原始byte数据
    private byte data;
    // Regular ECG Rhythm，规则心电
    private boolean isRegular = false;
    // Unable to analyze，信号弱
    private boolean isPoorSignal = false;
    // High Heart Rate，心率过高
    private boolean isHighHr = false;
    // Low Heart Rate，心率过低
    private boolean isLowHr = false;
    // Irregular ECG Rhythm，不规则心电
    private boolean isIrregular = false;
    // High QRS Value
    private boolean isHighQrs = false;
    // High ST Value
    private boolean isHighSt = false;
    // Low ST Value
    private boolean isLowSt = false;
    // Suspected Premature Beat
    private boolean isPrematureBeat = false;

    public CheckmeLeEcgDiagnosis() {

    }

    public CheckmeLeEcgDiagnosis(byte data) {
        this.data = data;
        int result = data & 0xFF;

        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0x01) == 0x01) {
                isHighHr = true;
            }
            if ((result & 0x02) == 0x02) {
                isLowHr = true;
            }
            if ((result & 0x04) == 0x04) {
                isHighQrs = true;
            }
            if ((result & 0x08) == 0x08) {
                isHighSt = true;
            }
            if ((result & 0x10) == 0x10) {
                isLowSt = true;
            }
            if ((result & 0x20) == 0x20) {
                isIrregular = true;
            }
            if ((result & 0x40) == 0x40) {
                isPrematureBeat = true;
            }
        }

    }

    public CheckmeLeEcgDiagnosis(int result) {
        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0x01) == 0x01) {
                isHighHr = true;
            }
            if ((result & 0x02) == 0x02) {
                isLowHr = true;
            }
            if ((result & 0x04) == 0x04) {
                isHighQrs = true;
            }
            if ((result & 0x08) == 0x08) {
                isHighSt = true;
            }
            if ((result & 0x10) == 0x10) {
                isLowSt = true;
            }
            if ((result & 0x20) == 0x20) {
                isIrregular = true;
            }
            if ((result & 0x40) == 0x40) {
                isPrematureBeat = true;
            }
        }

    }

    public String getResultMess() {
        String str = "";
        if (isRegular) {
            str += "Regular ECG Rhythm; ";
        }
        if (isLowSt) {
            str += "Low ST Value; ";
        }
        if (isHighSt) {
            str += "High ST Value; ";
        }
        if (isHighQrs) {
            str += "High QRS Value; ";
        }
        if (isIrregular) {
            str += "Irregular ECG Rhythm; ";
        }
        if (isLowHr) {
            str += "Low Heart Rate; ";
        }
        if (isHighHr) {
            str += "High Heart Rate; ";
        }
        if (isPoorSignal) {
            str += "Unable to analyze; ";
        }
        if (isPrematureBeat) {
            str += "Suspected Premature Beat; ";
        }
        return str;
    }

    public byte getData() {
        return data;
    }

    public void setData(byte data) {
        this.data = data;
    }

    public boolean isRegular() {
        return isRegular;
    }

    public void setRegular(boolean regular) {
        isRegular = regular;
    }

    public boolean isPoorSignal() {
        return isPoorSignal;
    }

    public void setPoorSignal(boolean poorSignal) {
        isPoorSignal = poorSignal;
    }

    public boolean isHighHr() {
        return isHighHr;
    }

    public void setHighHr(boolean highHr) {
        isHighHr = highHr;
    }

    public boolean isLowHr() {
        return isLowHr;
    }

    public void setLowHr(boolean lowHr) {
        isLowHr = lowHr;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isHighQrs() {
        return isHighQrs;
    }

    public void setHighQrs(boolean highQrs) {
        isHighQrs = highQrs;
    }

    public boolean isHighSt() {
        return isHighSt;
    }

    public void setHighSt(boolean highSt) {
        isHighSt = highSt;
    }

    public boolean isLowSt() {
        return isLowSt;
    }

    public void setLowSt(boolean lowSt) {
        isLowSt = lowSt;
    }

    public boolean isPrematureBeat() {
        return isPrematureBeat;
    }

    public void setPrematureBeat(boolean prematureBeat) {
        isPrematureBeat = prematureBeat;
    }

    @Override
    public String toString() {
        return "CheckmeLeEcgDiagnosis{" +
                "isRegular=" + isRegular +
                ", isPoorSignal=" + isPoorSignal +
                ", isHighHr=" + isHighHr +
                ", isLowHr=" + isLowHr +
                ", isIrregular=" + isIrregular +
                ", isHighQrs=" + isHighQrs +
                ", isHighSt=" + isHighSt +
                ", isLowSt=" + isLowSt +
                ", isPrematureBeat=" + isPrematureBeat +
                ", getResultMess=" + getResultMess() +
                '}';
    }
}
