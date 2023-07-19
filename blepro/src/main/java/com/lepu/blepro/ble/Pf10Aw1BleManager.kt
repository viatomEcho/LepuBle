package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: chenyongfeng
 */
class Pf10Aw1BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("E8FBA14B-0001-98F9-831B-4E2941D01248")
        write_uuid = UUID.fromString("E8FBA14B-0002-98F9-831B-4E2941D01248")
        notify_uuid = UUID.fromString("E8FBA14B-0003-98F9-831B-4E2941D01248")
        LepuBleLog.d("Pf10Aw1BleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("Pf10Aw1BleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("Pf10Aw1BleManager dealReqQueue")
        return requestQueue
    }

}