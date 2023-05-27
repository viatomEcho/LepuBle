package com.lepu.blepro.ext.ventilator;

public class WifiConfig {
    private Wifi wifi;
    private Server server;

    public Wifi getWifi() {
        return wifi;
    }

    public void setWifi(Wifi wifi) {
        this.wifi = wifi;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "WifiConfig{" +
                "wifi=" + wifi +
                ", server=" + server +
                '}';
    }
}
