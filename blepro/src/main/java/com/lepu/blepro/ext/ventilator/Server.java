package com.lepu.blepro.ext.ventilator;

public class Server {
    private int state;         // 0:断开 1:连接中 2:已连接 0xff:服务器无法连接
    private int addrType;      // 服务器地址类型  0:ipv4  1:域名形式
    private int addrLen;
    private String addr;       // 服务器地址 e.g. “192.168.1.33”
    private int port;          // 服务器端口号

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getAddrType() {
        return addrType;
    }

    public void setAddrType(int addrType) {
        this.addrType = addrType;
    }

    public int getAddrLen() {
        return addrLen;
    }

    public void setAddrLen(int addrLen) {
        this.addrLen = addrLen;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Server{" +
                "state=" + state +
                ", addrType=" + addrType +
                ", addrLen=" + addrLen +
                ", addr='" + addr + '\'' +
                ", port=" + port +
                '}';
    }
}
