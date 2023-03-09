package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * BM-2婴儿监护器心率贴：
 * send:
 * 1.同步时间/UTC时间
 * 2.获取设备信息
 * 3.获取实时数据
 * 4.设置系统开关
 * 5.设置心率、温度高低阈值
 * 6.设置温度单位
 * 7.复位
 * 8.恢复出厂设置
 * 9.恢复生产状态
 * 10.烧录信息
 */
class BtpBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "BtpBleInterface"

    private var fileName = ""
    private var fileId = 0
    private var fileSize = 0
    private var fileContent: ByteArray? = null

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Er1BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Er1BleManager(context)
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
    private fun onResponseReceived(response: BtpBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            BtpBleCmd.GET_INFO -> {
                if (response.content.size < 38) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = LepuDevice(response.content)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetInfo).post(InterfaceEvent(model, data))

            }

            BtpBleCmd.GET_BATTERY -> {
                if (response.content.size < 4) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = KtBleBattery(response.content)

                LepuBleLog.d(tag, "model:$model,GET_BATTERY => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetBattery).post(InterfaceEvent(model, data))

            }

            BtpBleCmd.RT_DATA -> {
                if (response.content.size < 10) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = BtpBleResponse.RtData(response.content)

                LepuBleLog.d(tag, "model:$model,RT_DATA => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpRtData).post(InterfaceEvent(model, data))
            }
            BtpBleCmd.GET_FILE_LIST -> {
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val data = BtpBleResponse.FileList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetFileList).post(InterfaceEvent(model, data))
            }
            BtpBleCmd.FILE_READ_START -> {
                if (response.content.size < 8 || response.pkgType != 0x01.toByte()) {
                    sendCmd(BtpBleCmd.fileReadEnd())
                    LepuBleLog.d(tag, "model:$model,FILE_READ_START => error")
                    return
                }
                fileContent = null
                fileId = toUInt(response.content.copyOfRange(0, 4))
                fileSize = toUInt(response.content.copyOfRange(4, 8))
                if (fileSize <= 0) {
                    sendCmd(BtpBleCmd.fileReadEnd())
                } else {
                    sendCmd(BtpBleCmd.fileReadPkg(fileId, offset, fileSize))
                }
                LepuBleLog.d(tag, "model:$model,FILE_READ_START => fileName: $fileName, fileId: $fileId, offset: $offset, fileSize: $fileSize")
            }
            BtpBleCmd.FILE_READ_PKG -> {
                offset += response.len
                fileContent = add(fileContent, response.content)
                val percent = offset*100/fileSize
                LepuBleLog.d(tag, "model:$model,FILE_READ_PKG => fileId: $fileId, offset: $offset, fileSize: $fileSize")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadingFileProgress).post(InterfaceEvent(model, percent))
                if (offset < fileSize) {
                    sendCmd(BtpBleCmd.fileReadPkg(fileId, offset, fileSize))
                } else {
                    sendCmd(BtpBleCmd.fileReadEnd())
                }
            }
            BtpBleCmd.FILE_READ_END -> {
                if (offset != fileSize || response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,FILE_READ_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadFileError).post(InterfaceEvent(model, true))
                    return
                }
                offset = 0
                fileContent?.let {
                    val data = BtpBleResponse.BtpFile(it)
                    LepuBleLog.d(tag, "model:$model,FILE_READ_END => data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadFileComplete).post(InterfaceEvent(model, data))
                } ?: kotlin.run {
                    LepuBleLog.d(tag, "model:$model,FILE_READ_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadFileError).post(InterfaceEvent(model, true))
                }
                fileContent = null
            }
            BtpBleCmd.GET_CONFIG -> {
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,GET_CONFIG => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetConfigError).post(InterfaceEvent(model, true))
                    return
                }
                val data = BtpBleResponse.ConfigInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetConfig).post(InterfaceEvent(model, data))
            }

            BtpBleCmd.RESET -> {
                LepuBleLog.d(tag, "model:$model,RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReset).post(InterfaceEvent(model, false))
                }
            }

            BtpBleCmd.FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryReset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryReset).post(InterfaceEvent(model, false))
                }
            }

            BtpBleCmd.FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryResetAll).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryResetAll).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetTime).post(InterfaceEvent(model, true))
            }
            BtpBleCmd.BURN_FACTORY_INFO -> {
                LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpBurnFactoryInfo).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpBurnFactoryInfo).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_LOW_HR -> {
                LepuBleLog.d(tag, "model:$model,SET_LOW_HR => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowHr).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowHr).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_HIGH_HR -> {
                LepuBleLog.d(tag, "model:$model,SET_HIGH_HR => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighHr).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighHr).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_LOW_TEMP -> {
                LepuBleLog.d(tag, "model:$model,SET_LOW_TEMP => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowTemp).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowTemp).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_HIGH_TEMP -> {
                LepuBleLog.d(tag, "model:$model,SET_HIGH_TEMP => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighTemp).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighTemp).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_TEMP_UNIT -> {
                LepuBleLog.d(tag, "model:$model,SET_TEMP_UNIT => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetTempUnit).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetTempUnit).post(InterfaceEvent(model, false))
                }
            }
            BtpBleCmd.SET_SYSTEM_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SWITCH => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetSystemSwitch).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetSystemSwitch).post(InterfaceEvent(model, false))
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

            // seqNo
            /*if (bytes[i+1].toInt() == BtpBleCmd.FILE_READ_PKG) {
                if (bytes[i+4].toInt() != (BtpBleCmd.seqNo-1)) {
                    continue@loop
                }
            }*/

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.size < 7) {
                continue@loop
            }
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = BtpBleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(BtpBleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(BtpBleCmd.setTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        sendCmd(BtpBleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        sendCmd(BtpBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        sendCmd(BtpBleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    /**
     * get real-time data
     */
    override fun getRtData() {
        sendCmd(BtpBleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData...")
    }

    fun getConfig() {
        sendCmd(BtpBleCmd.getConfig())
        LepuBleLog.d(tag, "getConfig...")
    }
    fun setSystemSwitch(hrSwitch: Boolean, lightSwitch: Boolean, tempSwitch: Boolean) {
        sendCmd(BtpBleCmd.setSystemSwitch(hrSwitch, lightSwitch, tempSwitch))
        LepuBleLog.d(tag, "setSystemSwitch...hrSwitch:$hrSwitch, lightSwitch:$lightSwitch, tempSwitch:$tempSwitch")
    }
    fun setLowHr(lowHr: Int) {
        sendCmd(BtpBleCmd.setLowHr(lowHr))
        LepuBleLog.d(tag, "setLowHr...lowHr:$lowHr")
    }
    fun setHighHr(highHr: Int) {
        sendCmd(BtpBleCmd.setHighHr(highHr))
        LepuBleLog.d(tag, "setHighHr...highHr:$highHr")
    }
    fun setLowTemp(lowTemp: Int) {
        sendCmd(BtpBleCmd.setLowTemp(lowTemp))
        LepuBleLog.d(tag, "setLowTemp...lowTemp:$lowTemp")
    }
    fun setHighTemp(highTemp: Int) {
        sendCmd(BtpBleCmd.setHighTemp(highTemp))
        LepuBleLog.d(tag, "setHighTemp...highTemp:$highTemp")
    }
    fun setTempUnit(unit: Int) {
        sendCmd(BtpBleCmd.setTempUnit(unit))
        LepuBleLog.d(tag, "setTempUnit...unit:$unit")
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(BtpBleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...config:$config")
    }

    fun getBattery() {
        sendCmd(BtpBleCmd.getBattery())
        LepuBleLog.d(tag, "getBattery...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(BtpBleCmd.getFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile...")
    }

    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(BtpBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

}