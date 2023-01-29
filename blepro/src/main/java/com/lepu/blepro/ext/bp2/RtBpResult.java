package com.lepu.blepro.ext.bp2;

public class RtBpResult {
    private boolean deflate;  // 是否放气
    private int pressure;     // 实时压
    private int sys;         // 收缩压
    private int dia;         // 舒张压
    private int mean;        // 平均圧
    private int pr;          // 脉率
    private int result;      // 状态码 0：正常，1：无法分析（袖套绑的太松，充气慢，缓慢漏气，气容大），
    //       2：波形混乱（打气过程中检测到胳膊有动作或者有其他干扰），3：信号弱，检测不到脉搏波（有干扰袖套的衣物），
    //       >=4：设备错误（堵阀，血压测量超量程，袖套漏气严重，软件系统异常，硬件系统错误，以及其他异常）
    public RtBpResult(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2DataBpResult data = new com.lepu.blepro.ble.data.Bp2DataBpResult(bytes);
        deflate = data.isDeflate();
        pressure = data.getPressure();
        sys = data.getSys();
        dia = data.getDia();
        mean = data.getMean();
        pr = data.getPr();
        result = data.getCode();
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

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMean() {
        return mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RtBpResult{" +
                "deflate=" + deflate +
                ", pressure=" + pressure +
                ", sys=" + sys +
                ", dia=" + dia +
                ", mean=" + mean +
                ", pr=" + pr +
                ", result=" + result +
                '}';
    }
}
