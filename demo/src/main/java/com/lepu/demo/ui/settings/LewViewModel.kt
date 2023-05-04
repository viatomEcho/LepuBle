package com.lepu.demo.ui.settings

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LewBleCmd
import com.lepu.blepro.ble.cmd.LewBleResponse
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding
import java.util.*

class LewViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        // 时间
        binding.lewLayout.lewGetTime.setOnClickListener {
            LpBleUtil.lewGetTime(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetTime.setOnClickListener {
            state++
            if (state > LewBleCmd.TimeFormat.FORMAT_24H) {
                state = LewBleCmd.TimeFormat.FORMAT_12H
            }
            val data = TimeData()
            data.formatHour = state
            data.formatDay = state
            LpBleUtil.lewSetTime(model, data)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $data"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetInfo.setOnClickListener {
            LpBleUtil.getInfo(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        // 绑定
        binding.lewLayout.lewBound.setOnClickListener {
            LpBleUtil.lewBoundDevice(model, true)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        // 解绑
        binding.lewLayout.lewUnbound.setOnClickListener {
            LpBleUtil.lewBoundDevice(model, false)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        // 获取电量
        binding.lewLayout.lewGetBattery.setOnClickListener {
            LpBleUtil.lewGetBattery(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        // 系统配置（语言、单位、翻腕亮屏、左右手）
        binding.lewLayout.lewGetSystemSetting.setOnClickListener {
            LpBleUtil.lewGetSystemSetting(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetSystemSetting.setOnClickListener {
            val setting = SystemSetting()
            setting.language = LewBleCmd.Language.CHINESE

            val unit = UnitSetting()
            unit.lengthUnit = LewBleCmd.Unit.LENGTH_FEET_INCH
            unit.weightUnit = LewBleCmd.Unit.WEIGHT_QUARTZ
            unit.tempUnit = LewBleCmd.Unit.TEMP_F
            setting.unit = unit

            val handRaise = HandRaiseSetting()
            switchState = !switchState
            handRaise.switch = switchState
            handRaise.startHour = 10
            handRaise.startMin = 0
            handRaise.stopHour = 18
            handRaise.stopMin = 0
            setting.handRaise = handRaise

            setting.hand = LewBleCmd.Hand.RIGHT
            Log.d("test12345", "lewSetSystemSetting $setting")
            LpBleUtil.lewSetSystemSetting(model, setting)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $setting"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetLanguage.setOnClickListener {
            LpBleUtil.lewGetLanguage(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetLanguage.setOnClickListener {
            state++
            if (state > LewBleCmd.Language.FARSI) {
                state = LewBleCmd.Language.ENGLISH
            }
            Log.d("test12345", "lewSetLanguage $state")
            LpBleUtil.lewSetLanguage(model, state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $state"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetNetwork.setOnClickListener {
            LpBleUtil.lewGetDeviceNetwork(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetUnit.setOnClickListener {
            LpBleUtil.lewGetUnit(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetUnit.setOnClickListener {
            val unit = UnitSetting()
            state++
            if (state > LewBleCmd.Unit.LENGTH_FEET_INCH) {
                state = LewBleCmd.Unit.LENGTH_KM_M
            }
            unit.lengthUnit = state
            state++
            if (state > LewBleCmd.Unit.WEIGHT_KG_G) {
                state = LewBleCmd.Unit.WEIGHT_QUARTZ
            }
            unit.weightUnit = state
            state++
            if (state > LewBleCmd.Unit.TEMP_F) {
                state = LewBleCmd.Unit.TEMP_C
            }
            unit.tempUnit = state
            Log.d("test12345", "lewSetUnit $unit")
            LpBleUtil.lewSetUnit(model, unit)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $unit"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetHandRaise.setOnClickListener {
            LpBleUtil.lewGetHandRaise(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetHandRaise.setOnClickListener {
            val handRaise = HandRaiseSetting()
            switchState = !switchState
            handRaise.switch = switchState
            handRaise.startHour = 0
            handRaise.startMin = 0
            handRaise.stopHour = 24
            handRaise.stopMin = 0
            Log.d("test12345", "lewSetHandRaise $handRaise")
            LpBleUtil.lewSetHandRaise(model, handRaise)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $handRaise"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetLrHand.setOnClickListener {
            LpBleUtil.lewGetLrHand(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetLrHand.setOnClickListener {
            state++
            if (state > LewBleCmd.Hand.RIGHT) {
                state = LewBleCmd.Hand.LEFT
            }
            Log.d("test12345", "lewSetLrHand $state")
            LpBleUtil.lewSetLrHand(model, state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $state"
            binding.sendCmd.text = cmdStr
        }
        // 寻找设备
        binding.lewLayout.lewFindDevice.setOnClickListener {
            switchState = !switchState
            LpBleUtil.lewFindDevice(model, switchState)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        // 勿扰模式
        binding.lewLayout.lewGetNoDisturb.setOnClickListener {
            LpBleUtil.lewGetNoDisturbMode(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetNoDisturb.setOnClickListener {
            switchState = !switchState
            val mode = NoDisturbMode()
            mode.switch = switchState

            val item = NoDisturbMode.Item()
            item.startHour = 7
            item.startMin = 0
            item.stopHour = 9
            item.stopMin = 30
            val item2 = NoDisturbMode.Item()
            item2.startHour = 17
            item2.startMin = 15
            item2.stopHour = 19
            item2.stopMin = 45

            mode.items.add(item)
            mode.items.add(item2)
            Log.d("test12345", "lewSetNoDisturb $mode")
            LpBleUtil.lewSetNoDisturbMode(model, mode)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $mode"
            binding.sendCmd.text = cmdStr
        }
        // app通知开关
        binding.lewLayout.lewGetAppSwitch.setOnClickListener {
            LpBleUtil.lewGetAppSwitch(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetAppSwitch.setOnClickListener {
            switchState = !switchState
            val appSwitch = AppSwitch()
            appSwitch.all = switchState
            switchState = !switchState
            appSwitch.phone = switchState
            switchState = !switchState
            appSwitch.message = switchState
            switchState = !switchState
            appSwitch.qq = switchState
            switchState = !switchState
            appSwitch.wechat = switchState
            switchState = !switchState
            appSwitch.email = switchState
            switchState = !switchState
            appSwitch.facebook = switchState
            switchState = !switchState
            appSwitch.twitter = switchState
            switchState = !switchState
            appSwitch.whatsApp = switchState
            switchState = !switchState
            appSwitch.instagram = switchState
            switchState = !switchState
            appSwitch.skype = switchState
            switchState = !switchState
            appSwitch.linkedIn = switchState
            switchState = !switchState
            appSwitch.line = switchState
            switchState = !switchState
            appSwitch.weibo = switchState
            switchState = !switchState
            appSwitch.other = switchState
            Log.d("test12345", "lewSetAppSwitch $appSwitch")
            LpBleUtil.lewSetAppSwitch(model, appSwitch)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $appSwitch"
            binding.sendCmd.text = cmdStr
        }
        // 消息通知
        binding.lewLayout.lewPhoneNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.PHONE
            noti.time = System.currentTimeMillis().div(1000)

            val phone = NotificationInfo.NotiPhone()
            phone.name = "张三里abc123"
            phone.phone = "13420111867"

            noti.info = phone
            Log.d("test12345", "lewPhoneNoti $noti")
            LpBleUtil.lewSendNotification(model, noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewMessageNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.MESSAGE
            noti.time = System.currentTimeMillis().div(1000)

            val mess = NotificationInfo.NotiMessage()
            mess.name = "张三里abc123"
            mess.phone = "13420111867"
            mess.text = "张三里abc123"

            noti.info = mess
            Log.d("test12345", "lewMessageNoti $noti")
            LpBleUtil.lewSendNotification(model, noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewOtherNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.OTHER
            noti.time = System.currentTimeMillis().div(1000)

            val other = NotificationInfo.NotiOther()
            other.name = "张三里abc123"
            other.text = "张三里abc123"

            noti.info = other
            Log.d("test12345", "lewOtherNoti $noti")
            LpBleUtil.lewSendNotification(model, noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        // 设备模式
        binding.lewLayout.lewGetDeviceMode.setOnClickListener {
            LpBleUtil.lewGetDeviceMode(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetDeviceMode.setOnClickListener {
            state++
            if (state > LewBleCmd.DeviceMode.MODE_FREE) {
                state = LewBleCmd.DeviceMode.MODE_NORMAL
            }
            Log.d("test12345", "lewSetDeviceMode $state")
            LpBleUtil.lewSetDeviceMode(model, state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $state"
            binding.sendCmd.text = cmdStr
        }
        // 闹钟
        binding.lewLayout.lewGetAlarmInfo.setOnClickListener {
            LpBleUtil.lewGetAlarmClock(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetAlarmInfo.setOnClickListener {
            switchState = !switchState
            val info = AlarmClockInfo()
            val item = AlarmClockInfo.Item()
            item.hour = 7
            item.minute = 10
            item.repeat = switchState
            item.switch = switchState
            item.everySunday = switchState
            switchState = !switchState
            item.everyMonday = switchState
            switchState = !switchState
            item.everyTuesday = switchState
            switchState = !switchState
            item.everyWednesday = switchState
            switchState = !switchState
            item.everyThursday = switchState
            switchState = !switchState
            item.everyFriday = switchState
            switchState = !switchState
            item.everySaturday = switchState

            val item2 = AlarmClockInfo.Item()
            item2.hour = 17
            item2.minute = 10
            item2.repeat = switchState
            item2.switch = switchState
            item2.everySunday = switchState
            switchState = !switchState
            item2.everyMonday = switchState
            switchState = !switchState
            item2.everyTuesday = switchState
            switchState = !switchState
            item2.everyWednesday = switchState
            switchState = !switchState
            item2.everyThursday = switchState
            switchState = !switchState
            item2.everyFriday = switchState
            switchState = !switchState
            item2.everySaturday = switchState

            info.items.add(item)
            info.items.add(item2)
            Log.d("test12345", "lewSetAlarmInfo $info")
            LpBleUtil.lewSetAlarmClock(model, info)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $info"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetPhoneSwitch.setOnClickListener {
            LpBleUtil.lewGetPhoneSwitch(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetPhoneSwitch.setOnClickListener {
            val phoneSwitch = PhoneSwitch()
            switchState = !switchState
            phoneSwitch.call = switchState
            switchState = !switchState
            phoneSwitch.message = switchState
            Log.d("test12345", "lewSetPhoneSwitch $phoneSwitch")
            LpBleUtil.lewSetPhoneSwitch(model, phoneSwitch)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $phoneSwitch"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetMedicineRemind.setOnClickListener {
            LpBleUtil.lewGetMedicineRemind(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetMedicineRemind.setOnClickListener {
            val remind = MedicineRemind()
            val item = MedicineRemind.Item()
            item.hour = 9
            item.minute = 10
            item.repeat = switchState
            item.switch = switchState
            item.everySunday = switchState
            switchState = !switchState
            item.everyMonday = switchState
            switchState = !switchState
            item.everyTuesday = switchState
            switchState = !switchState
            item.everyWednesday = switchState
            switchState = !switchState
            item.everyThursday = switchState
            switchState = !switchState
            item.everyFriday = switchState
            switchState = !switchState
            item.everySaturday = switchState
            item.name = "感冒药"

            val item2 = MedicineRemind.Item()
            item2.hour = 17
            item2.minute = 10
            item2.repeat = switchState
            item2.switch = switchState
            item2.everySunday = switchState
            switchState = !switchState
            item2.everyMonday = switchState
            switchState = !switchState
            item2.everyTuesday = switchState
            switchState = !switchState
            item2.everyWednesday = switchState
            switchState = !switchState
            item2.everyThursday = switchState
            switchState = !switchState
            item2.everyFriday = switchState
            switchState = !switchState
            item2.everySaturday = switchState
            item2.name = "发烧药"

            remind.items.add(item)
            remind.items.add(item2)
            Log.d("test12345", "lewSetMedicineRemind $remind")
            LpBleUtil.lewSetMedicineRemind(model, remind)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $remind"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetMeasureSetting.setOnClickListener {
            LpBleUtil.lewGetMeasureSetting(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetMeasureSetting.setOnClickListener {
            val setting = MeasureSetting()

            val sportTarget = SportTarget()
            sportTarget.step = 12
            sportTarget.distance = 5000
            sportTarget.calories = 12
            sportTarget.sleep = 30
            sportTarget.sportTime = 12
            setting.sportTarget = sportTarget

            switchState = !switchState
            setting.targetRemind = switchState

            val sittingRemind = SittingRemind()
            switchState = !switchState
            sittingRemind.switch = switchState
            switchState = !switchState
            sittingRemind.noonSwitch = switchState
            sittingRemind.everySunday = switchState
            switchState = !switchState
            sittingRemind.everyMonday = switchState
            switchState = !switchState
            sittingRemind.everyTuesday = switchState
            switchState = !switchState
            sittingRemind.everyWednesday = switchState
            switchState = !switchState
            sittingRemind.everyThursday = switchState
            switchState = !switchState
            sittingRemind.everyFriday = switchState
            switchState = !switchState
            sittingRemind.everySaturday = switchState
            sittingRemind.startHour = 10
            sittingRemind.startMin = 0
            sittingRemind.stopHour = 18
            sittingRemind.stopMin = 30
            setting.sittingRemind = sittingRemind

            val hrDetect = HrDetect()
            hrDetect.switch = switchState
            hrDetect.interval = 5

            val oxyDetect = OxyDetect()
            oxyDetect.switch = switchState
            oxyDetect.interval = 5

            setting.hrDetect = hrDetect
            setting.oxyDetect = oxyDetect
            Log.d("test12345", "lewSetMeasureSetting $setting")
            LpBleUtil.lewSetMeasureSetting(model, setting)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $setting"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSportTarget.setOnClickListener {
            LpBleUtil.lewGetSportTarget(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetSportTarget.setOnClickListener {
            val sportTarget = SportTarget()
            sportTarget.step = 22
            sportTarget.distance = 5000
            sportTarget.calories = 22
            sportTarget.sleep = 30
            sportTarget.sportTime = 22
            Log.d("test12345", "lewSetSportTarget $sportTarget")
            LpBleUtil.lewSetSportTarget(model, sportTarget)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $sportTarget"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetTargetRemind.setOnClickListener {
            LpBleUtil.lewGetTargetRemind(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetTargetRemind.setOnClickListener {
            switchState = !switchState
            Log.d("test12345", "lewSetTargetRemind $switchState")
            LpBleUtil.lewSetTargetRemind(model, switchState)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $switchState"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSittingRemind.setOnClickListener {
            LpBleUtil.lewGetSittingRemind(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetSittingRemind.setOnClickListener {
            val sittingRemind = SittingRemind()
            switchState = !switchState
            sittingRemind.switch = switchState
            switchState = !switchState
            sittingRemind.noonSwitch = switchState
            sittingRemind.everySunday = switchState
            switchState = !switchState
            sittingRemind.everyMonday = switchState
            switchState = !switchState
            sittingRemind.everyTuesday = switchState
            switchState = !switchState
            sittingRemind.everyWednesday = switchState
            switchState = !switchState
            sittingRemind.everyThursday = switchState
            switchState = !switchState
            sittingRemind.everyFriday = switchState
            switchState = !switchState
            sittingRemind.everySaturday = switchState
            sittingRemind.startHour = 10
            sittingRemind.startMin = 0
            sittingRemind.stopHour = 18
            sittingRemind.stopMin = 30
            Log.d("test12345", "lewSetSittingRemind $sittingRemind")
            LpBleUtil.lewSetSittingRemind(model, sittingRemind)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $sittingRemind"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetHrDetect.setOnClickListener {
            LpBleUtil.lewGetHrDetect(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetHrDetect.setOnClickListener {
            switchState = !switchState
            val detect = HrDetect()
            detect.switch = switchState
            detect.interval = 5
            Log.d("test12345", "lewSetHrDetect $detect")
            LpBleUtil.lewSetHrDetect(model, detect)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $detect"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetOxyDetect.setOnClickListener {
            LpBleUtil.lewGetOxyDetect(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetOxyDetect.setOnClickListener {
            switchState = !switchState
            val detect = OxyDetect()
            detect.switch = switchState
            detect.interval = 5
            Log.d("test12345", "lewSetOxyDetect $detect")
            LpBleUtil.lewSetOxyDetect(model, detect)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $detect"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetUserInfo.setOnClickListener {
            LpBleUtil.lewGetUserInfo(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetUserInfo.setOnClickListener {
            val info = UserInfo()
            info.aid = 12345
            info.uid = -1
            info.fName = "魑"
            info.name = "魅魍魉123"
            info.birthday = "1990-10-20"
            info.height = 170
            info.weight = 70f
            info.gender = 0
            Log.d("test12345", "lewSetUserInfo $info")
            LpBleUtil.lewSetUserInfo(model, info)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $info"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetPhonebook.setOnClickListener {
            LpBleUtil.lewGetPhoneBook(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetPhonebook.setOnClickListener {
            val list = PhoneBook()
            list.leftSize = 0
            list.currentSize = 3

            val item = PhoneBook.Item()
            item.id = 11111
            item.name = "张三里abc111"
            item.phone = "13420111867"
            val item2 = PhoneBook.Item()
            item2.id = 11112
            item2.name = "张三里abc112"
            item2.phone = "13420111867"
            val item3 = PhoneBook.Item()
            item3.id = 11113
            item3.name = "张三里abc113"
            item3.phone = "13420111867"

            list.items.add(item)
            list.items.add(item2)
            list.items.add(item3)
            Log.d("test12345", "lewSetPhonebook $list")
            LpBleUtil.lewSetPhoneBook(model, list)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $list"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSos.setOnClickListener {
            LpBleUtil.lewGetSosContact(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetSos.setOnClickListener {
            val sos = SosContact()
            switchState = !switchState
            sos.switch = switchState

            val item = SosContact.Item()
            item.name = "张三里abc111"
            item.phone = "13420111867"
            state++
            if (state > LewBleCmd.RelationShip.OTHER) {
                state = LewBleCmd.RelationShip.FATHER
            }
            item.relation = state
            val item2 = SosContact.Item()
            item2.name = "张三里abc112"
            item2.phone = "13420111867"
            state++
            if (state > LewBleCmd.RelationShip.OTHER) {
                state = LewBleCmd.RelationShip.FATHER
            }
            item2.relation = state

            sos.items.add(item)
//            sos.items.add(item2)
            Log.d("test12345", "lewSetSos $sos")
            LpBleUtil.lewSetSosContact(model, sos)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $sos"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetDial.setOnClickListener {
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetDial.setOnClickListener {
            // ???
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSecondScreen.setOnClickListener {
            LpBleUtil.lewGetSecondScreen(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetSecondScreen.setOnClickListener {
            val screen = SecondScreen()
            switchState = !switchState
            screen.medicineRemind = switchState
            screen.weather = switchState
            switchState = !switchState
            screen.clock = switchState
            screen.heartRate = switchState
            switchState = !switchState
            screen.spo2 = switchState
            screen.peripherals = switchState
            Log.d("test12345", "lewSetSecondScreen $screen")
            LpBleUtil.lewSetSecondScreen(model, screen)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $screen"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetCards.setOnClickListener {
            LpBleUtil.lewGetCards(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetCards.setOnClickListener {
            val cards = intArrayOf(LewBleCmd.Cards.HR, LewBleCmd.Cards.TARGET, LewBleCmd.Cards.WEATHER)
            Log.d("test12345", "lewSetCards ${Arrays.toString(cards)}")
            LpBleUtil.lewSetCards(model, cards)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n ${Arrays.toString(cards)}"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSportData.setOnClickListener {
            LpBleUtil.lewGetFileList(model, LewBleCmd.ListType.SPORT, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetEcgData.setOnClickListener {
            LpBleUtil.lewGetFileList(model, LewBleCmd.ListType.ECG, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetRtData.setOnClickListener {
            LpBleUtil.lewGetRtData(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetOxyData.setOnClickListener {
            LpBleUtil.lewGetFileList(model, LewBleCmd.ListType.OXY, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetHrData.setOnClickListener {
            LpBleUtil.lewGetFileList(model, LewBleCmd.ListType.HR, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetSleepData.setOnClickListener {
            LpBleUtil.lewGetFileList(model, LewBleCmd.ListType.SLEEP, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetHrThreshold.setOnClickListener {
            LpBleUtil.lewGetHrThreshold(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetHrThreshold.setOnClickListener {
            val threshold = HrThreshold()
            switchState = !switchState
            threshold.switch = switchState
            threshold.threshold = 100
            Log.d("test12345", "lewSetHrThreshold $threshold")
            LpBleUtil.lewSetHrThreshold(model, threshold)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $threshold"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewGetOxyThreshold.setOnClickListener {
            LpBleUtil.lewGetOxyThreshold(model)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)}"
            binding.sendCmd.text = cmdStr
        }
        binding.lewLayout.lewSetOxyThreshold.setOnClickListener {
            val threshold = OxyThreshold()
            switchState = !switchState
            threshold.switch = switchState
            threshold.threshold = 90
            Log.d("test12345", "lewSetOxyThreshold $threshold")
            LpBleUtil.lewSetOxyThreshold(model, threshold)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(model)} \n $threshold"
            binding.sendCmd.text = cmdStr
        }
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseContent)
            .observe(owner) {
                binding.responseCmd.text = "receive : ${bytesToHex(it)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewDeviceInfo)
            .observe(owner) {
                val data = it.data as DeviceInfo
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "请求绑定 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewUnBoundDevice)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "请求解绑 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFindPhone)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "查找手机 $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceNetwork)
            .observe(owner) {
                val data = it.data as DeviceNetwork
                binding.content.text = "$data"
                _toast.value = "获取联网模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBatteryInfo)
            .observe(owner) {
                val data = it.data as BatteryInfo
                binding.content.text = "$data"
                _toast.value =  "获取电量成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTime)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "设置时间 : $data"
                _toast.value = "设置时间成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTime)
            .observe(owner) {
                val data = it.data as TimeData
                binding.content.text = "$data"
                _toast.value = "获取时间成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSystemSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置系统配置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSystemSetting)
            .observe(owner) {
                val data = it.data as SystemSetting
                binding.content.text = "$data"
                _toast.value = "获取系统配置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLanguageSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置语言成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLanguageSetting)
            .observe(owner) {
                val data = it.data as Int
                binding.content.text = "$data"
                _toast.value = "获取语言成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUnitSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置单位成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUnitSetting)
            .observe(owner) {
                val data = it.data as UnitSetting
                binding.content.text = "$data"
                _toast.value = "获取单位成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHandRaiseSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置翻腕亮屏成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHandRaiseSetting)
            .observe(owner) {
                val data = it.data as HandRaiseSetting
                binding.content.text = "$data"
                _toast.value = "获取翻腕亮屏成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLrHandSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置左右手成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLrHandSetting)
            .observe(owner) {
                val data = it.data as Int
                binding.content.text = "$data"
                _toast.value = "获取左右手成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetNoDisturbMode)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置勿扰模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetNoDisturbMode)
            .observe(owner) {
                val data = it.data as NoDisturbMode
                binding.content.text = "$data"
                _toast.value = "获取勿扰模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAppSwitch)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置app提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAppSwitch)
            .observe(owner) {
                val data = it.data as AppSwitch
                binding.content.text = "$data"
                _toast.value = "获取app提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSendNotification)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "发送消息成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceMode)
            .observe(owner) {
                val data = it.data as Int
                binding.content.text = "$data"
                _toast.value = "获取设备模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetDeviceMode)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置设备模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAlarmClock)
            .observe(owner) {
                val data = it.data as AlarmClockInfo
                binding.content.text = "$data"
                _toast.value = "获取闹钟成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAlarmClock)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置闹钟成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneSwitch)
            .observe(owner) {
                val data = it.data as PhoneSwitch
                binding.content.text = "$data"
                _toast.value = "获取手机提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneSwitch)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置手机提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewPhoneCall)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "已挂断"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMeasureSetting)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置测量配置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMeasureSetting)
            .observe(owner) {
                val data = it.data as MeasureSetting
                binding.content.text = "$data"
                _toast.value = "获取测量配置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSportTarget)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置运动目标值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSportTarget)
            .observe(owner) {
                val data = it.data as SportTarget
                binding.content.text = "$data"
                _toast.value = "获取运动目标值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTargetRemind)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置达标提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTargetRemind)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "获取达标提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMedicineRemind)
            .observe(owner) {
                val data = it.data as MedicineRemind
                binding.content.text = "$data"
                _toast.value = "获取用药提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMedicineRemind)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置用药提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSittingRemind)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置久坐提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSittingRemind)
            .observe(owner) {
                val data = it.data as SittingRemind
                binding.content.text = "$data"
                _toast.value = "获取久坐提醒成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrDetect)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置自动心率成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrDetect)
            .observe(owner) {
                val data = it.data as HrDetect
                binding.content.text = "$data"
                _toast.value = "获取自测心率成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyDetect)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置自动血氧成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyDetect)
            .observe(owner) {
                val data = it.data as HrDetect
                binding.content.text = "$data"
                _toast.value = "获取自测血氧成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUserInfo)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置用户信息成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUserInfo)
            .observe(owner) {
                val data = it.data as UserInfo
                binding.content.text = "$data"
                _toast.value = "获取用户信息成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneBook)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置通讯录成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneBook)
            .observe(owner) {
                val data = it.data as PhoneBook
                binding.content.text = "$data"
                _toast.value = "获取通讯录成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSosContact)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置紧急联系人成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSosContact)
            .observe(owner) {
                val data = it.data as SosContact
                binding.content.text = "$data"
                _toast.value = "获取紧急联系人成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSecondScreen)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置副屏成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSecondScreen)
            .observe(owner) {
                val data = it.data as SecondScreen
                binding.content.text = "$data"
                _toast.value = "获取副屏设置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetCards)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置卡片成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetCards)
            .observe(owner) {
                val data = it.data as IntArray
                binding.content.text = "${data.joinToString()}"
                _toast.value = "获取卡片成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrThreshold)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置心率阈值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrThreshold)
            .observe(owner) {
                val data = it.data as HrThreshold
                binding.content.text = "$data"
                _toast.value = "获取心率阈值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyThreshold)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                _toast.value = "设置血氧阈值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyThreshold)
            .observe(owner) {
                val data = it.data as OxyThreshold
                binding.content.text = "$data"
                _toast.value = "获取血氧阈值成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewRtData)
            .observe(owner) {
                val data = it.data as RtData
                binding.content.text = "$data"
                _toast.value = "获取实时数据成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList)
            .observe(owner) {
                val data = it.data as LewBleResponse.FileList
                when (data.type) {
                    LewBleCmd.ListType.SPORT -> {
                        val list = SportList(data.listSize, data.content)
                        binding.content.text = "$list"
                        _toast.value = "获取运动列表成功"
                    }
                    LewBleCmd.ListType.ECG -> {
                        val list = EcgList(data.listSize, data.content)
                        binding.content.text = "$list"
                        _toast.value = "获取心电列表成功"
                    }
                    LewBleCmd.ListType.HR -> {
                        val list = HrList(data.listSize, data.content)
                        binding.content.text = "$list"
                        _toast.value = "获取心率列表成功"
                    }
                    LewBleCmd.ListType.OXY -> {
                        val list = OxyList(data.listSize, data.content)
                        binding.content.text = "$list"
                        _toast.value = "获取血氧列表成功"
                    }
                    LewBleCmd.ListType.SLEEP -> {
                        val list = SleepList(data.listSize, data.content)
                        binding.content.text = "$list"
                        _toast.value = "获取睡眠列表成功"
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
            .observe(owner) {
                val data = it.data as LewBleResponse.EcgFile
                binding.content.text = "${data.fileName} ${bytesToHex(data.content)}"
                _toast.value = "获取心电文件成功"
            }
    }

}