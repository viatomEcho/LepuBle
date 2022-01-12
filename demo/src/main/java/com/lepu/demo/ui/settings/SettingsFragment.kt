package com.lepu.demo.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.lepu.demo.R
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Ap20BleResponse
import com.lepu.blepro.ble.cmd.Bpw1BleResponse
import com.lepu.blepro.ble.cmd.Pc100BleResponse
import com.lepu.blepro.ble.data.Bp2Config
import com.lepu.blepro.ble.data.FscaleUserInfo
import com.lepu.blepro.ble.data.LeW3Config
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.databinding.FragmentSettingsBinding

/**
 * @ClassName SettingsFragment
 * @Description TODO
 * @Author chenyongfeng
 * @Date 2021/11/23 17:26
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by binding()
    private lateinit var measureTime: Array<String?>

    private var switchState = false
    private var state = 0
    private var volume = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
        LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
    }

    private fun initView() {
        //-------------------------er1/duoek--------------------
        binding.er1SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            var temp = "关"
            if (switchState)
                temp = "开"
            binding.er1SetConfig.text = "声音" + temp
        }
        binding.duoekSetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setDuoekVibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0, 0)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            var temp = "关"
            if (switchState)
                temp = "开"
            binding.duoekSetConfig.text = "声音" + temp
        }
        binding.er1GetConfig.setOnClickListener {
            LpBleUtil.getEr1VibrateConfig(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        //-------------------------er2------------------------
        binding.er2GetConfig.setOnClickListener {
            LpBleUtil.getEr2SwitcherState(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.er2SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setEr2SwitcherState(Constant.BluetoothConfig.currentModel[0], switchState)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            var temp = "关"
            if (switchState)
                temp = "开"
            binding.er2SetConfig.text = "声音" + temp
        }
        //-------------------------lew3------------------------
        binding.lew3Bound.setOnClickListener {
            LpBleUtil.boundDevice(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.lew3GetConfig.setOnClickListener {
            LpBleUtil.getLeW3Config(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.lew3SetConfig.setOnClickListener {
            LpBleUtil.setLeW3Config(Constant.BluetoothConfig.currentModel[0], "192.168.111.222", 5000)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }

        //-------------------------bp2/bp2A--------------------
        binding.bp2SetState.setOnClickListener {
            state++
            if (state > 5)
                state = 0
            LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], state)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.bp2SetState.text = "设备状态" + state
        }
        binding.bp2GetConfig.setOnClickListener {
            LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.bp2SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], switchState, volume)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.bp2SetVolume.setOnClickListener {
            volume++
            if (volume > 3)
                volume = 0
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], switchState, volume)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.bp2SetVolume.text = "音量" + volume
        }

        //-------------------------F4,F5-----------------------
        binding.scaleUserInfo.setOnClickListener {
            var userInfo = FscaleUserInfo()
            LpBleUtil.setUserInfo(Bluetooth.MODEL_F5_SCALE, userInfo)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.scaleUserList.setOnClickListener {
            var userList = arrayListOf<FscaleUserInfo>()
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            LpBleUtil.setUserList(Bluetooth.MODEL_F5_SCALE, userList)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        //-------------------------pc100-----------------------
        binding.pc100BpState.setOnClickListener {
            LpBleUtil.getBpState(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.pc100BoState.setOnClickListener {
            LpBleUtil.getBoState(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        //-------------------------ap20-----------------------
        binding.ap20Switch.setOnClickListener {
            switchState = !switchState
            var temp = "关"
            if (switchState) {
                LpBleUtil.setApConfig(Constant.BluetoothConfig.currentModel[0], 1, 1)
                temp = "开"
            } else {
                LpBleUtil.setApConfig(Constant.BluetoothConfig.currentModel[0], 1, 0)
            }
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.ap20Switch.text = "警报" + temp
        }
        binding.ap20GetConfig.setOnClickListener {
            state++
            if (state > 5)
                state = 0
            if (state == 5) {
                LpBleUtil.getBattery(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.getApConfig(Constant.BluetoothConfig.currentModel[0], state)
            }
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.ap20GetConfig.text = "获取参数" + state
        }
        binding.ap20SetConfig.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            if (state == 2 || state == 3) {
                LpBleUtil.setApConfig(Constant.BluetoothConfig.currentModel[0], state, 90)
            } else if (state == 4) {
                LpBleUtil.setApConfig(Constant.BluetoothConfig.currentModel[0], state, 125)
            } else if (state == 0) {
                volume++
                if (volume > 6)
                    volume = 0
                LpBleUtil.setApConfig(Constant.BluetoothConfig.currentModel[0], state, volume)
            }
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.ap20SetConfig.text = "设置参数" + state
        }

    }

    private fun initLiveEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
            .observe(this, {
                binding.content.text = (it.data as Int).toString()
                LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
            .observe(this, {
//                var config = it.data as Bp2Config
//                binding.content.text = config.toString()
//                switchState = config.switchState
//                volume = config.volume
//                binding.bp2SetConfig.text = "声音关"
//                binding.bp2SetVolume.text = "音量" + volume
//                if (switchState)
//                    binding.bp2SetConfig.text = "声音开"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1VibrateConfig)
            .observe(this, {
                binding.content.text = bytesToHex(it.data as ByteArray)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SwitcherState)
            .observe(this, {
                binding.content.text = bytesToHex(it.data as ByteArray)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStatus)
            .observe(this, {
                var data = it.data as Pc100BleResponse.BpStatus
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20Battery)
            .observe(this, {
                var data = it.data as Int
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20ConfigInfo)
            .observe(this, {
                var data = it.data as Ap20BleResponse.ConfigInfo
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3BoundDevice)
            .observe(this, {
                var data = it.data as Boolean
                binding.content.text = "请求绑定 : " + data
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3GetConfig)
            .observe(this, {
                var data = it.data as LeW3Config
                binding.content.text = data.toString()
            })

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