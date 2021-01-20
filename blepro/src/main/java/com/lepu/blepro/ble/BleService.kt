package com.lepu.blepro.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import androidx.lifecycle.LifecycleService
import com.lepu.blepro.event.BleProEvent
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LogUtils
import com.lepu.blepro.vals.oxyName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class BleService: LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        LogUtils.d("BleService onCreated")
        addObserver()
        initBle()
        initInterfaces()
    }

    fun reInit(){
        initBle()
    }

    /**
     * 未当前Service添加生命周期监听
     *
     */
    private fun addObserver(){
        observer?.let {
            lifecycle.addObserver(it)
            LogUtils.d("addObserver success")
        }
    }


    private fun initBle() {
        val bluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        LogUtils.d("initBle success")

    }

    private fun initInterfaces() {
        oxyInterface = OxyBleInterface()
    }

    private val binder = BleBinder()

    /**
     * ble interfaces
     * manage all ble client
     */
    lateinit var oxyInterface: OxyBleInterface


    /**
     * search
     */
    fun startDiscover() {
        BluetoothController.clear()
        LogUtils.d("start discover")
        isDiscovery = true
        scanDevice(true)
        // todo  o2暂时屏蔽
//        Timer().schedule(20000) {
//            stopDiscover()
//        }
    }


    fun stopDiscover() {
        isDiscovery = false
        scanDevice(false)
    }

    /**
     * check auto scan
     * 有未连接的绑定设备则继续搜索
     */
    fun checkNeedAutoScan() {
        var reScan = false
        if (oxyName != null && !oxyInterface.state) {
            reScan = true
        }
        if (reScan) {
            startDiscover()
        }

        LogUtils.d("$oxyName => ${oxyInterface.state} => ReScan: $reScan"
        )
    }

    private var isDiscovery : Boolean = false
    private var bluetoothAdapter : BluetoothAdapter? = null
    private var leScanner : BluetoothLeScanner? = null

    private fun scanDevice(enable: Boolean) {
        LogUtils.d("scanDevice => $enable")

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
                    LogUtils.d("scanDevice started")
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
            LogUtils.d("onScanResult $b.toString()")

            //扫描到的设备将被添加到BluetoothController
            //如果添加失败，代表已经添加过，则进行配对验证
            if (BluetoothController.addDevice(b)) {
//                BleProEvent.post(EventMsgConst.EventDeviceFound, b) //O2Ring 不必通知

                  // 已绑定后重连
                if (b.name == oxyName) {
                    //连接前必须停止扫描
                    stopDiscover()
                    oxyInterface.connect(this@BleService, b.device)
                    LogUtils.d("bound oxy found: ${b.device.name}")
                }

            }else{

                result.scanRecord?.let {
                    // 如果是O2设备：O2ring
                    // 并且当前未绑定O2设备
                    if (b.model == Bluetooth.MODEL_O2RING && oxyName == null){
                        val map = HashMap<String, Any>().apply {
                            this[EventMsgConst.Oxy.EventOxyKeyDevice] = b
                            this[EventMsgConst.Oxy.EventOxyKeyScanRecord] = result.scanRecord!!
                        }
                        BleProEvent.post(EventMsgConst.Oxy.EventOxyPairO2Ring, map)
                        LogUtils.d("oxy post pair msg: ${b.device.name}, ${result.scanRecord}")
                    }
                }


            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
        }

        override fun onScanFailed(errorCode: Int) {
            LogUtils.d("scan error: $errorCode")
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                LogUtils.d("already start")
            }
            if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                LogUtils.d("scan settings not supported")
            }
            if (errorCode == 6) {
                LogUtils.d("too frequently")
            }
        }
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
            LogUtils.d("startService")
            Intent(context, BleService::class.java).also { intent -> context.startService(intent)}
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, BleService::class.java)
            context.stopService(intent)
        }

    }



}