package com.lepu.demo.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.Bp2BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.service.BleService
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth

class LpBleUtil {
    interface State {
        companion object {
            /**
             * 不明 -1
             */
            const val UNKNOWN = Ble.State.UNKNOWN

            /**
             * 已连接 1
             */
            const val CONNECTED = Ble.State.CONNECTED

            /**
             *  未连接 2
             */
            const val DISCONNECTED = Ble.State.DISCONNECTED

            /**
             * 连接中 3
             */
            const val CONNECTING = Ble.State.CONNECTING
        }
    }

    interface StateStr {
        companion object {
            //不明
            const val UNKNOWN = "未知"

            //已连接
            const val CONNECTED = "已连接"

            // 未连接
            const val DISCONNECTED = "未连接"

            //连接中
            const val CONNECTING = "连接中"
        }
    }





    companion object {

        const val TAG : String = "LpBleUtil"




        fun convertState(state: Int): String {
            return when (state) {
                State.CONNECTED -> StateStr.CONNECTED
                State.DISCONNECTED -> StateStr.DISCONNECTED
                State.CONNECTING -> StateStr.CONNECTING
                else -> return StateStr.UNKNOWN
            }

        }

        fun getServiceHelper(): BleServiceHelper {
            return BleServiceHelper.BleServiceHelper
        }

        fun setInterface(model: Int){
            BleServiceHelper.BleServiceHelper.getInterfaces()?.clear()
            BleServiceHelper.BleServiceHelper.setInterfaces(model)
        }

        fun  getInterface(model: Int): BleInterface?{

           return BleServiceHelper.BleServiceHelper.getInterface(model)
        }

        /**
         * 重启蓝牙后调用
         */
        fun reInitBle() {
            BleServiceHelper.BleServiceHelper.reInitBle()
        }

        @JvmOverloads
        fun startScan(scanModel: Int, needPair: Boolean = false) {

            BleServiceHelper.BleServiceHelper.startScan(scanModel, needPair)
        }

        @JvmOverloads
        fun startScan(scanModel: IntArray, needPair: Boolean = false) {

            BleServiceHelper.BleServiceHelper.startScan(scanModel, needPair)
        }


        fun stopScan() {
            Log.d(TAG, "stopScan...")
            BleServiceHelper.BleServiceHelper.stopScan()
        }

        /**
         * 连接蓝牙
         * @param context Context
         * @param b Bluetooth
         * @param isAutoConnect Boolean 此次连接后，若自然断开是否再次扫描重连
         */
        @JvmOverloads
        fun connect(context: Context, b: Bluetooth, isAutoConnect: Boolean = true, toConnectUpdater: Boolean = false) {
            Log.d(TAG, "connect...${b.model}, ${b.name}, isAutoConnect = $isAutoConnect, withUpdater = $toConnectUpdater")
            BleServiceHelper.BleServiceHelper.connect(context, b.model, b.device, isAutoConnect, toConnectUpdater)
        }

        @JvmOverloads
        fun connect(context: Context, model: Int, b: BluetoothDevice, isAutoConnect: Boolean = true, toConnectUpdater: Boolean = false) {
            BleServiceHelper.BleServiceHelper.connect(context, model, b, isAutoConnect, toConnectUpdater)
        }


        /**
         * 发起重连，通过蓝牙名发起请求连接 必须已绑定
         * 应用场景：1.进入设备首页，如果已经绑定立即发起重连 2.某个model被切换至当前时立即发起重连
         * @param model Int
         * @param name String
         */
        @JvmOverloads
        fun reconnect(model: Int, name: String, toConnectUpdater: Boolean = false) {
            Log.d(TAG, "reconnect...$model, name = $name")
            name.isNullOrEmpty().let { it1 ->
                if (it1){
                    Log.d(TAG, "error: name")
                    return
                }
                //检查必须要有interface
                if (isDisconnected(model)) {
                    Log.d(TAG, "去重连...")
                    BleServiceHelper.BleServiceHelper.reconnect(model, name, toConnectUpdater)
                }else{
                    Log.d(TAG, "蓝牙处于连接状态，reconnect 不往下进行 ")
                }
            }

        }



        /**
         * 应用场景： 切换设备时，如果设备已经绑定可根据保存的设备mac地址自动连接设备
         * @param model Int
         * @param macAddress String
         * @param toConnectUpdater Boolean 扫描时检查扫描到的address 是否是传入的（绑定的address）的新地址
         */
        @JvmOverloads
        fun reconnectByMac(model: Int, macAddress: String) {
            if(macAddress.isNotEmpty() && BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                BleServiceHelper.BleServiceHelper.getConnectState(model).let {
                    if (it == State.DISCONNECTED)
                        BleServiceHelper.BleServiceHelper.reconnectByAddress(model, macAddress)
                }

            }
        }

        fun connectUpdaterByMac(model: Int, macAddress: String) {
            if(macAddress.isNotEmpty() && BluetoothAdapter.checkBluetoothAddress(macAddress)) {

                BleServiceHelper.BleServiceHelper.reconnectByAddress(model, macAddress, true)
            }
        }



        /**
         * 断开所有model
         * @param autoReconnect Boolean
         */
        fun disconnect(autoReconnect: Boolean) {
            Log.d(TAG, "disconnect all")
            BleServiceHelper.BleServiceHelper.disconnect(autoReconnect)
        }



        /**
         * 应用场景：切换设备时调用此方法，并且应该指定autoReconnect = false
         * @param model Int 要断开的model
         * @param autoReconnect Boolean 值 =true时，当蓝牙断开后会马上开启扫描尝试连接该model
         */
        fun disconnect(model: Int, autoReconnect: Boolean) {
            BleServiceHelper.BleServiceHelper.disconnect(model, autoReconnect)
        }



        /**
         * 设备蓝牙的连接状态：  未连接时不能代表绑定状态
         */
        fun getBleState(model: Int): Int {
            return BleServiceHelper.BleServiceHelper.getConnectState(model)
        }

        /**
         *
         * @param model Int
         */
        fun getInfo(model: Int) {
            Log.d(TAG, "getInfo")
            BleServiceHelper.BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.BleServiceHelper.getInfo(model)
            }

        }


        /**
         *
         * @param model Int
         */
        fun getFileList(model: Int){
            BleServiceHelper.BleServiceHelper.getFileList(model)

        }

        /**
         * 注意： bleSdk 不负责在读文件/更新设置操作时，停止实时任务，由APP自己处理
         * @param userId String
         * @param fileName String
         * @param model Int
         * @param offset Int
         */
        @JvmOverloads
        fun readFile(userId: String, fileName: String, model: Int, offset: Int = 0) {
            BleServiceHelper.BleServiceHelper.readFile(userId, fileName, model, offset)

        }

        /**
         *
         * @param model Int
         */
        fun stopRtTask(model: Int) {
            BleServiceHelper.BleServiceHelper.stopRtTask(model)
        }

        fun stopRtTask(models: IntArray) {
            for (m in models){
                BleServiceHelper.BleServiceHelper.stopRtTask(m)
            }
        }

        @JvmOverloads
        fun startRtTask(model: Int, delayMillis: Long = 200) {
            BleServiceHelper.BleServiceHelper.setRTDelayTime(model, delayMillis)
            BleServiceHelper.BleServiceHelper.startRtTask(model)
        }


        /**
         *
         * @param model Int
         * @return Boolean
         */
        fun isBleConnected(model: Int): Boolean{
            return getBleState(model) == State.CONNECTED
        }

        fun isBleConnected(models: IntArray): Boolean{
            for (m in models){
                if (!isBleConnected(m)) return false
            }
            return true
        }

        fun unconnectedModels(models: IntArray): ArrayList<Int>{
            return arrayListOf<Int>().apply {
                for (m in models){
                    if (!isBleConnected(m)) this.add(m)
                }
            }
        }


        fun isDisconnected(model: Int): Boolean{
            return getBleState(model) == State.DISCONNECTED
        }


        fun isAutoConnect(model: Int): Boolean{
           return getInterface(model)?.isAutoReconnect ?: false
        }


        fun bp2SwitchState(model: Int, state: Int){
           BleServiceHelper.BleServiceHelper.bp2SwitchState(model, state)

        }

        fun oxyGetPpgRt(model: Int){
            BleServiceHelper.BleServiceHelper.oxyGetPpgRt(model)
        }


    }




}