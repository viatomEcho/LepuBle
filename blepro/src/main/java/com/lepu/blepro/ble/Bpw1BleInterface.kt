package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */

class Bpw1BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bpw1BleInterface"

    private lateinit var context: Context
    private var timingSwitch: Boolean = false
    private lateinit var measureTime: Array<String?>

    var fileList: Bpw1BleResponse.Bpw1FileList? = null

    private val delayTime = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private var alreadySendDelayCmd: Boolean = true // 延迟指令发送标志，false拦截操作
    private var measureTimeIndex = 0 // 设置定时测量时间完成标志

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Bpw1BleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
        sendCmd(Bpw1BleCmd.factoryReset())
    }

    override fun factoryResetAll() {
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Bpw1BleResponse.Bpw1Response) {
        LepuBleLog.d(tag, "received cmd : " + bytesToHex(response.bytes))

        when(response.len) {
            Bpw1BleCmd.UNIVERSAL_RESPONSE_LEN -> {
                when(response.content[0].toInt()) {
                    Bpw1BleCmd.UNIVERSAL_RESPONSE -> {
                        when(response.content[1].toInt()) {
                            0 -> {
                                // 应答
                                when(Bpw1BleCmd.mCurrentCmd) {
                                    Bpw1BleCmd.SET_TIME -> {
                                        LepuBleLog.d(tag, "model:$model,SET_TIME => success")

                                        alreadySendDelayCmd = false
                                        handler.postDelayed(
                                            {
                                                alreadySendDelayCmd = true
                                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetTime).post(InterfaceEvent(model, true))
                                            }, delayTime)
                                        LepuBleLog.d(tag, "SET_TIME => LiveEventBus  EventBpw1SetTime")
                                    }
                                    Bpw1BleCmd.SET_MEASURE_TIME -> {
                                        LepuBleLog.d(
                                            tag,
                                            "model:$model,SET_MEASURE_TIME => success"
                                        )
                                        measureTimeIndex++
                                        if (measureTimeIndex == measureTime.size) {
                                            measureTimeIndex = 0
                                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetMeasureTime)
                                                .post(
                                                    InterfaceEvent(model, true)
                                                )
                                            LepuBleLog.d(
                                                tag,
                                                "SET_MEASURE_TIME => LiveEventBus  EventBpw1SetMeasureTime"
                                            )
                                        }
                                    }
                                    Bpw1BleCmd.SET_TIMING_SWITCH -> {
                                        LepuBleLog.d(tag, "model:$model,SET_TIMING_SWITCH => success")
                                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetTimingSwitch).post(
                                            InterfaceEvent(model, true)
                                        )
                                        LepuBleLog.d(tag, "SET_TIMING_SWITCH => LiveEventBus  EventBpw1SetTimingSwitch")
                                    }
                                }
                            }
                            1 -> {
                                alreadySendDelayCmd = false
                                // 设备返回主界面后执行未响应的消息
                                LepuBleLog.d(tag, "model:$model,Bpw1BleCmd.mCurrentCmd => " + Bpw1BleCmd.mCurrentCmd)

                                handler.postDelayed(
                                    {
                                        alreadySendDelayCmd = true
                                        when(Bpw1BleCmd.mCurrentCmd) {
                                            Bpw1BleCmd.GET_FILE_LIST -> getFileList()
                                            Bpw1BleCmd.CLEAR_FILE_LIST -> clearFileList()
                                            Bpw1BleCmd.GET_MEASURE_TIME -> getMeasureTime()
                                            Bpw1BleCmd.GET_DEVICE_INFO -> getInfo()
                                            Bpw1BleCmd.FACTORY_RESET -> factoryReset()
                                            Bpw1BleCmd.SET_TIME -> syncTime()
                                            Bpw1BleCmd.SET_MEASURE_TIME -> setMeasureTime(measureTime)
                                            Bpw1BleCmd.SET_TIMING_SWITCH -> setTimingSwitch(timingSwitch)
                                        }
                                    }, delayTime)
                            }
                        }
                    }

                    Bpw1BleCmd.MEASURE_RESPONSE -> {
                        LepuBleLog.d(tag, "model:$model,MEASURE_RESPONSE => success")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureState).post(
                            InterfaceEvent(model, response.content[1].toInt())
                        )
                        LepuBleLog.d(tag, "MEASURE_RESPONSE => LiveEventBus  EventBpw1MeasureState")
                    }
                    Bpw1BleCmd.RT_DATA -> {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => success")
                        var data = Bpw1BleResponse.RtData(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1RtData).post(
                            InterfaceEvent(model, data)
                        )
                        LepuBleLog.d(tag, "RT_DATA => LiveEventBus  EventBpw1RtData")
                    }
                    Bpw1BleCmd.ERROR_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,ERROR_RESULT => success")
                        var data = Bpw1BleResponse.ErrorResult(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1ErrorResult).post(
                            InterfaceEvent(model, data)
                        )
                        LepuBleLog.d(tag, "ERROR_RESULT => LiveEventBus  EventBpw1ErrorResult")
                    }
                    Bpw1BleCmd.HISTORY_FILE_NUM -> {
                        LepuBleLog.d(tag, "model:$model,HISTORY_FILE_NUM => success")
                        fileList = Bpw1BleResponse.Bpw1FileList(response.content[1].toInt())
                    }
                }
            }
            Bpw1BleCmd.BP_DATA_LEN -> {
                var data = Bpw1BleResponse.BpData(response.content)
                when(Bpw1BleCmd.mCurrentCmd) {
                    Bpw1BleCmd.GET_FILE_LIST -> {
                        fileList?.let {
                            if (it.index < it.listSize) {
                                it.addFile(data)
                                LepuBleLog.d(tag, "GET_FILE_LIST => success  addFile")
                            }
                            if (it.index == it.listSize) {
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetFileListComplete).post(
                                    InterfaceEvent(model, it)
                                )
                                LepuBleLog.d(tag, "GET_FILE_LIST => LiveEventBus  EventBpw1GetFileListComplete")
                            }
                        }
                    }
                    else -> {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureResult).post(
                            InterfaceEvent(model, data)
                        )
                        LepuBleLog.d(tag, "BP_DATA_LEN => LiveEventBus  EventBpw1MeasureResult")
                    }
                }
            }
            Bpw1BleCmd.MEASURE_TIME_OR_DEVICE_INFO_LEN -> {
                when(Bpw1BleCmd.mCurrentCmd) {
                    Bpw1BleCmd.GET_DEVICE_INFO -> {
                        var data = Bpw1BleResponse.DeviceInfo(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1DeviceInfo).post(
                            InterfaceEvent(model, data)
                        )
                        LepuBleLog.d(tag, "GET_DEVICE_INFO => LiveEventBus  EventBpw1DeviceInfo")
                    }
                    Bpw1BleCmd.GET_MEASURE_TIME -> {
                        var data = Bpw1BleResponse.MeasureTime(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetMeasureTime).post(
                            InterfaceEvent(model, data)
                        )
                        LepuBleLog.d(tag, "GET_MEASURE_TIME => LiveEventBus  EventBpw1GetMeasureTime")
                    }
                }
            }
        }

    }

    var tempBytes = ByteArray(14)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null) return bytes

        when(bytes.size) {
            10 -> {
                when(bytes[2].toInt()) {
                    10 -> {
                        for (i in bytes.indices)
                            tempBytes[i] = bytes[i]
                    }
                    6 -> {
                        val bleResponse = Bpw1BleResponse.Bpw1Response(bytes)
                        onResponseReceived(bleResponse)
                    }
                }
            }
            4 -> {
                for (i in bytes.indices)
                    tempBytes[i+10] = bytes[i]
                val bleResponse = Bpw1BleResponse.Bpw1Response(tempBytes)
                onResponseReceived(bleResponse)
            }
            else -> {
                val bleResponse = Bpw1BleResponse.Bpw1Response(bytes)
                onResponseReceived(bleResponse)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.getDeviceInfo())
    }

    override fun syncTime() {
        alreadySendDelayCmd = false
        handler.postDelayed(
            {
                alreadySendDelayCmd = true
                sendCmd(Bpw1BleCmd.setTime())
            }, delayTime)
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
    }

    /**
     * get file list
     */
    override fun getFileList() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.getFileList())
    }

    fun clearFileList() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.clearFileList())
    }
    fun startBp() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.startBp())
    }
    fun stopBp() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.stopBp())
    }
    fun getMeasureTime() {
        if (alreadySendDelayCmd)
            sendCmd(Bpw1BleCmd.getMeasureTime())
    }
    fun setMeasureTime(measureTime: Array<String?>) {
        if (!alreadySendDelayCmd) return
        this.measureTime = measureTime
        var index = 0
        for (time in measureTime) {
            var data = time?.split(",")
            if (data!!.size < 7) continue
            handler.postDelayed(
                {
                    sendCmd(Bpw1BleCmd.setMeasureTime(data[0].toInt(), data[1].toInt(), data[2].toInt(), data[3].toInt(), data[4].toInt(), data[5].toInt(), data[6].toInt()))
                }, delayTime*index)
            index++
        }
    }
    fun setTimingSwitch(timingSwitch: Boolean) {
        if (!alreadySendDelayCmd) return
        this.timingSwitch = timingSwitch
        sendCmd(Bpw1BleCmd.setTimingSwitch(timingSwitch))
    }

}