package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pulsebit.DeviceInfo
import com.lepu.blepro.ext.pulsebit.EcgFile
import com.lepu.blepro.ext.pulsebit.ExEcgDiagnosis
import com.lepu.blepro.utils.*
import kotlin.experimental.inv

/**
 * pulsebit ex心电设备：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取列表
 * 4.下载文件内容
 * 心电采样率：存储250HZ
 * 心电增益：n * 4033 / (32767 * 12 * 8) = n * 0.0012820952991323-----779.9732209273494倍
 */
class PulsebitBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "PulsebitBleInterface"

    var fileSize: Int = 0
    var curFileName:String = ""
    var curSize: Int = 0
    var fileContent : ByteArray? = null

    lateinit var tempList: PulsebitBleResponse.FileList

    private var deviceInfo = DeviceInfo()
    private var fileList = arrayListOf<String>()

    private var ecgFile = EcgFile()
    private var result = ExEcgDiagnosis()

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

                deviceInfo.region = info.region
                deviceInfo.model = info.model
                deviceInfo.hwVersion = info.hwVersion
                deviceInfo.swVersion = info.swVersion
                deviceInfo.lgVersion = info.lgVersion
                deviceInfo.curLanguage = info.curLanguage
                deviceInfo.sn = info.sn
                deviceInfo.fileVer = info.fileVer
                deviceInfo.spcpVer = info.spcpVer
                deviceInfo.branchCode = info.branchCode
                deviceInfo.application = info.application

                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitDeviceInfo).post(InterfaceEvent(model, deviceInfo))
            }

            PulsebitBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()

                if (response.state) {
                    fileSize = toUInt(response.content)
                    curSize = 0
                    fileContent = null
                    LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                    sendOxyCmd(PulsebitBleCmd.OXY_CMD_READ_CONTENT, PulsebitBleCmd.readFileContent())

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
                        tempList = PulsebitBleResponse.FileList(it)

                        for (i in tempList.list) {
                            fileList.add(i.recordName)
                        }

                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList).post(InterfaceEvent(model, fileList))
                        LepuBleLog.d(tag, "model:$model,  FileList $tempList")
                    } else {
                        val data = PulsebitBleResponse.EcgFile(curFileName, fileSize, it)

                        result.isRegular = data.diagnosis.isRegular
                        result.isPoorSignal = data.diagnosis.isPoorSignal
                        result.isFastHr = data.diagnosis.isFastHr
                        result.isSlowHr = data.diagnosis.isSlowHr
                        result.isIrregular = data.diagnosis.isIrregular
                        result.isPvcs = data.diagnosis.isPvcs
                        result.isHeartPause = data.diagnosis.isHeartPause
                        result.isFibrillation = data.diagnosis.isFibrillation
                        result.isWideQrs = data.diagnosis.isWideQrs
                        result.isProlongedQtc = data.diagnosis.isProlongedQtc
                        result.isShortQtc = data.diagnosis.isShortQtc
                        result.isStElevation = data.diagnosis.isStElevation
                        result.isStDepression = data.diagnosis.isStDepression
                        result.result = data.diagnosis.resultMess

                        ecgFile.result = result
                        ecgFile.hrsDataSize = data.hrsDataSize
                        ecgFile.recordingTime = data.recordingTime
                        ecgFile.waveDataSize = data.waveDataSize
                        ecgFile.hr = data.hr
                        ecgFile.st = data.st
                        ecgFile.qrs = data.qrs
                        ecgFile.pvcs = data.pvcs
                        ecgFile.qtc = data.qtc
                        ecgFile.measureMode = data.measureMode
                        ecgFile.filterMode = data.filterMode
                        ecgFile.qt = data.qt
                        ecgFile.hrsData = data.hrsData
                        ecgFile.hrsIntData = data.hrsIntData
                        ecgFile.waveData = data.waveData
                        ecgFile.waveShortData = data.waveShortData
                        ecgFile.wFs = data.wFs
                        ecgFile.fileName = curFileName

                        if (this::tempList.isInitialized) {
                            val index = tempList.fileNames.indexOf(curFileName)
                            ecgFile.user = tempList.list[index].user
                            ecgFile.year = tempList.list[index].year
                            ecgFile.month = tempList.list[index].month
                            ecgFile.day = tempList.list[index].day
                            ecgFile.hour = tempList.list[index].hour
                            ecgFile.minute = tempList.list[index].minute
                            ecgFile.second = tempList.list[index].second
                        }

                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete).post(InterfaceEvent(model, ecgFile))
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
        fileList.clear()
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