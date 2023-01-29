package com.lepu.blepro.ext.bp2w;

public class RtBpIng {
    private boolean deflate;  // 是否放气
    private int pressure;     // 实时压
    private boolean pulse;    // 是否检测到脉搏波
    private int pr;
    public RtBpIng(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2DataBpIng data = new com.lepu.blepro.ble.data.Bp2DataBpIng(bytes);
        deflate = data.isDeflate();
        pressure = data.getPressure();
        pulse = data.isPulse();
        pr = data.getPr();
    }
    public boolean isDeflate() {
        return deflate;
    }

    public void setDeflate(boolean deflate) {
        this.deflate = deflate;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public boolean isPulse() {
        return pulse;
    }

    public void setPulse(boolean pulse) {
        this.pulse = pulse;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    @Override
    public String toString() {
        return "RtBpIng{" +
                "deflate=" + deflate +
                ", pressure=" + pressure +
                ", pulse=" + pulse +
                ", pr=" + pr +
                '}';
    }
}
