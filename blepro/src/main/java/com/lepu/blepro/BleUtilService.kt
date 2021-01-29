package com.lepu.blepro

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.SparseArray
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.base.BleExport
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/28 19:27
 * description:
 */
object BleUtilService: BleExport {
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

    fun convertState(state: Int): String{
        return when(state){
            State.UNBOUND -> StateStr.UNBOUND
            State.CONNECTED -> StateStr.CONNECTED
            State.DISCONNECTED -> StateStr.DISCONNECTED
            State.CONNECTING -> StateStr.CONNECTING
            else -> return StateStr.UNKNOWN
        }

    }

    override fun initService(application: Application, observer: BleServiceObserver?): BleExport {
        BleServiceHelper.initService(application, observer)
        return this
    }

    override fun setRawFolder(folders: SparseArray<String>): BleExport {
        BleServiceHelper.initRawFolder(folders)
        return this
    }

    override fun setInterfaces(model: Int, isClear: Boolean, runRtImmediately: Boolean): BleExport {
        BleServiceHelper.setInterfaces(model, isClear, runRtImmediately)
        return this
    }

    /**
     * 是否打印log
     */
    override fun setLog(log: Boolean): BleExport {
        LepuBleLog.setDebug(log)
        return this
    }

    override fun reInitBle(): BleExport {
        BleServiceHelper.reInitBle()
        return this
    }

    override fun startScan() {
        BleServiceHelper.startScan()
    }

    override fun startScan(p: Boolean) {
        BleServiceHelper.startScan(p)
    }

    override fun startScan(singleScanMode: Boolean, targetModel: Int, p: Boolean) {
        BleServiceHelper.startScan(singleScanMode, targetModel, p)
    }

    override fun hasUnConnected(): Boolean {
        return BleServiceHelper.hasUnConnected()
    }

    override fun stopScan() {
        BleServiceHelper.stopScan()
    }

    override fun setInterface(model: Int, isClear: Boolean, runRtImmediately: Boolean) {
        BleServiceHelper.setInterfaces(model, isClear, runRtImmediately)
    }

    override fun connect(context: Context, b: Bluetooth) {
        BleServiceHelper.connect(context, b.model, b.device)
    }

    override fun connect(context: Context, model: Int, b: BluetoothDevice) {
        BleServiceHelper.connect(context, model, b)
    }

    override fun reconnect(model: Int) {
        BleServiceHelper.reconnect(model)
    }

    override fun reconnect() {
        BleServiceHelper.reconnect()
    }

    override fun disconnect(autoReconnect: Boolean) {
        BleServiceHelper.disconnect(autoReconnect)
    }

    override fun disconnect(model: Int, autoReconnect: Boolean) {
        BleServiceHelper.disconnect(model, autoReconnect)
    }

    override fun getBleState(model: Int): Int {
       return BleServiceHelper.getConnectState(model)
    }

    override fun getInfo(model: Int) {
        BleServiceHelper.getInfo(model)
    }

    override fun readFile(userId: String, fileName: String, model: Int) {
        BleServiceHelper.readFile(userId, fileName, model)
    }

    override fun stopRtTask(model: Int) {
        BleServiceHelper.stopRtTask(model)
    }

    override fun startRtTask(model: Int) {
        BleServiceHelper.startRtTask(model)
    }

    override fun syncData(model: Int, type: String, value: Any) {
        BleServiceHelper.syncData(model, type, value)
    }

    override fun reset(model: Int) {
        BleServiceHelper.reset(model)
    }

    fun bind(info: Any, b: Bluetooth): Boolean{
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
            else ->{
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