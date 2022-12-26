package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import kotlin.experimental.inv

/**
 * vtm01指甲血氧：
 * send:
 *
 * receive:
 * 1.实时血氧
 * 血氧采样率：参数1HZ，波形50HZ
 */
class Vtm01BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Vtm01BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = OxyBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = OxyBleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
        }
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
    private fun onResponseReceived(response: Vtm01BleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            Vtm01BleCmd.GET_INFO -> {
                if (response.content.size < 38) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = LepuDevice(response.content)
                LepuBleLog.d(tag, "model:$model,CMD_INFO => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01Info).post(InterfaceEvent(model, data))
            }
            Vtm01BleCmd.RESET -> {
                if (response.type == 1) {
                    LepuBleLog.d(tag, "model:$model,RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01Reset).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model,RESET => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01Reset).post(InterfaceEvent(model, false))
                }
            }
            Vtm01BleCmd.FACTORY_RESET -> {
                if (response.type == 1) {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01FactoryReset).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01FactoryReset).post(InterfaceEvent(model, false))
                }
            }
            Vtm01BleCmd.GET_CONFIG -> {
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
            }
            Vtm01BleCmd.RT_PARAM -> {
                if (response.content.size < 5) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = Vtm01BleResponse.RtParam(response.content)
                LepuBleLog.d(tag, "model:$model,RT_PARAM => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01RtParam).post(
                    InterfaceEvent(model, data)
                )
            }
            Vtm01BleCmd.RT_DATA -> {
                if (response.content.size < 12) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = Vtm01BleResponse.RtData(response.content)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01RtData).post(InterfaceEvent(model, data))
            }
            Vtm01BleCmd.GET_ORIGINAL_DATA -> {
                if (response.content.size < 2) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = Vtm01BleResponse.OriginalData(response.content)
                LepuBleLog.d(tag, "model:$model,GET_ORIGINAL_DATA => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01OriginalData).post(InterfaceEvent(model, data))
            }
            Vtm01BleCmd.SLEEP_MODE_ON -> {
                if (response.type == 1) {
                    LepuBleLog.d(tag, "model:$model,SLEEP_MODE_ON => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01SleepMode).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model,SLEEP_MODE_ON => failed")
                }
            }
            Vtm01BleCmd.SLEEP_MODE_OFF -> {
                if (response.type == 1) {
                    LepuBleLog.d(tag, "model:$model,SLEEP_MODE_OFF => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01SleepMode).post(InterfaceEvent(model, false))
                } else {
                    LepuBleLog.d(tag, "model:$model,SLEEP_MODE_OFF => failed")
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i+8+len > bytes.size) {
                return bytes.copyOfRange(i, bytes.size)
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == CrcUtil.calCRC8(temp)) {
                val bleResponse = Vtm01BleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)


                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Vtm01BleCmd.getInfo())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        sendCmd(Vtm01BleCmd.getRtData())
        LepuBleLog.e(tag, "getRtData")
    }

    fun getOriginalData() {
        sendCmd(Vtm01BleCmd.getOriginalData())
        LepuBleLog.e(tag, "getOriginalData")
    }
    fun getRtParam() {
        sendCmd(Vtm01BleCmd.getRtParam())
        LepuBleLog.e(tag, "getRtParam")
    }
    fun sleepMode(on: Boolean) {
        if (on) {
            sendCmd(Vtm01BleCmd.sleepModeOn())
        } else {
            sendCmd(Vtm01BleCmd.sleepModeOff())
        }
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList not yet implemented")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile not yet implemented")
    }

    override fun reset() {
        sendCmd(Vtm01BleCmd.reset())
        LepuBleLog.e(tag, "reset")
    }

    override fun factoryReset() {
        sendCmd(Vtm01BleCmd.factoryReset())
        LepuBleLog.e(tag, "factoryReset")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll not yet implemented")
    }

}