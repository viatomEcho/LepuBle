package com.lepu.blepro.base

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.collections.ArrayList

/**
 * author: wujuan
 * created on: 2021/1/20 17:41
 * description: 蓝牙指令、状态基类
 * 一个model对应一个Interface实例互不干扰。App中通过BleChangeObserver、BleInterfaceLifecycle向指定model(可多个)的Interface发起订阅，观察者无需管理生命周期，自身销毁时自动注销订阅
 * 订阅成功后interface将通过BleChangeObserver#onBleStateChanged()发布蓝牙更新状态
 *
 *  1.每次发起连接将默认将isAutoReconnect赋值为true，即在断开连接回调中会重新开启扫描，重连设备
 *
 *  2.如果进入到多设备重连{BleServiceHelper #isReconnectingMulti = true}则在其中一个设备连接之后再次开启扫描
 *
 *  3.通过runRtTask(),stopRtTask()控制实时任务的开关，并将发送相应的EventMsgConst.RealTime...通知
 *
 *  4.通过自定义InterfaceEvent，发送携带model的业务通知
 *
 */
abstract class BleInterface(val model: Int): ConnectionObserver, NotifyListener {

    private val tag = "BleInterface"

    /**
     * 蓝牙连接状态
     */
    internal var state = false


    /**
     * 连接中
     */
    private var connecting = false


    /**
     *  断开连接后是否重新开启扫描操作重连
     *  默认false
     *  当切换设备、解绑时应该置为false
     *  调用connect() 可重新赋值
     *
     */
    var isAutoReconnect: Boolean = false

    lateinit var manager: BaseBleManager
    lateinit var device: BluetoothDevice

    private var pool: ByteArray? = null

    /**
     * 是否在第一次获取设备信息后立即执行实时任务
     * 默认：false
     */
    var runRtImmediately: Boolean = false

    /**
     * 获取实时波形
     */
    private var count: Int = 0
    private val rtHandler = Handler(Looper.getMainLooper())
    private  var rTask: RtTask = RtTask()

    /**
     * 获取实时的间隔
     * 默认： 1000 ms
     */
    var  delayMillis: Long = 1000

    /**
     * 实时任务状态flag
     */
    var isRtStop: Boolean = true


    inner class RtTask : Runnable {
        override fun run() {
            count++
            if (state) {
                rtHandler.postDelayed(rTask, delayMillis)
                if (!isRtStop) getRtData()
            }
        }
    }

    /**
     * 是否暂停读文件
     */
    var isPausedRF: Boolean = false

    /**
     * 是否取消读文件
     */
    var isCancelRF: Boolean = false

    /**
     * 开始读取时的偏移量，用于断点续传
     * 继续读取的时候 offset = curFile.index + offset(初始赋值)
     */
    var offset: Int = 0;




    /**
     * 订阅者集合
     * 用于监听蓝牙状态的改变
     */
    private var stateSubscriber: ArrayList<BleChangeObserver> = ArrayList()

    /**
     * 添加订阅者
     */
    internal fun onSubscribe(observer: BleChangeObserver) {
        stateSubscriber.add(observer)
        LepuBleLog.d(tag, "model=>${model}, 总数${stateSubscriber.size}成功添加了一个订阅者")

    }


    /**
     * 移除订阅者, 订阅者销毁时自动移除
     */
    internal fun detach(observer: BleChangeObserver) {
        if (stateSubscriber.isNotEmpty()) stateSubscriber.remove(observer)
        LepuBleLog.d(tag, "model=>${model}, 总数${stateSubscriber.size}成功将要移除一个订阅者")
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    /**
     *
     */
    fun connect(context: Context, @NonNull device: BluetoothDevice, isAutoReconnect: Boolean = true) {
        if (connecting || state) {
            return
        }
        LepuBleLog.d(tag, "try connect: ${device.name}")
        this.device = device
        initManager(context, device)
        this.isAutoReconnect = isAutoReconnect
    }

    abstract fun initManager(context: Context, device: BluetoothDevice)



    fun disconnect(isAutoConnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect ")

        if (!this::manager.isInitialized) {
            LepuBleLog.d(tag, "manager unInitialized")
            return
        }
        manager.disconnect()
        manager.close()
        if (!this::device.isInitialized){
            LepuBleLog.d(tag, "device unInitialized")
            return
        }
        LepuBleLog.d(tag,"tay disconnect..." )
        this.isAutoReconnect = isAutoReconnect
        this.onDeviceDisconnected(device, ConnectionObserver.REASON_SUCCESS)


    }



    @OptIn(ExperimentalUnsignedTypes::class)
    abstract fun hasResponse(bytes: ByteArray?): ByteArray?

    override fun onDeviceConnected(device: BluetoothDevice) {
        LepuBleLog.d(tag, "${device.name} connected")
        state = true
        connecting = false
        publish()

        // 重连多个model时
        if(BleServiceHelper.isReconnectingMulti) {
            LepuBleLog.d("reconnectingMulti：检查是否还有未连接的设备")
            val scanModel = BleServiceHelper.bleService.scanModel
            val reconnectDeviceName = BleServiceHelper.bleService.reconnectDeviceName
            scanModel?.let {
                if ( reconnectDeviceName!= null){
                    if (BleServiceHelper.hasUnConnected(it))
                        BleServiceHelper.reconnect(it,
                            reconnectDeviceName
                        ) else BleServiceHelper.isReconnectingMulti = false
                }
            }
        }

    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        LepuBleLog.d(tag, "${device.name} Connecting")
        state = false
        connecting = true
        publish()

    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        LepuBleLog.d(tag, "${device.name} Disconnected")
        state = false
        connecting = false
        stopRtTask()
        publish()

        //断开后
        LepuBleLog.d(tag, "onDeviceDisconnected=====isAutoReconnect:$isAutoReconnect")
        if (isAutoReconnect){
            //重开扫描, 扫描该interface的设备
                LepuBleLog.d(tag, "onDeviceDisconnected....to do")
            BleServiceHelper.reconnect(model, device.name)
        }


    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        LepuBleLog.d(tag, "${device.name} Disconnecting")
        state = false
        connecting = false

        publish()

    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        LepuBleLog.d(tag, "${device.name} FailedToConnect")
        state = false

        connecting = false

        LepuBleLog.d(tag, "onDeviceFailedToConnect=====isAutoReconnect:$isAutoReconnect")

        if (isAutoReconnect){
            //重开扫描, 扫描该interface的设备
            LepuBleLog.d(tag, "onDeviceFailedToConnect....to do")
            BleServiceHelper.reconnect(model, device.name)
        }
    }



    override fun onDeviceReady(device: BluetoothDevice) {
        LepuBleLog.d(tag, "${device.name} isReady")
        connecting = false
    }




    fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        manager.sendCmd(bs)
    }

    /**
     * 发布蓝牙状态改变通知
     * 外部要监听蓝牙实现过程
     * 1.注册 @see O2BleObserverLifeImpl
     * 2.实现 @see O2BleObserver
     */
    private fun publish() {
        LepuBleLog.d(tag, "publish=>${stateSubscriber.size}")
        for (i in stateSubscriber) {
            i.onBleStateChanged(model, calBleState())
        }
    }

    /**
     * 蓝牙状态
     * {@link BleConst}
     */
    internal fun calBleState(): Int {
        LepuBleLog.d(tag, "calBleState ---model: $model, state::::$state,connecting::::$connecting")
        return if (state) Ble.State.CONNECTED else if (connecting) Ble.State.CONNECTING else Ble.State.DISCONNECTED
    }

    fun runRtTask() {
        LepuBleLog.d(tag, "runRtTask start..." )
        rtHandler.removeCallbacks(rTask)
        isRtStop = false
        rtHandler.postDelayed(rTask, 500)
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStart).post(model)
    }

    fun stopRtTask(){
        LepuBleLog.d(tag, "stopRtTask start..." )
        isRtStop = true
        rtHandler.removeCallbacks(rTask)
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).post(model)

    }


    /**
     * 获取设置信息
     */
    abstract fun getInfo()

    /**
     * 同步时间
     */
    abstract fun syncTime()

    /**
     * 更新设置
     */
    abstract fun updateSetting(type: String, value: Any)

    /**
     * 获取实时
     */
    abstract fun getRtData()
    /**
     * 获取文件列表
     */
    abstract fun getFileList()

    /**
     * 读文件
     */
    fun readFile(userId: String, fileName: String, offset: Int = 0){
        this.offset = offset// 作用于断点续传
        this.isCancelRF = false
        this.isPausedRF = false
        dealReadFile(userId, fileName)
    }
    abstract fun dealReadFile(userId: String, fileName: String)

    /**
     * 重置设备
     */
    abstract fun resetDeviceInfo()

    /**
     * 继续 读取文件
     */
    fun continueRf(userId: String, fileName: String, offset: Int){
        this.offset = offset
        dealContinueRF(userId, fileName)

    }
    abstract fun dealContinueRF(userId: String, fileName: String)





}