package com.lepu.blepro.ext

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.SparseArray
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.base.LpWorkManager
import com.lepu.blepro.ble.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.bp2w.Bp2wConfig
import com.lepu.blepro.ext.er1.Er1Config
import com.lepu.blepro.ext.er2.Er2Config
import com.lepu.blepro.ext.lpbp2w.LpBp2WifiConfig
import com.lepu.blepro.ext.lpbp2w.LpBp2wConfig
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import java.util.*
import kotlin.collections.ArrayList

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

    /**
     * 在Application onCreate中初始化本单列,
     *
     */
    fun initService(application: Application): BleServiceHelper {
        LepuBleLog.d(tag, "initService  start")
        LpWorkManager.application = application
        LpWorkManager.reInitBle()
        initCurrentFace()
        return this
    }

    fun initLog(log: Boolean): BleServiceHelper {
        LepuBleLog.setDebug(log)
        LepuBleLog.d(log.toString())
        return this
    }

    fun initRawFolder(folders: SparseArray<String>): BleServiceHelper {
        this.rawFolder = folders
        LepuBleLog.d(folders.toString())
        return this
    }
    fun initModelConfig(modelConfig: SparseArray<Int>): BleServiceHelper {
        LepuBleLog.d(tag, "initModelConfig")
        this.modelConfig = modelConfig
        return this
    }
    fun initRtConfig(runRtConfig: SparseArray<Boolean>): BleServiceHelper {
        LepuBleLog.d(tag, "initRtConfig")
        this.runRtConfig = runRtConfig
        return this
    }

    /**
     * 服务连接成功时初始化vailFace
     * @return BleServiceHelper
     */
    private fun initCurrentFace(): BleServiceHelper {
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

        } else {
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
        if (getInterface(model) == null) LpWorkManager.initInterfaces(model, runRtImmediately)
    }

    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(): BleServiceHelper {
        if (!checkService()) return this
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
        if (checkService()) LpWorkManager.stopDiscover()
        LepuBleLog.d(tag, "stopScan")
    }

    /**
     * 获取model的interface
     */
    fun getInterface(model: Int): BleInterface? {
        if (!checkService()) return null
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
            Bluetooth.MODEL_PC80B_BLE2, Bluetooth.MODEL_PC200_BLE -> false
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
    fun reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into reconnectByAddress 地址重连多个model")
        if (!checkService()) return
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
        val vailFace = LpWorkManager.vailFace
        for (i in 0 until vailFace.size()) {
            getInterface(vailFace.keyAt(i))?.let { it ->
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
    fun isScanning(): Boolean {
        if (!checkService()) return false
        LepuBleLog.d(tag, "isScanning:${LpWorkManager.isDiscovery}")
        return LpWorkManager.isDiscovery
    }

    /**
     * 获取实时任务停止状态
     */
    fun isRtStop(model: Int): Boolean {
        if (!checkService()) return false
        return getInterface(model)?.isRtStop ?: true
    }

    /**
     * 设置匹配状态
     */
    fun setNeedPair(needPair : Boolean) {
        LpWorkManager.needPair = needPair
    }

    fun removeReconnectName(name: String) {
        val iterator = LpWorkManager.reconnectDeviceName.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == name) {
                iterator.remove()
                LepuBleLog.d(tag, "从重连名单中移除 $name,  list = ${LpWorkManager.reconnectDeviceName.joinToString()}")
            }
        }
        LepuBleLog.d(tag, "removeReconnectName:$name")
    }

    fun removeReconnectAddress(address: String) {
        val iterator = LpWorkManager.reconnectDeviceAddress.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i == address) {
                iterator.remove()
                LepuBleLog.d(tag, "从重连名单中移除 $address,  list = ${LpWorkManager.reconnectDeviceAddress.joinToString()}")
            }
        }
        LepuBleLog.d(tag, "removeReconnectAddress:$address")
    }

    fun getReconnectDeviceName(): ArrayList<String> {
        return LpWorkManager.reconnectDeviceName
    }

    /**
     * 是否存在未连接状态的interface
     * @param model IntArray
     * @return Boolean
     */
    fun hasUnConnected(model: IntArray): Boolean {
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
    fun checkService(): Boolean {
        if (LpWorkManager.application == null){
            LepuBleLog.d("Error: LpWorkManager application == null")
            return false
        }
        return true
    }

    fun checkInterfaceType(model: Int, inter: BleInterface): Boolean {
        if (!checkService()) return false

        when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1  -> {
                return inter is Er1BleInterface
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
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
            Bluetooth.MODEL_O2M_WPS -> {
                return inter is OxyBleInterface
            }
            Bluetooth.MODEL_BP2,Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                return inter is Bp2BleInterface
            }
            Bluetooth.MODEL_BP2W -> {
                return inter is Bp2wBleInterface
            }
            Bluetooth.MODEL_LP_BP2W -> {
                return inter is LpBp2wBleInterface
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
            Bluetooth.MODEL_PC60NW_WPS -> {
                return inter is Pc60FwBleInterface
            }
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                return inter is Pc80BleInterface
            }
            Bluetooth.MODEL_PC100 -> {
                return inter is Pc100BleInterface
            }
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                return inter is Ap20BleInterface
            }
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                return inter is Sp20BleInterface
            }
            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                return inter is VetcorderBleInterface
            }
            Bluetooth.MODEL_TV221U -> {
                return inter is Vtm20fBleInterface
            }
            Bluetooth.MODEL_AOJ20A -> {
                return inter is Aoj20aBleInterface
            }
            Bluetooth.MODEL_CHECK_POD -> {
                return inter is CheckmePodBleInterface
            }
            Bluetooth.MODEL_PC_68B -> {
                return inter is Pc68bBleInterface
            }
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE -> {
                return inter is Pc300BleInterface
            }
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4,
            Bluetooth.MODEL_CHECKME -> {
                return inter is PulsebitBleInterface
            }
            Bluetooth.MODEL_CHECKME_LE -> {
                return inter is CheckmeLeBleInterface
            }
            Bluetooth.MODEL_LEM -> {
                return inter is LemBleInterface
            }
            Bluetooth.MODEL_LPM311 -> {
                return inter is Lpm311BleInterface
            }
            Bluetooth.MODEL_POCTOR_M3102 -> {
                return inter is PoctorM3102BleInterface
            }
            Bluetooth.MODEL_BIOLAND_BGM -> {
                return inter is BiolandBgmBleInterface
            }
            else -> {
                LepuBleLog.d(tag, "checkModel, 无效model：$model,${inter.javaClass}")
                return false
            }
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
    fun setRTDelayTime(model: Int, delayMillis: Long) {
        if (!checkService()) return
        getInterface(model)?.delayMillis = delayMillis
    }

    /**
     * 开启实时任务
     */
    fun startRtTask(model: Int) {
        if (!checkService()) return
        getInterface(model)?.runRtTask()
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

    // Oxy
    fun oxyGetInfo(model: Int) {
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
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    @JvmOverloads
    fun oxyReadFile(model: Int, fileName: String, userId: String = "") {
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
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS -> {
                getInterface(model)?.let {
                    LepuBleLog.d(tag, "--oxyReadFile--")
                    it.readFile(userId, fileName)
                }
            }
        }
    }
    fun oxyFactoryReset(model: Int) {
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
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS -> {
                getInterface(model)?.let {
                    LepuBleLog.d(tag, "--oxyFactoryReset--")
                    it.factoryReset()
                }
            }
        }
    }
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
            Bluetooth.MODEL_CMRING -> {
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
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_O2M_WPS -> {
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
    fun oxyUpdateSetting(model: Int, type: String, value: Any) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyUpdateSetting")
                        it.updateSetting(type, value)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyUpdateSetting current model $model unsupported!!")
        }
    }
    /**
     * type: "SetOxiThr", value: 80~95
     *       "SetOxiSwitch", value: (1) just sound or vibration: 0(0ff), 1(on)
     *                              (2) sound and vibration: 0(sound off, vibration off), 1(sound off, vibration on), 2(sound on, vibration off), 3(sound on, vibration on)
     *       "SetHRSwitch", value: (1) just sound or vibration: 0(0ff), 1(on)
     *                             (2) sound and vibration: 0(sound off, vibration off), 1(sound off, vibration on), 2(sound on, vibration off), 3(sound on, vibration on)
     *       "SetHRLowThr", value: 30~250
     *       "SetHRHighThr", value: 30~250
     *       "SetMotor", value: (1) KidsO2、Oxylink(0-5: MIN, 5-10: LOW, 10-17: MID, 17-22: HIGH, 22-35: MAX, 0 is off)
     *                          (2) O2Ring(0-20: MIN, 20-40: LOW, 40-60: MID, 60-80: HIGH, 80-100: MAX, 0 is off)
     *       "SetBuzzer", value: just for checkO2Plus(0-20：MIN，20-40：LOW，40-60：MID，60-80：HIGH，80-100：MAX，0 is off)
     */
    fun oxyUpdateSetting(model: Int, type: Array<String>, value: IntArray) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU -> {
                getInterface(model)?.let { it1 ->
                    (it1 as OxyBleInterface).let{
                        LepuBleLog.d(tag, "it as OxyBleInterface--oxyUpdateSetting")
                        it.updateSetting(type, value)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "oxyUpdateSetting current model $model unsupported!!")
        }
    }
    // ER1
    fun er1GetInfo(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun er1GetFileList(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    @JvmOverloads
    fun er1ReadFile(model: Int, fileName: String, userId: String = "", offset: Int = 0) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.readFile(userId, fileName, offset)
            }
        }
    }
    /**
     * 取消读取文件
     * @param model Int
     */
    fun er1CancelReadFile(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.let {
                    it.isCancelRF = true
                    LiveEventBus.get<Int>(EventMsgConst.Download.EventIsCancel).post(model)
                }
            }
        }
    }
    /**
     * 暂停 读取文件
     * @param model Int
     * @param isPause Boolean
     */
    fun er1PauseReadFile(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.let {
                    it.isPausedRF = true
                    LiveEventBus.get<Int>(EventMsgConst.Download.EventIsPaused).post(model)
                }
            }
        }
    }
    /**
     * 继续读取文件,并发送通知
     * @param model Int
     */
    @JvmOverloads
    fun er1ContinueReadFile(model: Int, fileName: String, userId: String = "", offset: Int = 0) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.let {
                    it.isPausedRF = false
                    it.continueRf(userId, fileName, offset)
                    LiveEventBus.get<Int>(EventMsgConst.Download.EventIsContinue).post(model)
                }
            }
        }
    }
    fun er1Reset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun er1FactoryReset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun er1FactoryResetAll(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    fun er1GetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--er1GetConfig")
                        it.getVibrateConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "er1GetConfig current model $model unsupported!!")
        }
    }
    fun er1SetConfig(model: Int, config: Er1Config){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er1BleInterface).let {
                        LepuBleLog.d(tag, "it as Er1BleInterface--er1SetConfig")
                        it.setVibrateConfig(config.isVibration, config.threshold1, config.threshold2)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "er1SetConfig current model $model unsupported!!")
        }
    }
    // BPM
    fun bpmGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun bpmGetFileList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    fun bpmGetRtState(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BPM -> {
                getInterface(model)?.let { it1 ->
                    (it1 as BpmBleInterface).let {
                        LepuBleLog.d(tag, "it as BpmBleInterface--bpmGetRtState")
                        it.getRtState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bpmGetRtState current model $model unsupported!!")
        }
    }
    // BP2
    fun bp2GetInfo(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun bp2GetFileList(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    fun bp2ReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    fun bp2Reset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun bp2FactoryReset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun bp2FactoryResetAll(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    fun bp2GetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2GetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2GetConfig current model $model unsupported!!")
        }
    }
    fun bp2SetConfig(model: Int, config: com.lepu.blepro.ext.bp2.Bp2Config){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2BleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2SetConfig")
                        it.setConfig(config.isSoundOn)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2SetConfig current model $model unsupported!!")
        }
    }
    // BP2W
    fun bp2wGetInfo(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun bp2wGetFileList(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    fun bp2wReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    fun bp2wReset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun bp2wFactoryReset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun bp2wFactoryResetAll(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    fun bp2wGetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2wGetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wGetConfig current model $model unsupported!!")
        }
    }
    fun bp2wSetConfig(model: Int, config: Bp2wConfig){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2BleInterface--bp2wSetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wSetConfig current model $model unsupported!!")
        }
    }
    fun bp2wDeleteFile(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2wDeleteFile")
                        it.deleteFile()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wDeleteFile current model $model unsupported!!")
        }
    }
    /**
     * 获取路由（bp2w）
     */
    /*fun bp2wGetWifiList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2wGetWifiList")
                        it.getWifiDevice()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wGetWifiList current model $model unsupported!!")
        }
    }*/

    /**
     * 配置WiFi信息（bp2w）
     */
    /*fun bp2wSetWifiConfig(model: Int, config: Bp2WifiConfig) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2wSetWifiConfig")
                        it.setWifiConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wSetWifiConfig current model $model unsupported!!")
        }
    }*/

    /**
     * 获取WiFi配置信息（bp2w）
     */
    /*fun bp2wGetWifiConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Bp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as Bp2wBleInterface--bp2wGetWifiConfig")
                        it.getWifiConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "bp2wGetWifiConfig current model $model unsupported!!")
        }
    }*/
    // LpBp2w
    fun lpBp2wGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun lpBp2wReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    fun lpBp2wReset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun lpBp2wFactoryReset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun lpBp2wFactoryResetAll(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    /**
     * fileType: 0-2
     * 0: Constant.LpBp2wListType.USER_TYPE
     * 1: Constant.LpBp2wListType.BP_TYPE
     * 2: Constant.LpBp2wListType.ECG_TYPE
     */
    fun lpBp2wGetFileList(model: Int, fileType: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wGetFileList")
                        it.getFileList(fileType)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wDeleteFile current model $model unsupported!!")
        }
    }
    fun lpBp2wGetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wGetConfig")
                        it.getConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wGetConfig current model $model unsupported!!")
        }
    }
    fun lpBp2wSetConfig(model: Int, config: LpBp2wConfig){
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wSetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wSetConfig current model $model unsupported!!")
        }
    }
    fun lpBp2wDeleteFile(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wDeleteFile")
                        it.deleteFile()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wDeleteFile current model $model unsupported!!")
        }
    }
    fun lpBp2WriteUserList(model: Int, userList: ArrayList<com.lepu.blepro.ext.lpbp2w.UserInfo>) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2WriteUserList")
                        it.writeUserList(userList)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2WriteUserList current model $model unsupported!!")
        }
    }
    /**
     * fileType: 0-2
     * 0: Constant.LpBp2wListType.USER_TYPE
     * 1: Constant.LpBp2wListType.BP_TYPE
     * 2: Constant.LpBp2wListType.ECG_TYPE
     */
    fun lpBp2wGetFileListCrc(model: Int, fileType: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wGetFileListCrc")
                        it.getFileListCrc(fileType)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wGetFileListCrc current model $model unsupported!!")
        }
    }
    /**
     * 获取路由（lp bp2w）
     */
    fun lpBp2wGetWifiList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wGetWifiList")
                        it.getWifiDevice()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wGetWifiList current model $model unsupported!!")
        }
    }

    /**
     * 配置WiFi信息（lp bp2w）
     */
    fun lpBp2wSetWifiConfig(model: Int, config: LpBp2WifiConfig) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wSetWifiConfig")
                        it.setWifiConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wSetWifiConfig current model $model unsupported!!")
        }
    }

    /**
     * 获取WiFi配置信息（lp bp2w）
     */
    fun lpBp2wGetWifiConfig(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LP_BP2W -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LpBp2wBleInterface).let {
                        LepuBleLog.d(tag, "it as LpBp2wBleInterface--lpBp2wGetWifiConfig")
                        it.getWifiConfig()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lpBp2wGetWifiConfig current model $model unsupported!!")
        }
    }
    // ER2
    fun er2GetInfo(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun er2GetFileList(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    fun er2ReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    fun er2Reset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun er2FactoryReset(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun er2FactoryResetAll(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    fun er2SetConfig(model: Int, config: Er2Config){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--er2SetConfig")
                        it.setConfig(config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "setEr2SwitcherState current model $model unsupported!!")
        }
    }
    fun er2GetConfig(model: Int){
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Er2BleInterface).let {
                        LepuBleLog.d(tag, "it as Er2BleInterface--er2GetConfig")
                        it.getSwitcherState()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "er2GetConfig current model $model unsupported!!")
        }
    }
    // PC60Fw
    fun pc60fwGetInfo(model: Int) {
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
            Bluetooth.MODEL_PC60NW_WPS -> {
                getInterface(model)?.getInfo()
            }
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
            Bluetooth.MODEL_PC60NW_WPS -> {
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
    // PC80B
    fun pc80bGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun pc80bGetBattery(model: Int) {
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
    // PC100
    fun pc100GetInfo(model: Int) {
        if (!checkService()) return
        when(model) {
            Bluetooth.MODEL_PC100 -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun pc100StartBp(model: Int) {
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_PC100 -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Pc100BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc100BleInterface--pc100StartBp")
                        it.startBp()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "pc100StartBp current model $model unsupported!!")
        }
    }
    fun pc100StopBp(model: Int) {
        if (!checkService()) return
        when(model){
            Bluetooth.MODEL_PC100 ->{
                getInterface(model)?.let { it1 ->
                    (it1 as Pc100BleInterface).let {
                        LepuBleLog.d(tag, "it as Pc100BleInterface--pc100StopBp")
                        it.stopBp()
                    }
                }
            }
            else -> LepuBleLog.e(tag, "pc100StopBp model error  ")
        }
    }
    // AP20
    fun ap20GetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    /**
     * type: 0-4
     * 0: Constant.Ap20ConfigType.BACK_LIGHT, config: 0~5
     * 1: Constant.Ap20ConfigType.ALARM_SWITCH, config: 0 off, 1 on
     * 2: Constant.Ap20ConfigType.LOW_OXY_THRESHOLD, config: 85~99
     * 3: Constant.Ap20ConfigType.LOW_HR_THRESHOLD, config: 30~99
     * 4: Constant.Ap20ConfigType.HIGH_HR_THRESHOLD, config: 100~250
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
     * type: 0-4
     * 0: Constant.Ap20ConfigType.BACK_LIGHT
     * 1: Constant.Ap20ConfigType.ALARM_SWITCH
     * 2: Constant.Ap20ConfigType.LOW_OXY_THRESHOLD
     * 3: Constant.Ap20ConfigType.LOW_HR_THRESHOLD
     * 4: Constant.Ap20ConfigType.HIGH_HR_THRESHOLD
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
    // SP20
    fun sp20GetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    /**
     * type: 2-4
     * 2: Constant.Sp20ConfigType.LOW_OXY_THRESHOLD, config: 85~99
     * 3: Constant.Sp20ConfigType.LOW_HR_THRESHOLD, config: 30~99
     * 4: Constant.Sp20ConfigType.HIGH_HR_THRESHOLD, config: 100~250
     */
    fun sp20SetConfig(model: Int, type: Int, config: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                getInterface(model)?.let { it1 ->
                    (it1 as Sp20BleInterface).let {
                        LepuBleLog.d(tag, "it as Sp20BleInterface--sp20SetConfig")
                        it.setConfig(type, config)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "sp20SetConfig current model $model unsupported!!")
        }
    }
    /**
     * type: 2-4
     * 2: Constant.Sp20ConfigType.LOW_OXY_THRESHOLD
     * 3: Constant.Sp20ConfigType.LOW_HR_THRESHOLD
     * 4: Constant.Sp20ConfigType.HIGH_HR_THRESHOLD
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
    // AOJ20a
    fun aoj20aGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AOJ20A -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun aoj20aGetFileList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_AOJ20A -> {
                getInterface(model)?.getFileList()
            }
        }
    }
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
    // CheckmePod
    fun checkmePodGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_CHECK_POD -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun checkmePodGetFileList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_CHECK_POD -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    // Pulsebit
    fun pulsebitExGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4, Bluetooth.MODEL_CHECKME -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun pulsebitExGetFileList(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4, Bluetooth.MODEL_CHECKME -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    fun pulsebitExReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4, Bluetooth.MODEL_CHECKME -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    // PC68B
    fun pc68bGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC_68B -> {
                getInterface(model)?.getInfo()
            }
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
    // PC300
    fun pc300GetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    // CheckmeLE
    fun checkmeLeGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_CHECKME_LE -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun checkmeLeReadFile(model: Int, fileName: String) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_CHECKME_LE -> {
                getInterface(model)?.readFile("", fileName)
            }
        }
    }
    /**
     * fileType: 0-3
     * 0: Constant.CheckmeLeListType.ECG_TYPE
     * 1: Constant.CheckmeLeListType.OXY_TYPE
     * 2: Constant.CheckmeLeListType.DLC_TYPE
     * 3: Constant.CheckmeLeListType.TEMP_TYPE
     */
    fun checkmeLeGetFileList(model: Int, fileType: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_CHECKME_LE -> {
                getInterface(model)?.let { it1 ->
                    (it1 as CheckmeLeBleInterface).let {
                        LepuBleLog.d(tag, "it as CheckmeLeBleInterface--checkmeLeGetList")
                        it.getFileList(fileType)
                    }
                }
            }
            else -> LepuBleLog.d(tag, "checkmeLeGetList current model $model unsupported!!")
        }
    }
    // LEM
    fun lemGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEM -> {
                getInterface(model)?.getInfo()
            }
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
    /**
     * mode: 0-4
     * 0: Vitality mode(Constant.LemMassageMode.VITALITY)
     * 1: Dynamic mode(Constant.LemMassageMode.DYNAMIC)
     * 2: Thump mode(Constant.LemMassageMode.HAMMERING)
     * 3: Soothing mode(Constant.LemMassageMode.SOOTHING)
     * 4: Automatic mode(Constant.LemMassageMode.AUTOMATIC)
     */
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
    /**
     * level: 0-15
     */
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
    /**
     * time: 0-2
     * 0: 15 min(Constant.LemMassageTime.MIN_15)
     * 1: 10min(Constant.LemMassageTime.MIN_10)
     * 2: 5min(Constant.LemMassageTime.MIN_5)
     */
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
    // LPM311
    fun lpm311GetData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LPM311 -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    // BiolandBgm
    fun biolandBgmGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BIOLAND_BGM -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun biolandBgmGetGluData(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_BIOLAND_BGM -> {
                getInterface(model)?.getFileList()
            }
        }
    }
    // ER3
    fun er3GetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun er3Reset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun er3FactoryReset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun er3FactoryResetAll(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_ER3 -> {
                getInterface(model)?.factoryResetAll()
            }
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
    // Lepod
    fun lepodGetInfo(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.getInfo()
            }
        }
    }
    fun lepodReset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.reset()
            }
        }
    }
    fun lepodFactoryReset(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.factoryReset()
            }
        }
    }
    fun lepodFactoryResetAll(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.factoryResetAll()
            }
        }
    }
    fun lepodStartEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--lepodStartEcg")
                        it.startEcg()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lepodStartEcg current model $model unsupported!!")
        }
    }
    fun lepodStopEcg(model: Int) {
        if (!checkService()) return
        when (model) {
            Bluetooth.MODEL_LEPOD -> {
                getInterface(model)?.let { it1 ->
                    (it1 as LepodBleInterface).let {
                        LepuBleLog.d(tag, "it as LepodBleInterface--lepodStopEcg")
                        it.stopEcg()
                    }
                }
            }
            else -> LepuBleLog.d(tag, "lepodStopEcg current model $model unsupported!!")
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
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE -> {
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
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE -> {
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

}