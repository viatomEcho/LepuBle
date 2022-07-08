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
 * checkmele心电血氧体温设备:
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取文件列表
 * 4.下载文件内容
 */
class CheckmeLeBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "CheckmeLeBleInterface"

    var fileSize: Int = 0
    var curFileName:String = ""
    var curSize: Int = 0
    var fileContent : ByteArray? = null

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = OxyBleManager(context)
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
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = CheckmeLeBleResponse.BleResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: CheckmeLeBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived curCmd: $curCmd, bytes: ${bytesToHex(response.bytes)}")
        if (curCmd == -1) {
            LepuBleLog.d(tag, "onResponseReceived curCmd:$curCmd")
            return
        }

        when (curCmd) {
            CheckmeLeBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeSetTime).post(InterfaceEvent(model, true))
            }
            CheckmeLeBleCmd.OXY_CMD_INFO -> {

                clearTimeout()
                val info = CheckmeLeBleResponse.DeviceInfo(response.content)

                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDeviceInfo).post(InterfaceEvent(model, info))
            }

            CheckmeLeBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()

                if (response.state) {
                    fileSize = toUInt(response.content)
                    curSize = 0
                    fileContent = null
                    LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                    sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_READ_CONTENT, CheckmeLeBleCmd.readFileContent())

                } else {
                    if (curFileName.contains(".dat")) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError).post(InterfaceEvent(model, true))
                    }
                    LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                }
            }

            CheckmeLeBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                fileContent = add(fileContent, response.content)
                curSize += response.len
                if (curFileName.contains(".dat")) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                }
                LepuBleLog.d(tag, "model:$model, 读文件中：$curFileName   => $curSize / $fileSize ${curSize*100/fileSize}")

                if (curSize < fileSize) {
                    sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_READ_CONTENT, CheckmeLeBleCmd.readFileContent())
                } else {
                    sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_READ_END, CheckmeLeBleCmd.readFileEnd())
                }
            }
            CheckmeLeBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, 读文件完成: $curFileName ==> $fileSize")

                fileContent?.let {
                    if (curFileName.contains(".dat")) {
                        LepuBleLog.d(tag, "model:$model, 读文件完成: $curFileName  bytesToHex ==> ${bytesToHex(it)})")
                        var type = 0
                        when (curFileName) {
                            "temp.dat" -> {
                                type = CheckmeLeBleCmd.ListType.TEMP_TYPE
                            }
                            "oxi.dat" -> {
                                type = CheckmeLeBleCmd.ListType.OXY_TYPE
                            }
                            "ecg.dat" -> {
                                type = CheckmeLeBleCmd.ListType.ECG_TYPE
                            }
                            "2dlc.dat" -> {
                                type = CheckmeLeBleCmd.ListType.DLC_TYPE
                            }
                        }
                        val data = CheckmeLeBleResponse.ListContent(type, it)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileList).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model, 读文件完成: $curFileName  data ==> $data")
                    } else {
                        val data = CheckmeLeBleResponse.EcgFile(curFileName, fileSize, it)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model,  EcgFile $data")
                    }
                } ?: run {
                    if (curFileName.contains(".dat")) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError).post(InterfaceEvent(model, true))
                    }
                    LepuBleLog.d(tag, "model:$model,  curFile error!!")
                }
            }
        }
    }

    private fun clearTimeout() {
        curCmd = -1
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.curFileName = fileName
        sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_READ_START, CheckmeLeBleCmd.readFileStart(fileName))
        LepuBleLog.d(tag, "dealReadFile userId:$userId 将要读取文件 $curFileName")
    }

    override fun syncTime() {
        sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_PARA_SYNC, CheckmeLeBleCmd.syncTime())
        LepuBleLog.e(tag, "syncTime")
    }

    fun getFileList(type: Int) {
        when (type) {
            CheckmeLeBleCmd.ListType.ECG_TYPE -> curFileName = "ecg.dat"
            CheckmeLeBleCmd.ListType.OXY_TYPE -> curFileName = "oxi.dat"
            CheckmeLeBleCmd.ListType.DLC_TYPE -> curFileName = "2dlc.dat"
            CheckmeLeBleCmd.ListType.TEMP_TYPE -> curFileName = "temp.dat"
        }
        sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_READ_START, CheckmeLeBleCmd.readFileStart(curFileName))
        LepuBleLog.e(tag, "getFileList type:$type")
    }

    override fun getInfo() {
        sendOxyCmd(CheckmeLeBleCmd.OXY_CMD_INFO, CheckmeLeBleCmd.getInfo())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList Not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData Not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset Not yet implemented")

    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF Not yet implemented")
    }

}