package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * O2Ring S血氧设备：
 */
class OxyIIBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "OxyIIBleInterface"
    var fileSize: Int = 0
    var fileName: String = ""
    var curSize: Int = 0
    var fileContent = ByteArray(0)

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Pf10Aw1BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Pf10Aw1BleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
        }
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
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
                val bleResponse = LepuBleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LepuBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            LpBleCmd.SET_TIME -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, SET_TIME => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetTime).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, SET_TIME => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetTime).post(InterfaceEvent(model, false))
                }
            }
            LpBleCmd.GET_BATTERY -> {
                if (response.len < 4) {
                    LepuBleLog.d(tag, "model:$model,GET_BATTERY => response.len < 4")
                    return
                }
                val data = KtBleBattery(response.content)
                LepuBleLog.d(tag, "model:$model,GET_BATTERY => success, data: $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetBattery).post(InterfaceEvent(model, data))
            }
            LpBleCmd.GET_INFO -> {
                if (response.content.size < 38) {
                    LepuBleLog.e(tag, "GET_INFO response.size:${response.content.size} error")
                    return
                }
                val data = LepuDevice(response.content)
                LepuBleLog.d(tag, "model:$model, GET_INFO => success, data: $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetInfo).post(InterfaceEvent(model, data))
            }
            LpBleCmd.RESET -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReset).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, RESET => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReset).post(InterfaceEvent(model, false))
                }
            }
            LpBleCmd.FACTORY_RESET -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, FACTORY_RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryReset).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, FACTORY_RESET => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryReset).post(InterfaceEvent(model, false))
                }
            }
            LpBleCmd.FACTORY_RESET_ALL -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, FACTORY_RESET_ALL => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryResetAll).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, FACTORY_RESET_ALL => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryResetAll).post(InterfaceEvent(model, false))
                }
            }
            LpBleCmd.BURN_FACTORY_INFO -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, BURN_FACTORY_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIBurnFactoryInfo).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, BURN_FACTORY_INFO => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIBurnFactoryInfo).post(InterfaceEvent(model, false))
                }
            }
            LpBleCmd.GET_FILE_LIST, OxyIIBleCmd.GET_FILE_LIST -> {
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "GET_FILE_LIST response.size:${response.content.size} error")
                    return
                }
                val data = if (response.cmd == LpBleCmd.GET_FILE_LIST) {
                    OxyIIBleResponse.FileList(OxyIIBleCmd.FileType.OXY, response.content)
                } else {
                    OxyIIBleResponse.FileList(OxyIIBleCmd.FileType.PPG, response.content)
                }
                LepuBleLog.d(tag, "model:$model, GET_FILE_LIST => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetFileList).post(InterfaceEvent(model, data))
            }
            LpBleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                    return
                }
                if (response.len < 4) {
                    LepuBleLog.d(tag, "model:$model,READ_FILE_START => response.len < 4")
                    return
                }
                fileContent = if (offset == 0) {
                    ByteArray(0)
                } else {
                    DownloadHelper.readFile(model, "", fileName)
                }
                offset = fileContent.size
                fileSize = toUInt(response.content)
                if (fileSize <= 0) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                } else {
                    sendCmd(LpBleCmd.readFileData(offset, aesEncryptKey))
                }
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => fileName: $fileName, offset: $offset, fileSize: $fileSize")
            }
            LpBleCmd.READ_FILE_DATA -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                    return
                }
                offset += response.len
                DownloadHelper.writeFile(model, "", fileName, "dat", response.content)
                fileContent = add(fileContent, response.content)
                val percent = offset*100/fileSize
                LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => offset: $offset, fileSize: $fileSize")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadingFileProgress).post(InterfaceEvent(model, percent))
                if (offset < fileSize) {
                    sendCmd(LpBleCmd.readFileData(offset, aesEncryptKey))
                } else {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                }
            }
            LpBleCmd.READ_FILE_END -> {
                if (isCancelRF || isPausedRF) {
                    LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                    return
                }
                if (fileContent.size < fileSize) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileError).post(InterfaceEvent(model, true))
                } else {
                    val data = OxyIIBleResponse.BleFile(OxyIIBleCmd.FileType.OXY, fileContent)
                    LepuBleLog.d(tag, "model:$model,READ_FILE_END => data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileComplete).post(InterfaceEvent(model, data))
                }
            }
            OxyIIBleCmd.RT_PARAM -> {
                if (response.content.size < 14) {
                    LepuBleLog.e(tag, "RT_PARAM response.size:${response.content.size} error")
                }
                val data = OxyIIBleResponse.RtParam(response.content)
                LepuBleLog.d(tag, "model:$model, RT_PARAM => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtParam).post(InterfaceEvent(model, data))
            }
            OxyIIBleCmd.RT_WAVE -> {
                if (response.content.size < 6) {
                    LepuBleLog.e(tag, "RT_WAVE response.size:${response.content.size} error")
                }
                val data = OxyIIBleResponse.RtWave(response.content)
                LepuBleLog.d(tag, "model:$model, RT_WAVE => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtWave).post(InterfaceEvent(model, data))
            }
            OxyIIBleCmd.RT_DATA -> {
                if (response.content.size < 26) {
                    LepuBleLog.e(tag, "RT_DATA response.size:${response.content.size} error")
                }
                val data = OxyIIBleResponse.RtData(response.content)
                LepuBleLog.d(tag, "model:$model, RT_DATA => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtData).post(InterfaceEvent(model, data))
            }
            OxyIIBleCmd.RT_PPG -> {
                if (response.content.size < 2) {
                    LepuBleLog.e(tag, "RT_PPG response.size:${response.content.size} error")
                }
                val data = OxyIIBleResponse.RtPpg(response.content)
                LepuBleLog.d(tag, "model:$model, RT_PPG => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtPpg).post(InterfaceEvent(model, data))
            }
            OxyIIBleCmd.GET_CONFIG -> {
                if (response.content.size < 9) {
                    LepuBleLog.e(tag, "GET_CONFIG response.size:${response.content.size} error")
                }
                val data = OxyIIConfig(response.content)
                LepuBleLog.d(tag, "model:$model, GET_CONFIG => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetConfig).post(InterfaceEvent(model, data))
            }
            OxyIIBleCmd.SET_CONFIG -> {
                if (response.pkgType == OxyIIBleCmd.TYPE_NORMAL_RECEIVE) {
                    LepuBleLog.d(tag, "model:$model, SET_CONFIG => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetConfig).post(InterfaceEvent(model, true))
                } else {
                    LepuBleLog.d(tag, "model:$model, SET_CONFIG => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetConfig).post(InterfaceEvent(model, false))
                }
            }
            OxyIIBleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(OxyIIBleCmd.readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                    return
                }
                if (response.len < 4) {
                    LepuBleLog.d(tag, "model:$model,READ_FILE_START => response.len < 4")
                    return
                }
                fileContent = if (offset == 0) {
                    ByteArray(0)
                } else {
                    DownloadHelper.readFile(model, "", fileName)
                }
                offset = fileContent.size
                fileSize = toUInt(response.content)
                if (fileSize <= 0) {
                    sendCmd(OxyIIBleCmd.readFileEnd())
                } else {
                    sendCmd(OxyIIBleCmd.readFileData(offset))
                }
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => fileName: $fileName, offset: $offset, fileSize: $fileSize")
            }
            OxyIIBleCmd.READ_FILE_DATA -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(OxyIIBleCmd.readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                    return
                }
                offset += response.len
                DownloadHelper.writeFile(model, "", fileName, "dat", response.content)
                fileContent = add(fileContent, response.content)
                val percent = offset*100/fileSize
                LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => offset: $offset, fileSize: $fileSize")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadingFileProgress).post(InterfaceEvent(model, percent))
                if (offset < fileSize) {
                    sendCmd(OxyIIBleCmd.readFileData(offset))
                } else {
                    sendCmd(OxyIIBleCmd.readFileEnd())
                }
            }
            OxyIIBleCmd.READ_FILE_END -> {
                if (isCancelRF || isPausedRF) {
                    LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                    return
                }
                if (fileContent.size < fileSize) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileError).post(InterfaceEvent(model, true))
                } else {
                    val data = OxyIIBleResponse.BleFile(OxyIIBleCmd.FileType.PPG, fileContent)
                    LepuBleLog.d(tag, "model:$model,READ_FILE_END => data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileComplete).post(InterfaceEvent(model, data))
                }
            }
        }
    }

    fun getConfig() {
        sendCmd(OxyIIBleCmd.getConfig())
        LepuBleLog.d(tag, "getConfig")
    }
    fun setConfig(config: OxyIIConfig) {
        sendCmd(OxyIIBleCmd.setConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setConfig : $config")
    }
    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LpBleCmd.burnFactoryInfo(config.convert2Data(), aesEncryptKey))
        LepuBleLog.d(tag, "burnFactoryInfo : $config")
    }
    fun getBattery() {
        sendCmd(LpBleCmd.getBattery(aesEncryptKey))
        LepuBleLog.d(tag, "getBattery")
    }

    override fun getInfo() {
        sendCmd(LpBleCmd.getInfo(aesEncryptKey))
        LepuBleLog.d(tag, "getInfo")
    }

    override fun syncTime() {
        sendCmd(LpBleCmd.setTime(aesEncryptKey))
        LepuBleLog.e(tag, "syncTime")
    }

    override fun getRtData() {
//        sendCmd(OxyIIBleCmd.getRtData())
        sendCmd(OxyIIBleCmd.getRtWave())
        LepuBleLog.e(tag, "getRtData")
    }

    override fun getFileList() {
        sendCmd(LpBleCmd.getFileList(aesEncryptKey))
        LepuBleLog.e(tag, "getFileList")
    }
    fun getFileList(type: Int) {
        if (type == OxyIIBleCmd.FileType.OXY) {
            sendCmd(LpBleCmd.getFileList(aesEncryptKey))
        } else if (type == OxyIIBleCmd.FileType.PPG) {
            sendCmd(OxyIIBleCmd.getFileList())
        }
        LepuBleLog.e(tag, "getFileList, type : $type")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(LpBleCmd.readFileStart(fileName.toByteArray(), 0, aesEncryptKey))
        LepuBleLog.e(tag, "dealReadFile")
    }

    override fun reset() {
        sendCmd(LpBleCmd.reset(aesEncryptKey))
        LepuBleLog.e(tag, "reset")
    }

    override fun factoryReset() {
        sendCmd(LpBleCmd.factoryReset(aesEncryptKey))
        LepuBleLog.e(tag, "factoryReset")
    }

    override fun factoryResetAll() {
        sendCmd(LpBleCmd.factoryResetAll(aesEncryptKey))
        LepuBleLog.e(tag, "factoryResetAll")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }


}