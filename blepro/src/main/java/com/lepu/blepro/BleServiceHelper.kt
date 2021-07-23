package com.lepu.blepro

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.SparseArray
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.*
import com.lepu.blepro.ble.cmd.Er1BleCmd
import com.lepu.blepro.ble.service.BleService
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog

/**
 * 单例的蓝牙服务帮助类，原则上只通过此类开放API
 *
 * 1. 在Application onCreate()中初始化，完成必须配置(modelConfig、runRtConfig)后通过initService()开启服务#BleService。
 *
 *
 */
class BleServiceHelper private constructor() {

    /**
     * 下载数据的保存路径，key为model
     */
    var rawFolder: SparseArray<String>? = null

    /**
     * 服务onServiceConnected()时，应该初始化的model配置。必须在initService()之前完成
     * key为model
     */
    var modelConfig: SparseArray<Int> = SparseArray()
    /**
     * 服务onServiceConnected()时，应该初始化的model配置。必须在initService()之前完成
     * key为model
     */
    var runRtConfig: SparseArray<Boolean> = SparseArray()

    /**
     * 多设备模式手动重连中
     */
    var isReconnectingMulti: Boolean = false

    companion object {
        const val tag: String = "BleServiceHelper"

        val BleServiceHelper: BleServiceHelper by lazy {
            BleServiceHelper()
        }

    }

    lateinit var bleService: BleService
    private val bleConn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            LepuBleLog.d("BleServiceHelper onServiceConnected")
            if (p1 is BleService.BleBinder) {
                BleServiceHelper.bleService = p1.getService()
                LepuBleLog.d("bleService inited")
                initCurrentFace()

            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            LepuBleLog.d("BleServiceHelper onServiceDisconnected")
        }
    }
    //===========================================================

    /**
     * 在Application onCreate中初始化本单列,
     *
     */
    fun initService(application: Application, observer: BleServiceObserver?): BleServiceHelper {

        LepuBleLog.d("BleServiceHelper initService  start")
        BleService.observer = observer
        BleService.startService(application)

        Intent(application, BleService::class.java).also { intent ->
            application.bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
        return this
    }

    fun initLog(log: Boolean): BleServiceHelper{
        LepuBleLog.setDebug(log)
        LepuBleLog.d(log.toString())
        return this
    }



    fun initRawFolder(folders: SparseArray<String>): BleServiceHelper{
        this.rawFolder = folders
        LepuBleLog.d(folders.toString())
        return this
    }
    fun initModelConfig(modelConfig: SparseArray<Int>): BleServiceHelper{
        this.modelConfig = modelConfig
        return this
    }
    fun initRtConfig(runRtConfig: SparseArray<Boolean>): BleServiceHelper{
        this.runRtConfig = runRtConfig
        return this
    }

    /**
     * 服务连接成功时初始化vailFace
     * @return BleServiceHelper
     */
    private fun initCurrentFace(): BleServiceHelper{
        if (this::bleService.isInitialized ) {

            LepuBleLog.d("initVailFace", "${modelConfig.size()}")
            for (i in 0 until modelConfig.size()) {

                val model = modelConfig.get(modelConfig.keyAt(i))
                runRtConfig.get(model)?.let {
                    LepuBleLog.d("setInterfaces ===== ", "$it")
                    setInterfaces(model, it)
                } ?: setInterfaces(model)
            }
            LiveEventBus.get(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)

        }else{
            LepuBleLog.d("initVailFace failed!!!")
        }
        return this
    }



    /**
     * 当前要设置的设备Model, 必须在initService 之后调用
     */
    @JvmOverloads
    fun setInterfaces(model: Int, runRtImmediately: Boolean = false) {
        if (!checkService()) return
        LepuBleLog.d(tag, "setInterfaces")
        if (getInterface(model) == null) bleService.initInterfaces(model, runRtImmediately)
    }



    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(): BleServiceHelper {
        if (!checkService()) return this
        BleServiceHelper.bleService.reInitBle()
        return this
    }



    /**
     * 注册蓝牙状态改变的监听
     *
     */
    internal fun subscribeBI(model: Int, observer: BleChangeObserver) {
        if (!checkService()){
            LepuBleLog.d("bleService.isInitialized  = false")
            return
        }
        getInterface(model)?.onSubscribe(observer)
    }




    /**
     * 注销蓝牙状态改变的监听
     */
    internal fun detachBI(model: Int, observer: BleChangeObserver) {
        if (checkService()) getInterface(model)?.detach(observer)
    }


    /**
     * 开始扫描 单设备
     * @param scanModel Int
     * @param needPair Boolean
     */
    @JvmOverloads
    fun startScan(scanModel: Int, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.startDiscover(intArrayOf(scanModel), needPair)
    }

    /**
     * 开始扫描 多设备
     */
    @JvmOverloads
    fun startScan(scanModel: IntArray, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.startDiscover(scanModel, needPair)
    }


    /**
     * 停止扫描
     * 连接之前调用该方法，并会重置扫描条件为默认值
     * 组合套装时,targetModel 会被重置为末尾添加的model
     *
     */
    fun stopScan() {
        if (checkService()) bleService.stopDiscover()
    }

    /**
     * 获取model的interface
     */
    fun getInterface(model: Int): BleInterface? {
        if (!checkService()) return null

        val vailFace = bleService.vailFace
        LepuBleLog.e(tag, "getInterface: getInterface => currentModel：$model, vailFaceSize：${vailFace.size()}, curIsNUll = ${vailFace.get(model) == null}")
        return vailFace.get(model)
    }

    /**
     * 获取服务中所有的interface
     * @return SparseArray<BleInterface>?
     */
    fun getInterfaces(): SparseArray<BleInterface>? {
        if (!checkService())  return null
        return bleService.vailFace
    }

     /**
     *
     * @param context Context
     * @param model Int
     * @param b BluetoothDevice
     * @param isAutoReconnect Boolean
     * @param withUpdater Boolean 检查是否是升级失败的设备
     */
    @JvmOverloads
    fun connect(context: Context,model: Int, b: BluetoothDevice, isAutoReconnect: Boolean = true, toConnectUpdater: Boolean = false) {
        if (!checkService()) return

        LepuBleLog.d(tag, "connect")
        getInterface(model)?.let {
            it.connect(context, b, isAutoReconnect, toConnectUpdater)
        }
    }


    /**
     *  发起重连 允许扫描多个设备
     */
    fun reconnect(scanModel: IntArray, name: Array<String>, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect " )
        if (!checkService()) return

        bleService.reconnect(scanModel, name, toConnectUpdater)

    }

    /**
     * 发起重连
     * @param scanModel Int
     * @param name String
     */
    fun reconnect(scanModel: Int, name: String, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect" )
        if (!checkService()) return
        bleService.reconnect(intArrayOf(scanModel), arrayOf(name), toConnectUpdater)

    }


    /**
     * madAddress 重连
     * @param scanModel IntArray
     * @param macAddress Array<String>
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress " )
        if (!checkService()) return

        bleService.reconnectByAddress( scanModel, macAddress, toConnectUpdater)

    }

    /**
     * 通过MacAddress重连
     * @param scanModel Int
     * @param macAddress String
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: Int, macAddress: String, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress" )
        if (!checkService()) return
        bleService.reconnectByAddress(intArrayOf(scanModel), arrayOf(macAddress), toConnectUpdater)

    }

    /**
     * 全部断开连接
     */
    fun disconnect(autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect" )

        if (!checkService()) return
        val vailFace = bleService.vailFace
        for (i in 0 until vailFace.size()) {
            getInterface(vailFace.keyAt(i))?.let { it ->
                if (bleService.isDiscovery)stopScan()
                it.disconnect(autoReconnect)
            }
        }
    }

    /**
     * 断开指定model
     */
    fun disconnect(model: Int, autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect" )
        if (!checkService()) return
        getInterface(model)?.let {
            stopScan()
            it.disconnect(autoReconnect)
        }

    }


    /**
     * 主动获取当前蓝牙连接状态
     */
    fun getConnectState(model: Int): Int {
        if (!checkService()) return Ble.State.UNKNOWN
        getInterface(model)?.let {
            return it.calBleState()
        }?: return  Ble.State.UNKNOWN

    }


    /**
     * 是否存在未连接状态的interface
     * @param model IntArray
     * @return Boolean
     */
    fun hasUnConnected(model: IntArray): Boolean{
        if (!checkService()) return false
        LepuBleLog.d(tag, "into hasUnConnected...")
        for (m in model){
            getInterface(m)?.let {
                if (!it.state && !it.connecting) return true
            }
//            getConnectState(m).let {
//                LepuBleLog.d(tag, "$it")
//                if (it == Ble.State.DISCONNECTED) return true
//            }
        }
        LepuBleLog.d(tag, "没有未连接和已连接中的设备")
        return false
    }


    /**
     * 获取主机信息
     */
    fun getInfo(model: Int) {
        if (!checkService()) return
        getInterface(model)?.getInfo()

    }



    fun getFileList(model: Int){
        when(model){
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER2,Bluetooth.MODEL_BP2,Bluetooth.MODEL_BP2A ->{
                getInterface(model)?.getFileList()
            }
            else -> LepuBleLog.d(tag, "getFileList, model$model,未被允许获取文件列表")
        }
    }

    /**
     * 读取主机文件
     * @param userId String
     * @param fileName String
     * @param model Int
     * @param offset Int 开始读文件的偏移量
     */
    fun readFile(userId: String, fileName: String, model: Int, offset: Int = 0) {
        if (!checkService()) return
        getInterface(model)?.let {
            it.readFile(userId, fileName, offset)
        }

    }

    /**
     * 取消读取文件
     * @param model Int
     */
    fun cancelReadFile(model: Int){
        if (!checkService()) return
        getInterface(model)?.let {
            it.isCancelRF = true
            LiveEventBus.get(EventMsgConst.Download.EventIsCancel).post(model)
        }
    }

    /**
     * 暂停 读取文件
     * @param model Int
     * @param isPause Boolean
     */
    fun pauseReadFile(model: Int){
        if (!checkService()) return
        getInterface(model)?.let {
            it.isPausedRF = true
            LiveEventBus.get(EventMsgConst.Download.EventIsPaused).post(model)
        }
    }

    /**
     * 继续读取文件,并发送通知
     * @param model Int
     */
    fun continueReadFile(model: Int,userId: String, fileName: String, offset: Int){
        if (!checkService()) return
        getInterface(model)?.let {
            it.isPausedRF = false
            it.continueRf(userId, fileName, offset)
            LiveEventBus.get(EventMsgConst.Download.EventIsContinue).post(model)
        }
    }


    /**
     * 重置主机
     */
    fun reset(model: Int) {
        if (!checkService()) return
        getInterface(model)?.resetDeviceInfo()

    }

    /**
     * 更新设备设置
     */
    fun updateSetting(model: Int, type: String, value: Any) {
        if (!checkService()) return
        getInterface(model)?.updateSetting(type, value)
    }

    /**
     * 同步时间
     */
    fun syncTime(model: Int) {
        if (!checkService()) return
        getInterface(model)?.syncTime()
    }

    /**
     * 设置实时任务的间隔时间
     */
    fun setRTDelayTime(model: Int, delayMillis: Long){
        if (!checkService()) return
        getInterface(model)?.delayMillis = delayMillis
    }



    /**
     * 开启实时任务
     */
    fun startRtTask(model: Int){
        if (!checkService()) return
        getInterface(model)?.runRtTask()
    }


    /**
     * 移除获取实时任务
     */
    fun stopRtTask(model: Int) {
        if (!checkService()) return
        getInterface(model)?.stopRtTask()
    }
    fun getConfig(model: Int){
        getInterface(model)?.let { it1 ->
            (it1 as Bp2BleInterface).let {
                LepuBleLog.d(tag, "it as Bp2BleInterface")
                it.getConfig()
            }
        }
    }
    fun setConfig(model: Int,switch:Boolean){
        getInterface(model)?.let { it1 ->
            (it1 as Bp2BleInterface).let {
                LepuBleLog.d(tag, "it as Bp2BleInterface")
                it.setConfig(switch)
            }
        }
    }
    fun resetAll(model: Int){
        getInterface(model)?.let { it1 ->
            (it1 as Bp2BleInterface).let {
                LepuBleLog.d(tag, "it as Bp2BleInterface")
                it.resetAll()
            }
        }
    }
    fun startBp(model: Int) {
        getInterface(model)?.let { it1 ->
            (it1 as Bp2BleInterface).let {
                LepuBleLog.d(tag, "it as Bp2BleInterface")
                it.startBp()
            }
        }
    }
    fun stopBp(model: Int) {
        getInterface(model)?.let { it1 ->
            (it1 as Bp2BleInterface).let {
                LepuBleLog.d(tag, "it as Bp2BleInterface")
                it.stopBp()
            }
        }
    }

    fun checkService(): Boolean{
        if (!this::bleService.isInitialized){
            LepuBleLog.d("Error: bleService unInitialized")
            return false
        }
        return true
    }


    fun getBpmFileList(model: Int, map: HashMap<String, Any>){
        if (!checkService()) return
        if (model != Bluetooth.MODEL_BPM){
            LepuBleLog.d(tag,"getBpmFileList, 无效model：$model" )
            return
        }
        getInterface(model)?.let { it1 ->
            (it1 as BpmBleInterface).let {
                LepuBleLog.d(tag, "it as BpmBleInterface")
                it.getBpmFileList(map)
            }
        }

    }

    fun startBpm(model: Int){
        if (!checkService()) return
        if (model != Bluetooth.MODEL_BPM){
            LepuBleLog.d(tag,"startBpm, 无效model：$model" )
            return
        }
        getInterface(model)?.let { it1 ->
            (it1 as BpmBleInterface).let {
                LepuBleLog.d(tag, "it as BpmBleInterface")
                it.startBp()
            }
        }
    }

    fun stopBpm(model: Int){
        if (!checkService()) return
        if (model != Bluetooth.MODEL_BPM){
            LepuBleLog.d(tag,"startBpm, 无效model：$model" )
            return
        }
        getInterface(model)?.let { it1 ->
            (it1 as BpmBleInterface).let {
                LepuBleLog.d(tag, "it as BpmBleInterface")
                it.stopBp()
            }
        }
    }

    fun isScanning(): Boolean{
        if (!checkService()) return false
        return bleService.isDiscovery
    }

    fun isRtStop(model: Int): Boolean{
        if (!checkService()) return false
        return getInterface(model)?.isRtStop ?: true
    }

    /**
     * er2 设置hr开关状态
     * @param model Int
     * @param hrFlag Boolean
     */
    fun setEr2SwitcherState(model: Int, hrFlag: Boolean){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER2 -> {
                getInterface(model)?.let {
                    if (it is Er2BleInterface) it.setSwitcherState(hrFlag)
                }
            }
            else -> {
                LepuBleLog.d(tag, "error: setEr2SwitcherState model=$model, 不匹配")
            }
        }
    }

    /**
     * er2 获取hr开关状态
     * @param model Int
     */
    fun getEr2SwitcherState(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER2 -> {
                getInterface(model)?.let {
                    if (it is Er2BleInterface) it.getSwitcherState()
                }
            }
            else -> {
                LepuBleLog.d(tag, "error: getEr2SwitcherState model=$model, 不匹配")
            }
        }
    }


    //er1  duoek----------------

    fun getEr1VibrateConfig(model: Int){
        if (!checkService()) return
        getInterface(model)?.let { ble ->
            checkInterfaceType(model, ble).let { check ->
                LepuBleLog.d(tag, "getEr1VibrateConfig, checkInterfaceType = $check")

                (ble as Er1BleInterface).getVibrateConfig()
            }
        }

    }

    fun setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int){
        getInterface(model)?.let { ble ->
            checkInterfaceType(model, ble).let { check ->
                LepuBleLog.d(tag, "setEr1Vibrate, checkInterfaceType = $check")

                (ble as Er1BleInterface).setVibrateConfig(switcher, threshold1, threshold2)
            }
        }

    }

    fun setEr1Vibrate(model: Int,switcher: Boolean, vector: Int, motionCount: Int,motionWindows: Int ){
        getInterface(model)?.let { ble ->
            checkInterfaceType(model, ble).let { check ->
                LepuBleLog.d(tag, "setSwitcher, checkInterfaceType = $check")

                (ble as Er1BleInterface).setVibrateConfig(switcher, vector, motionCount, motionWindows)
            }
        }

    }

    fun er1FactoryResetAll(model: Int) {
        getInterface(model)?.let { ble ->
            checkInterfaceType(model, ble).let { check ->
                LepuBleLog.d(tag, "er1FactoryResetAll, checkInterfaceType = $check")

                (ble as Er1BleInterface).factoryRestAll()
            }
        }

    }


    fun er1FactoryReset(model: Int) {
        getInterface(model)?.let { ble ->
            checkInterfaceType(model, ble).let { check ->
                LepuBleLog.d(tag, "er1FactoryReset, checkInterfaceType = $check")

                (ble as Er1BleInterface).resetDeviceInfo()
            }
        }

    }

    //------er1 duoek   end-----------------

    fun checkInterfaceType(model: Int, inter: BleInterface): Boolean {
        if (!checkService()) return false

        when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK -> {
                return inter is Er1BleInterface
            }
            Bluetooth.MODEL_ER2 -> {
                return inter is Er2BleInterface
            }
            Bluetooth.MODEL_BPM -> {
                return inter is BpmBleInterface
            }
            Bluetooth.MODEL_O2RING -> {
                return inter is OxyBleInterface
            }
            Bluetooth.MODEL_BP2,Bluetooth.MODEL_BP2A ->{
                return inter is Bp2BleInterface
            }
            else -> {
                LepuBleLog.d(tag, "checkModel, 无效model：$model,${inter.javaClass}")
                return false
            }
        }


    }




}