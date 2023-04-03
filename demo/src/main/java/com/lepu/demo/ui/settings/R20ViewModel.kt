package com.lepu.demo.ui.settings

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.R20BleCmd
import com.lepu.blepro.ble.cmd.R20BleResponse
import com.lepu.blepro.ble.data.r20.MeasureSetting
import com.lepu.blepro.ble.data.r20.SystemSetting
import com.lepu.blepro.ble.data.r20.VentilationSetting
import com.lepu.blepro.ble.data.r20.WarningSetting
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding

class R20ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context
    private var systemSetting = SystemSetting()
    private var measureSetting = MeasureSetting()
    private var ventilationSetting = VentilationSetting()
    private var warningSetting = WarningSetting()

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.context = context
        this.binding = binding
        binding.r20Layout.factoryInfo.setOnClickListener {
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.other.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.factoryInfoLayout.visibility = View.VISIBLE
            binding.r20Layout.systemSettingLayout.visibility = View.GONE
            binding.r20Layout.measureSettingLayout.visibility = View.GONE
            binding.r20Layout.ventilationSettingLayout.visibility = View.GONE
            binding.r20Layout.warningSettingLayout.visibility = View.GONE
            binding.r20Layout.otherLayout.visibility = View.GONE
        }
        binding.r20Layout.systemSetting.setOnClickListener {
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.other.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.systemSettingLayout.visibility = View.VISIBLE
            binding.r20Layout.factoryInfoLayout.visibility = View.GONE
            binding.r20Layout.measureSettingLayout.visibility = View.GONE
            binding.r20Layout.ventilationSettingLayout.visibility = View.GONE
            binding.r20Layout.warningSettingLayout.visibility = View.GONE
            binding.r20Layout.otherLayout.visibility = View.GONE
        }
        binding.r20Layout.measureSetting.setOnClickListener {
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.other.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSettingLayout.visibility = View.VISIBLE
            binding.r20Layout.factoryInfoLayout.visibility = View.GONE
            binding.r20Layout.systemSettingLayout.visibility = View.GONE
            binding.r20Layout.ventilationSettingLayout.visibility = View.GONE
            binding.r20Layout.warningSettingLayout.visibility = View.GONE
            binding.r20Layout.otherLayout.visibility = View.GONE
        }
        binding.r20Layout.ventilationSetting.setOnClickListener {
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.other.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSettingLayout.visibility = View.VISIBLE
            binding.r20Layout.factoryInfoLayout.visibility = View.GONE
            binding.r20Layout.systemSettingLayout.visibility = View.GONE
            binding.r20Layout.measureSettingLayout.visibility = View.GONE
            binding.r20Layout.warningSettingLayout.visibility = View.GONE
            binding.r20Layout.otherLayout.visibility = View.GONE
        }
        binding.r20Layout.warningSetting.setOnClickListener {
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.other.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSettingLayout.visibility = View.VISIBLE
            binding.r20Layout.factoryInfoLayout.visibility = View.GONE
            binding.r20Layout.systemSettingLayout.visibility = View.GONE
            binding.r20Layout.measureSettingLayout.visibility = View.GONE
            binding.r20Layout.ventilationSettingLayout.visibility = View.GONE
            binding.r20Layout.otherLayout.visibility = View.GONE
        }
        binding.r20Layout.other.setOnClickListener {
            binding.r20Layout.other.background = context.getDrawable(R.drawable.string_selected)
            binding.r20Layout.factoryInfo.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.systemSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.measureSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.ventilationSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.warningSetting.background = context.getDrawable(R.drawable.dialog_hint_shape)
            binding.r20Layout.otherLayout.visibility = View.VISIBLE
            binding.r20Layout.factoryInfoLayout.visibility = View.GONE
            binding.r20Layout.systemSettingLayout.visibility = View.GONE
            binding.r20Layout.measureSettingLayout.visibility = View.GONE
            binding.r20Layout.ventilationSettingLayout.visibility = View.GONE
            binding.r20Layout.warningSettingLayout.visibility = View.GONE
        }
        // 绑定/解绑
        binding.r20Layout.bound.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.r20DeviceBound(model, isChecked)
        }
        // 进入医生模式
        binding.r20Layout.doctorMode.setOnCheckedChangeListener { buttonView, isChecked ->
            var pin = binding.r20Layout.pin.text.toString()
            pin = if (pin == "") {
                "0319"
            } else {
                pin
            }
            LpBleUtil.r20DoctorMode(model, pin, System.currentTimeMillis().div(1000))
        }
        // 系统设置
        // 单位设置
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("cmH2O", "hPa")).apply {
            binding.r20Layout.unit.adapter = this
        }
        binding.r20Layout.unit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                systemSetting.type = R20BleCmd.SystemSetting.UNIT
                systemSetting.unitSetting.pressureUnit = position
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 语言设置
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("英文", "中文")).apply {
            binding.r20Layout.language.adapter = this
        }
        binding.r20Layout.language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                systemSetting.type = R20BleCmd.SystemSetting.LANGUAGE
                systemSetting.languageSetting.language = position
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 屏幕设置：屏幕亮度
        binding.r20Layout.brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.SCREEN
                systemSetting.screenSetting.brightness = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.brightnessProcess.text = "$progress %"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 屏幕设置：自动熄屏
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("常亮", "30秒", "60秒", "90秒", "120秒")).apply {
            binding.r20Layout.screenOff.adapter = this
        }
        binding.r20Layout.screenOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                systemSetting.type = R20BleCmd.SystemSetting.SCREEN
                systemSetting.screenSetting.autoOff = position
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 耗材设置：过滤棉
        binding.r20Layout.filter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.REPLACEMENT
                systemSetting.replacements.filter = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.filterProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 月"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 耗材设置：面罩
        binding.r20Layout.mask.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.REPLACEMENT
                systemSetting.replacements.mask = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.maskProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 月"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 耗材设置：管道
        binding.r20Layout.tube.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.REPLACEMENT
                systemSetting.replacements.tube = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.tubeProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 月"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 耗材设置：水箱
        binding.r20Layout.tank.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.REPLACEMENT
                systemSetting.replacements.tank = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.tankProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "$progress 月"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 音量设置
        binding.r20Layout.volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                systemSetting.type = R20BleCmd.SystemSetting.VOLUME
                systemSetting.volumeSetting.volume = progress
                LpBleUtil.r20SetSystemSetting(model, systemSetting)
                binding.r20Layout.volumeProcess.text = "$progress %"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 测量设置
        // 湿化等级
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3", "4", "5", "自动")).apply {
            binding.r20Layout.humidification.adapter = this
        }
        binding.r20Layout.humidification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                measureSetting.type = R20BleCmd.MeasureSetting.HUMIDIFICATION
                if (position == 6) {
                    measureSetting.humidification.humidification = 0x10
                } else {
                    measureSetting.humidification.humidification = position
                }
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸压力释放：吸气压力释放
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3")).apply {
            binding.r20Layout.ipr.adapter = this
        }
        binding.r20Layout.ipr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                measureSetting.type = R20BleCmd.MeasureSetting.PRESSURE_REDUCE
                measureSetting.pressureReduce.ipr = position
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸压力释放：呼气压力释放
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "1", "2", "3")).apply {
            binding.r20Layout.epr.adapter = this
        }
        binding.r20Layout.epr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                measureSetting.type = R20BleCmd.MeasureSetting.PRESSURE_REDUCE
                measureSetting.pressureReduce.epr = position
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 自动启停：自动启动
        binding.r20Layout.autoStart.setOnCheckedChangeListener { buttonView, isChecked ->
            measureSetting.type = R20BleCmd.MeasureSetting.AUTO_SWITCH
            measureSetting.autoSwitch.autoStart = isChecked
            LpBleUtil.r20SetMeasureSetting(model, measureSetting)
        }
        // 自动启停：自动停止
        binding.r20Layout.autoEnd.setOnCheckedChangeListener { buttonView, isChecked ->
            measureSetting.type = R20BleCmd.MeasureSetting.AUTO_SWITCH
            measureSetting.autoSwitch.autoEnd = isChecked
            LpBleUtil.r20SetMeasureSetting(model, measureSetting)
        }
        // 自动启停：自动停止
        binding.r20Layout.autoEnd.setOnCheckedChangeListener { buttonView, isChecked ->
            measureSetting.type = R20BleCmd.MeasureSetting.PRE_HEAT
            measureSetting.preHeat.on = isChecked
            LpBleUtil.r20SetMeasureSetting(model, measureSetting)
        }
        // 缓冲压力
        binding.r20Layout.rampPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                measureSetting.type = R20BleCmd.MeasureSetting.RAMP
                measureSetting.ramp.pressure = 3.0f + progress.times(0.5f)
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
                binding.r20Layout.rampPressureProcess.text = "${measureSetting.ramp.pressure} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 缓冲时间
        binding.r20Layout.rampTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                measureSetting.type = R20BleCmd.MeasureSetting.RAMP
                measureSetting.ramp.time = progress.times(5)
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
                binding.r20Layout.rampTimeProcess.text = "${measureSetting.ramp.time} min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 管道类型
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("15mm", "22mm")).apply {
            binding.r20Layout.tubeType.adapter = this
        }
        binding.r20Layout.tubeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                measureSetting.type = R20BleCmd.MeasureSetting.TUBE_TYPE
                measureSetting.tubeType.type = position+1
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩类型
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("口鼻罩", "鼻罩", "鼻枕")).apply {
            binding.r20Layout.maskType.adapter = this
        }
        binding.r20Layout.maskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                measureSetting.type = R20BleCmd.MeasureSetting.MASK
                measureSetting.mask.type = position+1
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 面罩佩戴匹配测试压力
        binding.r20Layout.maskPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                measureSetting.type = R20BleCmd.MeasureSetting.MASK
                measureSetting.mask.pressure = progress.toFloat()
                LpBleUtil.r20SetMeasureSetting(model, measureSetting)
                binding.r20Layout.maskPressureProcess.text = "${measureSetting.mask.pressure} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 通气设置
        // CPAP模式压力
        binding.r20Layout.cpapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.PRESSURE
                ventilationSetting.pressure.pressure = progress.times(0.5f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.cpapPressureProcess.text = "${ventilationSetting.pressure.pressure} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // APAP模式压力最大值Pmax
        binding.r20Layout.apapPressureMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.PRESSURE_MAX
                ventilationSetting.pressureMax.max = progress.times(0.5f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.apapPressureMaxProcess.text = "${ventilationSetting.pressureMax.max} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // APAP模式压力最小值Pmin
        binding.r20Layout.apapPressureMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.PRESSURE_MIN
                ventilationSetting.pressureMin.min = progress.times(0.5f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.apapPressureMinProcess.text = "${ventilationSetting.pressureMin.min} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 吸气压力
        binding.r20Layout.ipapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.PRESSURE_INHALE
                ventilationSetting.pressureInhale.inhale = progress.times(0.5f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.ipapPressureProcess.text = "${ventilationSetting.pressureInhale.inhale} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 呼气压力
        binding.r20Layout.epapPressure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.PRESSURE_EXHALE
                ventilationSetting.pressureExhale.exhale = progress.times(0.5f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.epapPressureProcess.text = "${ventilationSetting.pressureExhale.exhale} ${if (systemSetting.unitSetting.pressureUnit == 0) {
                    "cmH2O"
                } else {
                    "hPa"
                }}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 吸气时间
        binding.r20Layout.inspiratoryTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.INHALE_DURATION
                ventilationSetting.inhaleDuration.duration = progress.div(10f)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.inspiratoryTimeProcess.text = "${ventilationSetting.inhaleDuration.duration} s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 呼吸频率
        binding.r20Layout.respiratoryFrequency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.RESPIRATORY_RATE
                ventilationSetting.respiratoryRate.rate = progress
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.respiratoryFrequencyProcess.text = "$progress min"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 压力上升时间
        binding.r20Layout.raiseTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.RESPIRATORY_RATE
                ventilationSetting.pressureRaiseDuration.duration = progress.times(50)
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
                binding.r20Layout.riseTimeProcess.text = "${ventilationSetting.pressureRaiseDuration.duration} ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 吸气触发灵敏度
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.r20Layout.iTrigger.adapter = this
        }
        binding.r20Layout.iTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.INHALE_SENSITIVE
                ventilationSetting.inhaleSensitive.sentive = position
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼气触发灵敏度
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("自动挡", "1", "2", "3", "4", "5")).apply {
            binding.r20Layout.eTrigger.adapter = this
        }
        binding.r20Layout.eTrigger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ventilationSetting.type = R20BleCmd.VentilationSetting.EXHALE_SENSITIVE
                ventilationSetting.exhaleSensitive.sentive = position
                LpBleUtil.r20SetVentilationSetting(model, ventilationSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 报警设置
        // 漏气量高
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "15s", "30s", "45s", "60s")).apply {
            binding.r20Layout.leakHigh.adapter = this
        }
        binding.r20Layout.leakHigh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                warningSetting.type = R20BleCmd.WarningSetting.LEAK_HIGH
                warningSetting.warningLeak.high = position
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 呼吸暂停
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("关闭", "10s", "20s", "30s")).apply {
            binding.r20Layout.apnea.adapter = this
        }
        binding.r20Layout.apnea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                warningSetting.type = R20BleCmd.WarningSetting.APNEA
                warningSetting.warningApnea.apnea = position
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 潮气量低
        binding.r20Layout.vtLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.VT_LOW
                warningSetting.warningVt.low = if (progress == 19) {
                    0
                } else {
                    progress.times(10)
                }
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.vtLowProcess.text = if (progress == 19) {
                    "关闭"
                } else {
                    "${warningSetting.warningVt.low} ml"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 分钟通气量低
        binding.r20Layout.lowVentilation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.LOW_VENTILATION
                warningSetting.warningVentilation.low = progress
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.lowVentilationProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "${warningSetting.warningVentilation.low} L/min"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 呼吸频率高
        binding.r20Layout.rrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.RR_HIGH
                warningSetting.warningRrHigh.high = progress
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.rrHighProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "${warningSetting.warningRrHigh.high} bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 呼吸频率低
        binding.r20Layout.rrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.RR_LOW
                warningSetting.warningRrLow.low = progress
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.rrLowProcess.text = if (progress == 0) {
                    "关闭"
                } else {
                    "${warningSetting.warningRrLow.low} bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 血氧饱和度低
        binding.r20Layout.spo2Low.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.SPO2_LOW
                warningSetting.warningSpo2Low.low = if (progress == 79) {
                    0
                } else {
                    progress
                }
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.spo2LowProcess.text = if (progress == 79) {
                    "关闭"
                } else {
                    "${warningSetting.warningSpo2Low.low} %"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 脉率/心率高
        binding.r20Layout.hrHigh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.HR_HIGH
                warningSetting.warningHrHigh.high = if (progress == 9) {
                    0
                } else {
                    progress.times(10)
                }
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.hrHighProcess.text = if (progress == 9) {
                    "关闭"
                } else {
                    "${warningSetting.warningHrHigh.high} bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        // 脉率/心率低
        binding.r20Layout.hrLow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningSetting.type = R20BleCmd.WarningSetting.HR_LOW
                warningSetting.warningHrLow.low = if (progress == 5) {
                    0
                } else {
                    progress.times(5)
                }
                LpBleUtil.r20SetWarningSetting(model, warningSetting)
                binding.r20Layout.hrLowProcess.text = if (progress == 5) {
                    "关闭"
                } else {
                    "${warningSetting.warningHrLow.low} bpm"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20DeviceBound)
            .observe(owner) {
                val data = it.data as Int
                _toast.value = when (data) {
                    0 -> "绑定成功"
                    1 -> "绑定失败"
                    2 -> "绑定超时"
                    else -> "绑定消息"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20DeviceUnBound)
            .observe(owner) {
                _toast.value = "解绑成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20DoctorMode)
            .observe(owner) {
                val data = it.data as R20BleResponse.DoctorModeResult
                _toast.value = if (data.success) {
                    "进入医生模式成功"
                } else {
                    "进入医生模式失败${when (data.errCode) {
                        1 -> "设备处于医生模式"
                        2 -> "设备处于医生模式（BLE）"
                        3 -> "，设备处于医生模式（Socket）"
                        4 -> "，密码错误"
                        else -> ""
                    }}"
                }
                LpBleUtil.r20GetRtState(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20RtState)
            .observe(owner) {
                val data = it.data as R20BleResponse.RtState
                binding.r20Layout.doctorMode.isChecked = data.deviceMode != 0
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetSystemSetting)
            .observe(owner) {
                systemSetting = it.data as SystemSetting
                binding.r20Layout.unit.setSelection(systemSetting.unitSetting.pressureUnit)
                binding.r20Layout.language.setSelection(systemSetting.languageSetting.language)
                binding.r20Layout.brightness.progress = systemSetting.screenSetting.brightness
                binding.r20Layout.screenOff.setSelection(systemSetting.screenSetting.autoOff)
                binding.r20Layout.filter.progress = systemSetting.replacements.filter
                binding.r20Layout.mask.progress = systemSetting.replacements.mask
                binding.r20Layout.tube.progress = systemSetting.replacements.tube
                binding.r20Layout.tank.progress = systemSetting.replacements.tank
                binding.r20Layout.volume.progress = systemSetting.volumeSetting.volume
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetSystemSetting)
            .observe(owner) {
                _toast.value = "系统设置成功"
                LpBleUtil.r20GetSystemSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetMeasureSetting)
            .observe(owner) {
                measureSetting = it.data as MeasureSetting
                if (measureSetting.humidification.humidification == 0x10) {
                    binding.r20Layout.humidification.setSelection(6)
                } else if (measureSetting.humidification.humidification > 6) {
                    binding.r20Layout.humidification.setSelection(0)
                    Log.d("1111111111", "measureSetting.humidification.humidification == ${measureSetting.humidification.humidification}")
                } else {
                    binding.r20Layout.humidification.setSelection(measureSetting.humidification.humidification)
                }
                if (measureSetting.pressureReduce.ipr > 4) {
                    binding.r20Layout.ipr.setSelection(0)
                    Log.d("1111111111", "measureSetting.pressureReduce.ipr == ${measureSetting.pressureReduce.ipr}")
                } else {
                    binding.r20Layout.ipr.setSelection(measureSetting.pressureReduce.ipr)
                }
                if (measureSetting.pressureReduce.epr > 4) {
                    binding.r20Layout.epr.setSelection(0)
                    Log.d("1111111111", "measureSetting.pressureReduce.epr == ${measureSetting.pressureReduce.epr}")
                } else {
                    binding.r20Layout.epr.setSelection(measureSetting.pressureReduce.epr)
                }
                binding.r20Layout.autoStart.isChecked = measureSetting.autoSwitch.autoStart
                binding.r20Layout.autoEnd.isChecked = measureSetting.autoSwitch.autoEnd
                binding.r20Layout.preHeat.isChecked = measureSetting.preHeat.on
                binding.r20Layout.rampPressure.progress = (measureSetting.ramp.pressure - 3.0).div(0.5).toInt()
                binding.r20Layout.rampTime.progress = measureSetting.ramp.time.div(5)
                binding.r20Layout.tubeType.setSelection(measureSetting.tubeType.type-1)
                binding.r20Layout.maskType.setSelection(measureSetting.mask.type-1)
                binding.r20Layout.maskPressure.progress = measureSetting.mask.pressure.toInt()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetMeasureSetting)
            .observe(owner) {
                _toast.value = "测量设置成功"
                LpBleUtil.r20GetMeasureSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetVentilationSetting)
            .observe(owner) {
                ventilationSetting = it.data as VentilationSetting
                binding.r20Layout.cpapPressure.progress = ventilationSetting.pressure.pressure.div(0.5).toInt()
                binding.r20Layout.apapPressureMax.progress = ventilationSetting.pressureMax.max.div(0.5).toInt()
                binding.r20Layout.apapPressureMin.progress = ventilationSetting.pressureMin.min.div(0.5).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.r20Layout.apapPressureMax.min = binding.r20Layout.apapPressureMin.progress
                }
                binding.r20Layout.apapPressureMin.max = binding.r20Layout.apapPressureMax.progress
                binding.r20Layout.ipapPressure.progress = ventilationSetting.pressureInhale.inhale.div(0.5).toInt()
                binding.r20Layout.epapPressure.progress = ventilationSetting.pressureExhale.exhale.div(0.5).toInt()
                binding.r20Layout.inspiratoryTime.progress = ventilationSetting.inhaleDuration.duration.times(10).toInt()
                binding.r20Layout.respiratoryFrequency.progress = ventilationSetting.respiratoryRate.rate
                binding.r20Layout.raiseTime.progress = ventilationSetting.pressureRaiseDuration.duration.div(50)
                binding.r20Layout.iTrigger.setSelection(ventilationSetting.inhaleSensitive.sentive)
                binding.r20Layout.eTrigger.setSelection(ventilationSetting.exhaleSensitive.sentive)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetVentilationSetting)
            .observe(owner) {
                _toast.value = "通气设置成功"
                LpBleUtil.r20GetVentilationSetting(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWarningSetting)
            .observe(owner) {
                warningSetting = it.data as WarningSetting
                binding.r20Layout.leakHigh.setSelection(warningSetting.warningLeak.high)
                binding.r20Layout.lowVentilation.progress = warningSetting.warningVentilation.low
                binding.r20Layout.vtLow.progress = if (warningSetting.warningVt.low == 0) {
                    19
                } else {
                    warningSetting.warningVt.low.times(10)
                }
                binding.r20Layout.rrHigh.progress = warningSetting.warningRrHigh.high
                binding.r20Layout.rrLow.progress = warningSetting.warningRrLow.low
                binding.r20Layout.spo2Low.progress = if (warningSetting.warningSpo2Low.low == 0) {
                    79
                } else {
                    warningSetting.warningSpo2Low.low
                }
                binding.r20Layout.hrHigh.progress = if (warningSetting.warningHrHigh.high == 0) {
                    9
                } else {
                    warningSetting.warningHrHigh.high
                }
                binding.r20Layout.hrLow.progress = if (warningSetting.warningHrLow.low == 0) {
                    5
                } else {
                    warningSetting.warningHrLow.low
                }
                binding.r20Layout.apnea.setSelection(warningSetting.warningApnea.apnea)
            }
    }

}