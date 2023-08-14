package com.lepu.demo.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyBleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class OxyViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        this.context = context
        binding.o2Layout.o2FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.o2Layout.o2Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else {
                config.setHwVersion(tempVersion.first())
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.o2Layout.o2Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else {
                config.setSnCode(tempSn)
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.o2Layout.o2Code.text.toString())
            if (tempCode.isNullOrEmpty()) {
                enableCode = false
            } else {
                config.setBranchCode(tempCode)
            }
            config.setBurnFlag(enableSn, enableVersion, enableCode)
            LpBleUtil.burnFactoryInfo(model, config)
            val deviceFactoryData = DeviceFactoryData()
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            _deviceFactoryData.value = deviceFactoryData
        }
        binding.o2Layout.o2SetOxiThr.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2OxiThr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_OXI_THR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2SetHrThr1.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2HrThr1.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_HR_LOW_THR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2SetHrThr2.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2HrThr2.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_HR_HIGH_THR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2OxiSwitch.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2Oxi.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_OXI_SWITCH, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.setLightMode.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.lightMode.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_LIGHTING_MODE, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2HrSwitch.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2Hr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_HR_SWITCH, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2SetMotor.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2Motor.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_MOTOR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2SetBuzzer.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2Buzzer.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_BUZZER, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.setLightLevel.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.lightLevel.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_LIGHT_STR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2SetMtThr.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2MtThr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_MT_THR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2MtSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_MT_SW, 1)
            } else {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_MT_SW, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.o2Layout.o2SetIvThr.setOnClickListener {
            val temp = HexString.trimStr(binding.o2Layout.o2IvThr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_IV_THR, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.o2Layout.o2IvSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_IV_SW, 1)
            } else {
                LpBleUtil.updateSetting(model, OxyBleCmd.SYNC_TYPE_IV_SW, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyBurnFactoryInfo)
            .observe(owner) {
                _toast.value = context.getString(R.string.burn_info_success)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(owner) {
                val data = it.data as OxyBleResponse.OxyInfo
                binding.o2Layout.o2OxiThr.setText("${data.oxiThr}")
                binding.o2Layout.o2HrThr1.setText("${data.hrLowThr}")
                binding.o2Layout.o2HrThr2.setText("${data.hrHighThr}")
                binding.o2Layout.o2Oxi.setText("${data.oxiSwitch}")
                binding.o2Layout.o2Hr.setText("${data.hrSwitch}")
                binding.o2Layout.o2Motor.setText("${data.motor}")
                binding.o2Layout.o2Buzzer.setText("${data.buzzer}")
                binding.o2Layout.lightMode.setText("${data.lightingMode}")
                binding.o2Layout.lightLevel.setText("${data.lightStr}")
                binding.o2Layout.o2MtThr.setText("${data.mtThr}")
                binding.o2Layout.o2MtSwitch.isChecked = data.mtSwitch == 1
                binding.o2Layout.o2IvThr.setText("${data.ivThr}")
                binding.o2Layout.o2IvSwitch.isChecked = data.ivSwitch == 1
                binding.content.text = "$data"
            }
    }
}