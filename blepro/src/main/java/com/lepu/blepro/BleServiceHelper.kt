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
import com.lepu.blepro.base.LpWorkManager
import com.lepu.blepro.ble.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.ble.data.r20.VentilationSetting
import com.lepu.blepro.ble.data.r20.WarningSetting
import com.lepu.blepro.ble.service.BleService
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    /*fun stopService(application: Application) {
        if (!this::bleService.isInitialized) {
            LepuBleLog.d(tag, "stopService !this::bleService.isInitialized")
            return
        }
        application.unbindService(bleConn)
        BleService.stopService(application)
        LepuBleLog.d(tag, "stopService")
    }*/

    /**
     * 在Application onCreate中初始化本单列,
     *
     */
    fun initService(application: Application, observer: BleServiceObserver?): BleServiceHelper {

        LepuBleLog.d(tag, "initService  start")
        /*BleService.observer = observer
        BleService.startService(application)

        Intent(application, BleService::class.java).also { intent ->
            application.bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }*/
        LpWorkManager.observer = observer
        LpWorkManager.application = application
        LpWorkManager.reInitBle()
        initCurrentFace()
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
        LepuBleLog.d(tag, "initModelConfig")
        this.modelConfig = modelConfig
        return this
    }
    fun initRtConfig(runRtConfig: SparseArray<Boolean>): BleServiceHelper{
        LepuBleLog.d(tag, "initRtConfig")
        this.runRtConfig = runRtConfig
        return this
    }

    /**
     * 服务连接成功时初始化vailFace
     * @return BleServiceHelper
     */
    private fun initCurrentFace(): BleServiceHelper{
//        if (this::bleService.isInitialized) {
        if (LpWorkManager.application != null) {

            LepuBleLog.d(tag, "initVailFace ${modelConfig.size()}")
            for (i in 0 until modelConfig.size()) {

                val model = modelConfig.get(modelConfig.keyAt(i))
                runRtConfig.get(model)?.let {
                    LepuBleLog.d(tag, "setInterfaces ===== $it")
                    setInterfaces(model, it)
                } ?: setInterfaces(model)
            }
            LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).post(true)

        }else{
            LepuBleLog.d(tag, "initVailFace failed!!!")
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
//        if (getInterface(model) == null) bleService.initInterfaces(model, runRtImmediately)
        if (getInterface(model) == null) LpWorkManager.initInterfaces(model, runRtImmediately)
    }


    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(): BleServiceHelper {
        if (!checkService()) return this
//        BleServiceHelper.bleService.reInitBle()
        LepuBleLog.d(tag, "reInitBle")
        LpWorkManager.reInitBle()
        return this
    }


    /**
     * 注册蓝牙状态改变的监听
     */
    internal fun subscribeBI(model: Int, observer: BleChangeObserver) {
        if (!checkService()){
            LepuBleLog.d(tag, "subscribeBI bleService.isInitialized  = false")
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
        /*bleService.setScanDefineDevice(false, false, "")
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }*/
        LepuBleLog.d(tag, "into startScan 扫描单个model")
        LpWorkManager.setScanDefineDevice(false, false, "")
        if (scanModel != null) {
            LpWorkManager.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            LpWorkManager.startDiscover(null, needPair)
        }
    }

    /**
     * 开始扫描 多model设备
     */
    @JvmOverloads
    fun startScan(scanModel: IntArray, needPair: Boolean = false) {
        if (!checkService()) return
        /*bleService.setScanDefineDevice(false, false, "")
        bleService.startDiscover(scanModel, needPair)*/
        LepuBleLog.d(tag, "into startScan 扫描多个model")
        LpWorkManager.setScanDefineDevice(false, false, "")
        LpWorkManager.startDiscover(scanModel, needPair)
    }

    /**
     * 具体蓝牙名扫描
     */
    @JvmOverloads
    fun startScanByName(deviceName: String, scanModel: Int? = null, needPair: Boolean = false) {
        if (!checkService()) return
        /*bleService.setScanDefineDevice(true, true, deviceName)
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }*/
        LepuBleLog.d(tag, "into startScanByName deviceName:$deviceName")
        LpWorkManager.setScanDefineDevice(true, true, deviceName)
        if (scanModel != null) {
            LpWorkManager.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            LpWorkManager.startDiscover(null, needPair)
        }
    }

    /**
     * 具体蓝牙地址扫描
     */
    @JvmOverloads
    fun startScanByAddress(address: String, scanModel: Int? = null, needPair: Boolean = false) {
        if (!checkService()) return
        /*bleService.setScanDefineDevice(true, false, address)
        if (scanModel != null) {
            bleService.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            bleService.startDiscover(null, needPair)
        }*/
        LepuBleLog.d(tag, "into startScanByAddress address:$address")
        LpWorkManager.setScanDefineDevice(true, false, address)
        if (scanModel != null) {
            LpWorkManager.startDiscover(intArrayOf(scanModel), needPair)
        } else {
            LpWorkManager.startDiscover(null, needPair)
        }
    }

    /**
     * 停止扫描
     * 连接之前调用该方法，并会重置扫描条件为默认值
     * 组合套装时,targetModel 会被重置为末尾添加的model
     */
    fun stopScan() {
//        if (checkService()) bleService.stopDiscover()
        if (checkService()) LpWorkManager.stopDiscover()
        LepuBleLog.d(tag, "stopScan")
    }

    /**
     * 获取model的interface
     */
    fun getInterface(model: Int): BleInterface? {
        if (!checkService()) return null

//        val vailFace = bleService.vailFace
        val vailFace = LpWorkManager.vailFace
        LepuBleLog.d(tag, "getInterface: getInterface => currentModel：$model, vailFaceSize：${vailFace.size()}}")
        vailFace.get(model)?.let {
            return vailFace.get(model)
        }?: kotlin.run {
            LepuBleLog.e(tag, "current model $model unsupported!!")
            return null
        }

    }

    /**
     * 获取服务中所有的interface
     * @return SparseArray<BleInterface>?
     */
    fun getInterfaces(): SparseArray<BleInterface>? {
        if (!checkService()) return null
//        return bleService.vailFace
        return LpWorkManager.vailFace
    }

    /**
     * 判断设备名重连是否符合标准：过滤pc80b,fhr,bpw1
     * 有app控制，sdk不做处理
     */
    fun canReconnectByName(model: Int): Boolean {
        LepuBleLog.d(tag, "canReconnectByName model:$model")
        return when(model) {
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_FHR, Bluetooth.MODEL_BPW1,
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE,
            Bluetooth.MODEL_AOJ20A, Bluetooth.MODEL_TV221U,
            Bluetooth.MODEL_FETAL, Bluetooth.MODEL_VTM_AD5,
            Bluetooth.MODEL_VCOMIN, Bluetooth.MODEL_PC300,
            Bluetooth.MODEL_PC300_BLE, Bluetooth.MODEL_LPM311,
            Bluetooth.MODEL_POCTOR_M3102, Bluetooth.MODEL_BIOLAND_BGM,
            Bluetooth.MODEL_PC_68B, Bluetooth.MODEL_BPM,
            Bluetooth.MODEL_PC80B_BLE2, Bluetooth.MODEL_PC200_BLE,
            Bluetooth.MODEL_S5_SCALE -> false
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
        LepuBleLog.d(tag, "into connect")
        if (!checkService()) return

        getInterface(model)?.let {
            it.connect(context, b, isAutoReconnect, toConnectUpdater)
        }
    }


    /**
     *  发起重连 允许扫描多个设备
     */
    @JvmOverloads
    fun reconnect(scanModel: IntArray, name: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect 名称重连多个model")
        if (!checkService()) return

//        bleService.reconnect(scanModel, name, needPair, toConnectUpdater)
        LpWorkManager.reconnect(scanModel, name, needPair, toConnectUpdater)

    }

    /**
     * 发起重连 一次只连一个
     * @param scanModel Int
     * @param name String
     */
    @JvmOverloads
    fun reconnect(scanModel: Int? = null, name: String, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnect 名称重连单个model")
        if (!checkService()) return
//        bleService.reconnect(intArrayOf(scanModel), arrayOf(name), needPair, toConnectUpdater)
        // 规避vihealth连接设备后回到首页扫描时，设备断开连接自动开启指定扫描覆盖首页扫描问题
        // 因此sdk调用自动重连时不做scanModel过滤
        if (scanModel == null) {
            LpWorkManager.reconnect(null, arrayOf(name), needPair, toConnectUpdater)
        } else {
            LpWorkManager.reconnect(intArrayOf(scanModel), arrayOf(name), needPair, toConnectUpdater)
        }

    }


    /**
     * madAddress 重连
     * @param scanModel IntArray
     * @param macAddress Array<String>
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, needPair: Boolean, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress 地址重连多个model")
        if (!checkService()) return

//        bleService.reconnectByAddress(scanModel, macAddress, needPair, toConnectUpdater)
        LpWorkManager.reconnectByAddress(scanModel, macAddress, needPair, toConnectUpdater)

    }

    /**
     * 通过MacAddress重连
     * @param scanModel Int
     * @param macAddress String
     */
    @JvmOverloads
    fun reconnectByAddress(scanModel: Int? = null, macAddress: String, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress 地址重连单个model")
        if (!checkService()) return
//        bleService.reconnectByAddress(intArrayOf(scanModel), arrayOf(macAddress), needPair, toConnectUpdater)
        if (scanModel == null) {
            LpWorkManager.reconnectByAddress(null, arrayOf(macAddress), needPair, toConnectUpdater)
        } else {
            LpWorkManager.reconnectByAddress(intArrayOf(scanModel), arrayOf(macAddress), needPair, toConnectUpdater)
        }

    }

    /**
     * 全部断开连接
     */
    fun disconnect(autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect autoReconnect:$autoReconnect")

        if (!checkService()) return
//        val vailFace = bleService.vailFace
        val vailFace = LpWorkManager.vailFace
        for (i in 0 until vailFace.size()) {
            getInterface(vailFace.keyAt(i))?.let { it ->
//                if (bleService.isDiscovery)stopScan()
                if (LpWorkManager.isDiscovery)stopScan()
                it.disconnect(autoReconnect)
            }
        }
    }

    /**
     * 断开指定model
     */
    fun disconnect(model: Int, autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect model:$model, autoReconnect:$autoReconnect")
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
            LepuBleLog.d(tag, "getSendCmd:${it.getSendCmd()}")
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
            LepuBleLog.d(tag, "getConnectState:${it.calBleState()}")
            return it.calBleState()
        }?: return Ble.State.UNKNOWN

    }

    /**
     * 获取扫描状态
     */
    fun isScanning(): Boolean{
        if (!checkService()) return false
        LepuBleLog.d(tag, "isScanning:${LpWorkManager.isDiscovery}")
//        return bleService.isDiscovery
        return LpWorkManager.isDiscovery
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
//        BleServiceHelper.bleService.needPair = needPair
        LpWorkManager.needPair = needPair
    }

    fun removeReconnectName(name: String) {
//        val iterator = bleService.reconnectDeviceName.iterator()
        val iterator = LpWorkManager.reconnectDeviceName.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == name) {
                iterator.remove()
//                LepuBleLog.d(tag, "从重连名单中移除 $name,  list = ${bleService.reconnectDeviceName.joinToString()}")
                LepuBleLog.d(tag, "从重连名单中移除 $name,  list = ${LpWorkManager.reconnectDeviceName.joinToString()}")
            }
        }
        LepuBleLog.d(tag, "removeReconnectName:$name")
    }

    fun removeReconnectAddress(address: String) {
//        val iterator = bleService.reconnectDeviceAddress.iterator()
        val iterator = LpWorkManager.reconnectDeviceAddress.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == address) {
                iterator.remove()
//                LepuBleLog.d(tag, "从重连名单中移除 $address,  list = ${bleService.reconnectDeviceAddress.joinToString()}")
                LepuBleLog.d(tag, "从重连名单中移除 $address,  list = ${LpWorkManager.reconnectDeviceAddress.joinToString()}")
            }
        }
        LepuBleLog.d(tag, "removeReconnectAddress:$address")
    }

    fun getReconnectDeviceName(): ArrayList<String>{
//        return bleService.reconnectDeviceName
        return LpWorkManager.reconnectDeviceName
    }

    /**
     * 是否存在未连接状态的interface
     * @param model IntArray
     * @return Boolean
     */
    fun hasUnConnected(model: IntArray): Boolean{
        LepuBleLog.d(tag, "into hasUnConnected...")
        if (!checkService()) return false
        for (m in model){
            getInterface(m)?.let {
                if (!it.state && !it.connecting) return true
            }
        }
        LepuBleLog.d(tag, "没有未连接和已连接中的设备")
        return false
    }

    fun hasUnConnected(): Boolean {
        LepuBleLog.d(tag, "into hasUnConnected...")
        if (!checkService()) return false
        for (x in 0 until LpWorkManager.vailFace.size()) {
            LpWorkManager.vailFace[LpWorkManager.vailFace.keyAt(x)]?.let {
                it.let {
                    LepuBleLog.d(tag, "hasUnConnected  已初始化interface的设备: model = ${it.model}")

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
        /*if (!this::bleService.isInitialized){
            LepuBleLog.d("Error: bleService unInitialized")
            return false
        }
        return true*/
        if (LpWorkManager.application == null){
            LepuBleLog.d("Error: LpWorkManager application == null")
            return false
        }
        return true
    }

    fun checkInterfaceType(model: Int, inter: BleInterface): Boolean {
        if (!checkService()) return false

        when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                return inter is Er1BleInterface
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
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
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_CMRING,
            Bluetooth.MODEL_OXYU, Bluetooth.MODEL_AI_S100,
            Bluetooth.MODEL_O2M_WPS, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                return inter is OxyBleInterface
            }
            Bluetooth.MODEL_BP2,Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T ->{
                return inter is Bp2BleInterface
            }
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PF_10,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_PC60NW_BLE,
            Bluetooth.MODEL_PC60NW_WPS, Bluetooth.MODEL_PC_60NW_NO_SN -> {
                return inter is Pc60FwBleInterface
            }
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                return inter is Pc80BleInterface
            }
            Bluetooth.MODEL_FHR -> {
                return inter is FhrBleInterface
            }
            Bluetooth.MODEL_BPW1 -> {
                return inter is Bpw1BleInterface
            }
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_S5_SCALE -> {
                return inter is F4ScaleBleInterface
            }
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE -> {
                return inter is F5ScaleBleInterface
            }
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
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
     * @param fileType 4G手表获取文件列表类型（LewBleCmd.ListType.SPORT, ECG, HR, OXY, SLEEP）
     * @param startTime 起始时间戳 单位秒
     */
    @JvmOverloads
    fun getFileList(model: Int, fileType: Int? = null, startTime: Long = 0){
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
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--getFileList")
                        if (fileType == null) {
                            it.getFileList()
                        } else {
                            it.getFileList(fileType, startTime)
                        }
                    }
                }
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--getFileList")
                        if (fileType == null) {
                            it.getFileList()
                        } else {
                            it.getFileList(fileType, startTime)
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
     * 获取血氧状态：设备返回状态不对，暂不能使用
     */
    fun pc100GetBoState(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC100 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc100BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc100BleInterface--pc100GetBoState")
                        it.getBoState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc100GetBoState current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "startBp current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "stopBp current model $model unsupported!!")
        }
    }

    /**
     * 烧录出厂信息
     */
    fun burnFactoryInfo(model: Int, config: FactoryConfig) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let {
                        LepuBleLog.d(tag, "it as OxyBleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            Bluetooth.MODEL_VTM01 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Vtm01BleInterface).let {
                        LepuBleLog.d(tag, "it as OxyBleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--burnFactoryInfo")
                        it.burnFactoryInfo(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "burnFactoryInfo current model $model unsupported!!")
        }
    }

    /**
     * 加密Flash
     */
    fun burnLockFlash(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--burnLockFlash")
                        it.burnLockFlash()
                    }
                }
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--burnLockFlash")
                        it.burnLockFlash()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "burnLockFlash current model $model unsupported!!")
        }
    }

    /**
     * 获取Bpm设备文件列表
     */
    fun getBpmFileList(model: Int, map: HashMap<String, Any>){
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BpmBleInterface).let {
                        LepuBleLog.d(tag, "it as BpmBleInterface--getBpmFileList")
                        it.getBpmFileList(map)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "getBpmFileList current model $model unsupported!!")
        }

    }

    fun getBpmRtState(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BpmBleInterface).let {
                        LepuBleLog.d(tag, "it as BpmBleInterface--getBpmRtState")
                        it.getRtState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "getBpmRtState current model $model unsupported!!")
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
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--setEr2SwitcherState")
                        it.setSwitcherState(hrFlag)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setEr2SwitcherState current model $model unsupported!!")
        }
    }

    /**
     * er2 获取hr开关状态
     * @param model Int
     */
    fun getEr2SwitcherState(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--getEr2SwitcherState")
                        it.getSwitcherState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "getEr2SwitcherState current model $model unsupported!!")
        }
    }


    /**
     * er1/duoek获取参数
     */
    fun getEr1VibrateConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--getEr1VibrateConfig")
                        it.getVibrateConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "getEr1VibrateConfig current model $model unsupported!!")
        }

    }

    /**
     * er1设置参数
     */
    fun setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--setEr1Vibrate")
                        it.setVibrateConfig(switcher, threshold1, threshold2)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setEr1Vibrate current model $model unsupported!!")

        }

    }

    /**
     * duoek设置参数
     */
    fun setEr1Vibrate(model: Int,switcher: Boolean, vector: Int, motionCount: Int,motionWindows: Int ){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3  -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--setEr1Vibrate")
                        it.setVibrateConfig(switcher, vector, motionCount, motionWindows)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setEr1Vibrate current model $model unsupported!!")

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
            else -> LepuBleLog.d(tag, "bp2GetConfig current model $model unsupported!!")

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
            else -> LepuBleLog.d(tag, "bp2SetConfig current model $model unsupported!!")
        }
    }
    /**
     * 配置参数（bp2，bp2a，bp2w，le bp2w）
     */
    fun bp2SetConfig(model: Int, config: Bp2Config) {
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SetConfig")
                        it.setConfig(config)
                    }
                }
            }
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
            else -> LepuBleLog.d(tag, "bp2SetConfig current model $model unsupported!!")
        }
    }

    /**
     * 设置理疗状态（bp2t）
     */
    fun bp2SetPhyState(model: Int, state: Bp2BlePhyState){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SetPhyState")
                        it.setPhyState(state)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2SetPhyState current model $model unsupported!!")
        }
    }
    /**
     * 获取理疗状态（bp2t）
     */
    fun bp2GetPhyState(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2GetPhyState")
                        it.getPhyState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2GetPhyState current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2SwitchState current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2DeleteFile current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2GetRtState current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2GetWifiDevice current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2SetWifiConfig current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2GetWifiConfig current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2GetFileListCrc current model $model unsupported!!")
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
            else -> LepuBleLog.d(tag, "bp2WriteUserList current model $model unsupported!!")
        }
    }

    /**
     * 同步时区时间（le bp2w）
     */
    fun bp2SyncUtcTime(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LE_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LeBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LeBp2wBleInterface--bp2SyncUtcTime")
                        it.syncUtcTime()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2SyncUtcTime current model $model unsupported!!")
        }
    }

    /**
     * 获取实时波形（O2系列）
     */
    fun oxyGetRtWave(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyGetRtWave")
                        it.getRtWave()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyGetRtWave current model $model unsupported!!")
        }
    }

    /**
     * 获取实时参数值（O2系列）
     */
    fun oxyGetRtParam(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyGetRtParam")
                        it.getRtParam()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyGetRtParam current model $model unsupported!!")
        }
    }

    /**
     * 实时PPG数据（O2系列）
     */
    fun oxyGetPpgRt(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyGetPpgRt")
                        it.getPpgRT()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyGetPpgRt current model $model unsupported!!")
        }
    }
    /**
     * 更新设备设置（O2系列）
     * 单个设置
     */
    fun updateSetting(model: Int, type: String, value: Any) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--updateSetting")
                        it.updateSetting(type, value)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "updateSetting current model $model unsupported!!")
        }
    }
    /**
     * 更新设备设置（O2系列）
     * 多个参数设置
     */
    fun updateSetting(model: Int, type: Array<String>, value: IntArray) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--updateSetting")
                        it.updateSetting(type, value)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "updateSetting current model $model unsupported!!")
        }
    }
    /**
     * 获取盒子信息（BabyO2N）
     */
    fun oxyGetBoxInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BABYO2N -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyGetBoxInfo")
                        it.getBoxInfo()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyGetBoxInfo current model $model unsupported!!")
        }
    }

    /**
     * 电量查询（pc80b）
     */
    fun sendHeartbeat(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc80BleInterface).let {
                        LepuBleLog.d(tag, "it as PC80BleInterface--sendHeartbeat")
                        it.sendHeartbeat()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sendHeartbeat current model $model unsupported!!")
        }
    }

    /**
     * 设置定时测量血压时间（Bpw1）
     * @param measureTime Array<String?> 字符串格式："startHH,startMM,stopHH,stopMM,interval,serialNum,totalNum"
     */
    fun bpw1SetMeasureTime(model: Int, measureTime: Array<String?>) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPW1 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bpw1BleInterface).let {
                        LepuBleLog.d(tag, "it as Bpw1BleInterface--bpw1SetMeasureTime")
                        it.setMeasureTime(measureTime)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bpw1SetMeasureTime current model $model unsupported!!")
        }
    }

    /**
     * 获取定时测量血压时间（Bpw1）
     */
    fun bpw1GetMeasureTime(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPW1 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bpw1BleInterface).let {
                        LepuBleLog.d(tag, "it as Bpw1BleInterface--bpw1GetMeasureTime")
                        it.getMeasureTime()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bpw1GetMeasureTime current model $model unsupported!!")
        }
    }

    /**
     * 设置定时测量开关（Bpw1）
     */
    fun bpw1SetTimingSwitch(model: Int, timingSwitch: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPW1 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bpw1BleInterface).let {
                        LepuBleLog.d(tag, "it as Bpw1BleInterface--bpw1SetTimingSwitch")
                        it.setTimingSwitch(timingSwitch)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bpw1SetTimingSwitch current model $model unsupported!!")
        }
    }

    /**
     * F4,F5体脂秤
     */
    fun setUserInfo(model: Int, userInfo: FscaleUserInfo) {
        if (!checkService()) return

        when(model){
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_S5_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F4ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F4ScaleBleInterface--setUserInfo")
                        it.setUserInfo(userInfo)
                    }
                }
            }
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F5ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F5ScaleBleInterface--setUserInfo")
                        it.setUserInfo(userInfo)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setUserInfo current model $model unsupported!!")
        }
    }
    fun setUserList(model: Int, userList: List<FscaleUserInfo>) {
        if (!checkService()) return

        when(model){
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_S5_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F4ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F4ScaleBleInterface--setUserList")
                        it.setUserList(userList)
                    }
                }
            }
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as F5ScaleBleInterface).let {
                        LepuBleLog.d(tag, "it as F5ScaleBleInterface--setUserList")
                        it.setUserList(userList)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setUserList current model $model unsupported!!")
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
        when (model) {
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Ap20BleInterface).let {
                        LepuBleLog.d(tag, "it as Ap20BleInterface--ap20SetConfig")
                        it.setConfig(type, config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "ap20SetConfig current model $model unsupported!!")
        }
    }

    /**
     * 获取参数（ap20）
     * @param type Ap20BleCmd.ConfigType
     */
    fun ap20GetConfig(model: Int, type: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Ap20BleInterface).let {
                        LepuBleLog.d(tag, "it as Ap20BleInterface--ap20GetConfig")
                        it.getConfig(type)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "ap20GetConfig current model $model unsupported!!")
        }
    }

    /**
     * 使能实时数据发送（ap20）
     * @param type Ap20BleCmd.EnableType
     * @param enable true打开，false关闭
     */
    fun ap20EnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Ap20BleInterface).let {
                        LepuBleLog.d(tag, "it as Ap20BleInterface--ap20EnableRtData")
                        it.enableRtData(type,enable)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "ap20EnableRtData current model $model unsupported!!")
        }
    }

    /**
     * 获取电量（ap20）
     */
    fun ap20GetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Ap20BleInterface).let {
                        LepuBleLog.d(tag, "it as Ap20BleInterface--ap20GetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "ap20GetBattery current model $model unsupported!!")
        }
    }

    /**
     * 时间
     */
    fun lewGetTime(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetTime")
                        it.getTime()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetTime current model $model unsupported!!")
        }
    }
    fun lewSetTime(model: Int, data: TimeData) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetTime")
                        it.setTime(data)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetTime current model $model unsupported!!")
        }
    }

    /**
     * 请求绑定/解绑设备（lew）
     * @param bound true绑定，false解绑
     */
    fun lewBoundDevice(model: Int, bound: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewBoundDevice")
                        it.boundDevice(bound)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewBoundDevice current model $model unsupported!!")
        }
    }
    /**
     * 获取电量（lew）
     */
    fun lewGetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetBattery current model $model unsupported!!")
        }
    }
    /**
     * 寻找设备
     */
    fun lewFindDevice(model: Int, on: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewFindDevice")
                        it.findDevice(on)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewFindDevice current model $model unsupported!!")
        }
    }
    /**
     * 获取设备联网模式
     */
    fun lewGetDeviceNetwork(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetDeviceNetwork")
                        it.getDeviceNetwork()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetDeviceNetwork current model $model unsupported!!")
        }
    }
    /**
     * 系统配置（包括语言、单位、翻腕亮屏、左右手）
     */
    fun lewGetSystemSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetSystemSetting")
                        it.getSystemSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetSystemSetting current model $model unsupported!!")
        }
    }
    fun lewSetSystemSetting(model: Int, setting: SystemSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetSystemSetting")
                        it.setSystemSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetSystemSetting current model $model unsupported!!")
        }
    }
    fun lewGetLanguage(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetLanguage")
                        it.getLanguage()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetLanguage current model $model unsupported!!")
        }
    }
    fun lewSetLanguage(model: Int, language: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetLanguage")
                        it.setLanguage(language)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetLanguage current model $model unsupported!!")
        }
    }
    fun lewGetUnit(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetUnit")
                        it.getUnit()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetUnit current model $model unsupported!!")
        }
    }
    fun lewSetUnit(model: Int, unit: UnitSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetUnit")
                        it.setUnit(unit)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetUnit current model $model unsupported!!")
        }
    }
    fun lewGetHandRaise(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetHandRaise")
                        it.getHandRaise()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetHandRaise current model $model unsupported!!")
        }
    }
    fun lewSetHandRaise(model: Int, handRaise: HandRaiseSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetHandRaise")
                        it.setHandRaise(handRaise)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetHandRaise current model $model unsupported!!")
        }
    }
    fun lewGetLrHand(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetLrHand")
                        it.getLrHand()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetLrHand current model $model unsupported!!")
        }
    }
    fun lewSetLrHand(model: Int, hand: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetLrHand")
                        it.setLrHand(hand)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetLrHand current model $model unsupported!!")
        }
    }
    /**
     * 勿扰模式
     */
    fun lewGetNoDisturbMode(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetNoDisturbMode")
                        it.getNoDisturbMode()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetNoDisturbMode current model $model unsupported!!")
        }
    }
    fun lewSetNoDisturbMode(model: Int, mode: NoDisturbMode) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetNoDisturbMode")
                        it.setNoDisturbMode(mode)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetNoDisturbMode current model $model unsupported!!")
        }
    }
    /**
     * app消息提醒开关
     */
    fun lewGetAppSwitch(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetAppSwitch")
                        it.getAppSwitch()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetAppSwitch current model $model unsupported!!")
        }
    }
    fun lewSetAppSwitch(model: Int, app: AppSwitch) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetAppSwitch")
                        it.setAppSwitch(app)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetAppSwitch current model $model unsupported!!")
        }
    }
    /**
     * 发送消息
     */
    fun lewSendNotification(model: Int, info: NotificationInfo) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSendNotification")
                        it.notification(info)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSendNotification current model $model unsupported!!")
        }
    }
    /**
     * 设备模式
     */
    fun lewGetDeviceMode(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetDeviceMode")
                        it.getDeviceMode()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetDeviceMode current model $model unsupported!!")
        }
    }
    fun lewSetDeviceMode(model: Int, mode: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetDeviceMode")
                        it.setDeviceMode(mode)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetDeviceMode current model $model unsupported!!")
        }
    }
    /**
     * 闹钟
     */
    fun lewGetAlarmClock(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetAlarmClock")
                        it.getAlarmClock()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetAlarmClock current model $model unsupported!!")
        }
    }
    fun lewSetAlarmClock(model: Int, info: AlarmClockInfo) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetAlarmClock")
                        it.setAlarmClock(info)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetAlarmClock current model $model unsupported!!")
        }
    }
    /**
     * 手机短信、来电消息提醒开关
     */
    fun lewGetPhoneSwitch(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetPhoneSwitch")
                        it.getPhoneSwitch()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetPhoneSwitch current model $model unsupported!!")
        }
    }
    fun lewSetPhoneSwitch(model: Int, phone: PhoneSwitch) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetPhoneSwitch")
                        it.setPhoneSwitch(phone)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetPhoneSwitch current model $model unsupported!!")
        }
    }
    /**
     * 用药提醒
     */
    fun lewGetMedicineRemind(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetMedicineRemind")
                        it.getMedicineRemind()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetMedicineRemind current model $model unsupported!!")
        }
    }
    fun lewSetMedicineRemind(model: Int, remind: MedicineRemind) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetMedicineRemind")
                        it.setMedicineRemind(remind)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetMedicineRemind current model $model unsupported!!")
        }
    }
    /**
     * 测量配置（包括运动目标值、达标提醒、久坐提醒、自测心率）
     */
    fun lewGetMeasureSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetMeasureSetting")
                        it.getMeasureSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetMeasureSetting current model $model unsupported!!")
        }
    }
    fun lewSetMeasureSetting(model: Int, setting: MeasureSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetMeasureSetting")
                        it.setMeasureSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetMeasureSetting current model $model unsupported!!")
        }
    }
    fun lewGetSportTarget(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetSportTarget")
                        it.getSportTarget()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetSportTarget current model $model unsupported!!")
        }
    }
    fun lewSetSportTarget(model: Int, target: SportTarget) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetSportTarget")
                        it.setSportTarget(target)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetSportTarget current model $model unsupported!!")
        }
    }
    fun lewGetTargetRemind(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetTargetRemind")
                        it.getTargetRemind()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetTargetRemind current model $model unsupported!!")
        }
    }
    fun lewSetTargetRemind(model: Int, remind: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetTargetRemind")
                        it.setTargetRemind(remind)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetTargetRemind current model $model unsupported!!")
        }
    }
    fun lewGetSittingRemind(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetSittingRemind")
                        it.getSittingRemind()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetSittingRemind current model $model unsupported!!")
        }
    }
    fun lewSetSittingRemind(model: Int, remind: SittingRemind) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetSittingRemind")
                        it.setSittingRemind(remind)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetSittingRemind current model $model unsupported!!")
        }
    }
    fun lewGetHrDetect(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetHrDetect")
                        it.getHrDetect()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetHrDetect current model $model unsupported!!")
        }
    }
    fun lewSetHrDetect(model: Int, detect: HrDetect) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetHrDetect")
                        it.setHrDetect(detect)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetHrDetect current model $model unsupported!!")
        }
    }
    /**
     * 自测血氧
     */
    fun lewGetOxyDetect(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetOxyDetect")
                        it.getOxyDetect()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetOxyDetect current model $model unsupported!!")
        }
    }
    fun lewSetOxyDetect(model: Int, detect: OxyDetect) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetOxyDetect")
                        it.setOxyDetect(detect)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetOxyDetect current model $model unsupported!!")
        }
    }
    /**
     * 用户信息
     */
    fun lewGetUserInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetUserInfo")
                        it.getUserInfo()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetUserInfo current model $model unsupported!!")
        }
    }
    fun lewSetUserInfo(model: Int, info: UserInfo) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetUserInfo")
                        it.setUserInfo(info)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetUserInfo current model $model unsupported!!")
        }
    }
    /**
     * 通讯录
     */
    fun lewGetPhoneBook(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetPhoneBook")
                        it.getPhoneBook()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetPhoneBook current model $model unsupported!!")
        }
    }
    fun lewSetPhoneBook(model: Int, book: PhoneBook) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetPhoneBook")
                        it.setPhoneBook(book)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetPhoneBook current model $model unsupported!!")
        }
    }
    /**
     * 紧急联系人
     */
    fun lewGetSosContact(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetSosContact")
                        it.getSosContact()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetSosContact current model $model unsupported!!")
        }
    }
    fun lewSetSosContact(model: Int, sos: SosContact) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetSosContact")
                        it.setSosContact(sos)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetSosContact current model $model unsupported!!")
        }
    }
    /**
     * 副屏配置
     */
    fun lewGetSecondScreen(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetSecondScreen")
                        it.getSecondScreen()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetSecondScreen current model $model unsupported!!")
        }
    }
    fun lewSetSecondScreen(model: Int, screen: SecondScreen) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetSecondScreen")
                        it.setSecondScreen(screen)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetSecondScreen current model $model unsupported!!")
        }
    }
    /**
     * 编辑卡片
     */
    fun lewGetCards(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetCards")
                        it.getCards()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetCards current model $model unsupported!!")
        }
    }
    /**
     * @param cards LewBleCmd.Cards 排序数组
     */
    fun lewSetCards(model: Int, cards: IntArray) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetCards")
                        it.setCards(cards)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetCards current model $model unsupported!!")
        }
    }
    fun lewGetRtData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetRtData")
                        it.getRtData()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetRtData current model $model unsupported!!")
        }
    }
    /**
     * 心率阈值，大于等于提醒
     */
    fun lewGetHrThreshold(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetHrThreshold")
                        it.getHrThreshold()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetHrThreshold current model $model unsupported!!")
        }
    }
    fun lewSetHrThreshold(model: Int, threshold: HrThreshold) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetHrThreshold")
                        it.setHrThreshold(threshold)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetHrThreshold current model $model unsupported!!")
        }
    }
    /**
     * 血氧阈值，小于等于提醒
     */
    fun lewGetOxyThreshold(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewGetOxyThreshold")
                        it.getOxyThreshold()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewGetOxyThreshold current model $model unsupported!!")
        }
    }
    fun lewSetOxyThreshold(model: Int, threshold: OxyThreshold) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LewBleInterface).let {
                        LepuBleLog.d(tag, "it as LewBleInterface--lewSetOxyThreshold")
                        it.setOxyThreshold(threshold)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lewSetOxyThreshold current model $model unsupported!!")
        }
    }

    /**
     * 配置参数（sp20）
     * @param config Sp20Config
     */
    fun sp20SetConfig(model: Int, config: Sp20Config) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Sp20BleInterface).let {
                        LepuBleLog.d(tag, "it as Sp20BleInterface--sp20SetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sp20SetConfig current model $model unsupported!!")
        }
    }

    /**
     * 获取参数（sp20）
     * @param type Sp20BleCmd.ConfigType
     */
    fun sp20GetConfig(model: Int, type: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Sp20BleInterface).let {
                        LepuBleLog.d(tag, "it as Sp20BleInterface--sp20GetConfig")
                        it.getConfig(type)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sp20GetConfig current model $model unsupported!!")
        }
    }

    /**
     * 使能实时数据发送（sp20）
     * @param type Sp20BleCmd.EnableType
     * @param enable true打开，false关闭
     */
    fun sp20EnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Sp20BleInterface).let {
                        LepuBleLog.d(tag, "it as Sp20BleInterface--sp20EnableRtData")
                        it.enableRtData(type,enable)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sp20EnableRtData current model $model unsupported!!")
        }
    }

    /**
     * 获取电量（ap20）
     */
    fun sp20GetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Sp20BleInterface).let {
                        LepuBleLog.d(tag, "it as Sp20BleInterface--sp20GetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sp20GetBattery current model $model unsupported!!")
        }
    }

    /**
     * 删除历史数据（aoj20a）
     */
    fun aoj20aDeleteData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AOJ20A -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Aoj20aBleInterface).let {
                        LepuBleLog.d(tag, "it as Aoj20aBleInterface--aoj20aDeleteData")
                        it.deleteData()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "aoj20aDeleteData current model $model unsupported!!")
        }
    }
    /**
     * 获取最新测量数据（aoj20a）
     */
    fun aoj20aGetRtData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AOJ20A -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Aoj20aBleInterface).let {
                        LepuBleLog.d(tag, "it as Aoj20aBleInterface--aoj20aGetRtData")
                        it.getRtData()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "aoj20aGetRtData current model $model unsupported!!")
        }
    }

    /**
     * 使能实时数据发送（pc60fw，pc66b，oxysmart，pod1w）
     * @param type Pc60FwBleCmd.EnableType
     * @param enable true打开，false关闭
     */
    fun pc60fwEnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PF_10,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_PC60NW_BLE,
            Bluetooth.MODEL_PC60NW_WPS, Bluetooth.MODEL_PC_60NW_NO_SN -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc60FwBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc60FwBleInterface--pc60fwEnableRtData")
                        it.enableRtData(type, enable)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc60fwEnableRtData current model $model unsupported!!")
        }
    }
    fun pc60fwGetBranchCode(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PF_10,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_PC60NW_BLE,
            Bluetooth.MODEL_PC60NW_WPS, Bluetooth.MODEL_PC_60NW_NO_SN -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc60FwBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc60FwBleInterface--pc60fwGetBranchCode")
                        it.getBranchCode()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc60fwGetBranchCode current model $model unsupported!!")
        }
    }
    fun pc60fwSetBranchCode(model: Int, code: String) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PF_10,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_PC60NW_BLE,
            Bluetooth.MODEL_PC60NW_WPS, Bluetooth.MODEL_PC_60NW_NO_SN -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc60FwBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc60FwBleInterface--pc60fwSetBranchCode")
                        it.setBranchCode(code)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc60fwSetBranchCode current model $model unsupported!!")
        }
    }

    /**
     * 使能实时数据发送（pc68b）
     * @param type Pc68bBleCmd.EnableType
     * @param enable true打开，false关闭
     */
    fun pc68bEnableRtData(model: Int, type: Int, enable: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bEnableRtData")
                        it.enableRtData(type, enable)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bEnableRtData current model $model unsupported!!")
        }
    }

    /**
     * 删除文件（pc68b）
     */
    fun pc68bDeleteFile(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bDeleteFile")
                        it.deleteFile()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bDeleteFile current model $model unsupported!!")
        }
    }

    /**
     * 定时获取状态（pc68b）
     * @param interval 时间间隔
     */
    fun pc68bGetStateInfo(model: Int, interval: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetStateInfo")
                        it.getStateInfo(interval)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bGetStateInfo current model $model unsupported!!")
        }
    }

    /**
     * 获取配置（pc68b）
     */
    fun pc68bGetConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bGetConfig current model $model unsupported!!")
        }
    }

    /**
     * 配置参数（pc68b）
     * @param config Pc68bConfig
     */
    fun pc68bSetConfig(model: Int, config: Pc68bConfig) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bSetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bSetConfig current model $model unsupported!!")
        }
    }

    /**
     * 获取时间（pc68b）
     */
    fun pc68bGetTime(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc68bBleInterface).let {
                        LepuBleLog.d(tag, "it as Pc68bBleInterface--pc68bGetTime")
                        it.getTime()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc68bGetTime current model $model unsupported!!")
        }
    }

    fun ad5EnableRtData(model: Int, enable: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_VTM_AD5, Bluetooth.MODEL_FETAL -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Ad5FhrBleInterface).let {
                        LepuBleLog.d(tag, "it as Ad5FhrBleInterface--ad5EnableRtData")
                        it.enableRtData(enable)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "ad5EnableRtData current model $model unsupported!!")
        }
    }

    fun startEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--startEcg")
                        it.startEcg()
                    }
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--startEcg")
                        it.startEcg()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "startEcg current model $model unsupported!!")
        }
    }
    fun stopEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--stopEcg")
                        it.stopEcg()
                    }
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--stopEcg")
                        it.stopEcg()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "stopEcg current model $model unsupported!!")
        }
    }
    fun pc300SetEcgDataDigit(model: Int, digit: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetEcgDataDigit")
                        it.setEcgDataDigit(digit)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc300SetEcgDataDigit current model $model unsupported!!")
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
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--pc300SetGlucometerType")
                        it.setGlucometerType(type)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc300SetGlucometerType current model $model unsupported!!")
        }
    }
    fun pc300GetGlucometerType(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc300BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc300BleInterface--pc300GetGlucometerType")
                        it.getGlucometerType()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "pc300GetGlucometerType current model $model unsupported!!")
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

    fun lemDeviceSwitch(model: Int, on: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemDeviceSwitch")
                        it.deviceSwitch(on)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemDeviceSwitch current model $model unsupported!!")
        }
    }
    fun lemGetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemGetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemGetBattery current model $model unsupported!!")
        }
    }
    fun lemHeatMode(model: Int, on: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemHeatMode")
                        it.heatMode(on)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemHeatMode current model $model unsupported!!")
        }
    }
    fun lemMassageMode(model: Int, mode: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemMassageMode")
                        it.massageMode(mode)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemMassageMode current model $model unsupported!!")
        }
    }
    fun lemMassageLevel(model: Int, level: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemMassageLevel")
                        it.massageLevel(level)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemMassageLevel current model $model unsupported!!")
        }
    }
    fun lemMassageTime(model: Int, time: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LemBleInterface).let {
                        LepuBleLog.d(tag, "it as LemBleInterface--lemMassageTime")
                        it.massageTime(time)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lemMassageTime current model $model unsupported!!")
        }
    }

    fun er3GetConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er3BleInterface).let {
                        LepuBleLog.d(tag, "it as Er3BleInterface--er3GetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "er3GetConfig current model $model unsupported!!")
        }
    }
    /**
     * mode：心电测量模式
     * 0：监护模式（带宽0.5HZ-40HZ）
     * 1：手术模式（带宽1HZ-20HZ）
     * 2：ST模式（带宽0.05HZ-40HZ）
     */
    fun er3SetConfig(model: Int, mode: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er3BleInterface).let {
                        LepuBleLog.d(tag, "it as Er3BleInterface--er3SetConfig")
                        it.setConfig(mode)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "er3SetConfig current model $model unsupported!!")
        }
    }

    fun lepodGetRtParam(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--lepodGetRtParam")
                        it.getRtParam()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lepodGetRtParam current model $model unsupported!!")
        }
    }
    fun lepodGetConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--lepodGetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lepodGetConfig current model $model unsupported!!")
        }
    }
    /**
     * mode：心电测量模式
     * 0：监护模式（带宽0.5HZ-40HZ）
     * 1：手术模式（带宽1HZ-20HZ）
     * 2：ST模式（带宽0.05HZ-40HZ）
     */
    fun lepodSetConfig(model: Int, mode: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--lepodSetConfig")
                        it.setConfig(mode)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lepodSetConfig current model $model unsupported!!")
        }
    }

    fun vtm01GetOriginalData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_VTM01 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Vtm01BleInterface).let {
                        LepuBleLog.d(tag, "it as Vtm01BleInterface--vtm01GetOriginalData")
                        it.getOriginalData()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "vtm01GetOriginalData current model $model unsupported!!")
        }
    }
    fun vtm01GetRtParam(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_VTM01 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Vtm01BleInterface).let {
                        LepuBleLog.d(tag, "it as Vtm01BleInterface--vtm01GetRtParam")
                        it.getRtParam()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "vtm01GetRtParam current model $model unsupported!!")
        }
    }
    fun vtm01SleepMode(model: Int, on: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_VTM01 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Vtm01BleInterface).let {
                        LepuBleLog.d(tag, "it as Vtm01BleInterface--vtm01SleepMode")
                        it.sleepMode(on)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "vtm01SleepMode current model $model unsupported!!")
        }
    }

    // BTP
    fun btpGetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpGetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpGetBattery current model $model unsupported!!")
        }
    }
    fun btpGetConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpGetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpGetConfig current model $model unsupported!!")
        }
    }
    fun btpSetLowHr(model: Int, lowHr: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetLowHr")
                        it.setLowHr(lowHr)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetLowHr current model $model unsupported!!")
        }
    }
    fun btpSetHighHr(model: Int, highHr: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetHighHr")
                        it.setHighHr(highHr)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetHighHr current model $model unsupported!!")
        }
    }
    fun btpSetLowTemp(model: Int, lowTemp: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetLowTemp")
                        it.setLowTemp(lowTemp)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetLowTemp current model $model unsupported!!")
        }
    }
    fun btpSetHighTemp(model: Int, highTemp: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetHighTemp")
                        it.setHighTemp(highTemp)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetHighTemp current model $model unsupported!!")
        }
    }
    fun btpSetTempUnit(model: Int, unit: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetTempUnit")
                        it.setTempUnit(unit)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetTempUnit current model $model unsupported!!")
        }
    }
    fun btpSetSystemSwitch(model: Int, hrSwitch: Boolean, lightSwitch: Boolean, tempSwitch: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BTP -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BtpBleInterface).let {
                        LepuBleLog.d(tag, "it as BtpBleInterface--btpSetSystemSwitch")
                        it.setSystemSwitch(hrSwitch, lightSwitch, tempSwitch)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "btpSetSystemSwitch current model $model unsupported!!")
        }
    }

    // R20
    fun r20Echo(model: Int, data: ByteArray) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20Echo")
                        it.echo(data)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20Echo current model $model unsupported!!")
        }
    }
    fun r20GetBattery(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetBattery")
                        it.getBattery()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetBattery current model $model unsupported!!")
        }
    }
    /**
     * bound: 绑定/解绑
     */
    fun r20DeviceBound(model: Int, bound: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20DeviceBound")
                        it.deviceBound(bound)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20DeviceBound current model $model unsupported!!")
        }
    }
    fun r20SetUserInfo(model: Int, userInfo: com.lepu.blepro.ble.data.r20.UserInfo) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetUserInfo")
                        it.setUserInfo(userInfo)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetUserInfo current model $model unsupported!!")
        }
    }
    fun r20GetUserInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetUserInfo")
                        it.getUserInfo()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetUserInfo current model $model unsupported!!")
        }
    }
    /**
     * pin: 设备进入医生模式的密码
     * timestamp: 当前时间戳s
     */
    fun r20DoctorMode(model: Int, pin: String, timestamp: Long) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20DoctorMode")
                        it.doctorMode(pin, timestamp)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20DoctorMode current model $model unsupported!!")
        }
    }
    /**
     * deviceNum: 所需设备个数
     * 默认0，发送搜索到的所有WiFi
     */
    @JvmOverloads
    fun r20GetWifiList(model: Int, deviceNum: Int = 0) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetWifiList")
                        it.getWifiList(deviceNum)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetWifiList current model $model unsupported!!")
        }
    }
    fun r20SetWifiConfig(model: Int, config: Bp2WifiConfig) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetWifiConfig")
                        it.setWifiConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetWifiConfig current model $model unsupported!!")
        }
    }
    /**
     * option: Bit0:wifi Bit1:server Bit2:device
     * 默认3，WiFi+Server
     */
    fun r20GetWifiConfig(model: Int, option: Int = 3) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetWifiConfig")
                        it.getWifiConfig(option)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetWifiConfig current model $model unsupported!!")
        }
    }
    fun r20GetVersionInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetVersionInfo")
                        it.getVersionInfo()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetVersionInfo current model $model unsupported!!")
        }
    }
    fun r20GetSystemSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetSystemSetting")
                        it.getSystemSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetSystemSetting current model $model unsupported!!")
        }
    }
    fun r20SetSystemSetting(model: Int, setting: com.lepu.blepro.ble.data.r20.SystemSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetSystemSetting")
                        it.setSystemSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetSystemSetting current model $model unsupported!!")
        }
    }
    fun r20GetMeasureSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetMeasureSetting")
                        it.getMeasureSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetMeasureSetting current model $model unsupported!!")
        }
    }
    fun r20SetMeasureSetting(model: Int, setting: com.lepu.blepro.ble.data.r20.MeasureSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetMeasureSetting")
                        it.setMeasureSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetMeasureSetting current model $model unsupported!!")
        }
    }
    fun r20MaskTest(model: Int, start: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20MaskTest")
                        it.maskTest(start)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20MaskTest current model $model unsupported!!")
        }
    }
    fun r20GetVentilationSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetVentilationSetting")
                        it.getVentilationSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetVentilationSetting current model $model unsupported!!")
        }
    }
    fun r20SetVentilationSetting(model: Int, setting: VentilationSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetVentilationSetting")
                        it.setVentilationSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetVentilationeSetting current model $model unsupported!!")
        }
    }
    fun r20GetWarningSetting(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetWarningSetting")
                        it.getWarningSetting()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetWarningSetting current model $model unsupported!!")
        }
    }
    fun r20SetWarningSetting(model: Int, setting: WarningSetting) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20SetWarningSetting")
                        it.setWarningSetting(setting)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20SetWarningSetting current model $model unsupported!!")
        }
    }
    fun r20VentilationSwitch(model: Int, start: Boolean) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20VentilationSwitch")
                        it.ventilationSwitch(start)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20VentilationSwitch current model $model unsupported!!")
        }
    }
    fun r20GetRtState(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                getInterface(model)?.let { it1 ->
                    (it1 as R20BleInterface).let {
                        LepuBleLog.d(tag, "it as R20BleInterface--r20GetRtState")
                        it.getRtState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "r20GetRtState current model $model unsupported!!")
        }
    }
}