package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.BoDeviceInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

/**
 *
 * 蓝牙操作
 */

class Ap20BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Ap20BleInterface"
    private var ap20Device = BoDeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Ap20BleManager(context)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Ap20BleResponse.Ap20Response) {
        if (response.token == Ap20BleCmd.TOKEN_F0) {
            when (response.type) {
                Ap20BleCmd.MSG_GET_DEVICE_SN -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_SN => success")
                    ap20Device.sn = com.lepu.blepro.utils.toString(response.content)
                    LepuBleLog.d(tag, "model:$model, ap20Device.sn == " + ap20Device.sn)
                }
                Ap20BleCmd.MSG_GET_DEVICE_INFO -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_INFO => success")
                    val data = Ap20BleResponse.DeviceInfo(response.content)
                    ap20Device.deviceName = device.name
                    ap20Device.softwareV = data.softwareV
                    ap20Device.hardwareV = data.hardwareV
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LepuBleLog.d(tag, "model:$model, DeviceInfo.deviceName:ap20Device.sn == " + data.deviceName + ":" + ap20Device.sn)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20DeviceInfo).post(
                        InterfaceEvent(model, ap20Device)
                    )
                }
                Ap20BleCmd.MSG_GET_BATTERY -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_BATTERY => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                    val data = toUInt(response.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20Battery).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_GET_BACKLIGHT -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_BACKLIGHT => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                    val data = Ap20BleResponse.ConfigInfo(byteArrayOf(0, response.content[0]))
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20ConfigInfo).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_SET_BACKLIGHT -> {
                    LepuBleLog.d(tag, "model:$model,MSG_SET_BACKLIGHT => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                }
            }
        } else if (response.token == Ap20BleCmd.TOKEN_0F) {
            when (response.type) {
                Ap20BleCmd.MSG_ENABLE_OXY_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_BO_PARAM => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                }
                Ap20BleCmd.MSG_ENABLE_OXY_WAVE -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_BO_WAVE => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                }
                Ap20BleCmd.MSG_RT_OXY_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,MSG_RT_BO_PARAM => success")
                    val data = Ap20BleResponse.RtBoParam(response.content)
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoParam).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_RT_OXY_WAVE -> {
                    LepuBleLog.d(tag, "model:$model,MSG_RT_BO_WAVE => success")
                    val data = Ap20BleResponse.RtBoWave(response.content)
                    LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoWave).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_SET_TIME -> {
                    LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetTime).post(InterfaceEvent(model, true))
                }
                Ap20BleCmd.MSG_GET_CONFIG -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_CONFIG => success")
                    val data = Ap20BleResponse.ConfigInfo(response.content)
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20ConfigInfo).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_SET_CONFIG -> {
                    LepuBleLog.d(tag, "model:$model,MSG_SET_CONFIG => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content) == " + bytesToHex(response.content))
                }
            }
        } else if (response.token == Ap20BleCmd.TOKEN_2D) {
            when (response.type) {
                Ap20BleCmd.MSG_ENABLE_BREATH_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_BREATH_PARAM => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                }
                Ap20BleCmd.MSG_ENABLE_BREATH_WAVE -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_BREATH_WAVE => success")
                    LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                }
                Ap20BleCmd.MSG_RT_BREATH_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,MSG_RT_BREATH_PARAM => success")
                    LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                    val data = Ap20BleResponse.RtBreathParam(response.content)
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam).post(InterfaceEvent(model, data))
                }
                Ap20BleCmd.MSG_RT_BREATH_WAVE -> {
                    LepuBleLog.d(tag, "model:$model,MSG_RT_BREATH_WAVE => success")
                    LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                    val data = Ap20BleResponse.RtBreathWave(response.content)
                    LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave).post(InterfaceEvent(model, data))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes
        if (bytes == null || bytes.size < 6) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 5) {
            if (bytes[i] != 0xAA.toByte()) {
                continue@loop
            }

            if (bytes[i + 1] != 0x55.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+3, i+4))
            if ((len < 0) || (i+4+len > bytes.size)) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = Ap20BleResponse.Ap20Response(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Ap20BleCmd.getSn())
        sendCmd(Ap20BleCmd.getInfo())
//        enableRtData(Ap20BleCmd.EnableType.OXY_PARAM, true)
//        enableRtData(Ap20BleCmd.EnableType.OXY_WAVE, true)
    }

    override fun syncTime() {
        sendCmd(Ap20BleCmd.setTime())
    }


    override fun reset() {

    }

    override fun factoryReset() {

    }

    override fun factoryResetAll() {

    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
    }

    override fun getRtData() {

    }

    override fun getFileList() {
        // 设备没有记录
    }

    fun setConfig(type: Int, config: Int) {
        if (type == Ap20BleCmd.ConfigType.BACK_LIGHT) {
            setBacklight(config)
        } else {
            sendCmd(Ap20BleCmd.setConfig(type, config))
        }
    }
    fun getConfig(type: Int) {
        when (type) {
            Ap20BleCmd.ConfigType.BACK_LIGHT -> {
                getBacklight()
            }
            else -> {
                sendCmd(Ap20BleCmd.getConfig(type))
            }
        }
    }
    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Ap20BleCmd.enableSwitch(type, enable))
    }
    private fun setBacklight(level: Int) {
        sendCmd(Ap20BleCmd.setBacklight(level))
    }
    private fun getBacklight() {
        sendCmd(Ap20BleCmd.getBacklight())
    }
    fun getBattery() {
        sendCmd(Ap20BleCmd.getBattery())
    }

}