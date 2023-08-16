package com.lepu.demo.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.util.SparseArray
import com.blankj.utilcode.util.PathUtils
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.Bp2Config
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.ble.data.ventilator.VentilationSetting
import com.lepu.blepro.ble.data.ventilator.WarningSetting
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.config.Constant.BluetoothConfig

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

            val RAW_FOLDERS = SparseArray<String>()
            RAW_FOLDERS.put(Bluetooth.MODEL_ER1, PathUtils.getExternalAppFilesPath() + "/er1/")
            RAW_FOLDERS.put(Bluetooth.MODEL_ER1S, PathUtils.getExternalAppFilesPath() + "/er1s/")
            RAW_FOLDERS.put(Bluetooth.MODEL_HHM1, PathUtils.getExternalAppFilesPath() + "/hhm1/")
            RAW_FOLDERS.put(Bluetooth.MODEL_HHM2, PathUtils.getExternalAppFilesPath() + "/hhm2/")
            RAW_FOLDERS.put(Bluetooth.MODEL_HHM3, PathUtils.getExternalAppFilesPath() + "/hhm3/")
            RAW_FOLDERS.put(Bluetooth.MODEL_LEW, PathUtils.getExternalAppFilesPath() + "/lew/")
            RAW_FOLDERS.put(Bluetooth.MODEL_W12C, PathUtils.getExternalAppFilesPath() + "/w12c/")
            RAW_FOLDERS.put(Bluetooth.MODEL_DUOEK, PathUtils.getExternalAppFilesPath() + "/duoek/")
            RAW_FOLDERS.put(Bluetooth.MODEL_ER2, PathUtils.getExternalAppFilesPath() + "/er2/")
            RAW_FOLDERS.put(Bluetooth.MODEL_LP_ER2, PathUtils.getExternalAppFilesPath() + "/lper2/")
            RAW_FOLDERS.put(Bluetooth.MODEL_BP2, PathUtils.getExternalAppFilesPath() + "/bp2/")
            RAW_FOLDERS.put(Bluetooth.MODEL_BP2A, PathUtils.getExternalAppFilesPath() + "/bp2a/")
            RAW_FOLDERS.put(Bluetooth.MODEL_BP2T, PathUtils.getExternalAppFilesPath() + "/bp2t/")
            RAW_FOLDERS.put(Bluetooth.MODEL_BTP, PathUtils.getExternalAppFilesPath() + "/btp/")
//            RAW_FOLDERS.put(Bluetooth.MODEL_R20, PathUtils.getExternalAppFilesPath() + "/r20/")
//            RAW_FOLDERS.put(Bluetooth.MODEL_R21, PathUtils.getExternalAppFilesPath() + "/r21/")
//            RAW_FOLDERS.put(Bluetooth.MODEL_R10, PathUtils.getExternalAppFilesPath() + "/r10/")
//            RAW_FOLDERS.put(Bluetooth.MODEL_R11, PathUtils.getExternalAppFilesPath() + "/r11/")
//            RAW_FOLDERS.put(Bluetooth.MODEL_LERES, PathUtils.getExternalAppFilesPath() + "/leres/")
            RAW_FOLDERS.put(Bluetooth.MODEL_PF_10BWS, PathUtils.getExternalAppFilesPath() + "/pf10bws/")
            RAW_FOLDERS.put(Bluetooth.MODEL_O2RING_S, PathUtils.getExternalAppFilesPath() + "/o2rings/")
            RAW_FOLDERS.put(Bluetooth.MODEL_O2RING, PathUtils.getExternalAppFilesPath() + "/o2/")

            getServiceHelper()
                .initLog(true)
//                .initLog(BuildConfig.DEBUG)
//                .initModelConfig(Constant.BluetoothConfig.SUPPORT_FACES) // 配置要支持的设备
                .initRawFolder(RAW_FOLDERS)
                .initService(
                    application,
                    BleSO.getInstance(application)
                ) //必须在initModelConfig initRawFolder之后调用
        }

        fun clearInterface(){
            BleServiceHelper.getInterfaces()?.let {
                it.clear()
            }
        }

        fun setInterface(model: Int, needClear: Boolean){
            if (needClear) {
                clearInterface()
            }
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
            BleServiceHelper.startScan(scanModel, BluetoothConfig.needPair)
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
        fun reconnect(model: IntArray, name: Array<String>, toConnectUpdater: Boolean = false) {
            Log.d(TAG, "reconnect...$model, name = $name")
            name.isNullOrEmpty().let { it1 ->
                if (it1) {
                    Log.d(TAG, "error: name")
                    return
                }
                BleServiceHelper.reconnect(model, name, toConnectUpdater)
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
            Log.d("test12345", "${BleServiceHelper.getSendCmd(model)}")
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
        fun setNeedPair(needPair : Boolean) {
            BleServiceHelper.setNeedPair(needPair)
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
        fun updateSetting(model: Int, type: Array<String>, value: IntArray) {
            BleServiceHelper.updateSetting(model, type, value)
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

        fun burnFactoryInfo(model: Int, config: FactoryConfig) {
            BleServiceHelper.burnFactoryInfo(model, config)
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
        fun readFile(userId: String, fileName: String, model: Int, offset: Int = 0, fileType: Int? = null) {
            BleServiceHelper.readFile(userId, fileName, model, offset, fileType)
        }
        fun cancelReadFile(model: Int){
            BleServiceHelper.cancelReadFile(model)
        }
        fun pauseReadFile(model: Int){
            BleServiceHelper.pauseReadFile(model)
        }
        @JvmOverloads
        fun continueReadFile(model: Int, userId: String, fileName: String, offset: Int, fileType: Int? = null){
            BleServiceHelper.continueReadFile(model, userId, fileName, offset, fileType)
        }
        fun getRawFolder(model: Int): String? {
            return BleServiceHelper.rawFolder?.get(model)
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

        fun pc100GetBoState(model: Int) {
            BleServiceHelper.pc100GetBoState(model)
        }

        fun setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int){
            BleServiceHelper.setEr1Vibrate(model, switcher, threshold1, threshold2)
        }
        fun setEr1Vibrate(model: Int, config: Er1Config) {
            BleServiceHelper.setEr1Vibrate(model, config)
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

        // 定制BP2A_Sibel
        fun bp2aCmd0x40(model: Int, key: Boolean, measure: Boolean) {
            BleServiceHelper.bp2aCmd0x40(model, key, measure)
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
        fun bp2SyncUtcTime(model: Int) {
            BleServiceHelper.bp2SyncUtcTime(model)
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
        fun getBattery(model: Int) {
            Log.d(TAG, "getBattery")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.getBattery(model)
            }
        }

        fun lewBoundDevice(model: Int, bound: Boolean){
            Log.d(TAG, "lewBoundDevice")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                BleServiceHelper.lewBoundDevice(model, bound)
            }
        }
        fun lewSetTime(model: Int, data: TimeData) {
            BleServiceHelper.lewSetTime(model, data)
        }
        fun lewGetTime(model: Int) {
            BleServiceHelper.lewGetTime(model)
        }
        fun lewFindDevice(model: Int, on: Boolean) {
            BleServiceHelper.lewFindDevice(model, on)
        }
        fun lewGetDeviceNetwork(model: Int) {
            BleServiceHelper.lewGetDeviceNetwork(model)
        }
        fun lewGetSystemSetting(model: Int) {
            BleServiceHelper.lewGetSystemSetting(model)
        }
        fun lewSetSystemSetting(model: Int, setting: SystemSetting) {
            BleServiceHelper.lewSetSystemSetting(model,setting)
        }
        fun lewGetLanguage(model: Int) {
            BleServiceHelper.lewGetLanguage(model)
        }
        fun lewSetLanguage(model: Int, language: Int) {
            BleServiceHelper.lewSetLanguage(model,language)
        }
        fun lewGetUnit(model: Int) {
            BleServiceHelper.lewGetUnit(model)
        }
        fun lewSetUnit(model: Int, setting: UnitSetting) {
            BleServiceHelper.lewSetUnit(model,setting)
        }
        fun lewGetHandRaise(model: Int) {
            BleServiceHelper.lewGetHandRaise(model)
        }
        fun lewSetHandRaise(model: Int, setting: HandRaiseSetting) {
            BleServiceHelper.lewSetHandRaise(model,setting)
        }
        fun lewGetLrHand(model: Int) {
            BleServiceHelper.lewGetLrHand(model)
        }
        fun lewSetLrHand(model: Int, hand: Int) {
            BleServiceHelper.lewSetLrHand(model,hand)
        }
        fun lewGetNoDisturbMode(model: Int) {
            BleServiceHelper.lewGetNoDisturbMode(model)
        }
        fun lewSetNoDisturbMode(model: Int, mode: NoDisturbMode) {
            BleServiceHelper.lewSetNoDisturbMode(model, mode)
        }
        fun lewGetAppSwitch(model: Int) {
            BleServiceHelper.lewGetAppSwitch(model)
        }
        fun lewSetAppSwitch(model: Int, app: AppSwitch) {
            BleServiceHelper.lewSetAppSwitch(model, app)
        }
        fun lewSendNotification(model: Int, info: NotificationInfo) {
            BleServiceHelper.lewSendNotification(model, info)
        }
        fun lewGetDeviceMode(model: Int) {
            BleServiceHelper.lewGetDeviceMode(model)
        }
        fun lewSetDeviceMode(model: Int, mode: Int) {
            BleServiceHelper.lewSetDeviceMode(model, mode)
        }
        fun lewGetAlarmClock(model: Int) {
            BleServiceHelper.lewGetAlarmClock(model)
        }
        fun lewSetAlarmClock(model: Int, info: AlarmClockInfo) {
            BleServiceHelper.lewSetAlarmClock(model, info)
        }
        fun lewGetPhoneSwitch(model: Int) {
            BleServiceHelper.lewGetPhoneSwitch(model)
        }
        fun lewSetPhoneSwitch(model: Int, phone: PhoneSwitch) {
            BleServiceHelper.lewSetPhoneSwitch(model, phone)
        }
        fun lewGetMedicineRemind(model: Int) {
            BleServiceHelper.lewGetMedicineRemind(model)
        }
        fun lewSetMedicineRemind(model: Int, remind: MedicineRemind) {
            BleServiceHelper.lewSetMedicineRemind(model, remind)
        }
        fun lewGetMeasureSetting(model: Int) {
            BleServiceHelper.lewGetMeasureSetting(model)
        }
        fun lewSetMeasureSetting(model: Int, setting: MeasureSetting) {
            BleServiceHelper.lewSetMeasureSetting(model, setting)
        }
        fun lewGetSportTarget(model: Int) {
            BleServiceHelper.lewGetSportTarget(model)
        }
        fun lewSetSportTarget(model: Int, target: SportTarget) {
            BleServiceHelper.lewSetSportTarget(model, target)
        }
        fun lewGetTargetRemind(model: Int) {
            BleServiceHelper.lewGetTargetRemind(model)
        }
        fun lewSetTargetRemind(model: Int, remind: Boolean) {
            BleServiceHelper.lewSetTargetRemind(model, remind)
        }
        fun lewGetSittingRemind(model: Int) {
            BleServiceHelper.lewGetSittingRemind(model)
        }
        fun lewSetSittingRemind(model: Int, remind: SittingRemind) {
            BleServiceHelper.lewSetSittingRemind(model, remind)
        }
        fun lewGetHrDetect(model: Int) {
            BleServiceHelper.lewGetHrDetect(model)
        }
        fun lewSetHrDetect(model: Int, detect: HrDetect) {
            BleServiceHelper.lewSetHrDetect(model, detect)
        }
        fun lewGetOxyDetect(model: Int) {
            BleServiceHelper.lewGetOxyDetect(model)
        }
        fun lewSetOxyDetect(model: Int, detect: OxyDetect) {
            BleServiceHelper.lewSetOxyDetect(model, detect)
        }
        fun lewGetUserInfo(model: Int) {
            BleServiceHelper.lewGetUserInfo(model)
        }
        fun lewSetUserInfo(model: Int, info: UserInfo) {
            BleServiceHelper.lewSetUserInfo(model, info)
        }
        fun lewGetPhoneBook(model: Int) {
            BleServiceHelper.lewGetPhoneBook(model)
        }
        fun lewSetPhoneBook(model: Int, book: PhoneBook) {
            BleServiceHelper.lewSetPhoneBook(model, book)
        }
        fun lewGetSosContact(model: Int) {
            BleServiceHelper.lewGetSosContact(model)
        }
        fun lewSetSosContact(model: Int, sos: SosContact) {
            BleServiceHelper.lewSetSosContact(model, sos)
        }
        fun lewGetSecondScreen(model: Int) {
            BleServiceHelper.lewGetSecondScreen(model)
        }
        fun lewSetSecondScreen(model: Int, screen: SecondScreen) {
            BleServiceHelper.lewSetSecondScreen(model, screen)
        }
        fun lewGetCards(model: Int) {
            BleServiceHelper.lewGetCards(model)
        }
        fun lewSetCards(model: Int, cards: IntArray) {
            BleServiceHelper.lewSetCards(model, cards)
        }
        fun lewGetRtData(model: Int) {
            BleServiceHelper.lewGetRtData(model)
        }
        fun lewGetFileList(model: Int, type: Int, startTime: Long) {
            BleServiceHelper.getFileList(model, type, startTime)
        }
        fun lewGetHrThreshold(model: Int) {
            BleServiceHelper.lewGetHrThreshold(model)
        }
        fun lewSetHrThreshold(model: Int, threshold: HrThreshold) {
            BleServiceHelper.lewSetHrThreshold(model, threshold)
        }
        fun lewGetOxyThreshold(model: Int) {
            BleServiceHelper.lewGetOxyThreshold(model)
        }
        fun lewSetOxyThreshold(model: Int, threshold: OxyThreshold) {
            BleServiceHelper.lewSetOxyThreshold(model, threshold)
        }
        fun checkmeGetFileList(model: Int, fileType: Int, id: Int) {
            BleServiceHelper.checkmeGetFileList(model, fileType, id)
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
        fun aoj20aGetRtData(model: Int) {
            BleServiceHelper.aoj20aGetRtData(model)
        }

        fun enableRtData(model: Int, type: Int, enable: Boolean) {
            Log.d(TAG, "enableRtData")
            BleServiceHelper.getInterface(model)?.let {
                if(getBleState(model) != State.CONNECTED){
                    Log.d(TAG, "设备未连接")
                    return
                }
                when (model) {
                    Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                        BleServiceHelper.ap20EnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                        BleServiceHelper.sp20EnableRtData(model, type, enable)
                    }
                    Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_POD_1W,
                    Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                    Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
                    Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
                    Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
                    Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_S5W,
                    Bluetooth.MODEL_S6W, Bluetooth.MODEL_S7W,
                    Bluetooth.MODEL_S7BW, Bluetooth.MODEL_S6W1,
                    Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS,
                    Bluetooth.MODEL_PC_60NW_NO_SN -> {
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

        fun pc60fwGetBranchCode(model: Int) {
            BleServiceHelper.pc60fwGetBranchCode(model)
        }
        fun pc60fwSetBranchCode(model: Int, code: String) {
            BleServiceHelper.pc60fwSetBranchCode(model, code)
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
        fun pc300SetGluUnit(model: Int, unit: Int) {
//            BleServiceHelper.pc300SetGluUnit(model, unit)
        }
        /*fun pc300SetDeviceId(model: Int, id: Int) {
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
        fun lemMassMode(model: Int, mode: Int) {
            BleServiceHelper.lemMassageMode(model, mode)
        }
        fun lemMassLevel(model: Int, level: Int) {
            BleServiceHelper.lemMassageLevel(model, level)
        }
        fun lemMassTime(model: Int, time: Int) {
            BleServiceHelper.lemMassageTime(model, time)
        }

        fun er3SetConfig(model: Int, mode: Int) {
            BleServiceHelper.er3SetConfig(model, mode)
        }
        fun er3GetConfig(model: Int) {
            BleServiceHelper.er3GetConfig(model)
        }

        fun lepodSetConfig(model: Int, mode: Int) {
            BleServiceHelper.lepodSetConfig(model, mode)
        }
        fun lepodGetConfig(model: Int) {
            BleServiceHelper.lepodGetConfig(model)
        }
        fun lepodGetRtParam(model: Int) {
            BleServiceHelper.lepodGetRtParam(model)
        }

        fun bpmGetRtState(model: Int) {
            BleServiceHelper.getBpmRtState(model)
        }

        fun vtm01GetOriginalData(model: Int) {
            BleServiceHelper.vtm01GetOriginalData(model)
        }
        fun vtm01GetRtParam(model: Int) {
            BleServiceHelper.vtm01GetRtParam(model)
        }
        fun vtm01SleepMode(model: Int, on: Boolean) {
            BleServiceHelper.vtm01SleepMode(model, on)
        }
        // BTP
        fun btpGetConfig(model: Int) {
            BleServiceHelper.btpGetConfig(model)
        }
        fun btpSetLowHr(model: Int, lowHr: Int) {
            BleServiceHelper.btpSetLowHr(model, lowHr)
        }
        fun btpSetHighHr(model: Int, highHr: Int) {
            BleServiceHelper.btpSetHighHr(model, highHr)
        }
        fun btpSetLowTemp(model: Int, lowTemp: Int) {
            BleServiceHelper.btpSetLowTemp(model, lowTemp)
        }
        fun btpSetHighTemp(model: Int, highTemp: Int) {
            BleServiceHelper.btpSetHighTemp(model, highTemp)
        }
        fun btpSetTempUnit(model: Int, unit: Int) {
            BleServiceHelper.btpSetTempUnit(model, unit)
        }
        fun btpSetSystemSwitch(model: Int, hrSwitch: Boolean, lightSwitch: Boolean, tempSwitch: Boolean) {
            BleServiceHelper.btpSetSystemSwitch(model, hrSwitch, lightSwitch, tempSwitch)
        }
        fun echo(model: Int, data: ByteArray) {
            BleServiceHelper.echo(model, data)
        }
        // Ventilator
        fun ventilatorGetFileList(model: Int, fileType: Int, timestamp: Long) {
            BleServiceHelper.getFileList(model, fileType, timestamp)
        }
        fun encrypt(model: Int, id: String) {
            BleServiceHelper.encrypt(model, id)
        }
        fun ventilatorDeviceBound(model: Int, bound: Boolean) {
            BleServiceHelper.ventilatorDeviceBound(model, bound)
        }
        fun ventilatorSetUserInfo(model: Int, userInfo: com.lepu.blepro.ble.data.ventilator.UserInfo) {
            BleServiceHelper.ventilatorSetUserInfo(model, userInfo)
        }
        fun ventilatorGetUserInfo(model: Int) {
            BleServiceHelper.ventilatorGetUserInfo(model)
        }
        fun ventilatorDoctorModeIn(model: Int, pin: String, timestamp: Long) {
            BleServiceHelper.ventilatorDoctorModeIn(model, pin, timestamp)
        }
        fun ventilatorDoctorModeOut(model: Int) {
            BleServiceHelper.ventilatorDoctorModeOut(model)
        }
        fun ventilatorGetWifiList(model: Int) {
            BleServiceHelper.ventilatorGetWifiList(model)
        }
        fun ventilatorSetWifiConfig(model: Int, config: Bp2WifiConfig) {
            BleServiceHelper.ventilatorSetWifiConfig(model, config)
        }
        fun ventilatorGetWifiConfig(model: Int) {
            BleServiceHelper.ventilatorGetWifiConfig(model)
        }
        fun ventilatorGetVersionInfo(model: Int) {
            BleServiceHelper.ventilatorGetVersionInfo(model)
        }
        fun ventilatorGetSystemSetting(model: Int) {
            BleServiceHelper.ventilatorGetSystemSetting(model)
        }
        fun ventilatorSetSystemSetting(model: Int, setting: com.lepu.blepro.ble.data.ventilator.SystemSetting) {
            BleServiceHelper.ventilatorSetSystemSetting(model, setting)
        }
        fun ventilatorGetMeasureSetting(model: Int) {
            BleServiceHelper.ventilatorGetMeasureSetting(model)
        }
        fun ventilatorSetMeasureSetting(model: Int, setting: com.lepu.blepro.ble.data.ventilator.MeasureSetting) {
            BleServiceHelper.ventilatorSetMeasureSetting(model, setting)
        }
        fun ventilatorMaskTest(model: Int, start: Boolean) {
            BleServiceHelper.ventilatorMaskTest(model, start)
        }
        fun ventilatorGetVentilationSetting(model: Int) {
            BleServiceHelper.ventilatorGetVentilationSetting(model)
        }
        fun ventilatorSetVentilationSetting(model: Int, setting: VentilationSetting) {
            BleServiceHelper.ventilatorSetVentilationSetting(model, setting)
        }
        fun ventilatorGetWarningSetting(model: Int) {
            BleServiceHelper.ventilatorGetWarningSetting(model)
        }
        fun ventilatorSetWarningSetting(model: Int, setting: WarningSetting) {
            BleServiceHelper.ventilatorSetWarningSetting(model, setting)
        }
        fun ventilatorVentilationSwitch(model: Int, start: Boolean) {
            BleServiceHelper.ventilatorVentilationSwitch(model, start)
        }
        fun ventilatorGetRtState(model: Int) {
            BleServiceHelper.ventilatorGetRtState(model)
        }
        fun ventilatorFwUpdate(model: Int, fwUpdate: FwUpdate) {
            BleServiceHelper.ventilatorFwUpdate(model, fwUpdate)
        }
        // BP3
        fun bp3GetConfig(model: Int) {
            BleServiceHelper.bp3GetConfig(model)
        }
        fun bp3SetConfig(model: Int, config: Bp2Config) {
            BleServiceHelper.bp3SetConfig(model, config)
        }
        fun bp3CalibrationZero(model: Int) {
            BleServiceHelper.bp3CalibrationZero(model)
        }
        fun bp3CalibrationSlope(model: Int, pressure: Int) {
            BleServiceHelper.bp3CalibrationSlope(model, pressure)
        }
        @JvmOverloads
        fun bp3GetRtPressure(model: Int, rate: Int = 0) {
            BleServiceHelper.bp3GetRtPressure(model, rate)
        }
        fun bp3GetRtWave(model: Int) {
            BleServiceHelper.bp3GetRtWave(model)
        }
        fun bp3PressureTest(model: Int, pressure: Int) {
            BleServiceHelper.bp3PressureTest(model, pressure)
        }
        fun bp3SwitchValve(model: Int, on: Boolean) {
            BleServiceHelper.bp3SwitchValve(model, on)
        }
        fun bp3GetCurPressure(model: Int) {
            BleServiceHelper.bp3GetCurPressure(model)
        }
        fun bp3SwitchTestMode(model: Int, mode: Int) {
            BleServiceHelper.bp3SwitchTestMode(model, mode)
        }
        fun bp3SwitchBpUnit(model: Int, unit: Int) {
            BleServiceHelper.bp3SwitchBpUnit(model, unit)
        }
        @JvmOverloads
        fun bp3GetWifiList(model: Int, deviceNum: Int = 0) {
            BleServiceHelper.bp3GetWifiList(model, deviceNum)
        }
        fun bp3SetWifiConfig(model: Int, config: Bp2WifiConfig) {
            BleServiceHelper.bp3SetWifiConfig(model, config)
        }
        @JvmOverloads
        fun bp3GetWifiConfig(model: Int, option: Int = 3) {
            BleServiceHelper.bp3GetWifiConfig(model, option)
        }
        fun bp3SwitchWifi4g(model: Int, on: Boolean) {
            BleServiceHelper.bp3SwitchWifi4g(model, on)
        }
        // ECN
        fun ecnStartCollect(model: Int) {
            BleServiceHelper.ecnStartCollect(model)
        }
        fun ecnStopCollect(model: Int) {
            BleServiceHelper.ecnStopCollect(model)
        }
        fun ecnStartRtData(model: Int) {
            BleServiceHelper.ecnStartRtData(model)
        }
        fun ecnStopRtData(model: Int) {
            BleServiceHelper.ecnStopRtData(model)
        }
        fun ecnGetRtState(model: Int) {
            BleServiceHelper.ecnGetRtState(model)
        }
        fun ecnGetDiagnosisResult(model: Int) {
            BleServiceHelper.ecnGetDiagnosisResult(model)
        }
        fun pf10Aw1SetConfig(model: Int, config: Pf10Aw1Config) {
            BleServiceHelper.pf10Aw1SetConfig(model, config)
        }
        fun pf10Aw1GetConfig(model: Int) {
            BleServiceHelper.pf10Aw1GetConfig(model)
        }
        fun pf10Aw1EnableRtData(model: Int, type: Int, enable: Boolean) {
            BleServiceHelper.pf10Aw1EnableRtData(model, type, enable)
        }
        fun oxyIISetConfig(model: Int, config: OxyIIConfig) {
            BleServiceHelper.oxyIISetConfig(model, config)
        }
        fun oxyIIGetConfig(model: Int) {
            BleServiceHelper.oxyIIGetConfig(model)
        }
        fun oxyIIGetRtParam(model: Int) {
            BleServiceHelper.oxyIIGetRtParam(model)
        }
        fun oxyIIGetRtWave(model: Int) {
            BleServiceHelper.oxyIIGetRtWave(model)
        }
        fun oxyIIGetRtPpg(model: Int) {
            BleServiceHelper.oxyIIGetRtPpg(model)
        }
    }




}