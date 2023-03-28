package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.ble.data.r20.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * 呼吸机：
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
class R20BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "R20BleInterface"

    private var userId = ""
    private var fileName = ""
    private var fileSize = 0
    private var fileContent = ByteArray(0)

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
                val bleResponse = R20BleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: R20BleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
            val data = ResponseError()
            data.model = model
            data.cmd = response.cmd
            data.type = response.pkgType
            LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError).post(data)
            LepuBleLog.d(tag, "model:$model,ResponseError => $data")
        } else {
            when(response.cmd) {
                LpBleCmd.ECHO -> {
                    LepuBleLog.d(tag, "model:$model,ECHO => success ${bytesToHex(response.content)}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20EchoData).post(InterfaceEvent(model, response))
                }
                LpBleCmd.SET_UTC_TIME -> {
                    LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetUtcTime).post(InterfaceEvent(model, true))
                }
                LpBleCmd.GET_INFO -> {
                    if (response.len < 38) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => response.len < 38")
                        return
                    }
                    val data = LepuDevice(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetInfo).post(InterfaceEvent(model, data))
                }
                LpBleCmd.RESET -> {
                    LepuBleLog.d(tag, "model:$model,RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20Reset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.FACTORY_RESET -> {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20FactoryReset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.GET_BATTERY -> {
                    if (response.len < 4) {
                        LepuBleLog.d(tag, "model:$model,GET_BATTERY => response.len < 4")
                        return
                    }
                    val data = KtBleBattery(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_BATTERY => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetBattery).post(InterfaceEvent(model, data))
                }
                LpBleCmd.BURN_FACTORY_INFO -> {
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20BurnFactoryInfo).post(InterfaceEvent(model, true))
                }
                LpBleCmd.ENCRYPT -> {
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => success")
                }
                R20BleCmd.DEVICE_BOUND -> {
                    if (response.content.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => response.content.isEmpty()")
                        return
                    }
                    // 0x00成功, 0x01失败, 0x02超时
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20DeviceBound).post(InterfaceEvent(model, data))
                }
                R20BleCmd.DEVICE_UNBOUND -> {
                    LepuBleLog.d(tag, "model:$model,DEVICE_UNBOUND => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20DeviceUnBound).post(InterfaceEvent(model, true))
                }
                R20BleCmd.SET_USER_INFO -> {
                    LepuBleLog.d(tag, "model:$model,SET_USER_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetUserInfo).post(InterfaceEvent(model, true))
                }
                R20BleCmd.GET_USER_INFO -> {
                    if (response.len < 81) {
                        LepuBleLog.d(tag, "model:$model,GET_USER_INFO => response.len < 81")
                        return
                    }
                    val data = UserInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_USER_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetUserInfo).post(InterfaceEvent(model, data))
                }
                R20BleCmd.INTO_DOCTOR_MODE -> {
                    if (response.len < 2) {
                        LepuBleLog.d(tag, "model:$model,INTO_DOCTOR_MODE => response.len < 2")
                        return
                    }
                    val data = R20BleResponse.DoctorModeResult(response.content)
                    LepuBleLog.d(tag, "model:$model,INTO_DOCTOR_MODE => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20IntoDoctorMode).post(InterfaceEvent(model, data))
                }
                R20BleCmd.GET_WIFI_LIST -> {
                    if (response.content.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => response.content.isEmpty()")
                        return
                    }
                    val data = WifiList(response.bytes)
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWifiList).post(InterfaceEvent(model, data))
                }
                R20BleCmd.SET_WIFI_CONFIG -> {
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetWifiConfig).post(InterfaceEvent(model, true))
                }
                R20BleCmd.GET_WIFI_CONFIG -> {
                    if (response.content.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => response.content.isEmpty()")
                        return
                    }
                    val data = WifiConfig(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWifiConfig).post(InterfaceEvent(model, data))
                }
                R20BleCmd.GET_VERSION_INFO -> {
                    if (response.content.size < 17) {
                        LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => response.content.size < 17")
                        return
                    }
                    val data = R20BleResponse.VersionInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetVersionInfo).post(InterfaceEvent(model, data))
                }
                R20BleCmd.GET_SYSTEM_SETTING -> {
                    if (response.len < 9) {
                        LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => response.len < 9")
                        return
                    }
                    val data = SystemSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetSystemSetting).post(InterfaceEvent(model, data))
                }
                R20BleCmd.SET_SYSTEM_SETTING -> {
                    LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetSystemSetting).post(InterfaceEvent(model, true))
                }
                R20BleCmd.GET_MEASURE_SETTING -> {
                    if (response.len < 9) {
                        LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => response.len < 9")
                        return
                    }
                    val data = MeasureSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetMeasureSetting).post(InterfaceEvent(model, data))
                }
                R20BleCmd.SET_MEASURE_SETTING -> {
                    LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetMeasureSetting).post(InterfaceEvent(model, true))
                }
                R20BleCmd.MASK_TEST -> {
                    if (response.len < 3) {
                        LepuBleLog.d(tag, "model:$model,MASK_TEST => response.len < 3")
                        return
                    }
                    val data = R20BleResponse.MaskTestResult(response.content)
                    LepuBleLog.d(tag, "model:$model,MASK_TEST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20MaskTest).post(InterfaceEvent(model, data))
                }
                R20BleCmd.GET_VENTILATION_SETTING -> {
                    if (response.len < 10) {
                        LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => response.len < 10")
                        return
                    }
                    val data = VentilationSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetVentilationSetting).post(InterfaceEvent(model, data))
                }
                R20BleCmd.SET_VENTILATION_SETTING -> {
                    LepuBleLog.d(tag, "model:$model,SET_VENTILATION_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetVentilationSetting).post(InterfaceEvent(model, true))
                }
                R20BleCmd.GET_WARNING_SETTING -> {
                    if (response.len < 9) {
                        LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => response.len < 9")
                        return
                    }
                    val data = WarningSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWarningSetting).post(InterfaceEvent(model, data))
                }
                R20BleCmd.SET_WARNING_SETTING -> {
                    LepuBleLog.d(tag, "model:$model,SET_WARNING_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetWarningSetting).post(InterfaceEvent(model, true))
                }
                R20BleCmd.GET_FILE_LIST -> {
                    if (response.len < 12) {
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => response.len < 12")
                        return
                    }
                    val data = R20BleResponse.RecordList(response.content)
                    LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetFileList).post(InterfaceEvent(model, data))
                }
                R20BleCmd.READ_FILE_START -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(R20BleCmd.readFileEnd())
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
                        DownloadHelper.readFile(model, userId, fileName)
                    }
                    offset = fileContent.size
                    fileSize = toUInt(response.content)
                    if (fileSize <= 0) {
                        sendCmd(R20BleCmd.readFileEnd())
                    } else {
                        sendCmd(R20BleCmd.readFileData(offset))
                    }
                }
                R20BleCmd.READ_FILE_DATA -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(R20BleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    offset += response.len
                    fileContent = fileContent.plus(response.content)
                    LepuBleLog.d(tag, "READ_FILE_DATA offset: $offset, fileSize: $fileSize")
                    val percent = offset.times(100).div(fileSize)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadingFileProgress).post(InterfaceEvent(model, percent))
                    if (offset < fileSize) {
                        sendCmd(R20BleCmd.readFileData(offset))
                    } else {
                        sendCmd(R20BleCmd.readFileEnd())
                    }
                }
                R20BleCmd.READ_FILE_END -> {
                    if (isCancelRF || isPausedRF) {
                        LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                        return
                    }
                    if (fileContent.size < fileSize) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadFileError).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadFileComplete).post(InterfaceEvent(model, fileContent))
                    }
                }
            }
        }
    }

    /**
     * get device info
     */
    override fun getInfo() {
        LpBleCmd.getInfo()
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        LpBleCmd.setUtcTime()
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        LpBleCmd.reset()
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        LpBleCmd.factoryReset()
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    /**
     * get real-time data
     */
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.fileName = fileName
        LepuBleLog.d(tag, "dealReadFile...")
        sendCmd(R20BleCmd.readFileStart(fileName.toByteArray(), 0))
    }

    /**
     * get file list
     */
    override fun getFileList() {
        LepuBleLog.d(tag, "getFileList...")
    }
    fun getFileList(recordType: Int ,startTime: Long) {
        sendCmd(R20BleCmd.getFileList(startTime, recordType))
        LepuBleLog.d(tag, "getFileList...startTime: $startTime, recordType: $recordType")
    }
    fun echo(data: ByteArray) {
        sendCmd(LpBleCmd.echo(data))
        LepuBleLog.d(tag, "echo...")
    }
    fun getBattery() {
        sendCmd(LpBleCmd.getBattery())
        LepuBleLog.d(tag, "getBattery...")
    }
    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LpBleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...config: $config")
    }
    // 密钥交换
    fun encrypt() {
        sendCmd(LpBleCmd.encrypt(ByteArray(0)))
    }
    // 绑定/解绑
    fun deviceBound(bound: Boolean) {
        sendCmd(R20BleCmd.deviceBound(bound))
    }
    // 设置用户信息
    fun setUserInfo(data: UserInfo) {
        sendCmd(R20BleCmd.setUserInfo(data.getDataBytes()))
    }
    // 获取用户信息
    fun getUserInfo() {
        sendCmd(R20BleCmd.getUserInfo())
    }
    // 进入医生模式
    fun intoDoctorMode(pin: String, timestamp: Long) {
        sendCmd(R20BleCmd.intoDoctorMode(pin.toByteArray(), timestamp))
    }
    // 搜索WiFi列表
    fun getWifiList() {
        sendCmd(R20BleCmd.getWifiList())
    }
    // 配置WiFi信息
    fun setWifiConfig(data: WifiConfig) {
        sendCmd(R20BleCmd.setWifiConfig(data.getDataBytes()))
    }
    // 获取WiFi信息
    fun getWifiConfig() {
        sendCmd(R20BleCmd.getWifiConfig())
    }
    // 获取详细版本信息
    fun getVersionInfo() {
        sendCmd(R20BleCmd.getVersionInfo())
    }
    // 获取系统设置
    fun getSystemSetting() {
        sendCmd(R20BleCmd.getSystemSetting())
    }
    // 配置系统设置
    fun setSystemSetting(data: SystemSetting) {
        sendCmd(R20BleCmd.setSystemSetting(data.getDataBytes()))
    }
    // 获取测量设置
    fun getMeasureSetting() {
        sendCmd(R20BleCmd.getMeasureSetting())
    }
    // 配置测量设置
    fun setMeasureSetting(data: MeasureSetting) {
        sendCmd(R20BleCmd.setMeasureSetting(data.getDataBytes()))
    }
    // 佩戴测试
    fun maskTest(start: Boolean) {
        sendCmd(R20BleCmd.maskTest(start))
    }
    // 获取通气控制参数
    fun getVentilationSetting() {
        sendCmd(R20BleCmd.getVentilationSetting())
    }
    // 配置通气控制参数
    fun setVentilationSetting(data: VentilationSetting) {
        sendCmd(R20BleCmd.setVentilationSetting(data.getDataBytes()))
    }
    // 获取报警提示参数
    fun getWarningSetting() {
        sendCmd(R20BleCmd.getWarningSetting())
    }
    // 配置报警提示参数
    fun setWarningSetting(data: WarningSetting) {
        sendCmd(R20BleCmd.setWarningSetting(data.getDataBytes()))
    }
    // 启动/停止通气
    fun ventilationSwitch(start: Boolean) {
        sendCmd(R20BleCmd.ventilationSwitch(start))
    }
}