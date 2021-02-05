package com.lepu.demo.ble

import android.app.Application
import android.bluetooth.BluetoothDevice
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/28 19:27
 * description:
 */
class BleUtilService{
    interface State{
        companion object{
            //不明
            const val UNKNOWN = -1

            /**
             * 未绑定
             */
            const val UNBOUND= 0

            //已连接
            const val CONNECTED = Ble.State.CONNECTED

            // 未连接
            const val DISCONNECTED = Ble.State.DISCONNECTED

            //连接中
            const val CONNECTING = Ble.State.CONNECTING
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

companion object {
    fun convertState(state: Int): String {
        return when (state) {
            State.UNBOUND -> StateStr.UNBOUND
            State.CONNECTED -> StateStr.CONNECTED
            State.DISCONNECTED -> StateStr.DISCONNECTED
            State.CONNECTING -> StateStr.CONNECTING
            else -> return StateStr.UNKNOWN
        }

    }


    fun setInterface(model: Int, runRtImmediately: Boolean = false) {
        BleServiceHelper.BleServiceHelper.setInterfaces(model, runRtImmediately)
    }

    fun reInitBle() {
        BleServiceHelper.BleServiceHelper.reInitBle()
    }

    fun startScan(targetModel: Int, needPair: Boolean = false) {
        BleServiceHelper.BleServiceHelper.startScan(targetModel, needPair)
    }

    fun startScan(targetModel: IntArray, needPair: Boolean = false) {
        BleServiceHelper.BleServiceHelper.startScan(targetModel, needPair)
    }



    fun stopScan() {
        BleServiceHelper.BleServiceHelper.stopScan()
    }


    fun connect(context: Application, b: Bluetooth) {
        BleServiceHelper.BleServiceHelper.connect(context, b.model, b.device)
    }

    fun connect(context: Application, model: Int, b: BluetoothDevice) {
        BleServiceHelper.BleServiceHelper.connect(context, model, b)
    }

    fun reconnect(model: IntArray, name : Array<String>) {
        BleServiceHelper.BleServiceHelper.reconnect(model, name)
    }

    fun reconnect(model: Int, name : String) {
        BleServiceHelper.BleServiceHelper.reconnect(model, name)
    }

    fun disconnect(autoReconnect: Boolean) {
        BleServiceHelper.BleServiceHelper.disconnect(autoReconnect)
    }

    fun disconnect(model: Int, autoReconnect: Boolean) {
        BleServiceHelper.BleServiceHelper.disconnect(model, autoReconnect)
    }

    fun getBleState(model: Int): Int {
        return BleServiceHelper.BleServiceHelper.getConnectState(model)
    }

    fun getInfo(model: Int) {
        BleServiceHelper.BleServiceHelper.getInfo(model)
    }

    fun readFile(userId: String, fileName: String, model: Int) {
        BleServiceHelper.BleServiceHelper.readFile(userId, fileName, model)
    }

    fun stopRtTask(model: Int) {
        BleServiceHelper.BleServiceHelper.stopRtTask(model)
    }

    fun startRtTask(model: Int) {
        BleServiceHelper.BleServiceHelper.startRtTask(model)
    }

    fun syncData(model: Int, type: String, value: Any) {
        BleServiceHelper.BleServiceHelper.updateSetting(model, type, value)
    }

    fun reset(model: Int) {
        BleServiceHelper.BleServiceHelper.reset(model)
    }

    fun bind(info: Any, b: Bluetooth): Boolean {
        LepuBleLog.d("start binding...")
        when (b.model) {
            Bluetooth.MODEL_O2RING -> {
                //真正绑定执行
                bindO2Ring(b, info as OxyBleResponse.OxyInfo)
                return true
            }
            Bluetooth.MODEL_ER1 -> {
                bindEr1(b, info as LepuDevice)
                return true
            }
            else -> {
                LepuBleLog.d("Warning: 没有此model!!")
            }

        }
        LepuBleLog.d("绑定失败!!")
        return false
    }

    private fun bindO2Ring(b: Bluetooth, oxyInfo: OxyBleResponse.OxyInfo) {
    }

    private fun bindEr1(b: Bluetooth, info: LepuDevice) {
    }
}

}