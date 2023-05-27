package com.lepu.blepro.ext.ventilator;

public class MaskTestResult {
    private int status;  // 0:未在测试状态；1：测试中；2：测试结束
    private float leak;  // 实时漏气量(0~120L/min),单位0.1L/min,e.g.10:1L/min[0,1200]
    private int result;  // 0:测试未完成；1：不合适；2：合适

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getLeak() {
        return leak;
    }

    public void setLeak(float leak) {
        this.leak = leak;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "MaskTestResult{" +
                "status=" + status +
                ", leak=" + leak +
                ", result=" + result +
                '}';
    }
}
