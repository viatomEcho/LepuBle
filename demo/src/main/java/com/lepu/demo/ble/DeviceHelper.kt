package com.lepu.demo.ble

import android.content.Context
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyBleResponse.OxyInfo
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.vals.hasOxy
import com.lepu.blepro.vals.oxyName
import com.lepu.blepro.vals.oxySn

/**
 * author: wujuan
 * created on: 2021/1/22 10:38
 * description:
 */
object DeviceHelper {
    interface State{
        companion object{
            //不明
            const val UNKNOWN = -1

            /**
             * 未绑定
             */
            const val UNBOUND= 0

            //已连接
            const val CONNECTED = 1

            // 未连接
            const val DISCONNECTED = 2

            //连接中
            const val CONNECTING = 3
        }
    }

    interface StateStr{
        companion object{
            //不明
            const val UNKNOWN = "未知"
            /**
             * 未绑定
             */
            const val UNBOUND= "未绑定"

            //已连接
            const val CONNECTED = "已连接"

            // 未连接
            const val DISCONNECTED = "未连接"

            //连接中
            const val CONNECTING = "连接中"
        }
    }

    fun convertState(state: Int): String{
        return when(state){
            State.UNBOUND -> StateStr.UNBOUND
            State.CONNECTED -> StateStr.CONNECTED
            State.DISCONNECTED -> StateStr.DISCONNECTED
            State.CONNECTING -> StateStr.CONNECTING
            else -> return StateStr.UNKNOWN
        }

    }
    fun setInterface(model: IntArray){
        BleServiceHelper.setInterfaces(model)
    }

    fun connect(context: Context, b: Bluetooth){
        BleServiceHelper.connect(context, b.model, b.device)
    }

    fun reconnect(model: Int){
        BleServiceHelper.reconnect(model)
    }

    fun disconnect(autoReconnect: Boolean){
        BleServiceHelper.disconnect(autoReconnect)
    }

    fun disconnect(model: Int, autoReconnect: Boolean){
        BleServiceHelper.disconnect(model, autoReconnect)
    }


    fun bind(info: Any, b: Bluetooth): Boolean{
        LepuBleLog.d("start binding...")
        when (b.model) {
            Bluetooth.MODEL_O2RING -> {
                //真正绑定执行
                val oxyInfo = info as OxyInfo
                bindO2Ring(b, oxyInfo)
                return true
            }
        }
        return false
    }

    private fun bindO2Ring(b: Bluetooth, oxyInfo: OxyInfo) {
        oxyName = b.name
        oxySn = oxyInfo.sn
        hasOxy = true
    }

}