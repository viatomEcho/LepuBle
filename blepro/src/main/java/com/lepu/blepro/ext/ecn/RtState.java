package com.lepu.blepro.ext.ecn;

public class RtState {
    private int state;
    private int duration;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "RtState{" +
                "state=" + state +
                ", duration=" + duration +
                '}';
    }
}
