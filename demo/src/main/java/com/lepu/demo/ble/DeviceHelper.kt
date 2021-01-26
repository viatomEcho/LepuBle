package com.lepu.demo.ble

import android.content.Context
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyBleResponse.OxyInfo
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.vals.*

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



    /**
     * 开始扫描
     *
     */
    fun startScan() {
       BleServiceHelper.startScan()
    }


    /**
     * 开始扫描,组合套装可用此方法来设置扫描条件
     * @param singleScanMode 是否只过滤出targetModel设备 , false时targetModel无效
     * @param targetModel 过滤的设备Model
     *
     */
    fun startScan(singleScanMode: Boolean, targetModel: Int) {
       BleServiceHelper.startScan(singleScanMode, targetModel)
    }

    fun hasUnbound(): Boolean{
        //todo
        return false
    }

    /**
     * 检查是否有未连接设备，如有开启扫描
     */
    fun hasUnConnected(): Boolean{
        return BleServiceHelper.hasUnConnected()
    }

    fun stopScan(){
        BleServiceHelper.stopScan()
    }

    fun clearVailFace(){
        BleServiceHelper.clearVailFace()
    }

    fun setInterface(model: Int, isClear: Boolean){
        BleServiceHelper.setInterfaces(model, isClear)
    }

    fun connect(context: Context, b: Bluetooth){
        BleServiceHelper.connect(context, b.model, b.device)
    }

    fun reconnect(model: Int){
        BleServiceHelper.reconnect(model)
    }

    fun reconnect(){

       BleServiceHelper.reconnect()


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
        LepuBleLog.d("绑定失败！！！")
        return false
    }

    fun bind(b: Bluetooth): Boolean{
        when(b.model){
            Bluetooth.MODEL_ER1 -> {
                bindEr1(b)
                return true
            }
            else ->{
                LepuBleLog.d("Warning: 没有此model！！！！")
            }
        }
        return false
    }

    private fun bindO2Ring(b: Bluetooth, oxyInfo: OxyInfo) {
        oxyName = b.name
        oxySn = oxyInfo.sn
        hasOxy = true
    }

    private fun bindEr1(b: Bluetooth) {
        er1Name = b.name
        er1Sn = b.name
        hasEr1 = true
    }

}