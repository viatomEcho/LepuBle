package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 *
 * 蓝牙操作
 */

class LemBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LemBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = LemBleManager(context)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LemBleResponse.BleResponse) {
        LepuBleLog.d(tag, "received : ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            LemBleCmd.DEVICE_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_SWITCH => success ${bytesToHex(response.content)}")
            }
            LemBleCmd.DEVICE_BATTERY -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_BATTERY => success ${bytesToHex(response.content)}")
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MSG_DEVICE_STATE -> {
                LepuBleLog.d(tag, "model:$model,MSG_DEVICE_STATE => success ${bytesToHex(response.content)}")
                val data = LemBleResponse.DeviceInfo(response.content)
                LepuBleLog.d(tag, "model:$model,MSG_DEVICE_STATE => LemBleResponse.DeviceInfo $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo).post(InterfaceEvent(model, data))
            }
            LemBleCmd.HEAT_MODE -> {
                LepuBleLog.d(tag, "model:$model,HEAT_MODE => success ${bytesToHex(response.content)}")
                val data = byte2UInt(response.content[0])
                if (data == 1) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode).post(InterfaceEvent(model, false))
                }
            }
            LemBleCmd.MASSAGE_MODE -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_MODE => success ${bytesToHex(response.content)}")
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MASSAGE_TIME -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_TIME => success ${bytesToHex(response.content)}")
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MASSAGE_LEVEL -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_LEVEL => success ${bytesToHex(response.content)}")
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel).post(InterfaceEvent(model, data))
            }

        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 9) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-8) {
            if (bytes[i] != 0x55.toByte() || bytes[i+1] != 0xAA.toByte()) {
                continue@loop
            }

            // need content length
            val len = byte2UInt(bytes[i+6])

            if (i+9+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+9+len)
            onResponseReceived(LemBleResponse.BleResponse(temp))

            val tempBytes: ByteArray? = if (i+9+len == bytes.size) null else bytes.copyOfRange(i+9+len, bytes.size)

            return hasResponse(tempBytes)
        }

        return bytesLeft
    }

    fun getBattery() {
        sendCmd(LemBleCmd.getBattery())
    }
    fun deviceSwitch(on: Boolean) {
        sendCmd(LemBleCmd.deviceSwitch(on))
    }
    fun heatMode(on: Boolean) {
        sendCmd(LemBleCmd.heatMode(on))
    }
    fun massageMode(mode: Int) {
        sendCmd(LemBleCmd.massageMode(mode))
    }
    /**
     * 1-15挡
     */
    fun massageLevel(level: Int) {
        sendCmd(LemBleCmd.massageLevel(level))
    }
    fun massageTime(time: Int) {
        sendCmd(LemBleCmd.massageTime(time))
    }

    override fun syncTime() {
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(LemBleCmd.getDeviceState())
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
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
    }

}