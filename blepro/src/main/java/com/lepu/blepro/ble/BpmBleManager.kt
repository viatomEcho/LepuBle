package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.ble.cmd.BpmBleCmd
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/2/5 15:14
 * description:
 */
class BpmBleManager(context: Context): LpBleManager(context) {

    override fun initUUID() {
        service_uuid = UUID.fromString("000018F0-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("00002AF1-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("00002AF0-0000-1000-8000-00805F9B34FB")
        LepuBleLog.d("BpmBleManager initUUID ")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("BpmBleManager dealReqQueue ")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("BpmBleManager initialize ")
    }

}