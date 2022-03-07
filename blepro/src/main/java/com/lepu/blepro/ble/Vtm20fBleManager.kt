package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class Vtm20fBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFE4-0000-1000-8000-00805F9B34FB")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("Vtm20fBleManager inited")
    }

}