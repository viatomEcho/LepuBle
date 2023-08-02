package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: chenyongfeng
 */
class Pf10Aw1BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("E8FB0001-A14B-98F9-831B-4E2941D01248")
        write_uuid = UUID.fromString("E8FB0002-A14B-98F9-831B-4E2941D01248")
        notify_uuid = UUID.fromString("E8FB0003-A14B-98F9-831B-4E2941D01248")
        LepuBleLog.d("Pf10Aw1BleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("Pf10Aw1BleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        requestQueue.add(requestMtu(247)
            .with { device: BluetoothDevice?, mtu: Int ->
                log(Log.INFO, "Pf10Aw1BleManager MTU set to $mtu")
            }
            .fail { device: BluetoothDevice?, status: Int ->
                log(Log.WARN, "Pf10Aw1BleManager Requested MTU not supported: $status")
            })
            .add(requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH))
        LepuBleLog.d("Pf10Aw1BleManager dealReqQueue")
        return requestQueue
    }

}