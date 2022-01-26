package com.lepu.demo.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lepu.demo.R
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Bpw1BleResponse
import com.lepu.blepro.ble.cmd.Pc100BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.WifiAdapter
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

    private var fileType = Ble.File.ECG_TYPE

    private lateinit var adapter: WifiAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
    }

    private fun initView() {
        //-------------------------er1/duoek--------------------
        binding.er1SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.duoekSetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setDuoekVibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0, 0)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
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
        }

        //-------------------------bp2/bp2A--------------------
        binding.bp2SetState.setOnClickListener {
            LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], state)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            state++
            if (state > 5)
                state = 0
        }
        binding.bp2GetConfig.setOnClickListener {
            LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.bp2SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], switchState)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }

        //-------------------------bp2w------------------------
        binding.bp2wGetWifiRoute.setOnClickListener {
            LpBleUtil.getWifiDevice(Constant.BluetoothConfig.currentModel[0])
        }
        binding.bp2wUserList.setOnClickListener {
            val icon1 = Bp2wUserInfo.Icon()
            icon1.width = 28
            icon1.height = 19
            icon1.icon = ByteArray(84)
            val icon2 = Bp2wUserInfo.Icon()
            icon2.width = 28
            icon2.height = 19
            icon2.icon = ByteArray(84)

            val userInfo1 = Bp2wUserInfo()
            userInfo1.aid = 12345
            userInfo1.uid = 12345
            userInfo1.fName = "王"
            userInfo1.name = "五"
            userInfo1.birthday = 19991020
            userInfo1.height = 170
            userInfo1.weight = 70
            userInfo1.gender = 0
            userInfo1.icon = icon1
            val userInfo2 = Bp2wUserInfo()
            userInfo2.aid = 12345
            userInfo2.uid = 11111
            userInfo2.fName = "黄"
            userInfo2.name = "六"
            userInfo2.birthday = 19901020
            userInfo2.height = 175
            userInfo2.weight = 50
            userInfo2.gender = 1
            userInfo2.icon = icon2

            val userList = Bp2wUserList()
            userList.userList.add(userInfo1)
            userList.userList.add(userInfo2)

            LepuBleLog.d("icon1 == " + bytesToHex(icon1.getDataBytes()))
            LepuBleLog.d("icon1.getDataBytes().size == " + icon1.getDataBytes().size)
            LepuBleLog.d("userInfo1 == " + bytesToHex(userInfo1.getDataBytes()))
            LepuBleLog.d("userInfo1.getDataBytes().size == " + userInfo1.getDataBytes().size)
            LepuBleLog.d("userList == " + bytesToHex(userList.getDataBytes()))
            LepuBleLog.d("userList.getDataBytes().size == " + userList.getDataBytes().size)

            LpBleUtil.writeUserList(Constant.BluetoothConfig.currentModel[0], userList)
        }
        binding.bp2wGetWifiConfig.setOnClickListener {
            LpBleUtil.getWifiConfig(Constant.BluetoothConfig.currentModel[0])
        }
        binding.bp2wGetFileCrc.setOnClickListener {
            fileType++
            if (fileType > 2) {
                fileType = 0
            }
            LpBleUtil.getFileListCrc(Constant.BluetoothConfig.currentModel[0], fileType)
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.rcv.layoutManager = this
        }
        adapter = WifiAdapter(R.layout.device_item, null).apply {
            binding.rcv.adapter = this
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bp2Wifi).let {
                val wifiConfig = Bp2WifiConfig()
                wifiConfig.option = 3
                it.ssid = "小米手机"
                it.pwd = "chen12345"
                wifiConfig.wifi = it
                val server = Bp2wServer()
                server.addr = "34.209.148.123"
                server.port = 7100
                wifiConfig.server = server
                LpBleUtil.setWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                binding.content.text = wifiConfig.toString()
            }
        }

        //-------------------------F4,F5-----------------------
        binding.scaleUserInfo.setOnClickListener {
            val userInfo = FscaleUserInfo()
            LpBleUtil.setUserInfo(Bluetooth.MODEL_F5_SCALE, userInfo)
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        binding.scaleUserList.setOnClickListener {
            val userList = arrayListOf<FscaleUserInfo>()
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

    }

    private fun initLiveEvent() {
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
                val data = it.data as Pc100BleResponse.BpStatus
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
            .observe(this, {
                if (it.model == Bluetooth.MODEL_BP2W) {
                    binding.content.text = (it.data as Bp2wConfig).toString()
                } else {
                    binding.content.text = (it.data as Int).toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2WifiScanning)
            .observe(this, {
                binding.content.text = "设备正在扫描wifi"
                LpBleUtil.getWifiDevice(Constant.BluetoothConfig.currentModel[0])
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2WifiDevice)
            .observe(this, {
                val data = it.data as Bp2WifiDevice
                adapter.setNewInstance(data.wifiList)
                adapter.notifyDataSetChanged()
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetWifiConfig)
            .observe(this, {
                val data = it.data as Bp2WifiConfig
                binding.content.text = data.toString()
                if ((data.wifi.state != 2 || data.server.state != 2) && data.wifi.ssid.isNotEmpty()) {
                    LpBleUtil.getWifiConfig(Constant.BluetoothConfig.currentModel[0])
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2wGetFileListCrc)
            .observe(this, {
                val data = it.data as FileListCrc
                binding.content.text = data.toString()
            })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetTime)
            .observe(this, {
                binding.content.text = "device init"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureState)
            .observe(this, {
                val state = it.data as Int
                binding.content.text = if(state == 1) "start bp" else "stop bp"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1RtData)
            .observe(this, {
                val data = it.data as Bpw1BleResponse.RtData
                binding.content.text = "压力值 ：" + data.pressure.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1ErrorResult)
            .observe(this, {
                val data = it.data as Bpw1BleResponse.ErrorResult
                binding.content.text = "测量出错 类型：" + data.type + " 结果：" + data.result
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetFileListComplete)
            .observe(this, {
                val bpw1FileList = it.data as Bpw1BleResponse.Bpw1FileList
                binding.content.text = bpw1FileList.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureResult)
            .observe(this, {
                val data = it.data as Bpw1BleResponse.BpData
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1DeviceInfo)
            .observe(this, {
                val data = it.data as Bpw1BleResponse.DeviceInfo
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetMeasureTime)
            .observe(this, {
                val data = it.data as Bpw1BleResponse.MeasureTime
                binding.content.text = data.toString()
            })
    }

}