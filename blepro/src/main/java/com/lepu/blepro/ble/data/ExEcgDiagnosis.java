package com.lepu.blepro.ble.data;

public class ExEcgDiagnosis {
    // 原始bytes数据
    private byte[] bytes;
    // Regular ECG Rhythm (心电未见明显异常，遵循医生意见)
    private boolean isRegular = false;
    // Unable to analyze (心电信号幅度低或噪声干扰)
    private boolean isPoorSignal = false;
    // Fast Heart Rate (心率过快)
    private boolean isFastHr = false;
    // Slow Heart Rate (心率过缓)
    private boolean isSlowHr = false;
    // Irregular ECG Rhythm (疑似窦性心律失常)
    private boolean isIrregular = false;
    // Possible ventricular premature beats (疑似室性早博)
    private boolean isPvcs = false;
    // Possible heart pause (疑似心跳暂停)
    private boolean isHeartPause = false;
    // Possible Atrial fibrillation (疑似房颤)
    private boolean isFibrillation = false;
    // Wide QRS duration (QRS持续时间)
    private boolean isWideQrs = false;
    // QTc is prolonged (QTc延长)
    private boolean isProlongedQtc = false;
    // QTc is short (QTc短暂)
    private boolean isShortQtc = false;
    // ST segment elevation (ST段抬高)
    private boolean isStElevation = false;
    // ST segment depression (ST压低)
    private boolean isStDepression = false;

    public ExEcgDiagnosis() {

    }

    public ExEcgDiagnosis(byte[] bytes) {
        this.bytes = bytes;
        if (bytes.length != 4) {
            return;
        }

        int result = (bytes[0]&0xFF) + ((bytes[1]&0xFF)<<8) + ((bytes[2]&0xFF)<<16) + ((bytes[1]&0xFF)<<24);

        if (result == 0) {//(心电未见明显异常，遵循医生意见)
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;//心电信号幅度低或噪声干扰
        } else {
            if ((result & 0x00000001) == 0x00000001) {
                isFastHr = true;//心率过快
            }
            if ((result & 0x00000002) == 0x00000002) {
                isSlowHr = true;//心率过缓
            }
            if ((result & 0x00000004) == 0x00000004) {
                isIrregular = true;//疑似窦性心律失常
            }
            if ((result & 0x00000008) == 0x00000008) {
                isPvcs = true;//疑似室性早博
            }
            if ((result & 0x00000010) == 0x00000010) {
                isHeartPause = true;//疑似心跳暂停
            }
            if ((result & 0x00000020) == 0x00000020) {
                isFibrillation = true;//疑似房颤
            }
            if ((result & 0x00000040) == 0x00000040) {
                isWideQrs = true;//QRS持续时间
            }
            if ((result & 0x00000080) == 0x00000080) {
                isProlongedQtc = true;//QTc延长
            }
            if ((result & 0x00000100) == 0x00000100) {
                isShortQtc = true;//QTc短暂
            }
            if ((result & 0x00000200) == 0x00000200) {
                isStElevation = true;//ST段抬高
            }
            if ((result & 0x00000400) == 0x00000400) {
                isStDepression = true;//ST压低
            }
        }

    }

    public ExEcgDiagnosis(int result) {
        if (result == 0) {//(心电未见明显异常，遵循医生意见)
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;//心电信号幅度低或噪声干扰
        } else {
            if ((result & 0x00000001) == 0x00000001) {
                isFastHr = true;//心率过快
            }
            if ((result & 0x00000002) == 0x00000002) {
                isSlowHr = true;//心率过缓
            }
            if ((result & 0x00000004) == 0x00000004) {
                isIrregular = true;//疑似窦性心律失常
            }
            if ((result & 0x00000008) == 0x00000008) {
                isPvcs = true;//疑似室性早博
            }
            if ((result & 0x00000010) == 0x00000010) {
                isHeartPause = true;//疑似心跳暂停
            }
            if ((result & 0x00000020) == 0x00000020) {
                isFibrillation = true;//疑似房颤
            }
            if ((result & 0x00000040) == 0x00000040) {
                isWideQrs = true;//QRS持续时间
            }
            if ((result & 0x00000080) == 0x00000080) {
                isProlongedQtc = true;//QTc延长
            }
            if ((result & 0x00000100) == 0x00000100) {
                isShortQtc = true;//QTc短暂
            }
            if ((result & 0x00000200) == 0x00000200) {
                isStElevation = true;//ST段抬高
            }
            if ((result & 0x00000400) == 0x00000400) {
                isStDepression = true;//ST压低
            }
        }

    }

    public String getResultMess() {
        String str = "";
        if (isRegular) {
            str += "Regular ECG Rhythm; ";
        }
        if (isStDepression) {
            str += "ST segment depression; ";
        }
        if (isStElevation) {
            str += "ST segment elevation; ";
        }
        if (isShortQtc) {
            str += "QTc is short; ";
        }
        if (isProlongedQtc) {
            str += "QTc is prolonged; ";
        }
        if (isWideQrs) {
            str += "Wide QRS duration; ";
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
            str += "Slow Heart Rate; ";
        }
        if (isFastHr) {
            str += "Fast Heart Rate; ";
        }
        if (isPoorSignal) {
            str += "Unable to analyze; ";
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
        return "ExEcgDiagnosis{" +
                "isRegular = " + isRegular +
                ", isPoorSignal = " + isPoorSignal +
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
