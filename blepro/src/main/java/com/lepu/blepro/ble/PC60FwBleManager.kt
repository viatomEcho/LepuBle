package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.Er1BleCmd
import com.lepu.blepro.utils.LepuBleLog
import java.util.*

/**
 * author: wujuan
 * created on: 2021/719 10:22
 * description:
 */
class PC60FwBleManager(context: Context): BaseBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        write_uuid = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        notify_uuid = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    }

    override fun initReqQueue() {
        beginAtomicRequestQueue()
            .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                .with { device: BluetoothDevice?, mtu: Int ->
                    log(
                        Log.INFO,
                        "MTU set to $mtu"
                    )
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Requested MTU not supported: $status"
                    )
                }) //                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
            //                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
            //                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
            .add(enableNotifications(notify_char))
            .done { device: BluetoothDevice? ->
                log(
                    Log.INFO,
                    "Target initialized"
                )
            }
            .enqueue()
    }
    override fun init() {
        if (!isUpdater)
            LepuBleLog.d("PC60FW BleManager inited")
    }

}