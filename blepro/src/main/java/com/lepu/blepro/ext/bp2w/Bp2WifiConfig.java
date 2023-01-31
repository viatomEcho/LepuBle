package com.lepu.blepro.ext.bp2w;

public class Bp2WifiConfig {
    private Bp2Wifi wifi;
    private Bp2wServer server;

    public Bp2Wifi getWifi() {
        return wifi;
    }

    public void setWifi(Bp2Wifi wifi) {
        this.wifi = wifi;
    }

    public Bp2wServer getServer() {
        return server;
    }

    public void setServer(Bp2wServer server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "Bp2WifiConfig{" +
                "wifi=" + wifi +
                ", server=" + server +
                '}';
    }
}
