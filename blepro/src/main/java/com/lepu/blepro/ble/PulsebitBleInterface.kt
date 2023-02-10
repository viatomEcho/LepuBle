package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import kotlin.experimental.inv

/**
 * pulsebit ex心电设备：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取列表
 * 4.下载文件内容
 * 心电采样率：存储250HZ，实际采样率500HZ
 * 心电增益：n * 4033 / (32767 * 12 * 8) = n * 0.0012820952991323-----779.9732209273494倍
 */
class PulsebitBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "PulsebitBleInterface"

    var fileSize: Int = 0
    var curFileName:String = ""
    var curSize: Int = 0
    var fileContent : ByteArray? = null

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
            .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    private fun sendOxyCmd(cmd: Int, bs: ByteArray){
        LepuBleLog.d(tag, "sendOxyCmd $cmd")

        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + cmd.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
            return
        }
        sendCmd(bs)
        curCmd = cmd
    }

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] != 0x55.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i + 8 + len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.size < 8) {
                continue@loop
            }
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = PulsebitBleResponse.BleResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: PulsebitBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived curCmd: $curCmd, bytes: ${bytesToHex(response.bytes)}")
        if (curCmd == -1) {
            LepuBleLog.d(tag, "onResponseReceived curCmd:$curCmd")
            return
        }

        when (curCmd) {
            PulsebitBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitSetTime).post(InterfaceEvent(model, true))
            }
            PulsebitBleCmd.OXY_CMD_INFO -> {

                clearTimeout()
                val info = PulsebitBleResponse.DeviceInfo(response.content)

                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitDeviceInfo).post(InterfaceEvent(model, info))
            }

            PulsebitBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()

                if (response.state) {
                    fileSize = toUInt(response.content)
                    curSize = 0
                    fileContent = null
                    LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                    if (fileSize <= 0) {
                        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_END, PulsebitBleCmd.readFileEnd())
                    } else {
                        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_CONTENT, PulsebitBleCmd.readFileContent())
                    }
                } else {
                    if (curFileName.contains(".dat")) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError).post(InterfaceEvent(model, true))
                    }
                    LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                }
            }

            PulsebitBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                fileContent = add(fileContent, response.content)
                curSize += response.len
                if (curFileName.contains(".dat")) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                }
                LepuBleLog.d(tag, "model:$model, 读文件中：$curFileName   => $curSize / $fileSize ${curSize*100/fileSize}")

                if (curSize < fileSize) {
                    sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_CONTENT, PulsebitBleCmd.readFileContent())
                } else {
                    sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_END, PulsebitBleCmd.readFileEnd())
                }
            }
            PulsebitBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, 读文件完成: $curFileName ==> $fileSize")

                fileContent?.let {
                    if (curFileName.contains(".dat")) {
                        val data = PulsebitBleResponse.FileList(it)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model,  FileList $data")
                    } else {
                        val data = PulsebitBleResponse.EcgFile(curFileName, fileSize, it)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model,  EcgFile $data")
                    }
                } ?: run {
                    if (curFileName.contains(".dat")) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError).post(InterfaceEvent(model, true))
                    }
                    LepuBleLog.d(tag, "model:$model,  curFile error!!")
                }
            }
            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearTimeout() {
        curCmd = -1
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.curFileName = fileName
        LepuBleLog.d(tag, "$userId 将要读取文件 $curFileName")
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_START, PulsebitBleCmd.readFileStart(fileName))
        LepuBleLog.e(tag, "dealReadFile")
    }

    override fun syncTime() {
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_PARA_SYNC, PulsebitBleCmd.syncTime())
        LepuBleLog.e(tag, "syncTime")
    }

    override fun getFileList() {
        this.curFileName = "1dlc.dat"
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_START, PulsebitBleCmd.readFileStart(curFileName))
        LepuBleLog.e(tag, "getFileList")
    }

    override fun getInfo() {
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_INFO, PulsebitBleCmd.getInfo())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData Not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll Not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset Not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF Not yet implemented")
    }

}