package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.BoDeviceInfo
import com.lepu.blepro.ble.data.Sp20Config
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

class Sp20BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Sp20BleInterface"
    private var sp20Device = BoDeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Sp20BleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
                enableRtData(Sp20BleCmd.EnableType.OXY_PARAM, true)
                enableRtData(Sp20BleCmd.EnableType.OXY_WAVE, true)
            }
            .enqueue()
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Sp20BleResponse.Sp20Response) {
        when (response.token) {
            Sp20BleCmd.TOKEN_F0 -> {
                when (response.type) {
                    Sp20BleCmd.MSG_GET_DEVICE_SN -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_SN => success")
                        sp20Device.sn = com.lepu.blepro.utils.toString(response.content)
                        LepuBleLog.d(tag, "model:$model, sp20Device.sn == " + sp20Device.sn)
                    }
                    Sp20BleCmd.MSG_GET_DEVICE_INFO -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_INFO => success")
                        val data = Sp20BleResponse.DeviceInfo(response.content)
                        sp20Device.deviceName = data.deviceName
                        device.name?.let {
                            sp20Device.deviceName = it
                        }
                        sp20Device.softwareV = data.softwareV
                        sp20Device.hardwareV = data.hardwareV
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LepuBleLog.d(
                            tag,
                            "model:$model, DeviceInfo.deviceName:sp20Device.sn == " + data.deviceName + ":" + sp20Device.sn
                        )
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20DeviceInfo)
                            .post(
                                InterfaceEvent(model, sp20Device)
                            )
                    }
                    Sp20BleCmd.MSG_GET_BATTERY -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_BATTERY => success")
                        LepuBleLog.d(
                            tag,
                            "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content)
                        )
                        val data = toUInt(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery)
                            .post(InterfaceEvent(model, data))
                    }
                }
            }
            Sp20BleCmd.TOKEN_0F -> {
                when (response.type) {
                    Sp20BleCmd.MSG_ENABLE_OXY_PARAM -> {
                        LepuBleLog.d(tag, "model:$model,MSG_ENABLE_OXY_PARAM => success")
                        LepuBleLog.d(
                            tag,
                            "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content)
                        )
                    }
                    Sp20BleCmd.MSG_ENABLE_OXY_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_ENABLE_OXY_WAVE => success")
                        LepuBleLog.d(
                            tag,
                            "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content)
                        )
                    }
                    Sp20BleCmd.MSG_RT_OXY_PARAM -> {
                        LepuBleLog.d(tag, "model:$model,MSG_RT_OXY_PARAM => success")
                        val data = Sp20BleResponse.RtParam(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtParam)
                            .post(InterfaceEvent(model, data))
                    }
                    Sp20BleCmd.MSG_RT_OXY_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_RT_OXY_WAVE => success")
                        val data = Sp20BleResponse.RtWave(response.content)
                        LepuBleLog.d(
                            tag,
                            "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content)
                        )
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtWave)
                            .post(InterfaceEvent(model, data))
                    }
                    Sp20BleCmd.MSG_SET_TIME -> {
                        LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success")
                        LepuBleLog.d(
                            tag,
                            "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content)
                        )
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetTime)
                            .post(InterfaceEvent(model, true))
                    }
                    Sp20BleCmd.MSG_GET_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_CONFIG => success")
                        val data = Sp20Config(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig)
                            .post(InterfaceEvent(model, data))
                    }
                    Sp20BleCmd.MSG_SET_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,MSG_SET_CONFIG => success")
                        val data = Sp20Config(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetConfig)
                            .post(InterfaceEvent(model, data))
                    }
                }
            }
            Sp20BleCmd.TOKEN_3C -> {
                when (response.type) {
                    Sp20BleCmd.MSG_TEMP -> {
                        LepuBleLog.d(tag, "model:$model,MSG_TEMP => success")
                        val data = Sp20BleResponse.TempData(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20TempData)
                            .post(InterfaceEvent(model, data))
                    }
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
                val bleResponse = Sp20BleResponse.Sp20Response(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Sp20BleCmd.getSn())
        sendCmd(Sp20BleCmd.getInfo())
//        enableRtData(Sp20BleCmd.EnableType.OXY_PARAM, true)
//        enableRtData(Sp20BleCmd.EnableType.OXY_WAVE, true)
    }

    override fun syncTime() {
        sendCmd(Sp20BleCmd.setTime())
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

    }

    fun setConfig(config: Sp20Config) {
        sendCmd(Sp20BleCmd.setConfig(config.getDataBytes()))
    }
    fun getConfig(type: Int) {
        sendCmd(Sp20BleCmd.getConfig(type))
    }
    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Sp20BleCmd.enableSwitch(type, enable))
    }

    fun getBattery() {
        sendCmd(Sp20BleCmd.getBattery())
    }

}