package com.lepu.demo.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.BuildConfig
import com.lepu.demo.cofig.Constant.BluetoothConfig

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
            return BleServiceHelper
        }


        /**
         * 初始化蓝牙服务
         * @param application Application
         */
        fun initBle(application: Application) {

            getServiceHelper()
                .initLog(BuildConfig.DEBUG)
//                .initModelConfig(Constant.BluetoothConfig.SUPPORT_FACES) // 配置要支持的设备

                .initService(
                    application,
                    BleSO.getInstance(application)
                ) //必须在initModelConfig initRawFolder之后调用
        }

        fun stopService(application: Application) {
            BleServiceHelper.stopService(application)
        }

        fun clearInterface(){
            BleServiceHelper.getInterfaces()?.let {
                it.clear()
            }
        }


        fun setInterface(model: Int, needClear: Boolean){
            clearInterface()
            BleServiceHelper.setInterfaces(model)
        }

        fun  getInterface(model: Int): BleInterface?{

           return BleServiceHelper.getInterface(model)
        }

        /**
         * 重启蓝牙后调用
         */
        fun reInitBle() {
            BleServiceHelper.reInitBle()
        }

        @JvmOverloads
        fun startScan(scanModel: Int? = null, needPair: Boolean = false) {
            BleServiceHelper.startScan(scanModel, needPair)
        }

        @JvmOverloads
        fun startScan(scanModel: IntArray, needPair: Boolean = false) {
            BleServiceHelper.startScan(scanModel, needPair)
        }

        fun startScanByName(deviceName: String, scanModel: Int? = null) {
            BleServiceHelper.startScanByName(deviceName, scanModel)
        }
        fun startScanByAddress(address: String, scanModel: Int? = null) {
            BleServiceHelper.startScanByAddress(address, scanModel)
        }

        fun stopScan() {
            Log.d(TAG, "stopScan...")
            BleServiceHelper.stopScan()
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
            BleServiceHelper.connect(context, b.model, b.device, isAutoConnect, toConnectUpdater)
        }

        @JvmOverloads
        fun connect(context: Context, model: Int, b: BluetoothDevice, isAutoConnect: Boolean = true, toConnectUpdater: Boolean = false) {
            BleServiceHelper.connect(context, model, b, isAutoConnect, toConnectUpdater)
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
                    BleServiceHelper.reconnect(model, name, toConnectUpdater)
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
                BleServiceHelper.getConnectState(model).let {
                    if (it == State.DISCONNECTED)
                        BleServiceHelper.reconnectByAddress(model, macAddress)
                }

            }
        }

        fun connectUpdaterByMac(model: Int, macAddress: String) {
            if(macAddress.isNotEmpty() && BluetoothAdapter.checkBluetoothAddress(macAddress)) {

                BleServiceHelper.reconnectByAddress(model, macAddress, true)
            }
        }



        /**
         * 断开所有model
         * @param autoReconnect Boolean
         */
        fun disconnect(autoReconnect: Boolean) {
            Log.d(TAG, "disconnect all")
            BleServiceHelper.disconnect(autoReconnect)
        }



        /**
         * 应用场景：切换设备时调用此方法，并且应该指定autoReconnect = false
         * @param model Int 要断开的model
         * @param autoReconnect Boolean 值 =true时，当蓝牙断开后会马上开启扫描尝试连接该model
         */
        fun disconnect(model: Int, autoReconnect: Boolean) {
            BleServiceHelper.disconnect(model, autoReconnect)
        }



        /**
         * 设备蓝牙的连接状态：  未连接时不能代表绑定状态
         */
        fun getBleState(model: Int): Int {
            return BleServiceHelper.getConnectState(model)
        }

        fun getSendCmd(model: Int): String {
            return BleServiceHelper.getSendCmd(model)
        }

        fun getBleMtu(model: Int): Int {
            return BleServiceHelper.getBleMtu(model)
        }
        fun setBleMtu(model: Int, mtu: Int) {
            BleServiceHelper.setBleMtu(model, mtu)
        }
        fun setTime(model: Int) {
            BleServiceHelper.syncTime(model)
        }

        /**
         *
         * @param model Int
         */
        fun getInfo(model: Int) {
            Log.d(TAG, "getInfo")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.getInfo(model)
            }

        }
        fun oxyGetBoxInfo(model: Int) {
            Log.d(TAG, "getBoxInfo")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.oxyGetBoxInfo(model)
            }

        }
        fun updateSetting(model: Int, type: String, value: Any) {
            Log.d(TAG, "updateSetting")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.updateSetting(model, type, value)
            }
        }
        fun reset(model: Int) {
            Log.d(TAG, "reset")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.reset(model)
            }
        }
        fun factoryReset(model: Int) {
            Log.d(TAG, "factoryReset")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.factoryReset(model)
            }
        }
        fun factoryResetAll(model: Int) {
            Log.d(TAG, "factoryResetAll")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.factoryResetAll(model)
            }
        }

        /**
         * @param fileType
         */
        @JvmOverloads
        fun getFileList(model: Int, fileType: Int? = null){
            BleServiceHelper.getFileList(model, fileType)

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
            BleServiceHelper.readFile(userId, fileName, model, offset)

        }

        /**
         *
         * @param model Int
         */
        @JvmOverloads
        fun stopRtTask(model: Int, sendCmd: () -> Unit = {}) {
            BleServiceHelper.stopRtTask(model){
                sendCmd.invoke()
            }
        }
        @JvmOverloads
        fun stopRtTask(models: IntArray, sendCmd: () -> Unit = {}) {
            for (m in models){
                BleServiceHelper.stopRtTask(m) {
                    sendCmd.invoke()
                }
            }
        }

        fun stopRtTask(sendCmd: () -> Unit = {}){
            if(BluetoothConfig.singleConnect && BluetoothConfig.currentModel.isNotEmpty()){
                    stopRtTask(BluetoothConfig.currentModel[0]){
                        sendCmd.invoke()
                    }

            }
        }

        fun startRtTask(delayMillis: Long = 200){
            if(BluetoothConfig.singleConnect && BluetoothConfig.currentModel.isNotEmpty()){
                    startRtTask(BluetoothConfig.currentModel[0], delayMillis)

            }
        }

        @JvmOverloads
        fun startRtTask(model: Int, delayMillis: Long = 200) {


            BleServiceHelper.setRTDelayTime(model, delayMillis)
            BleServiceHelper.startRtTask(model)
        }

        fun isRtStop(model: Int): Boolean{
            return BleServiceHelper.isRtStop(model)
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

        fun setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int){
            BleServiceHelper.setEr1Vibrate(model, switcher, threshold1, threshold2)
        }
        fun setDuoekVibrate(model: Int,switcher: Boolean, vector: Int, motionCount: Int,motionWindows: Int ){
            BleServiceHelper.setEr1Vibrate(model, switcher, vector, motionCount, motionWindows)
        }
        fun getEr1VibrateConfig(model: Int) {
            BleServiceHelper.getEr1VibrateConfig(model)
        }

        fun setEr2SwitcherState(model: Int, hrFlag: Boolean){
            BleServiceHelper.setEr2SwitcherState(model, hrFlag)
        }
        fun getEr2SwitcherState(model: Int){
            BleServiceHelper.getEr2SwitcherState(model)
        }

        @JvmOverloads
        fun bp2SetConfig(model: Int, switchState: Boolean, volume: Int = 2){
            BleServiceHelper.bp2SetConfig(model, switchState, volume)
        }
        fun bp2GetConfig(model: Int){
            BleServiceHelper.bp2GetConfig(model)
        }
        fun bp2SwitchState(model: Int, state: Int){
           BleServiceHelper.bp2SwitchState(model, state)
        }
        fun bp2GetPhyState(model: Int) {
            BleServiceHelper.bp2GetPhyState(model)
        }
        fun bp2SetPhyState(model: Int, state: Bp2BlePhyState) {
            BleServiceHelper.bp2SetPhyState(model, state)
        }

        fun bp2SetConfig(model: Int, config: Bp2Config) {
            BleServiceHelper.bp2SetConfig(model, config)
        }
        fun bp2GetRtState(model: Int) {
            BleServiceHelper.bp2GetRtState(model)
        }
        fun bp2GetWifiDevice(model: Int) {
            BleServiceHelper.bp2GetWifiDevice(model)
        }
        fun bp2SetWifiConfig(model: Int, config: Bp2WifiConfig) {
            BleServiceHelper.bp2SetWifiConfig(model, config)
        }
        fun bp2GetWifiConfig(model: Int) {
            BleServiceHelper.bp2GetWifiConfig(model)
        }
        fun bp2WriteUserList(model: Int, userList: LeBp2wUserList) {
            BleServiceHelper.bp2WriteUserList(model, userList)
        }
        fun bp2GetFileListCrc(model: Int, fileType: Int) {
            BleServiceHelper.bp2GetFileListCrc(model, fileType)
        }
        fun bp2DeleteFile(model: Int) {
            BleServiceHelper.bp2DeleteFile(model)
        }

        fun oxyGetRtWave(model: Int) {
            BleServiceHelper.oxyGetRtWave(model)
        }
        fun oxyGetRtParam(model: Int) {
            BleServiceHelper.oxyGetRtParam(model)
        }
        fun oxyGetPpgRt(model: Int){
            BleServiceHelper.oxyGetPpgRt(model)
        }

        fun startBp(model: Int) {
            Log.d(TAG, "startBp")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.startBp(model)
            }

        }
        fun stopBp(model: Int) {
            Log.d(TAG, "stopBp")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.stopBp(model)
            }

        }
        fun bpw1SetMeasureTime(model: Int, measureTime: Array<String?>) {
            Log.d(TAG, "setMeasureTime")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.bpw1SetMeasureTime(model, measureTime)
            }
        }
        fun bpw1GetMeasureTime(model: Int) {
            Log.d(TAG, "getMeasureTime")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.bpw1GetMeasureTime(model)
            }
        }
        fun bpw1SetTimingSwitch(model: Int, timingSwitch: Boolean) {
            Log.d(TAG, "setTimingSwitch")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.bpw1SetTimingSwitch(model, timingSwitch)
            }
        }

        fun setUserInfo(model: Int, userInfo: FscaleUserInfo) {
            Log.d(TAG, "setUserInfo")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.setUserInfo(model, userInfo)
            }
        }

        fun setUserList(model: Int, userList: List<FscaleUserInfo>) {
            Log.d(TAG, "setUserInfo")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.setUserList(model, userList)
            }
        }

        fun ap20SetConfig(model: Int, type: Int, config: Int) {
            Log.d(TAG, "setApConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.ap20SetConfig(model, type, config)
            }
        }
        fun ap20GetConfig(model: Int, type: Int) {
            Log.d(TAG, "getApConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.ap20GetConfig(model, type)
            }
        }
        fun ap20GetBattery(model: Int) {
            Log.d(TAG, "ap20GetBattery")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.ap20GetBattery(model)
            }
        }

        fun lew3BoundDevice(model: Int){
            Log.d(TAG, "lew3BoundDevice")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.lew3BoundDevice(model)
            }
        }
        fun lew3SetServer(model: Int, server: Lew3Config){
            Log.d(TAG, "lew3SetServer")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.lew3SetServer(model, server)
            }
        }
        fun lew3GetConfig(model: Int){
            Log.d(TAG, "lew3GetConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.lew3GetConfig(model)
            }
        }
        fun lew3GetBattery(model: Int){
            Log.d(TAG, "lew3GetBattery")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.lew3GetBattery(model)
            }
        }

        fun sp20GetBattery(model: Int) {
            Log.d(TAG, "sp20GetBattery")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.sp20GetBattery(model)
            }
        }
        fun sp20GetConfig(model: Int, type: Int) {
            Log.d(TAG, "sp20GetConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.sp20GetConfig(model, type)
            }
        }
        fun sp20SetConfig(model: Int, config: Sp20Config) {
            Log.d(TAG, "sp20SetConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.sp20SetConfig(model, config)
            }
        }

        fun aoj20aDeleteData(model: Int) {
            Log.d(TAG, "aoj20aDeleteData")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.aoj20aDeleteData(model)
            }
        }

        fun enableRtData(model: Int, type: Int, enable: Boolean) {
            Log.d(TAG, "enableRtData")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                when (model) {
                    Bluetooth.MODEL_AP20 -> {
                        BleServiceHelper.ap20EnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_SP20 -> {
                        BleServiceHelper.sp20EnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_POD_1W,
                    Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                    Bluetooth.MODEL_PC_60NW -> {
                        BleServiceHelper.pc60fwEnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_PC_68B -> {
                        BleServiceHelper.pc68bEnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_FETAL, Bluetooth.MODEL_VTM_AD5 -> {
                        BleServiceHelper.ad5EnableRtData(model, enable)
                    }
                }

            }
        }

        fun pc68bDeleteFile(model: Int) {
            Log.d(TAG, "pc68bDeleteFile")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.pc68bDeleteFile(model)
            }
        }
        fun pc68bGetStateInfo(model: Int, interval: Int) {
            Log.d(TAG, "pc68bStateInfo")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.pc68bGetStateInfo(model, interval)
            }
        }
        fun pc68bSetConfig(model: Int, config: Pc68bConfig) {
            Log.d(TAG, "pc68bSetConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.pc68bSetConfig(model, config)
            }
        }
        fun pc68bGetConfig(model: Int) {
            Log.d(TAG, "pc68bGetConfig")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.pc68bGetConfig(model)
            }
        }
        fun pc68bGetTime(model: Int) {
            Log.d(TAG, "pc68bGetTime")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.pc68bGetTime(model)
            }
        }
        
        fun startEcg(model: Int) {
            BleServiceHelper.startEcg(model)
        }
        fun stopEcg(model: Int) {
            BleServiceHelper.stopEcg(model)
        }
        fun pc300SetEcgDataDigit(model: Int, digit: Int) {
            BleServiceHelper.pc300SetEcgDataDigit(model, digit)
        }
        /*fun pc300SetGluUnit(model: Int, unit: Int) {
            BleServiceHelper.pc300SetGluUnit(model, unit)
        }
        fun pc300SetDeviceId(model: Int, id: Int) {
            BleServiceHelper.pc300SetDeviceId(model, id)
        }
        fun pc300GetDeviceId(model: Int) {
            BleServiceHelper.pc300GetDeviceId(model)
        }*/
        fun pc300SetGlucometerType(model: Int, type: Int) {
            BleServiceHelper.pc300SetGlucometerType(model, type)
        }
        fun pc300GetGlucometerType(model: Int) {
            BleServiceHelper.pc300GetGlucometerType(model)
        }
        /*fun pc300SetTempMode(model: Int, mode: Int) {
            BleServiceHelper.pc300SetTempMode(model, mode)
        }
        fun pc300GetTempMode(model: Int) {
            BleServiceHelper.pc300GetTempMode(model)
        }
        fun pc300SetBpMode(model: Int, mode: Int) {
            BleServiceHelper.pc300SetBpMode(model, mode)
        }
        fun pc300GetBpMode(model: Int) {
            BleServiceHelper.pc300GetBpMode(model)
        }*/

        fun lemDeviceSwitch(model: Int, on: Boolean) {
            BleServiceHelper.lemDeviceSwitch(model, on)
        }
        fun lemHeatMode(model: Int, on: Boolean) {
            BleServiceHelper.lemHeatMode(model, on)
        }
        fun lemGetBattery(model: Int) {
            BleServiceHelper.lemGetBattery(model)
        }
        fun lemMassMode(model: Int, mode: Int) {
            BleServiceHelper.lemMassageMode(model, mode)
        }
        fun lemMassLevel(model: Int, level: Int) {
            BleServiceHelper.lemMassageLevel(model, level)
        }
        fun lemMassTime(model: Int, time: Int) {
            BleServiceHelper.lemMassageTime(model, time)
        }


    }




}