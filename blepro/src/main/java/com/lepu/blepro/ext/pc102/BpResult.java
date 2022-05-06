package com.lepu.blepro.ext.pc102;

public class BpResult {
    private int sys;
    private int result;
    private String resultMess;
    private int map;
    private int dia;
    private int pr;

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultMess() {
        return resultMess;
    }

    public void setResultMess(String resultMess) {
        this.resultMess = resultMess;
    }

    public int getMap() {
        return map;
    }

    public void setMap(int map) {
        this.map = map;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    @Override
    public String toString() {
        return "BpResult{" +
                "sys=" + sys +
                ", result=" + result +
                ", resultMess='" + resultMess + '\'' +
                ", map=" + map +
                ", dia=" + dia +
                ", pr=" + pr +
                '}';
    }
}
