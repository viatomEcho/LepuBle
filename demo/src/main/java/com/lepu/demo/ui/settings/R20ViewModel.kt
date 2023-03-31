package com.lepu.demo.ui.settings

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.R20BleResponse
import com.lepu.blepro.ble.data.r20.SystemSetting
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding

class R20ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.r20Layout.bound.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.r20DeviceBound(model, isChecked)
        }
        binding.r20Layout.doctorMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                var pin = binding.r20Layout.pin.text.toString()
                pin = if (pin == "") {
                    "0319"
                } else {
                    pin
                }
                LpBleUtil.r20DoctorMode(model, pin, System.currentTimeMillis().div(1000))
            } else {
                binding.r20Layout.doctorMode.isChecked = true
            }
        }
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
                binding.r20Layout.doctorMode.isChecked = data.success
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20RtState)
            .observe(owner) {
                val data = it.data as R20BleResponse.RtState

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetSystemSetting)
            .observe(owner) {
                val data = it.data as SystemSetting

            }
    }


}