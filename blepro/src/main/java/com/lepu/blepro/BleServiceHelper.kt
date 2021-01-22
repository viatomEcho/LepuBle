package com.lepu.blepro

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.util.isEmpty
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.service.BleService
import com.lepu.blepro.ble.OxyBleInterface
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog

/**
 * 蓝牙服务
 */
class BleServiceHelper private constructor() {
    private val TAG = "BleServiceHelper"

    companion object {
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
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            LepuBleLog.d("BleServiceHelper onServiceDisconnected")
        }
    }

    /**
     * 是否打印log
     */
    fun setLog(log: Boolean): BleServiceHelper {
        LepuBleLog.setDebug(log)
        return this
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

    /**
     * 当前要设置的设备Model, 必须在
     * @see initService 之后调用
     * o2ring、er1
     *
     */
    fun setInterfaces(model: IntArray): BleServiceHelper {
        if (!this::bleService.isInitialized) return this else bleService.initInterfaces(model)
        return this
    }


    /**
     *
     * 扫描模式：只有在组合套装时候有效
     * 默认true，即为设备扫描模式
     * false时，会发送组合套装设备的所有扫描结果
     */
    fun singleScanMode(s: Boolean): BleServiceHelper {
        if (!check()) return this else bleService.singleScanMode = s
        return this
    }





    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(): BleServiceHelper {
        if (!check()) return this else BleServiceHelper.bleService.reInit()
        return this
    }



    /**
     * 注册蓝牙状态改变的监听
     *
     */
    internal fun subscribeBI(model: Int, observer: BleChangeObserver) {
        if (!this::bleService.isInitialized){
            LepuBleLog.d("bleService.isInitialized  = false")
            return
        }

        getInterface(model)?.onSubscribe(observer) ?: run {
            setInterfaces(intArrayOf(model))
            getInterface(model)?.onSubscribe(observer)
        }
    }




    /**
     * 注销蓝牙状态改变的监听
     */
    internal fun detachBI(model: Int, observer: BleChangeObserver) {
        if (check()) getInterface(model)?.detach(observer)
    }


    /**
     * 开始扫描
     */
    fun startScan() {
        LepuBleLog.d("startScan....")
        if (check()) bleService.startDiscover()
    }

    /**
     * 指定目标设备，开始扫描
     */
    fun startScan(m: Int) {
        if (!check()) return
        bleService.singleScanMode = true
        bleService.targetModel = m
        startScan()
    }


    /**
     * 停止扫描
     */
    fun stopScan() {
        if (check()) bleService.stopDiscover()
    }


    private fun getInterface(model: Int): BleInterface? {
        if (!check()) return null

        val vailFace = bleService.vailFace
        LepuBleLog.d(TAG, "getInterface => $model, ${vailFace.size()}, isNUll = ${vailFace.get(model) == null}")
        return vailFace.get(model)
    }

    /**
     * 连接
     */
    fun connect(context: Context,model: Int, b: BluetoothDevice) {
        if (!check()) return
        val vailFace = bleService.vailFace
        when (model) {
            Bluetooth.MODEL_O2RING -> {
                if (vailFace.get(Bluetooth.MODEL_O2RING) != null) {
                    val oxyBleInterface = vailFace.get(Bluetooth.MODEL_O2RING) as OxyBleInterface
                    stopScan()
                    oxyBleInterface.connect(context, b)
                }
            }
            // todo
        }

    }

    /**
     *  主动发起重连, 只能单设备扫描
     *  场景：绑定成功后，从其他设备切换回来
     */
    fun reconnect(model: Int) {
        LepuBleLog.d(TAG, "into reconnect " )
        if (!check()) return
        bleService.targetModel = 0
        bleService.singleScanMode = true
        bleService.reconnect(model)

    }


    /**
     * 全部断开连接
     * 主动断开连接：执行停止扫描、断开、 移除实时任务
     * 场景：切换到其他设备
     */
    fun disconnect(autoReconnect: Boolean) {
        LepuBleLog.d(TAG, "into disconnect" )

        if (!check()) return
        stopScan()
        val vailFace = bleService.vailFace
        for (i in 0 until vailFace.size()) {
            getInterface(vailFace.keyAt(i))?.let { it ->
                    it.disconnect(autoReconnect)
            }
        }
    }

    /**
     * 断开指定设备
     */
    fun disconnect(model: Int, autoReconnect: Boolean) {
        LepuBleLog.d(TAG, "into disconnect" )
        if (!check()) return
        val i =
        stopScan()

        getInterface(model)?.let {
            LepuBleLog.d(TAG, "$i")
            it.disconnect(autoReconnect)
        }

    }


    /**
     * 主动获取当前蓝牙连接状态
     */
    fun getConnectState(model: Int): Int = if (check()) getInterface(model)?.calBleState()!! else Ble.State.UNKNOWN


    /**
     * 获取主机信息
     */
    fun getInfo(model: Int) {
        if (!check()) return
        getInterface(model)?.getInfo()

    }


    /**
     * 读取主机文件
     */
    fun readFile(userId: String, fileName: String, model: Int) {
        if (!check()) return
        getInterface(model)?.readFile(userId, fileName)

    }

    /**
     * 重置主机
     */
    fun reset(model: Int) {
        if (!check()) return
        getInterface(model)?.resetDeviceInfo()

    }

    /**
     * 同步信息
     */
    fun syncData(model: Int, type: String, value: Int) {
        if (!check()) return
        getInterface(model)?.syncData(type, value)
    }


    /**
     * 移除获取实时任务
     */
    fun stopRtTask(model: Int) {
        if (!check()) return
        getInterface(model)?.stopRtTask()
    }

    private fun check(): Boolean{
        if (!this::bleService.isInitialized){
            LepuBleLog.d("Warning: bleService.isInitialized = false!!")
            return false
        }
        if (bleService.vailFace.isEmpty()){
            LepuBleLog.d("Warning: bleService.vailFace isEmpty!!")
            return false
        }
        return true
    }



}