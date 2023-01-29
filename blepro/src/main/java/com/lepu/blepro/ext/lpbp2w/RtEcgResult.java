package com.lepu.blepro.ext.lpbp2w;

public class RtEcgResult {
    private int result;
    private EcgDiagnosis diagnosis;   // 诊断结果
    private int hr;
    private int qrs;
    private int pvcs;
    private int qtc;
    public RtEcgResult(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2DataEcgResult data = new com.lepu.blepro.ble.data.Bp2DataEcgResult(bytes);
        result = data.getResult();
        diagnosis = new EcgDiagnosis(data.getDiagnosis().getBytes());
        hr = data.getHr();
        qrs = data.getQrs();
        pvcs = data.getPvcs();
        qtc = data.getQtc();
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public EcgDiagnosis getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(EcgDiagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getQrs() {
        return qrs;
    }

    public void setQrs(int qrs) {
        this.qrs = qrs;
    }

    public int getPvcs() {
        return pvcs;
    }

    public void setPvcs(int pvcs) {
        this.pvcs = pvcs;
    }

    public int getQtc() {
        return qtc;
    }

    public void setQtc(int qtc) {
        this.qtc = qtc;
    }

    @Override
    public String toString() {
        return "RtEcgResult{" +
                "result=" + result +
                ", diagnosis=" + diagnosis +
                ", hr=" + hr +
                ", qrs=" + qrs +
                ", pvcs=" + pvcs +
                ", qtc=" + qtc +
                '}';
    }
}
