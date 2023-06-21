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
import com.lepu.blepro.ble.data.FwUpdate
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
import org.apache.commons.io.IOUtils
import kotlin.math.max
import kotlin.math.min

class VentilatorViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
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
        binding.ventilatorLayout.factoryInfo.setOnClickListener {
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.systemSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
        }
        binding.ventilatorLayout.systemSetting.setOnClickListener {
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSettingLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayout.visibility = View.GONE
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
            binding.ventilatorLayout.measureSettingLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.GONE
            binding.ventilatorLayout.systemSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            spinnerSet = false
            LpBleUtil.ventilatorGetWarningSetting(model)
            LpBleUtil.ventilatorGetRtState(model)
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
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.GONE
            binding.ventilatorLayout.systemSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.otherLayout.visibility = View.GONE
            spinnerSet = false
            LpBleUtil.ventilatorGetRtState(model)
            LpBleUtil.ventilatorGetVentilationSetting(model)
        }
        binding.ventilatorLayout.warningSetting.setOnClickListener {
            binding.ventilatorLayout.warningSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.ventilatorLayout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.otherSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.ventilatorLayout.warningSettingLayout.visibility = View.VISIBLE
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.GONE
            binding.ventilatorLayout.systemSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.GONE
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
            binding.ventilatorLayout.factoryInfoLayout.visibility = View.GONE
            binding.ventilatorLayout.systemSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.measureSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.ventilationSettingLayout.visibility = View.GONE
            binding.ventilatorLayout.warningSettingLayout.visibility = View.GONE
            LpBleUtil.ventilatorGetRtState(model)
        }
        binding.ventilatorLayout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.ventilatorLayout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.btpLayout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.ventilatorLayout.code.text.toString())
            if (tempCode.isNullOrEmpty()) {
                enableCode = false
            } else if (tempCode.length == 8) {
                config.setBranchCode(tempCode)
            } else {
                _toast.value = "code请输入8位"
                return@setOnClickListener
            }
            config.setBurnFlag(enableSn, enableVersion, enableCode)
            LpBleUtil.burnFactoryInfo(model, config)
            val deviceFactoryData = DeviceFactoryData()
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            _deviceFactoryData.value = deviceFactoryData
        }
        // 升级
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("选择", "bootloader", "app", "language")).apply {
            binding.ventilatorLayout.fwUpdate.adapter = this
        }
        binding.ventilatorLayout.fwUpdate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> {
                        val fwUpdate = FwUpdate()
                        fwUpdate.deviceType = 0x3631
                        fwUpdate.curMask = 1
                        fwUpdate.setMask(true, false, false)
                        fwUpdate.data = IOUtils.toByteArray(context.assets.open("update/update_XINANBAO_Bootloader.bin"))
                        fwUpdate.size = fwUpdate.data.size
                        LpBleUtil.ventilatorFwUpdate(model, fwUpdate)
                    }
                    2 -> {
                        val fwUpdate = FwUpdate()
                        fwUpdate.deviceType = 0x3631
                        fwUpdate.curMask = 2
                        fwUpdate.setMask(false, true, false)
                        fwUpdate.data = IOUtils.toByteArray(context.assets.open("update/update_XINANBAO_APP.bin"))
                        fwUpdate.size = fwUpdate.data.size
                        LpBleUtil.ventilatorFwUpdate(model, fwUpdate)
                    }
                    3 -> {
                        val fwUpdate = FwUpdate()
                        fwUpdate.deviceType = 0x3631
                        fwUpdate.curMask = 4
                        fwUpdate.setMask(false, false, true)
                        fwUpdate.data = IOUtils.toByteArray(context.assets.open("update/update_XINANBAO_Language.bin"))
                        fwUpdate.size = fwUpdate.data.size
                        LpBleUtil.ventilatorFwUpdate(model, fwUpdate)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
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
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("cmH2O", "hPa")).apply {
            binding.ventilatorLayout.unit.adapter = this
        }
        binding.ventilatorLayout.unit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.UNIT
                    systemSetting.unitSetting.pressureUnit = position
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 语言设置
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("英文", "中文")).apply {
            binding.ventilatorLayout.language.adapter = this
        }
        binding.ventilatorLayout.language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.LANGUAGE
                    systemSetting.languageSetting.language = position
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 屏幕设置：屏幕亮度
        binding.ventilatorLayout.brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.SCREEN
                    systemSetting.screenSetting.brightness = progress
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.brightnessProcess.text = "$progress %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.brightnessRange.text = "范围：${binding.ventilatorLayout.brightness.min}% - ${binding.ventilatorLayout.brightness.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.brightnessSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.SCREEN
            systemSetting.screenSetting.brightness = --binding.ventilatorLayout.brightness.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.brightnessAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.SCREEN
            systemSetting.screenSetting.brightness = ++binding.ventilatorLayout.brightness.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 屏幕设置：自动熄屏
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("常亮", "30秒", "60秒", "90秒", "120秒")).apply {
            binding.ventilatorLayout.screenOff.adapter = this
        }
        binding.ventilatorLayout.screenOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.SCREEN
                    systemSetting.screenSetting.autoOff = position.times(30)
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 耗材设置：过滤棉
        binding.ventilatorLayout.filter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
                    systemSetting.replacements.filter = progress
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.filterProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.ventilatorLayout.filterRange.text = "范围：关 - ${binding.ventilatorLayout.filter.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.filterSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.filter = --binding.ventilatorLayout.filter.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.filterAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.filter = ++binding.ventilatorLayout.filter.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：面罩
        binding.ventilatorLayout.mask.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
                    systemSetting.replacements.mask = progress
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.maskProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.ventilatorLayout.maskRange.text = "范围：关 - ${binding.ventilatorLayout.mask.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.maskSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.mask = --binding.ventilatorLayout.mask.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.maskAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.mask = ++binding.ventilatorLayout.mask.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：管道
        binding.ventilatorLayout.tube.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
                    systemSetting.replacements.tube = progress
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.tubeProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "$progress 个月"
                }
                binding.ventilatorLayout.tubeRange.text = "范围：关 - ${binding.ventilatorLayout.tube.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.tubeSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.tube = --binding.ventilatorLayout.tube.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.tubeAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.tube = ++binding.ventilatorLayout.tube.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 耗材设置：水箱
        binding.ventilatorLayout.tank.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
                    systemSetting.replacements.tank = progress
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.tankProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 个月"
                }
                binding.ventilatorLayout.tankRange.text = "范围：关 - ${binding.ventilatorLayout.tank.max}个月"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.tankSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.tank = --binding.ventilatorLayout.tank.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.tankAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.REPLACEMENT
            systemSetting.replacements.tank = ++binding.ventilatorLayout.tank.progress
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 音量设置
        binding.ventilatorLayout.volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    systemSetting.type = VentilatorBleCmd.SystemSetting.VOLUME
                    systemSetting.volumeSetting.volume = progress.times(5)
                    LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
                }
                binding.ventilatorLayout.volumeProcess.text = "${progress.times(5)} %"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.volumeRange.text = "范围：${binding.ventilatorLayout.volume.min}% - ${binding.ventilatorLayout.volume.max}%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.volumeSub.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.VOLUME
            binding.ventilatorLayout.volume.progress--
            systemSetting.volumeSetting.volume = binding.ventilatorLayout.volume.progress.times(5)
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        binding.ventilatorLayout.volumeAdd.setOnClickListener {
            systemSetting.type = VentilatorBleCmd.SystemSetting.VOLUME
            binding.ventilatorLayout.volume.progress++
            systemSetting.volumeSetting.volume = binding.ventilatorLayout.volume.progress.times(5)
            LpBleUtil.ventilatorSetSystemSetting(model, systemSetting)
        }
        // 测量设置
        // 湿化等级
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3", "4", "5", "自动")).apply {
            binding.ventilatorLayout.humidification.adapter = this
        }
        binding.ventilatorLayout.humidification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.HUMIDIFICATION
                    if (position == 6) {
                        measureSetting.humidification.humidification = 0xff
                    } else {
                        measureSetting.humidification.humidification = position
                    }
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸压力释放：吸气压力释放
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3")).apply {
            binding.ventilatorLayout.ipr.adapter = this
        }
        binding.ventilatorLayout.ipr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.PRESSURE_REDUCE
                    measureSetting.pressureReduce.ipr = position
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸压力释放：呼气压力释放
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3")).apply {
            binding.ventilatorLayout.epr.adapter = this
        }
        binding.ventilatorLayout.epr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.PRESSURE_REDUCE
                    measureSetting.pressureReduce.epr = position
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 自动启停：自动启动
        binding.ventilatorLayout.autoStart.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = VentilatorBleCmd.MeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.autoStart = isChecked
                LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 自动启停：自动停止
        binding.ventilatorLayout.autoEnd.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = VentilatorBleCmd.MeasureSetting.AUTO_SWITCH
                measureSetting.autoSwitch.autoEnd = isChecked
                LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 预加热
        binding.ventilatorLayout.preHeat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                measureSetting.type = VentilatorBleCmd.MeasureSetting.PRE_HEAT
                measureSetting.preHeat.on = isChecked
                LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
            }
        }
        // 缓冲压力
        binding.ventilatorLayout.rampPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
                    measureSetting.ramp.pressure = progress.times(0.5f)
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.ventilatorLayout.rampPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.rampPressureRange.text = "范围：${binding.ventilatorLayout.rampPressure.min.times(0.5f)} - ${binding.ventilatorLayout.rampPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.rampPressureSub.setOnClickListener {
            binding.ventilatorLayout.rampPressure.progress--
            measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
            measureSetting.ramp.pressure = binding.ventilatorLayout.rampPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.ventilatorLayout.rampPressureAdd.setOnClickListener {
            binding.ventilatorLayout.rampPressure.progress++
            measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
            measureSetting.ramp.pressure = binding.ventilatorLayout.rampPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 缓冲时间
        binding.ventilatorLayout.rampTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
                    measureSetting.ramp.time = progress.times(5)
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.ventilatorLayout.rampTimeProcess.text = "${progress.times(5)}min"
                binding.ventilatorLayout.rampTimeRange.text = "范围：关 - ${binding.ventilatorLayout.rampTime.max.times(5)}min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.rampTimeSub.setOnClickListener {
            binding.ventilatorLayout.rampTime.progress--
            measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
            measureSetting.ramp.time = binding.ventilatorLayout.rampTime.progress.times(5)
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.ventilatorLayout.rampTimeAdd.setOnClickListener {
            binding.ventilatorLayout.rampTime.progress++
            measureSetting.type = VentilatorBleCmd.MeasureSetting.RAMP
            measureSetting.ramp.time = binding.ventilatorLayout.rampTime.progress.times(5)
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 管道类型
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("15mm", "22mm")).apply {
            binding.ventilatorLayout.tubeType.adapter = this
        }
        binding.ventilatorLayout.tubeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.TUBE_TYPE
                    measureSetting.tubeType.type = position
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩类型
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("口鼻罩", "鼻罩", "鼻枕")).apply {
            binding.ventilatorLayout.maskType.adapter = this
        }
        binding.ventilatorLayout.maskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.MASK
                    measureSetting.mask.type = position
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩佩戴匹配测试压力
        binding.ventilatorLayout.maskPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    measureSetting.type = VentilatorBleCmd.MeasureSetting.MASK
                    measureSetting.mask.pressure = progress.toFloat()
                    LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
                }
                binding.ventilatorLayout.maskPressureProcess.text = "${progress.toFloat()}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.maskPressureRange.text = "范围：${binding.ventilatorLayout.maskPressure.min.times(1.0f)} - ${binding.ventilatorLayout.maskPressure.max.times(1.0f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.maskPressureSub.setOnClickListener {
            binding.ventilatorLayout.maskPressure.progress--
            measureSetting.type = VentilatorBleCmd.MeasureSetting.MASK
            measureSetting.mask.pressure = binding.ventilatorLayout.maskPressure.progress.toFloat()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        binding.ventilatorLayout.maskPressureAdd.setOnClickListener {
            binding.ventilatorLayout.maskPressure.progress++
            measureSetting.type = VentilatorBleCmd.MeasureSetting.MASK
            measureSetting.mask.pressure = binding.ventilatorLayout.maskPressure.progress.toFloat()
            LpBleUtil.ventilatorSetMeasureSetting(model, measureSetting)
        }
        // 通气设置
        // 通气模式
        val ventilatorModel = VentilatorModel(mainViewModel._er1Info.value?.branchCode)
        val modes = ArrayList<String>()
        if (ventilatorModel.isSupportCpap) {
            modes.add("CPAP")
        }
        if (ventilatorModel.isSupportApap) {
            modes.add("APAP")
        }
        if (ventilatorModel.isSupportS) {
            modes.add("S")
        }
        if (ventilatorModel.isSupportST) {
            modes.add("S/T")
        }
        if (ventilatorModel.isSupportT) {
            modes.add("T")
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("CPAP", "APAP", "S", "S/T", "T")).apply {
            binding.ventilatorLayout.ventilationMode.adapter = this
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, modes).apply {
            binding.ventilatorLayout.ventilationMode.adapter = this
        }
        binding.ventilatorLayout.ventilationMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.VENTILATION_MODE
                    ventilationSetting.ventilationMode.mode = position
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // CPAP模式压力
        binding.ventilatorLayout.cpapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE
                    ventilationSetting.cpapPressure.pressure = progress.times(0.5f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.cpapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.cpapPressureRange.text = "范围：${binding.ventilatorLayout.cpapPressure.min.times(0.5f)} - ${binding.ventilatorLayout.cpapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.cpapPressureSub.setOnClickListener {
            binding.ventilatorLayout.cpapPressure.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = binding.ventilatorLayout.cpapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.cpapPressureAdd.setOnClickListener {
            binding.ventilatorLayout.cpapPressure.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE
            ventilationSetting.cpapPressure.pressure = binding.ventilatorLayout.cpapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最大值Pmax
        binding.ventilatorLayout.apapPressureMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MAX
                    ventilationSetting.apapPressureMax.max = progress.times(0.5f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.apapPressureMaxProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.apapPressureMaxRange.text = "范围：${binding.ventilatorLayout.apapPressureMax.min.times(0.5f)} - ${binding.ventilatorLayout.apapPressureMax.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.apapPressureMaxSub.setOnClickListener {
            binding.ventilatorLayout.apapPressureMax.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = binding.ventilatorLayout.apapPressureMax.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.apapPressureMaxAdd.setOnClickListener {
            binding.ventilatorLayout.apapPressureMax.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MAX
            ventilationSetting.apapPressureMax.max = binding.ventilatorLayout.apapPressureMax.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // APAP模式压力最小值Pmin
        binding.ventilatorLayout.apapPressureMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MIN
                    ventilationSetting.apapPressureMin.min = progress.times(0.5f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.apapPressureMinProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.apapPressureMinRange.text = "范围：${binding.ventilatorLayout.apapPressureMin.min.times(0.5f)} - ${binding.ventilatorLayout.apapPressureMin.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.apapPressureMinSub.setOnClickListener {
            binding.ventilatorLayout.apapPressureMin.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = binding.ventilatorLayout.apapPressureMin.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.apapPressureMinAdd.setOnClickListener {
            binding.ventilatorLayout.apapPressureMin.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_MIN
            ventilationSetting.apapPressureMin.min = binding.ventilatorLayout.apapPressureMin.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气压力
        binding.ventilatorLayout.ipapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_INHALE
                    ventilationSetting.pressureInhale.inhale = progress.times(0.5f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.ipapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.ipapPressureRange.text = "范围：${binding.ventilatorLayout.ipapPressure.min.times(0.5f)} - ${binding.ventilatorLayout.ipapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.ipapPressureSub.setOnClickListener {
            binding.ventilatorLayout.ipapPressure.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = binding.ventilatorLayout.ipapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.ipapPressureAdd.setOnClickListener {
            binding.ventilatorLayout.ipapPressure.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_INHALE
            ventilationSetting.pressureInhale.inhale = binding.ventilatorLayout.ipapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼气压力
        binding.ventilatorLayout.epapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_EXHALE
                    ventilationSetting.pressureExhale.exhale = progress.times(0.5f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.epapPressureProcess.text = "${progress.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.epapPressureRange.text = "范围：${binding.ventilatorLayout.epapPressure.min.times(0.5f)} - ${binding.ventilatorLayout.epapPressure.max.times(0.5f)}${if (systemSetting.unitSetting.pressureUnit == 0) {
                        "cmH2O"
                    } else {
                        "hPa"
                    }}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.epapPressureSub.setOnClickListener {
            binding.ventilatorLayout.epapPressure.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = binding.ventilatorLayout.epapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.epapPressureAdd.setOnClickListener {
            binding.ventilatorLayout.epapPressure.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.PRESSURE_EXHALE
            ventilationSetting.pressureExhale.exhale = binding.ventilatorLayout.epapPressure.progress.times(0.5f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气时间
        binding.ventilatorLayout.inspiratoryTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_DURATION
                    ventilationSetting.inhaleDuration.duration = progress.div(10f)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.inspiratoryTimeProcess.text = "${progress.div(10f)}s"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.inspiratoryTimeRange.text = "范围：${String.format("%.1f", binding.ventilatorLayout.inspiratoryTime.min.times(0.1f))} - ${String.format("%.1f", binding.ventilatorLayout.inspiratoryTime.max.times(0.1f))}s"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.inspiratoryTimeSub.setOnClickListener {
            binding.ventilatorLayout.inspiratoryTime.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = binding.ventilatorLayout.inspiratoryTime.progress.div(10f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.inspiratoryTimeAdd.setOnClickListener {
            binding.ventilatorLayout.inspiratoryTime.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_DURATION
            ventilationSetting.inhaleDuration.duration = binding.ventilatorLayout.inspiratoryTime.progress.div(10f)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 呼吸频率
        binding.ventilatorLayout.respiratoryFrequency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RESPIRATORY_RATE
                    ventilationSetting.respiratoryRate.rate = progress
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.respiratoryFrequencyProcess.text = "$progress bpm"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.respiratoryFrequencyRange.text = "范围：${binding.ventilatorLayout.respiratoryFrequency.min} - ${binding.ventilatorLayout.respiratoryFrequency.max}bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.respiratoryFrequencySub.setOnClickListener {
            binding.ventilatorLayout.respiratoryFrequency.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = binding.ventilatorLayout.respiratoryFrequency.progress
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.respiratoryFrequencyAdd.setOnClickListener {
            binding.ventilatorLayout.respiratoryFrequency.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RESPIRATORY_RATE
            ventilationSetting.respiratoryRate.rate = binding.ventilatorLayout.respiratoryFrequency.progress
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 压力上升时间
        binding.ventilatorLayout.raiseTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RAISE_DURATION
                    ventilationSetting.pressureRaiseDuration.duration = progress.times(50)
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
                binding.ventilatorLayout.raiseTimeProcess.text = "${progress.times(50)}ms"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.raiseTimeRange.text = "范围：${binding.ventilatorLayout.raiseTime.min.times(50)} - ${binding.ventilatorLayout.raiseTime.max.times(50)}ms"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.raiseTimeSub.setOnClickListener {
            binding.ventilatorLayout.raiseTime.progress--
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = binding.ventilatorLayout.raiseTime.progress.times(50)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        binding.ventilatorLayout.raiseTimeAdd.setOnClickListener {
            binding.ventilatorLayout.raiseTime.progress++
            ventilationSetting.type = VentilatorBleCmd.VentilationSetting.RAISE_DURATION
            ventilationSetting.pressureRaiseDuration.duration = binding.ventilatorLayout.raiseTime.progress.times(50)
            LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
        }
        // 吸气触发灵敏度
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.ventilatorLayout.iTrigger.adapter = this
        }
        binding.ventilatorLayout.iTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.INHALE_SENSITIVE
                    ventilationSetting.inhaleSensitive.sentive = position
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼气触发灵敏度
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.ventilatorLayout.eTrigger.adapter = this
        }
        binding.ventilatorLayout.eTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    ventilationSetting.type = VentilatorBleCmd.VentilationSetting.EXHALE_SENSITIVE
                    ventilationSetting.exhaleSensitive.sentive = position
                    LpBleUtil.ventilatorSetVentilationSetting(model, ventilationSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 报警设置
        // 漏气量高
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "15s", "30s", "45s", "60s")).apply {
            binding.ventilatorLayout.leakHigh.adapter = this
        }
        binding.ventilatorLayout.leakHigh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (warningSetting.warningLeak.high == 0 && position != 0) {
                    AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("开此报警，会停用自动停止功能")
                        .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                            if (spinnerSet) {
                                warningSetting.type = VentilatorBleCmd.WarningSetting.LEAK_HIGH
                                warningSetting.warningLeak.high = position.times(15)
                                LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                            }
                            binding.ventilatorLayout.autoEnd.isChecked = false
                            binding.ventilatorLayout.autoEnd.isEnabled = false
                        }
                        .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                            binding.ventilatorLayout.leakHigh.setSelection(0)
                        }
                        .create()
                        .show()
                } else {
                    if (spinnerSet) {
                        warningSetting.type = VentilatorBleCmd.WarningSetting.LEAK_HIGH
                        warningSetting.warningLeak.high = position.times(15)
                        LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                    }
                    binding.ventilatorLayout.autoEnd.isEnabled = position == 0
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸暂停
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "10s", "20s", "30s")).apply {
            binding.ventilatorLayout.apnea.adapter = this
        }
        binding.ventilatorLayout.apnea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerSet) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.APNEA
                    warningSetting.warningApnea.apnea = position.times(10)
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 潮气量低
        binding.ventilatorLayout.vtLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.VT_LOW
                    warningSetting.warningVt.low = if (progress == 19) {
                        0
                    } else {
                        progress.times(10)
                    }
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.vtLowProcess.text = if (progress == 19) {
                    "关"
                } else {
                    "${progress.times(10)}ml"
                }
                binding.ventilatorLayout.vtLowRange.text = "范围：关 - ${binding.ventilatorLayout.vtLow.max.times(10)}ml"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.vtLowSub.setOnClickListener {
            binding.ventilatorLayout.vtLow.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.VT_LOW
            warningSetting.warningVt.low = if (binding.ventilatorLayout.vtLow.progress == 19) {
                0
            } else {
                binding.ventilatorLayout.vtLow.progress.times(10)
            }
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.vtLowAdd.setOnClickListener {
            binding.ventilatorLayout.vtLow.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.VT_LOW
            warningSetting.warningVt.low = binding.ventilatorLayout.vtLow.progress.times(10)
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 分钟通气量低
        binding.ventilatorLayout.lowVentilation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.LOW_VENTILATION
                    warningSetting.warningVentilation.low = progress
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.lowVentilationProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}L/min"
                }
                binding.ventilatorLayout.lowVentilationRange.text = "范围：关 - ${binding.ventilatorLayout.lowVentilation.max}L/min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.lowVentilationSub.setOnClickListener {
            binding.ventilatorLayout.lowVentilation.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = binding.ventilatorLayout.lowVentilation.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.lowVentilationAdd.setOnClickListener {
            binding.ventilatorLayout.lowVentilation.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.LOW_VENTILATION
            warningSetting.warningVentilation.low = binding.ventilatorLayout.lowVentilation.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率高
        binding.ventilatorLayout.rrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.RR_HIGH
                    warningSetting.warningRrHigh.high = if (progress == binding.ventilatorLayout.rrHigh.min) {
                        0
                    } else {
                        progress
                    }
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.rrHighProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                binding.ventilatorLayout.rrHighRange.text = "范围：关 - ${binding.ventilatorLayout.rrHigh.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.rrHighSub.setOnClickListener {
            binding.ventilatorLayout.rrHigh.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = if (binding.ventilatorLayout.rrHigh.progress == binding.ventilatorLayout.rrHigh.min) {
                0
            } else {
                binding.ventilatorLayout.rrHigh.progress
            }
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.rrHighAdd.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (warningSetting.warningRrHigh.high == 0 && warningSetting.warningRrLow.low != 0) {
                    binding.ventilatorLayout.rrHigh.min = warningSetting.warningRrLow.low + 1
                }
            }
            binding.ventilatorLayout.rrHigh.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_HIGH
            warningSetting.warningRrHigh.high = binding.ventilatorLayout.rrHigh.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 呼吸频率低
        binding.ventilatorLayout.rrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.RR_LOW
                    warningSetting.warningRrLow.low = progress
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.rrLowProcess.text = if (progress == 0) {
                    "关"
                } else {
                    "${progress}bpm"
                }
                binding.ventilatorLayout.rrLowRange.text = "范围：关 - ${binding.ventilatorLayout.rrLow.max}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.rrLowSub.setOnClickListener {
            binding.ventilatorLayout.rrLow.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_LOW
            warningSetting.warningRrLow.low = binding.ventilatorLayout.rrLow.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.rrLowAdd.setOnClickListener {
            binding.ventilatorLayout.rrLow.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.RR_LOW
            warningSetting.warningRrLow.low = binding.ventilatorLayout.rrLow.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 血氧饱和度低
        binding.ventilatorLayout.spo2Low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.SPO2_LOW
                    warningSetting.warningSpo2Low.low = if (progress == 79) {
                        0
                    } else {
                        progress
                    }
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.spo2LowProcess.text = if (progress == 79) {
                    "关"
                } else {
                    "${progress}%"
                }
                binding.ventilatorLayout.spo2LowRange.text = "范围：关 - ${binding.ventilatorLayout.spo2Low.max}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.spo2LowSub.setOnClickListener {
            binding.ventilatorLayout.spo2Low.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = if (binding.ventilatorLayout.spo2Low.progress == 79) {
                0
            } else {
                binding.ventilatorLayout.spo2Low.progress
            }
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.spo2LowAdd.setOnClickListener {
            binding.ventilatorLayout.spo2Low.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.SPO2_LOW
            warningSetting.warningSpo2Low.low = binding.ventilatorLayout.spo2Low.progress
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率高
        binding.ventilatorLayout.hrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.HR_HIGH
                    warningSetting.warningHrHigh.high = if (progress == 9) {
                        0
                    } else {
                        progress.times(10)
                    }
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.hrHighProcess.text = if (progress == 9) {
                    "关"
                } else {
                    "${progress.times(10)}bpm"
                }
                binding.ventilatorLayout.hrHighRange.text = "范围：关 - ${binding.ventilatorLayout.hrHigh.max.times(10)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.hrHighSub.setOnClickListener {
            binding.ventilatorLayout.hrHigh.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = if (binding.ventilatorLayout.hrHigh.progress == 9) {
                0
            } else {
                binding.ventilatorLayout.hrHigh.progress.times(10)
            }
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.hrHighAdd.setOnClickListener {
            binding.ventilatorLayout.hrHigh.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_HIGH
            warningSetting.warningHrHigh.high = binding.ventilatorLayout.hrHigh.progress.times(10)
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 脉率/心率低
        binding.ventilatorLayout.hrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    warningSetting.type = VentilatorBleCmd.WarningSetting.HR_LOW
                    warningSetting.warningHrLow.low = if (progress == 5) {
                        0
                    } else {
                        progress.times(5)
                    }
                    LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
                }
                binding.ventilatorLayout.hrLowProcess.text = if (progress == 5) {
                    "关"
                } else {
                    "${progress.times(5)}bpm"
                }
                binding.ventilatorLayout.hrLowRange.text = "范围：关 - ${binding.ventilatorLayout.hrLow.max.times(5)}bpm"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.ventilatorLayout.hrLowSub.setOnClickListener {
            binding.ventilatorLayout.hrLow.progress--
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_LOW
            warningSetting.warningHrLow.low = if (binding.ventilatorLayout.hrLow.progress == 5) {
                0
            } else {
                binding.ventilatorLayout.hrLow.progress.times(5)
            }
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        binding.ventilatorLayout.hrLowAdd.setOnClickListener {
            binding.ventilatorLayout.hrLow.progress++
            warningSetting.type = VentilatorBleCmd.WarningSetting.HR_LOW
            warningSetting.warningHrLow.low = binding.ventilatorLayout.hrLow.progress.times(5)
            LpBleUtil.ventilatorSetWarningSetting(model, warningSetting)
        }
        // 设置用户
        binding.ventilatorLayout.setUser.setOnClickListener {
            val data = UserInfo()
            data.aid = 1133
            data.uid = 2233
            data.fName = "测试"
            data.name = "一"
            data.birthday = "1999-06-04"
            data.height = 155
            data.weight = 40.5f
            data.gender = 1
            LpBleUtil.ventilatorSetUserInfo(model, data)
        }
        binding.ventilatorLayout.getUser.setOnClickListener {
            LpBleUtil.ventilatorGetUserInfo(model)
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
                when (data.standard) {
                    // CFDA
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ventilatorLayout.ipapPressure.min = 12
                        }
                        binding.ventilatorLayout.epapPressure.max = 46
                    }
                    // CE
                    2 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ventilatorLayout.ipapPressure.min = 8
                        }
                        binding.ventilatorLayout.epapPressure.max = 50
                    }
                }
                if (data.deviceMode == 2) {
                    isEnableVentilationSetting(true)
                    isEnableWarningSetting(true)
                } else {
                    isEnableVentilationSetting(false)
                    isEnableWarningSetting(false)
                }
                if (data.isVentilated) {
                    isEnableSystemSetting(false)
                    isEnableMeasureSetting(false)
                } else {
                    isEnableSystemSetting(true)
                    isEnableMeasureSetting(true)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting)
            .observe(owner) {
                spinnerSet = false
                systemSetting = it.data as SystemSetting
                binding.ventilatorLayout.unit.setSelection(systemSetting.unitSetting.pressureUnit)
                binding.ventilatorLayout.language.setSelection(systemSetting.languageSetting.language)
                binding.ventilatorLayout.brightness.progress = systemSetting.screenSetting.brightness
                binding.ventilatorLayout.screenOff.setSelection(systemSetting.screenSetting.autoOff.div(30))
                binding.ventilatorLayout.filter.progress = systemSetting.replacements.filter
                binding.ventilatorLayout.mask.progress = systemSetting.replacements.mask
                binding.ventilatorLayout.tube.progress = systemSetting.replacements.tube
                binding.ventilatorLayout.tank.progress = systemSetting.replacements.tank
                binding.ventilatorLayout.volume.progress = systemSetting.volumeSetting.volume.div(5)
                spinnerSet = true
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
                if (measureSetting.humidification.humidification == 0xff) {
                    binding.ventilatorLayout.humidification.setSelection(6)
                } else if (measureSetting.humidification.humidification > 6) {
                    binding.ventilatorLayout.humidification.setSelection(0)
                } else {
                    binding.ventilatorLayout.humidification.setSelection(measureSetting.humidification.humidification)
                }
                if (measureSetting.pressureReduce.ipr > 4) {
                    binding.ventilatorLayout.ipr.setSelection(0)
                } else {
                    binding.ventilatorLayout.ipr.setSelection(measureSetting.pressureReduce.ipr)
                }
                if (measureSetting.pressureReduce.epr > 4) {
                    binding.ventilatorLayout.epr.setSelection(0)
                } else {
                    binding.ventilatorLayout.epr.setSelection(measureSetting.pressureReduce.epr)
                }
                binding.ventilatorLayout.autoStart.isChecked = measureSetting.autoSwitch.autoStart
                binding.ventilatorLayout.autoEnd.isChecked = measureSetting.autoSwitch.autoEnd
                binding.ventilatorLayout.preHeat.isChecked = measureSetting.preHeat.on
                binding.ventilatorLayout.rampPressure.progress = measureSetting.ramp.pressure.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    when (rtState.ventilationMode) {
                        // CPAP
                        0 -> binding.ventilatorLayout.rampPressure.max = binding.ventilatorLayout.cpapPressure.progress
                        // APAP
                        1 -> binding.ventilatorLayout.rampPressure.max = binding.ventilatorLayout.apapPressureMin.progress
                        // S、S/T、T
                        2, 3, 4 -> binding.ventilatorLayout.rampPressure.max = binding.ventilatorLayout.epapPressure.progress
                    }
                }
                binding.ventilatorLayout.rampTime.progress = measureSetting.ramp.time.div(5)
                binding.ventilatorLayout.tubeType.setSelection(measureSetting.tubeType.type)
                binding.ventilatorLayout.maskType.setSelection(measureSetting.mask.type)
                binding.ventilatorLayout.maskPressure.progress = measureSetting.mask.pressure.toInt()
                spinnerSet = true
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
                binding.ventilatorLayout.ventilationMode.setSelection(ventilationSetting.ventilationMode.mode)
                layoutGone()
                layoutVisible(ventilationSetting.ventilationMode.mode)
                binding.ventilatorLayout.cpapPressure.progress = ventilationSetting.cpapPressure.pressure.div(0.5).toInt()
                binding.ventilatorLayout.apapPressureMax.progress = ventilationSetting.apapPressureMax.max.div(0.5).toInt()
                binding.ventilatorLayout.apapPressureMin.progress = ventilationSetting.apapPressureMin.min.div(0.5).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.apapPressureMax.min = binding.ventilatorLayout.apapPressureMin.progress
                }
                binding.ventilatorLayout.apapPressureMin.max = binding.ventilatorLayout.apapPressureMax.progress
                binding.ventilatorLayout.ipapPressure.progress = ventilationSetting.pressureInhale.inhale.div(0.5).toInt()
                binding.ventilatorLayout.epapPressure.progress = ventilationSetting.pressureExhale.exhale.div(0.5).toInt()
                if (this::rtState.isInitialized) {
                    if (rtState.standard == 2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ventilatorLayout.ipapPressure.min = binding.ventilatorLayout.epapPressure.progress
                        }
                        binding.ventilatorLayout.epapPressure.max = binding.ventilatorLayout.ipapPressure.progress
                    } else if (rtState.standard == 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.ventilatorLayout.ipapPressure.min = binding.ventilatorLayout.epapPressure.progress + 4
                        }
                        binding.ventilatorLayout.epapPressure.max = binding.ventilatorLayout.ipapPressure.progress - 4
                    }
                }
                binding.ventilatorLayout.raiseTime.progress = ventilationSetting.pressureRaiseDuration.duration.div(50)
                val limT = when (ventilationSetting.inhaleDuration.duration) {
                    0.3f -> 200
                    0.4f -> 250
                    0.5f -> 300
                    0.6f -> 400
                    0.7f -> 450
                    0.8f -> 500
                    0.9f -> 600
                    1.0f -> 650
                    1.1f -> 700
                    1.2f -> 800
                    1.3f -> 850
                    else -> 900
                }
                val iepap = ventilationSetting.pressureInhale.inhale - ventilationSetting.pressureExhale.exhale
                val minT = when (iepap) {
                    in 2.0..5.0 -> 100
                    in 5.5..10.0 -> 200
                    in 10.5..15.0 -> 300
                    in 15.5..20.0 -> 400
                    in 20.5..21.0 -> 450
                    else -> 100
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.raiseTime.min = max(100, minT).div(50)
                }
                binding.ventilatorLayout.raiseTime.max = min((limT), 900).div(50)
                binding.ventilatorLayout.inspiratoryTime.progress = ventilationSetting.inhaleDuration.duration.times(10).toInt()
                val temp = when (ventilationSetting.pressureRaiseDuration.duration) {
                    200 -> 0.3f
                    250 -> 0.4f
                    300 -> 0.5f
                    400 -> 0.6f
                    450 -> 0.7f
                    500 -> 0.8f
                    600 -> 0.9f
                    650 -> 1.0f
                    700 -> 1.1f
                    800 -> 1.2f
                    850 -> 1.3f
                    else -> 0.3f
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.ventilatorLayout.inspiratoryTime.min = max(0.3f, temp).times(10).toInt()
                }
                binding.ventilatorLayout.inspiratoryTime.max = min((60f/ventilationSetting.respiratoryRate.rate)*2/3, 4.0f).times(10).toInt()
                binding.ventilatorLayout.respiratoryFrequency.progress = ventilationSetting.respiratoryRate.rate
                binding.ventilatorLayout.respiratoryFrequency.max = min((60/(ventilationSetting.inhaleDuration.duration/2*3).toInt()),30)
                binding.ventilatorLayout.iTrigger.setSelection(ventilationSetting.inhaleSensitive.sentive)
                binding.ventilatorLayout.eTrigger.setSelection(ventilationSetting.exhaleSensitive.sentive)
                spinnerSet = true
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
                binding.ventilatorLayout.autoEnd.isEnabled = warningSetting.warningLeak.high == 0
                binding.ventilatorLayout.leakHigh.setSelection(warningSetting.warningLeak.high.div(15))
                binding.ventilatorLayout.lowVentilation.progress = warningSetting.warningVentilation.low
                binding.ventilatorLayout.vtLow.progress = if (warningSetting.warningVt.low == 0) {
                    19
                } else {
                    warningSetting.warningVt.low.div(10)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (warningSetting.warningRrLow.low != 0 && warningSetting.warningRrHigh.high != 0) {
                        binding.ventilatorLayout.rrHigh.min = warningSetting.warningRrLow.low + 1
                        binding.ventilatorLayout.rrLow.max = warningSetting.warningRrHigh.high - 2
                    } else {
                        binding.ventilatorLayout.rrLow.min = 0
                        binding.ventilatorLayout.rrLow.max = 60
                        binding.ventilatorLayout.rrHigh.min = 0
                        binding.ventilatorLayout.rrHigh.max = 60
                    }
                }
                binding.ventilatorLayout.rrHigh.progress = warningSetting.warningRrHigh.high
                binding.ventilatorLayout.rrLow.progress = warningSetting.warningRrLow.low
                binding.ventilatorLayout.spo2Low.progress = if (warningSetting.warningSpo2Low.low == 0) {
                    79
                } else {
                    warningSetting.warningSpo2Low.low
                }
                binding.ventilatorLayout.hrHigh.progress = if (warningSetting.warningHrHigh.high == 0) {
                    9
                } else {
                    warningSetting.warningHrHigh.high.div(10)
                }
                binding.ventilatorLayout.hrLow.progress = if (warningSetting.warningHrLow.low == 0) {
                    5
                } else {
                    warningSetting.warningHrLow.low.div(5)
                }
                binding.ventilatorLayout.apnea.setSelection(warningSetting.warningApnea.apnea.div(10))
                spinnerSet = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting)
            .observe(owner) {
                _toast.value = "警告设置成功"
                LpBleUtil.ventilatorGetWarningSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUserInfo)
            .observe(owner) {
                _toast.value = "用户设置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetUserInfo)
            .observe(owner) {
                val data = it.data as UserInfo
                binding.ventilatorLayout.userInfo.text = "$data"
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorWritingFileProgress)
            .observe(owner) {
                val data = it.data as Int
                binding.ventilatorLayout.updateInfo.text = "升级进度：$data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorWriteFileEnd)
            .observe(owner) {
                val data = it.data as Boolean
                binding.ventilatorLayout.updateInfo.text = "升级完成：$data"
            }
    }

    private fun layoutGone() {
        binding.ventilatorLayout.eprLayout.visibility = View.GONE
        binding.ventilatorLayout.cpapPressureLayout.visibility = View.GONE
        binding.ventilatorLayout.apapPressureMaxLayout.visibility = View.GONE
        binding.ventilatorLayout.apapPressureMinLayout.visibility = View.GONE
        binding.ventilatorLayout.ipapPressureLayout.visibility = View.GONE
        binding.ventilatorLayout.epapPressureLayout.visibility = View.GONE
        binding.ventilatorLayout.raiseTimeLayout.visibility = View.GONE
        binding.ventilatorLayout.iTriggerLayout.visibility = View.GONE
        binding.ventilatorLayout.eTriggerLayout.visibility = View.GONE
        binding.ventilatorLayout.inspiratoryTimeLayout.visibility = View.GONE
        binding.ventilatorLayout.respiratoryFrequencyLayout.visibility = View.GONE
        binding.ventilatorLayout.lowVentilationLayout.visibility = View.GONE
        binding.ventilatorLayout.vtLowLayout.visibility = View.GONE
        binding.ventilatorLayout.rrHighLayout.visibility = View.GONE
        binding.ventilatorLayout.rrLowLayout.visibility = View.GONE
    }
    private fun layoutVisible(mode: Int) {
        when (mode) {
            // CPAP
            0 -> {
                binding.ventilatorLayout.eprLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.cpapPressureLayout.visibility = View.VISIBLE
            }
            // APAP
            1 -> {
                binding.ventilatorLayout.eprLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.apapPressureMaxLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.apapPressureMinLayout.visibility = View.VISIBLE
            }
            // S
            2 -> {
                binding.ventilatorLayout.ipapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.iTriggerLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.eTriggerLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayout.visibility = View.VISIBLE
            }
            // S/T
            3 -> {
                binding.ventilatorLayout.ipapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.inspiratoryTimeLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.respiratoryFrequencyLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.iTriggerLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.eTriggerLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayout.visibility = View.VISIBLE
            }
            // T
            4 -> {
                binding.ventilatorLayout.ipapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.epapPressureLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.inspiratoryTimeLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.respiratoryFrequencyLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.raiseTimeLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.lowVentilationLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.vtLowLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrHighLayout.visibility = View.VISIBLE
                binding.ventilatorLayout.rrLowLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun isEnableSystemSetting(enable: Boolean) {
        binding.ventilatorLayout.unit.isEnabled = enable
        binding.ventilatorLayout.language.isEnabled = enable
        binding.ventilatorLayout.screenOff.isEnabled = enable
        binding.ventilatorLayout.brightness.isEnabled = enable
        binding.ventilatorLayout.filter.isEnabled = enable
        binding.ventilatorLayout.mask.isEnabled = enable
        binding.ventilatorLayout.tube.isEnabled = enable
        binding.ventilatorLayout.tank.isEnabled = enable
        binding.ventilatorLayout.volume.isEnabled = enable
    }
    private fun isEnableMeasureSetting(enable: Boolean) {
        binding.ventilatorLayout.humidification.isEnabled = enable
        binding.ventilatorLayout.epr.isEnabled = enable
        binding.ventilatorLayout.tubeType.isEnabled = enable
        binding.ventilatorLayout.maskType.isEnabled = enable
        binding.ventilatorLayout.maskPressure.isEnabled = enable
        binding.ventilatorLayout.rampPressure.isEnabled = enable
        binding.ventilatorLayout.rampTime.isEnabled = enable
        binding.ventilatorLayout.autoStart.isEnabled = enable
        binding.ventilatorLayout.autoEnd.isEnabled = enable
        binding.ventilatorLayout.preHeat.isEnabled = enable
    }
    private fun isEnableVentilationSetting(enable: Boolean) {
        binding.ventilatorLayout.ventilationMode.isEnabled = enable
        binding.ventilatorLayout.iTrigger.isEnabled = enable
        binding.ventilatorLayout.eTrigger.isEnabled = enable
        binding.ventilatorLayout.cpapPressureSub.isEnabled = enable
        binding.ventilatorLayout.cpapPressure.isEnabled = enable
        binding.ventilatorLayout.cpapPressureAdd.isEnabled = enable
        binding.ventilatorLayout.apapPressureMaxSub.isEnabled = enable
        binding.ventilatorLayout.apapPressureMax.isEnabled = enable
        binding.ventilatorLayout.apapPressureMaxAdd.isEnabled = enable
        binding.ventilatorLayout.apapPressureMinSub.isEnabled = enable
        binding.ventilatorLayout.apapPressureMin.isEnabled = enable
        binding.ventilatorLayout.apapPressureMinAdd.isEnabled = enable
        binding.ventilatorLayout.ipapPressureSub.isEnabled = enable
        binding.ventilatorLayout.ipapPressure.isEnabled = enable
        binding.ventilatorLayout.ipapPressureAdd.isEnabled = enable
        binding.ventilatorLayout.epapPressureSub.isEnabled = enable
        binding.ventilatorLayout.epapPressure.isEnabled = enable
        binding.ventilatorLayout.epapPressureAdd.isEnabled = enable
        binding.ventilatorLayout.inspiratoryTimeSub.isEnabled = enable
        binding.ventilatorLayout.inspiratoryTime.isEnabled = enable
        binding.ventilatorLayout.inspiratoryTimeAdd.isEnabled = enable
        binding.ventilatorLayout.respiratoryFrequencySub.isEnabled = enable
        binding.ventilatorLayout.respiratoryFrequency.isEnabled = enable
        binding.ventilatorLayout.respiratoryFrequencyAdd.isEnabled = enable
        binding.ventilatorLayout.raiseTimeSub.isEnabled = enable
        binding.ventilatorLayout.raiseTime.isEnabled = enable
        binding.ventilatorLayout.raiseTimeAdd.isEnabled = enable
    }
    private fun isEnableWarningSetting(enable: Boolean) {
        binding.ventilatorLayout.leakHigh.isEnabled = enable
        binding.ventilatorLayout.apnea.isEnabled = enable
        binding.ventilatorLayout.lowVentilationSub.isEnabled = enable
        binding.ventilatorLayout.lowVentilation.isEnabled = enable
        binding.ventilatorLayout.lowVentilationAdd.isEnabled = enable
        binding.ventilatorLayout.vtLowSub.isEnabled = enable
        binding.ventilatorLayout.vtLow.isEnabled = enable
        binding.ventilatorLayout.vtLowAdd.isEnabled = enable
        binding.ventilatorLayout.rrHighSub.isEnabled = enable
        binding.ventilatorLayout.rrHigh.isEnabled = enable
        binding.ventilatorLayout.rrHighAdd.isEnabled = enable
        binding.ventilatorLayout.rrLowSub.isEnabled = enable
        binding.ventilatorLayout.rrLow.isEnabled = enable
        binding.ventilatorLayout.rrLowAdd.isEnabled = enable
        binding.ventilatorLayout.spo2LowSub.isEnabled = enable
        binding.ventilatorLayout.spo2Low.isEnabled = enable
        binding.ventilatorLayout.spo2LowAdd.isEnabled = enable
        binding.ventilatorLayout.hrHighSub.isEnabled = enable
        binding.ventilatorLayout.hrHigh.isEnabled = enable
        binding.ventilatorLayout.hrHighAdd.isEnabled = enable
        binding.ventilatorLayout.hrLowSub.isEnabled = enable
        binding.ventilatorLayout.hrLow.isEnabled = enable
        binding.ventilatorLayout.hrLowAdd.isEnabled = enable
    }

}