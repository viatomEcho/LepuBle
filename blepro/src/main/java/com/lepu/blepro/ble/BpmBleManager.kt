package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.BpmBleCmd
import com.lepu.blepro.utils.LepuBleLog
import java.util.*

/**
 * author: wujuan
 * created on: 2021/2/5 15:14
 * description:
 */
class BpmBleManager(context: Context): BaseBleManager(context) {

    override fun initUUID() {
        service_uuid =
            UUID.fromString("000018F0-0000-1000-8000-00805F9B34FB")
        write_uuid =
            UUID.fromString("00002AF1-0000-1000-8000-00805F9B34FB")
        notify_uuid =
            UUID.fromString("00002AF0-0000-1000-8000-00805F9B34FB")
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
        LepuBleLog.d("BpmBleManager inited ")
    }
    private fun syncTime() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_SET_TIME));
    }


}