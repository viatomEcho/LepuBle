package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.BoDeviceInfo
import com.lepu.blepro.ble.data.Pc68bConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import java.util.*

/**
 *
 * 蓝牙操作
 */

class Pc68bBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Pc68bBleInterface"
    private var deviceInfo = BoDeviceInfo()
    private var fileList = mutableListOf<String>()
    private var size = 0
    private var fileName = ""
    private var fileContent = ByteArray(0)

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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Pc68bBleResponse.BleResponse) {
        when (response.token) {
            Pc68bBleCmd.TOKEN_F0 -> {
                when (response.type) {
                    Pc68bBleCmd.MSG_GET_DEVICE_SN -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_SN => success")
                        deviceInfo.sn = com.lepu.blepro.utils.toString(response.content)
                        LepuBleLog.d(tag, "model:$model, sp20Device.sn == " + deviceInfo.sn)
                    }
                    Pc68bBleCmd.MSG_GET_DEVICE_INFO -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_INFO => success")
                        val data = Sp20BleResponse.DeviceInfo(response.content)
                        deviceInfo.deviceName = device.name
                        deviceInfo.softwareV = data.softwareV
                        deviceInfo.hardwareV = data.hardwareV
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LepuBleLog.d(tag, "model:$model, DeviceInfo.deviceName:sp20Device.sn == " + data.deviceName + ":" + deviceInfo.sn)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeviceInfo).post(InterfaceEvent(model, deviceInfo))
                    }
                }
            }
            Pc68bBleCmd.TOKEN_0F -> {
                when (response.type) {
                    Pc68bBleCmd.MSG_ENABLE_RT_PARAM -> {
                        LepuBleLog.d(tag, "model:$model,MSG_ENABLE_RT_PARAM => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                    }
                    Pc68bBleCmd.MSG_ENABLE_RT_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_ENABLE_RT_WAVE => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                    }
                    Pc68bBleCmd.MSG_RT_PARAM -> {
                        LepuBleLog.d(tag, "model:$model,MSG_RT_PARAM => success")
                        val data = Pc68bBleResponse.RtParam(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtParam).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_RT_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_RT_WAVE => success")
                        val data = Pc68bBleResponse.RtWave(response.content)
                        LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtWave).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_SET_TIME -> {
                        LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bSetTime).post(InterfaceEvent(model, true))
                    }
                    Pc68bBleCmd.MSG_GET_OR_SET_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_OR_SET_CONFIG => success")
                        val data = Pc68bConfig(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bConfigInfo).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_STATE_INFO -> {
                        LepuBleLog.d(tag, "model:$model,MSG_STATE_INFO => success")
                        if (response.len < 4) return
                        val data = Pc68bBleResponse.StatusInfo(response.content)
                        LepuBleLog.d(tag, "model:$model, data == $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bStatusInfo).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_GET_TIME -> {
                        val data = Pc68bBleResponse.DeviceTime(response.content)
                        LepuBleLog.d(tag, "model:$model,MSG_GET_TIME => success $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bGetTime).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_GET_FILES -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_FILES => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                        val num = bytes2UIntBig(response.content[0], response.content[1])
                        if (fileName.isEmpty()) {
                            if (num != 0xFFFF) {
                                val data = Pc68bBleResponse.DeviceTime(response.content.copyOfRange(2, 9))
                                fileList.add(data.getTimeString())
                            } else {
                                size = bytes2UIntBig(response.content[2], response.content[3])
                            }
                            if (size == 0 || num == (size-1)) {
                                LepuBleLog.d(tag, "model:$model, fileList == $fileList")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bFileList).post(InterfaceEvent(model, fileList))
                                size = 0
                            }
                        } else {
                            fileContent = ByteArray(bytes2UIntBig(response.content[response.content.size-4],
                                response.content[response.content.size-3],
                                response.content[response.content.size-2],
                                response.content[response.content.size-1]) * 2)
                            size = 0
                            LepuBleLog.d(tag, "model:$model, fileContent size == $size")
                        }
                    }
                    Pc68bBleCmd.MSG_FILE_CONTENT -> {
                        LepuBleLog.d(tag, "model:$model,MSG_FILE_CONTENT => success")
                        System.arraycopy(response.content, 0, fileContent, size, response.content.size)
                        size += response.content.size
                        if (size == fileContent.size) {
                            val data = Pc68bBleResponse.Record(fileContent, fileName)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bReadFileComplete).post(InterfaceEvent(model, data))
                            LepuBleLog.d(tag, "model:$model,read file complete $fileName => $data")
                        }
                    }
                    Pc68bBleCmd.MSG_DELETE_FILE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_DELETE_FILE => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeleteFile).post(InterfaceEvent(model, true))
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
            val len = byte2UInt(bytes[i+3])
            if ((len < 0) || (i+4+len > bytes.size)) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = Pc68bBleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Pc68bBleCmd.getSn())
        sendCmd(Pc68bBleCmd.getInfo())
    }

    override fun syncTime() {
        sendCmd(Pc68bBleCmd.setTime())
//        enableRtData(Pc68bBleCmd.EnableType.RT_PARAM, true)
//        enableRtData(Pc68bBleCmd.EnableType.RT_WAVE, true)
    }

    fun deleteFile() {
        sendCmd(Pc68bBleCmd.deleteFile())
    }

    override fun getFileList() {
        fileName = ""
        fileList.clear()
        sendCmd(Pc68bBleCmd.getFiles(0xFFFF))
    }

    fun setConfig(config: Pc68bConfig) {
        sendCmd(Pc68bBleCmd.setConfig(config.getDataBytes()))
    }
    fun getConfig() {
        sendCmd(Pc68bBleCmd.getConfig())
    }
    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Pc68bBleCmd.enableSwitch(type, enable))
    }

    fun getStateInfo(interval: Int) {
        sendCmd(Pc68bBleCmd.getStateInfo(interval))
    }
    fun getTime() {
        sendCmd(Pc68bBleCmd.getTime())
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(Pc68bBleCmd.getFiles(fileList.indexOf(fileName)))
    }
    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }

    override fun getRtData() {

    }

}