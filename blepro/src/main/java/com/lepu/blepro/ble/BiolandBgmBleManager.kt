package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class BiolandBgmBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        /*service_uuid = UUID.fromString("0000FF00-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")*/
        LepuBleLog.d("BiolandBgmBleManager initUUID")
        service_uuid = UUID.fromString("00001000-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("00001003-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("00001002-0000-1000-8000-00805F9B34FB")
    }

    override fun initialize() {
        LepuBleLog.d("BiolandBgmBleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("BiolandBgmBleManager dealReqQueue")
        return requestQueue
    }

}