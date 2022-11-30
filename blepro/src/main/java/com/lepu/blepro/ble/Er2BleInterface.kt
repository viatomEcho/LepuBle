package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.cmd.Er2File
import com.lepu.blepro.ble.data.Er2DeviceInfo
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.er2.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * er2心电宝：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电
 * 4.获取列表
 * 5.下载文件内容
 * 6.恢复出厂设置
 * 7.获取/配置参数
 * 8.烧录
 * 心电采样率：实时125HZ，存储125HZ
 * 心电增益：n * (1.0035 * 1800) / (4096 * 178.74) = n * 0.0024672217239426-----405.3142002989537倍
 */
class Er2BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Er2BleInterface"

    private var deviceInfo = DeviceInfo()
    private var config = Er2Config()
    private var file = com.lepu.blepro.ext.er2.Er2File()
    private var deviceRtData = RtData()
    private var deviceRtParam = RtParam()
    private var deviceRtWave = RtWave()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Er2BleManager(context)
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

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    private var userId: String? = null
    var curFile: Er2File? = null

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val respPkg = Er2BleResponse(temp)
                onResponseReceived(respPkg)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(
                    i + 8 + len,
                    bytes.size
                )

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    private fun onResponseReceived(respPkg: Er2BleResponse) {
        when(respPkg.cmd) {
            Er2BleCmd.CMD_RETRIEVE_DEVICE_INFO -> {
                val info = if (device.name == null) {
                    Er2DeviceInfo("", device.address, respPkg.data)
                } else {
                    Er2DeviceInfo(device.name, device.address, respPkg.data)
                }

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")

                deviceInfo.hwVersion = info.hwVersion
                deviceInfo.swVersion = info.fwVersion
                deviceInfo.branchCode = info.branchCode
                deviceInfo.spcpVer = info.protocolVersion
                deviceInfo.btlVersion = info.blVersion
                deviceInfo.snLen = byte2UInt(info.snLength)
                deviceInfo.sn = info.serialNum

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info).post(InterfaceEvent(model, deviceInfo))
                // 本版本注释，测试通过后删除
                /*if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }*/
            }
            Er2BleCmd.CMD_SET_TIME -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime).post(InterfaceEvent(model, true))
            }
            Er2BleCmd.CMD_SET_SWITCHER_STATE -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_SWITCHER_STATE => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig).post(InterfaceEvent(model, false))
                }
            }
            Er2BleCmd.CMD_RETRIEVE_SWITCHER_STATE -> {

                if (respPkg.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,CMD_RETRIEVE_SWITCHER_STATE => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfigError).post(InterfaceEvent(model, true))
                    return
                }

                LepuBleLog.d(tag, "model:$model,CMD_RETRIEVE_SWITCHER_STATE => success")

                val data = SwitcherConfig.parse(respPkg.data)
                config.isSoundOn = data.switcher
                config.motionCount = data.motionCount
                config.motionWindows = data.motionWindows
                config.vector = data.vector

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfig).post(InterfaceEvent(model, config))
            }
            Er2BleCmd.CMD_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Reset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Reset).post(InterfaceEvent(model, false))
                }
            }
            Er2BleCmd.CMD_FACTORY_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset).post(InterfaceEvent(model, false))
                }
            }
            Er2BleCmd.CMD_FACTORY_RESET_ALL -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryResetAll).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryResetAll).post(InterfaceEvent(model, false))
                }
            }
            Er2BleCmd.CMD_GET_REAL_TIME_DATA -> {

                val rtData = Er2RtData(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_GET_REAL_TIME_DATA => success")

                deviceRtParam.hr = rtData.rtParam.hr
                deviceRtParam.battery = byte2UInt(rtData.rtParam.percent)
                deviceRtParam.batteryState = rtData.rtParam.batteryState
                deviceRtParam.recordTime = rtData.rtParam.recordTime
                deviceRtParam.curStatus = rtData.rtParam.currentState
                deviceRtParam.isRSignal = rtData.rtParam.isrFlag()
                deviceRtParam.isPoorSignal = rtData.rtParam.isSignalPoor
                deviceRtData.param = deviceRtParam
                deviceRtWave.ecgBytes = rtData.waveData.bytes
                deviceRtWave.ecgShorts = rtData.waveData.dataShorts
                deviceRtWave.ecgFloats = rtData.waveData.datas
                deviceRtData.wave = deviceRtWave

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData).post(InterfaceEvent(model, deviceRtData))
            }
            Er2BleCmd.CMD_LIST_FILE -> {
                val fileArray = Er2FileList(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_LIST_FILE => success")

                val data = arrayListOf<String>()
                for (da in fileArray.fileNames) {
                    data.add(da)
                }

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).post(InterfaceEvent(model, data))
            }
            Er2BleCmd.CMD_START_READ_FILE -> {

                LepuBleLog.d(tag, "model:$model,CMD_START_READ_FILE => success, $respPkg")
                if (respPkg.pkgType == 0x01.toByte()) {
                    curFile = curFileName?.let {
                        Er2File(model, it, toUInt(respPkg.data), userId!!)
                    }
                    sendCmd(Er2BleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${respPkg.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError).post(InterfaceEvent(model, true))
                }

            }
            Er2BleCmd.CMD_READ_FILE_CONTENT -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "CMD_READ_FILE_CONTENT: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Er1BleCmd.readFileEnd())
                        LepuBleLog.d(tag, "CMD_READ_FILE_CONTENT isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                        return
                    }

                    this.addContent(respPkg.data)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 100).div(this.fileSize) }")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 100).div(this.fileSize) ))

                    if (this.index < this.fileSize) {
                        sendCmd(Er2BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(Er2BleCmd.readFileEnd())
                    }
                }
            }
            Er2BleCmd.CMD_END_READ_FILE -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curFileName = null// 一定要放在发通知之前
                curFile?.let {
                    if (it.index < it.fileSize){
                        if ((isCancelRF || isPausedRF)) {
                            LepuBleLog.d(tag, "CMD_END_READ_FILE isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                            return
                        }
                    }else {
                        file.fileName = it.fileName
                        file.content = it.content
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete).post(InterfaceEvent(model, file))
                    }
                }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                curFile = null
            }
            Er2BleCmd.CMD_BURN_SN_CODE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BURN_SN_CODE => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnFactoryInfo).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnFactoryInfo).post(InterfaceEvent(model, false))
                }
            }
            Er2BleCmd.CMD_LOCK_FLASH -> {
                LepuBleLog.d(tag, "model:$model,CMD_LOCK_FLASH => success")
                if (respPkg.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnLockFlash).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnLockFlash).post(InterfaceEvent(model, false))
                }
            }
        }
    }
    fun setSwitcherState(hrFlag: Boolean) {
        sendCmd(Er2BleCmd.setSwitcherState(hrFlag))
        LepuBleLog.d(tag, "setSwitcherState...hrFlag:$hrFlag")
    }

    fun setConfig(config: Er2Config) {
        sendCmd(Er2BleCmd.setConfig(config))
        LepuBleLog.d(tag, "setConfig...$config")
    }

    fun getSwitcherState() {
        sendCmd(Er2BleCmd.getSwitcherState())
        LepuBleLog.d(tag, "getSwitcherState...")
    }

    override fun getInfo() {
        sendCmd(Er2BleCmd.getDeviceInfo())
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(Er2BleCmd.setTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun getRtData() {
        sendCmd(Er2BleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun getFileList() {
        sendCmd(Er2BleCmd.listFiles())
        LepuBleLog.d(tag, "getFileList...")
    }

    override fun factoryReset() {
        sendCmd(Er2BleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun reset() {
        sendCmd(Er2BleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryResetAll() {
        sendCmd(Er2BleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF...userId:$userId, fileName:$fileName")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        sendCmd(Er2BleCmd.readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(Er1BleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...config:$config")
    }
    fun burnLockFlash() {
        sendCmd(Er1BleCmd.burnLockFlash())
        LepuBleLog.d(tag, "burnLockFlash...")
    }

}