package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.BoDeviceInfo
import com.lepu.blepro.ble.data.Pc68bConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc68b.DeviceInfo
import com.lepu.blepro.ext.pc68b.RtParam
import com.lepu.blepro.ext.pc68b.RtWave
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.getTimeString

/**
 * pc68b指甲血氧：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取设备状态
 * 4.获取历史数据
 * 5.删除文件
 * 6.获取/配置参数
 * 7.实时血氧使能开关
 * receive:
 * 1.实时血氧
 * 血氧采样率：参数1HZ，波形50HZ
 */
class Pc68bBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Pc68bBleInterface"
    private var deviceInfo = BoDeviceInfo()
    private var fileList = mutableListOf<String>()
    private var size = 0
    private var fileName = ""
    private var fileContent = ByteArray(0)

    private var pc68bInfo = DeviceInfo()
    private var param = RtParam()
    private var wave = RtWave()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Ap20BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Ap20BleManager(context)
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
                enableRtData(Pc68bBleCmd.EnableType.RT_PARAM, true)
                enableRtData(Pc68bBleCmd.EnableType.RT_WAVE, true)
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Pc68bBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        when (response.token) {
            Pc68bBleCmd.TOKEN_F0 -> {
                when (response.type) {
                    // 定制版
                    Pc68bBleCmd.MSG_GET_DEVICE_SN -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_SN => success")
                        deviceInfo.sn = com.lepu.blepro.utils.toString(response.content)
                        LepuBleLog.d(tag, "model:$model, sp20Device.sn == " + deviceInfo.sn)
                    }
                    Pc68bBleCmd.MSG_GET_DEVICE_INFO -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_INFO => success")
                        if (response.content.size < 3) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val data = Pc68bBleResponse.DeviceInfo(response.content)
                        deviceInfo.deviceName = data.deviceName
                        device.name?.let {
                            deviceInfo.deviceName = it
                        }
                        deviceInfo.softwareV = data.softwareV
                        deviceInfo.hardwareV = data.hardwareV
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")
                        LepuBleLog.d(tag, "model:$model, DeviceInfo.deviceName:sp20Device.sn == " + data.deviceName + ":" + deviceInfo.sn)

                        pc68bInfo.deviceName = deviceInfo.deviceName
                        pc68bInfo.softwareV = deviceInfo.softwareV
                        pc68bInfo.hardwareV = deviceInfo.hardwareV

                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeviceInfo).post(InterfaceEvent(model, pc68bInfo))
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
                        if (response.content.size < 6) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val data = Pc68bBleResponse.RtParam(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")

                        param.spo2 = data.spo2
                        param.pr = data.pr
                        param.pi = data.pi
                        param.isProbeOff = data.isProbeOff
                        param.isPulseSearching = data.isPulseSearching
                        param.isCheckProbe = data.isCheckProbe
                        param.vol = data.vol
                        param.batLevel = data.battery

                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtParam).post(InterfaceEvent(model, param))
                    }
                    Pc68bBleCmd.MSG_RT_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_RT_WAVE => success")
                        if (response.content.size < 5) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val data = Pc68bBleResponse.RtWave(response.content)
                        LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))

                        wave.waveData = data.waveData
                        wave.waveIntData = data.waveIntData

                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtWave).post(InterfaceEvent(model, wave))
                    }
                    // 定制版
                    Pc68bBleCmd.MSG_SET_TIME -> {
                        LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
//                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bSetTime).post(InterfaceEvent(model, true))
                    }
                    // 定制版
                    Pc68bBleCmd.MSG_GET_OR_SET_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_OR_SET_CONFIG => success")
                        if (response.content.size < 6) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val data = Pc68bConfig(response.content)
                        LepuBleLog.d(tag, "model:$model, data.toString() == $data")



//                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bConfigInfo).post(InterfaceEvent(model, data))
                    }
                    Pc68bBleCmd.MSG_STATE_INFO -> {
                        LepuBleLog.d(tag, "model:$model,MSG_STATE_INFO => success")
                        if (response.len < 4) {
                            LepuBleLog.d(tag, "MSG_STATE_INFO response.len:${response.len}")
                            return
                        }
                        val data = Pc68bBleResponse.StatusInfo(response.content)
                        LepuBleLog.d(tag, "model:$model, data == $data")



//                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bStatusInfo).post(InterfaceEvent(model, data))
                    }
                    // 定制版
                    Pc68bBleCmd.MSG_GET_TIME -> {
                        if (response.content.size < 7) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val data = Pc68bBleResponse.DeviceTime(response.content)
                        LepuBleLog.d(tag, "model:$model,MSG_GET_TIME => success $data")



//                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bGetTime).post(InterfaceEvent(model, data))
                    }
                    // 定制版
                    Pc68bBleCmd.MSG_GET_FILES -> {
                        LepuBleLog.d(tag, "model:$model,MSG_GET_FILES => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
                        if (response.content.size < 2) {
                            LepuBleLog.e(tag, "response.size:${response.content.size} error")
                            return
                        }
                        val num = bytes2UIntBig(response.content[0], response.content[1])
                        if (fileName.isEmpty()) {
                            if (num != 0xFFFF) {
                                val data = Pc68bBleResponse.DeviceTime(response.content.copyOfRange(2, 9))
                                fileList.add(getTimeString(data.year, data.month, data.day, data.hour, data.minute, data.second))
                            } else {
                                size = bytes2UIntBig(response.content[2], response.content[3])
                            }
                            if (size == 0 || num == (size-1)) {
                                LepuBleLog.d(tag, "model:$model, fileList == $fileList")



//                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bFileList).post(InterfaceEvent(model, fileList))
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
                    // 定制版
                    Pc68bBleCmd.MSG_FILE_CONTENT -> {
                        LepuBleLog.d(tag, "model:$model,MSG_FILE_CONTENT => success")
                        System.arraycopy(response.content, 0, fileContent, size, response.content.size)
                        size += response.content.size
                        if (size == fileContent.size) {
                            val data = Pc68bBleResponse.Record(fileContent, fileName)



//                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bReadFileComplete).post(InterfaceEvent(model, data))
                            LepuBleLog.d(tag, "model:$model,read file complete $fileName => $data")
                        }
                    }
                    // 定制版
                    Pc68bBleCmd.MSG_DELETE_FILE -> {
                        LepuBleLog.d(tag, "model:$model,MSG_DELETE_FILE => success")
                        LepuBleLog.d(tag, "model:$model, bytesToHex(response.content)) == " + bytesToHex(response.content))
//                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeleteFile).post(InterfaceEvent(model, true))
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
        LepuBleLog.e(tag, "getInfo")
    }

    override fun syncTime() {
        sendCmd(Pc68bBleCmd.setTime())
        LepuBleLog.e(tag, "syncTime")
    }

    fun deleteFile() {
        sendCmd(Pc68bBleCmd.deleteFile())
        LepuBleLog.e(tag, "deleteFile")
    }

    override fun getFileList() {
        fileName = ""
        fileList.clear()
        sendCmd(Pc68bBleCmd.getFiles(0xFFFF))
        LepuBleLog.e(tag, "getFileList")
    }

    fun setConfig(config: Pc68bConfig) {
        sendCmd(Pc68bBleCmd.setConfig(config.getDataBytes()))
        LepuBleLog.e(tag, "setConfig config:$config")
    }
    fun getConfig() {
        sendCmd(Pc68bBleCmd.getConfig())
        LepuBleLog.e(tag, "getConfig")
    }
    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Pc68bBleCmd.enableSwitch(type, enable))
        LepuBleLog.e(tag, "enableRtData type:$type, enable:$enable")
    }

    fun getStateInfo(interval: Int) {
        sendCmd(Pc68bBleCmd.getStateInfo(interval))
        LepuBleLog.e(tag, "getStateInfo interval:$interval")
    }
    fun getTime() {
        sendCmd(Pc68bBleCmd.getTime())
        LepuBleLog.e(tag, "getTime")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(Pc68bBleCmd.getFiles(fileList.indexOf(fileName)))
        LepuBleLog.e(tag, "dealReadFile userId:$userId, fileName:$fileName")
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

}