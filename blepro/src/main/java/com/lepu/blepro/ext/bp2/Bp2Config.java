package com.lepu.blepro.ext.bp2;

public class Bp2Config {
    private boolean soundOn;            // 心电音开关

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    @Override
    public String toString() {
        return "Bp2Config{" +
                "soundOn=" + soundOn +
                '}';
    }
}
