package com.lepu.demo.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyIIBleCmd
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.OxyIIConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.isNumber
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class OxyIIViewModel : SettingViewModel() {
    
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        this.context = context
        binding.oxy2Layout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.oxy2Layout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.oxy2Layout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.oxy2Layout.code.text.toString())
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
        binding.oxy2Layout.setSpo2MotorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null || !buttonView.isPressed) return@setOnCheckedChangeListener
            (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.SPO2_SWITCH
            (config as OxyIIConfig).spo2Switch.motorOn = isChecked
            LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
        }
        binding.oxy2Layout.setSpo2BuzzerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null || !buttonView.isPressed) return@setOnCheckedChangeListener
            (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.SPO2_SWITCH
            (config as OxyIIConfig).spo2Switch.buzzerOn = isChecked
            LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
        }
        binding.oxy2Layout.setHrMotorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null || !buttonView.isPressed) return@setOnCheckedChangeListener
            (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.HR_SWITCH
            (config as OxyIIConfig).hrSwitch.motorOn = isChecked
            LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
        }
        binding.oxy2Layout.setHrBuzzerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.HR_SWITCH
            (config as OxyIIConfig).hrSwitch.buzzerOn = isChecked
            LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("低", "中", "高")).apply {
            binding.oxy2Layout.brightnessMode.adapter = this
        }
        binding.oxy2Layout.brightnessMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (config == null) return
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.BRIGHTNESS
                (config as OxyIIConfig).brightnessMode.mode = position
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("Standard", "Always Off", "Always On")).apply {
            binding.oxy2Layout.displayMode.adapter = this
        }
        binding.oxy2Layout.displayMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (config == null) return
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.DISPLAY_MODE
                (config as OxyIIConfig).displayMode.mode = position
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.oxy2Layout.setSpo2.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.spo2Thr.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.SPO2_LOW
                (config as OxyIIConfig).spo2Low.low = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.oxy2Layout.setLowHr.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.hrLowThr.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.HR_LOW
                (config as OxyIIConfig).hrLow.low = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.oxy2Layout.setHighHr.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.hrHighThr.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.HR_HIGH
                (config as OxyIIConfig).hrHi.hi = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.oxy2Layout.setMotor.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.motor.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.MOTOR
                (config as OxyIIConfig).motor.motor = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.oxy2Layout.setBuzzer.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.buzzer.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.BUZZER
                (config as OxyIIConfig).buzzer.buzzer = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.oxy2Layout.setInterval.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.oxy2Layout.interval.text.toString())
            if (isNumber(temp)) {
                (config as OxyIIConfig).type = OxyIIBleCmd.ConfigType.INTERVAL
                (config as OxyIIConfig).storageInterval.interval = temp.toInt()
                LpBleUtil.oxyIISetConfig(model, (config as OxyIIConfig))
            } else {
                _toast.value = "请输入数字"
            }
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIBurnFactoryInfo)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = context.getString(R.string.burn_info_success)
                } else {
                    _toast.value = "烧录失败"
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetConfig)
            .observe(owner) {
                val config = it.data as OxyIIConfig
                this.config = config
                binding.oxy2Layout.setSpo2MotorSwitch.isChecked = config.spo2Switch.motorOn
                binding.oxy2Layout.setSpo2BuzzerSwitch.isChecked = config.spo2Switch.buzzerOn
                binding.oxy2Layout.setHrMotorSwitch.isChecked = config.hrSwitch.motorOn
                binding.oxy2Layout.setHrBuzzerSwitch.isChecked = config.hrSwitch.buzzerOn
                binding.oxy2Layout.brightnessMode.setSelection(config.brightnessMode.mode)
                binding.oxy2Layout.displayMode.setSelection(config.displayMode.mode)
                binding.oxy2Layout.spo2Thr.setText("${config.spo2Low.low}")
                binding.oxy2Layout.hrHighThr.setText("${config.hrHi.hi}")
                binding.oxy2Layout.hrLowThr.setText("${config.hrLow.low}")
                binding.oxy2Layout.motor.setText("${config.motor.motor}")
                binding.oxy2Layout.buzzer.setText("${config.buzzer.buzzer}")
                binding.oxy2Layout.interval.setText("${config.storageInterval.interval}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIISetConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置成功"
                } else {
                    _toast.value = "设置失败"
                }
                LpBleUtil.oxyIIGetConfig(it.model)
            }
    }

}