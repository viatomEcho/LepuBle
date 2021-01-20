package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import com.lepu.blepro.ble.cmd.Er1BleCRC
import com.lepu.blepro.ble.cmd.OxyBleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.constants.BleConst
import com.lepu.blepro.event.BleProEvent
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.O2RTEvent
import com.lepu.blepro.observer.O2.O2BleObserver
import com.lepu.blepro.utils.LogUtils
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.toHex
import com.lepu.blepro.utils.toUInt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.experimental.inv


class OxyBleInterface : ConnectionObserver, OxyBleManager.onNotifyListener {

    /**
     * 订阅者集合
     * 用于监听蓝牙状态的改变
     */
    private var stateSubscriber: ArrayList<O2BleObserver> = ArrayList()

    /**
     * 蓝牙连接状态
     */
    var state = false

    /**
     * 连接中
     */
    var connectting = false


    private var curCmd: Int = 0
    private var timeout: Job? = null

    lateinit var manager: OxyBleManager
    lateinit var myDevice: BluetoothDevice

    private var pool: ByteArray? = null
    private var count: Int = 0

    private var mSyncType: String = ""
    private var mSyncDataValue:Int=0

    /**
     * 是否需要发送实时指令，不会停止实时任务
     */
    private var mIsNeedRtCmd = true

    /**
     * 响应超时，是否重发, 默认不需要重发
     */
    private var isAutoResend: Boolean = false


    /**
     * 获取实时波形
     */
    private val rtHandler = Handler()
    private  var rTask: RtTask = RtTask()
    inner class RtTask : Runnable {
        override fun run() {
            count++
            LogUtils.d("RtTask: ble => $state")
            if (state) {
                rtHandler.postDelayed(rTask, 1000)
                getRtData()
            }
        }
    }

    fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connectting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = OxyBleManager(context)
        myDevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(true)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LogUtils.d("Device Init")
                }
                .enqueue()

    }

    private fun sendCmd(cmd: Int, bs: ByteArray) {
        LogUtils.d("try send cmd: $cmd, ${bs.toHex()}")
        if (curCmd != 0) {
            // busy
            LogUtils.d("busy: $cmd =>$curCmd")
            return
        }

        curCmd = cmd
        pool = null
        if (this::manager.isInitialized)
            manager.sendCmd(bs)
        timeout = GlobalScope.launch {
            delay(3000)
            // timeout
            LogUtils.d("timeout: $curCmd,isAutoResend: $isAutoResend")
            if (!isAutoResend)
                return@launch
            when (curCmd) {

                OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                    if (mSyncType == OxyBleCmd.OXY_CMD_SYNC_TIME) {
                        curCmd = 0
                        getInfo()
                    } else {
                        curCmd = 0
                        LogUtils.d("同步失败：继续同步")
                        if (!TextUtils.isEmpty(mSyncType)){
                            syncData(mSyncType,mSyncDataValue)
                        }
                    }
                }

                OxyBleCmd.OXY_CMD_INFO -> {
                    curCmd = 0
                    getInfo()
                }
//                OxyBleCmd.OXY_CMD_RT_DATA -> {
//                    curCmd = 0
//                }
                else -> {
                    curCmd = 0
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: OxyBleResponse.OxyResponse) {
        LogUtils.d("Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == 0) {
            return
        }

        when (curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()
                if (mSyncType == OxyBleCmd.OXY_CMD_SYNC_TIME) {
                    getInfo()
                } else {
                    BleProEvent.post(EventMsgConst.Oxy.EventOxySyncDeviceInfo, mSyncType)
                }
                LogUtils.d("同步完成 type=$mSyncType")
                mSyncType = ""
                mSyncDataValue =0
            }

            OxyBleCmd.OXY_CMD_INFO -> {
                clearTimeout()
                val info = OxyBleResponse.OxyInfo(response.content)
                BleProEvent.post(EventMsgConst.Oxy.EventOxyInfo, info)
                LogUtils.d("发送 info")
                // 即可开启实时任务
                runRtTask()

            }

            OxyBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()
                val rtWave = OxyBleResponse.RtWave(response.content)
                //发送实时数据
                O2RTEvent.post(EventMsgConst.Oxy.EventOxyRtData, rtWave)

            }
            OxyBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()
                if (response.state) {
                    val fileSize = toUInt(response.content)

                    LogUtils.d("文件大小：${fileSize}  文件名：$curFileName")
                    curFileName?.let {

                        curFile = OxyBleResponse.OxyFile(curFileName!!, fileSize, userId)
                        sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    }


                } else {
                    BleProEvent.post(EventMsgConst.Oxy.EventOxyReadFileError)
                    LogUtils.d("读文件失败：${response.content.toHex()}")
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                curFile?.apply {

                    this.addContent(response.content)
                    BleProEvent.post(EventMsgConst.Oxy.EventOxyReadingFileProgress, (curFile!!.index * 1000).div(curFile!!.fileSize) )
                    LogUtils.d("读文件中：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    } else {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_END, OxyBleCmd.readFileEnd())
                    }
                }
            }
            OxyBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LogUtils.d("读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null
                BleProEvent.post(EventMsgConst.Oxy.EventOxyReadFileComplete, curFile)
                curFile = null

            }

            OxyBleCmd.OXY_CMD_RESET -> {
                clearTimeout()
                BleProEvent.post(EventMsgConst.Oxy.EventOxyResetDeviceInfo)
            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearVar() {
//        model.battery.value = 0
//        model.pr.value = 0
//        model.spo2.value = 0
//        model.pi.value = 0.0f
    }

    private fun clearTimeout() {
        curCmd = 0
        timeout?.cancel()
        timeout = null
    }

    @ExperimentalUnsignedTypes
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] != 0x55.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i + 8 + len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == Er1BleCRC.calCRC8(temp)) {
                val bleResponse = OxyBleResponse.OxyResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    fun disconnect(isAutoResend: Boolean) {
        this.isAutoResend = false
        if (this::manager.isInitialized) {
            manager.disconnect()
            manager.close()
        }
        if (this::myDevice.isInitialized)
            this.onDeviceDisconnected(myDevice, ConnectionObserver.REASON_SUCCESS)

    }

    fun syncTime(type: String, value: Int) {
        mSyncType = type
        sendCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncData(type, value))
    }

    fun syncData(type: String, value: Int) {
        mSyncType = type
        mSyncDataValue = value
        sendCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncData(type, value))
    }

    fun getInfo() {
        sendCmd(OxyBleCmd.OXY_CMD_INFO, OxyBleCmd.getInfo())
    }

    fun getRtData() {
        if (mIsNeedRtCmd) {
            sendCmd(OxyBleCmd.OXY_CMD_RT_DATA, OxyBleCmd.getRtWave())
        }
    }

    fun resetDeviceInfo() {
        sendCmd(OxyBleCmd.OXY_CMD_RESET, OxyBleCmd.resetDeviceInfo())
    }

    fun runRtTask() {
        stopRtTask()
        rtHandler.postDelayed(rTask, 200)
    }

    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null

    lateinit var userId: String

    fun readFile(userId: String, fileName: String) {

        this.curFileName = fileName
        this.userId = userId
        LogUtils.d("$userId 将要读取文件 $curFileName" )
//        20201210095928
//        AA03FC00000F003230323031323130303935393238004C
        sendCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }


    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        LogUtils.d("${device.name} connected")
        state = true
        connectting = false
        publish()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Connecting")
        state = false
        connectting = true

        publish()

    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} Disconnected")
        state = false
        curCmd = 0
        stopRtTask();
        clearVar()
        connectting = false
        publish()

    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Disconnecting")
        state = false
        connectting = false

        publish()

    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} FailedToConnect")
        state = false

        connectting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {
//        runRtTask()
        LogUtils.d("${device.name} isReady")
        curCmd = 0

        connectting = false
        Timer().schedule(500) {
            syncTime(OxyBleCmd.OXY_CMD_SYNC_TIME, 0)
        }
    }


    fun onSubscribe(observer: O2BleObserver) {
        stateSubscriber.add(observer)
        LogUtils.d("添加了一个订阅者")

    }

    fun detach(observer: O2BleObserver) {
        LogUtils.d("将要移除一个订阅者")
        if (stateSubscriber.isNotEmpty()) stateSubscriber.remove(observer)
    }

    /**
     * 发布蓝牙状态改变通知
     * 外部要监听蓝牙实现过程
     * 1.注册 @see O2BleObserverLifeImpl
     * 2.实现 @see O2BleObserver
     */
    private fun publish() {
        for (i in stateSubscriber) {
            i?.onBleStateChange(calBleState())
        }

    }


    fun calBleState(): Int {
        LogUtils.d("ble state::::$state  connecting::::$connectting")
        return if (state) BleConst.DeviceState.CONNECTED else if (connectting) BleConst.DeviceState.CONNECTING else BleConst.DeviceState.DISCONNECTED
    }

    fun setNeedSendCmd(isNeed:Boolean) {
        this.mIsNeedRtCmd =isNeed
        // 发送是否已暂停实时
        BleProEvent.post(EventMsgConst.Oxy.EventOxyRtDataStop, !isNeed)
    }

    fun stopRtTask(){
        rtHandler.removeCallbacks(rTask)
    }
}