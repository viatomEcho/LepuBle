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
 * lem1护颈仪：
 * send:
 * 1.获取电量
 * 2.恒温加热模式开关
 * 3.设置按摩模式
 * 4.设置按摩力度等级
 * 5.设置按摩时间
 * 6.获取设备信息
 */
class LemBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LemBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = LemBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LemBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            LemBleCmd.DEVICE_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_SWITCH => success ${bytesToHex(response.content)}")
            }
            LemBleCmd.DEVICE_BATTERY -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_BATTERY => success ${bytesToHex(response.content)}")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MSG_DEVICE_STATE -> {
                LepuBleLog.d(tag, "model:$model,MSG_DEVICE_STATE => success ${bytesToHex(response.content)}")
                if (response.content.size < 5) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = LemBleResponse.DeviceInfo(response.content)
                LepuBleLog.d(tag, "model:$model,MSG_DEVICE_STATE => LemBleResponse.DeviceInfo $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo).post(InterfaceEvent(model, data))
            }
            LemBleCmd.HEAT_MODE -> {
                LepuBleLog.d(tag, "model:$model,HEAT_MODE => success ${bytesToHex(response.content)}")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = byte2UInt(response.content[0])
                if (data == 1) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode).post(InterfaceEvent(model, false))
                }
            }
            LemBleCmd.MASSAGE_MODE -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_MODE => success ${bytesToHex(response.content)}")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MASSAGE_TIME -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_TIME => success ${bytesToHex(response.content)}")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = byte2UInt(response.content[0])
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime).post(InterfaceEvent(model, data))
            }
            LemBleCmd.MASSAGE_LEVEL -> {
                LepuBleLog.d(tag, "model:$model,MASSAGE_LEVEL => success ${bytesToHex(response.content)}")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
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
        LepuBleLog.e(tag, "getBattery")
    }
    fun deviceSwitch(on: Boolean) {
        sendCmd(LemBleCmd.deviceSwitch(on))
        LepuBleLog.e(tag, "deviceSwitch on:$on")
    }
    fun heatMode(on: Boolean) {
        sendCmd(LemBleCmd.heatMode(on))
        LepuBleLog.e(tag, "heatMode on:$on")
    }
    fun massageMode(mode: Int) {
        sendCmd(LemBleCmd.massageMode(mode))
        LepuBleLog.e(tag, "massageMode mode:$mode")
    }
    /**
     * 1-15挡
     */
    fun massageLevel(level: Int) {
        sendCmd(LemBleCmd.massageLevel(level))
        LepuBleLog.e(tag, "massageLevel level:$level")
    }
    fun massageTime(time: Int) {
        sendCmd(LemBleCmd.massageTime(time))
        LepuBleLog.e(tag, "massageTime time:$time")
    }

    override fun getInfo() {
        sendCmd(LemBleCmd.getDeviceState())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList not yet implemented")
    }

}