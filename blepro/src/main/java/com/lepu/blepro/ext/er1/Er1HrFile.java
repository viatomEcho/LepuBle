package com.lepu.blepro.ext.er1;

import com.lepu.blepro.ble.data.VBeatHrFile;

public class Er1HrFile {

    private int fileVersion;     // 文件版本 e.g.  0x01 :  V1
    private int[] hrList;
    private int[] motionList;
    private boolean[] vibrationList;
    private int recordingTime;   // 记录时长 e.g. 3600 :  3600s
    private long magic;           // 文件标志 固定值为0xA55A0438

    public Er1HrFile(byte[] bytes) {
        VBeatHrFile data = new VBeatHrFile(bytes);
        fileVersion = data.getFileVersion();
        recordingTime = data.getRecordingTime();
        magic = data.getMagic();
        int len = data.getHrList().size();
        hrList = new int[len];
        motionList = new int[len];
        vibrationList = new boolean[len];
        for (int i=0; i<len; i++) {
            hrList[i] = data.getHrList().get(i).getHr();
            motionList[i] = data.getHrList().get(i).getMotion();
            vibrationList[i] = data.getHrList().get(i).getVibration();
        }
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int[] getHrList() {
        return hrList;
    }

    public void setHrList(int[] hrList) {
        this.hrList = hrList;
    }

    public int[] getMotionList() {
        return motionList;
    }

    public void setMotionList(int[] motionList) {
        this.motionList = motionList;
    }

    public boolean[] getVibrationList() {
        return vibrationList;
    }

    public void setVibrationList(boolean[] vibrationList) {
        this.vibrationList = vibrationList;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }
}
