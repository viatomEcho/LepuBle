package com.lepu.blepro

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.SparseArray
import androidx.core.util.isEmpty
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.service.BleService
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.observer.BleServiceObserver
import com.lepu.blepro.utils.LepuBleLog
import java.lang.Exception

/**
 * 蓝牙服务
 */
class BleServiceHelper private constructor() {

    var rawFolder: SparseArray<String>? = null

    /**
     * 组合套装手动重连中
     */
    var reconnectingMulti: Boolean = false

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
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            LepuBleLog.d("BleServiceHelper onServiceDisconnected")
        }
    }
    //===========================================================

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

    fun initLog(log: Boolean): BleServiceHelper{
        LepuBleLog.setDebug(log)
        return this
    }



    fun initRawFolder(folders: SparseArray<String>): BleServiceHelper{
        this.rawFolder = folders
        return this
    }


    /**
     * 当前要设置的设备Model, 必须在initService 之后调用
     * vailFace.get(model) == null 考虑两种情况
     * 1. 单设备模式下，应该初始化缺没有初始化,这种情况正常使用BleService initInterface（先清空vailFace）
     * 2. 组合套装下：
     *  后一设备注册时找不到设备对应的interface，
     *  此时 BleService initInterface()不应该清空vailFace
     * o2ring、er1
     */
    fun setInterfaces(model: Int, isClear: Boolean = true, runRtImmediately: Boolean = false) {
        if (!this::bleService.isInitialized) return
        if (getInterface(model) == null) bleService.initInterfaces(model, isClear ,runRtImmediately)
    }

    //--------------------------------------------------------



    /**
     * 重新初始化蓝牙
     * 场景：蓝牙关闭状态进入页面，开启系统蓝牙后，重新初始化
     */
    fun reInitBle(): BleServiceHelper {
        if (!check()) return this
        BleServiceHelper.bleService.reInit()
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
        getInterface(model)?.onSubscribe(observer)
    }




    /**
     * 注销蓝牙状态改变的监听
     */
    internal fun detachBI(model: Int, observer: BleChangeObserver) {
        if (check()) getInterface(model)?.detach(observer)
    }

//    /**
//     *
//     */
//    fun targetModel(model: Int){
//        if (!check())return
//        bleService.targetModel = model
//    }

//    fun setNeedPair(p: Boolean){
//        if (!check()) return
//        bleService.needPair = p
//    }

    /**
     * 开始扫描
     * 注意：当前的扫描参数, 如果前一次扫描正常结束， 此次扫描相关属性将为默认值
     */
//    fun startScan() {
//        if (!check()) return
//        bleService.startDiscover()
//    }


    /**
     * 开始扫描,组合套装可用此方法来设置扫描条件
     * @param singleScanMode 是否只过滤出targetModel设备 , false时targetModel无效
     * @param targetModel 默认上一次的model， 第一次为末尾添加到VialFace的model
     *
     */
    @JvmOverloads
    fun startScan(needPair: Boolean = false) {
        if (!check()) return
        bleService.startDiscover(true, needPair = needPair)
    }

    @JvmOverloads
    fun startScan(targetModel: Int, needPair: Boolean = false) {
        if (!check()) return
        bleService.startDiscover(true, targetModel, needPair)
    }

    @JvmOverloads
    fun startScanMulti(needPair: Boolean = false){
        if (!check()) return
        bleService.startDiscover(false, needPair = needPair)
    }





    /**
     * 停止扫描
     * 连接之前调用该方法，并会重置扫描条件为默认值
     * 组合套装时,targetModel 会被重置为末尾添加的model
     *
     */
    fun stopScan() {
        if (check()) bleService.stopDiscover()
    }

    /**
     * vailFace.get(model) == null 考虑两种情况
     * 1. 单设备模式下，应该初始化缺没有初始化,这种情况正常使用BleService initInterface（先清空vailFace）
     * 2. 组合套装下：
     *  后一设备注册时找不到设备对应的interface，
     *  此时 BleService initInterface()不应该清空vailFace
     */
    fun getInterface(model: Int): BleInterface? {
        if (!check()) return null

        val vailFace = bleService.vailFace
        LepuBleLog.d(tag, "Warning: getInterface => $model, ${vailFace.size()}, isNUll = ${vailFace.get(model) == null}")
        return vailFace.get(model)
    }
    fun getInterfaces(): SparseArray<BleInterface>? {
        if (!check()) return null
        return bleService.vailFace
    }
    /**
     * 连接之前停止扫描
     * 组合套装： 如果还有设备要连接将重启扫描
     *
     */
    fun connect(context: Context,model: Int, b: BluetoothDevice) {
        if (!check()) return
        getInterface(model)?.let {
            stopScan()
            it.connect(context, b)
        }
    }




    /**
     *  主动发起重连, 只能单设备扫描
     *  场景：绑定成功后，从其他设备切换回来
     */
    fun reconnect(model: Int) {
        LepuBleLog.d(tag, "into reconnect " )
        if (!check()) return
        bleService.reconnect(model)

    }
    fun reconnect() {
        if (!check()) return
        bleService.singleScanMode = false
        bleService.reconnect()

        reconnectingMulti = true
    }


    /**
     * 全部断开连接
     * 主动断开连接：执行停止扫描、断开、 移除实时任务
     * 场景：切换到其他设备
     */
    fun disconnect(autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect" )

        if (!check()) return
        val vailFace = bleService.vailFace
        for (i in 0 until vailFace.size()) {
            getInterface(vailFace.keyAt(i))?.let { it ->
                stopScan()
                it.disconnect(autoReconnect)
            }
        }
    }

    /**
     * 断开指定设备
     */
    fun disconnect(model: Int, autoReconnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect" )
        if (!check()) return
        getInterface(model)?.let {
            stopScan()
            it.disconnect(autoReconnect)
        }

    }


    /**
     * 主动获取当前蓝牙连接状态
     */
    fun getConnectState(model: Int): Int = if (check()) getInterface(model)?.calBleState()!! else Ble.State.UNKNOWN



    fun hasUnConnected(): Boolean{
        if (!check()) return false
        bleService.vailFace.let {
            for (i in 0 until it.size()) {
                val ble = getInterface(it.keyAt(i)) as BleInterface
                if (!ble.state) return true
            }
        }
        LepuBleLog.d(tag, "没有未连接设备")
        return false
    }


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
    fun syncData(model: Int, type: String, value: Any) {
        if (!check()) return
        getInterface(model)?.syncData(type, value)
    }

    /**
     * 设置实时认为间隔时间
     */
    fun setRTDelayTime(model: Int, delayMillis: Long){
        if (!check()) return
        getInterface(model)?.delayMillis = delayMillis
    }

    /**
     * 设置是否立即开启实时任务
     */
    fun setRunRtImmediately(model: Int, i: Boolean){
        if (!check()) return
        getInterface(model)?.runRtImmediately = i
    }


    /**
     * 开启实时任务
     */
    fun startRtTask(model: Int){
        if (!check()) return
        getInterface(model)?.runRtTask()
    }


    /**
     * 移除获取实时任务
     */
    fun stopRtTask(model: Int) {
        if (!check()) return
        getInterface(model)?.stopRtTask()
    }

    fun clearVailFace(){
        if (!check()) return
        bleService.vailFace.clear()
    }

    private fun check(): Boolean{
        if (!this::bleService.isInitialized){
            LepuBleLog.d("Error: bleService unInitialized")
            return false
        }
        if (bleService.vailFace.isEmpty()){
            LepuBleLog.d("Error: bleService.vailFace unInitialized")
            return false
        }
        return true
    }


//    fun subscribeAll(observer: BleChangeObserver){
//        if (!check()) return
//        LepuBleLog.d("getInterfaceList")
//        for (i in 0 until bleService.vailFace.size()) {
//            getInterface(bleService.vailFace.keyAt(i))?.let { it ->
//               subscribeBI(it.model, observer)
//            }
//        }
//    }


}