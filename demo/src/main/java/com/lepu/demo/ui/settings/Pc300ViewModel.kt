package com.lepu.demo.ui.settings

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding

class Pc300ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var context: Context

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        this.context = context
        ArrayAdapter(context, R.layout.simple_list_item_1, arrayListOf("8bit", "12bit")).apply {
            binding.pc300Layout.digit.adapter = this
        }
        binding.pc300Layout.digit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 1:8bit 2:12bit
                LpBleUtil.pc300SetEcgDataDigit(model, position+1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        /*binding.pc300Layout.setGluUnit.setOnClickListener {
            state++
            if (state > Pc300BleCmd.GluUnit.MG_DL) {
                state = Pc300BleCmd.GluUnit.MMOL_L
            }
            LpBleUtil.pc300Layout.setGluUnit(model, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc300Layout.setId.setOnClickListener {
            state++
            LpBleUtil.pc300SetDeviceId(model, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc300Layout.getId.setOnClickListener {
            LpBleUtil.pc300GetDeviceId(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }*/
        ArrayAdapter(context, R.layout.simple_list_item_1, arrayListOf("爱奥乐", "百捷", "CE")).apply {
            binding.pc300Layout.bsType.adapter = this
        }
        binding.pc300Layout.bsType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 1:爱奥乐 2:百捷 4:CE
                if (position == 2) {
                    LpBleUtil.pc300SetGlucometerType(model, position+2)
                } else {
                    LpBleUtil.pc300SetGlucometerType(model, position+1)
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        /*binding.pc300Layout.setTempMode.setOnClickListener {
                state++
                if (state > Pc300BleCmd.TempMode.OBJECT_F) {
                state = Pc300BleCmd.TempMode.EAR_C
            }
            LpBleUtil.pc300SetTempMode(model, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc300Layout.getTempMode.setOnClickListener {
            LpBleUtil.pc300GetTempMode(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc300Layout.setBpMode.setOnClickListener {
            state++
            if (state > Pc300BleCmd.BpMode.CHILD_MODE) {
                state = Pc300BleCmd.BpMode.ADULT_MODE
            }
            LpBleUtil.pc300SetBpMode(model, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }
        binding.pc300Layout.getBpMode.setOnClickListener {
            LpBleUtil.pc300GetBpMode(model)
            cmdStr = "send : " + LpBleUtil.getSendCmd(model)
            binding.sendCmd.text = cmdStr
        }*/
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300SetGlucometerType)
            .observe(owner) {
                val data = it.data as Boolean
                _toast.value = if (data) {
                    "血糖仪类型设置成功"
                } else {
                    "血糖仪类型设置失败"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GetGlucometerType)
            .observe(owner) {
                val data = it.data as Int
                if (data == 4) {
                    binding.pc300Layout.bsType.setSelection(data-2)
                } else {
                    binding.pc300Layout.bsType.setSelection(data-1)
                }
                _toast.value = "获取血糖仪类型成功"
            }
    }
}