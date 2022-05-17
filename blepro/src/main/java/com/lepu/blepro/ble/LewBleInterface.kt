package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.experimental.inv

/**
 * author: chenyongfeng
 * created on: 2022/1/11 16:24
 * description:
 */
class LewBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LewBleInterface"
    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = LewBleManager(context)
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

    /**
     * download a file, name come from filelist
     */
    var fileListName: String? = null
    var curFileName: String? = null
    private var userId: String? = null
    var curFile: LewBleResponse.EcgFile? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(LewBleCmd.readFileStart(fileName.toByteArray(), 0))
    }

    @OptIn(ExperimentalUnsignedTypes::class)
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
                val respPkg = LewBleResponse.BleResponse(temp)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LewBleResponse.BleResponse) {
        LiveEventBus.get<String>(EventMsgConst.Cmd.EventCmdResponseContent).post(bytesToHex(response.bytes))
        when(response.cmd) {
            LewBleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = TimeData(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTime).post(InterfaceEvent(model, true))
            }
            LewBleCmd.GET_BATTERY -> {
                if (response.len == 0) return
                val data = BatteryInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_BATTERY => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBatteryInfo).post(InterfaceEvent(model, data))
            }
            LewBleCmd.GET_INFO -> {
                if (response.len == 0) return
                val data = DeviceInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewInfo).post(InterfaceEvent(model, data))
                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }
            }
            LewBleCmd.BOUND_DEVICE -> {
                if (response.len == 0) return
                LepuBleLog.d(tag, "model:$model,BOUND_DEVICE => success")
                if (response.content[0].toInt() == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice).post(InterfaceEvent(model, false))
                }
            }
            LewBleCmd.UNBOUND_DEVICE -> {
                LepuBleLog.d(tag, "model:$model,UNBOUND_DEVICE => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewUnBoundDevice).post(InterfaceEvent(model, true))
            }
            LewBleCmd.FIND_DEVICE -> {
                LepuBleLog.d(tag, "model:$model,FIND_DEVICE => success")
            }
            LewBleCmd.GET_SYSTEM_SETTING -> {
                if (response.len == 0) return
                val data = SystemSetting(response.content)
                LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSystemSetting).post(InterfaceEvent(model, data))
            }
            LewBleCmd.SET_SYSTEM_SETTING -> {
                LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSystemSetting).post(InterfaceEvent(model, true))
            }
            LewBleCmd.NO_DISTURB_MODE -> {
                LepuBleLog.d(tag, "model:$model,NO_DISTURB_MODE => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = NoDisturbMode(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.APP_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,APP_SWITCH => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = AppSwitch(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.NOTIFICATION_INFO -> {
                LepuBleLog.d(tag, "model:$model,NOTIFICATION_INFO => success")
            }
            LewBleCmd.DEVICE_MODE -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_MODE => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.ALARM_CLOCK_INFO -> {
                LepuBleLog.d(tag, "model:$model,ALARM_CLOCK_INFO => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = AlarmClockInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.PHONE_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,PHONE_SWITCH => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = PhoneSwitch(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.CALL_CONTROL -> {
                LepuBleLog.d(tag, "model:$model,CALL_CONTROL => success")
            }
            LewBleCmd.GET_MEASURE_SETTING -> {
                if (response.len == 0) return
                val data = MeasureSetting(response.content)
                LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMeasureSetting).post(InterfaceEvent(model, data))
            }
            LewBleCmd.SET_MEASURE_SETTING -> {
                LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMeasureSetting).post(InterfaceEvent(model, true))
            }
            LewBleCmd.USER_INFO -> {
                LepuBleLog.d(tag, "model:$model,USER_INFO => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = UserInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.PHONE_BOOK -> {
                LepuBleLog.d(tag, "model:$model,PHONE_BOOK => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = PhoneBook(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.SOS_CONTACT -> {
                LepuBleLog.d(tag, "model:$model,SOS_CONTACT => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = SosContact(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.GET_SPORT_LIST -> {
                if (response.len == 0) return
                val data = SportList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_SPORT_LIST => success $data")
            }
            LewBleCmd.GET_ECG_LIST -> {
                if (response.len == 0) return
                val data = EcgList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_ECG_LIST => success $data")
            }
            LewBleCmd.GET_HR_LIST -> {
                if (response.len == 0) return
                val data = HrList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_HR_LIST => success $data")
            }
            LewBleCmd.GET_OXY_LIST -> {
                if (response.len == 0) return
                val data = OxyList(response.content)
                LepuBleLog.d(tag, "model:$model,GET_OXY_LIST => success $data")
            }
            LewBleCmd.HR_THRESHOLD -> {
                LepuBleLog.d(tag, "model:$model,HR_THRESHOLD => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = HrThreshold(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.OXY_THRESHOLD -> {
                LepuBleLog.d(tag, "model:$model,OXY_THRESHOLD => success")
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                } else {
                    val data = OxyThreshold(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                }
            }
            LewBleCmd.RT_DATA -> {
                if (response.len == 0) return
                val data = RtData(response.content)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success $data")
            }

            LewBleCmd.RESET -> {
                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReset).post(InterfaceEvent(model, true))
            }
            LewBleCmd.FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFactoryReset).post(InterfaceEvent(model, true))
            }
            LewBleCmd.FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFactoryResetAll).post(InterfaceEvent(model, true))
            }

            LewBleCmd.GET_FILE_LIST -> {
                if (response.len == 0) return
                fileListName = trimStr(com.lepu.blepro.utils.toString(response.content.copyOfRange(1, response.content.size)))
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success")
                curFileName = fileListName
                LepuBleLog.d(tag, "model:$model, curFileName == $curFileName")
                LepuBleLog.d(tag, "model:$model, size == " + response.content[0].toInt())
                sendCmd(LewBleCmd.readFileStart(curFileName?.toByteArray(), 0))
            }

            LewBleCmd.READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")
                if (response.pkgType == 0x01.toByte()) {
                    curFile =  curFileName?.let {
                        LewBleResponse.EcgFile(model, it, toUInt(response.content))
                    }
                    sendCmd(LewBleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileError).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                }

            }
            LewBleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(LewBleCmd.readFileEnd())
                        return
                    }
                    // 心电文件数据不知为何有空数据的文件
                    if (response.len == 0) {
                        sendCmd(LewBleCmd.readFileEnd())
                        return
                    }

                    this.addContent(response.content)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                    if (this.index < this.fileSize) {
                        sendCmd(LewBleCmd.readFileData(this.index))
                    } else {
                        sendCmd(LewBleCmd.readFileEnd())
                    }
                }
            }
            LewBleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                if (curFileName?.equals(fileListName) == true) {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ) {
                            if ((isCancelRF || isPausedRF) ) return
                        } else {
                            val list =
                                LewBleResponse.FileList(it.content)

                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(
                                InterfaceEvent(
                                    model,
                                    list
                                )
                            )
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END EventLewFileList  model:$model,  curFile error!!")
                } else {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
                                .post(InterfaceEvent(model, it))
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END EventLewReadFileComplete  model:$model,  curFile error!!")
                }
                curFile = null
            }


        }
    }

    fun getBattery() {
        sendCmd(LewBleCmd.getBattery())
    }
    override fun getInfo() {
        sendCmd(LewBleCmd.getDeviceInfo())
    }
    override fun syncTime() {
        sendCmd(LewBleCmd.setTime())
    }

    fun boundDevice(b: Boolean) {
        if (b) {
            sendCmd(LewBleCmd.boundDevice())
        } else {
            sendCmd(LewBleCmd.unBoundDevice())
        }
    }
    fun findDevice() {
        sendCmd(LewBleCmd.findDevice())
    }
    fun getSystemSetting() {
        sendCmd(LewBleCmd.getSystemSetting())
    }
    fun setSystemSetting(setting: SystemSetting) {
        sendCmd(LewBleCmd.setSystemSetting(setting.getDataBytes()))
    }
    fun getLanguage() {
        sendCmd(LewBleCmd.getLanguageSetting())
    }
    fun setLanguage(num: Int) {
        sendCmd(LewBleCmd.setLanguageSetting(num))
    }
    fun getUnit() {
        sendCmd(LewBleCmd.getUnitSetting())
    }
    fun setUnit(unit: UnitSetting) {
        sendCmd(LewBleCmd.setUnitSetting(unit.getDataBytes()))
    }
    fun getHandRaise() {
        sendCmd(LewBleCmd.getHandRaiseSetting())
    }
    fun setHandRaise(handRaise: HandRaiseSetting) {
        sendCmd(LewBleCmd.setHandRaiseSetting(handRaise.getDataBytes()))
    }
    fun getLrHand() {
        sendCmd(LewBleCmd.getLrHandSetting())
    }
    fun setLrHand(hand: Int) {
        sendCmd(LewBleCmd.setLrHandSetting(hand))
    }
    fun getNoDisturbMode() {
        sendCmd(LewBleCmd.getNoDisturbMode())
    }
    fun setNoDisturbMode(mode: NoDisturbMode) {
        sendCmd(LewBleCmd.setNoDisturbMode(mode.getDataBytes()))
    }
    fun getAppSwitch() {
        sendCmd(LewBleCmd.getAppSwitch())
    }
    fun setAppSwitch(switches: AppSwitch) {
        sendCmd(LewBleCmd.setAppSwitch(switches.getDataBytes()))
    }
    fun notification(info: NotificationInfo) {
        sendCmd(LewBleCmd.notificationInfo(info.getDataBytes()))
    }
    fun getDeviceMode() {
        sendCmd(LewBleCmd.getDeviceMode())
    }
    fun setDeviceMode(mode: Int) {
        sendCmd(LewBleCmd.setDeviceMode(mode))
    }
    fun getAlarmClockInfo() {
        sendCmd(LewBleCmd.getAlarmClockInfo())
    }
    fun setAlarmClockInfo(alarm: AlarmClockInfo) {
        sendCmd(LewBleCmd.setAlarmClockInfo(alarm.getDataBytes()))
    }
    fun getPhoneSwitch() {
        sendCmd(LewBleCmd.getPhoneSwitch())
    }
    fun setPhoneSwitch(switches: PhoneSwitch) {
        sendCmd(LewBleCmd.setPhoneSwitch(switches.getDataBytes()))
    }
    fun getMeasureSetting() {
        sendCmd(LewBleCmd.getMeasureSetting())
    }
    fun setMeasureSetting(setting: MeasureSetting) {
        sendCmd(LewBleCmd.setMeasureSetting(setting.getDataBytes()))
    }
    fun getSportTarget() {
        sendCmd(LewBleCmd.getSportTarget())
    }
    fun setSportTarget(target: SportTarget) {
        sendCmd(LewBleCmd.setSportTarget(target.getDataBytes()))
    }
    fun getTargetRemind() {
        sendCmd(LewBleCmd.getTargetRemind())
    }
    fun setTargetRemind(remind: Boolean) {
        sendCmd(LewBleCmd.setTargetRemind(remind))
    }
    fun getSittingRemind() {
        sendCmd(LewBleCmd.getSittingRemind())
    }
    fun setSittingRemind(remind: SittingRemind) {
        sendCmd(LewBleCmd.setSittingRemind(remind.getDataBytes()))
    }
    fun getHrDetect() {
        sendCmd(LewBleCmd.getHrDetect())
    }
    fun setHrDetect(detect: HrDetect) {
        sendCmd(LewBleCmd.setHrDetect(detect.getDataBytes()))
    }
    fun getUserInfo() {
        sendCmd(LewBleCmd.getUserInfo())
    }
    fun setUserInfo(info: UserInfo) {
        sendCmd(LewBleCmd.setUserInfo(info.getDataBytes()))
    }
    fun getPhoneBook() {
        sendCmd(LewBleCmd.getPhoneBook())
    }
    fun setPhoneBook(book: PhoneBook) {
        sendCmd(LewBleCmd.setPhoneBook(book.getDataBytes()))
    }
    fun getSosContact() {
        sendCmd(LewBleCmd.getSosContact())
    }
    fun setSosContact(sos: SosContact) {
        sendCmd(LewBleCmd.setSosContact(sos.getDataBytes()))
    }
    fun setDialNum(num: Int) {
        sendCmd(LewBleCmd.setDialNum(num))
    }
    fun setDialFormat() {
        // ???
    }
    override fun getFileList() {
        offset = 0
        isCancelRF = false
        isPausedRF = false
        sendCmd(LewBleCmd.listFiles())
    }
    fun getFileList(type: Int, startTime: Int) {
        when (type) {
            LewBleCmd.ListType.SPORT -> {
                sendCmd(LewBleCmd.getSportList(startTime))
            }
            LewBleCmd.ListType.ECG -> {
                sendCmd(LewBleCmd.getEcgList(startTime))
            }
            LewBleCmd.ListType.HR -> {
                sendCmd(LewBleCmd.getHrList(startTime))
            }
            LewBleCmd.ListType.OXY -> {
                sendCmd(LewBleCmd.getOxyList(startTime))
            }
        }
    }
    fun getHrThreshold() {
        sendCmd(LewBleCmd.getHrThreshold())
    }
    fun setHrThreshold(threshold: HrThreshold) {
        sendCmd(LewBleCmd.setHrThreshold(threshold.getDataBytes()))
    }
    fun getOxyThreshold() {
        sendCmd(LewBleCmd.getOxyThreshold())
    }
    fun setOxyThreshold(threshold: OxyThreshold) {
        sendCmd(LewBleCmd.setOxyThreshold(threshold.getDataBytes()))
    }

    override fun getRtData() {
        sendCmd(LewBleCmd.getRtData())
    }

    fun deleteFile(fileName: String) {
        sendCmd(LewBleCmd.deleteFile(fileName.toByteArray()))
    }

    override fun factoryReset() {
        sendCmd(LewBleCmd.factoryReset())
    }

    override fun reset() {
        sendCmd(LewBleCmd.reset())
    }

    override fun factoryResetAll() {
        sendCmd(LewBleCmd.factoryResetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
    }



}