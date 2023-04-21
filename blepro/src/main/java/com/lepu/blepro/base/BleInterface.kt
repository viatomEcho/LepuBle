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
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.EncryptUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * author: wujuan
 * created on: 2021/1/20 17:41
 * description: 蓝牙指令、状态基类
 * 一个model对应一个Interface实例互不干扰。App中通过BleChangeObserver、BleInterfaceLifecycle向指定model(可多个)的Interface发起订阅，观察者无需管理生命周期，自身销毁时自动注销订阅
 * 订阅成功后interface将通过BleChangeObserver#onBleStateChanged()发布蓝牙更新状态
 *
 *  1.每次发起连接将默认将isAutoReconnect赋值为true，即在断开连接回调中会重新开启扫描，重连设备。可根据需要设置
 *
 *  2.如果进入到多设备重连{BleServiceHelper #isReconnectingMulti = true}则在其中一个设备连接之后再次开启扫描
 *
 *  3.通过runRtTask(),stopRtTask()控制实时任务的开关，并将发送相应的EventMsgConst.RealTime...通知
 *
 *  4.通过自定义InterfaceEvent，发送携带model的业务通知
 *
 *  5.断开连接时可重置isAutoReconnect，根据需要决定是否断开后是否重连
 *
 */
abstract class BleInterface(val model: Int): ConnectionObserver, NotifyListener{

    private val tag = "BleInterface"

    private var sendCmdString = ""

    /**
     * 蓝牙连接状态
     */
    var state = false

    /**
     * manage ready状态
     */
    var ready = false

    /**
     * 连接中
     */
    var connecting = false

    /**
     *  断开连接后是否重新开启扫描操作重连
     *  interface实例此参数默认false
     *  当切换设备、解绑时应该置为false
     *  以后再调用connect() 可重新赋值
     *
     */
    var isAutoReconnect: Boolean = false

    lateinit var manager: LpBleManager
    // device在扫描时会动态刷新device name,可能为null
    lateinit var device: BluetoothDevice

    private var pool: ByteArray? = null

    /**
     * 获取实时波形
     */
    private val rtHandler = Handler(Looper.getMainLooper())
    private var rTask: RtTask = RtTask()

    /**
     * 获取实时的间隔
     * 默认： 500 ms
     */
    var delayMillis: Long = 500

    /**
     * 实时任务状态flag
     */
    var isRtStop: Boolean = true

    /**
     * 初始化后是否在第一次获取设备信息后立即执行实时任务
     * 默认：false
     */
    var runRtImmediately: Boolean = false

    /**
     * 记录本次连接是否来自Updater
     */
    var toConnectUpdater: Boolean = false

    /**
     * cmd响应超时
     */
    var cmdTimeout: Job? = null

    var curCmd = -1
    // 加密模式通讯
    var isEncryptMode = false
    val lepuEncryptKey = EncryptUtil.getSecretKey()
    var aesEncryptKey = ByteArray(0)

    inner class RtTask : Runnable {
        override fun run() {
            LepuBleLog.d(tag, "rtTask running...")
            if (state) {
                rtHandler.postDelayed(rTask, delayMillis)
                if (!isRtStop) getRtData() else LepuBleLog.d(tag, "isRtStop = $isRtStop")
            }else {
                LepuBleLog.d(tag, "ble state = false !!!!")
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
    var offset: Int = 0

    abstract fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean = false)

    fun isManagerInitialized(): Boolean = this::manager.isInitialized

    /**
     * 订阅者集合
     * 用于监听蓝牙状态的改变
     */
//    private var stateSubscriber: ArrayList<BleChangeObserver> = ArrayList()
    private var stateSubscriber: HashSet<BleChangeObserver> = HashSet()

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
     * 发起连接
     * @param context Context
     * @param device BluetoothDevice
     * @param isAutoReconnect Boolean 默认参数值为true，目的：当设备自然断开后会重新开启扫描并尝试连接。
     */
    fun connect(context: Context, @NonNull device: BluetoothDevice, isAutoReconnect: Boolean = true, toConnectUpdater: Boolean = false) {
        LepuBleLog.d(tag, "into connect ")
        if (connecting || state) {
            LepuBleLog.d(tag, "connect connecting:$connecting, state:$state")
            return
        }
        if (!isRtStop){
            stopRtTask()
        }

        device.name?.let {
            LepuBleLog.d(tag, "try connect: $it，isAutoReconnect = $isAutoReconnect, toConnectUpdater = $toConnectUpdater")
        }
        this.device = device

        this.isAutoReconnect = isAutoReconnect
        this.toConnectUpdater = toConnectUpdater
        initManager(context, device, toConnectUpdater)

    }


    /**
     * 断开连接
     * @param isAutoConnect Boolean APP主动断开连接后是否再发起扫描重连
     */
    fun disconnect(isAutoConnect: Boolean) {
        LepuBleLog.d(tag, "into disconnect ")

        if (!this::manager.isInitialized) {
            LepuBleLog.d(tag, "manager unInitialized")
            return
        }
        this.isAutoReconnect = isAutoConnect
        LepuBleLog.d(tag,"try disconnect..., isAutoReconnect = $isAutoConnect" )
        manager.disconnect()/*.enqueue()*/ // 此方式华为手机只走了disconnecting，没有走disconnected
        manager.close()
        if (!this::device.isInitialized){
            LepuBleLog.d(tag, "device unInitialized")
            return
        }
        this.onDeviceDisconnected(device, ConnectionObserver.REASON_SUCCESS)

    }



    @OptIn(ExperimentalUnsignedTypes::class)
    abstract fun hasResponse(bytes: ByteArray?): ByteArray?

    override fun onDeviceConnected(device: BluetoothDevice) {
        LepuBleLog.d(tag, "into onDeviceConnected ")
        if (BleServiceHelper.isScanning()) BleServiceHelper.stopScan()
        state = true
        ready = false
        connecting = false

        // 广播名有可能为null
        device.name?.let {
            BleServiceHelper.removeReconnectName(it)
            LepuBleLog.d(tag, "onDeviceConnected $it connected, ready: $ready")
        }

        device.address?.let {
            BleServiceHelper.removeReconnectAddress(it)
        }

        // 多设备重连，因SDK扫描到需重连设备，去连接时并不会关闭扫描，
        // 所以在重连此设备成功后，检查是否还有设备需要重连，再关闭扫描
        // 暂不使用
        /*if (BleServiceHelper.hasUnConnected()) {
            if (LpWorkManager.isReconnectByAddress) {
                if (LpWorkManager.reconnectDeviceAddress.size == 0) {
                    BleServiceHelper.stopScan()
                    LepuBleLog.d(tag, "onDeviceConnected stopScan")
                } else {
                    LepuBleLog.d(tag, "onDeviceConnected reconnectDeviceAddress : ${LpWorkManager.reconnectDeviceAddress}")
                }
            } else {
                if (LpWorkManager.reconnectDeviceName.size == 0) {
                    BleServiceHelper.stopScan()
                    LepuBleLog.d(tag, "onDeviceConnected stopScan")
                } else {
                    LepuBleLog.d(tag, "onDeviceConnected reconnectDeviceName : ${LpWorkManager.reconnectDeviceName}")
                }
            }
        } else {
            BleServiceHelper.stopScan()
        }*/
    }


    override fun onDeviceConnecting(device: BluetoothDevice) {
        LepuBleLog.d(tag, "into onDeviceConnecting ")
        state = false
        ready = false
        connecting = true
        publish()

        device.name?.let {
            LepuBleLog.d(tag, "$it Connecting")
        }

    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        if (!this::manager.isInitialized) {
            LepuBleLog.d(tag, "manager unInitialized")
        } else {
            manager.close()
            LepuBleLog.d(tag, "onDeviceDisconnected manager.close()")
        }
        state = false
        ready = false
        connecting = false
        isEncryptMode = false
        aesEncryptKey = ByteArray(0)
        stopRtTask()
//        LpWorkManager.vailManager.remove(model)
        publish()
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason).post(reason)
        //断开后
        LepuBleLog.d(tag, "onDeviceDisconnected==reason:${reason}===isAutoReconnect:$isAutoReconnect ${device.name} ${device.address}")
        if (reason != ConnectionObserver.REASON_NOT_SUPPORTED) {
            if (BleServiceHelper.canReconnectByName(model)) {
                device.name?.let {
                    if (isAutoReconnect) {
                        //重开扫描, 扫描该interface的设备
                        LepuBleLog.d(tag, "onDeviceDisconnected....to do reconnectByName $it")
                        BleServiceHelper.reconnect(null, it)
                    } else {
                        BleServiceHelper.removeReconnectName(it)
                    }
                }
            } else {
                device.address?.let {
                    if (isAutoReconnect) {
                        //重开扫描, 扫描该interface的设备
                        LepuBleLog.d(tag, "onDeviceDisconnected....to do reconnectByAddress $it")
                        BleServiceHelper.reconnectByAddress(null, it)
                    } else {
                        BleServiceHelper.removeReconnectAddress(it)
                    }
                }
            }
        }
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        LepuBleLog.d(tag, "into onDeviceDisconnecting ")
        state = false
        ready = false
        connecting = false
        isEncryptMode = false
        aesEncryptKey = ByteArray(0)
        publish()

        device.name?.let {
            LepuBleLog.d(tag, "$it onDeviceDisconnecting")
        }

    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {

        state = false
        ready = false
        connecting = false
        isEncryptMode = false
        aesEncryptKey = ByteArray(0)
        publish()
        LepuBleLog.d(tag, "onDeviceFailedToConnect==reason:${reason}===isAutoReconnect:$isAutoReconnect")
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason).post(reason)
        if (reason != ConnectionObserver.REASON_NOT_SUPPORTED) {
            if (BleServiceHelper.canReconnectByName(model)) {
                device.name?.let {
                    if (isAutoReconnect) {
                        //重开扫描, 扫描该interface的设备
                        LepuBleLog.d(tag, "onDeviceDisconnected....to do reconnectByName $it")
                        BleServiceHelper.reconnect(null, it)
                    } else {
                        BleServiceHelper.removeReconnectName(it)
                    }
                }
            } else {
                device.address?.let {
                    if (isAutoReconnect) {
                        //重开扫描, 扫描该interface的设备
                        LepuBleLog.d(tag, "onDeviceDisconnected....to do reconnectByAddress $it")
                        BleServiceHelper.reconnectByAddress(null, it)
                    } else {
                        BleServiceHelper.removeReconnectAddress(it)
                    }
                }
            }
        }

        device.name?.let {
            LepuBleLog.d(tag, "$it onDeviceFailedToConnect")
        }

    }



    override fun onDeviceReady(device: BluetoothDevice) {
        LepuBleLog.d(tag, "into onDeviceReady ")
        device.name?.let {
            LepuBleLog.d(tag, "$it onDeviceReady, state: $state")
        }
        state = true
        ready = true
        connecting = false
        clearCmdTimeout()
        publish()

        if (model == Bluetooth.MODEL_PC80B
            || model == Bluetooth.MODEL_PC80B_BLE
            || model == Bluetooth.MODEL_PC80B_BLE2
            || model == Bluetooth.MODEL_PC60FW
            || model == Bluetooth.MODEL_PF_10
            || model == Bluetooth.MODEL_PF_10AW
            || model == Bluetooth.MODEL_PF_10AW1
            || model == Bluetooth.MODEL_PF_10BW
            || model == Bluetooth.MODEL_PF_10BW1
            || model == Bluetooth.MODEL_PF_20
            || model == Bluetooth.MODEL_PF_20AW
            || model == Bluetooth.MODEL_PF_20B
            || model == Bluetooth.MODEL_POD_1W
            || model == Bluetooth.MODEL_S5W
            || model == Bluetooth.MODEL_S6W
            || model == Bluetooth.MODEL_S6W1
            || model == Bluetooth.MODEL_S7W
            || model == Bluetooth.MODEL_S7BW
            || model == Bluetooth.MODEL_PC_60NW_1
            || model == Bluetooth.MODEL_PC_60NW
            || model == Bluetooth.MODEL_PC_60NW_NO_SN
            || model == Bluetooth.MODEL_PC60NW_BLE
            || model == Bluetooth.MODEL_PC60NW_WPS
            || model == Bluetooth.MODEL_POD2B
            || model == Bluetooth.MODEL_PC_60B
            || model == Bluetooth.MODEL_OXYSMART
            || model == Bluetooth.MODEL_TV221U
            || model == Bluetooth.MODEL_PC100
            || model == Bluetooth.MODEL_PC66B
            || model == Bluetooth.MODEL_PC_68B
            || model == Bluetooth.MODEL_VETCORDER
            || model == Bluetooth.MODEL_CHECK_ADV
            || model == Bluetooth.MODEL_FHR
            || model == Bluetooth.MODEL_FETAL
            || model == Bluetooth.MODEL_VTM_AD5
            || model == Bluetooth.MODEL_VCOMIN
            || model == Bluetooth.MODEL_LEM
            || model == Bluetooth.MODEL_W12C
            || model == Bluetooth.MODEL_LEW
            || model == Bluetooth.MODEL_LPM311
            || model == Bluetooth.MODEL_POCTOR_M3102
            || model == Bluetooth.MODEL_BIOLAND_BGM
            || model == Bluetooth.MODEL_PC300
            || model == Bluetooth.MODEL_PC300_BLE
            || model == Bluetooth.MODEL_GM_300SNT
            || model == Bluetooth.MODEL_PC200_BLE
            || model == Bluetooth.MODEL_R20
            || model == Bluetooth.MODEL_R21
            || model == Bluetooth.MODEL_R11
            || model == Bluetooth.MODEL_R10
            || model == Bluetooth.MODEL_LERES
            || model == Bluetooth.MODEL_VTM01) { // 部分设备没有同步时间命令，发送此消息通知获取设备信息，进行绑定操作
            LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady).post(model)
        } else {
            if (toConnectUpdater) {
                LiveEventBus.get<BluetoothDevice>(EventMsgConst.Updater.EventBleConnected).post(device)
            } else {
                syncTime()
            }
        }
    }

    open fun sendCmd(bs: ByteArray): Boolean {
        if (!state && !ready) {
            LepuBleLog.d(tag, "send cmd fail， state = false")
            return false
        }
        manager.sendCmd(bs)

        sendCmdString = bytesToHex(bs)

        return true
    }

    fun getSendCmd(): String {
        return sendCmdString
    }

    fun setBleMtu(mtu: Int) {
        manager.setBleMtu(mtu)
    }

    fun getBleMtu(): Int {
        return manager.getBleMtu()
    }

    fun clearCmdTimeout() {
        curCmd = -1
        cmdTimeout?.cancel()
        cmdTimeout = null
        LepuBleLog.d("clearCmdTimeout")
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
            LepuBleLog.d(tag, "calBleState() : ${calBleState()}")
        }
    }

    /**
     * 蓝牙状态
     * {@link BleConst}
     */
    internal fun calBleState(): Int {
        LepuBleLog.d(tag, "calBleState ---model: $model, state::::$state, ready::::$ready, connecting::::$connecting")
        return if (state && ready) Ble.State.CONNECTED else if (connecting) Ble.State.CONNECTING else Ble.State.DISCONNECTED
    }

    fun runRtTask() {
        LepuBleLog.d(tag, "startRtTask")
        rtHandler.removeCallbacks(rTask)
        isRtStop = false
        rtHandler.postDelayed(rTask, 500)
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).post(model)
    }

    fun stopRtTask(sendCmd:() -> Unit = {}){
        LepuBleLog.d(tag, "stopRtTask start...")
        isRtStop = true
        rtHandler.removeCallbacks(rTask)
        GlobalScope.launch {
            delay(500)
            sendCmd.invoke()
            LepuBleLog.d(tag, "stopRtTask invoke start...")
            LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).post(model)
        }
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
        this.offset = offset //初始赋值 本地文件长度
        this.isCancelRF = false
        this.isPausedRF = false
        dealReadFile(userId, fileName)
    }
    abstract fun dealReadFile(userId: String, fileName: String)

    /**
     *  设备复位
     */
    abstract fun reset()

    /**
     * 恢复出厂设置
     */
    abstract fun factoryReset()

    /**
     * 恢复生产出厂状态
     */
    abstract fun factoryResetAll()

    /**
     * 继续 读取文件
     */
    fun continueRf(userId: String, fileName: String, offset: Int){
        this.offset = offset
        dealContinueRF(userId, fileName)
    }

    abstract fun dealContinueRF(userId: String, fileName: String)

}