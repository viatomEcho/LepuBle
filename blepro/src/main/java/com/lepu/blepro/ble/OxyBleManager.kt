package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/1/27 10:25
 * description:
 */
class OxyBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        write_uuid = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        notify_uuid = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
        LepuBleLog.d("OxyBleManager initUUID")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        requestQueue.add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
        LepuBleLog.d("OxyBleManager dealReqQueue")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("OxyBleManager initialize")
    }
}