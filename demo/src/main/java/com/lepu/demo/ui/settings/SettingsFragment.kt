package com.lepu.demo.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.lepu.demo.R
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Bpw1BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.databinding.FragmentSettingsBinding

/**
 * @ClassName SettingsFragment
 * @Description TODO
 * @Author chenyongfeng
 * @Date 2021/11/23 17:26
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by binding()
    private var measureTime = arrayOfNulls<String>(3)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
    }

    private fun initView() {
        binding.btGetInfo.setOnClickListener {
            LpBleUtil.getInfo(Bluetooth.MODEL_BPW1)
        }
        binding.btGetList.setOnClickListener {
            LpBleUtil.getFileList(Bluetooth.MODEL_BPW1)
        }
        binding.btStartBp.setOnClickListener {
            LpBleUtil.startBp(Bluetooth.MODEL_BPW1)
        }
        binding.btStopBp.setOnClickListener {
            LpBleUtil.stopBp(Bluetooth.MODEL_BPW1)
        }
        binding.btSetMeasureTime.setOnClickListener {
            measureTime[0] = "1,0,5,0,120,1,3"
            measureTime[1] = "6,0,12,0,150,2,3"
            measureTime[2] = "15,0,20,0,240,3,3"
            LpBleUtil.setMeasureTime(Bluetooth.MODEL_BPW1, measureTime)
        }
        binding.btGetMeasureTime.setOnClickListener {
            LpBleUtil.getMeasureTime(Bluetooth.MODEL_BPW1)
        }
        binding.btSetTimingSwitch.setOnClickListener {
            LpBleUtil.setTimingSwitch(Bluetooth.MODEL_BPW1, true)
        }
    }

    private fun initLiveEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetTime)
            .observe(this, {
                binding.content.text = "device init"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureState)
            .observe(this, {
                var state = it.data as Int
                binding.content.text = if(state == 1) "start bp" else "stop bp"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1RtData)
            .observe(this, {
                var data = it.data as Bpw1BleResponse.RtData
                binding.content.text = "压力值 ：" + data.pressure.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1ErrorResult)
            .observe(this, {
                var data = it.data as Bpw1BleResponse.ErrorResult
                binding.content.text = "测量出错 类型：" + data.type + " 结果：" + data.result
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetFileListComplete)
            .observe(this, {
                var bpw1FileList = it.data as Bpw1BleResponse.Bpw1FileList
                binding.content.text = bpw1FileList.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureResult)
            .observe(this, {
                var data = it.data as Bpw1BleResponse.BpData
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1DeviceInfo)
            .observe(this, {
                var data = it.data as Bpw1BleResponse.DeviceInfo
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetMeasureTime)
            .observe(this, {
                var data = it.data as Bpw1BleResponse.MeasureTime
                binding.content.text = data.toString()
            })
    }

}