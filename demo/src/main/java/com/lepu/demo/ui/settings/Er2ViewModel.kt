package com.lepu.demo.ui.settings

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.SwitcherConfig
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.HexString
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Er2ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        this.context = context
        binding.er2Layout.er2FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er2Layout.er2Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = HexString.trimStr(binding.er2Layout.er2Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = HexString.trimStr(binding.er2Layout.er2Code.text.toString())
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
        binding.er2Layout.er2SetConfig.setOnCheckedChangeListener { buttonView, isChecked ->
            if (model == Bluetooth.MODEL_ER2
                || model == Bluetooth.MODEL_LP_ER2) {
                LpBleUtil.setEr2SwitcherState(model, isChecked)
            } else {
                LpBleUtil.setDuoekVibrate(model, isChecked, 0, 0, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        /*LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnFactoryInfo)
            .observe(owner) {
                _toast.value = context.getString(R.string.burn_info_success)
            }*/
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig)
            .observe(owner) {
                LpBleUtil.getEr2SwitcherState(it.model)
                when (it.model) {
                    Bluetooth.MODEL_ER2 -> {
                        _toast.value = "ER2 设置参数成功"
                    }
                    Bluetooth.MODEL_LP_ER2 -> {
                        _toast.value = "LP ER2 设置参数成功"
                    }
                    else -> _toast.value = "ER2 设置参数成功"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfig)
            .observe(owner) {
                val data = it.data as ByteArray
                val config = SwitcherConfig.parse(data)
                this.config = config
                binding.er2Layout.er2SetConfig.isChecked = config.switcher
                binding.content.text = "switcher : " + config.switcher + " vector : " + config.vector + " motionCount : " + config.motionCount + " motionWindows : " + config.motionWindows
                when (it.model) {
                    Bluetooth.MODEL_ER2 -> {
                        _toast.value = "ER2 获取参数成功"
                    }
                    Bluetooth.MODEL_LP_ER2 -> {
                        _toast.value = "LP ER2 获取参数成功"
                    }
                    else -> _toast.value = "ER2 获取参数成功"
                }
            }
    }

}