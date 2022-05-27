package com.lepu.demo.ui.settings

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lepu.demo.R
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.FscaleUserInfo
import com.lepu.blepro.ble.data.Lew3Config
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.ap20.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.HexString.hexToBytes
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.MainViewModel
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.WifiAdapter
import com.lepu.demo.cofig.Constant
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.FileUtil
import com.lepu.demo.util.icon.BitmapConvertor

/**
 * @ClassName SettingsFragment
 * @Description 设置
 * @Author chenyongfeng
 * @Date 2021/11/23 17:26
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by binding()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var measureTime: Array<String?>

    private lateinit var config: Any
    private var isReceive = false

    private var switchState = false
    private var state = 0
    private var volume = 0
    private var motor1 = intArrayOf(20, 40, 60, 80, 100)  // O2Ring
    private var motor2 = intArrayOf(5, 10, 17, 22, 35)    // KidsO2、Oxylink、BabyO2、BabyO2N
    private var cmdStr = ""

    private var fileType = LeBp2wBleCmd.FileType.ECG_TYPE

    private lateinit var bp2wAdapter: WifiAdapter
    private lateinit var leBp2wAdapter: WifiAdapter

    private var handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
    }

    private fun setViewVisible(v: View?) {
        binding.er1Layout.visibility = View.GONE
        binding.er2Layout.visibility = View.GONE
        binding.bp2Bp2aLayout.visibility = View.GONE
        binding.bp2wLayout.visibility = View.GONE
        binding.leBp2wLayout.visibility = View.GONE
        binding.o2Layout.visibility = View.GONE
        binding.scaleLayout.visibility = View.GONE
        binding.pc100Layout.visibility = View.GONE
        binding.ap20Layout.visibility = View.GONE
        binding.lew3Layout.visibility = View.GONE
        binding.sp20Layout.visibility = View.GONE
        binding.aoj20aLayout.visibility = View.GONE
        binding.pc68bLayout.visibility = View.GONE
        binding.ad5Layout.visibility = View.GONE
        binding.pc300Layout.visibility = View.GONE
        binding.lemLayout.visibility = View.GONE
        if (v == null) return
        v.visibility = View.VISIBLE
    }

    private fun initView() {
        mainViewModel.bleState.observe(viewLifecycleOwner, {
            if (it) {
                binding.settingLayout.visibility = View.VISIBLE
            } else {
                binding.settingLayout.visibility = View.GONE
            }
        })
        mainViewModel.curBluetooth.observe(viewLifecycleOwner, {
            when (it!!.modelNo) {
                Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N -> {
                    setViewVisible(binding.er1Layout)
                    LpBleUtil.getEr1VibrateConfig(it.modelNo)
                }
                Bluetooth.MODEL_ER2, Bluetooth.MODEL_DUOEK -> {
                    setViewVisible(binding.er2Layout)
                    LpBleUtil.getEr2SwitcherState(it.modelNo)
                }
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A,Bluetooth.MODEL_BP2T -> {
                    setViewVisible(binding.bp2Bp2aLayout)
                    LpBleUtil.bp2GetConfig(it.modelNo)
                    LpBleUtil.bp2GetPhyState(it.modelNo)
                }
                Bluetooth.MODEL_BP2W -> {
                    setViewVisible(binding.bp2wLayout)
                    LpBleUtil.bp2GetConfig(it.modelNo)
                }
                Bluetooth.MODEL_LE_BP2W -> {
                    setViewVisible(binding.leBp2wLayout)
                    LpBleUtil.bp2GetConfig(it.modelNo)
                }
                Bluetooth.MODEL_O2RING, Bluetooth.MODEL_BABYO2,
                Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_CHECKO2,
                Bluetooth.MODEL_O2M, Bluetooth.MODEL_SLEEPO2,
                Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
                Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
                Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT -> {
                    setViewVisible(binding.o2Layout)
                    LpBleUtil.getInfo(it.modelNo)
                }
                Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_F5_SCALE,
                Bluetooth.MODEL_F8_SCALE -> {
                    setViewVisible(binding.scaleLayout)
                }
                Bluetooth.MODEL_PC100 -> {
                    setViewVisible(binding.pc100Layout)
                }
                Bluetooth.MODEL_AP20 -> {
                    setViewVisible(binding.ap20Layout)
                    LpBleUtil.ap20GetConfig(it.modelNo, state)
                }
                Bluetooth.MODEL_LEW3 -> {
                    setViewVisible(binding.lew3Layout)
                    LpBleUtil.lew3GetConfig(it.modelNo)
                }
                Bluetooth.MODEL_SP20 -> {
                    setViewVisible(binding.sp20Layout)
                    LpBleUtil.sp20GetConfig(it.modelNo, state)
                }
                Bluetooth.MODEL_AOJ20A -> {
                    setViewVisible(binding.aoj20aLayout)
                }
                Bluetooth.MODEL_PC_68B -> {
                    setViewVisible(binding.pc68bLayout)
                    LpBleUtil.pc68bGetConfig(it.modelNo)
                }
                Bluetooth.MODEL_VTM_AD5 -> {
                    setViewVisible(binding.ad5Layout)
                }
                Bluetooth.MODEL_PC300 -> {
                    setViewVisible(binding.pc300Layout)
                }
                Bluetooth.MODEL_LEM -> {
                    setViewVisible(binding.lemLayout)
                    LpBleUtil.getInfo(it.modelNo)
                }
                else -> {
                    setViewVisible(null)
                }
            }
        })
        if (isReceive) {
            binding.bytesSwitch.text = "原始数据显示开"
        } else {
            binding.bytesSwitch.text = "原始数据显示关"
        }
        binding.bytesSwitch.setOnClickListener {
            isReceive = !isReceive
            if (isReceive) {
                binding.bytesSwitch.text = "原始数据显示开"
            } else {
                binding.bytesSwitch.text = "原始数据显示关"
            }
        }

        // -----------------------pc100-------------------
        binding.pc100BoState.setOnClickListener {
            LpBleUtil.pc100GetBoState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //-------------------------er1--------------------
        binding.er1SetSound.setOnClickListener {
            switchState = !switchState
            LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, binding.er1Hr1.text.toString().toInt(), binding.er1Hr2.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.er1GetConfig.setOnClickListener {
            LpBleUtil.getEr1VibrateConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.er1SetHr.setOnClickListener {
            LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, binding.er1Hr1.text.toString().toInt(), binding.er1Hr2.text.toString().toInt())
        }
        //-------------------------er2/duoek------------------------
        binding.er2GetConfig.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2) {
                LpBleUtil.getEr2SwitcherState(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.getEr1VibrateConfig(Constant.BluetoothConfig.currentModel[0])
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.er2SetConfig.setOnClickListener {
            switchState = !switchState
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2) {
                LpBleUtil.setEr2SwitcherState(Constant.BluetoothConfig.currentModel[0], switchState)
            } else {
                LpBleUtil.setDuoekVibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        //-------------------------lew3------------------------
        binding.lew3Bound.setOnClickListener {
            LpBleUtil.lew3BoundDevice(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lew3GetConfig.setOnClickListener {
            LpBleUtil.lew3GetConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lew3SetConfig.setOnClickListener {
            val server = Lew3Config()
            server.addr = "192.168.111.222"
            server.port = 5000
            LpBleUtil.lew3SetServer(Constant.BluetoothConfig.currentModel[0], server)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lew3GetBattery.setOnClickListener {
            LpBleUtil.lew3GetBattery(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //-------------------------bp2/bp2A/bp2T--------------------
        binding.bp2SetDeviceState.setOnClickListener {
            state++
            if (state > 5)
                state = 0
            LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.bp2SetDeviceState.text = "当前设备状态$state"
        }
        binding.bp2GetDeviceState.setOnClickListener {
            LpBleUtil.bp2GetRtState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2SetPhyState.setOnClickListener {
            if (this::config.isInitialized) {
                state++
                if (state > 6)
                    state = 0
                (config as Bp2BlePhyState).mode = state
                (config as Bp2BlePhyState).intensy = state
                (config as Bp2BlePhyState).remainingTime = state
                LpBleUtil.bp2SetPhyState(
                    Constant.BluetoothConfig.currentModel[0],
                    (config as Bp2BlePhyState)
                )
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
        }
        binding.bp2GetPhyState.setOnClickListener {
            LpBleUtil.bp2GetPhyState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2GetConfig.setOnClickListener {
            LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2SetConfig.setOnClickListener {
            switchState = !switchState
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], switchState, volume)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2SetVolume.setOnClickListener {
            volume++
            if (volume > 3)
                volume = 0
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], switchState, volume)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.bp2SetVolume.text = "音量$volume"
        }

        //-------------------------bp2w------------------------
        binding.bp2wSetState.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.bp2wSetState.text = "设备状态$state"
        }
        binding.bp2wGetState.setOnClickListener {
            LpBleUtil.bp2GetRtState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wGetConfig.setOnClickListener {
            LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wBeepSw.setOnClickListener {
            switchState = !switchState
            (config as Bp2Config).beepSwitch = switchState
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wSetVolume.setOnClickListener {
            volume++
            if (volume > 3)
                volume = 0
            (config as Bp2Config).volume = volume
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wMode.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            (config as Bp2Config).avgMeasureMode = state
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wGetWifiRoute.setOnClickListener {
            LpBleUtil.bp2GetWifiDevice(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wDeleteFile.setOnClickListener {
            LpBleUtil.bp2DeleteFile(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2wGetWifiConfig.setOnClickListener {
            LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.bp2wRcv.layoutManager = this
        }
        bp2wAdapter = WifiAdapter(R.layout.device_item, null).apply {
            binding.bp2wRcv.adapter = this
        }
        bp2wAdapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bp2Wifi).let {
                val wifiConfig = Bp2WifiConfig()
                wifiConfig.option = 3
                it.ssid = "VIATOM_WIFI"
                it.pwd = "ViatomCtrl"
                wifiConfig.wifi = it
                val server = Bp2Server()
//                server.addr = "34.209.148.123"
                server.addr = "bptest.viatomtech.com"
//                server.addr = "bp.viatomtech.com"
//                server.addr = "ai.viatomtech.com.cn"
                server.port = 7100
                server.addrType = 1
                wifiConfig.server = server
                LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                binding.content.text = wifiConfig.toString()
            }
            adapter.setList(null)
            adapter.notifyDataSetChanged()
        }
        //-------------------------le bp2w------------------------
        binding.leBp2wSetState.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.leBp2wSetState.text = "设备状态$state"
        }
        binding.leBp2wGetState.setOnClickListener {
            LpBleUtil.bp2GetRtState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wGetConfig.setOnClickListener {
            LpBleUtil.bp2GetConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wBeepSw.setOnClickListener {
            switchState = !switchState
            (config as Bp2Config).beepSwitch = switchState
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wSetVolume.setOnClickListener {
            volume++
            if (volume > 3)
                volume = 0
            (config as Bp2Config).volume = volume
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wMode.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            (config as Bp2Config).avgMeasureMode = state
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wGetWifiRoute.setOnClickListener {
            LpBleUtil.bp2GetWifiDevice(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wDeleteFile.setOnClickListener {
            LpBleUtil.bp2DeleteFile(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wUserList.setOnClickListener {

            FileUtil.getBmp(context)

//            val string = "0007040F0C0700040504060D070405000007040C0F0404070002020F0202020000070C0F040407000F090F090F090F000007040F0404070009090F090F09090000000102030700000000020404040603030000020404040607030000F891FE9FF70503DF56DA76D2D0DF0300F993BCF79395F3434C70FF7048444300F893FC9793F503FF203CA46521FF0100F893FE9793F701FF0CF01CF119FF0100000000FFFF0000000003070C1830F0C000000120202060D19F0E00408000C0404040C040404040C0408040800000C0404040404040404040408040800000C0404040404040404040C08040800000C04040404040404040404080000000008080000000008080808080808000000080808080800000"
            val string = "00000000000000000000001F00000000000000000000001F00000000000000000000001F00000000000000000000001F1810181030FE10FE0000001F7DFE7F107EAA7E28041E0F9F5428497E52AA52281C2391DF549A491052FE52FE2C0180DF7CAE7F107E927EAA0C0180DF54FE49FF528A52AA0C01819F5410593852FE52AA0C030F1F7CFE7F387EA27EBE0C0700DF58A2505450A250D60C0C007F1ED21A521ABA1AD20C18007F1DFB2D912D823E820C30007F2F836F912F8F2F8F0C3010DF28854801480348010C3F8F9F4FFE8FFE8FFE8FFE0000001F00000000000000000000001F00000000000000000000001F00000000000000000000001F"
            val bytes = hexToBytes(string)
            val bytesString = bytesToHex(bytes)

            LepuBleLog.d("test bytesToHex(bytes) == $bytesString")
            LepuBleLog.d("test bytesString.equals(string) == " + bytesString.equals(string))
            LepuBleLog.d("test bytes.size == " + bytes.size)

            val icon1 = LeBp2wUserInfo.Icon()
            icon1.width = 91
            icon1.height = 21
            icon1.icon = bytes
            val icon2 = BitmapConvertor(context).createIcon("一二")
            val icon3 = BitmapConvertor(context).createIcon("一二三")
            val icon4 = BitmapConvertor(context).createIcon("一二三四")

            val userInfo1 = LeBp2wUserInfo()
            userInfo1.aid = 12345
            userInfo1.uid = -1
            userInfo1.fName = "魑"
            userInfo1.name = "魅魍魉123"
            userInfo1.birthday = "1990-10-20"
            userInfo1.height = 170
            userInfo1.weight = 70f
            userInfo1.gender = 0
            userInfo1.icon = icon1
            val userInfo2 = LeBp2wUserInfo()
            userInfo2.aid = 12345
            userInfo2.uid = 11111
            userInfo2.fName = "一"
            userInfo2.name = "二"
            userInfo2.birthday = "1991-10-20"
            userInfo2.height = 175
            userInfo2.weight = 50f
            userInfo2.gender = 1
            userInfo2.icon = icon2
            val userInfo3 = LeBp2wUserInfo()
            userInfo3.aid = 12345
            userInfo3.uid = 22222
            userInfo3.fName = "一"
            userInfo3.name = "二三"
            userInfo3.birthday = "1992-10-20"
            userInfo3.height = 175
            userInfo3.weight = 50f
            userInfo3.gender = 1
            userInfo3.icon = icon3
            val userInfo4 = LeBp2wUserInfo()
            userInfo4.aid = 12345
            userInfo4.uid = 33333
            userInfo4.fName = "一"
            userInfo4.name = "二三四"
            userInfo4.birthday = "1993-10-20"
            userInfo4.height = 175
            userInfo4.weight = 50f
            userInfo4.gender = 1
            userInfo4.icon = icon4

            val userList = LeBp2wUserList()
            userList.userList.add(userInfo2)
            userList.userList.add(userInfo3)
            userList.userList.add(userInfo4)
            userList.userList.add(userInfo1)

            FileUtil.saveFile(context, userList.getDataBytes())



            LepuBleLog.d("test icon1 == " + bytesToHex(icon1.getDataBytes()))
            LepuBleLog.d("test icon1.getDataBytes().size == " + icon1.getDataBytes().size)
            LepuBleLog.d("test userInfo1 == " + bytesToHex(userInfo1.getDataBytes()))
            LepuBleLog.d("test userInfo1.getDataBytes().size == " + userInfo1.getDataBytes().size)
            LepuBleLog.d("test userList == " + bytesToHex(userList.getDataBytes()))
            LepuBleLog.d("test userList.getDataBytes().size == " + userList.getDataBytes().size)

            LpBleUtil.bp2WriteUserList(Constant.BluetoothConfig.currentModel[0], userList)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wGetWifiConfig.setOnClickListener {
            LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wGetFileCrc.setOnClickListener {
            fileType++
            if (fileType > 2) {
                fileType = 0
            }
            LpBleUtil.bp2GetFileListCrc(Constant.BluetoothConfig.currentModel[0], fileType)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.leBp2wUtcTime.setOnClickListener {
            LpBleUtil.bp2SyncUtcTime(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.leBp2wRcv.layoutManager = this
        }
        leBp2wAdapter = WifiAdapter(R.layout.device_item, null).apply {
            binding.leBp2wRcv.adapter = this
        }
        leBp2wAdapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bp2Wifi).let {
                val wifiConfig = Bp2WifiConfig()
                wifiConfig.option = 3
                it.ssid = "VIATOM_WIFI"
                it.pwd = "ViatomCtrl"
                wifiConfig.wifi = it
                val server = Bp2Server()
//                server.addr = "34.209.148.123"
                server.addr = "bptest.viatomtech.com"
//                server.addr = "bp.viatomtech.com"
//                server.addr = "ai.viatomtech.com.cn"
                server.port = 7100
                server.addrType = 1
                wifiConfig.server = server
                LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                binding.content.text = wifiConfig.toString()
            }
            adapter.setList(null)
            adapter.notifyDataSetChanged()
        }

        //-------------------------F4,F5,F8-----------------------
        binding.scaleUserInfo.setOnClickListener {
            val userInfo = FscaleUserInfo()
            LpBleUtil.setUserInfo(Constant.BluetoothConfig.currentModel[0], userInfo)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.scaleUserList.setOnClickListener {
            val userList = arrayListOf<FscaleUserInfo>()
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            userList.add(FscaleUserInfo())
            LpBleUtil.setUserList(Constant.BluetoothConfig.currentModel[0], userList)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //-------------------------ap20-----------------------
        binding.ap20Switch.setOnClickListener {
            switchState = !switchState
            var temp = "关"
            if (switchState) {
                LpBleUtil.ap20SetConfig(Constant.BluetoothConfig.currentModel[0], 1, 1)
                temp = "开"
            } else {
                LpBleUtil.ap20SetConfig(Constant.BluetoothConfig.currentModel[0], 1, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.ap20Switch.text = "警报$temp"
        }
        binding.ap20GetConfig.setOnClickListener {
            state++
            if (state > 5)
                state = 0
            if (state == 5) {
                LpBleUtil.ap20GetBattery(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], state)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.ap20GetConfig.text = "获取参数$state"
        }
        binding.ap20SetConfig.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            if (state == 2 || state == 3) {
                LpBleUtil.ap20SetConfig(Constant.BluetoothConfig.currentModel[0], state, 90)
            } else if (state == 4) {
                LpBleUtil.ap20SetConfig(Constant.BluetoothConfig.currentModel[0], state, 125)
            } else if (state == 0) {
                volume++
                if (volume > 6)
                    volume = 0
                LpBleUtil.ap20SetConfig(Constant.BluetoothConfig.currentModel[0], state, volume)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.ap20SetConfig.text = "设置参数$state"
        }
        //-------------------------o2-----------------------
        binding.o2SetOxiThr.setOnClickListener {
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_OXI_THR, binding.o2OxiThr.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2SetHrThr1.setOnClickListener {
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_HR_LOW_THR, binding.o2HrThr1.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2SetHrThr2.setOnClickListener {
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_HR_HIGH_THR, binding.o2HrThr2.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2OxiSwitch.setOnClickListener {
            state++
            if (state > 3)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_OXI_SWITCH, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2HrSwitch.setOnClickListener {
            state++
            if (state > 3)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_HR_SWITCH, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2Spo2Switch.setOnClickListener {
            state++
            if (state > 1)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_SPO2SW, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2Motor.setOnClickListener {
            volume++
            if (volume > 4)
                volume = 0
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYLINK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2N) {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_MOTOR, motor2[volume])
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_MOTOR, motor1[volume])
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2Buzzer.setOnClickListener {
            volume++
            if (volume > 4)
                volume = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_BUZZER, motor1[volume])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2LightMode.setOnClickListener {
            volume++
            if (volume > 2)
                volume = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_LIGHTING_MODE, volume)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2LightStr.setOnClickListener {
            volume++
            if (volume > 2)
                volume = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_LIGHT_STR, volume)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2SetMtThr.setOnClickListener {
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_MT_THR, binding.o2MtThr.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2MtSwitch.setOnClickListener {
            state++
            if (state > 1)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_MT_SW, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2SetIvThr.setOnClickListener {
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_IV_THR, binding.o2IvThr.text.toString().toInt())
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2IvSwitch.setOnClickListener {
            state++
            if (state > 1)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_IV_SW, state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.o2AllSwitch.setOnClickListener {
            state++
            if (state > 1)
                state = 0
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_ALL_SW, state)
//            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0],
//                arrayOf(OxyBleCmd.SYNC_TYPE_OXI_SWITCH, OxyBleCmd.SYNC_TYPE_HR_SWITCH, OxyBleCmd.SYNC_TYPE_MT_SW, OxyBleCmd.SYNC_TYPE_IV_SW),
//                intArrayOf(state, state, state, state))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //-------------------------sp20-----------------------
        binding.sp20AlarmSwitch.setOnClickListener {
            switchState = !switchState
            var temp = "关"
            (config as Sp20Config).type = Sp20BleCmd.ConfigType.ALARM_SWITCH
            if (switchState) {
                (config as Sp20Config).value = 1
                temp = "开"
            } else {
                (config as Sp20Config).value = 0
            }
            LpBleUtil.sp20SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Sp20Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.sp20AlarmSwitch.text = "警报$temp"
        }
        binding.sp20PulseSwitch.setOnClickListener {
            switchState = !switchState
            var temp = "关"
            (config as Sp20Config).type = Sp20BleCmd.ConfigType.PULSE_BEEP
            if (switchState) {
                (config as Sp20Config).value = 1
                temp = "开"
            } else {
                (config as Sp20Config).value = 0
            }
            LpBleUtil.sp20SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Sp20Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.sp20PulseSwitch.text = "搏动音$temp"
        }
        binding.sp20GetConfig.setOnClickListener {
            state++
            if (state > 5)
                state = 0
            if (state == 0) {
                LpBleUtil.sp20GetBattery(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.sp20GetConfig(Constant.BluetoothConfig.currentModel[0], state)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.sp20GetConfig.text = "获取参数$state"
        }
        binding.sp20SetConfig.setOnClickListener {
            state++
            if (state > 4)
                state = 0
            (config as Sp20Config).type = state
            if (state == 2 || state == 3) {
                (config as Sp20Config).value = 90
            } else if (state == 4) {
                (config as Sp20Config).value = 110
            } else if (state == 0) {
                volume++
                if (volume > 6)
                    volume = 0
                (config as Sp20Config).value = volume
            }
            LpBleUtil.sp20SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Sp20Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
            binding.sp20SetConfig.text = "设置参数$state"
        }

        //----------------------aoj20a--------------------
        binding.aoj20aDeleteFile.setOnClickListener {
            LpBleUtil.aoj20aDeleteData(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //----------------------pc68b----------------------
        binding.pc68bDeleteFile.setOnClickListener {
            LpBleUtil.pc68bDeleteFile(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc68bStateInfo.setOnClickListener {
            LpBleUtil.pc68bGetStateInfo(Constant.BluetoothConfig.currentModel[0], 5)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc68bGetConfig.setOnClickListener {
            LpBleUtil.pc68bGetConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
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
            LpBleUtil.pc68bSetConfig(Constant.BluetoothConfig.currentModel[0], (config as Pc68bConfig))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //----------------------ad5---------------------
        binding.ad5EnableRtData.setOnClickListener {
            switchState = !switchState
            LpBleUtil.enableRtData(Constant.BluetoothConfig.currentModel[0], 0, switchState)
            binding.ad5EnableRtData.text = if (switchState) {
                "使能开"
            } else {
                "使能关"
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //----------------------pc300--------------------
        binding.pc300SetDigit.setOnClickListener {
            // 1:8bit 2:12bit
            state++
            if (state > 2) {
                state = 1
            }
            LpBleUtil.pc300SetEcgDataDigit(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        /*binding.pc300SetGluUnit.setOnClickListener {
            state++
            if (state > Pc300BleCmd.GluUnit.MG_DL) {
                state = Pc300BleCmd.GluUnit.MMOL_L
            }
            LpBleUtil.pc300SetGluUnit(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300SetId.setOnClickListener {
            state++
            LpBleUtil.pc300SetDeviceId(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300GetId.setOnClickListener {
            LpBleUtil.pc300GetDeviceId(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }*/
        binding.pc300SetBsType.setOnClickListener {
            state++
            if (state > Pc300BleCmd.GlucometerType.ON_CALL_SURE_SYNC) {
                state = Pc300BleCmd.GlucometerType.AI_AO_LE
            }
            LpBleUtil.pc300SetGlucometerType(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300GetBsType.setOnClickListener {
            LpBleUtil.pc300GetGlucometerType(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        /*binding.pc300SetTempMode.setOnClickListener {
                state++
                if (state > Pc300BleCmd.TempMode.OBJECT_F) {
                state = Pc300BleCmd.TempMode.EAR_C
            }
            LpBleUtil.pc300SetTempMode(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300GetTempMode.setOnClickListener {
            LpBleUtil.pc300GetTempMode(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300SetBpMode.setOnClickListener {
            state++
            if (state > Pc300BleCmd.BpMode.CHILD_MODE) {
                state = Pc300BleCmd.BpMode.ADULT_MODE
            }
            LpBleUtil.pc300SetBpMode(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.pc300GetBpMode.setOnClickListener {
            LpBleUtil.pc300GetBpMode(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }*/

        // ---------------------------lem---------------------------
        binding.lemDeviceSwitch.setOnClickListener {
            switchState = !switchState
            LpBleUtil.lemDeviceSwitch(Constant.BluetoothConfig.currentModel[0], switchState)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemHeatSwitch.setOnClickListener {
            switchState = !switchState
            LpBleUtil.lemHeatMode(Constant.BluetoothConfig.currentModel[0], switchState)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemGetBattery.setOnClickListener {
            LpBleUtil.lemGetBattery(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemMassMode.setOnClickListener {
            state++
            if (state > LemBleCmd.MassageMode.AUTOMATIC) {
                state = LemBleCmd.MassageMode.VITALITY
            }
            LpBleUtil.lemMassMode(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemMassTime.setOnClickListener {
            state++
            if (state > LemBleCmd.MassageTime.MIN_5) {
                state = LemBleCmd.MassageTime.MIN_15
            }
            LpBleUtil.lemMassTime(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemMassLevel.setOnClickListener {
            state++
            if (state > 15) {
                state = 0
            }
            LpBleUtil.lemMassLevel(Constant.BluetoothConfig.currentModel[0], state)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

    }

    private fun setReceiveCmd(bytes: ByteArray) {
        if (isReceive) {
            binding.receiveCmd.text = "receive : " + bytesToHex(bytes)
        }
    }

    private fun initLiveEvent() {
        LiveEventBus.get<String>(EventMsgConst.Cmd.EventCmdResponseContent)
            .observe(this, {
                cmdStr += "\n receive : $it"
                binding.receiveCmd.text = cmdStr
            })
        //----------------------------er1/duoek-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetSwitcherState)
            .observe(this, {
                LpBleUtil.getEr1VibrateConfig(it.model)
                Toast.makeText(
                    context,
                    "er1/duoek 设置参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1VibrateConfig)
            .observe(this, {
                val data = it.data as ByteArray
                setReceiveCmd(data)
                if (it.model == Bluetooth.MODEL_ER1 || it.model == Bluetooth.MODEL_ER1_N) {
                    val config = VbVibrationSwitcherConfig.parse(data)
                    this.config = config
                    var temp = "关"
                    if (config.switcher)
                        temp = "开"
                    binding.er1SetSound.text = "声音$temp"
                    binding.er1Hr1.setText("${config.hr1}")
                    binding.er1Hr2.setText("${config.hr2}")
                    binding.content.text = "switcher : " + config.switcher + " hr1 : " + config.hr1 + " hr2 : " + config.hr2
                    Toast.makeText(
                        context,
                        "er1 获取参数成功",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val data = it.data as ByteArray
                    val config = SwitcherConfig.parse(data)
                    var temp = "关"
                    if (config.switcher)
                        temp = "开"
                    binding.er2SetConfig.text = "声音$temp"
                    binding.content.text = "switcher : " + config.switcher + " vector : " + config.vector + " motionCount : " + config.motionCount + " motionWindows : " + config.motionWindows
                    Toast.makeText(
                        context,
                        "duoek 获取参数成功",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        //-----------------------------er2------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetSwitcherState)
            .observe(this, {
                LpBleUtil.getEr2SwitcherState(it.model)
                Toast.makeText(
                    context,
                    "er2 设置参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SwitcherState)
            .observe(this, {
                val data = it.data as ByteArray
                setReceiveCmd(data)
                val config = SwitcherConfig.parse(data)
                this.config = config
                var temp = "关"
                if (config.switcher)
                    temp = "开"
                binding.er1SetSound.text = "声音$temp"
                binding.content.text = "switcher : " + config.switcher + " vector : " + config.vector + " motionCount : " + config.motionCount + " motionWindows : " + config.motionWindows
                Toast.makeText(
                    context,
                    "er2 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        //----------------------------bp2/bp2a/bp2t-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
            .observe(this, {
                val data = it.data as Int
                var temp = "关"
                switchState = false
                if (data == 1) {
                    temp = "开"
                    switchState = true
                }
                binding.bp2SetConfig.text = "声音$temp"
                Toast.makeText(
                    context,
                    "bp2 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
            .observe(this, {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(
                    context,
                    "bp2 设置参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
            .observe(this, {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2 获取设备状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSwitchState)
            .observe(this, {
                val data = it.data as Boolean
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2 设置设备状态$data",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyState)
            .observe(this, {
                val data = it.data as Bp2BlePhyState
                setReceiveCmd(data.bytes)
                config = data
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2 获取理疗状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError)
            .observe(this, {
                Toast.makeText(
                    context,
                    "获取失败，请先进入理疗模式",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetPhyState)
            .observe(this, {
                val data = it.data as Bp2BlePhyState
                setReceiveCmd(data.bytes)
                config = data
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2 设置理疗状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })

        //------------------------------bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wDeleteFile)
            .observe(this, {
                Toast.makeText(
                    context,
                    "bp2w 删除文件成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig)
            .observe(this, {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(
                    context,
                    "bp2w 设置参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSwitchState)
            .observe(this, {
                LpBleUtil.bp2GetRtState(it.model)
                Toast.makeText(
                    context,
                    "bp2w 设置主机状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtState)
            .observe(this, {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.bp2wGetState.text = "实时状态${data.status}"
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2w 获取主机状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig)
            .observe(this, {
                val config = it.data as Bp2Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = config.toString()
                var on = "关"
                if (config.beepSwitch) {
                    on = "开"
                }
                binding.bp2wBeepSw.text = "心跳音${on}"
                binding.bp2wSetVolume.text = "音量" + config.volume
                binding.bp2wMode.text = "测量模式" + config.avgMeasureMode
                Toast.makeText(
                    context,
                    "bp2w 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiScanning)
            .observe(this, {
                binding.content.text = "设备正在扫描wifi"
                handler.postDelayed({
                    LpBleUtil.bp2GetWifiDevice(it.model)
                }, 1000)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiDevice)
            .observe(this, {
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                bp2wAdapter.setNewInstance(data.wifiList)
                bp2wAdapter.notifyDataSetChanged()
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "bp2w 获取路由成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetWifiConfig)
            .observe(this, {
                val data = it.data as Boolean
                if (data) {
                    LpBleUtil.bp2GetWifiConfig(it.model)
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetWifiConfig)
            .observe(this, {
                val data = it.data as Bp2WifiConfig
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
                if (data.wifi.ssid.isNotEmpty()) {
                    /*if ((data.wifi.state != 2 || data.server.state != 2) && (data.wifi.state != 255 || data.server.state != 255)) {
                        LpBleUtil.bp2GetWifiConfig(it.model)
                    } else {
                        Toast.makeText(context, "bp2w WiFi连接成功", Toast.LENGTH_SHORT).show()
                    }*/
                } else {
                    Toast.makeText(context, "bp2w 尚未配置WiFi", Toast.LENGTH_SHORT).show()
                }

            })
        //------------------------------lp bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncUtcTime)
            .observe(this, {
                Toast.makeText(
                    context,
                    "le bp2w 同步 UTC 时间成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile)
            .observe(this, {
                Toast.makeText(
                    context,
                    "le bp2w 删除文件成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig)
            .observe(this, {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(
                    context,
                    "le bp2w 设置参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState)
            .observe(this, {
                LpBleUtil.bp2GetRtState(it.model)
                Toast.makeText(
                    context,
                    "le bp2w 设置主机状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtState)
            .observe(this, {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.leBp2wGetState.text = "实时状态${data.status}"
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "le bp2w 获取主机状态成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetConfig)
            .observe(this, {
                val config = it.data as Bp2Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = config.toString()
                var on = "关"
                if (config.beepSwitch) {
                    on = "开"
                }
                binding.leBp2wBeepSw.text = "心跳音$on"
                binding.leBp2wSetVolume.text = "音量${config.volume}"
                binding.leBp2wMode.text = "测量模式${config.avgMeasureMode}"
                Toast.makeText(
                    context,
                    "le bp2w 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiScanning)
            .observe(this, {
                binding.content.text = "设备正在扫描wifi"
                LpBleUtil.bp2GetWifiDevice(it.model)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiDevice)
            .observe(this, {
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                leBp2wAdapter.setNewInstance(data.wifiList)
                leBp2wAdapter.notifyDataSetChanged()
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "le bp2w 获取路由成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig)
            .observe(this, {
                val data = it.data as Boolean
                if (data) {
                    LpBleUtil.bp2GetWifiConfig(it.model)
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetWifiConfig)
            .observe(this, {
                val data = it.data as Bp2WifiConfig
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
                if (data.wifi.ssid.isNotEmpty()) {
                    if ((data.wifi.state != 2 || data.server.state != 2)) {
                        LpBleUtil.bp2GetWifiConfig(it.model)
                        Toast.makeText(context, "le bp2w WiFi未连接成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "le bp2w WiFi连接成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "le bp2w 尚未配置WiFi", Toast.LENGTH_SHORT).show()
                }

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
            .observe(this, {
                val data = it.data as FileListCrc
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "le bp2w 获取文件列表校验成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WritingFileProgress)
            .observe(this, {
                val data = it.data as Bp2FilePart
                Toast.makeText(
                    context,
                    "le bp2w 写文件列表进度：" + (data.percent*100).toInt().toString() + "%",
                    Toast.LENGTH_SHORT
                ).show()

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileComplete)
            .observe(this, {
                val data = it.data as FileListCrc
                Toast.makeText(
                    context,
                    "le bp2w 写文件完成 crc：" + data.crc,
                    Toast.LENGTH_SHORT
                ).show()

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
            .observe(this, {
                val data = it.data as String
                Toast.makeText(
                    context,
                    "le bp2w 写文件错误 filename：$data",
                    Toast.LENGTH_SHORT
                ).show()

            })
        //------------------------------o2/babyO2-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this, {
                Toast.makeText(
                    context,
                    "o2/babyO2 设置参数成功 ${it.data}",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this, {
                val data = it.data as OxyBleResponse.OxyInfo
                setReceiveCmd(data.bytes)
                binding.o2OxiThr.setText("${data.oxiThr}")
                binding.o2HrThr1.setText("${data.hrLowThr}")
                binding.o2HrThr2.setText("${data.hrHighThr}")
                binding.o2OxiSwitch.text = when (data.oxiSwitch) {
                    1 -> "血氧震动开声音关"
                    2 -> "血氧震动关声音开"
                    3 -> "血氧震动开声音开"
                    else -> "血氧震动关声音关"
                }
                binding.o2HrSwitch.text = when (data.hrSwitch) {
                    1 -> "心率震动开声音关"
                    2 -> "心率震动关声音开"
                    3 -> "心率震动开声音开"
                    else -> "心率震动关声音关"
                }
                binding.o2Spo2Switch.text = if (data.spo2Switch == 1) {
                    "血氧功能开"
                } else {
                    "血氧功能关"
                }
                binding.o2Motor.text = "震动强度${data.motor}"
                binding.o2Buzzer.text = "声音强度${data.buzzer}"
                binding.o2LightMode.text = "亮屏模式${data.lightingMode}"
                binding.o2LightStr.text = "屏幕亮度${data.lightStr}"
                binding.o2MtThr.setText("${data.mtThr}")
                binding.o2MtSwitch.text = if (data.mtSwitch == 1) {
                    "体动开"
                } else {
                    "体动关"
                }
                binding.o2IvThr.setText("${data.ivThr}")
                binding.o2IvSwitch.text = if (data.ivSwitch == 1) {
                    "无效值开"
                } else {
                    "无效值关"
                }
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "o2/babyO2 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        //------------------------------ap20-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel)
            .observe(this, {
                var data = it.data as Int
                binding.content.text = "电量${data.toString()}"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult)
            .observe(this, {
                var data = it.data as GetConfigResult
//                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
            })
        //------------------------------lew3-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3BoundDevice)
            .observe(this, {
                var data = it.data as Boolean
                binding.content.text = "请求绑定 : $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3SetServer)
            .observe(this, {
                LpBleUtil.lew3GetConfig(it.model)
                Toast.makeText(
                    context,
                    "lew3手表 配置服务器成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3GetConfig)
            .observe(this, {
                var config = it.data as Lew3Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = config.toString()
                Toast.makeText(
                    context,
                    "lew3手表 获取参数成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3BatteryInfo)
            .observe(this, {
                val data = it.data as KtBleBattery
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
                Toast.makeText(
                    context,
                    "lew3手表 获取电量成功",
                    Toast.LENGTH_SHORT
                ).show()
            })

        //------------------------------sp20-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery)
            .observe(this, {
                var data = it.data as Int
                binding.content.text = "电量${data.toString()}"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig)
            .observe(this, {
                var config = it.data as Sp20Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = config.toString()
            })

        //---------------------------aoj20a-------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observe(this, {
                Toast.makeText(
                    context,
                    "aoj20a 测量完成",
                    Toast.LENGTH_SHORT
                ).show()
                val data = it.data as Aoj20aBleResponse.TempRtData
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData)
            .observe(this, {
                Toast.makeText(
                    context,
                    "aoj20a 删除成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observe(this, {
                val data = it.data as Aoj20aBleResponse.ErrorMsg
                binding.content.text = data.toString()
            })

        //---------------------pc68b-------------------
        /*LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bStatusInfo)
            .observe(this, {
                val data = it.data as Pc68bBleResponse.StatusInfo
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeleteFile)
            .observe(this, {
                val data = it.data as Boolean
                binding.content.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bConfigInfo)
            .observe(this, {
                val config = it.data as Pc68bConfig
                this.config = config
                binding.content.text = config.toString()
            })*/

        // --------------------------LEM--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo)
            .observe(this, {
                val data = it.data as LemBleResponse.DeviceInfo
                binding.content.text = data.toString()
                binding.lemGetBattery.text = "电量${data.battery}%"
                binding.lemHeatSwitch.text = "加热${data.heatMode}"
                binding.lemMassMode.text = when (data.massageMode) {
                    LemBleCmd.MassageMode.VITALITY -> "活力模式"
                    LemBleCmd.MassageMode.DYNAMIC -> "动感模式"
                    LemBleCmd.MassageMode.HAMMERING -> "捶击模式"
                    LemBleCmd.MassageMode.SOOTHING -> "舒缓模式"
                    LemBleCmd.MassageMode.AUTOMATIC -> "自动模式"
                    else -> ""
                }
                binding.lemMassLevel.text = "按摩力度${data.massageLevel}"
                binding.lemMassTime.text = when (data.massageTime) {
                    LemBleCmd.MassageTime.MIN_15 -> "15min"
                    LemBleCmd.MassageTime.MIN_10 -> "10min"
                    LemBleCmd.MassageTime.MIN_5 -> "5min"
                    else -> ""
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery)
            .observe(this, {
                val data = it.data as Int
                binding.lemGetBattery.text = "电量$data%"
                Toast.makeText(
                    context,
                    "lem 获取电量成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode)
            .observe(this, {
                val data = it.data as Boolean
                binding.lemHeatSwitch.text = "加热$data"
                Toast.makeText(
                    context,
                    "lem 设置加热模式 $data 成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode)
            .observe(this, {
                val data = it.data as Int
                binding.lemMassMode.text = when (data) {
                    LemBleCmd.MassageMode.VITALITY -> "活力模式"
                    LemBleCmd.MassageMode.DYNAMIC -> "动感模式"
                    LemBleCmd.MassageMode.HAMMERING -> "捶击模式"
                    LemBleCmd.MassageMode.SOOTHING -> "舒缓模式"
                    LemBleCmd.MassageMode.AUTOMATIC -> "自动模式"
                    else -> ""
                }
                Toast.makeText(
                    context,
                    "lem 设置按摩模式 $data 成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel)
            .observe(this, {
                val data = it.data as Int
                binding.lemMassLevel.text = "按摩力度$data"
                Toast.makeText(
                    context,
                    "lem 设置按摩力度 $data 成功",
                    Toast.LENGTH_SHORT
                ).show()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime)
            .observe(this, {
                val data = it.data as Int
                binding.lemMassTime.text = when (data) {
                    LemBleCmd.MassageTime.MIN_15 -> "15min"
                    LemBleCmd.MassageTime.MIN_10 -> "10min"
                    LemBleCmd.MassageTime.MIN_5 -> "5min"
                    else -> ""
                }
                Toast.makeText(
                    context,
                    "lem 设置按摩时间 $data 成功",
                    Toast.LENGTH_SHORT
                ).show()
            })


        //-----------------------------------
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