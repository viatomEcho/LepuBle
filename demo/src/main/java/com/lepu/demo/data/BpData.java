package com.lepu.demo.data;

public class BpData {

    private String fileName;
    private int sys;
    private int dia;
    private int mean;
    private int pr;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    @Override
    public String toString() {
        return "BpData{" +
                "fileName='" + fileName + '\'' +
                ", sys=" + sys +
                ", dia=" + dia +
                ", mean=" + mean +
                ", pr=" + pr +
                '}';
    }
}
