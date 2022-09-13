package com.lepu.blepro.ble.data;

public class Er2EcgDiagnosis {
    // 原始bytes数据
    private byte[] bytes;
    // 除下列异常情况之外 (正常心电图)，规则心电
    private boolean isRegular = false;
    // 波形质量差，或者导联一直脱落等算法无法分析的情况 (无法分析)，信号弱
    private boolean isPoorSignal = false;
    // 不满30s (不满30s不分析)
    private boolean isLessThan30s = false;
    // 检测到动作 (不分析)
    private boolean isMoving = false;
    // HR>100bpm (心率过速)，心率过高
    private boolean isFastHr = false;
    // HR<50bpm (心率过缓)，心率过低
    private boolean isSlowHr = false;
    // RR间期不规则 (不规则心律)，不规则心电
    private boolean isIrregular = false;
    // PVC (心室早搏)
    private boolean isPvcs = false;
    // 停搏
    private boolean isHeartPause = false;
    // 房颤
    private boolean isFibrillation = false;
    // QRS>120ms (QRS过宽)
    private boolean isWideQrs = false;
    // QTc>450ms (QTc间期延长)
    private boolean isProlongedQtc = false;
    // QTc<300ms (QTc间期缩短)
    private boolean isShortQtc = false;
    // ST>+0.2mV (ST段抬高)
    private boolean isStElevation = false;
    // ST<-0.2mV (ST段压低)
    private boolean isStDepression = false;

    public Er2EcgDiagnosis() {

    }

    public Er2EcgDiagnosis(byte[] bytes) {
        this.bytes = bytes;
        if (bytes.length != 4) {
            return;
        }

        int result = (bytes[0]&0xFF) + ((bytes[1]&0xFF)<<8) + ((bytes[2]&0xFF)<<16) + ((bytes[3]&0xFF)<<24);

        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0xFFFFFFFE) == 0xFFFFFFFE) {
                isLessThan30s = true;
            }
            if ((result & 0xFFFFFFFD) == 0xFFFFFFFD) {
                isMoving = true;
            }
            if ((result & 0x00000001) == 0x00000001) {
                isFastHr = true;
            }
            if ((result & 0x00000002) == 0x00000002) {
                isSlowHr = true;
            }
            if ((result & 0x00000004) == 0x00000004) {
                isIrregular = true;
            }
            if ((result & 0x00000008) == 0x00000008) {
                isPvcs = true;
            }
            if ((result & 0x00000010) == 0x00000010) {
                isHeartPause = true;
            }
            if ((result & 0x00000020) == 0x00000020) {
                isFibrillation = true;
            }
            if ((result & 0x00000040) == 0x00000040) {
                isWideQrs = true;
            }
            if ((result & 0x00000080) == 0x00000080) {
                isProlongedQtc = true;
            }
            if ((result & 0x00000100) == 0x00000100) {
                isShortQtc = true;
            }
            if ((result & 0x00000200) == 0x00000200) {
                isStElevation = true;
            }
            if ((result & 0x00000400) == 0x00000400) {
                isStDepression = true;
            }
        }

    }

    public Er2EcgDiagnosis(int result) {
        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0xFFFFFFFE) == 0xFFFFFFFE) {
                isLessThan30s = true;
            }
            if ((result & 0xFFFFFFFD) == 0xFFFFFFFD) {
                isMoving = true;
            }
            if ((result & 0x00000001) == 0x00000001) {
                isFastHr = true;
            }
            if ((result & 0x00000002) == 0x00000002) {
                isSlowHr = true;
            }
            if ((result & 0x00000004) == 0x00000004) {
                isIrregular = true;
            }
            if ((result & 0x00000008) == 0x00000008) {
                isPvcs = true;
            }
            if ((result & 0x00000010) == 0x00000010) {
                isHeartPause = true;
            }
            if ((result & 0x00000020) == 0x00000020) {
                isFibrillation = true;
            }
            if ((result & 0x00000040) == 0x00000040) {
                isWideQrs = true;
            }
            if ((result & 0x00000080) == 0x00000080) {
                isProlongedQtc = true;
            }
            if ((result & 0x00000100) == 0x00000100) {
                isShortQtc = true;
            }
            if ((result & 0x00000200) == 0x00000200) {
                isStElevation = true;
            }
            if ((result & 0x00000400) == 0x00000400) {
                isStDepression = true;
            }
        }

    }

    public String getResultMess() {
        String str = "";
        if (isRegular) {
            str += "正常心电; ";
        }
        if (isStDepression) {
            str += "ST<-0.2mV，ST段压低; ";
        }
        if (isStElevation) {
            str += "ST>+0.2mV，ST段抬高; ";
        }
        if (isShortQtc) {
            str += "QTc<300ms，QTc间期缩短; ";
        }
        if (isProlongedQtc) {
            str += "QTc>450ms，QTc间期延长; ";
        }
        if (isWideQrs) {
            str += "QRS>120ms，QRS过宽; ";
        }
        if (isFibrillation) {
            str += "房颤; ";
        }
        if (isHeartPause) {
            str += "停搏; ";
        }
        if (isPvcs) {
            str += "心室早搏; ";
        }
        if (isIrregular) {
            str += "RR间期不规则; ";
        }
        if (isSlowHr) {
            str += "HR<50bpm，心率过缓; ";
        }
        if (isFastHr) {
            str += "HR>100bpm，心率过速; ";
        }
        if (isMoving) {
            str += "检测到动作，不分析; ";
        }
        if (isLessThan30s) {
            str += "不满30s不分析; ";
        }
        if (isPoorSignal) {
            str += "波形质量差，或者导联一直脱落等算法无法分析; ";
        }
        return str;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
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

    public boolean isLessThan30s() {
        return isLessThan30s;
    }

    public void setLessThan30s(boolean lessThan30s) {
        isLessThan30s = lessThan30s;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public boolean isFastHr() {
        return isFastHr;
    }

    public void setFastHr(boolean fastHr) {
        isFastHr = fastHr;
    }

    public boolean isSlowHr() {
        return isSlowHr;
    }

    public void setSlowHr(boolean slowHr) {
        isSlowHr = slowHr;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isPvcs() {
        return isPvcs;
    }

    public void setPvcs(boolean pvcs) {
        isPvcs = pvcs;
    }

    public boolean isHeartPause() {
        return isHeartPause;
    }

    public void setHeartPause(boolean heartPause) {
        isHeartPause = heartPause;
    }

    public boolean isFibrillation() {
        return isFibrillation;
    }

    public void setFibrillation(boolean fibrillation) {
        isFibrillation = fibrillation;
    }

    public boolean isWideQrs() {
        return isWideQrs;
    }

    public void setWideQrs(boolean wideQrs) {
        isWideQrs = wideQrs;
    }

    public boolean isProlongedQtc() {
        return isProlongedQtc;
    }

    public void setProlongedQtc(boolean prolongedQtc) {
        isProlongedQtc = prolongedQtc;
    }

    public boolean isShortQtc() {
        return isShortQtc;
    }

    public void setShortQtc(boolean shortQtc) {
        isShortQtc = shortQtc;
    }

    public boolean isStElevation() {
        return isStElevation;
    }

    public void setStElevation(boolean stElevation) {
        isStElevation = stElevation;
    }

    public boolean isStDepression() {
        return isStDepression;
    }

    public void setStDepression(boolean stDepression) {
        isStDepression = stDepression;
    }

    @Override
    public String toString() {
        return "Er2EcgDiagnosis{" +
                "isRegular = " + isRegular +
                ", isPoorSignal = " + isPoorSignal +
                ", isLessThan30s = " + isLessThan30s +
                ", isMoving = " + isMoving +
                ", isFastHr = " + isFastHr +
                ", isSlowHr = " + isSlowHr +
                ", isIrregular = " + isIrregular +
                ", isPvcs = " + isPvcs +
                ", isHeartPause = " + isHeartPause +
                ", isFibrillation = " + isFibrillation +
                ", isWideQrs = " + isWideQrs +
                ", isProlongedQtc = " + isProlongedQtc +
                ", isShortQtc = " + isShortQtc +
                ", isStElevation = " + isStElevation +
                ", isStDepression = " + isStDepression +
                ", resultMess = " + getResultMess() +
                '}';
    }
}
