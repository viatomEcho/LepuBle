package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.base.LpWorkManager
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

/**
 * author: wujuan
 * created on: 2021/1/27 10:22
 * description:
 */
class LewBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        write_uuid = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        notify_uuid = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
        LepuBleLog.d("LewBleManager initUUID")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
//        if (BleServiceHelper.BleServiceHelper.bleService.support2MPhy) {
        if (LpWorkManager.support2MPhy) {
            requestQueue.add(requestMtu(247)
                .with { device: BluetoothDevice?, mtu: Int ->
                    log(Log.INFO, "LewBleManager MTU set to $mtu")
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "LewBleManager Requested MTU not supported: $status")
                })
                .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                    .fail { device: BluetoothDevice?, status: Int ->
                        log(Log.WARN, "LewBleManager Requested PHY not supported: $status")
                    })
        } else {
            requestQueue.add(requestMtu(247)
                .with { device: BluetoothDevice?, mtu: Int ->
                    log(Log.INFO, "LewBleManager MTU set to $mtu")
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "LewBleManager Requested MTU not supported: $status")
                })
        }
        LepuBleLog.d("LewBleManager dealReqQueue")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("LewBleManager initialize")
    }
}