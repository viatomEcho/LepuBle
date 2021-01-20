package com.lepu.blepro.event;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class BleProEvent {


    private String action;
    private Object data;

    public BleProEvent() {
    }
    public BleProEvent(String action) {
        this.action = action;
    }


    public BleProEvent(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public static void post(String actionKey, Object data) {
        EventBus.getDefault().post(new BleProEvent(actionKey, data));
    }


    public static void post(String actionKey) {
        EventBus.getDefault().post(new BleProEvent(actionKey));
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public Object getData() {
        return data;
    }
}
