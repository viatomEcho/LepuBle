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
import com.lepu.blepro.ext.lpbp2w.*
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * lpbp2w心电血压计：
 * send:
 * 1.同步时间(utc时间)
 * 2.获取设备信息
 * 3.获取实时心电、血压
 * 4.获取/配置参数
 * 5.获取/设置设备状态
 * 6.获取文件列表
 * 7.下载文件内容
 * 8.恢复出厂设置
 * 9.获取路由
 * 10.获取/配置WiFi
 * 11.写用户文件
 * 12.获取文件crc
 * 心电采样率：实时250HZ，存储125HZ
 * 血压采样率：实时50HZ，存储50HZ
 * 心电增益：n * 0.003098-----322.7888960619755倍
 */
class LpBp2wBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LpBp2wBleInterface"
    private var deviceInfo = DeviceInfo()
    private var ecgFile = EcgFile()
    private var rtData = RtData()
    private var rtStatus = RtStatus()
    private var rtParam = RtParam()
    private var config = LpBp2wConfig()
    private var users = arrayListOf<UserInfo>()
    private var bpRecords = arrayListOf<BpRecord>()
    private var ecgRecords = arrayListOf<EcgRecord>()
    private var wifiList = arrayListOf<LpBp2Wifi>()
    private var wifi = LpBp2Wifi()
    private var server = LpBp2wServer()
    private var wifiConfig = LpBp2WifiConfig()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Bp2BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Bp2BleManager(context)
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
        LepuBleLog.d(tag, "onResponseReceived cmd: ${bleResponse.cmd}, bytes: ${bytesToHex(bleResponse.bytes)}")

        when (bleResponse.cmd) {
            GET_INFO -> {
                if (bleResponse.len == 0 || bleResponse.content.size < 38) {
                    LepuBleLog.d(tag, "GET_INFO bleResponse.len == 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                val info = LepuDevice(bleResponse.content)
                deviceInfo.hwVersion = info.hwV
                deviceInfo.swVersion = info.fwV
                deviceInfo.btlVersion = info.btlV
                deviceInfo.branchCode = info.branchCode
                deviceInfo.snLen = info.snLen
                deviceInfo.sn = info.sn
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wInfo).post(InterfaceEvent(model, deviceInfo))
            }

            SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSyncTime).post(InterfaceEvent(model, true))
            }
            SET_UTC_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSyncUtcTime).post(InterfaceEvent(model, true))
            }

            RT_STATE -> {
                if (bleResponse.len == 0 || bleResponse.content.size < 7) {
                    LepuBleLog.d(tag, "RT_STATE bleResponse.len == 0")
                    return
                }
                // 主机状态
                LepuBleLog.d(tag, "model:$model,RT_STATE => success")
                val rtState = Bp2BleRtState(bleResponse.content)
                rtStatus.deviceStatus = rtState.status
                rtStatus.deviceStatusMsg = rtState.statusMsg
                rtStatus.batteryStatus = rtState.battery.state
                rtStatus.batteryStatusMsg = rtState.battery.stateMsg
                rtStatus.percent = rtState.battery.percent
                rtStatus.vol = rtState.battery.vol
                rtStatus.avgCnt = rtState.avgCnt
                rtStatus.avgWaitTick = rtState.avgWaitTick
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wRtState).post(InterfaceEvent(model, rtStatus))
            }

            //----------------------------读文件--------------------------
            READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }

                fileContent = null
                fileSize = toUInt(bleResponse.content.copyOfRange(0, 4))
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_START fileSize == $fileSize")
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
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_CONTENT => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }

                curSize += bleResponse.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName READ_FILE_CONTENT curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadingFileProgress).post(InterfaceEvent(model, (part.percent*100).toInt()))
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
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileError).post(InterfaceEvent(model, fileName))
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
                            val data = if (device.name == null) {
                                Bp2BleFile(fileName, it, "")
                            } else {
                                Bp2BleFile(fileName, it, device.name)
                            }
                            when (data.type) {
                                FileType.USER_TYPE -> {
                                    for (u in LeBp2wUserList(data.content).userList) {
                                        val user = UserInfo()
                                        user.aid = u.aid
                                        user.uid = u.uid
                                        user.firstName = u.fName
                                        user.lastName = u.name
                                        user.birthday = u.birthday
                                        user.height = u.height
                                        user.weight = u.weight
                                        user.gender = u.gender
                                        val icon = UserInfo().Icon()
                                        icon.width = u.icon.width
                                        icon.height = u.icon.height
                                        icon.icon = u.icon.icon
                                        user.icon = icon
                                        users.add(user)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wUserFileList).post(InterfaceEvent(model, users))
                                }
                                FileType.BP_TYPE -> {
                                    for (r in LeBp2wBpList(data.content).bpFileList) {
                                        val record = BpRecord()
                                        record.startTime = r.time
                                        record.fileName = r.fileName
                                        record.uid = r.uid
                                        record.measureMode = r.mode
                                        record.interval = r.interval
                                        record.sys = r.sys
                                        record.dia = r.dia
                                        record.mean = r.mean
                                        record.pr = r.pr
                                        record.isIrregular = r.isIrregular
                                        record.isMovement = r.isMovement
                                        bpRecords.add(record)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wBpFileList).post(InterfaceEvent(model, bpRecords))
                                }
                                FileType.ECG_TYPE -> {
                                    for (r in LeBp2wEcgList(data.content).ecgFileList) {
                                        val record = EcgRecord()
                                        record.startIime = r.time
                                        record.fileName = r.fileName
                                        record.uid = r.uid
                                        record.recordingTime = r.recordingTime
                                        record.result = r.result
                                        record.hr = r.hr
                                        record.qrs = r.qrs
                                        record.pvcs = r.pvcs
                                        record.qtc = r.qtc
                                        ecgRecords.add(record)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wEcgFileList).post(InterfaceEvent(model, ecgRecords))
                                }
                            }
                        } else {
                            val data = if (device.name == null) {
                                LeBp2wEcgFile(fileName, it, "")
                            } else {
                                LeBp2wEcgFile(fileName, it, device.name)
                            }
                            ecgFile.fileName = data.fileName
                            ecgFile.fileVersion = data.fileVersion
                            ecgFile.fileType = data.fileType
                            ecgFile.startTime = data.timestamp
                            ecgFile.waveData = data.waveData
                            ecgFile.waveShortData = data.waveShortData
                            ecgFile.duration = data.duration
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileComplete).post(InterfaceEvent(model, ecgFile))
                        }
                    } else {
                        if (fileName.endsWith(".list")) {
                            val data = if (device.name == null) {
                                Bp2BleFile(fileName, byteArrayOf(0, fileType.toByte(), 0, 0, 0, 0, 0, 0, 0, 0), "")
                            } else {
                                Bp2BleFile(fileName, byteArrayOf(0, fileType.toByte(), 0, 0, 0, 0, 0, 0, 0, 0), device.name)
                            }
                            when (data.type) {
                                FileType.USER_TYPE -> {
                                    for (u in LeBp2wUserList(data.content).userList) {
                                        val user = UserInfo()
                                        user.aid = u.aid
                                        user.uid = u.uid
                                        user.firstName = u.fName
                                        user.lastName = u.name
                                        user.birthday = u.birthday
                                        user.height = u.height
                                        user.weight = u.weight
                                        user.gender = u.gender
                                        val icon = UserInfo().Icon()
                                        icon.width = u.icon.width
                                        icon.height = u.icon.height
                                        icon.icon = u.icon.icon
                                        user.icon = icon
                                        users.add(user)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wUserFileList).post(InterfaceEvent(model, users))
                                }
                                FileType.BP_TYPE -> {
                                    for (r in LeBp2wBpList(data.content).bpFileList) {
                                        val record = BpRecord()
                                        record.startTime = r.time
                                        record.fileName = r.fileName
                                        record.uid = r.uid
                                        record.measureMode = r.mode
                                        record.interval = r.interval
                                        record.sys = r.sys
                                        record.dia = r.dia
                                        record.mean = r.mean
                                        record.pr = r.pr
                                        record.isIrregular = r.isIrregular
                                        record.isMovement = r.isMovement
                                        bpRecords.add(record)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wBpFileList).post(InterfaceEvent(model, bpRecords))
                                }
                                FileType.ECG_TYPE -> {
                                    for (r in LeBp2wEcgList(data.content).ecgFileList) {
                                        val record = EcgRecord()
                                        record.startIime = r.time
                                        record.fileName = r.fileName
                                        record.uid = r.uid
                                        record.recordingTime = r.recordingTime
                                        record.result = r.result
                                        record.hr = r.hr
                                        record.qrs = r.qrs
                                        record.pvcs = r.pvcs
                                        record.qtc = r.qtc
                                        ecgRecords.add(record)
                                    }
                                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wEcgFileList).post(InterfaceEvent(model, ecgRecords))
                                }
                            }
                        } else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReadFileError).post(InterfaceEvent(model, fileName))
                        }
                    }
                }
            }

            //--------------------------写文件--------------------------
            WRITE_FILE_START -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileError).post(InterfaceEvent(model, fileName))
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
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileError).post(InterfaceEvent(model, fileName))
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

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WritingFileProgress).post(InterfaceEvent(model, (part.percent*100).toInt()))

            }
            WRITE_FILE_END -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_END => success")
                val crc = FileListCrc(FileType.USER_TYPE, bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WriteFileComplete).post(InterfaceEvent(model, crc.crc))
                curSize = 0
            }

            //实时波形
            RT_DATA -> {
                if (bleResponse.len == 0 || bleResponse.content.size < 32) {
                    LepuBleLog.d(tag, "RT_DATA bleResponse.len == 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,RT_DATA => success")

                val data = Bp2BleRtData(bleResponse.content)
                rtStatus.deviceStatus = data.rtState.status
                rtStatus.deviceStatusMsg = data.rtState.statusMsg
                rtStatus.batteryStatus = data.rtState.battery.state
                rtStatus.batteryStatusMsg = data.rtState.battery.stateMsg
                rtStatus.percent = data.rtState.battery.percent
                rtStatus.vol = data.rtState.battery.vol
                rtStatus.avgCnt = data.rtState.avgCnt
                rtStatus.avgWaitTick = data.rtState.avgWaitTick
                rtData.status = rtStatus
                rtParam.paramDataType = data.rtWave.waveDataType
                rtParam.paramData = data.rtWave.waveData
                rtParam.ecgBytes = data.rtWave.waveform
                rtParam.ecgShorts = data.rtWave.waveShorts
                rtParam.ecgFloats = data.rtWave.waveFloats
                rtData.param = rtParam
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wRtData).post(InterfaceEvent(model, rtData))
            }

            SET_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSetConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                //心跳音开关
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSetConfig).post(InterfaceEvent(model, true))
            }

            GET_CONFIG -> {
                if (bleResponse.len == 0 || bleResponse.content.size < 27) {
                    LepuBleLog.d(tag, "GET_CONFIG bleResponse.len == 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                val data = Bp2Config(bleResponse.content)
                config.isSoundOn = data.beepSwitch
                config.avgMeasureMode = data.avgMeasureMode
                config.volume = data.volume
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetConfig).post(InterfaceEvent(model, config))
            }

            RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wReset).post(InterfaceEvent(model, true))
            }

            FACTORY_RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wFactoryReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wFactoryReset).post(InterfaceEvent(model, true))
            }

            FACTORY_RESET_ALL -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wFactoryResetAll).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wFactoryResetAll).post(InterfaceEvent(model, true))
            }

            SWITCH_STATE ->{
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSwitchState).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SWITCH_STATE => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSwitchState).post(InterfaceEvent(model, true))
            }

            GET_WIFI_ROUTE -> {
                LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                if (bleResponse.pkgType == 0xFF.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WifiScanning).post(InterfaceEvent(model, true))
                } else {
                    val data = Bp2WifiDevice(bleResponse.content)
                    LepuBleLog.d(tag, "model:$model, data.toString == $data")
                    for (w in data.wifiList) {
                        val wifi = LpBp2Wifi()
                        wifi.state = w.state
                        wifi.ssidLen = w.ssidLen
                        wifi.ssid = w.ssid
                        wifi.type = w.type
                        wifi.rssi = w.rssi
                        wifi.pwdLen = w.pwdLen
                        wifi.pwd = w.pwd
                        wifi.macAddr = w.macAddr
                        wifi.ipType = w.ipType
                        wifi.ipLen = w.ipLen
                        wifi.ipAddr = w.ipAddr
                        wifi.netmaskLen = w.netmaskLen
                        wifi.netmaskAddr = w.netmaskAddr
                        wifi.gatewayLen = w.gatewayLen
                        wifi.gatewayAddr = w.gatewayAddr
                        wifiList.add(wifi)
                    }
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2WifiList).post(InterfaceEvent(model, wifiList))
                }
            }

            GET_FILE_LIST -> {
                val data = LeBp2wBleList(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, GET_FILE_LIST $data")
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wList).post(InterfaceEvent(model, data))
            }

            GET_WIFI_CONFIG -> {
                if (bleResponse.len == 0) {
                    LepuBleLog.d(tag, "GET_WIFI_CONFIG bleResponse.len == 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                val data = Bp2WifiConfig(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, data.toString == $data")
                wifi.state = data.wifi.state
                wifi.ssidLen = data.wifi.ssidLen
                wifi.ssid = data.wifi.ssid
                wifi.type = data.wifi.type
                wifi.rssi = data.wifi.rssi
                wifi.pwdLen = data.wifi.pwdLen
                wifi.pwd = data.wifi.pwd
                wifi.macAddr = data.wifi.macAddr
                wifi.ipType = data.wifi.ipType
                wifi.ipLen = data.wifi.ipLen
                wifi.ipAddr = data.wifi.ipAddr
                wifi.netmaskLen = data.wifi.netmaskLen
                wifi.netmaskAddr = data.wifi.netmaskAddr
                wifi.gatewayLen = data.wifi.gatewayLen
                wifi.gatewayAddr = data.wifi.gatewayAddr
                wifiConfig.wifi = wifi
                server.state = data.server.state
                server.addrType = data.server.addrType
                server.addrLen = data.server.addrLen
                server.addr = data.server.addr
                server.port = data.server.port
                wifiConfig.server = server
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetWifiConfig).post(InterfaceEvent(model, wifiConfig))
            }

            SET_WIFI_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSetWifiConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wSetWifiConfig).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
            }

            GET_ECG_LIST_CRC -> {
                if (bleResponse.len == 0) {
                    LepuBleLog.d(tag, "GET_ECG_LIST_CRC bleResponse.len == 0")
                    return
                }
                val crc = FileListCrc(FileType.ECG_TYPE, bleResponse.content)
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_ECG_LIST_CRC => success")
            }
            GET_BP_LIST_CRC -> {
                if (bleResponse.len == 0) {
                    LepuBleLog.d(tag, "GET_BP_LIST_CRC bleResponse.len == 0")
                    return
                }
                val crc = FileListCrc(FileType.BP_TYPE, bleResponse.content)
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_BP_LIST_CRC => success")
            }
            GET_USER_LIST_CRC -> {
                if (bleResponse.len == 0) {
                    LepuBleLog.d(tag, "GET_USER_LIST_CRC bleResponse.len == 0")
                    return
                }
                val crc = FileListCrc(FileType.USER_TYPE, bleResponse.content)
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_USER_LIST_CRC => success")
            }
            DELETE_FILE -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wDeleteFile).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,DELETE_FILE => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LpBp2w.EventLpBp2wDeleteFile).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,DELETE_FILE => success")
            }

        }
    }

    override fun getInfo() {
        sendCmd(LeBp2wBleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(setUtcTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    fun syncUtcTime() {
        sendCmd(setUtcTime())
        LepuBleLog.d(tag, "syncUtcTime..." + bytesToHex(setUtcTime()))
    }

    fun deleteFile() {
        sendCmd(LeBp2wBleCmd.deleteFile())
        LepuBleLog.d(tag, "deleteFile...")
    }

    //实时波形命令
    override fun getRtData() {
        sendCmd(LeBp2wBleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData...")
    }

    fun setConfig(c: LpBp2wConfig) {
        val config = Bp2Config()
        config.beepSwitch = c.isSoundOn
        config.avgMeasureMode = c.avgMeasureMode
        config.volume = c.volume
        sendCmd(setConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setConfig...config:$config")
    }
     fun getConfig() {
         sendCmd(LeBp2wBleCmd.getConfig())
         LepuBleLog.d(tag, "getConfig...")
    }

    override fun getFileList() {
        sendCmd(LeBp2wBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

    fun getFileListCrc(fileType: Int) {
        sendCmd(LeBp2wBleCmd.getFileListCrc(fileType))
        LepuBleLog.d(tag, "getFileListCrc...fileType:$fileType")
    }

    fun getFileList(fileType: Int) {
        this.fileType = fileType
        when (fileType) {
            FileType.ECG_TYPE -> {
                ecgRecords.clear()
                fileName = "bp2ecg.list"
            }
            FileType.BP_TYPE -> {
                bpRecords.clear()
                fileName = "bp2nibp.list"
            }
            FileType.USER_TYPE -> {
                users.clear()
                fileName = "user.list"
            }
        }
        sendCmd(readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "getFileList... fileName == $fileName, fileType:$fileType")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile... fileName == $fileName, userId:$userId")
    }

    fun writeUserList(userList: LeBp2wUserList) {
        this.userList = userList
        fileSize = userList.getDataBytes().size
        fileName = "user.list"
        sendCmd(writeFileStart(fileName.toByteArray(), 0, fileSize))
        LepuBleLog.d(tag, "writeUserList... fileName == $fileName, fileSize == $fileSize")
    }

    fun writeUserList(users: ArrayList<UserInfo>) {
        val userList = LeBp2wUserList()
        for (u in users) {
            val user = LeBp2wUserInfo()
            user.aid = u.aid
            user.uid = u.uid
            user.fName = u.firstName
            user.name = u.lastName
            user.birthday = u.birthday
            user.height = u.height
            user.weight = u.weight
            user.gender = u.gender
            val icon = LeBp2wUserInfo.Icon()
            icon.width = u.icon.width
            icon.height = u.icon.height
            icon.icon = u.icon.icon
            user.icon = icon
            userList.userList.add(user)
        }
        this.userList = userList
        fileSize = userList.getDataBytes().size
        fileName = "user.list"
        sendCmd(writeFileStart(fileName.toByteArray(), 0, fileSize))
        LepuBleLog.d(tag, "writeUserList... fileName == $fileName, fileSize == $fileSize")
    }

    override fun reset() {
        sendCmd(LeBp2wBleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }
    override fun factoryReset() {
        sendCmd(LeBp2wBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }
    override fun factoryResetAll() {
        sendCmd(LeBp2wBleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
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
        sendCmd(LeBp2wBleCmd.switchState(state))
        LepuBleLog.e("enter  switchState： SWITCH_STATE===$state")
    }

    /**
     * 获取wifi路由
     */
    fun getWifiDevice() {
        wifiList.clear()
        sendCmd(getWifiRoute(0))
        LepuBleLog.d(tag, "getWifiDevice...")
    }

    /**
     * 配置要连接的wifi
     */
    fun setWifiConfig(config: Bp2WifiConfig) {
        sendCmd(setWifiConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }
    fun setWifiConfig(c: LpBp2WifiConfig) {
        val config = Bp2WifiConfig()
        config.option = 3
        val wifi = Bp2Wifi()
        wifi.state = c.wifi.state
        wifi.ssidLen = c.wifi.ssidLen
        wifi.ssid = c.wifi.ssid
        wifi.type = c.wifi.type
        wifi.rssi = c.wifi.rssi
        wifi.pwdLen = c.wifi.pwdLen
        wifi.pwd = c.wifi.pwd
        wifi.macAddr = c.wifi.macAddr
        wifi.ipType = c.wifi.ipType
        wifi.ipLen = c.wifi.ipLen
        wifi.ipAddr = c.wifi.ipAddr
        wifi.netmaskLen = c.wifi.netmaskLen
        wifi.netmaskAddr = c.wifi.netmaskAddr
        wifi.gatewayLen = c.wifi.gatewayLen
        wifi.gatewayAddr = c.wifi.gatewayAddr
        config.wifi = wifi
        val server = Bp2Server()
        server.state = c.server.state
        server.addrType = c.server.addrType
        server.addrLen = c.server.addrLen
        server.addr = c.server.addr
        server.port = c.server.port
        config.server = server
        sendCmd(setWifiConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }

    /**
     * 获取当前配置的wifi
     */
    fun getWifiConfig() {
        sendCmd(LeBp2wBleCmd.getWifiConfig())
        LepuBleLog.d(tag, "getWifiConfig...")
    }

    fun getRtState() {
        sendCmd(LeBp2wBleCmd.getRtState())
        LepuBleLog.d(tag, "getRtState...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

}