package com.lepu.demo.ui.settings

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.BtpBleResponse
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class BtpViewModel : SettingViewModel() {
    
    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.btpLayout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.btpLayout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.btpLayout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.btpLayout.code.text.toString())
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
        binding.btpLayout.setHrSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as BtpBleResponse.ConfigInfo).hrSwitch = isChecked
            LpBleUtil.btpSetSystemSwitch(
                model,
                (config as BtpBleResponse.ConfigInfo).hrSwitch,
                (config as BtpBleResponse.ConfigInfo).lightSwitch,
                (config as BtpBleResponse.ConfigInfo).tempSwitch)
        }
        binding.btpLayout.setLightSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as BtpBleResponse.ConfigInfo).lightSwitch = isChecked
            LpBleUtil.btpSetSystemSwitch(
                model,
                (config as BtpBleResponse.ConfigInfo).hrSwitch,
                (config as BtpBleResponse.ConfigInfo).lightSwitch,
                (config as BtpBleResponse.ConfigInfo).tempSwitch)
        }
        binding.btpLayout.setTempSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (config == null) return@setOnCheckedChangeListener
            (config as BtpBleResponse.ConfigInfo).tempSwitch = isChecked
            LpBleUtil.btpSetSystemSwitch(
                model,
                (config as BtpBleResponse.ConfigInfo).hrSwitch,
                (config as BtpBleResponse.ConfigInfo).lightSwitch,
                (config as BtpBleResponse.ConfigInfo).tempSwitch)
        }
        binding.btpLayout.setTempUnit.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.unit_c) {
                LpBleUtil.btpSetTempUnit(model, 0)
            } else {
                LpBleUtil.btpSetTempUnit(model, 1)
            }
        }
        binding.btpLayout.setLowHr.setOnClickListener {
            val temp = HexString.trimStr(binding.btpLayout.hrLowThr.text.toString())
            if (temp.isEmpty() || !StringUtil.isNumber(temp)) {
                _toast.value = "请输入正确阈值！"
                return@setOnClickListener
            }
            LpBleUtil.btpSetLowHr(model, temp.toInt())
        }
        binding.btpLayout.setHighHr.setOnClickListener {
            val temp = HexString.trimStr(binding.btpLayout.hrHighThr.text.toString())
            if (temp.isEmpty() || !StringUtil.isNumber(temp)) {
                _toast.value = "请输入正确阈值！"
                return@setOnClickListener
            }
            LpBleUtil.btpSetHighHr(model, temp.toInt())
        }
        binding.btpLayout.setLowTemp.setOnClickListener {
            val temp = HexString.trimStr(binding.btpLayout.tempLowThr.text.toString())
            if (temp.isEmpty() || !StringUtil.isNumber(temp)) {
                _toast.value = "请输入正确阈值！"
                return@setOnClickListener
            }
            LpBleUtil.btpSetLowTemp(model, temp.toInt())
        }
        binding.btpLayout.setHighTemp.setOnClickListener {
            val temp = HexString.trimStr(binding.btpLayout.tempHighThr.text.toString())
            if (temp.isEmpty() || !StringUtil.isNumber(temp)) {
                _toast.value = "请输入正确阈值！"
                return@setOnClickListener
            }
            LpBleUtil.btpSetHighTemp(model, temp.toInt())
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpBurnFactoryInfo)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "烧录成功"
                } else {
                    _toast.value = "烧录失败"
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetConfig)
            .observe(owner) {
                val config = it.data as BtpBleResponse.ConfigInfo
                this.config = config
                binding.btpLayout.setHrSwitch.isChecked = config.hrSwitch
                binding.btpLayout.setLightSwitch.isChecked = config.lightSwitch
                binding.btpLayout.setTempSwitch.isChecked = config.tempSwitch
                if (config.tempUnit == 0) {
                    binding.btpLayout.setTempUnit.check(R.id.unit_c)
                } else {
                    binding.btpLayout.setTempUnit.check(R.id.unit_f)
                }
                binding.btpLayout.hrLowThr.setText("${config.hrLowThr}")
                binding.btpLayout.hrHighThr.setText("${config.hrHighThr}")
                binding.btpLayout.tempLowThr.setText("${config.tempLowThr}")
                binding.btpLayout.tempHighThr.setText("${config.tempHighThr}")
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowHr)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置心率低阈值成功"
                } else {
                    _toast.value = "设置心率低阈值失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighHr)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置心率高阈值成功"
                } else {
                    _toast.value = "设置心率高阈值失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetTempUnit)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置温度单位成功"
                } else {
                    _toast.value = "设置温度单位失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetLowTemp)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置温度低阈值成功"
                } else {
                    _toast.value = "设置温度低阈值失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetHighTemp)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置温度高阈值成功"
                } else {
                    _toast.value = "设置温度高阈值失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpSetSystemSwitch)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    _toast.value = "设置系统开关成功"
                } else {
                    _toast.value = "设置系统开关失败"
                }
                LpBleUtil.btpGetConfig(it.model)
            }
    }

}