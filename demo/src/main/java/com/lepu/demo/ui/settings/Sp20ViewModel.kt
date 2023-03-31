package com.lepu.demo.ui.settings

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.Sp20Config
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Sp20ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.sp20Layout.getBattery.setOnClickListener {
            LpBleUtil.sp20GetBattery(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.sp20Layout.setLowSpo2.setOnClickListener {
            val temp = HexString.trimStr(binding.sp20Layout.lowSpo2.text.toString())
            if (StringUtil.isNumber(temp)) {
                (config as Sp20Config).type = 2
                (config as Sp20Config).value = temp.toInt()
                LpBleUtil.sp20SetConfig(model, (config as Sp20Config))
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.sp20Layout.setLowHr.setOnClickListener {
            val temp = HexString.trimStr(binding.sp20Layout.lowHr.text.toString())
            if (StringUtil.isNumber(temp)) {
                (config as Sp20Config).type = 3
                (config as Sp20Config).value = temp.toInt()
                LpBleUtil.sp20SetConfig(model, (config as Sp20Config))
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.sp20Layout.setHighHr.setOnClickListener {
            val temp = HexString.trimStr(binding.sp20Layout.highHr.text.toString())
            if (StringUtil.isNumber(temp)) {
                (config as Sp20Config).type = 4
                (config as Sp20Config).value = temp.toInt()
                LpBleUtil.sp20SetConfig(model, (config as Sp20Config))
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery)
            .observe(owner) {
                val data = it.data as Int
                binding.content.text = "电量${data}"
                binding.sp20Layout.getBattery.text = "电量 ${when (data) {
                    0 -> "0-25%"
                    1 -> "25-50%"
                    2 -> "50-75%"
                    3 -> "75-100%"
                    else -> "0"
                }}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetConfig)
            .observe(owner) {
                val config = it.data as Sp20Config
                this.config = config
                when (config.type) {
                    2 -> {
                        if (config.value == 1) {
                            _toast.value = "设置血氧过低阈值成功"
                        } else {
                            _toast.value = "设置血氧过低阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 2)
                    }
                    3 -> {
                        if (config.value == 1) {
                            _toast.value = "设置脉率过低阈值成功"
                        } else {
                            _toast.value = "设置脉率过低阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 3)
                    }
                    4 -> {
                        if (config.value == 1) {
                            _toast.value = "设置脉率过高阈值成功"
                        } else {
                            _toast.value = "设置脉率过高阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 4)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig)
            .observe(owner) {
                val config = it.data as Sp20Config
                this.config = config
                when (config.type) {
                    2 -> binding.sp20Layout.lowSpo2.setText("${config.value}")
                    3 -> binding.sp20Layout.lowHr.setText("${config.value}")
                    4 -> binding.sp20Layout.highHr.setText("${config.value}")
                }
            }
    }
}