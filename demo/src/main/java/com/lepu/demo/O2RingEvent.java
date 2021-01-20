package com.lepu.demo;

import org.greenrobot.eventbus.EventBus;

/**
 * author: wujuan
 * created on: 2020/12/11 14:31
 * description:
 */
public class O2RingEvent {
    public static final String SYNC_RECORD_REALM_LIST_COMPLETE = "sync_realm_record_list_complete";

    /**
     * UI
     */
    public static final String O2UIConnectingLoading = "connecting_loading";
    public static final String O2UIBindFinish = "bind_finish";


    private String action;
    private Object data;




    public O2RingEvent(String action) {
        this.action = action;
    }

    public O2RingEvent(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public static void post(String actionKey, Object data) {
        EventBus.getDefault().post(new O2RingEvent(actionKey, data));
    }

    public static void post(String actionKey) {
        EventBus.getDefault().post(new O2RingEvent(actionKey));
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
