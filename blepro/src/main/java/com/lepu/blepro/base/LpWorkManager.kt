package com.lepu.blepro.base

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.SparseArray
import androidx.core.util.isEmpty
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.HexString
import com.lepu.blepro.utils.LepuBleLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object LpWorkManager {

    val tag: String = "LpWorkManager"

    var application: Application? = null
    var observer: BleServiceObserver? = null

    /**
     * 保存可用的BleInterface集合
     */
    var vailFace: SparseArray<BleInterface> = SparseArray()
    var vailManager: SparseArray<LpBleManager> = SparseArray()

    /**
     *  指定扫描的Model，扫描结果根据它的值过滤
     *  每次开启扫描时传入指定model，被重新赋值
     *
     */
    var scanModel: IntArray? = null

    /**
     * 扫描指定设备
     */
    var isScanDefineDevice = false
    var isScanByName = false
    var scanByName = ""
    var scanByAddress = ""

    /**
     * 本次扫描是否需要发送配对信息
     * 默认： false
     * 开启扫描被重新赋值
     */
    var needPair: Boolean = false

    /**
     * 本次扫描是否来自重连(已知蓝牙名)，默认false，通过reconnect()开启扫描被赋值true
     */
    var isReconnectScan: Boolean = false

    /**
     * 发起重连扫描时应匹配的蓝牙名的集合
     *
     */
    var reconnectDeviceName: ArrayList<String> = ArrayList()

    /**
     * 发起重连扫描时应匹配的蓝牙macAddress集合
     */
    var reconnectDeviceAddress:  ArrayList<String> = ArrayList()

    var isReconnectByAddress: Boolean = false


    /**
     * address重连时检查是否是Updater
     */
    var toConnectUpdater: Boolean = false

    var support2MPhy: Boolean = false

    /**
     * 等待扫描结果（当status=6重新开启扫描）
     */
    var isWaitingScanResult = false
    var scanTimer = object : CountDownTimer(3000, 3000) {
        override fun onTick(millisUntilFinished: Long) {
            LepuBleLog.d(tag, "-------scanTimer-onTick------")
        }

        override fun onFinish() {
            startDiscover(scanModel, needPair, isReconnectScan)
            LepuBleLog.d(tag, "-------scanTimer-onFinish------")
        }

    }

    var startScan: Job? = null

    /**
     * 蓝牙适配器重新赋值，系统蓝牙开关切换时应该调用
     */
    fun reInitBle(){
        initBle()
        LepuBleLog.d(tag, "reInitBle")
    }

    private fun initBle() {
        val bluetoothManager =
            application?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        LepuBleLog.d(tag, "initBle success")

        if (leScanner == null) {
            LepuBleLog.d(tag, "leScanner is null")
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S)) {
            bluetoothAdapter?.let {
                support2MPhy = it.isLe2MPhySupported
            }
        }
    }

    /**
     * 添加新model时 必须在此配置
     * @param m Int  根据model 配置interface
     * @param runRtImmediately Boolean 接收主机info响应后，是否立即开启实时监测任务
     * @return BleInterface
     */
    fun initInterfaces(m: Int, runRtImmediately: Boolean = false): BleInterface? {
        LepuBleLog.d(tag, "initInterfaces start...${vailFace.size()},$m")

        vailFace.get(m)?.let { return it }
        when(m) {
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
                OxyBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                Er1BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }

            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                Er2BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ER3 -> {
                Er3BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                LepodBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BPM -> {
                BpmBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BP2 , Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                Bp2BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BP2W -> {
                Bp2wBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LE_BP2W -> {
                LeBp2wBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately
                    vailFace.put(m, this)
                    return this
                }
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
                Pc60FwBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }

            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                Pc80BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_FHR -> {
                FhrBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BPW1 -> {
                Bpw1BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_S5_SCALE -> {
                F4ScaleBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE -> {
                F5ScaleBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PC100 -> {
                Pc100BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                Ap20BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                Sp20BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                LewBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                VetcorderBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_TV221U -> {
                Vtm20fBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_AOJ20A -> {
                Aoj20aBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS -> {
                CheckmePodBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PC_68B -> {
                Pc68bBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                Pc300BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4,
            Bluetooth.MODEL_CHECKME -> {
                PulsebitBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_CHECKME_LE -> {
                CheckmeLeBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_VTM_AD5, Bluetooth.MODEL_FETAL -> {
                Ad5FhrBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_VCOMIN -> {
                VcominFhrBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEM -> {
                LemBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LES1 -> {
                LeS1BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LPM311 -> {
                Lpm311BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_POCTOR_M3102 -> {
                PoctorM3102BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BIOLAND_BGM -> {
                BiolandBgmBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_VTM01 -> {
                Vtm01BleInterface(m).apply {
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BTP -> {
                BtpBleInterface(m).apply {
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_LERES -> {
                R20BleInterface(m).apply {
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ECN -> {
                EcnBleInterface(m).apply {
                    vailFace.put(m, this)
                    return this
                }
            }

            else -> {
//                return throw Exception("BleService initInterfaces() 未配置此model:$m")
                return null
            }
        }


    }

    fun initManagers(m: Int, context: Context): LpBleManager? {
        LepuBleLog.d(tag, "initManagers start...${vailManager.size()},$m")

        vailManager.get(m)?.let { return it }
        when(m) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_CHECK_POD,
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4,
            Bluetooth.MODEL_CHECKME_LE, Bluetooth.MODEL_LES1,
            Bluetooth.MODEL_CHECKME, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2_WPS, Bluetooth.MODEL_CHECKME_POD_WPS -> {
                OxyBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3,
            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                Er1BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }

            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                Er2BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BPM -> {
                BpmBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BP2 , Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T,
            Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LE_BP2W -> {
                Bp2BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
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
                Pc60FwBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }

            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                Pc80BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_FHR -> {
                FhrBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BPW1 -> {
                Bpw1BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE,
            Bluetooth.MODEL_S5_SCALE -> {
                F4ScaleBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_MY_SCALE, Bluetooth.MODEL_F5_SCALE -> {
                F5ScaleBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PC100, Bluetooth.MODEL_PC300,
            Bluetooth.MODEL_PC300_BLE, Bluetooth.MODEL_PC200_BLE,
            Bluetooth.MODEL_GM_300SNT -> {
                Pc100BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_PC_68B,
            Bluetooth.MODEL_AP20_WPS -> {
                Ap20BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_VCOMIN,
            Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                Sp20BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                LewBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_TV221U -> {
                Vtm20fBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_AOJ20A -> {
                Aoj20aBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_VTM_AD5, Bluetooth.MODEL_FETAL -> {
                Ad5FhrBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEM -> {
                LemBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LPM311 -> {
                Lpm311BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_POCTOR_M3102 -> {
                PoctorM3102BleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_BIOLAND_BGM -> {
                BiolandBgmBleManager(context).apply {
                    vailManager.put(m, this)
                    return this
                }
            }

            else -> {
                return null
            }
        }


    }


    fun setScanDefineDevice(isScanDefineDevice: Boolean, isScanByName: Boolean, defineDevice: String) {
        LepuBleLog.d(tag, "setScanDefineDevice isScanDefineDevice:$isScanDefineDevice, isScanByName:$isScanByName, defineDevice:$defineDevice")
        this.isScanDefineDevice = isScanDefineDevice
        this.isScanByName = isScanByName
        if (isScanDefineDevice) {
            if (isScanByName) {
                scanByName = defineDevice
            } else {
                scanByAddress = defineDevice
            }
        } else {
            scanByName = ""
            scanByAddress = ""
        }

    }

    /**
     *
     * @param scanModel IntArray 本次扫描过滤的model
     * @param needPair Boolean 本次扫描是否需要发送配对Event通知
     * @param isReconnecting Boolean 本次扫描是否自来重连
     */
    fun startDiscover(scanModel: IntArray? = null, needPair: Boolean = false, isReconnecting :Boolean = false) {
        LepuBleLog.d(tag, "start discover.....vailFace.size = ${vailFace.size()}, needPair = $needPair, isReconnecting = $isReconnecting")
        stopDiscover()

        if (vailFace.isEmpty() && isReconnecting) {
            LepuBleLog.d(tag, "startDiscover vailFace.isEmpty(), isReconnecting:$isReconnecting")
            return
        }

        BluetoothController.clear()
        this.needPair = needPair
        this.scanModel = scanModel
        this.isReconnectScan = isReconnecting

        startScan?.cancel()

        startScan = GlobalScope.launch {
            delay(1000)
            scanDevice(true)
        }

        LepuBleLog.d(tag, "startScan...., scanModel:${scanModel?.joinToString()}, needPair:$needPair")
    }

    /**
     * 连接前都应调用此方法
     */
    fun stopDiscover() {
        LepuBleLog.d(tag, "stopDiscover...")
        startScan?.cancel()
        scanDevice(false)
    }

    /**
     * 重新连接开启扫描
     * 必定开启 isAutoConnecting = true
     *
     * 现在的多设备重连，无法SDK自动完成所有的设备的重连，需要连接一个后再次调用重连 去连另一个
     *
     * 蓝牙名一致的设备重连不能使用蓝牙名重连方法
     */
    fun reconnect(scanModel : IntArray? = null,reconnectDeviceName: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {

        if (vailFace.isEmpty()){
            LepuBleLog.d(tag, "reconnect vailFace.isEmpty()")
            return
        }

        var reScan = false

        if (BleServiceHelper.BleServiceHelper.hasUnConnected()) {
            LepuBleLog.d(tag, "reconnectByName 有未连接的设备....")
            reScan = true
        }
        if (reScan) {
            this.reconnectDeviceName.addAll(reconnectDeviceName.asList())

            this.isReconnectByAddress = false
            this.toConnectUpdater = toConnectUpdater
            this.needPair = needPair
            setScanDefineDevice(false, false, "")
            startDiscover(scanModel, needPair, isReconnecting = true)
        }
        LepuBleLog.d(tag, "reconnect: scanModel=> ${scanModel?.joinToString()} reconnectDeviceName=> ${reconnectDeviceName.joinToString()} ReScan: $reScan")
    }


    /**
     * 重新连接开启扫描
     * 必定开启 isAutoConnecting = true
     *
     * 蓝牙名一致的设备重连必须使用蓝牙地址重连方法
     */
    fun reconnectByAddress(scanModel: IntArray? = null, reconnectDeviceAddress: Array<String>, needPair: Boolean,  toConnectUpdater: Boolean = false) {

        if (vailFace.isEmpty()) {
            LepuBleLog.d(tag, "reconnectByAddress vailFace.isEmpty()")
            return
        }

        var reScan = false

        if (BleServiceHelper.BleServiceHelper.hasUnConnected()) {
            LepuBleLog.d(tag, "reconnectByAddress 有未连接的设备")
            reScan = true
        }
        if (reScan) {
            this.reconnectDeviceAddress.addAll(reconnectDeviceAddress.toList())

            this.isReconnectByAddress = true
            this.needPair = needPair
            this.toConnectUpdater = toConnectUpdater
            setScanDefineDevice(false, false, "")
            startDiscover(scanModel, isReconnecting = true)
        }

        LepuBleLog.d(tag, "reconnectByAddress: => ${reconnectDeviceAddress.joinToString()} => ReScan: $reScan")
    }



    var isDiscovery : Boolean = false
    private var bluetoothAdapter : BluetoothAdapter? = null
    private var leScanner : BluetoothLeScanner? = null

    /**
     * @param enable true(startScan) false(stopScan)
     * startScan前必须先stopScan
     */
    private fun scanDevice(enable: Boolean) {
        LepuBleLog.d(tag, "scanDevice => $enable")

        scanTimer.cancel()
        LepuBleLog.d(tag, "scanDevice scanTimer.cancel()")

        if (enable) {
            bluetoothAdapter?.isEnabled?.let {
                if (it) {
                    val settings: ScanSettings = if (Build.VERSION.SDK_INT >= 23) {
                        ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build()
                    } else {
                        ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()
                    }
                    //                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
                    //                    filters.add(new ScanFilter.Builder().build());
                    if (leScanner == null) {
                        leScanner = bluetoothAdapter?.bluetoothLeScanner
                    }
                    isWaitingScanResult = true
                    LepuBleLog.d(tag, "scanDevice isWaitingScanResult = true")
                    leScanner?.startScan(null, settings, leScanCallback)
                    isDiscovery = true
                    scanTimer.start()
                    LepuBleLog.d(tag, "scanDevice scanTimer.start()")
                }
            }
        } else {
            bluetoothAdapter?.isEnabled?.let {
                if (it) {
                    if (leScanner == null) {
                        leScanner = bluetoothAdapter?.bluetoothLeScanner
                    }
                    leScanner?.stopScan(leScanCallback)
                }
            }
            isDiscovery = false
            isWaitingScanResult = false
            LepuBleLog.d(tag, "scanDevice isWaitingScanResult = false")
        }

    }


    /**
     * lescan callback
     * 扫描到设备后，只负责发送设备及scanResult(nullable)
     */
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)

            // 扫描有结果返回，取消定时重扫机制
            if (isWaitingScanResult) {
                isWaitingScanResult = false
                LepuBleLog.d(tag, "onScanResult 扫描有结果返回")
                scanTimer.cancel()
                LepuBleLog.d(tag, "onScanResult 取消定时重扫机制")
            }

            val device = result.device
            var deviceName = result.device.name
            val deviceAddress = result.device.address
            // 更新广播的蓝牙名
            result.scanRecord?.let {
                deviceName = it.deviceName
                if (!TextUtils.isEmpty(deviceName)) {
                    deviceName = HexString.trimStr(deviceName)
                }
            }

            if (TextUtils.isEmpty(deviceName)) {
                deviceName = BluetoothController.getDeviceName(deviceAddress)
            }

            @Bluetooth.MODEL val model: Int = Bluetooth.getDeviceModel(deviceName)
            if (model == Bluetooth.MODEL_UNRECOGNIZED) {
                if (scanModel == null) {
                    if (needPair)
                        result.scanRecord?.let {
                            HashMap<String, Any>().apply {
                                this[EventMsgConst.Discovery.EventDeviceFound_Device] = device
                                this[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] = it

                                LepuBleLog.d(tag, "post paring...${device.name}")
                                LiveEventBus.get<HashMap<String, Any>>(EventMsgConst.Discovery.EventDeviceFound_ScanRecordUnRegister)
                                    .post(this)

                            }

                        }
                    LepuBleLog.d(tag, "onScanResult 外发sdk未能识别model的信息")
                    LiveEventBus.get<ScanResult>(EventMsgConst.Discovery.EventDeviceFoundForUnRegister).post(result)
                }
                LepuBleLog.d(tag, "onScanResult sdk未能识别到此model")
                return
            }
            val b = Bluetooth(
                model,  /*ecgResult.getScanRecord().getDeviceName()*/
                deviceName,
                device,
                result.rssi
            )

            if(scanModel != null)
                if (!filterResult(b)) {
                    LepuBleLog.d(tag, "filterResult 未扫描到指定model的设备")
                    return
                }

            if (isScanDefineDevice) {
                if (isScanByName) {
                    if (!b.name.equals(scanByName)) {
                        LepuBleLog.d(tag, "isScanDefineDevice 未扫描到指定蓝牙名的设备")
                        return
                    }
                    LepuBleLog.d(tag, "b.name == " + b.name)
                    LepuBleLog.d(tag, "scanByName == " + scanByName)
                } else {
                    if (!b.macAddr.equals(scanByAddress)) {
                        LepuBleLog.d(tag, "isScanDefineDevice 未扫描到指定蓝牙地址的设备")
                        return
                    }
                    LepuBleLog.d(tag, "b.macAddr == " + b.macAddr)
                    LepuBleLog.d(tag, "scanByAddress == " + scanByAddress)
                }
            }

            if (needPair)
                result.scanRecord?.let {
                    HashMap<String, Any>().apply {
                        this[EventMsgConst.Discovery.EventDeviceFound_Device] = b
                        this[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] = it

                        LepuBleLog.d(tag, "post paring...${b.name}")
                        LiveEventBus.get<HashMap<String, Any>>(EventMsgConst.Discovery.EventDeviceFound_ScanRecord).post(this)

                    }

                }

            if (BluetoothController.addDevice(b)) {
                LepuBleLog.d(tag, "model = ${b.model}, isReconnecting::$isReconnectScan, b= ${b.name}, recName = ${reconnectDeviceName.joinToString()}, " +
                        "toConnectUpdater = $toConnectUpdater,  isReconnectByAddress = $isReconnectByAddress ,  recAddress:${reconnectDeviceAddress.joinToString()}")

                LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound).post(b)

                val isContains: Boolean = if(isReconnectByAddress) reconnectDeviceAddress.contains(b.device.address) else reconnectDeviceName.contains(b.name)

                if (isReconnectScan && isContains){
                    stopDiscover()
                    if (model == Bluetooth.MODEL_AOJ20A || model == Bluetooth.MODEL_LPM311
                        || model == Bluetooth.MODEL_LEW || model == Bluetooth.MODEL_W12C) {
                        GlobalScope.launch {
                            delay(2000)
                            vailFace.get(b.model)?.connect(application!!, b.device, true, toConnectUpdater)
                            LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
                        }
                    } else {
                        vailFace.get(b.model)?.connect(application!!, b.device, true, toConnectUpdater)
                        LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
                    }
                }

            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
        }

        override fun onScanFailed(errorCode: Int) {
            LepuBleLog.e(tag, "scan error: $errorCode")
            LiveEventBus.get<Int>(EventMsgConst.Discovery.EventDeviceFoundError).post(errorCode)
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                LepuBleLog.e(tag, "Fails to start scan as BLE scan with the same settings is already started by the app.")

                // 执行BluetoothLeScanner.startScan前必须先stopScan，否则出现此错误
            }
            if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                LepuBleLog.e(tag, "Fails to start scan as app cannot be registered.")

                // 手机蓝牙未开启
            }
            if (errorCode == SCAN_FAILED_INTERNAL_ERROR) {
                LepuBleLog.e(tag, "Fails to start scan due an internal error")
            }
            if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                LepuBleLog.e(tag, "Fails to start power optimized scan as this feature is not supported.")
            }

            if (errorCode == 5) {
                // @hide
                LepuBleLog.e(tag, "Fails to start scan as it is out of hardware resources.")
            }
            if (errorCode == 6) {
                // @hide
                LepuBleLog.e(tag, "Fails to start scan as application tries to scan too frequently.")

            }

        }
    }

    /**
     * 过滤出当前类型设备
     * 组合套装时:
     * （singleScanMode = true）  model = scanModel的设备被过滤出
     * （singleScanMode = false） model 属于套装的设备被过滤出
     */
    private fun filterResult(b: Bluetooth): Boolean{
        LepuBleLog.d(tag, "filterResult scanModel:${scanModel?.joinToString()}, b.model:${b.model} b.name:${b.name} b.address:${b.macAddr}")
        return scanModel?.contains(b.model) ?: return false
    }

}