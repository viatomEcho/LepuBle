package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: chenyongfeng
 * created on: 2021/11/22 19:04
 * description:
 */
class Bpw1BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("69400001-b5a3-f393-e0a9-e50e24dcca99")
        write_uuid = UUID.fromString("69400002-b5a3-f393-e0a9-e50e24dcca99")
        notify_uuid = UUID.fromString("69400003-b5a3-f393-e0a9-e50e24dcca99")
        LepuBleLog.d("Bpw1BleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("Bpw1BleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("Bpw1BleManager dealReqQueue")
        return requestQueue
    }

}