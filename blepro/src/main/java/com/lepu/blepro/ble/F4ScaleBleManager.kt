package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class F4ScaleBleManager(context: Context): LpBleManager(context) {

    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFB0-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FFB1-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFB2-0000-1000-8000-00805F9B34FB")
//        indicate_uuid = UUID.fromString("0000FFB3-0000-1000-8000-00805F9B34FB")
    }

    override fun initialize() {
        LepuBleLog.d("F4ScaleBleManager inited")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

}