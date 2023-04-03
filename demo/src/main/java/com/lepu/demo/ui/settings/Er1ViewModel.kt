package com.lepu.demo.ui.settings

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.HexString
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Er1ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.er1Layout.er1FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er1Layout.er1Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.er1Layout.er1Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.er1Layout.er1Code.text.toString())
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
        binding.er1Layout.er1SetSound.setOnCheckedChangeListener { buttonView, isChecked ->
            val temp1 = HexString.trimStr(binding.er1Layout.er1Hr1.text.toString())
            val temp2 = HexString.trimStr(binding.er1Layout.er1Hr2.text.toString())
            if (temp1.isNullOrEmpty() || temp2.isNullOrEmpty()) {
                _toast.value = "阈值输入不能为空"
                return@setOnCheckedChangeListener
            } else {
                if (StringUtil.isNumber(temp1) && StringUtil.isNumber(temp2)) {
                    LpBleUtil.setEr1Vibrate(model, isChecked, temp1.toInt(), temp2.toInt())
                    cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                    binding.sendCmd.text = cmdStr
                } else {
                    _toast.value = "阈值请输入数字"
                }
            }
        }
        binding.er1Layout.er1SetHr.setOnClickListener {
            val temp1 = HexString.trimStr(binding.er1Layout.er1Hr1.text.toString())
            val temp2 = HexString.trimStr(binding.er1Layout.er1Hr2.text.toString())
            if (temp1.isNullOrEmpty() || temp2.isNullOrEmpty()) {
                _toast.value = "输入不能为空"
            } else {
                if (StringUtil.isNumber(temp1) && StringUtil.isNumber(temp2)) {
                    LpBleUtil.setEr1Vibrate(model, switchState, temp1.toInt(), temp2.toInt())
                    cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                    binding.sendCmd.text = cmdStr
                } else {
                    _toast.value = "请输入数字"
                }
            }
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnFactoryInfo)
            .observe(owner) {
                _toast.value = "烧录成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetSwitcherState)
            .observe(owner) {
                LpBleUtil.getEr1VibrateConfig(it.model)
                when (it.model) {
                    Bluetooth.MODEL_ER1 -> {
                        _toast.value = "ER1 设置参数成功"
                    }
                    Bluetooth.MODEL_ER1_N -> {
                        _toast.value = "VBeat 设置参数成功"
                    }
                    Bluetooth.MODEL_HHM1 -> {
                        _toast.value = "HHM1 设置参数成功"
                    }
                    Bluetooth.MODEL_DUOEK -> {
                        _toast.value = "DuoEK 设置参数成功"
                    }
                    Bluetooth.MODEL_HHM2 -> {
                        _toast.value = "HHM2 设置参数成功"
                    }
                    Bluetooth.MODEL_HHM3 -> {
                        _toast.value = "HHM3 设置参数成功"
                    }
                    else -> _toast.value = "ER1 设置参数成功"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1VibrateConfig)
            .observe(owner) {
                val data = it.data as ByteArray
                if (it.model == Bluetooth.MODEL_DUOEK || it.model == Bluetooth.MODEL_HHM2 || it.model == Bluetooth.MODEL_HHM3) {
                    val data = it.data as ByteArray
                    val config = SwitcherConfig.parse(data)
                    binding.er2Layout.er2SetConfig.isChecked = config.switcher
                    binding.content.text = "switcher : ${config.switcher} vector : ${config.vector} motionCount : ${config.motionCount} motionWindows : ${config.motionWindows}"
                    when (it.model) {
                        Bluetooth.MODEL_DUOEK -> {
                            _toast.value = "DuoEK 获取参数成功"
                        }
                        Bluetooth.MODEL_HHM2 -> {
                            _toast.value = "HHM2 获取参数成功"
                        }
                        Bluetooth.MODEL_HHM3 -> {
                            _toast.value = "HHM3 获取参数成功"
                        }
                        else -> _toast.value = "DuoEK 获取参数成功"
                    }
                } else {
                    val config = VbVibrationSwitcherConfig.parse(data)
                    this.config = config
                    binding.er1Layout.er1SetSound.isChecked = config.switcher
                    binding.er1Layout.er1Hr1.setText("${config.hr1}")
                    binding.er1Layout.er1Hr2.setText("${config.hr2}")
                    binding.content.text = "switcher : " + config.switcher + " hr1 : " + config.hr1 + " hr2 : " + config.hr2
                    when (it.model) {
                        Bluetooth.MODEL_ER1 -> {
                            _toast.value = "ER1 获取参数成功"
                        }
                        Bluetooth.MODEL_ER1_N -> {
                            _toast.value = "VBeat 获取参数成功"
                        }
                        Bluetooth.MODEL_HHM1 -> {
                            _toast.value = "HHM1 获取参数成功"
                        }
                        else -> _toast.value = "ER1 获取参数成功"
                    }
                }
            }
    }

}