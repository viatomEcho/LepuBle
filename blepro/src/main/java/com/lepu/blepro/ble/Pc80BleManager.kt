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
class Pc80BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb")
        write_uuid = UUID.fromString("0000FFF2-0000-1000-8000-00805f9b34fb")
        notify_uuid = UUID.fromString("0000FFF1-0000-1000-8000-00805f9b34fb")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("PC80BleManager inited")
    }

}