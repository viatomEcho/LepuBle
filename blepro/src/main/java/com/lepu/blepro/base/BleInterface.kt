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
 * description: 蓝牙指令、操作基类
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
     *  断开连接后是否开启扫描重连
     *  默认false
     *
     */
    private var isAutoReconnect: Boolean = false

    lateinit var manager: BaseBleManager
    lateinit var device: BluetoothDevice

    private var pool: ByteArray? = null

    /**
     * 是否在获取设备信息后立即执行实时任务
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




    inner class RtTask : Runnable {
        override fun run() {
            count++
            if (state) {
                rtHandler.postDelayed(rTask, delayMillis)
                getRtData()
            }
        }
    }
    abstract fun getRtData()



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


    fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        LepuBleLog.d(tag, "try connect: ${device.name}")
        this.device = device
        initManager(context, device)


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
        this.onDeviceDisconnected(device, ConnectionObserver.REASON_SUCCESS)
        this.isAutoReconnect = isAutoReconnect

    }



    @OptIn(ExperimentalUnsignedTypes::class)
    abstract fun hasResponse(bytes: ByteArray?): ByteArray?

    override fun onDeviceConnected(device: BluetoothDevice) {
        LepuBleLog.d(tag, "${device.name} connected")
        state = true
        connecting = false
        publish()

        //如果是组合套装 全部重连中
        if(BleServiceHelper.isReconnecting) {
            if (BleServiceHelper.hasUnConnected()) BleServiceHelper.reconnect() else BleServiceHelper.isReconnecting = false
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
        if (isAutoReconnect){
            //todo
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
            i.onBleStateChange(model, calBleState())
        }
    }

    /**
     * 蓝牙状态
     * {@link BleConst}
     */
    internal fun calBleState(): Int {
        LepuBleLog.d(tag, "ble state::::$state  connecting::::$connecting")
        return if (state) Ble.State.CONNECTED else if (connecting) Ble.State.CONNECTING else Ble.State.DISCONNECTED
    }

    fun runRtTask() {
        LepuBleLog.d(tag, "runRtTask start..." )
        stopRtTask()
        rtHandler.postDelayed(rTask, 500)
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).post(false)
    }

    fun stopRtTask(){
        LepuBleLog.d(tag, "stopRtTask start..." )
        rtHandler.removeCallbacks(rTask)
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).post(true)

    }


    /**
     * 获取设置信息
     */
    abstract fun getInfo()

    /**
     * 同步信息
     */
    abstract fun syncData(type: String, value: Int)

    /**
     * 获取文件列表
     */
    abstract fun getFileList()

    /**
     * 读文件
     */
    abstract fun readFile(userId: String, fileName: String)

    /**
     * 重置设备
     */
    abstract fun resetDeviceInfo()



}