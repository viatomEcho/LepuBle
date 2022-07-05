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
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ble.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
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
    var scanTimeout: Job? = null
    var scanTimer = object : CountDownTimer(3000, 3000) {
        override fun onTick(millisUntilFinished: Long) {

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
    }

    private fun initBle() {
        val bluetoothManager =
            application!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        LepuBleLog.d(tag, "initBle success")

        if (leScanner == null) {
            LepuBleLog.d(tag, "leScanner is null")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            support2MPhy = bluetoothAdapter!!.isLe2MPhySupported
        }
    }

    /**
     * 添加新model时 必须在此配置
     * @param m Int  根据model 配置interface
     * @param runRtImmediately Boolean 接收主机info响应后，是否立即开启实时监测任务
     * @return BleInterface
     */
    fun initInterfaces(m: Int, runRtImmediately: Boolean = false): BleInterface {
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
            Bluetooth.MODEL_BBSM_S2 -> {
                OxyBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER1_N -> {
                Er1BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }

            }
            Bluetooth.MODEL_ER2 -> {
                Er2BleInterface(m).apply {
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
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PC_60NW -> {
                Pc60FwBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }

            Bluetooth.MODEL_PC80B -> {
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
            Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F8_SCALE -> {
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
            Bluetooth.MODEL_AP20 -> {
                Ap20BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_SP20 -> {
                Sp20BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_LEW -> {
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
            Bluetooth.MODEL_CHECK_POD -> {
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
            Bluetooth.MODEL_PC300 -> {
                Pc300BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_PULSEBITEX -> {
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

            else -> {
//                return throw Exception("BleService initInterfaces() 未配置此model:$m")
                return throw Exception("LpWorkManager initInterfaces() 未配置此model:$m")
            }
        }


    }

    fun setScanDefineDevice(isScanDefineDevice: Boolean, isScanByName: Boolean, defineDevice: String) {
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
        LepuBleLog.d(tag, "start discover.....${vailFace.size()}, needPair = $needPair, isReconnecting = $isReconnecting")
        stopDiscover()

        if (vailFace.isEmpty() && isReconnecting)return

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
    fun reconnect(scanModel : IntArray,reconnectDeviceName: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {

        if (vailFace.isEmpty())return

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
            startDiscover(scanModel, needPair, isReconnecting = true)

            LepuBleLog.d(tag, "reconnectByName: => ${reconnectDeviceName.joinToString()} => ReScan: $reScan")
        }


    }


    /**
     * 重新连接开启扫描
     * 必定开启 isAutoConnecting = true
     *
     * 蓝牙名一致的设备重连必须使用蓝牙地址重连方法
     */
    fun reconnectByAddress(scanModel: IntArray, reconnectDeviceAddress: Array<String>, needPair: Boolean,  toConnectUpdater: Boolean = false) {

        if (vailFace.isEmpty())return

//        if (scanModel.size != reconnectDeviceAddress.size){
//            LepuBleLog.d(tag,"请检查重连model && reconnectDeviceAddress  size")
//            return
//        }
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
            startDiscover(scanModel, isReconnecting = true)
        }

        LepuBleLog.d(tag, "reconnect: => ${reconnectDeviceAddress.joinToString()} => ReScan: $reScan")
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

//        scanTimeout?.cancel()
        scanTimer.cancel()
        LepuBleLog.d(tag, "scanDevice scanTimeout.cancel()")

//        GlobalScope.launch {

        if (enable) {
            if (bluetoothAdapter?.isEnabled!!) {
                val settings: ScanSettings = if (Build.VERSION.SDK_INT >= 23) {
                    ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()
                }else {
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
                /*scanTimeout = GlobalScope.launch {
                    delay(10000)
                    startDiscover(scanModel, needPair, isReconnectScan)
                    LepuBleLog.d(tag, "-------scanTimeout-------")
                }*/
                LepuBleLog.d(tag, "scanDevice scanTimeout.start()")
                LepuBleLog.d(tag, "scanDevice started")
            }
        } else {
            if (bluetoothAdapter?.isEnabled!! && leScanCallback != null) {
                if (leScanner == null) {
                    leScanner = bluetoothAdapter?.bluetoothLeScanner
                }
                leScanner?.stopScan(leScanCallback)
                isDiscovery = false
                isWaitingScanResult = false
                LepuBleLog.d(tag, "scanDevice isWaitingScanResult = false")
            }
        }
//        }

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

            if (isWaitingScanResult) {
                isWaitingScanResult = false
                LepuBleLog.d(tag, "onScanResult isWaitingScanResult = false")
//                scanTimeout?.cancel()
                scanTimer.cancel()
                LepuBleLog.d(tag, "onScanResult scanTimeout.cancel()")
            }

            val device = result.device
            var deviceName = result.device.name
            val deviceAddress = result.device.address
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

                    LiveEventBus.get<ScanResult>(EventMsgConst.Discovery.EventDeviceFoundForUnRegister).post(result)
                }
                return
            }
            val b = Bluetooth(
                model,  /*ecgResult.getScanRecord().getDeviceName()*/
                deviceName,
                device,
                result.rssi
            )
//            if (vailFace.isEmpty()){
//                //切换设备 先断开连接 再clear interface
//                LepuBleLog.d(tag, "Warning: vailFace isEmpty!!")
//                stopDiscover()
//                return
//            }

            if(scanModel != null)
                if (!filterResult(b)) return

            if (isScanDefineDevice) {
                if (isScanByName) {
                    if (!b.name.equals(scanByName)) return
                    LepuBleLog.d(tag, "b.name == " + b.name)
                    LepuBleLog.d(tag, "scanByName == " + scanByName)
                } else {
                    if (!b.macAddr.equals(scanByAddress)) return
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
//                    if (isReconnectByAddress) {
                        // 避免体温计aoj20a在装电池开机后自动关机过程连接上设备，延迟重连
                        if (model == Bluetooth.MODEL_AOJ20A) {
                            GlobalScope.launch {
                                delay(2000)
                                vailFace.get(b.model)?.connect(application!!, b.device, true, toConnectUpdater)
                                LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
                            }
                        } else {
                            vailFace.get(b.model)?.connect(application!!, b.device, true, toConnectUpdater)
                            LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
                        }
//                    } else {
//                        if (BleServiceHelper.BleServiceHelper.canReconnectByName(b.model)) {
//                            vailFace.get(b.model)?.connect(application!!, b.device, true, toConnectUpdater)
//                            LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
//                        } else {
//                            LepuBleLog.d(tag, "发现需要重连的设备不可使用蓝牙名重连 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
//                        }
//                    }

                } else {
                    if (isReconnectScan) {
                        LepuBleLog.d(tag, "找到了新蓝牙名设备， 去连接Updater${b.name}")
                        if (b.name.contains("ER1 Updater")) { //如果扫描到的是新蓝牙名，连接
                            LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound_ER1_UPDATE).post(b)
                        }
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
        LepuBleLog.d(tag, "scanModel:${scanModel?.joinToString()}, b.model${b.model}")
        return scanModel?.contains(b.model) ?: return false
    }

}