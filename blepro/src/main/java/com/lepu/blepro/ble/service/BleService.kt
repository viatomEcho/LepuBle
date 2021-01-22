package com.lepu.blepro.ble.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.lifecycle.LifecycleService
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.OxyBleInterface
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap


class BleService: LifecycleService() {



    /**
     * 保存可用的BleInterface集合
     */
    var vailFace: SparseArray<BleInterface> = SparseArray()


    /**
     * 套装组合时是否只扫描当前设备
     * 结束扫描恢复默认值
     */
    var singleScanMode: Boolean = true


    /**
     *  套装组合时指定扫描的目标设备
     *  结束扫描恢复默认值
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
     * ble interfaces
     * manage all ble client
     */
//    lateinit var oxyInterface: OxyBleInterface



    /**
     * 未当前Service添加生命周期监听
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
     */
    fun initInterfaces(models: IntArray) {
        LepuBleLog.d("initInterfaces start...${models.size}")
        vailFace.clear()
        for (m in models){
            LepuBleLog.d("initInterfaces model= > m")
            when(m) {
                Bluetooth.MODEL_O2RING -> {
                    vailFace.put(m, OxyBleInterface(m))
                    LepuBleLog.d("initInterfaces ${vailFace.size()}")
                }
            }
        }

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


    fun stopDiscover() {
        targetModel = 0
        singleScanMode = true
        isDiscovery = false
        scanDevice(false)
    }



    /**
     * check auto scan
     * 有未连接的绑定设备则继续搜索
     */
    fun reconnect(m: Int) {


        if (vailFace.isEmpty())return

        var reScan = false


        if (!vailFace.get(m).state) {
            reScan = true
        }
        if (reScan) {

            startDiscover()
        }

        LepuBleLog.d("${vailFace.get(m)::class.simpleName} => ${vailFace.get(m).state} => ReScan: $reScan")
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

            result.scanRecord?.let {
                HashMap<String, Any>().apply {
                    this[EventMsgConst.Discovery.EventDeviceFound_Device] = b
                    this[EventMsgConst.Discovery.EventDeviceFound_ScanResult] = it
                    LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound_ScanResult).post(this)
                    LepuBleLog.d("post paring...${b.name}")
                }

            }

            if (BluetoothController.addDevice(b)) {
                LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound).post(b)
                LepuBleLog.d("post found...${b.name}")
                // 如果已绑定 则重连
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

    private fun filterResult(b: Bluetooth): Boolean{
        if(vailFace.size() == 1){
            if (b.model == vailFace.keyAt(0))return true
        }else {
            //组合
            var fix = false
            for (i in 0 until vailFace.size()) {
                val key: Int = vailFace.keyAt(i)

                LepuBleLog.d("${b.model} => $key")

                var f = if (singleScanMode && targetModel != 0) targetModel else key  // 如果组合套装设置但设备扫描模式
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