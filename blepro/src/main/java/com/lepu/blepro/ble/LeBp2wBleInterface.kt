package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd.*
import com.lepu.blepro.ble.cmd.LepuBleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.experimental.inv

class LeBp2wBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LeBp2wBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Bp2BleManager(context)
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

    var fileSize: Int = 0
    var fileName:String = ""
    var fileType:Int = 0
    var curSize: Int = 0
    var fileContent : ByteArray? = null
    var userList: LeBp2wUserList? = null
    var chunkSize: Int = 200  // 每次写文件大小

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
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i+8+len > bytes.size) {
                return bytes.copyOfRange(i, bytes.size)
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == calCRC8(temp)) {
                val bleResponse = LepuBleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)


                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }


    private fun onResponseReceived(bleResponse: LepuBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived : " + bleResponse.cmd)

        when (bleResponse.cmd) {
            GET_INFO -> {
                if (bleResponse.len == 0) return
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                val info = LepuDevice(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wInfo)
                    .post(InterfaceEvent(model, info))
            }

            SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncTime)
                    .post(InterfaceEvent(model, true))
            }

            RT_STATE -> {
                if (bleResponse.len == 0) return
                // 主机状态
                LepuBleLog.d(tag, "model:$model,RT_STATE => success")
                val data = Bp2BleRtState(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtState)
                    .post(InterfaceEvent(model, data))
            }

            //----------------------------读文件--------------------------
            READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(readFileEnd())
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }

                fileContent = null
                fileSize = toUInt(bleResponse.content.copyOfRange(0, 4))
                LepuBleLog.d(
                    tag,
                    "download file $fileName CMD_FILE_READ_START fileSize == $fileSize"
                )
                if (fileSize == 0) {
                    sendCmd(readFileEnd())
                } else {
                    sendCmd(readFileData(0))
                }
            }
            READ_FILE_DATA -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_CONTENT => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(readFileEnd())
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_CONTENT => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }

                curSize += bleResponse.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName READ_FILE_CONTENT curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadingFileProgress)
                    .post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(readFileData(curSize))
                } else {
                    sendCmd(readFileEnd())
                }
            }
            READ_FILE_END -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")

                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }

                fileContent?.let {
                    if (it.isNotEmpty()) {
                        if (fileName.endsWith(".list")) {
                            val data = Bp2BleFile(fileName, it, device.name)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList).post(
                                InterfaceEvent(model, data)
                            )
                        } else {
                            val data = LeBp2wEcgFile(fileName, it, device.name)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileComplete)
                                .post(InterfaceEvent(model, data))
                        }
                    } else {
                        if (fileName.endsWith(".list")) {
                            val data = Bp2BleFile(
                                fileName,
                                byteArrayOf(0, fileType.toByte(), 0, 0, 0, 0, 0, 0, 0, 0),
                                device.name
                            )
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList)
                                .post(InterfaceEvent(model, data))
                        } else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError)
                                .post(InterfaceEvent(model, fileName))
                        }
                    }
                }
            }

            //--------------------------写文件--------------------------
            WRITE_FILE_START -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_START => success")
                if (fileSize == 0) {
                    sendCmd(writeFileEnd())
                } else {
                    curSize = if (fileSize < chunkSize) {
                        sendCmd(writeFileData(userList?.getDataBytes()?.copyOfRange(0, fileSize)))
                        fileSize
                    } else {
                        sendCmd(writeFileData(userList?.getDataBytes()?.copyOfRange(0, chunkSize)))
                        chunkSize
                    }
                }
            }
            WRITE_FILE_DATA -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_DATA => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_DATA => success")
                if (curSize < fileSize) {

                    if (fileSize - curSize < chunkSize) {
                        sendCmd(writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, fileSize)))
                        curSize = fileSize
                    } else {
                        sendCmd(writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, curSize + chunkSize)))
                        curSize += chunkSize
                    }

                } else {
                    sendCmd(writeFileEnd())
                }

                val part = Bp2FilePart(fileName, fileSize, curSize)
                LepuBleLog.d(tag, "write file $fileName WRITE_FILE_DATA curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WritingFileProgress)
                    .post(InterfaceEvent(model, part))

            }
            WRITE_FILE_END -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_END => success")
                val crc = FileListCrc(FileType.USER_TYPE, bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileComplete)
                    .post(InterfaceEvent(model, crc))
                curSize = 0
            }

            //实时波形
            RT_DATA -> {
                if (bleResponse.len == 0) return
                LepuBleLog.d(tag, "model:$model,RT_DATA => success")

                val rtData = Bp2BleRtData(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtData)
                    .post(InterfaceEvent(model, rtData))
            }

            SET_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                //心跳音开关
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig)
                    .post(InterfaceEvent(model, true))
            }

            GET_CONFIG -> {
                if (bleResponse.len == 0) return
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                val data = Bp2Config(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetConfig)
                    .post(InterfaceEvent(model, data))
            }

            RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReset)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReset)
                    .post(InterfaceEvent(model, true))
            }

            FACTORY_RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryReset)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryReset)
                    .post(InterfaceEvent(model, true))
            }

            FACTORY_RESET_ALL -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryResetAll)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryResetAll)
                    .post(InterfaceEvent(model, true))
            }

            SWITCH_STATE ->{
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SWITCH_STATE => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState).post(InterfaceEvent(model, true))
            }

            GET_WIFI_ROUTE -> {
                LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                if (bleResponse.pkgType == 0xFF.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiScanning).post(InterfaceEvent(model, true))
                } else {
                    val data = Bp2WifiDevice(bleResponse.content)
                    LepuBleLog.d(tag, "model:$model, data.toString == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiDevice).post(InterfaceEvent(model, data))
                }
            }

            GET_FILE_LIST -> {
                val data = LeBp2wBleList(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, GET_FILE_LIST $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wList).post(
                    InterfaceEvent(model, data)
                )
            }

            GET_WIFI_CONFIG -> {
                if (bleResponse.len == 0) return
                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                var data = Bp2WifiConfig(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, data.toString == $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetWifiConfig).post(InterfaceEvent(model, data))
            }

            SET_WIFI_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig)
                    .post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
            }

            GET_ECG_LIST_CRC -> {
                if (bleResponse.len == 0) return
                val crc = FileListCrc(FileType.ECG_TYPE, bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
                    .post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_ECG_LIST_CRC => success")
            }
            GET_BP_LIST_CRC -> {
                if (bleResponse.len == 0) return
                val crc = FileListCrc(FileType.BP_TYPE, bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
                    .post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_BP_LIST_CRC => success")
            }
            GET_USER_LIST_CRC -> {
                if (bleResponse.len == 0) return
                val crc = FileListCrc(FileType.USER_TYPE, bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
                    .post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_USER_LIST_CRC => success")
            }
            DELETE_FILE -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile)
                        .post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,DELETE_FILE => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile)
                    .post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,DELETE_FILE => success")
            }

        }
    }

    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(LeBp2wBleCmd.getInfo())
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
        sendCmd(setTime())
    }

    fun deleteFile() {
        sendCmd(LeBp2wBleCmd.deleteFile())
    }

    //实时波形命令
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(LeBp2wBleCmd.getRtData())
    }

    fun setConfig(config: Bp2Config){
        LepuBleLog.d(tag, "setConfig...")
        sendCmd(setConfig(config.getDataBytes()))
    }
     fun getConfig(){
         LepuBleLog.d(tag, "getConfig...")
         sendCmd(LeBp2wBleCmd.getConfig())
    }

    override fun getFileList() {
        LepuBleLog.d(tag, "getFileList...")
        sendCmd(LeBp2wBleCmd.getFileList())
    }

    fun getFileListCrc(fileType: Int) {
        sendCmd(LeBp2wBleCmd.getFileListCrc(fileType))
    }

    fun getFileList(fileType: Int) {
        this.fileType = fileType
        when (fileType) {
            FileType.ECG_TYPE -> {
                fileName = "bp2ecg.list"
            }
            FileType.BP_TYPE -> {
                fileName = "bp2nibp.list"
            }
            FileType.USER_TYPE -> {
                fileName = "user.list"
            }
        }
        LepuBleLog.d(tag, "getFileList... fileName == $fileName")
        sendCmd(readFileStart(fileName.toByteArray(), 0))
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.d(tag, "dealReadFile... fileName == $fileName")
        this.fileName = fileName
        sendCmd(readFileStart(fileName.toByteArray(), 0))
    }

    fun writeUserList(userList: LeBp2wUserList) {
        this.userList = userList
        fileSize = userList.getDataBytes().size
        fileName = "user.list"
        LepuBleLog.d(tag, "writeUserList... fileName == $fileName, fileSize == $fileSize")
        sendCmd(writeFileStart(fileName.toByteArray(), 0, fileSize))
    }

    override fun reset() {
        LepuBleLog.d(tag, "reset...")
        sendCmd(LeBp2wBleCmd.reset())
    }
    override fun factoryReset() {
        LepuBleLog.d(tag, "factoryReset...")
        sendCmd(LeBp2wBleCmd.factoryReset())
    }
    override fun factoryResetAll() {
        LepuBleLog.d(tag, "factoryResetAll...")
        sendCmd(LeBp2wBleCmd.factoryResetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }


    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    fun switchState(state: Int){
        LepuBleLog.e("enter  switchState： SWITCH_STATE===$state")
        sendCmd(LeBp2wBleCmd.switchState(state))
    }

    /**
     * 获取wifi路由
     */
    fun getWifiDevice() {
        LepuBleLog.d(tag, "getWifiDevice...")
        sendCmd(getWifiRoute(0))
    }

    /**
     * 配置要连接的wifi
     */
    fun setWifiConfig(config: Bp2WifiConfig) {
        LepuBleLog.d(tag, "setWifiConfig...")
        sendCmd(setWifiConfig(config.getDataBytes()))
    }

    /**
     * 获取当前配置的wifi
     */
    fun getWifiConfig() {
        LepuBleLog.d(tag, "getWifiConfig...")
        sendCmd(LeBp2wBleCmd.getWifiConfig())
    }

    fun getRtState() {
        sendCmd(LeBp2wBleCmd.getRtState())
    }


}