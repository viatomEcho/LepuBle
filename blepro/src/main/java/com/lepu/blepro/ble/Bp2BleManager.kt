package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.BpmBleCmd
import com.lepu.blepro.utils.LepuBleLog
import java.util.*

/**
 * author: wujuan
 * created on: 2021/2/5 15:14
 * description:
 */
class Bp2BleManager(context: Context): BaseBleManager(context) {

    override fun initUUID() {
        service_uuid =
            UUID.fromString("14839AC4-7D7E-415C-9A42-167340CF2339")
        write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
    }

    override fun initReqQueue() {

        beginAtomicRequestQueue()
            // .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
            //                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
            //                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
            //                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
            //                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
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
            syncTime()
            //            syncTime()
        LepuBleLog.d("BpmBleManager inited ")
    }
    private fun syncTime() {
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_SET_TIME));
    }

    private fun getInfo() {
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_GET_INFO));
    }


}