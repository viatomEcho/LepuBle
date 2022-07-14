package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class Pf20BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        write_uuid = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        notify_uuid = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        LepuBleLog.d("Pf20BleManager initUUID")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        requestQueue.add(requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH))
        LepuBleLog.d("Pf20BleManager dealReqQueue")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("Pf20BleManager initialize")
    }

}