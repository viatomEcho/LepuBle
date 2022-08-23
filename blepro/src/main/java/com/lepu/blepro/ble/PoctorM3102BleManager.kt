package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class PoctorM3102BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        /*service_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1912")
        write_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B12")
        notify_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B12")*/
        service_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1910")
        write_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B11")
        notify_uuid = UUID.fromString("00010203-0405-0607-0809-0A0B0C0D2B10")
    }

    override fun initialize() {
        LepuBleLog.d("PoctorM3102BleManager inited")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

}