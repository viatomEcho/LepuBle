package com.lepu.demo.ble

import android.content.Context
import com.lepu.blepro.ble.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyBleResponse.OxyInfo
import com.lepu.blepro.event.BleProEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LogUtils
import com.lepu.blepro.vals.hasOxy
import com.lepu.blepro.vals.oxyName
import com.lepu.blepro.vals.oxySn
import com.lepu.demo.O2RingEvent
import java.lang.String

/**
 * author: wujuan
 * created on: 2021/1/19 11:26
 * description:
 */
class DeviceHelper {
    companion object{
        fun connectO2(context: Context, bluetoothDevice: Bluetooth) {
            BleServiceHelper.connectO2(context, bluetoothDevice.device)
        }

        /**
         * 收到执行绑定设置的通知（EventOxyInfo）
         *
         * @param event
         */
        fun bindO2(
            event: BleProEvent,
            currentModel: Int,
            currentBluetooth: Bluetooth?
        ) {
            if (currentBluetooth == null || currentBluetooth.model != currentModel) return
            LogUtils.d("start binding...")
            val oxyInfo = event.data as OxyInfo
            when (currentModel) {
                Bluetooth.MODEL_O2RING -> {
                    //真正绑定执行
                    bindO2Ring(currentBluetooth, oxyInfo)

                    LogUtils.d("bind success:${currentBluetooth.name} , ${oxyInfo.sn}")
                    O2RingEvent.post(O2RingEvent.O2UIBindFinish, currentBluetooth)
                }
            }
        }


        private fun bindO2Ring(b: Bluetooth, oxyInfo: OxyInfo) {
            oxyName = b.name
            oxySn = oxyInfo.sn
            hasOxy = true
        }
    }



}