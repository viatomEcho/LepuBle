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
class Ad5FhrBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFE4-0000-1000-8000-00805F9B34FB")

        /*service_uuid = UUID.fromString("0000FFE5-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FFE9-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")*/
    }

    override fun initialize() {
        LepuBleLog.d("Ad5FhrBleManager inited")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

}