package com.lepu.blepro.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 只负责发送O2获取实时消息
 */
public class O2RTEvent {


    private String action;
    private Object data;

    public O2RTEvent() {
    }
    public O2RTEvent(String action) {
        this.action = action;
    }


    public O2RTEvent(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public static void post(String actionKey, Object data) {
        EventBus.getDefault().post(new O2RTEvent(actionKey, data));
    }


    public static void post(String actionKey) {
        EventBus.getDefault().post(new O2RTEvent(actionKey));
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
