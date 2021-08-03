package com.lepu.blepro.ble.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.lifecycle.LifecycleService
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.DfuUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.stream.Collectors.toList
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 一个为蓝牙通讯的服务的Service, {@link BleServiceHelper}是它的帮助类
 *
 * 1. 在Application onCreate中，通过BleServiceHelper初始化
 *
 * 2.继承LifecycleService使生命周期可被观察, 通过实现{@link BleServiceObserver}可自定义订阅者，订阅者通过观察此服务的生命周期变化，在不同阶段进行相应的工作
 *    如： 在Service OnCreate/ onDestroy时进行 数据库初始化/关闭
 *
 * 3.vailFace是保存BleInterface的SparseArray集合, 以设备的Model值为key, value为BleInterface
 *
 * 4.开启扫描时，可指定多个需要过滤的model，可指定是否发送pair配对通知
 *   - 来自绑定时发起的扫描：APP应该只在绑定页注册foundDevice和pair通知，接收到通知后由APP进行配对及连接
 *   - 来自重连时发起的扫描：不会发送pair和found通知。判断设备是否属于reconnectDeviceName， 是则由该model的Interface发起连接。
 *                         如果指定扫描多个model，将进入到多设备扫描状态{BleServiceHelper #isReconnectingMulti = true}则在某一个设备连接成功后，检查是否还有未连接的设备，是则继续重新开启扫描
 *
 * 5. 每次发起连接前必须关闭扫描
 *
 */

open class BleService: LifecycleService() {
    
    val tag: String = "BleService"

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
     * 发起重连扫描时应匹配的蓝牙名集合
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

    /**
     * 扫描通知严格模式
     */
    var isStrict: Boolean = false




    override fun onCreate() {
        super.onCreate()
        LepuBleLog.d(tag, "BleService onCreated")
        addObserver()
        initBle()
    }

    /**
     * 蓝牙适配器重新赋值，系统蓝牙开关切换时应该调用
     */
    fun reInitBle(){
        initBle()
    }


    /**
     * 为当前Service添加生命周期监听
     *
     */
    private fun addObserver(){
        observer?.let {
            lifecycle.addObserver(it)
            LepuBleLog.d(tag, "addObserver success")
        }
    }


    private fun initBle() {
        val bluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        LepuBleLog.d(tag, "initBle success")
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
            Bluetooth.MODEL_O2RING -> {
                OxyBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately
                    vailFace.put(m, this)
                    return this
                }
            }
            Bluetooth.MODEL_ER1,Bluetooth.MODEL_DUOEK -> {
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
            Bluetooth.MODEL_BP2 ,Bluetooth.MODEL_BP2A-> {
                Bp2BleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }

            Bluetooth.MODEL_PC60FW ,Bluetooth.MODEL_PC60FW-> {
                PC60FwBleInterface(m).apply {
                    this.runRtImmediately = runRtImmediately

                    vailFace.put(m, this)
                    return this
                }
            }

            else -> {
                return throw Exception("BleService initInterfaces() 未配置此model:$m")
            }
        }


    }



    private val binder = BleBinder()

    /**
     *
     * @param scanModel IntArray 本次扫描过滤的model
     * @param needPair Boolean 本次扫描是否需要发送配对Event通知
     * @param isReconnecting Boolean 本次扫描是否自来重连
     */
    fun startDiscover(scanModel: IntArray, needPair: Boolean = false, isReconnecting :Boolean = false) {
        LepuBleLog.d(tag, "start discover.....${vailFace.size()}, needPair = $needPair, isReconnecting = $isReconnecting")
        stopDiscover()
        if (vailFace.isEmpty())return

        BluetoothController.clear()
        this.needPair = needPair
        this.scanModel = scanModel
        this.isReconnectScan = isReconnecting

        isDiscovery = true

        GlobalScope.launch {
            delay(3000)
            scanDevice(true)
        }

        LepuBleLog.d(tag, "startScan...., scanModel:${scanModel.joinToString()}, needPair:$needPair")
    }

    /**
     * 连接前都应调用此方法
     */
    fun stopDiscover() {
        LepuBleLog.d(tag, "stopDiscover...")
        isDiscovery = false
        scanDevice(false)
    }


//
//    /**
//     * 重新连接开启扫描
//     * 必定开启 isAutoConnecting = true
//     */
//    fun reconnect(scanModel: IntArray, reconnectDeviceName: Array<String>, toConnectUpdater: Boolean = false) {
//
//        if (vailFace.isEmpty())return
//
////        if (scanModel.size != reconnectDeviceName.size){
////            LepuBleLog.d(tag,"请检查重连model && name  size")
////            return
////        }
//        var reScan = false
//
//        if (BleServiceHelper.BleServiceHelper.hasUnConnected(scanModel)) {
//            LepuBleLog.d(tag, "reconnectByAddress 有未连接的设备::::${scanModel.joinToString()}")
//            reScan = true
//        }
//        if (reScan) {
//            if (scanModel.size > 1) BleServiceHelper.BleServiceHelper.isReconnectingMulti = true
//            this.reconnectDeviceName = reconnectDeviceName
//            this.isReconnectByAddress = false
//            this.toConnectUpdater = toConnectUpdater
//            startDiscover(scanModel, isReconnecting = true)
//
//            LepuBleLog.d(tag, "reconnect::::: => ${scanModel?.joinToString()} => ReScan: $reScan")
//        }
//
//
//    }


    /**
     * 重新连接开启扫描
     * 必定开启 isAutoConnecting = true
     */
    fun reconnect( scanModel : IntArray,reconnectDeviceName: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false) {

        if (vailFace.isEmpty())return

        var reScan = false

        if (BleServiceHelper.BleServiceHelper.hasUnConnected()) {
            LepuBleLog.d(tag, "reconnectByName 有未连接的设备....}")
            reScan = true
        }
        if (reScan) {
            this.reconnectDeviceName.addAll(reconnectDeviceName.asList())
            this.isReconnectByAddress = false
            this.toConnectUpdater = toConnectUpdater
            this.needPair = needPair
            startDiscover(scanModel,needPair, isReconnecting = true)

            LepuBleLog.d(tag, "reconnectByName: => ${reconnectDeviceName.joinToString()} => ReScan: $reScan")
        }


    }


    /**
     * 重新连接开启扫描
     * 必定开启 isAutoConnecting = true
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

    private fun scanDevice(enable: Boolean) {
        LepuBleLog.d(tag, "scanDevice => $enable")



        GlobalScope.launch {

            if (enable) {
                if (bluetoothAdapter?.isEnabled!!) {
                    val settings: ScanSettings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build()
                    //                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
                    //                    filters.add(new ScanFilter.Builder().build());
                    leScanner?.startScan(null, settings, leScanCallback)
                    LepuBleLog.d(tag, "scanDevice started")
                }
            } else {
                if (bluetoothAdapter?.isEnabled!! && leScanCallback != null) {

                    leScanner?.stopScan(leScanCallback)
                }
            }
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

            val device = result.device
            var deviceName = result.device.name
            val deviceAddress = result.device.address
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = BluetoothController.getDeviceName(deviceAddress)
            }
            @Bluetooth.MODEL val model: Int = Bluetooth.getDeviceModel(deviceName)
            if (model == Bluetooth.MODEL_UNRECOGNIZED) {
                return
            }
            val b = Bluetooth(
                    model,  /*ecgResult.getScanRecord().getDeviceName()*/
                    deviceName,
                    device,
                    result.rssi
            )
            if (vailFace.isEmpty()){
                //切换设备 先断开连接 再clear interface
                LepuBleLog.d(tag, "Warning: vailFace isEmpty!!")
                stopDiscover()
                return
            }

            if(isStrict)
                if (!filterResult(b)) return

            if (needPair)
            result.scanRecord?.let {
                HashMap<String, Any>().apply {
                    this[EventMsgConst.Discovery.EventDeviceFound_Device] = b
                    this[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] = it

                    LepuBleLog.d(tag, "post paring...${b.name}")
                    LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound_ScanRecord).post(this)

                }

            }

            if (BluetoothController.addDevice(b)) {
                LepuBleLog.d(tag, "model = ${b.model}, isReconnecting::$isReconnectScan, b= ${b.name}, recName = ${reconnectDeviceName?.joinToString()}, " +
                        "toConnectUpdater = $toConnectUpdater,  isReconnectByAddress = $isReconnectByAddress ,  recAddress:${reconnectDeviceAddress?.joinToString()}")

                LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound).post(b)



                val isContains: Boolean = if(isReconnectByAddress) reconnectDeviceAddress?.contains(b.device.address) == true else reconnectDeviceName.contains(b.name)

                if (isReconnectScan && isContains){
                    stopDiscover()
                    LepuBleLog.d(tag, "发现需要重连的设备....去连接 model = ${b.model} name = ${b.name}  address = ${b.macAddr}")
                    vailFace.get(b.model)?.connect(this@BleService, b.device, true, toConnectUpdater)
                }

            }


        }

        override fun onBatchScanResults(results: List<ScanResult>) {
        }





        override fun onScanFailed(errorCode: Int) {
            LepuBleLog.e(tag, "scan error: $errorCode")
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                LepuBleLog.e(tag, "already start")

            }
            if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                LepuBleLog.e(tag, "scan settings not supported")
            }
            if (errorCode == 6) {
                LepuBleLog.e(tag, "too frequently")

            }
            if (errorCode == 2){ // 连接超时，去重连扫描时候可能碰到，解决办法重启蓝牙 待验证
                LepuBleLog.e(tag, "去重启蓝牙")
                whenScanFail()
            }

        }
    }

    fun whenScanFail(){
        bluetoothAdapter?.let {
            it.disable()
            LepuBleLog.d(tag, "关闭蓝牙中...")
            GlobalScope.launch {
                delay(1000L)
                LepuBleLog.d(tag, "1秒后 ble state = ${it.state}")
                if(it.state == BluetoothAdapter.STATE_OFF){
                    it.enable()
                    delay(1000L)
                    scanDevice(true)
                }
            }
            runBlocking { delay(1000L) }
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



    override fun onBind(p0: Intent): IBinder {
        super.onBind(p0)
        return binder
    }



    inner class BleBinder: Binder() {
        fun getService(): BleService = this@BleService
    }


    companion object {
        var observer: BleServiceObserver? = null

        @JvmStatic
        fun startService(context: Context) {
            LepuBleLog.d("startService")
            Intent(context, BleService::class.java).also { intent -> context.startService(intent)}
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, BleService::class.java)
            context.stopService(intent)
        }

    }



}