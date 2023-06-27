package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.CountDownTimer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import kotlin.experimental.inv

/**
 * checkme心电设备：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取列表
 * 4.下载文件内容
 * 心电采样率：存储250HZ，实际采样率500HZ
 * 心电增益：n * 4033 / (32767 * 12 * 8) = n * 0.0012820952991323-----779.9732209273494倍
 */
class CheckmeBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "CheckmeBleInterface"

    var fileSize: Int = 0
    var curFileName:String = ""
    var curSize: Int = 0
    var fileContent : ByteArray? = null
    var userId = 1
    var fileType = -1
    var cmdTimer = object : CountDownTimer(3000, 3000) {
        override fun onTick(millisUntilFinished: Long) {
            LepuBleLog.d(tag, "-------cmdTimer-onTick------")
        }
        override fun onFinish() {
            LiveEventBus.get<Int>(EventMsgConst.Cmd.EventCmdResponseTimeOut).post(curCmd)
            curCmd = -1
            LepuBleLog.d(tag, "-------cmdTimer-onFinish------")
        }
    }

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

    private fun sendOxyCmd(cmd: Int){
        LepuBleLog.d(tag, "sendOxyCmd $cmd")

        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + cmd.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
            return
        }
        val bs: ByteArray = when (cmd) {
            PulsebitBleCmd.OXY_CMD_READ_LIST_START -> PulsebitBleCmd.readListStart()
            PulsebitBleCmd.OXY_CMD_READ_LIST_CONTENT -> PulsebitBleCmd.readListContent()
            PulsebitBleCmd.OXY_CMD_READ_LIST_END -> PulsebitBleCmd.readListEnd()
            PulsebitBleCmd.OXY_CMD_READ_END -> PulsebitBleCmd.readFileEnd()
            PulsebitBleCmd.OXY_CMD_READ_CONTENT -> PulsebitBleCmd.readFileContent()
            PulsebitBleCmd.OXY_CMD_PARA_SYNC -> PulsebitBleCmd.syncTime()
            PulsebitBleCmd.OXY_CMD_INFO -> PulsebitBleCmd.getInfo()
            else -> return
        }
        sendCmd(bs)
        curCmd = cmd
        cmdTimer.start()
    }

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] == 0x55.toByte() && bytes[i + 1] == bytes[i + 2].inv()) {
                // need content length
                val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
                if (i + 8 + len > bytes.size) {
                    continue@loop
                }

                val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
                if (temp.size < 8) {
                    continue@loop
                }
                if (temp.last() == BleCRC.calCRC8(temp)) {
                    val bleResponse = CheckmeBleResponse.BleResponse(temp)
                    onResponseReceived(bleResponse)

                    val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                    return hasResponse(tempBytes)
                }
            } else if (bytes[i] == 0xA5.toByte() && bytes[i+1] == 0x5A.toByte()) {

                // need content length
                val len = toUInt(bytes.copyOfRange(i+2, i+3))
                LepuBleLog.d(tag, "want bytes length: $len")
                if (i+len > bytes.size) {
                    continue@loop
                }

                val temp: ByteArray = bytes.copyOfRange(i, i+len)
//                if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = CheckmeBleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+len == bytes.size) null else bytes.copyOfRange(i+len, bytes.size)

                return hasResponse(tempBytes)
//            }
            } else {
                continue@loop
            }
        }
        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: CheckmeBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived curCmd: $curCmd, bytes: ${bytesToHex(response.bytes)}")
        if (response.head == 0x55) {
            if (curCmd == -1) {
                LepuBleLog.d(tag, "onResponseReceived curCmd:$curCmd")
                return
            }

            when (curCmd) {
                PulsebitBleCmd.OXY_CMD_PARA_SYNC -> {
                    clearTimeout()
                    LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeSetTime).post(InterfaceEvent(model, true))
                }
                PulsebitBleCmd.OXY_CMD_INFO -> {

                    clearTimeout()
                    val info = CheckmeBleResponse.DeviceInfo(response.content)

                    LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeDeviceInfo).post(InterfaceEvent(model, info))
                }

                PulsebitBleCmd.OXY_CMD_READ_START -> {
                    clearTimeout()

                    if (response.state) {
                        fileSize = toUInt(response.content)
                        curSize = 0
                        fileContent = null
                        LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                        if (fileSize <= 0) {
                            sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_END)
                        } else {
                            sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_CONTENT)
                        }
                    } else {
                        if (curFileName.contains(".dat")) {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListError).post(InterfaceEvent(model, true))
                        } else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileError).post(InterfaceEvent(model, true))
                        }
                        LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                    }
                }

                PulsebitBleCmd.OXY_CMD_READ_CONTENT -> {
                    clearTimeout()
                    fileContent = add(fileContent, response.content)
                    curSize += response.len
                    if (curFileName.contains(".dat")) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadingFileProgress).post(InterfaceEvent(model, curSize*100/fileSize))
                    }
                    LepuBleLog.d(tag, "model:$model, 读文件中：$curFileName   => $curSize / $fileSize ${curSize*100/fileSize}")

                    if (curSize < fileSize) {
                        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_CONTENT)
                    } else {
                        sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_END)
                    }
                }
                PulsebitBleCmd.OXY_CMD_READ_END -> {
                    clearTimeout()
                    LepuBleLog.d(tag, "model:$model, 读文件完成: $curFileName ==> $fileSize")

                    fileContent?.let {
                        if (curFileName.contains(".dat")) {
                            when (curFileName) {
                                "ecg.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.ECG_TYPE
                                }
                                "oxi.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.OXY_TYPE
                                }
                                "tmp.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.TEMP_TYPE
                                }
                                "usr.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.USER_TYPE
                                }
                                "slm.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.SLM_TYPE
                                }
                                "${userId}bpcal.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.BPCAL_TYPE
                                }
                                "${userId}nibp.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.BP_TYPE
                                }
                                "${userId}dlc.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.DLC_TYPE
                                }
                                "${userId}ped.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.PED_TYPE
                                }
                                "${userId}glu.dat" -> {
                                    fileType = CheckmeBleCmd.ListType.GLU_TYPE
                                }
                            }
                            val data = CheckmeBleResponse.ListContent(userId, fileType, it)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileList).post(InterfaceEvent(model, data))
                            LepuBleLog.d(tag, "model:$model, ListContent : $data")
                        } else {
                            val data = CheckmeBleResponse.FileContent(curFileName, userId, fileType, it)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileComplete).post(InterfaceEvent(model, data))
                            LepuBleLog.d(tag, "model:$model, FileContent : $data")
                        }
                    } ?: run {
                        if (curFileName.contains(".dat")) {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListError).post(InterfaceEvent(model, true))
                        } else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileError).post(InterfaceEvent(model, true))
                        }
                        LepuBleLog.d(tag, "model:$model,  curFile error!!")
                    }
                }
                else -> {
                    clearTimeout()
                }
            }
        } else {
            clearTimeout()
            val data = CheckmeBleResponse.RtData(response.content)
            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeRtData).post(InterfaceEvent(model, data))
            LepuBleLog.d(tag, "model:$model, RtData : $data")
        }
    }

    private fun clearTimeout() {
        curCmd = -1
        cmdTimer.cancel()
    }

    override fun dealReadFile(userId: String, fileName: String) {
        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + PulsebitBleCmd.OXY_CMD_READ_START.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
            return
        }
        if (fileType == -1) {
            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileError).post(InterfaceEvent(model, true))
            return
        }
        this.curFileName = fileName
        LepuBleLog.d(tag, "$userId 将要读取文件 $curFileName")
        sendCmd(PulsebitBleCmd.readFileStart(fileName))
        curCmd = PulsebitBleCmd.OXY_CMD_READ_START
        LepuBleLog.e(tag, "dealReadFile")
    }

    override fun syncTime() {
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_PARA_SYNC)
        LepuBleLog.e(tag, "syncTime")
    }

    override fun getFileList() {
        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + PulsebitBleCmd.OXY_CMD_READ_START.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
            return
        }
        this.curFileName = "1dlc.dat"
        LepuBleLog.d(tag, "将要读取文件 $curFileName")
        sendCmd(PulsebitBleCmd.readFileStart(curFileName))
        curCmd = PulsebitBleCmd.OXY_CMD_READ_START
        LepuBleLog.e(tag, "getFileList")
    }

    fun getFileList(type: Int, id: Int) {
        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + PulsebitBleCmd.OXY_CMD_READ_START.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
            return
        }
        userId = id
        when (type) {
            // 文件无数据返回读文件错误
            CheckmeBleCmd.ListType.ECG_TYPE -> curFileName = "ecg.dat"
            CheckmeBleCmd.ListType.OXY_TYPE -> curFileName = "oxi.dat"
            CheckmeBleCmd.ListType.TEMP_TYPE -> curFileName = "tmp.dat"
            CheckmeBleCmd.ListType.USER_TYPE -> curFileName = "usr.dat"
            CheckmeBleCmd.ListType.SLM_TYPE -> curFileName = "slm.dat"
            CheckmeBleCmd.ListType.BPCAL_TYPE -> curFileName = "${id}bpcal.dat"
            CheckmeBleCmd.ListType.BP_TYPE -> curFileName = "${id}nibp.dat"  // xnibp.dat
            CheckmeBleCmd.ListType.PED_TYPE -> curFileName = "${id}ped.dat"   // xped.dat
            CheckmeBleCmd.ListType.DLC_TYPE -> curFileName = "${id}dlc.dat"    // xdlc.dat
            CheckmeBleCmd.ListType.GLU_TYPE -> curFileName = "${id}glu.dat"    // xglu.dat
        }
        sendCmd(PulsebitBleCmd.readFileStart(curFileName))
        curCmd = PulsebitBleCmd.OXY_CMD_READ_START
        LepuBleLog.e(tag, "getFileList type:$type, curFileName:$curFileName")
    }

    override fun getInfo() {
        sendOxyCmd(PulsebitBleCmd.OXY_CMD_INFO)
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

    fun startRtData() {
        sendCmd(byteArrayOf(0x01))
    }

}