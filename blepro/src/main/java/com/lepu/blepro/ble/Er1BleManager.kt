package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.ble.cmd.Er1BleCmd
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/1/27 10:22
 * description:
 */
class Er1BleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        write_uuid = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        notify_uuid = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        return requestQueue
    }

//    override fun dealReqQueue() {
//        beginAtomicRequestQueue()
//            .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
//                .with { device: BluetoothDevice?, mtu: Int ->
//                    log(
//                        Log.INFO,
//                        "MTU set to $mtu"
//                    )
//                }
//                .fail { device: BluetoothDevice?, status: Int ->
//                    log(
//                        Log.WARN,
//                        "Requested MTU not supported: $status"
//                    )
//                }) //                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//            //                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
//            //                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
//            .add(enableNotifications(notify_char))
//            .done { device: BluetoothDevice? ->
//                log(
//                    Log.INFO,
//                    "Target initialized"
//                )
//            }
//            .enqueue()
//    }


    override fun initialize() {
        if (!isUpdater)
            syncTime()
        LepuBleLog.d("Er1BleManager inited")
    }


    private fun syncTime() {
        LepuBleLog.d("Er1 manager init : to set time")
        sendCmd(Er1BleCmd.setTime());
    }
}