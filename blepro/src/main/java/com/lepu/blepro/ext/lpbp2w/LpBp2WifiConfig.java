package com.lepu.blepro.ext.lpbp2w;

public class LpBp2WifiConfig {
    private LpBp2Wifi wifi;
    private LpBp2wServer server;

    public LpBp2Wifi getWifi() {
        return wifi;
    }

    public void setWifi(LpBp2Wifi wifi) {
        this.wifi = wifi;
    }

    public LpBp2wServer getServer() {
        return server;
    }

    public void setServer(LpBp2wServer server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "LpBp2WifiConfig{" +
                "wifi=" + wifi +
                ", server=" + server +
                '}';
    }
}
