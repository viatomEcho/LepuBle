package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.experimental.inv

/**
 * 心电采样率：实时250HZ，存储250HZ
 * 心电增益：n * 1 / 345 = n * 0.0028985507246377-----345倍
 */
class LewBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LewBleInterface"
    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = LewBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = LewBleManager(context)
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

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    private var userId: String? = null
    var curFile: LewBleResponse.EcgFile? = null
    var listContent = ByteArray(0)
    var listSize = 0

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
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseContent).post(response.bytes)
        when(response.cmd) {
            LewBleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTime).post(InterfaceEvent(model, true))
                } else {
                    val data = TimeData(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTime).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.GET_BATTERY -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_BATTERY response.len <= 0")
                    return
                }
                val data = BatteryInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_BATTERY => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBatteryInfo).post(InterfaceEvent(model, data))
            }
            LewBleCmd.GET_INFO -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_INFO response.len <= 0")
                    return
                }
                val data = DeviceInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewDeviceInfo).post(InterfaceEvent(model, data))
            }
            LewBleCmd.BOUND_DEVICE -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "BOUND_DEVICE response.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,BOUND_DEVICE => success")
                if (response.content[0].toInt() == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice).post(InterfaceEvent(model, true))
                }
            }
            LewBleCmd.UNBOUND_DEVICE -> {
                LepuBleLog.d(tag, "model:$model,UNBOUND_DEVICE => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewUnBoundDevice).post(InterfaceEvent(model, true))
            }
            LewBleCmd.FIND_DEVICE -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "FIND_DEVICE response.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,FIND_DEVICE => success")
                val data = toUInt(response.content)
                if (data == 1) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFindPhone).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFindPhone).post(InterfaceEvent(model, false))
                }
            }
            LewBleCmd.GET_DEVICE_NETWORK -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_DEVICE_NETWORK response.len <= 0")
                    return
                }
                val data = DeviceNetwork(response.content)
                LepuBleLog.d(tag, "model:$model,GET_DEVICE_NETWORK => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceNetwork).post(InterfaceEvent(model, data))
            }
            LewBleCmd.GET_SYSTEM_SETTING -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_SYSTEM_SETTING response.len <= 0")
                    return
                }
                val data = SystemSetting(response.content)
                LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSystemSetting).post(InterfaceEvent(model, data))
            }
            LewBleCmd.SET_SYSTEM_SETTING -> {
                LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSystemSetting).post(InterfaceEvent(model, true))
            }
            LewBleCmd.LANGUAGE_SETTING -> {
                LepuBleLog.d(tag, "model:$model,LANGUAGE_SETTING => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLanguageSetting).post(InterfaceEvent(model, true))
                } else {
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLanguageSetting).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.UNIT_SETTING -> {
                LepuBleLog.d(tag, "model:$model,UNIT_SETTING => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUnitSetting).post(InterfaceEvent(model, true))
                } else {
                    val data = UnitSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUnitSetting).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.HAND_RAISE_SETTING -> {
                LepuBleLog.d(tag, "model:$model,HAND_RAISE_SETTING => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHandRaiseSetting).post(InterfaceEvent(model, true))
                } else {
                    val data = HandRaiseSetting(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHandRaiseSetting).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.LR_HAND_SETTING -> {
                LepuBleLog.d(tag, "model:$model,LR_HAND_SETTING => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLrHandSetting).post(InterfaceEvent(model, true))
                } else {
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLrHandSetting).post(InterfaceEvent(model, data))
                }
            }

            LewBleCmd.NO_DISTURB_MODE -> {
                LepuBleLog.d(tag, "model:$model,NO_DISTURB_MODE => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetNoDisturbMode).post(InterfaceEvent(model, true))
                } else {
                    val data = NoDisturbMode(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetNoDisturbMode).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.APP_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,APP_SWITCH => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAppSwitch).post(InterfaceEvent(model, true))
                } else {
                    val data = AppSwitch(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAppSwitch).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.NOTIFICATION_INFO -> {
                LepuBleLog.d(tag, "model:$model,NOTIFICATION_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSendNotification).post(InterfaceEvent(model, true))
            }
            LewBleCmd.DEVICE_MODE -> {
                LepuBleLog.d(tag, "model:$model,DEVICE_MODE => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetDeviceMode).post(InterfaceEvent(model, true))
                } else {
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceMode).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.ALARM_CLOCK_INFO -> {
                LepuBleLog.d(tag, "model:$model,ALARM_CLOCK_INFO => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAlarmClock).post(InterfaceEvent(model, true))
                } else {
                    val data = AlarmClockInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAlarmClock).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.PHONE_SWITCH -> {
                LepuBleLog.d(tag, "model:$model,PHONE_SWITCH => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneSwitch).post(InterfaceEvent(model, true))
                } else {
                    val data = PhoneSwitch(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneSwitch).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.CALL_CONTROL -> {
                LepuBleLog.d(tag, "model:$model,CALL_CONTROL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewPhoneCall).post(InterfaceEvent(model, true))
            }
            LewBleCmd.MEDICINE_REMIND -> {
                LepuBleLog.d(tag, "model:$model,MEDICINE_REMIND => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMedicineRemind).post(InterfaceEvent(model, true))
                } else {
                    val data = MedicineRemind(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMedicineRemind).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.GET_MEASURE_SETTING -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_MEASURE_SETTING response.len <= 0")
                    return
                }
                val data = MeasureSetting(response.content)
                LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMeasureSetting).post(InterfaceEvent(model, data))
            }
            LewBleCmd.SET_MEASURE_SETTING -> {
                LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMeasureSetting).post(InterfaceEvent(model, true))
            }
            LewBleCmd.SPORT_TARGET -> {
                LepuBleLog.d(tag, "model:$model,SPORT_TARGET => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSportTarget).post(InterfaceEvent(model, true))
                } else {
                    val data = SportTarget(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSportTarget).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.TARGET_REMIND -> {
                LepuBleLog.d(tag, "model:$model,TARGET_REMIND => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTargetRemind).post(InterfaceEvent(model, true))
                } else {
                    val data = toUInt(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTargetRemind).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.SITTING_REMIND -> {
                LepuBleLog.d(tag, "model:$model,SITTING_REMIND => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSittingRemind).post(InterfaceEvent(model, true))
                } else {
                    val data = SittingRemind(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSittingRemind).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.HR_DETECT -> {
                LepuBleLog.d(tag, "model:$model,HR_DETECT => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrDetect).post(InterfaceEvent(model, true))
                } else {
                    val data = HrDetect(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrDetect).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.OXY_DETECT -> {
                LepuBleLog.d(tag, "model:$model,OXY_DETECT => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyDetect).post(InterfaceEvent(model, true))
                } else {
                    val data = OxyDetect(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyDetect).post(InterfaceEvent(model, data))
                }
            }

            LewBleCmd.USER_INFO -> {
                LepuBleLog.d(tag, "model:$model,USER_INFO => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUserInfo).post(InterfaceEvent(model, true))
                } else {
                    val data = UserInfo(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUserInfo).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.PHONE_BOOK -> {
                LepuBleLog.d(tag, "model:$model,PHONE_BOOK => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneBook).post(InterfaceEvent(model, true))
                } else {
                    val data = PhoneBook(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneBook).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.SOS_CONTACT -> {
                LepuBleLog.d(tag, "model:$model,SOS_CONTACT => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSosContact).post(InterfaceEvent(model, true))
                } else {
                    val data = SosContact(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSosContact).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.SECOND_SCREEN -> {
                LepuBleLog.d(tag, "model:$model,SECOND_SCREEN => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSecondScreen).post(InterfaceEvent(model, true))
                } else {
                    val data = SecondScreen(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSecondScreen).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.CARDS -> {
                LepuBleLog.d(tag, "model:$model,CARDS => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetCards).post(InterfaceEvent(model, true))
                } else {
                    var data = IntArray(0)
                    for (i in 0 until response.content.size) {
                        data = data.plus(byte2UInt(response.content[i]))
                    }
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetCards).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.GET_SPORT_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_SPORT_LIST response.len <= 0")
                    return
                }
                listSize += byte2UInt(response.content[1])
                listContent += response.content.copyOfRange(2, response.content.size)
                if (byte2UInt(response.content[0]) == 0) {
                    val list = LewBleResponse.FileList(LewBleCmd.ListType.SPORT, listSize, listContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(InterfaceEvent(model, list))
                    listContent = ByteArray(0)
                    listSize = 0
                }
            }
            LewBleCmd.GET_ECG_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_ECG_LIST response.len <= 0")
                    return
                }
                listSize += byte2UInt(response.content[1])
                listContent += response.content.copyOfRange(2, response.content.size)
                if (byte2UInt(response.content[0]) == 0) {
                    val list = LewBleResponse.FileList(LewBleCmd.ListType.ECG, listSize, listContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(InterfaceEvent(model, list))
                    listContent = ByteArray(0)
                    listSize = 0
                }
            }
            LewBleCmd.GET_HR_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_HR_LIST response.len <= 0")
                    return
                }
                listSize += byte2UInt(response.content[1])
                listContent += response.content.copyOfRange(2, response.content.size)
                if (byte2UInt(response.content[0]) == 0) {
                    val list = LewBleResponse.FileList(LewBleCmd.ListType.HR, listSize, listContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(InterfaceEvent(model, list))
                    listContent = ByteArray(0)
                    listSize = 0
                }
            }
            LewBleCmd.GET_OXY_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_OXY_LIST response.len <= 0")
                    return
                }

                listSize += byte2UInt(response.content[1])
                listContent += response.content.copyOfRange(2, response.content.size)
                if (byte2UInt(response.content[0]) == 0) {
                    val list = LewBleResponse.FileList(LewBleCmd.ListType.OXY, listSize, listContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(InterfaceEvent(model, list))
                    listContent = ByteArray(0)
                    listSize = 0
                }
            }
            LewBleCmd.GET_SLEEP_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_SLEEP_LIST response.len <= 0")
                    return
                }
                listSize += byte2UInt(response.content[1])
                listContent += response.content.copyOfRange(2, response.content.size)
                if (byte2UInt(response.content[0]) == 0) {
                    val list = LewBleResponse.FileList(LewBleCmd.ListType.SLEEP, listSize, listContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).post(InterfaceEvent(model, list))
                    listContent = ByteArray(0)
                    listSize = 0
                }
            }
            LewBleCmd.HR_THRESHOLD -> {
                LepuBleLog.d(tag, "model:$model,HR_THRESHOLD => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrThreshold).post(InterfaceEvent(model, true))
                } else {
                    val data = HrThreshold(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrThreshold).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.OXY_THRESHOLD -> {
                LepuBleLog.d(tag, "model:$model,OXY_THRESHOLD => success")
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "model:$model,set => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyThreshold).post(InterfaceEvent(model, true))
                } else {
                    val data = OxyThreshold(response.content)
                    LepuBleLog.d(tag, "model:$model,get => success $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyThreshold).post(InterfaceEvent(model, data))
                }
            }
            LewBleCmd.RT_DATA -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "RT_DATA response.len <= 0")
                    return
                }
                val data = RtData(response.content)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewRtData).post(InterfaceEvent(model, data))
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

            // 以下部分是之前协议，需测试手表是否兼容
            LewBleCmd.GET_FILE_LIST -> {
                if (response.len <= 0) {
                    LepuBleLog.d(tag, "GET_FILE_LIST response.len <= 0")
                    return
                }
                val data = trimStr(com.lepu.blepro.utils.toString(response.content.copyOfRange(1, response.content.size)))
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success")
                LepuBleLog.d(tag, "model:$model, data == $data")
                LepuBleLog.d(tag, "model:$model, size == " + response.content[0].toInt())
            }

            LewBleCmd.READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")
                if (response.pkgType == 0x01.toByte()) {
                    val fileSize = toUInt(response.content)
                    if (fileSize <= 0) {
                        sendCmd(LewBleCmd.readFileEnd())
                        return
                    }
                    curFile =  curFileName?.let {
                        LewBleResponse.EcgFile(model, it, fileSize)
                    }
                    sendCmd(LewBleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileError).post(InterfaceEvent(model, true))
                }

            }
            LewBleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(LewBleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                        return
                    }
                    // 心电文件数据不知为何有空数据的文件
                    if (response.len <= 0) {
                        sendCmd(LewBleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA response.len <= 0")
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
                } ?: kotlin.run {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileError).post(InterfaceEvent(model, true))
                }
            }
            LewBleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curFileName = null// 一定要放在发通知之前
                curFile?.let {
                    if (it.index < it.fileSize){
                        if ((isCancelRF || isPausedRF)) {
                            LepuBleLog.d(tag, "READ_FILE_END isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                            return
                        }
                    }else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete).post(InterfaceEvent(model, it))
                    }
                } ?: kotlin.run {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileError).post(InterfaceEvent(model, true))
                }
            }
        }
    }

    fun getBattery() {
        sendCmd(LewBleCmd.getBattery())
        LepuBleLog.d(tag, "getBattery")
    }
    override fun getInfo() {
        sendCmd(LewBleCmd.getDeviceInfo())
        LepuBleLog.d(tag, "getInfo")
    }
    override fun syncTime() {
        sendCmd(LewBleCmd.setTime())
        LepuBleLog.d(tag, "syncTime")
    }

    fun setTime(data: TimeData) {
        sendCmd(LewBleCmd.setTime(data.getDataBytes()))
        LepuBleLog.d(tag, "setTime data:$data")
    }
    fun getTime() {
        sendCmd(LewBleCmd.getTime())
        LepuBleLog.d(tag, "getTime")
    }

    fun boundDevice(b: Boolean) {
        if (b) {
            sendCmd(LewBleCmd.boundDevice())
        } else {
            sendCmd(LewBleCmd.unBoundDevice())
        }
        LepuBleLog.d(tag, "boundDevice b:$b")
    }
    fun findDevice(on: Boolean) {
        //0:关闭查找，1:打开查找
        if (on) {
            sendCmd(LewBleCmd.findDevice(1))
        } else {
            sendCmd(LewBleCmd.findDevice(0))
        }
        LepuBleLog.d(tag, "findDevice")
    }
    fun getDeviceNetwork() {
        sendCmd(LewBleCmd.getDeviceNetwork())
        LepuBleLog.d(tag, "getDeviceNetwork")
    }
    fun getSystemSetting() {
        sendCmd(LewBleCmd.getSystemSetting())
        LepuBleLog.d(tag, "getSystemSetting")
    }
    fun setSystemSetting(setting: SystemSetting) {
        sendCmd(LewBleCmd.setSystemSetting(setting.getDataBytes()))
        LepuBleLog.d(tag, "setSystemSetting setting:$setting")
    }
    fun getLanguage() {
        sendCmd(LewBleCmd.getLanguageSetting())
        LepuBleLog.d(tag, "getLanguage")
    }
    fun setLanguage(num: Int) {
        sendCmd(LewBleCmd.setLanguageSetting(num))
        LepuBleLog.d(tag, "setLanguage num:$num")
    }
    fun getUnit() {
        sendCmd(LewBleCmd.getUnitSetting())
        LepuBleLog.d(tag, "getUnit")
    }
    fun setUnit(unit: UnitSetting) {
        sendCmd(LewBleCmd.setUnitSetting(unit.getDataBytes()))
        LepuBleLog.d(tag, "setUnit unit:$unit")
    }
    fun getHandRaise() {
        sendCmd(LewBleCmd.getHandRaiseSetting())
        LepuBleLog.d(tag, "getHandRaise")
    }
    fun setHandRaise(handRaise: HandRaiseSetting) {
        sendCmd(LewBleCmd.setHandRaiseSetting(handRaise.getDataBytes()))
        LepuBleLog.d(tag, "setHandRaise handRaise:$handRaise")
    }
    fun getLrHand() {
        sendCmd(LewBleCmd.getLrHandSetting())
        LepuBleLog.d(tag, "getLrHand")
    }
    fun setLrHand(hand: Int) {
        sendCmd(LewBleCmd.setLrHandSetting(hand))
        LepuBleLog.d(tag, "setLrHand hand:$hand")
    }
    fun getNoDisturbMode() {
        sendCmd(LewBleCmd.getNoDisturbMode())
        LepuBleLog.d(tag, "getNoDisturbMode")
    }
    fun setNoDisturbMode(mode: NoDisturbMode) {
        sendCmd(LewBleCmd.setNoDisturbMode(mode.getDataBytes()))
        LepuBleLog.d(tag, "setNoDisturbMode mode:$mode")
    }
    fun getAppSwitch() {
        sendCmd(LewBleCmd.getAppSwitch())
        LepuBleLog.d(tag, "getAppSwitch")
    }
    fun setAppSwitch(switches: AppSwitch) {
        sendCmd(LewBleCmd.setAppSwitch(switches.getDataBytes()))
        LepuBleLog.d(tag, "setAppSwitch switches:$switches")
    }
    fun notification(info: NotificationInfo) {
        sendCmd(LewBleCmd.notificationInfo(info.getDataBytes()))
        LepuBleLog.d(tag, "notification info:$info")
    }
    fun getDeviceMode() {
        sendCmd(LewBleCmd.getDeviceMode())
        LepuBleLog.d(tag, "getDeviceMode")
    }
    fun setDeviceMode(mode: Int) {
        sendCmd(LewBleCmd.setDeviceMode(mode))
        LepuBleLog.d(tag, "setDeviceMode mode:$mode")
    }
    fun getAlarmClock() {
        sendCmd(LewBleCmd.getAlarmClock())
        LepuBleLog.d(tag, "getAlarmClock")
    }
    fun setAlarmClock(alarm: AlarmClockInfo) {
        sendCmd(LewBleCmd.setAlarmClock(alarm.getDataBytes()))
        LepuBleLog.d(tag, "setAlarmClock alarm:$alarm")
    }
    fun getPhoneSwitch() {
        sendCmd(LewBleCmd.getPhoneSwitch())
        LepuBleLog.d(tag, "getPhoneSwitch")
    }
    fun setPhoneSwitch(switches: PhoneSwitch) {
        sendCmd(LewBleCmd.setPhoneSwitch(switches.getDataBytes()))
        LepuBleLog.d(tag, "setPhoneSwitch switches:$switches")
    }
    fun getMedicineRemind() {
        sendCmd(LewBleCmd.getMedicineRemind())
        LepuBleLog.d(tag, "getMedicineRemind")
    }
    fun setMedicineRemind(remind: MedicineRemind) {
        sendCmd(LewBleCmd.setMedicineRemind(remind.getDataBytes()))
        LepuBleLog.d(tag, "setMedicineRemind remind:$remind")
    }
    fun getMeasureSetting() {
        sendCmd(LewBleCmd.getMeasureSetting())
        LepuBleLog.d(tag, "getMeasureSetting")
    }
    fun setMeasureSetting(setting: MeasureSetting) {
        sendCmd(LewBleCmd.setMeasureSetting(setting.getDataBytes()))
        LepuBleLog.d(tag, "setMeasureSetting setting:$setting")
    }
    fun getSportTarget() {
        sendCmd(LewBleCmd.getSportTarget())
        LepuBleLog.d(tag, "getSportTarget")
    }
    fun setSportTarget(target: SportTarget) {
        sendCmd(LewBleCmd.setSportTarget(target.getDataBytes()))
        LepuBleLog.d(tag, "setSportTarget target:$target")
    }
    fun getTargetRemind() {
        sendCmd(LewBleCmd.getTargetRemind())
        LepuBleLog.d(tag, "getTargetRemind")
    }
    fun setTargetRemind(remind: Boolean) {
        sendCmd(LewBleCmd.setTargetRemind(remind))
        LepuBleLog.d(tag, "setTargetRemind remind:$remind")
    }
    fun getSittingRemind() {
        sendCmd(LewBleCmd.getSittingRemind())
        LepuBleLog.d(tag, "getSittingRemind")
    }
    fun setSittingRemind(remind: SittingRemind) {
        sendCmd(LewBleCmd.setSittingRemind(remind.getDataBytes()))
        LepuBleLog.d(tag, "setSittingRemind remind:$remind")
    }
    fun getHrDetect() {
        sendCmd(LewBleCmd.getHrDetect())
        LepuBleLog.d(tag, "getHrDetect")
    }
    fun setHrDetect(detect: HrDetect) {
        sendCmd(LewBleCmd.setHrDetect(detect.getDataBytes()))
        LepuBleLog.d(tag, "setHrDetect detect:$detect")
    }
    fun getOxyDetect() {
        sendCmd(LewBleCmd.getOxyDetect())
        LepuBleLog.d(tag, "getOxyDetect")
    }
    fun setOxyDetect(detect: OxyDetect) {
        sendCmd(LewBleCmd.setOxyDetect(detect.getDataBytes()))
        LepuBleLog.d(tag, "setOxyDetect detect:$detect")
    }
    fun getUserInfo() {
        sendCmd(LewBleCmd.getUserInfo())
        LepuBleLog.d(tag, "getUserInfo")
    }
    fun setUserInfo(info: UserInfo) {
        sendCmd(LewBleCmd.setUserInfo(info.getDataBytes()))
        LepuBleLog.d(tag, "setUserInfo info:$info")
    }
    fun getPhoneBook() {
        sendCmd(LewBleCmd.getPhoneBook())
        LepuBleLog.d(tag, "getPhoneBook")
    }
    fun setPhoneBook(book: PhoneBook) {
        sendCmd(LewBleCmd.setPhoneBook(book.getDataBytes()))
        LepuBleLog.d(tag, "setPhoneBook book:$book")
    }
    fun getSosContact() {
        sendCmd(LewBleCmd.getSosContact())
        LepuBleLog.d(tag, "getSosContact")
    }
    fun setSosContact(sos: SosContact) {
        sendCmd(LewBleCmd.setSosContact(sos.getDataBytes()))
        LepuBleLog.d(tag, "setSosContact sos:$sos")
    }
    fun setDialNum(num: Int) {
        sendCmd(LewBleCmd.setDialNum(num))
        LepuBleLog.d(tag, "setDialNum")
    }
    fun setDialFormat() {
        // ???
        LepuBleLog.d(tag, "setDialFormat")
    }
    fun getSecondScreen() {
        sendCmd(LewBleCmd.getSecondScreen())
        LepuBleLog.d(tag, "getSecondScreen")
    }
    fun setSecondScreen(screen: SecondScreen) {
        sendCmd(LewBleCmd.setSecondScreen(screen.getDataBytes()))
        LepuBleLog.d(tag, "setSecondScreen screen:$screen")
    }
    fun getCards() {
        sendCmd(LewBleCmd.getCards())
        LepuBleLog.d(tag, "getCards")
    }
    fun setCards(cards: IntArray) {
        sendCmd(LewBleCmd.setCards(cards))
        LepuBleLog.d(tag, "setCards cards:${Arrays.toString(cards)}")
    }
    override fun getFileList() {
        offset = 0
        isCancelRF = false
        isPausedRF = false
        sendCmd(LewBleCmd.listFiles())
        LepuBleLog.d(tag, "getFileList")
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
            LewBleCmd.ListType.SLEEP -> {
                sendCmd(LewBleCmd.getSleepList(startTime))
            }
        }
        LepuBleLog.d(tag, "getFileList type:$type, startTime:$startTime")
    }
    fun getHrThreshold() {
        sendCmd(LewBleCmd.getHrThreshold())
        LepuBleLog.d(tag, "getHrThreshold")
    }
    fun setHrThreshold(threshold: HrThreshold) {
        sendCmd(LewBleCmd.setHrThreshold(threshold.getDataBytes()))
        LepuBleLog.d(tag, "setHrThreshold")
    }
    fun getOxyThreshold() {
        sendCmd(LewBleCmd.getOxyThreshold())
        LepuBleLog.d(tag, "getOxyThreshold")
    }
    fun setOxyThreshold(threshold: OxyThreshold) {
        sendCmd(LewBleCmd.setOxyThreshold(threshold.getDataBytes()))
        LepuBleLog.d(tag, "setOxyThreshold")
    }

    override fun getRtData() {
        sendCmd(LewBleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData")
    }

    fun deleteFile(fileName: String) {
        sendCmd(LewBleCmd.deleteFile(fileName.toByteArray()))
        LepuBleLog.d(tag, "deleteFile fileName:$fileName")
    }

    override fun factoryReset() {
        sendCmd(LewBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset")
    }

    override fun reset() {
        sendCmd(LewBleCmd.reset())
        LepuBleLog.d(tag, "reset")
    }

    override fun factoryResetAll() {
        sendCmd(LewBleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF userId:$userId, fileName:$fileName")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        sendCmd(LewBleCmd.readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
    }

}