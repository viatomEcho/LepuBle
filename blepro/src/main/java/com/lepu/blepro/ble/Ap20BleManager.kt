package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class Ap20BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFB0-0000-1000-8000-00805f9b34fb")
        write_uuid = UUID.fromString("0000FFB2-0000-1000-8000-00805f9b34fb")
        notify_uuid = UUID.fromString("0000FFB2-0000-1000-8000-00805f9b34fb")
        LepuBleLog.d("Ap20BleManager initUUID")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("Ap20BleManager dealReqQueue")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("Ap20BleManager initialize")
    }

}