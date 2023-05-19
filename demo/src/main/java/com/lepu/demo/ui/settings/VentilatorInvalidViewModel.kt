package com.lepu.demo.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LpBleCmd
import com.lepu.blepro.ble.cmd.VentilatorBleCmd
import com.lepu.blepro.ble.cmd.VentilatorBleResponse
import com.lepu.blepro.ble.cmd.ResponseError
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.ventilator.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil
import kotlin.math.max
import kotlin.math.min

class VentilatorInvalidViewModel : SettingViewModel() {

    /*private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context
    private var systemSetting = SystemSetting()
    private var measureSetting = MeasureSetting()
    private var ventilationSetting = VentilationSetting()
    private var warningSetting = WarningSetting()
    private lateinit var rtState: VentilatorBleResponse.RtState
    private var spinnerSet = true

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int, mainViewModel: MainViewModel) {
        this.context = context
        this.binding = binding
        binding.ventilatorLayout.systemSetting.setOnClickListener {
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.VISIBLE
            binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            spinnerSet = false
            LpBleUtil.ventilatorGetSystemSetting(model)
        }
        binding.ventilatorLayout.measureSetting.setOnClickListener {
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.VISIBLE
            binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            spinnerSet = false
            LpBleUtil.ventilatorGetVentilationSetting(model)
            LpBleUtil.ventilatorGetMeasureSetting(model)
        }
        binding.ventilatorLayout.ventilationSetting.setOnClickListener {
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.VISIBLE
            binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            LpBleUtil.ventilatorGetRtState(model)
            spinnerSet = false
            LpBleUtil.ventilatorGetVentilationSetting(model)
        }
        binding.ventilatorLayout.warningSetting.setOnClickListener {
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.VISIBLE
            binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            spinnerSet = false
            LpBleUtil.ventilatorGetWarningSetting(model)
        }
        binding.ventilatorLayout.otherSetting.setOnClickListener {
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.GONE
            LpBleUtil.ventilatorGetRtState(model)
        }
        // 绑定/解绑
        binding.ventilatorLayout.bound.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.ventilatorDeviceBound(model, isChecked)
        }
        // 进入医生模式
        binding.ventilatorLayout.doctorMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    var pin = binding.ventilatorLayout.pin.text.toString()
                    pin = if (pin == "") {
                        "0319"
                    } else {
                        pin
                    }
                    LpBleUtil.ventilatorDoctorModeIn(model, pin, System.currentTimeMillis().div(1000))
                } else {
                    LpBleUtil.ventilatorDoctorModeOut(model)
                }
            }
        }
        // 系统设置
        // 单位设置
        binding.ventilatorLayout.systemSettingUnitSet.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.UNIT
            systemSetting.unitSetting.pressureUnit = binding.ventilatorLayout.systemSettingUnit.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 语言设置
        binding.ventilatorLayout.systemSettingLanguageSet.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.LANGUAGE
            systemSetting.languageSetting.language = binding.ventilatorLayout.systemSettingLanguage.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 屏幕设置：屏幕亮度
        // 屏幕设置：自动熄屏
        binding.ventilatorLayout.systemSettingScreenSet.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.SCREEN
            systemSetting.screenSetting.brightness = binding.ventilatorLayout.systemSettingScreenBrightness.text.toString().toFloat().toInt()
            systemSetting.screenSetting.autoOff = binding.ventilatorLayout.systemSettingScreenAutoOff.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：过滤棉
        // 耗材设置：面罩
        // 耗材设置：管道
        // 耗材设置：水箱
        binding.ventilatorLayout.systemSettingReplacementSet.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.filter = binding.ventilatorLayout.systemSettingReplacementFilter.text.toString().toFloat().toInt()
            systemSetting.replacements.mask = binding.ventilatorLayout.systemSettingReplacementMask.text.toString().toFloat().toInt()
            systemSetting.replacements.tube = binding.ventilatorLayout.systemSettingReplacementTube.text.toString().toFloat().toInt()
            systemSetting.replacements.tank = binding.ventilatorLayout.systemSettingReplacementTank.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 音量设置
        binding.ventilatorLayout.systemSettingVolumeSet.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.VOLUME
            systemSetting.volumeSetting.volume = binding.ventilatorLayout.systemSettingVolume.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 测量设置
        // 湿化等级
        binding.ventilatorLayout.measureSettingHumidificationSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.HUMIDIFICATION
            measureSetting.humidification.humidification = binding.ventilatorLayout.measureSettingHumidification.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 呼吸压力释放：呼气压力释放
        binding.ventilatorLayout.measureSettingPressureEprSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.PRESSURE_REDUCE
            measureSetting.pressureReduce.epr = binding.ventilatorLayout.measureSettingPressureEpr.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 自动启停：自动启动
        // 自动启停：自动停止
        binding.ventilatorLayout.measureSettingAutoSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.AUTO_SWITCH
            measureSetting.autoSwitch.autoStart = binding.ventilatorLayout.measureSettingAutoOn.text.toString().toFloat().toInt() == 1
            measureSetting.autoSwitch.autoEnd = binding.ventilatorLayout.measureSettingAutoOff.text.toString().toFloat().toInt() == 1
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 预加热
        binding.ventilatorLayout.measureSettingPreHeatSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.PRE_HEAT
            measureSetting.preHeat.on = binding.ventilatorLayout.measureSettingPreHeat.text.toString().toFloat().toInt() == 1
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 缓冲压力
        // 缓冲时间
        binding.ventilatorLayout.measureSettingRampSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
            measureSetting.ramp.pressure = binding.ventilatorLayout.measureSettingRampPressure.text.toString().toFloat()
            measureSetting.ramp.time = binding.ventilatorLayout.measureSettingRampTime.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 管道类型
        binding.ventilatorLayout.measureSettingTubeSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.TUBE_TYPE
            measureSetting.tubeType.type = binding.ventilatorLayout.measureSettingTube.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 面罩类型
        binding.ventilatorLayout.measureSettingMaskSet.setOnClickListener {
            measureSetting.type = VentilatorBleCmd.MeasureSetting.MASK
            measureSetting.mask.type = binding.ventilatorLayout.measureSettingMask.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 通气设置
        // 通气模式
        binding.ventilatorLayout.ventilationModeInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.VENTILATION_MODE
            ventilationSetting.ventilationMode.mode = binding.ventilatorLayout.ventilationModeInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // CPAP模式压力
        binding.ventilatorLayout.cpapPressureInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = binding.ventilatorLayout.cpapPressureInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最大值Pmax
        binding.ventilatorLayout.apapPressureMaxInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = binding.ventilatorLayout.apapPressureMaxInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最小值Pmin
        binding.ventilatorLayout.apapPressureMinInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = binding.ventilatorLayout.apapPressureMinInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气压力
        binding.ventilatorLayout.ipapPressureInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = binding.ventilatorLayout.ipapPressureInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼气压力
        binding.ventilatorLayout.epapPressureInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = binding.ventilatorLayout.epapPressureInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气时间
        binding.ventilatorLayout.inspiratoryTimeInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = binding.ventilatorLayout.inspiratoryTimeInvalid.text.toString().toFloat()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼吸频率
        binding.ventilatorLayout.respiratoryFrequencyInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = binding.ventilatorLayout.respiratoryFrequencyInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 压力上升时间
        binding.ventilatorLayout.raiseTimeInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = binding.ventilatorLayout.raiseTimeInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气触发灵敏度
        binding.ventilatorLayout.iTriggerInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_SENSITIVE
            ventilationSetting.inhaleSensitive.sentive = binding.ventilatorLayout.iTriggerInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼气触发灵敏度
        binding.ventilatorLayout.eTriggerInvalidSet.setOnClickListener {
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.EXHALE_SENSITIVE
            ventilationSetting.exhaleSensitive.sentive = binding.ventilatorLayout.eTriggerInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 报警设置
        // 漏气量高
        binding.ventilatorLayout.leakHighInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.LEAK_HIGH
            warningSetting.warningLeak.high = binding.ventilatorLayout.leakHighInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸暂停
        binding.ventilatorLayout.apneaInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.APNEA
            warningSetting.warningApnea.apnea = binding.ventilatorLayout.apneaInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 潮气量低
        binding.ventilatorLayout.vtLowInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.VT_LOW
            warningSetting.warningVt.low = binding.ventilatorLayout.vtLowInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 分钟通气量低
        binding.ventilatorLayout.lowVentilationInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = binding.ventilatorLayout.lowVentilationInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率高
        binding.ventilatorLayout.rrHighInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = binding.ventilatorLayout.rrHighInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率低
        binding.ventilatorLayout.rrLowInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_LOW
            warningSetting.warningRrLow.low = binding.ventilatorLayout.rrLowInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 血氧饱和度低
        binding.ventilatorLayout.spo2LowInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = binding.ventilatorLayout.spo2LowInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率高
        binding.ventilatorLayout.hrHighInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = binding.ventilatorLayout.hrHighInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率低
        binding.ventilatorLayout.hrLowInvalidSet.setOnClickListener {
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_LOW
            warningSetting.warningHrLow.low = binding.ventilatorLayout.hrLowInvalid.text.toString().toFloat().toInt()
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound)
            .observe(owner) {
                val data = it.data as Int
                binding.ventilatorLayout.bound.isChecked = data == 0
                _toast.value = when (data) {
                    0 -> "绑定成功"
                    1 -> "绑定失败"
                    2 -> "绑定超时"
                    else -> "绑定消息"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceUnBound)
            .observe(owner) {
                _toast.value = "解绑成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode)
            .observe(owner) {
                val data = it.data as VentilatorBleResponse.DoctorModeResult
                _toast.value = if (data.success) {
                    if (data.isOut) {
                        "退出医生模式成功"
                    } else {
                        "进入医生模式成功"
                    }
                } else {
                    if (data.isOut) {
                        "退出医生模式失败, ${when (data.errCode) {
                            1 -> "设备处于医生模式"
                            2 -> "设备处于医生模式（BLE）"
                            3 -> "设备处于医生模式（Socket）"
                            5 -> "设备处于患者模式"
                            else -> ""
                        }}"
                    } else {
                        "进入医生模式失败, ${when (data.errCode) {
                            1 -> "设备处于医生模式"
                            2 -> "设备处于医生模式（BLE）"
                            3 -> "设备处于医生模式（Socket）"
                            4 -> context.getString(R.string.password_error)
                            else -> ""
                        }}"
                    }
                }
                LpBleUtil.ventilatorGetRtState(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtState)
            .observe(owner) {
                val data = it.data as VentilatorBleResponse.RtState
                rtState = data
                binding.ventilatorLayout.doctorMode.isChecked = data.deviceMode != 0
                binding.ventilatorLayout.deviceMode.text = "${when (data.deviceMode) {
                    0 -> "(设备处于患者模式)"
                    1 -> "(设备端医生模式)"
                    2 -> "(BLE端医生模式)"
                    3 -> "(Socket端医生模式)"
                    else -> ""
                }}"
                layoutGone()
                layoutVisible(data.ventilationMode)
                if (data.deviceMode == 2) {
                    binding.ventilatorLayout.ventilationSetting.visibility = View.VISIBLE
                    binding.ventilatorLayout.warningSetting.visibility = View.VISIBLE
                } else {
                    binding.ventilatorLayout.ventilationSetting.visibility = View.GONE
                    binding.ventilatorLayout.ventilationSettingLayoutInvalid.visibility = View.GONE
                    binding.ventilatorLayout.warningSetting.visibility = View.GONE
                    binding.ventilatorLayout.warningSettingLayoutInvalid.visibility = View.GONE
                }
                if (data.isVentilated) {
                    binding.ventilatorLayout.systemSetting.visibility = View.GONE
                    binding.ventilatorLayout.systemSettingLayoutInvalid.visibility = View.GONE
                    binding.ventilatorLayout.measureSetting.visibility = View.GONE
                    binding.ventilatorLayout.measureSettingLayoutInvalid.visibility = View.GONE
                } else {
                    binding.ventilatorLayout.systemSetting.visibility = View.VISIBLE
                    binding.ventilatorLayout.measureSetting.visibility = View.VISIBLE
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting)
            .observe(owner) {
                systemSetting = it.data as SystemSetting
                binding.ventilatorLayout.systemSettingUnit.setText("${systemSetting.unitSetting.pressureUnit}")
                binding.ventilatorLayout.systemSettingLanguage.setText("${systemSetting.languageSetting.language}")
                binding.ventilatorLayout.systemSettingScreenBrightness.setText("${systemSetting.screenSetting.brightness}")
                binding.ventilatorLayout.systemSettingScreenAutoOff.setText("${systemSetting.screenSetting.autoOff}")
                binding.ventilatorLayout.systemSettingReplacementFilter.setText("${systemSetting.replacements.filter}")
                binding.ventilatorLayout.systemSettingReplacementMask.setText("${systemSetting.replacements.mask}")
                binding.ventilatorLayout.systemSettingReplacementTube.setText("${systemSetting.replacements.tube}")
                binding.ventilatorLayout.systemSettingReplacementTank.setText("${systemSetting.replacements.tank}")
                binding.ventilatorLayout.systemSettingVolume.setText("${systemSetting.volumeSetting.volume}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting)
            .observe(owner) {
                _toast.value = "系统设置成功"
                LpBleUtil.ventilatorGetSystemSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting)
            .observe(owner) {
                spinnerSet = false
                measureSetting = it.data as MeasureSetting
                binding.ventilatorLayout.measureSettingHumidification.setText("${measureSetting.humidification.humidification}")
                binding.ventilatorLayout.ipapPressureInvalid.setText("${measureSetting.pressureReduce.ipr}")
                binding.ventilatorLayout.epapPressureInvalid.setText("${measureSetting.pressureReduce.epr}")
                binding.ventilatorLayout.measureSettingAutoOn.setText("${measureSetting.autoSwitch.autoStart}")
                binding.ventilatorLayout.measureSettingAutoOff.setText("${measureSetting.autoSwitch.autoEnd}")
                binding.ventilatorLayout.measureSettingPreHeat.setText("${measureSetting.preHeat.on}")
                binding.ventilatorLayout.measureSettingRampPressure.setText("${measureSetting.ramp.pressure}")
                binding.ventilatorLayout.measureSettingRampTime.setText("${measureSetting.ramp.time}")
                binding.ventilatorLayout.measureSettingTube.setText("${measureSetting.tubeType.type}")
                binding.ventilatorLayout.measureSettingMask.setText("${measureSetting.mask.type}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting)
            .observe(owner) {
                _toast.value = "测量设置成功"
                LpBleUtil.ventilatorGetMeasureSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting)
            .observe(owner) {
                spinnerSet = false
                ventilationSetting = it.data as VentilationSetting
                binding.ventilatorLayout.ventilationModeInvalid.setText("${ventilationSetting.ventilationMode.mode}")
                layoutGone()
                layoutVisible(ventilationSetting.ventilationMode.mode)
                binding.ventilatorLayout.cpapPressureInvalid.setText("${ventilationSetting.cpapPressure.pressure}")
                binding.ventilatorLayout.apapPressureMaxInvalid.setText("${ventilationSetting.apapPressureMax.max}")
                binding.ventilatorLayout.apapPressureMinInvalid.setText("${ventilationSetting.apapPressureMin.min}")
                binding.ventilatorLayout.ipapPressureInvalid.setText("${ventilationSetting.pressureInhale.inhale}")
                binding.ventilatorLayout.epapPressureInvalid.setText("${ventilationSetting.pressureExhale.exhale}")
                binding.ventilatorLayout.raiseTimeInvalid.setText("${ventilationSetting.pressureRaiseDuration.duration}")
                binding.ventilatorLayout.inspiratoryTimeInvalid.setText("${ventilationSetting.inhaleDuration.duration}")
                binding.ventilatorLayout.respiratoryFrequencyInvalid.setText("${ventilationSetting.respiratoryRate.rate}")
                binding.ventilatorLayout.iTriggerInvalid.setText("${ventilationSetting.inhaleSensitive.sentive}")
                binding.ventilatorLayout.eTriggerInvalid.setText("${ventilationSetting.exhaleSensitive.sentive}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting)
            .observe(owner) {
                _toast.value = "通气设置成功"
                LpBleUtil.ventilatorGetVentilationSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting)
            .observe(owner) {
                spinnerSet = false
                warningSetting = it.data as WarningSetting
                binding.ventilatorLayout.leakHighInvalid.setText("${warningSetting.warningLeak.high}")
                binding.ventilatorLayout.lowVentilationInvalid.setText("${warningSetting.warningVentilation.low}")
                binding.ventilatorLayout.vtLowInvalid.setText("${warningSetting.warningVt.low}")
                binding.ventilatorLayout.rrHighInvalid.setText("${warningSetting.warningRrHigh.high}")
                binding.ventilatorLayout.rrLowInvalid.setText("${warningSetting.warningRrLow.low}")
                binding.ventilatorLayout.spo2LowInvalid.setText("${warningSetting.warningSpo2Low.low}")
                binding.ventilatorLayout.hrHighInvalid.setText("${warningSetting.warningHrHigh.high}")
                binding.ventilatorLayout.hrLowInvalid.setText("${warningSetting.warningHrLow.low}")
                binding.ventilatorLayout.apneaInvalid.setText("${warningSetting.warningApnea.apnea}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting)
            .observe(owner) {
                _toast.value = "警告设置成功"
                LpBleUtil.ventilatorGetWarningSetting(it.model)
            }
        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError)
            .observe(owner) {
                when (it.type) {
                    LpBleCmd.TYPE_FILE_NOT_FOUND -> _toast.value = "找不到文件"
                    LpBleCmd.TYPE_FILE_READ_FAILED -> _toast.value = "读文件失败"
                    LpBleCmd.TYPE_FILE_WRITE_FAILED -> _toast.value = "写文件失败"
                    LpBleCmd.TYPE_FIRMWARE_UPDATE_FAILED -> _toast.value = "固件升级失败"
                    LpBleCmd.TYPE_LANGUAGE_UPDATE_FAILED -> _toast.value = "语言包升级失败"
                    LpBleCmd.TYPE_PARAM_ILLEGAL -> _toast.value = "参数不合法"
                    LpBleCmd.TYPE_PERMISSION_DENIED -> _toast.value = "权限不足"
                    LpBleCmd.TYPE_DECRYPT_FAILED -> {
                        _toast.value = "解密失败，断开连接"
                        LpBleUtil.disconnect(false)
                    }
                    LpBleCmd.TYPE_DEVICE_BUSY -> _toast.value = "设备资源被占用/设备忙"
                    LpBleCmd.TYPE_CMD_FORMAT_ERROR -> _toast.value = "指令格式错误"
                    LpBleCmd.TYPE_CMD_NOT_SUPPORTED -> _toast.value = "不支持指令"
                    LpBleCmd.TYPE_NORMAL_ERROR -> _toast.value = "通用错误"
                }
            }
    }

    private fun layoutGone() {
        binding.ventilatorLayout.eprLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.cpapPressureLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.apapPressureMaxLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.apapPressureMinLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.ipapPressureLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.epapPressureLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.raiseTimeLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.iTriggerLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.eTriggerLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.inspiratoryTimeLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.respiratoryFrequencyLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.lowVentilationLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.vtLowLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.rrHighLayoutInvalid.visibility = View.GONE
        binding.ventilatorLayout.rrLowLayoutInvalid.visibility = View.GONE
    }
    private fun layoutVisible(mode: Int) {
        when (mode) {
            // CPAP
            0 -> {
                binding.ventilatorLayout.eprLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.cpapPressureLayoutInvalid.visibility = View.VISIBLE
            }
            // APAP
            1 -> {
                binding.ventilatorLayout.eprLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.apapPressureMaxLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.apapPressureMinLayoutInvalid.visibility = View.VISIBLE
            }
            // S
            2 -> {
                binding.ventilatorLayout.ipapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.iTriggerLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.eTriggerLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayoutInvalid.visibility = View.VISIBLE
            }
            // S/T
            3 -> {
                binding.ventilatorLayout.ipapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.inspiratoryTimeLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.respiratoryFrequencyLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.iTriggerLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.eTriggerLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayoutInvalid.visibility = View.VISIBLE
            }
            // T
            4 -> {
                binding.ventilatorLayout.ipapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.inspiratoryTimeLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.respiratoryFrequencyLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayoutInvalid.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayoutInvalid.visibility = View.VISIBLE
            }
        }
    }*/

}