package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: chenyongfeng
 * created on: 2021/11/18 18:41
 * description:
 */
class FhrBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000AE30-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000AE01-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000AE02-0000-1000-8000-00805F9B34FB")
        LepuBleLog.d("FhrBleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("FhrBleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("FhrBleManager dealReqQueue")
        return requestQueue
    }

}