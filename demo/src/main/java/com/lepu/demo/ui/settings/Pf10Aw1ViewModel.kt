package com.lepu.demo.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Pf10Aw1BleCmd
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.Pf10Aw1Config
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.isNumber
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Pf10Aw1ViewModel : SettingViewModel() {
    
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        this.context = context
        binding.pf10aw1Layout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.pf10aw1Layout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.pf10aw1Layout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.pf10aw1Layout.code.text.toString())
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
        binding.pf10aw1Layout.setAlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.ALARM_SWITCH
            (config as Pf10Aw1Config).alarmSwitch.on = isChecked
            LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
        }
        binding.pf10aw1Layout.setBeepSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.BEEP_SWITCH
            (config as Pf10Aw1Config).beepSwitch.on = isChecked
            LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
        }
        binding.pf10aw1Layout.setBleSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.BLE_SWITCH
            (config as Pf10Aw1Config).bleSwitch.on = isChecked
            LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
        }
        binding.pf10aw1Layout.setLanguage.setOnCheckedChangeListener { group, checkedId ->
            if (config == null) return@setOnCheckedChangeListener
            (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.LANGUAGE
            if (checkedId == R.id.english) {
                (config as Pf10Aw1Config).language.language = 0
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            } else {
                (config as Pf10Aw1Config).language.language = 1
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            }
        }
        binding.pf10aw1Layout.setMeasureMode.setOnCheckedChangeListener { group, checkedId ->
            if (config == null) return@setOnCheckedChangeListener
            (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.MEASURE_MODE
            if (checkedId == R.id.spot_mode) {
                (config as Pf10Aw1Config).measureMode.mode = 1
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            } else {
                (config as Pf10Aw1Config).measureMode.mode = 2
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            }
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("常亮", "1分钟", "3分钟", "5分钟")).apply {
            binding.pf10aw1Layout.esMode.adapter = this
        }
        binding.pf10aw1Layout.esMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (config == null) return
                (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.ES_MODE
                (config as Pf10Aw1Config).esMode.mode = position
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.pf10aw1Layout.setSpo2.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.pf10aw1Layout.spo2Thr.text.toString())
            if (isNumber(temp)) {
                (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.SPO2_LOW
                (config as Pf10Aw1Config).spo2Low.low = temp.toInt()
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.pf10aw1Layout.setHighPr.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.pf10aw1Layout.prHighThr.text.toString())
            if (isNumber(temp)) {
                (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.PR_HIGH
                (config as Pf10Aw1Config).prHi.hi = temp.toInt()
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            } else {
                _toast.value = "请输入数字"
            }
        }
        binding.pf10aw1Layout.setLowPr.setOnClickListener {
            if (config == null) return@setOnClickListener
            val temp = trimStr(binding.pf10aw1Layout.prLowThr.text.toString())
            if (isNumber(temp)) {
                (config as Pf10Aw1Config).type = Pf10Aw1BleCmd.ConfigType.PR_LOW
                (config as Pf10Aw1Config).prLow.low = temp.toInt()
                LpBleUtil.pf10Aw1SetConfig(model, (config as Pf10Aw1Config))
            } else {
                _toast.value = "请输入数字"
            }
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1BurnFactoryInfo)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = context.getString(R.string.burn_info_success)
                } else {
                    _toast.value = "烧录失败"
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetConfig)
            .observe(owner) {
                val config = it.data as Pf10Aw1Config
                this.config = config
                binding.pf10aw1Layout.setAlarmSwitch.isChecked = config.alarmSwitch.on
                binding.pf10aw1Layout.setBeepSwitch.isChecked = config.beepSwitch.on
                binding.pf10aw1Layout.setBleSwitch.isChecked = config.bleSwitch.on
                if (config.measureMode.mode == 1) {
                    binding.pf10aw1Layout.setMeasureMode.check(R.id.spot_mode)
                } else {
                    binding.pf10aw1Layout.setMeasureMode.check(R.id.continue_mode)
                }
                if (config.language.language == 0) {
                    binding.pf10aw1Layout.setLanguage.check(R.id.english)
                } else {
                    binding.pf10aw1Layout.setLanguage.check(R.id.chinese)
                }
                binding.pf10aw1Layout.esMode.setSelection(config.esMode.mode)
                binding.pf10aw1Layout.spo2Thr.setText("${config.spo2Low.low}")
                binding.pf10aw1Layout.prHighThr.setText("${config.prHi.hi}")
                binding.pf10aw1Layout.prLowThr.setText("${config.prLow.low}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1SetConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置成功"
                } else {
                    _toast.value = "设置失败"
                }
                LpBleUtil.pf10Aw1GetConfig(it.model)
            }
    }

}