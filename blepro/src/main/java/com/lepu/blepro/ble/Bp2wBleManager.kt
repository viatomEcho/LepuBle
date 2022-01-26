package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: chenyongfeng
 * created on: 2022/1/25 15:14
 * description:
 */
class Bp2wBleManager(context: Context): LpBleManager(context) {

    override fun initUUID() {
        service_uuid =
            UUID.fromString("14839AC4-7D7E-415C-9A42-167340CF2339")
        write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        if (BleServiceHelper.BleServiceHelper.bleService.support2MPhy) {
            requestQueue.add(requestMtu(247)
                .with { device: BluetoothDevice?, mtu: Int ->
                    log(Log.INFO, "Bp2wBleManager MTU set to $mtu")
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "Bp2wBleManager Requested MTU not supported: $status")
                })
                .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                    .fail { device: BluetoothDevice?, status: Int ->
                        log(Log.WARN, "Bp2wBleManager Requested PHY not supported: $status")
                    })
                .add(requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH))
        } else {
            requestQueue.add(requestMtu(247)
                .with { device: BluetoothDevice?, mtu: Int ->
                    log(Log.INFO, "Bp2wBleManager MTU set to $mtu")
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "Bp2wBleManager Requested MTU not supported: $status")
                })
                .add(requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH))
        }
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("Bp2wBleManager inited ")
    }

}