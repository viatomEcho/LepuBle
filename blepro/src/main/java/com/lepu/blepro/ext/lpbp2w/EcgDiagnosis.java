package com.lepu.blepro.ext.lpbp2w;

public class EcgDiagnosis {
    // 原始bytes数据
    private byte[] bytes;
    // 除下列异常情况之外 (正常心电图)，规则心电
    private boolean isRegular = false;
    // 波形质量差，或者导联一直脱落等算法无法分析的情况 (无法分析)，信号弱
    private boolean isPoorSignal = false;
    // 导联一直脱落 (无法分析)
    private boolean isLeadOff = false;
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

    public EcgDiagnosis() {

    }

    public EcgDiagnosis(byte[] bytes) {
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
                isLeadOff = true;
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
        }

    }

    public EcgDiagnosis(int result) {
        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0xFFFFFFFE) == 0xFFFFFFFE) {
                isLeadOff = true;
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
        }

    }

    public String getResultMess() {
        String str = "";
        if (isRegular) {
            str += "Regular ECG Rhythm; ";
        }
        if (isShortQtc) {
            str += "QTc is short(QTc<300ms); ";
        }
        if (isProlongedQtc) {
            str += "QTc is prolonged(QTc>450ms); ";
        }
        if (isWideQrs) {
            str += "Wide QRS duration(QRS>120ms); ";
        }
        if (isFibrillation) {
            str += "Possible Atrial fibrillation; ";
        }
        if (isHeartPause) {
            str += "Possible heart pause; ";
        }
        if (isPvcs) {
            str += "Possible ventricular premature beats; ";
        }
        if (isIrregular) {
            str += "Irregular ECG Rhythm; ";
        }
        if (isSlowHr) {
            str += "Slow Heart Rate(HR<50bpm); ";
        }
        if (isFastHr) {
            str += "Fast Heart Rate(HR>100bpm); ";
        }
        if (isLeadOff) {
            str += "Unable to analyze(Always lead off); ";
        }
        if (isPoorSignal) {
            str += "Unable to analyze(Poor signal); ";
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

    public boolean isLeadOff() {
        return isLeadOff;
    }

    public void setLeadOff(boolean leadOff) {
        isLeadOff = leadOff;
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

    @Override
    public String toString() {
        return "EcgDiagnosis{" +
                "isRegular = " + isRegular +
                ", isPoorSignal = " + isPoorSignal +
                ", isLeadOff = " + isLeadOff +
                ", isFastHr = " + isFastHr +
                ", isSlowHr = " + isSlowHr +
                ", isIrregular = " + isIrregular +
                ", isPvcs = " + isPvcs +
                ", isHeartPause = " + isHeartPause +
                ", isFibrillation = " + isFibrillation +
                ", isWideQrs = " + isWideQrs +
                ", isProlongedQtc = " + isProlongedQtc +
                ", isShortQtc = " + isShortQtc +
                ", resultMess = " + getResultMess() +
                '}';
    }
}
