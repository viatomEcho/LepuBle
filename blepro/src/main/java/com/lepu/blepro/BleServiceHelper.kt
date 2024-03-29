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
import com.lepu.blepro.ble.data.*
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
     * 目前SDK会保存Er1，Oxy原始文件
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
     * 停止服务
     */
    fun stopService(application: Application) {
        application.unbindService(bleConn)
        BleService.stopService(application)
        LepuBleLog.d("BleServiceHelper stopService")
    }

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
        if (this::bleService.isInitialized) {

            LepuBleLog.d("initVailFace", "${modelConfig.size()}")
            for (i in 0 until modelConfig.size()) {

                val model = modelConfig.get(modelConfig.keyAt(i))
                runRtConfig.get(model)?.let {
                    LepuBleLog.d("setInterfaces ===== ", "$it")
                    setInterfaces(model, it)
                } ?: setInterfaces(model)
            }
            LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)

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
     * 开始扫描 单model设备
     * @param scanModel Int
     * @param needPair Boolean
     */
    @JvmOverloads
    fun startScan(scanModel: Int? = null, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.setScanDefineDevice(false, false, "")
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }
    }

    /**
     * 开始扫描 多model设备
     */
    @JvmOverloads
    fun startScan(scanModel: IntArray, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.setScanDefineDevice(false, false, "")
        bleService.startDiscover(scanModel, needPair)
    }

    /**
     * 具体蓝牙名扫描
     */
    @JvmOverloads
    fun startScanByName(deviceName: String, scanModel: Int? = null, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.setScanDefineDevice(true, true, deviceName)
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }
    }

    /**
     * 具体蓝牙地址扫描
     */
    @JvmOverloads
    fun startScanByAddress(address: String, scanModel: Int? = null, needPair: Boolean = false) {
        if (!checkService()) return
        bleService.setScanDefineDevice(true, false, address)
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }
    }

    /**
     * 停止扫描
     * 连接之前调用该方法，并会重置扫描条件为默认值
     * 组合套装时,targetModel 会被重置为末尾添加的model
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
        LepuBleLog.d(tag, "getInterface: getInterface => currentModel：$model, vailFaceSize：${vailFace.size()}}")
        vailFace.get(model)?.let {
            return vailFace.get(model)
        }?: kotlin.run {
            LepuBleLog.e("current model unsupported!!")
            return null
        }

    }

    /**
     * 获取服务中所有的interface
     * @return SparseArray<BleInterface>?
     */
    fun getInterfaces(): SparseArray<BleInterface>? {
        if (!checkService()) return null
        return bleService.vailFace
    }

    /**
     * 判断设备名重连是否符合标准：过滤pc80b,fhr,bpw1
     */
    fun canReconnectByName(model: Int): Boolean {
        return when(model) {
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_FHR, Bluetooth.MODEL_BPW1,
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F5_SCALE -> false
            else -> true
        }
    }

    /**
     *
     * @param context Context
     * @param model Int
     * @param b BluetoothDevice
     * @param isAutoReconnect Boolean
     * @param toConnectUpdater Boolean 检查是否是连接升级失败的设备（Er1设备升级失败，蓝牙名是Er1 updater）
     *
     * 连接成功后自动同步时间（pc80b,pc60fw,胎心仪设备没有同步时间设置，发送EventMsgConst.Ble.EventBleDeviceReady）
     */
     @JvmOverloads
     fun connect(context: Context, model: Int, b: BluetoothDevice, isAutoReconnect: Boolean = true, toConnectUpdater: Boolean = false) {
        if (!checkService()) return

        LepuBleLog.d(tag, "connect")
        getInterface(model)?.let {
            it.connect(context, b, isAutoReconnect, toConnectUpdater)
        }
    }


    /**
     *  发起重连 允许扫描多个设备
     */
    @JvmOverloads
    fun reconnect(scanModel: IntArray, name: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect " )
        if (!checkService()) return

        bleService.reconnect(scanModel, name, needPair, toConnectUpdater)

    }

    /**
     * 发起重连 一次只连一个
     * @param scanModel Int
     * @param name String
     */
    @JvmOverloads
    fun reconnect(scanModel: Int, name: String, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect" )
        if (!checkService()) return
        bleService.reconnect(intArrayOf(scanModel), arrayOf(name), needPair, toConnectUpdater)

    }


    /**
     * madAddress 重连
     * @param scanModel IntArray
     * @param macAddress Array<String>
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, needPair: Boolean, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress " )
        if (!checkService()) return

        bleService.reconnectByAddress(scanModel, macAddress, needPair, toConnectUpdater)

    }

    /**
     * 通过MacAddress重连
     * @param scanModel Int
     * @param macAddress String
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: Int, macAddress: String, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress" )
        if (!checkService()) return
        bleService.reconnectByAddress(intArrayOf(scanModel), arrayOf(macAddress), needPair, toConnectUpdater)

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
     * 获取发送的指令字符串
     */
    fun getSendCmd(model: Int): String {
        if (!checkService()) return ""
        getInterface(model)?.let {
            return it.getSendCmd()
        }
        return ""
    }

    /**
     * 主动获取当前蓝牙连接状态
     */
    fun getConnectState(model: Int): Int {
        if (!checkService()) return Ble.State.UNKNOWN
        getInterface(model)?.let {
            return it.calBleState()
        }?: return Ble.State.UNKNOWN

    }

    /**
     * 获取扫描状态
     */
    fun isScanning(): Boolean{
        if (!checkService()) return false
        return bleService.isDiscovery
    }

    /**
     * 获取实时任务停止状态
     */
    fun isRtStop(model: Int): Boolean{
        if (!checkService()) return false
        return getInterface(model)?.isRtStop ?: true
    }

    /**
     * 设置mtu
     */
    fun setBleMtu(model: Int, mtu: Int){
        if (!checkService()) return
        getInterface(model)?.setBleMtu(mtu)
    }

    /**
     * 获取mtu
     */
    fun getBleMtu(model: Int): Int{
        var mtu = 0
        if (!checkService()) return mtu
        mtu = getInterface(model)?.getBleMtu()!!
        return mtu
    }

    /**
     * 设置匹配状态
     */
    fun setNeedPair(needPair : Boolean){
        BleServiceHelper.bleService.needPair = needPair
    }

    fun removeReconnectName(name: String) {
        val iterator = bleService.reconnectDeviceName.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == name) {
                iterator.remove()
                LepuBleLog.d(
                    tag,
                    "从重连名单中移除 $name,  list = ${bleService.reconnectDeviceName.joinToString()}"
                )
            }
        }
    }

    fun removeReconnectAddress(address: String) {
        val iterator = bleService.reconnectDeviceAddress.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == address) {
                iterator.remove()
                LepuBleLog.d(
                    tag,
                    "从重连名单中移除 $address,  list = ${bleService.reconnectDeviceAddress.joinToString()}"
                )
            }
        }
    }

    fun getReconnectDeviceName(): ArrayList<String>{
        return bleService.reconnectDeviceName
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

    fun hasUnConnected(): Boolean {
        if (!checkService()) return false
        LepuBleLog.d(tag, "into hasUnConnected...")
        for (x in 0 until bleService.vailFace.size()) {
            bleService.vailFace[bleService.vailFace.keyAt(x)]?.let {
                it.let {
                    LepuBleLog.d(tag, "hasUnConnected  有未连接的设备: model = ${it.model}")

                    if (!it.state && !it.connecting) return true
                }
            }

        }
        LepuBleLog.d(tag, "hasUnConnected 没有未连接的设备 ")
        return false
    }

    /**
     * BleService初始化判断
     */
    fun checkService(): Boolean{
        if (!this::bleService.isInitialized){
            LepuBleLog.d("Error: bleService unInitialized")
            return false
        }
        return true
    }

    fun checkInterfaceType(model: Int, inter: BleInterface): Boolean {
        if (!checkService()) return false

        when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N -> {
                return inter is Er1BleInterface
            }
            Bluetooth.MODEL_ER2 -> {
                return inter is Er2BleInterface
            }
            Bluetooth.MODEL_BPM -> {
                return inter is BpmBleInterface
            }
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_BABYO2,
            Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_CHECKO2,
            Bluetooth.MODEL_O2M, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT -> {
                return inter is OxyBleInterface
            }
            Bluetooth.MODEL_BP2,Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T ->{
                return inter is Bp2BleInterface
            }
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_PC_60B -> {
                return inter is Pc60FwBleInterface
            }
            Bluetooth.MODEL_PC80B -> {
                return inter is Pc80BleInterface
            }
            Bluetooth.MODEL_FHR -> {
                return inter is FhrBleInterface
            }
            Bluetooth.MODEL_BPW1 -> {
                return inter is Bpw1BleInterface
            }
            Bluetooth.MODEL_F4_SCALE -> {
                return inter is F4ScaleBleInterface
            }
            Bluetooth.MODEL_F5_SCALE -> {
                return inter is F5ScaleBleInterface
            }
            Bluetooth.MODEL_AP20 -> {
                return inter is Ap20BleInterface
            }
            Bluetooth.MODEL_PC_68B -> {
                return inter is Pc68bBleInterface
            }
            else -> {
                LepuBleLog.d(tag, "checkModel, 无效model：$model,${inter.javaClass}")
                return false
            }
        }
    }

    /**
     * 获取主机信息
     */
    fun getInfo(model: Int) {
        if (!checkService()) return
        getInterface(model)?.getInfo()

    }

    /**
     * 获取设备文件列表
     * @param fileType LeBp2w获取文件列表类型（LeBp2wBleCmd.FileType.ECG_TYPE, BP_TYPE, USER_TYPE）
     * @param fileType CheckmeLe获取文件列表类型（CheckmeLeBleCmd.ListType.ECG_TYPE, OXY_TYPE, DLC_TYPE）
     */
    @JvmOverloads
    fun getFileList(model: Int, fileType: Int? = /*LeBp2wBleCmd.FileType.ECG_TYPE*/ null){
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--getFileList")
                        if (fileType == null) {
                            it.getFileList()
                        } else {
                            it.getFileList(fileType)
                        }
                    }
                }
            }
            Bluetooth.MODEL_CHECKME_LE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as CheckmeLeBleInterface).let {
                        LepuBleLog.d(tag, "it as CheckmeLeBleInterface--getFileList")
                        if (fileType == null) {
                            it.getFileList()
                        } else {
                            it.getFileList(fileType)
                        }
                    }
                }
            }
            else -> {
                getInterface(model)?.getFileList()
            }
        }
    }

    /**
     * 读取主机文件
     * @param userId String
     * @param fileName String
     * @param model Int
     * @param offset Int 开始读文件的偏移量
     */
    @JvmOverloads
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
            LiveEventBus.get<Int>(EventMsgConst.Download.EventIsCancel).post(model)
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
            LiveEventBus.get<Int>(EventMsgConst.Download.EventIsPaused).post(model)
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
            LiveEventBus.get<Int>(EventMsgConst.Download.EventIsContinue).post(model)
        }
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
     * 恢复生产出厂状态
     */
    fun factoryResetAll(model: Int){
        if (!checkService()) return
        getInterface(model)?.factoryResetAll()
    }

    /**
     * 恢复出厂设置
     */
    fun factoryReset(model: Int){
        if (!checkService()) return
        getInterface(model)?.factoryReset()
    }

    /**
     * 设备复位
     */
    fun reset(model: Int){
        if (!checkService()) return
        getInterface(model)?.reset()
    }

    /**
     * 开启实时任务
     */
    fun startRtTask(model: Int){
        if (!checkService()) return
        getInterface(model)?.let {
            it.runRtTask()
        }
    }

    /**
     * 移除获取实时任务
     */
    @JvmOverloads
    fun stopRtTask(model: Int, sendCmd: () -> Unit = {}) {
        if (!checkService()) return
        getInterface(model)?.let {
            it.stopRtTask {
                sendCmd.invoke()
            }
        }
    }

    /**
     * 开始测量血压
     */
    fun startBp(model: Int) {
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BPM ->{
                getInterface(model)?.let { it1 ->
                    (it1 as BpmBleInterface).let {
                        LepuBleLog.d(tag, "it as BpmBleInterface--startBp")
                        it.startBp()
                    }
                }
            }
            Bluetooth.MODEL_BPW1 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Bpw1BleInterface).let {
                        LepuBleLog.d(tag, "it as Bpw1BleInterface--startBp")
                        it.startBp()
                    }
                }
            }
            Bluetooth.MODEL_PC100 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Pc100BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc100BleInterface--startBp")
                        it.startBp()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "startBp model error  ")
        }

    }

    /**
     * 停止测量血压
     */
    fun stopBp(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BpmBleInterface).let {
                        LepuBleLog.d(tag, "it as BpmBleInterface--stopBp")
                        it.stopBp()
                    }
                }
            }
            Bluetooth.MODEL_BPW1 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Bpw1BleInterface).let {
                        LepuBleLog.d(tag, "it as Bpw1BleInterface--stopBp")
                        it.stopBp()
                    }
                }
            }
            Bluetooth.MODEL_PC100 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Pc100BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc100BleInterface--stopBp")
                        it.stopBp()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "stopBp model error ")
        }
    }

    /**
     * 烧录出厂信息
     */
    fun burnFactoryInfo(model: Int, config: FactoryConfig) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
        }
    }

    /**
     * 加密Flash
     */
    fun burnLockFlash(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--burnLockFlash")
                        it.burnLockFlash()
                    }
                }
            }
        }
    }

    /**
     * 获取Bpm设备文件列表
     */
    fun getBpmFileList(model: Int, map: HashMap<String, Any>){
        if (!checkService()) return
        if (model != Bluetooth.MODEL_BPM){
            LepuBleLog.d(tag,"getBpmFileList, 无效model：$model" )
            return
        }
        getInterface(model)?.let { it1 ->
            (it1 as BpmBleInterface).let {
                LepuBleLog.d(tag, "it as BpmBleInterface--getBpmFileList")
                it.getBpmFileList(map)
            }
        }

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


    /**
     * er1/duoek获取参数
     */
    fun getEr1VibrateConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N ->{
                getInterface(model)?.let { ble ->
                    (ble as Er1BleInterface).getVibrateConfig()
                }
            }
            else -> LepuBleLog.e(tag, "model error")
        }

    }

    /**
     * er1设置参数
     */
    fun setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N -> {
                getInterface(model)?.let { ble ->
                    (ble as Er1BleInterface).setVibrateConfig(switcher, threshold1, threshold2)
                }
            }
            else -> LepuBleLog.e(tag, "model error")

        }

    }

    /**
     * duoek设置参数
     */
    fun setEr1Vibrate(model: Int,switcher: Boolean, vector: Int, motionCount: Int,motionWindows: Int ){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_DUOEK -> {
                getInterface(model)?.let { ble ->
                    (ble as Er1BleInterface).setVibrateConfig(switcher, vector, motionCount, motionWindows)
                }
            }
            else -> LepuBleLog.e(tag, "model error")

        }
    }

    /**
     * 获取配置信息（bp2，bp2a，bp2t，bp2w，le bp2w）
     */
    fun bp2GetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2GetConfig")
                        it.getConfig()
                    }
                }
            }
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2GetConfig")
                        it.getConfig()
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2GetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetConfig model error")

        }

    }

    /**
     * 设置提示音开关（bp2，bp2a）
     */
    @JvmOverloads
    fun bp2SetConfig(model: Int, switch: Boolean, volume: Int = 2){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SetConfig")
                        it.setConfig(switch, volume)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2SetConfig model error")
        }
    }
    /**
     * 配置参数（bp2w，le bp2w）
     */
    fun bp2SetConfig(model: Int, config: Bp2Config) {
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2SetConfig")
                        it.setConfig(config)
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2SetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2SetConfig model error")
        }
    }

    /**
     * 设置提示音开关（bp2，bp2a，bp2t）
     */
    fun bp2SetPhyState(model: Int, state: Bp2BlePhyState){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SetPhyState")
                        it.setPhyState(state)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2SetPhyState model error")
        }
    }
    /**
     * 设置提示音开关（bp2，bp2a，bp2t）
     */
    fun bp2GetPhyState(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2GetPhyState")
                        it.getPhyState()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetPhyState model error")
        }
    }

    /**
     * 切换设备状态（bp2，bp2a，bp2t，bp2w，le bp2w）
     * @param state Bp2BleCmd.SwitchState
     */
    fun bp2SwitchState(model: Int, state: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SwitchState")
                        it.switchState(state)
                    }
                }
            }
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2SwitchState")
                        it.switchState(state)
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2SwitchState")
                        it.switchState(state)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2SwitchState model error")
        }
    }

    /**
     * 删除文件（bp2w，le bp2w）
     */
    fun bp2DeleteFile(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2DeleteFile")
                        it.deleteFile()
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2DeleteFile")
                        it.deleteFile()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2DeleteFile model error")
        }
    }

    /**
     * 获取设备状态（bp2，bp2a，bp2t，bp2w，le bp2w）
     */
    fun bp2GetRtState(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2GetRtState")
                        it.getRtState()
                    }
                }
            }
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2GetRtState")
                        it.getRtState()
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2GetRtState")
                        it.getRtState()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetRtState model error")
        }
    }

    /**
     * 获取路由（bp2w，le bp2w）
     */
    fun bp2GetWifiDevice(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2GetWifiDevice")
                        it.getWifiDevice()
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2GetWifiDevice")
                        it.getWifiDevice()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetWifiDevice model error")
        }
    }

    /**
     * 配置WiFi信息（bp2w，le bp2w）
     */
    fun bp2SetWifiConfig(model: Int, config: Bp2WifiConfig) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2SetWifiConfig")
                        it.setWifiConfig(config)
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2SetWifiConfig")
                        it.setWifiConfig(config)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2SetWifiConfig model error")
        }
    }

    /**
     * 获取WiFi配置信息（bp2w，le bp2w）
     */
    fun bp2GetWifiConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2GetWifiConfig")
                        it.getWifiConfig()
                    }
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2GetWifiConfig")
                        it.getWifiConfig()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetWifiConfig model error")
        }
    }

    /**
     * 获取文件列表校验码（le bp2w）
     * @param fileType LeBp2wBleCmd.FileType
     */
    fun bp2GetFileListCrc(model: Int, fileType: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2GetFileListCrc")
                        it.getFileListCrc(fileType)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2GetFileListCrc model error")
        }
    }

    /**
     * 写用户信息（le bp2w）
     */
    fun bp2WriteUserList(model: Int, userList: LeBp2wUserList) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2WriteUserList")
                        it.writeUserList(userList)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "bp2WriteUserList model error")
        }
    }

    /**
     * 获取实时波形（O2Ring，BabyO2）
     */
    fun oxyGetRtWave(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as OxyBleInterface).getRtWave()
        }
    }

    /**
     * 获取实时参数值（O2Ring，BabyO2）
     */
    fun oxyGetRtParam(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as OxyBleInterface).getRtParam()
        }
    }

    /**
     * 实时PPG数据（O2Ring，BabyO2）
     */
    fun oxyGetPpgRt(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as OxyBleInterface).let {
                it.getPpgRT()
            }
        }
    }
    /**
     * 更新设备设置（O2Ring，BabyO2）
     */
    fun updateSetting(model: Int, type: String, value: Any) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as OxyBleInterface).let {
                it.updateSetting(type, value)
            }
        }
    }

    /**
     * 获取盒子信息（BabyO2N）
     */
    fun oxyGetBoxInfo(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as OxyBleInterface).let {
                it.getBoxInfo()
            }
        }
    }

    /**
     * 电量查询（pc80b）
     */
    fun sendHeartbeat(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC80B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc80BleInterface).let {
                        LepuBleLog.d(tag, "it as PC80BleInterface--sendHeartbeat")
                        it.sendHeartbeat()
                    }
                }
            }
        }
    }

    /**
     * 设置定时测量血压时间（Bpw1）
     * @param measureTime Array<String?> 字符串格式："startHH,startMM,stopHH,stopMM,interval,serialNum,totalNum"
     */
    fun bpw1SetMeasureTime(model: Int, measureTime: Array<String?>) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Bpw1BleInterface).let {
                LepuBleLog.d(tag, "it as Bpw1BleInterface--setMeasureTime")
                it.setMeasureTime(measureTime)
            }
        }
    }

    /**
     * 获取定时测量血压时间（Bpw1）
     */
    fun bpw1GetMeasureTime(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Bpw1BleInterface).let {
                LepuBleLog.d(tag, "it as Bpw1BleInterface--getMeasureTime")
                it.getMeasureTime()
            }
        }
    }

    /**
     * 设置定时测量开关（Bpw1）
     */
    fun bpw1SetTimingSwitch(model: Int, timingSwitch: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Bpw1BleInterface).let {
                LepuBleLog.d(tag, "it as Bpw1BleInterface--setTimingSwitch")
                it.setTimingSwitch(timingSwitch)
            }
        }
    }

    /**
     * F4,F5体脂秤
     */
    fun setUserInfo(model: Int, userInfo: FscaleUserInfo) {
        if (!checkService()) return

        when(model){
            Bluetooth.MODEL_F4_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F4ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F4ScaleBleInterface--setUserInfo")
                        it.setUserInfo(userInfo)
                    }
                }
            }
            Bluetooth.MODEL_F5_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F5ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F5ScaleBleInterface--setUserInfo")
                        it.setUserInfo(userInfo)
                    }
                }
            }
            else -> LepuBleLog.e(tag, "model error")
        }
    }
    fun setUserList(model: Int, userList: List<FscaleUserInfo>) {
        if (!checkService()) return

        when(model){
            Bluetooth.MODEL_F4_SCALE -> {
                getInterface(model)?.let {
                    it as F4ScaleBleInterface
                    it.setUserList(userList)
                }
            }
            Bluetooth.MODEL_F5_SCALE -> {
                getInterface(model)?.let {
                    it as F5ScaleBleInterface
                    it.setUserList(userList)
                }
            }
            else -> LepuBleLog.e(tag, "model error")
        }
    }

    /**
     * 配置参数（ap20）
     * @param type Ap20BleCmd.ConfigType
     * @param config
     * 0 设置背光等级（0-5）
     * 1 警报功能开关（0 off，1 on）
     * 2 血氧过低阈值（85-99）
     * 3 脉率过低阈值（30-99）
     * 4 脉率过高阈值（100-250）
     */
    fun ap20SetConfig(model: Int, type: Int, config: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Ap20BleInterface).let {
                LepuBleLog.d(tag, "it as Ap20BleInterface--setAp20Config")
                it.setConfig(type, config)
            }
        }
    }

    /**
     * 获取参数（ap20）
     * @param type Ap20BleCmd.ConfigType
     */
    fun ap20GetConfig(model: Int, type: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Ap20BleInterface).let {
                LepuBleLog.d(tag, "it as Ap20BleInterface--getAp20Config")
                it.getConfig(type)
            }
        }
    }

    /**
     * 使能实时数据发送（ap20）
     * @param type Ap20BleCmd.EnableType
     */
    fun ap20EnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Ap20BleInterface).let {
                LepuBleLog.d(tag, "it as Ap20BleInterface--ap20EnableRtData")
                it.enableRtData(type,enable)
            }
        }
    }

    /**
     * 获取电量（ap20）
     */
    fun ap20GetBattery(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Ap20BleInterface).let {
                LepuBleLog.d(tag, "it as Ap20BleInterface--ap20GetBattery")
                it.getBattery()
            }
        }
    }

    /**
     * 请求绑定设备（lew3）
     */
    fun lew3BoundDevice(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Lew3BleInterface).let {
                LepuBleLog.d(tag, "it as Lew3BleInterface--boundDevice")
                it.boundDevice()
            }
        }
    }

    /**
     * 获取服务器信息（lew3）
     */
    fun lew3GetConfig(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Lew3BleInterface).let {
                LepuBleLog.d(tag, "it as Lew3BleInterface--lew3GetConfig")
                it.getConfig()
            }
        }
    }

    /**
     * 配置服务器信息（lew3）
     * @param server 服务器信息
     */
    fun lew3SetServer(model: Int, server: Lew3Config) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Lew3BleInterface).let {
                LepuBleLog.d(tag, "it as Lew3BleInterface--lew3SetServer")
                it.setServer(server)
            }
        }
    }

    /**
     * 获取电量（lew3）
     */
    fun lew3GetBattery(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Lew3BleInterface).let {
                LepuBleLog.d(tag, "it as Lew3BleInterface--lew3GetBattery")
                it.getBattery()
            }
        }
    }

    /**
     * 配置参数（sp20）
     * @param config Sp20Config
     */
    fun sp20SetConfig(model: Int, config: Sp20Config) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Sp20BleInterface).let {
                LepuBleLog.d(tag, "it as Sp20BleInterface--sp20SetConfig")
                it.setConfig(config)
            }
        }
    }

    /**
     * 获取参数（sp20）
     * @param type Ap20BleCmd.ConfigType
     */
    fun sp20GetConfig(model: Int, type: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Sp20BleInterface).let {
                LepuBleLog.d(tag, "it as Sp20BleInterface--sp20GetConfig")
                it.getConfig(type)
            }
        }
    }

    /**
     * 使能实时数据发送（sp20）
     * @param type Sp20BleCmd.EnableType
     */
    fun sp20EnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Sp20BleInterface).let {
                LepuBleLog.d(tag, "it as Sp20BleInterface--sp20EnableRtData")
                it.enableRtData(type,enable)
            }
        }
    }

    /**
     * 获取电量（ap20）
     */
    fun sp20GetBattery(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Sp20BleInterface).let {
                LepuBleLog.d(tag, "it as Sp20BleInterface--sp20GetBattery")
                it.getBattery()
            }
        }
    }

    /**
     * 删除历史数据（aoj20a）
     */
    fun aoj20aDeleteData(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Aoj20aBleInterface).let {
                LepuBleLog.d(tag, "it as Aoj20aBleInterface--aoj20aDeleteData")
                it.deleteData()
            }
        }
    }

    /**
     * 使能实时数据发送（pc60fw，pc66b，oxysmart，pod1w）
     * @param type Pc60FwBleCmd.EnableType
     */
    fun pc60fwEnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc60FwBleInterface).let {
                LepuBleLog.d(tag, "it as Pc60FwBleInterface--pc60fwEnableRtData")
                it.enableRtData(type,enable)
            }
        }
    }

    /**
     * 使能实时数据发送（pc68b）
     * @param type Pc68bBleCmd.EnableType
     */
    fun pc68bEnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bEnableRtData")
                it.enableRtData(type,enable)
            }
        }
    }

    /**
     * 删除文件（pc68b）
     */
    fun pc68bDeleteFile(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bDeleteFile")
                it.deleteFile()
            }
        }
    }

    /**
     * 定时获取状态（pc68b）
     * @param interval 时间间隔
     */
    fun pc68bGetStateInfo(model: Int, interval: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetStateInfo")
                it.getStateInfo(interval)
            }
        }
    }

    /**
     * 获取配置（pc68b）
     */
    fun pc68bGetConfig(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetConfig")
                it.getConfig()
            }
        }
    }

    /**
     * 配置参数（pc68b）
     * @param config Pc68bConfig
     */
    fun pc68bSetConfig(model: Int, config: Pc68bConfig) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bSetConfig")
                it.setConfig(config)
            }
        }
    }

    /**
     * 获取时间（pc68b）
     */
    fun pc68bGetTime(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc68bBleInterface).let {
                LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetTime")
                it.getTime()
            }
        }
    }

    fun ad5EnableRtData(model: Int, enable: Boolean) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Ad5FhrBleInterface).let {
                LepuBleLog.d(tag, "it as Ad5FhrBleInterface--ad5EnableRtData")
                it.enableRtData(enable)
            }
        }
    }

    fun startEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--startEcg")
                        it.startEcg()
                    }
                }
            }
        }
    }
    fun stopEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--stopEcg")
                        it.stopEcg()
                    }
                }
            }
        }
    }
    fun pc300SetEcgDataDigit(model: Int, digit: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetEcgDataDigit")
                it.setEcgDataDigit(digit)
            }
        }
    }
    /*fun pc300SetGluUnit(model: Int, unit: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetGluUnit")
                it.setGluUnit(unit)
            }
        }
    }
    fun pc300SetDeviceId(model: Int, id: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetDeviceId")
                it.setDeviceId(id)
            }
        }
    }
    fun pc300GetDeviceId(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300GetDeviceId")
                it.getDeviceId()
            }
        }
    }*/
    fun pc300SetGlucometerType(model: Int, type: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetGlucometerType")
                it.setGlucometerType(type)
            }
        }
    }
    fun pc300GetGlucometerType(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300GetGlucometerType")
                it.getGlucometerType()
            }
        }
    }
    /*fun pc300SetTempMode(model: Int, mode: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetTempMode")
                it.setTempMode(mode)
            }
        }
    }
    fun pc300GetTempMode(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300GetTempMode")
                it.getTempMode()
            }
        }
    }
    fun pc300SetBpMode(model: Int, mode: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetBpMode")
                it.setBpMode(mode)
            }
        }
    }
    fun pc300GetBpMode(model: Int) {
        if (!checkService()) return
        getInterface(model)?.let { it1 ->
            (it1 as Pc300BleInterface).let {
                LepuBleLog.d(tag, "it as Pc300BleInterface--pc300GetBpMode")
                it.getBpMode()
            }
        }
    }*/


}