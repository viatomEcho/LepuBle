package com.lepu.blepro.ble

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import com.lepu.blepro.constants.BleConst
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.observer.O2.O2BleObserver
import com.lepu.blepro.utils.LogUtils
import com.lepu.blepro.vals.hasOxy
import com.lepu.blepro.vals.oxyName

/**
 * 暴露给外部使用的单例服务
 */
class BleServiceHelper private constructor(){

    companion object{
        val BleServiceHelper: BleServiceHelper by lazy {
            BleServiceHelper()
        }
    }


    lateinit var bleService: BleService
    private val bleConn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            LogUtils.d("BleServiceHelper onServiceConnected")
            if (p1 is BleService.BleBinder){
                BleServiceHelper.bleService = p1.getService()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            LogUtils.d("BleServiceHelper onServiceDisconnected")
        }
    }


    /**
     * 在Application onCreate中初始化本单列
     */
    fun initService(application: Application, observer: BleServiceObserver?){

        LogUtils.d("initService")
        BleService.observer = observer
        BleService.startService(application)

        Intent(application, BleService::class.java).also {
            intent -> application.bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
        // 以下初始化 应该再封装


    }

    /**
     * 初始化绑定信息，应该从本地读取缓存
     */
    fun initRunVal(deviceName: String){
        oxyName = if (TextUtils.isEmpty(deviceName)) null else deviceName
        hasOxy = !TextUtils.isEmpty(deviceName)
    }

    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(){
        if (this::bleService.isInitialized)
            BleServiceHelper.bleService.reInit()
    }

    /**
     * 开始扫描
     */
    fun startDiscover(){
        if (this::bleService.isInitialized) bleService.startDiscover()
    }

    /**
     * 停止扫描
     */
    fun stopDiscover(){
        if (this::bleService.isInitialized) bleService.stopDiscover()
    }

    //-------------------------------------O2 -----------------------------------

    /**
     * 注册蓝牙状态改变的监听
     */
    fun subscribeO2Ble(observer: O2BleObserver){
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.onSubscribe(observer)
        }
    }

    /**
     * 注销蓝牙状态改变的监听
     */
    fun detachO2Ble(observer: O2BleObserver){
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.detach(observer)
        }

    }

    /**
     * 连接前必须停止扫描
     */
    fun connectO2(context: Context, bluetoothDevice: BluetoothDevice){
        if (this::bleService.isInitialized){
            stopDiscover()
            bleService.oxyInterface.connect(context, bluetoothDevice)
        }

    }
    /**
     *  主动发起重连
     *  场景：绑定成功后，从其他设备切换回来
     */
    fun reconnectO2(){
        if (this::bleService.isInitialized){
            LogUtils.d("发起主动reconnectO2")
            bleService.checkNeedAutoScan()
        }
    }


    /**
     * 主动断开连接：执行停止扫描、断开、 移除实时任务
     * 场景：切换到其他设备
     */
    fun disconnectO2(autoReconnect: Boolean){
        if (this::bleService.isInitialized) {
            stopDiscover()
            bleService.oxyInterface.disconnect(autoReconnect)
            bleService.oxyInterface.stopRtTask()
        }
    }


    /**
     * 主动获取当前蓝牙连接状态
     */
    fun getO2ConnectState(): Int = if (this::bleService.isInitialized) bleService.oxyInterface.calBleState() else BleConst.DeviceState.UNKNOWN


    /**
     * 获取主机信息
     */
    fun getO2Info(){
        if (this::bleService.isInitialized)
            bleService.oxyInterface.getInfo()
    }

    /**
     * 读取主机文件
     */
    fun readO2File(userId: String, fileName: String){
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.readFile(userId, fileName)
        }
    }

    /**
     * 重置主机
     */
    fun reset() {
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.resetDeviceInfo()
        }
    }

    /**
     * 同步信息
     */
    fun syncData(type:String,value:Int) {
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.syncData(type, value)
        }
    }

    /**
     * 设置是否需要发送实时指令，实时任务还在
     * 场景：同步设备信息的时候，设置为false
     */
    fun setNeedRtCmd(isNeed:Boolean) {
        if (this::bleService.isInitialized) {
            bleService.oxyInterface.setNeedSendCmd(isNeed)
        }
    }

    /**
     * 移除获取实时任务
     */
    fun stopRtTask(){
        if (this::bleService.isInitialized)
            bleService.oxyInterface.stopRtTask()
    }

    //--------------------------------O2 end---------------------------------------------------------






}