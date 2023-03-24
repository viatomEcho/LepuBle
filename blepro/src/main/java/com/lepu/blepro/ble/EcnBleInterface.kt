package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * 口袋心电图机
 */
class EcnBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "EcnBleInterface"
    private var fileName: String? = null
    private var userId: String? = null
    private var curSize = 0
    private var fileSize = 0
    private var fileContent = ByteArray(0)

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = EcnBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = EcnBleManager(context)
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
    private fun onResponseReceived(response: EcnBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            EcnBleCmd.GET_FILE_LIST -> {
                if (response.content.isEmpty()) {
                    LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => error response.content.isEmpty()")
                    return
                }
                val data = EcnBleResponse.FileList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success data: $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetFileList).post(InterfaceEvent(model, data))
            }
            EcnBleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    sendCmd(EcnBleCmd.readFileEnd())
                    return
                }
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileError).post(InterfaceEvent(model, true))
                    return
                }
                fileSize = toUInt(response.content)
                fileContent = ByteArray(0)
                curSize = 0
                if (fileSize <= 0) {
                    sendCmd(EcnBleCmd.readFileEnd())
                } else {
                    sendCmd(EcnBleCmd.readFileData(curSize))
                }
            }
            EcnBleCmd.READ_FILE_DATA -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    sendCmd(EcnBleCmd.readFileEnd())
                    return
                }
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_DATA => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileError).post(InterfaceEvent(model, true))
                    return
                }
                fileContent = fileContent.plus(response.content)
                curSize += response.len
                LepuBleLog.d(tag, "download file $fileName READ_FILE_DATA curSize == $curSize | fileSize == $fileSize")
                val percent = curSize*100/fileSize
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadingFileProgress).post(InterfaceEvent(model, percent))
                if (curSize < fileSize) {
                    sendCmd(EcnBleCmd.readFileData(curSize))
                } else {
                    sendCmd(EcnBleCmd.readFileEnd())
                }
            }
            EcnBleCmd.READ_FILE_END -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    LepuBleLog.d(tag, "READ_FILE_END isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    sendCmd(EcnBleCmd.readFileEnd())
                    return
                }
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_END => error")
                    return
                }
                val data = bytesToHex(fileContent)
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success data: $data")
                if (curSize != fileSize) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileError).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileComplete).post(InterfaceEvent(model, fileContent))
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
                val bleResponse = EcnBleResponse.BleResponse(temp)
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
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "setVibrateConfig...userId:$userId, fileName:$fileName")
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.fileName = fileName
        sendCmd(EcnBleCmd.readFileStart(fileName.toByteArray(), 0)) // 读开始永远是0
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
    }

    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(EcnBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

}