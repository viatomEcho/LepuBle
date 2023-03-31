package com.lepu.demo.ui.settings

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Pc68bBleResponse
import com.lepu.blepro.ble.data.Pc68bConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding

class Pc68bViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.pc68bDeleteFile.setOnClickListener {
            LpBleUtil.pc68bDeleteFile(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc68bStateInfo.setOnClickListener {
            LpBleUtil.pc68bGetStateInfo(model, 5)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc68bGetConfig.setOnClickListener {
            LpBleUtil.pc68bGetConfig(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc68bSetConfig.setOnClickListener {
            switchState = !switchState
            (config as Pc68bConfig).alert = switchState
            (config as Pc68bConfig).pulseBeep = switchState
            (config as Pc68bConfig).sensorAlert = switchState
            (config as Pc68bConfig).spo2Lo = 90
            (config as Pc68bConfig).prLo = 90
            (config as Pc68bConfig).prHi = 90
            LpBleUtil.pc68bSetConfig(model, (config as Pc68bConfig))
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bStatusInfo)
            .observe(owner) {
                val data = it.data as Pc68bBleResponse.StatusInfo
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeleteFile)
            .observe(owner) {
                val data = it.data as Boolean
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bConfigInfo)
            .observe(owner) {
                val config = it.data as Pc68bConfig
                this.config = config
                binding.content.text = "$config"
            }
    }

}