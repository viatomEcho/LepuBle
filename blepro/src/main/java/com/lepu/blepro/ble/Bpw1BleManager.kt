package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/1/27 10:22
 * description:
 */
class Bpw1BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("69400001-b5a3-f393-e0a9-e50e24dcca99")
        write_uuid = UUID.fromString("69400002-b5a3-f393-e0a9-e50e24dcca99")
        notify_uuid = UUID.fromString("69400003-b5a3-f393-e0a9-e50e24dcca99")
    }

    override fun initialize() {
        LepuBleLog.d("Bpw1BleManager inited")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

}