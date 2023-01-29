package com.lepu.blepro.ext.lpbp2w;

public class LpBp2wConfig {
    private boolean soundOn;            // 心电音开关
    private int avgMeasureMode;         // 0：x3模式关闭 1：x3模式开启（时间间隔30s） 2：时间间隔60s 3：时间间隔90s 4：时间间隔120s
    private int volume;                 // 音量大小（0关，1，2，3）

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    public int getAvgMeasureMode() {
        return avgMeasureMode;
    }

    public void setAvgMeasureMode(int avgMeasureMode) {
        this.avgMeasureMode = avgMeasureMode;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "LpBp2wConfig{" +
                "soundOn=" + soundOn +
                ", avgMeasureMode=" + avgMeasureMode +
                ", volume=" + volume +
                '}';
    }
}
