package com.lepu.blepro.ble.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.lifecycle.LifecycleService
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.OxyBleInterface
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.lepuble.ble.Er1BleInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * 一个为蓝牙通讯的服务的Service, {@link BleServiceHelper}是它的帮助类
 *
 * 1.单列，在Application onCreate中初始化
 *
 * 2.继承LifecycleService使生命周期可被观察, 通过实现{@link BleServiceObserver}可自定义订阅者，订阅者通过观察此服务的生命周期变化，在不同阶段进行相应的工作
 *    如： 在Service OnCreate/ onDestroy时进行 数据库初始化/关闭
 *
 * 3.vailFace是保存BleInterface的SparseArray集合, 以设备的Model值为key, 所以，不知支持不同类型（Model）设备同时使用，不支持同类型不同主机的情况
 *
 * 4. 扫描
 *      单设备模式： 正常扫描，过滤出扫描结果中的当前model的设备，如果配对成功通过LiveEventBus发送通知，
 *
 *
 *
 *
 *
 *
 */

class BleService: LifecycleService() {

    /**
     * 保存可用的BleInterface集合
     */
    var vailFace: SparseArray<BleInterface> = SparseArray()



    /**
     * 是否只过滤出targetModel设备
     * 结束扫描恢复默认值:true
     *
     */
    var singleScanMode: Boolean = true


    /**
     *  指定扫描的目标设备
     *  结束扫描恢复默认值 0
     *  初始化：详见initInterfaces()
     *  单一设备模式 = 当前model
     *  组合套装模式 = 最后被添加的model
     *
     */
    var targetModel: Int = 0




    override fun onCreate() {
        super.onCreate()
        LepuBleLog.d("BleService onCreated")
        addObserver()
        initBle()
    }

    fun reInit(){
        initBle()
    }


    /**
     * 为当前Service添加生命周期监听
     *
     */
    private fun addObserver(){
        observer?.let {
            lifecycle.addObserver(it)
            LepuBleLog.d("addObserver success")
        }
    }


    private fun initBle() {
        val bluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        LepuBleLog.d("initBle success")
    }

    /**
     * 根据model 配置interface
     * @param isClear 只有组合时，先后一一初始化interface才应该false
     */
    fun initInterfaces(m: Int, isClear: Boolean) {
        LepuBleLog.d("initInterfaces start...$m, $isClear")
        if (vailFace.get(m) != null) return

        if (isClear) vailFace.clear()

        when(m) {
            Bluetooth.MODEL_O2RING -> {
                vailFace.put(m, OxyBleInterface(m))
            }
            Bluetooth.MODEL_ER1 -> {
                vailFace.put(m, Er1BleInterface(m))

            }
        }
        targetModel = m

        LepuBleLog.d("initInterfaces ${vailFace.size()}")

    }



    private val binder = BleBinder()

    /**
     * search
     */
    fun startDiscover() {
        BluetoothController.clear()
        LepuBleLog.d("start discover")
        isDiscovery = true
        scanDevice(true)
    }

    /**
     * 连接前都应调用此方法，会重置扫描条件为默认值
     * 组合套装 将targetModel重置为默认最后添加的设备model
     *
     *
     */
    fun stopDiscover() {
        //重置
        targetModel = vailFace.keyAt(vailFace.size() -1)
        singleScanMode = true

        isDiscovery = false
        scanDevice(false)
    }



    /**
     * 重新连接开启扫描
     */
    fun reconnect(m: Int) {

        if (vailFace.isEmpty())return

        var reScan = false


        if (!vailFace.get(m).state) {
            reScan = true
        }
        if (reScan) {
            targetModel = m
            startDiscover()
        }

        LepuBleLog.d("${vailFace.get(m)::class.simpleName} => ${vailFace.get(m).state} => ReScan: $reScan")
    }

    fun reconnect() {
        if (vailFace.isEmpty())return

        var reScan = false

        if (BleServiceHelper.BleServiceHelper.hasUnConnected()) {
            reScan = true
        }
        if (reScan) {
            startDiscover()
        }
    }


    private var isDiscovery : Boolean = false
    private var bluetoothAdapter : BluetoothAdapter? = null
    private var leScanner : BluetoothLeScanner? = null

    private fun scanDevice(enable: Boolean) {
        LepuBleLog.d("scanDevice => $enable")

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
                    LepuBleLog.d("scanDevice started")
                }
            } else {
                if (bluetoothAdapter?.isEnabled!!) {
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
                LepuBleLog.d("Warning: vailFace isEmpty!!")
                return
            }

            if (!filterResult(b)) return

            if (BluetoothController.checkO2Device(b.model))
            result.scanRecord?.let {
                HashMap<String, Any>().apply {
                    this[EventMsgConst.Discovery.EventDeviceFound_Device] = b
                    this[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] = it

                    LepuBleLog.d("post paring...${b.name}")
                    LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound_ScanRecord).post(this)

                }

            }

            if (BluetoothController.addDevice(b)) {
                LepuBleLog.d("post found...${b.name}")
                LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound).post(b)
            }


        }

        override fun onBatchScanResults(results: List<ScanResult>) {
        }

        override fun onScanFailed(errorCode: Int) {
            LepuBleLog.d("scan error: $errorCode")
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                LepuBleLog.d("already start")
            }
            if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                LepuBleLog.d("scan settings not supported")
            }
            if (errorCode == 6) {
                LepuBleLog.d("too frequently")
            }
        }
    }

    /**
     * 过滤出当前类型设备
     * 组合套装时:
     * （singleScanMode = true）  model = targetModel的设备被过滤出
     * （singleScanMode = false） model 属于套装的设备被过滤出
     */
    private fun filterResult(b: Bluetooth): Boolean{
        if(vailFace.size() == 1 && b.model == targetModel){
           return true
        }else if (vailFace.size() > 1){
            //组合
            var fix = false
            for (i in 0 until vailFace.size()) {
                val key: Int = vailFace.keyAt(i)

                var f = if (singleScanMode) targetModel else key  // 如果组合套装设置但设备扫描模式
                if (b.model == f) {
                    fix = true
                    break
                }
            }
            return fix
        }
        return false
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