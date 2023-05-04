package com.lepu.demo.ui.settings

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Ap20BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Ap20ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.context = context
        this.binding = binding
        binding.ap20Layout.setSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                LpBleUtil.ap20SetConfig(model, 1, 1)
            } else {
                LpBleUtil.ap20SetConfig(model, 1, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        ArrayAdapter(context,
            R.layout.simple_list_item_1,
            arrayListOf("0", "1", "2", "3", "4", "5")
        ).apply {
            binding.ap20Layout.lightLevelSpinner.adapter = this
        }
        binding.ap20Layout.lightLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.ap20SetConfig(model, 0, position)
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        binding.ap20Layout.setLowSpo2.setOnClickListener {
            val temp = HexString.trimStr(binding.ap20Layout.lowSpo2.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.ap20SetConfig(model, 2, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.ap20Layout.setLowHr.setOnClickListener {
            val temp = HexString.trimStr(binding.ap20Layout.lowHr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.ap20SetConfig(model, 3, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
        binding.ap20Layout.setHighHr.setOnClickListener {
            val temp = HexString.trimStr(binding.ap20Layout.highHr.text.toString())
            if (StringUtil.isNumber(temp)) {
                LpBleUtil.ap20SetConfig(model, 4, temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(model)
                binding.sendCmd.text = cmdStr
            } else {
                _toast.value = "输入不正确，请重新输入"
            }
        }
    }

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel)
            .observe(owner) {
                val data = it.data as Int
                binding.content.text = "电量${data}"
                binding.ap20Layout.getBattery.text = "电量 ${when (data) {
                    0 -> "0-25%"
                    1 -> "25-50%"
                    2 -> "50-75%"
                    3 -> "75-100%"
                    else -> "0"
                }}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetConfigResult)
            .observe(owner) {
                val data = it.data as Ap20BleResponse.ConfigInfo
                when (data.type) {
                    0 -> {
                        if (data.data == 1) {
                            _toast.value = "设置背光等级成功"
                        } else {
                            _toast.value = "设置背光等级失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 0)
                    }
                    1 -> {
                        if (data.data == 1) {
                            _toast.value = "设置警报成功"
                        } else {
                            _toast.value = "设置警报失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 1)
                    }
                    2 -> {
                        if (data.data == 1) {
                            _toast.value = "设置血氧过低阈值成功"
                        } else {
                            _toast.value = "设置血氧过低阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 2)
                    }
                    3 -> {
                        if (data.data == 1) {
                            _toast.value = "设置脉率过低阈值成功"
                        } else {
                            _toast.value = "设置脉率过低阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 3)
                    }
                    4 -> {
                        if (data.data == 1) {
                            _toast.value = "设置脉率过高阈值成功"
                        } else {
                            _toast.value = "设置脉率过高阈值失败"
                        }
                        LpBleUtil.ap20GetConfig(it.model, 4)
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult)
            .observe(owner) {
                val data = it.data as Ap20BleResponse.ConfigInfo
                when (data.type) {
                    0 -> binding.ap20Layout.lightLevelSpinner.setSelection(data.data)
                    1 -> {
                        binding.ap20Layout.setSwitch.isChecked = data.data == 1
                        switchState = data.data == 1
                    }
                    2 -> binding.ap20Layout.lowSpo2.setText("${data.data}")
                    3 -> binding.ap20Layout.lowHr.setText("${data.data}")
                    4 -> binding.ap20Layout.highHr.setText("${data.data}")
                }
            }
    }
    
}