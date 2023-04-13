package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.Bp2Config
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * bp2w心电血压计：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电、血压
 * 4.获取/配置参数
 * 5.获取/设置设备状态
 * 6.获取文件列表
 * 7.下载文件内容
 * 8.恢复出厂设置
 * 9.获取路由
 * 10.获取/配置WiFi
 * 心电采样率：实时250HZ，存储125HZ
 * 血压采样率：实时50HZ，存储50HZ
 * 心电增益：n * 0.003098-----322.7888960619755倍
 */
class Bp3BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bp3BleInterface"

    private var fileName = ""
    private var fileSize: Int = 0
    private var curSize: Int = 0
    private var fileContent = ByteArray(0)
    private var userList: LeBp2wUserList? = null
    private var chunkSize: Int = 200  // 每次写文件大小

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
            if (temp.size < 7) {
                continue@loop
            }
            if (temp.last() == calCRC8(temp)) {
                val bleResponse = LepuBleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)


                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }


    private fun onResponseReceived(response: LepuBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes : ${bytesToHex(response.bytes)}")
        if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
            val data = ResponseError()
            data.model = model
            data.cmd = response.cmd
            data.type = response.pkgType
            LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError).post(data)
            LepuBleLog.d(tag, "model:$model,ResponseError => $data")
        } else {
            when (response.cmd) {
                LpBleCmd.GET_INFO -> {
                    if (response.len < 38) {
                        LepuBleLog.d(tag, "GET_INFO response.len < 38")
                        return
                    }
                    val data = LepuDevice(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_INFO => success data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetInfo).post(InterfaceEvent(model, data))
                }
                LpBleCmd.SET_UTC_TIME -> {
                    //同步时间
                    LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetUtcTime).post(InterfaceEvent(model, true))
                }
                LpBleCmd.RESET -> {
                    LepuBleLog.d(tag, "model:$model,RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3Reset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.FACTORY_RESET -> {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryReset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.FACTORY_RESET_ALL -> {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryResetAll).post(InterfaceEvent(model, true))
                }
                LpBleCmd.GET_BATTERY -> {
                    if (response.len < 4) {
                        LepuBleLog.d(tag, "model:$model,GET_BATTERY => response.len < 4")
                        return
                    }
                    val data = KtBleBattery(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_BATTERY => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetBattery).post(InterfaceEvent(model, data))
                }
                LpBleCmd.BURN_FACTORY_INFO -> {
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3BurnFactoryInfo).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.GET_CONFIG -> {
                    if (response.len < 35) {
                        LepuBleLog.d(tag, "model:$model,GET_CONFIG => response.len < 35")
                        return
                    }
                    val data = Bp2Config(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_CONFIG => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetConfig).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.CALIBRATION_ZERO -> {
                    if (response.len < 4) {
                        LepuBleLog.d(tag, "model:$model,CALIBRATION_ZERO => response.len < 4")
                        return
                    }
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,CALIBRATION_ZERO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3CalibrationZero).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.CALIBRATION_SLOPE -> {
                    if (response.len < 4) {
                        LepuBleLog.d(tag, "model:$model,CALIBRATION_SLOPE => response.len < 4")
                        return
                    }
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,CALIBRATION_SLOPE => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3CalibrationSlope).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.RT_PRESSURE -> {
                    if (response.len < 2) {
                        LepuBleLog.d(tag, "model:$model,RT_PRESSURE => response.len < 2")
                        return
                    }
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,RT_PRESSURE => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtPressure).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.RT_WAVE -> {
                    if (response.len < 23) {
                        LepuBleLog.d(tag, "model:$model,RT_WAVE => response.len < 23")
                        return
                    }
                    val data = Bp2BleRtWave(response.content)
                    LepuBleLog.d(tag, "model:$model,RT_WAVE => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtWave).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.RT_DATA -> {
                    if (response.len < 32) {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => response.len < 32")
                        return
                    }
                    val data = Bp2BleRtData(response.content)
                    LepuBleLog.d(tag, "model:$model,RT_DATA => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtData).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.PRESSURE_TEST -> {
                    LepuBleLog.d(tag, "model:$model,PRESSURE_TEST => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3PressureTest).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.SET_CONFIG -> {
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetConfig).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.SWITCH_VALVE -> {
                    LepuBleLog.d(tag, "model:$model,SWITCH_VALVE => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchValve).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.CURRENT_PRESSURE -> {
                    if (response.len < 2) {
                        LepuBleLog.d(tag, "model:$model,CURRENT_PRESSURE => response.len < 32")
                        return
                    }
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,CURRENT_PRESSURE => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3CurPressure).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.SWITCH_TEST_MODE -> {
                    LepuBleLog.d(tag, "model:$model,SWITCH_TEST_MODE => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchTestMode).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.SWITCH_BP_UNIT -> {
                    LepuBleLog.d(tag, "model:$model,SWITCH_BP_UNIT => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchBpUnit).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.GET_WIFI_LIST -> {
                    if (response.len == 0) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => response.len == 0")
                        return
                    }
                    val data = Bp2WifiDevice(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetWifiList).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.SET_WIFI_CONFIG -> {
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetWifiConfig).post(InterfaceEvent(model, true))
                }
                Bp3BleCmd.GET_WIFI_CONFIG -> {
                    if (response.len == 0) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => response.len == 0")
                        return
                    }
                    val data = Bp2WifiConfig(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetWifiConfig).post(InterfaceEvent(model, data))
                }
                Bp3BleCmd.SWITCH_WIFI_4G -> {
                    LepuBleLog.d(tag, "model:$model,SWITCH_WIFI_4G => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchWifi4g).post(InterfaceEvent(model, true))
                }
                //--------------------------写文件--------------------------
                LpBleCmd.WRITE_FILE_START -> {
                    //检查返回是否异常
                    LepuBleLog.d(tag, "model:$model,WRITE_FILE_START => success")
                    if (fileSize <= 0) {
                        sendCmd(LpBleCmd.writeFileEnd())
                    } else {
                        curSize = if (fileSize < chunkSize) { sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(0, fileSize)))
                            fileSize
                        } else {
                            sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(0, chunkSize)))
                            chunkSize
                        }
                    }
                }
                LpBleCmd.WRITE_FILE_DATA -> {
                    //检查返回是否异常
                    LepuBleLog.d(tag, "model:$model,WRITE_FILE_DATA => success")
                    if (curSize < fileSize) {
                        if (fileSize - curSize < chunkSize) {
                            sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, fileSize)))
                            curSize = fileSize
                        } else {
                            sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, curSize + chunkSize)))
                            curSize += chunkSize
                        }

                    } else {
                        sendCmd(LpBleCmd.writeFileEnd())
                    }
                    val percent = curSize*100/fileSize
                    LepuBleLog.d(tag, "write file $fileName WRITE_FILE_DATA curSize == $curSize | fileSize == $fileSize")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WritingFileProgress).post(InterfaceEvent(model, percent))
                }
                LpBleCmd.WRITE_FILE_END -> {
                    //检查返回是否异常
                    LepuBleLog.d(tag, "model:$model, WRITE_FILE_END => success")
                    val crc = toUInt(response.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WriteFileComplete).post(InterfaceEvent(model, crc))
                    curSize = 0
                }
                LpBleCmd.GET_FILE_LIST -> {
                    if (response.len < 1) {
                        LepuBleLog.d(tag, "model:$model,READ_FILE_START => response.len < 1")
                        return
                    }
                    val data = if (device.name == null) {
                        KtBleFileList(response.content, "")
                    } else {
                        KtBleFileList(response.content, device.name)
                    }
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetFileList).post(InterfaceEvent(model, data))
                }
                LpBleCmd.READ_FILE_START -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(LpBleCmd.readFileEnd())
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
                        sendCmd(LpBleCmd.readFileEnd())
                    } else {
                        sendCmd(LpBleCmd.readFileData(offset))
                    }
                    LepuBleLog.d(tag, "model:$model,READ_FILE_START => fileName: $fileName, offset: $offset, fileSize: $fileSize")
                }
                LpBleCmd.READ_FILE_DATA -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(LpBleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    offset += response.len
                    DownloadHelper.writeFile(model, "", fileName, "dat", response.content)
                    fileContent = add(fileContent, response.content)
                    val percent = offset*100/fileSize
                    LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => offset: $offset, fileSize: $fileSize")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadingFileProgress).post(InterfaceEvent(model, percent))
                    if (offset < fileSize) {
                        sendCmd(LpBleCmd.readFileData(offset))
                    } else {
                        sendCmd(LpBleCmd.readFileEnd())
                    }
                }
                LpBleCmd.READ_FILE_END -> {
                    if (isCancelRF || isPausedRF) {
                        LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                        return
                    }
                    if (fileContent.size < fileSize) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileError).post(InterfaceEvent(model, true))
                    } else {
                        val data = LeBp2wUserList(fileContent)
                        LepuBleLog.d(tag, "model:$model,READ_FILE_END => data: $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileComplete).post(InterfaceEvent(model, fileContent))
                    }
                }
            }
        }

    }
    override fun getInfo() {
        sendCmd(LpBleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo...")
    }
    override fun syncTime() {
        sendCmd(LpBleCmd.setUtcTime())
        LepuBleLog.d(tag, "syncTime...")
    }
    override fun getRtData() {
        sendCmd(Bp3BleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData ...")
    }
    override fun getFileList() {
        sendCmd(LpBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }
    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(LpBleCmd.readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile... userId:$userId, fileName == $fileName")
    }
    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }
    override fun reset() {
        sendCmd(LpBleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }
    override fun factoryReset() {
        sendCmd(LpBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }
    override fun factoryResetAll() {
        sendCmd(LpBleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
    }
    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LpBleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...")
    }
    fun getBattery(){
        sendCmd(LpBleCmd.getBattery())
        LepuBleLog.d(tag, "getBattery...")
    }
    fun setConfig(config: Bp2Config){
        sendCmd(Bp3BleCmd.setConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setConfig...$config")
    }
    fun getConfig(){
         sendCmd(Bp3BleCmd.getConfig())
         LepuBleLog.d(tag, "getConfig...")
    }
    fun calibrationZero() {
        sendCmd(Bp3BleCmd.calibrationZero())
        LepuBleLog.d(tag, "calibrationZero...")
    }
    fun calibrationSlope(pressure: Int) {
        sendCmd(Bp3BleCmd.calibrationSlope(pressure))
        LepuBleLog.d(tag, "calibrationSlope...")
    }
    fun getRtPressure(rate: Int) {
        sendCmd(Bp3BleCmd.getRtPressure(rate))
        LepuBleLog.d(tag, "getRtPressure...")
    }
    fun getRtWave() {
        sendCmd(Bp3BleCmd.getRtWave())
        LepuBleLog.d(tag, "getRtWave...")
    }
    fun pressureTest(pressure: Int) {
        sendCmd(Bp3BleCmd.pressureTest(pressure))
        LepuBleLog.d(tag, "pressureTest...")
    }
    fun switchValve(on: Boolean) {
        sendCmd(Bp3BleCmd.switchValve(on))
        LepuBleLog.d(tag, "switchValve...")
    }
    fun getCurPressure() {
        sendCmd(Bp3BleCmd.getCurPressure())
        LepuBleLog.d(tag, "getCurPressure...")
    }
    fun switchTestMode(mode: Int) {
        sendCmd(Bp3BleCmd.switchTestMode(mode))
        LepuBleLog.d(tag, "switchTestMode...")
    }
    fun switchBpUnit(unit: Int) {
        sendCmd(Bp3BleCmd.switchBpUnit(unit))
        LepuBleLog.d(tag, "switchBpUnit...")
    }
    fun getWifiList(deviceNum: Int) {
        sendCmd(Bp3BleCmd.getWifiList(deviceNum))
        LepuBleLog.d(tag, "getWifiList...")
    }
    fun setWifiConfig(config: Bp2WifiConfig) {
        sendCmd(Bp3BleCmd.setWifiConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }
    fun getWifiConfig(option: Int) {
        sendCmd(Bp3BleCmd.getWifiConfig(option))
        LepuBleLog.d(tag, "getWifiConfig...")
    }
    fun switchWifi4g(on: Boolean) {
        sendCmd(Bp3BleCmd.switchWifi4g(on))
        LepuBleLog.d(tag, "switchWifi4g...")
    }
    fun writeUserList(userList: LeBp2wUserList) {
        this.userList = userList
        fileSize = userList.getDataBytes().size
        fileName = "user.list"
        sendCmd(LpBleCmd.writeFileStart(fileName.toByteArray(), 0, fileSize))
        LepuBleLog.d(tag, "writeUserList... fileName == $fileName, fileSize == $fileSize")
    }
}