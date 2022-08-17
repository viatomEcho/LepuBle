package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/7/19 10:22
 * description:
 */
class Pc60FwBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        write_uuid = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        notify_uuid = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        requestQueue.add(requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH))
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("PC60FW BleManager inited")
    }

}