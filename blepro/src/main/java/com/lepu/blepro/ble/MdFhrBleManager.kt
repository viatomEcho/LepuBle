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
class MdFhrBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("00005500-d102-11e1-9b23-00025b00a5a5")
        write_uuid = UUID.fromString("00005501-d102-11e1-9b23-00025b00a5a5")
        notify_uuid = UUID.fromString("00005501-d102-11e1-9b23-00025b00a5a5")
        LepuBleLog.d("MdFhrBleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("MdFhrBleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("MdFhrBleManager dealReqQueue")
        return requestQueue
    }

}