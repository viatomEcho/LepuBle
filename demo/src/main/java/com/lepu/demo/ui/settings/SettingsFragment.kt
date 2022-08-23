package com.lepu.demo.ui.settings

import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.ble.data.lew.DeviceInfo
import com.lepu.blepro.ble.data.lew.TimeData
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.ap20.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.HexString.hexToBytes
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.MainViewModel
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.WifiAdapter
import com.lepu.demo.cofig.Constant
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.FileUtil
import com.lepu.demo.util.StringUtil.*
import com.lepu.demo.util.icon.BitmapConvertor
import java.util.*

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
        binding.lewLayout.visibility = View.GONE
        binding.sp20Layout.visibility = View.GONE
        binding.aoj20aLayout.visibility = View.GONE
        binding.pc68bLayout.visibility = View.GONE
        binding.ad5Layout.visibility = View.GONE
        binding.pc300Layout.visibility = View.GONE
        binding.lemLayout.visibility = View.GONE
        binding.bpmLayout.visibility = View.GONE
        binding.pc60fwLayout.visibility = View.GONE
        if (v == null) return
        v.visibility = View.VISIBLE
    }

    private fun initView() {
        mainViewModel.bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.settingLayout.visibility = View.VISIBLE
            } else {
                binding.settingLayout.visibility = View.GONE
            }
        }
        mainViewModel.curBluetooth.observe(viewLifecycleOwner) {
            when (it!!.modelNo) {
                Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                    setViewVisible(binding.er1Layout)
                    LpBleUtil.getEr1VibrateConfig(it.modelNo)
                }
                Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                    setViewVisible(binding.er2Layout)
                    LpBleUtil.getEr1VibrateConfig(it.modelNo)
                }
                Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                    setViewVisible(binding.er2Layout)
                    LpBleUtil.getEr2SwitcherState(it.modelNo)
                }
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
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
                Bluetooth.MODEL_BBSM_S1, Bluetooth.MODEL_BBSM_S2,
                Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_CHECKO2,
                Bluetooth.MODEL_O2M, Bluetooth.MODEL_SLEEPO2,
                Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
                Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
                Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
                Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_CMRING,
                Bluetooth.MODEL_OXYU, Bluetooth.MODEL_AI_S100 -> {
                    setViewVisible(binding.o2Layout)
                    LpBleUtil.getInfo(it.modelNo)
                }
                Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_MY_SCALE,
                Bluetooth.MODEL_F5_SCALE, Bluetooth.MODEL_F8_SCALE -> {
                    setViewVisible(binding.scaleLayout)
                }
                Bluetooth.MODEL_PC100 -> {
                    setViewVisible(binding.pc100Layout)
                }
                Bluetooth.MODEL_AP20 -> {
                    setViewVisible(binding.ap20Layout)
                    LpBleUtil.ap20GetConfig(it.modelNo, state)
                }
                Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                    setViewVisible(binding.lewLayout)
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
                Bluetooth.MODEL_BPM -> {
                    setViewVisible(binding.bpmLayout)
                }
                Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
                Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW -> {
                    setViewVisible(binding.pc60fwLayout)
                }
                else -> {
                    setViewVisible(null)
                }
            }
        }
        mainViewModel.er1Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1_N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM1) {
                binding.er1Version.setText("${it.hwV}")
                binding.er1Sn.setText("${it.sn}")
                binding.er1Code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_DUOEK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM3) {
                binding.er2Version.setText("${it.hwV}")
                binding.er2Sn.setText("${it.sn}")
                binding.er2Code.setText("${it.branchCode}")
            }
        }
        mainViewModel.er2Info.observe(viewLifecycleOwner) {
            binding.er2Version.setText("${it.hwVersion}")
            binding.er2Sn.setText("${it.serialNum}")
            binding.er2Code.setText("${it.branchCode}")
        }
        mainViewModel.boInfo.observe(viewLifecycleOwner) {
            binding.pc60fwCode.setText("${it.branchCode}")
        }
        mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
            binding.o2Version.setText("${it.hwVersion}")
            binding.o2Sn.setText("${it.sn}")
            binding.o2Code.setText("${it.branchCode}")
        }
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
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
            binding.sendCmd.text = cmdStr
        }

        //-------------------------er1--------------------
        binding.er1FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er1Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.er1Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.er1Code.text.toString())
            if (tempCode.isNullOrEmpty()) {
                enableCode = false
            } else if (tempCode.length == 8) {
                config.setBranchCode(tempCode)
            } else {
                Toast.makeText(context, "code请输入8位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            config.setBurnFlag(enableSn, enableVersion, enableCode)
            LpBleUtil.burnFactoryInfo(Constant.BluetoothConfig.currentModel[0], config)
        }
        binding.er1SetSound.setOnClickListener {
            if (binding.er1Hr1.text.toString().isNullOrEmpty() || binding.er1Hr2.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                val temp1 = trimStr(binding.er1Hr1.text.toString())
                val temp2 = trimStr(binding.er1Hr2.text.toString())
                if (isNumber(temp1) && isNumber(temp2)) {
                    switchState = !switchState
                    LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, temp1.toInt(), temp2.toInt())
                    cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                    binding.sendCmd.text = cmdStr
                } else {
                    Toast.makeText(context, "请输入数字", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.er1GetConfig.setOnClickListener {
            LpBleUtil.getEr1VibrateConfig(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.er1SetHr.setOnClickListener {
            if (binding.er1Hr1.text.toString().isNullOrEmpty() || binding.er1Hr2.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                val temp1 = trimStr(binding.er1Hr1.text.toString())
                val temp2 = trimStr(binding.er1Hr2.text.toString())
                if (isNumber(temp1) && isNumber(temp2)) {
                    LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], switchState, temp1.toInt(), temp2.toInt())
                    cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                    binding.sendCmd.text = cmdStr
                } else {
                    Toast.makeText(context, "请输入数字", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //-------------------------er2/duoek------------------------
        binding.er2FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er2Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.er2Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.er2Code.text.toString())
            if (tempCode.isNullOrEmpty()) {
                enableCode = false
            } else if (tempCode.length == 8) {
                config.setBranchCode(tempCode)
            } else {
                Toast.makeText(context, "code请输入8位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            config.setBurnFlag(enableSn, enableVersion, enableCode)
            LpBleUtil.burnFactoryInfo(Constant.BluetoothConfig.currentModel[0], config)
        }
        binding.er2GetConfig.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_ER2) {
                LpBleUtil.getEr2SwitcherState(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.getEr1VibrateConfig(Constant.BluetoothConfig.currentModel[0])
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.er2SetConfig.setOnClickListener {
            switchState = !switchState
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_ER2) {
                LpBleUtil.setEr2SwitcherState(Constant.BluetoothConfig.currentModel[0], switchState)
            } else {
                LpBleUtil.setDuoekVibrate(Constant.BluetoothConfig.currentModel[0], switchState, 0, 0, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        //-------------------------lew------------------------
        // 时间
        binding.lewGetTime.setOnClickListener {
            LpBleUtil.lewGetTime(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetTime.setOnClickListener {
            state++
            if (state > LewBleCmd.TimeFormat.FORMAT_24H) {
                state = LewBleCmd.TimeFormat.FORMAT_12H
            }
            val data = TimeData()
            data.formatHour = state
            data.formatDay = state
            LpBleUtil.lewSetTime(Constant.BluetoothConfig.currentModel[0], data)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $data"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        // 绑定
        binding.lewBound.setOnClickListener {
            LpBleUtil.lewBoundDevice(Constant.BluetoothConfig.currentModel[0], true)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        // 解绑
        binding.lewUnbound.setOnClickListener {
            LpBleUtil.lewBoundDevice(Constant.BluetoothConfig.currentModel[0], false)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        // 获取电量
        binding.lewGetBattery.setOnClickListener {
            LpBleUtil.lewGetBattery(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        // 系统配置（语言、单位、翻腕亮屏、左右手）
        binding.lewGetSystemSetting.setOnClickListener {
            LpBleUtil.lewGetSystemSetting(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetSystemSetting.setOnClickListener {
            val setting = SystemSetting()
            setting.language = LewBleCmd.Language.CHINESE

            val unit = UnitSetting()
            unit.lengthUnit = LewBleCmd.Unit.LENGTH_FEET_INCH
            unit.weightUnit = LewBleCmd.Unit.WEIGHT_QUARTZ
            unit.tempUnit = LewBleCmd.Unit.TEMP_F
            setting.unit = unit

            val handRaise = HandRaiseSetting()
            switchState = !switchState
            handRaise.switch = switchState
            handRaise.startHour = 0
            handRaise.startMin = 0
            handRaise.stopHour = 24
            handRaise.stopMin = 0
            setting.handRaise = handRaise

            setting.hand = LewBleCmd.Hand.RIGHT
            Log.d("test12345", "lewSetSystemSetting $setting")
            LpBleUtil.lewSetSystemSetting(Constant.BluetoothConfig.currentModel[0], setting)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $setting"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetLanguage.setOnClickListener {
            LpBleUtil.lewGetLanguage(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetLanguage.setOnClickListener {
            state++
            if (state > LewBleCmd.Language.FARSI) {
                state = LewBleCmd.Language.ENGLISH
            }
            Log.d("test12345", "lewSetLanguage $state")
            LpBleUtil.lewSetLanguage(Constant.BluetoothConfig.currentModel[0], state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $state"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetNetwork.setOnClickListener {
            LpBleUtil.lewGetDeviceNetwork(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetUnit.setOnClickListener {
            LpBleUtil.lewGetUnit(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetUnit.setOnClickListener {
            val unit = UnitSetting()
            state++
            if (state > LewBleCmd.Unit.LENGTH_FEET_INCH) {
                state = LewBleCmd.Unit.LENGTH_KM_M
            }
            unit.lengthUnit = state
            state++
            if (state > LewBleCmd.Unit.WEIGHT_KG_G) {
                state = LewBleCmd.Unit.WEIGHT_QUARTZ
            }
            unit.weightUnit = state
            state++
            if (state > LewBleCmd.Unit.TEMP_F) {
                state = LewBleCmd.Unit.TEMP_C
            }
            unit.tempUnit = state
            Log.d("test12345", "lewSetUnit $unit")
            LpBleUtil.lewSetUnit(Constant.BluetoothConfig.currentModel[0], unit)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $unit"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetHandRaise.setOnClickListener {
            LpBleUtil.lewGetHandRaise(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetHandRaise.setOnClickListener {
            val handRaise = HandRaiseSetting()
            switchState = !switchState
            handRaise.switch = switchState
            handRaise.startHour = 0
            handRaise.startMin = 0
            handRaise.stopHour = 24
            handRaise.stopMin = 0
            Log.d("test12345", "lewSetHandRaise $handRaise")
            LpBleUtil.lewSetHandRaise(Constant.BluetoothConfig.currentModel[0], handRaise)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $handRaise"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetLrHand.setOnClickListener {
            LpBleUtil.lewGetLrHand(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetLrHand.setOnClickListener {
            state++
            if (state > LewBleCmd.Hand.RIGHT) {
                state = LewBleCmd.Hand.LEFT
            }
            Log.d("test12345", "lewSetLrHand $state")
            LpBleUtil.lewSetLrHand(Constant.BluetoothConfig.currentModel[0], state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $state"
            binding.sendCmd.text = cmdStr
        }
        // 寻找设备
        binding.lewFindDevice.setOnClickListener {
            switchState = !switchState
            LpBleUtil.lewFindDevice(Constant.BluetoothConfig.currentModel[0], switchState)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        // 勿扰模式
        binding.lewGetNoDisturb.setOnClickListener {
            LpBleUtil.lewGetNoDisturbMode(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetNoDisturb.setOnClickListener {
            switchState = !switchState
            val mode = NoDisturbMode()
            mode.switch = switchState

            val item = NoDisturbMode.Item()
            item.startHour = 7
            item.startMin = 0
            item.stopHour = 9
            item.stopMin = 30
            val item2 = NoDisturbMode.Item()
            item2.startHour = 17
            item2.startMin = 15
            item2.stopHour = 19
            item2.stopMin = 45

            mode.items.add(item)
            mode.items.add(item2)
            Log.d("test12345", "lewSetNoDisturb $mode")
            LpBleUtil.lewSetNoDisturbMode(Constant.BluetoothConfig.currentModel[0], mode)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $mode"
            binding.sendCmd.text = cmdStr
        }
        // app通知开关
        binding.lewGetAppSwitch.setOnClickListener {
            LpBleUtil.lewGetAppSwitch(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetAppSwitch.setOnClickListener {
            switchState = !switchState
            val appSwitch = AppSwitch()
            appSwitch.all = switchState
            switchState = !switchState
            appSwitch.phone = switchState
            switchState = !switchState
            appSwitch.message = switchState
            switchState = !switchState
            appSwitch.qq = switchState
            switchState = !switchState
            appSwitch.wechat = switchState
            switchState = !switchState
            appSwitch.email = switchState
            switchState = !switchState
            appSwitch.facebook = switchState
            switchState = !switchState
            appSwitch.twitter = switchState
            switchState = !switchState
            appSwitch.whatsApp = switchState
            switchState = !switchState
            appSwitch.instagram = switchState
            switchState = !switchState
            appSwitch.skype = switchState
            switchState = !switchState
            appSwitch.linkedIn = switchState
            switchState = !switchState
            appSwitch.line = switchState
            switchState = !switchState
            appSwitch.weibo = switchState
            switchState = !switchState
            appSwitch.other = switchState
            Log.d("test12345", "lewSetAppSwitch $appSwitch")
            LpBleUtil.lewSetAppSwitch(Constant.BluetoothConfig.currentModel[0], appSwitch)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $appSwitch"
            binding.sendCmd.text = cmdStr
        }
        // 消息通知
        binding.lewPhoneNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.PHONE
            noti.time = System.currentTimeMillis().div(1000).toInt()

            val phone = NotificationInfo.NotiPhone()
            phone.name = "张三里abc123"
            phone.phone = "13420111867"

            noti.info = phone
            Log.d("test12345", "lewPhoneNoti $noti")
            LpBleUtil.lewSendNotification(Constant.BluetoothConfig.currentModel[0], noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        binding.lewMessageNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.MESSAGE
            noti.time = System.currentTimeMillis().div(1000).toInt()

            val mess = NotificationInfo.NotiMessage()
            mess.name = "张三里abc123"
            mess.phone = "13420111867"
            mess.text = "张三里abc123"

            noti.info = mess
            Log.d("test12345", "lewMessageNoti $noti")
            LpBleUtil.lewSendNotification(Constant.BluetoothConfig.currentModel[0], noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        binding.lewOtherNoti.setOnClickListener {
            val noti = NotificationInfo()
            noti.appId = LewBleCmd.AppId.OTHER
            noti.time = System.currentTimeMillis().div(1000).toInt()

            val other = NotificationInfo.NotiOther()
            other.name = "张三里abc123"
            other.text = "张三里abc123"

            noti.info = other
            Log.d("test12345", "lewOtherNoti $noti")
            LpBleUtil.lewSendNotification(Constant.BluetoothConfig.currentModel[0], noti)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $noti"
            binding.sendCmd.text = cmdStr
        }
        // 设备模式
        binding.lewGetDeviceMode.setOnClickListener {
            LpBleUtil.lewGetDeviceMode(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetDeviceMode.setOnClickListener {
            state++
            if (state > LewBleCmd.DeviceMode.MODE_FREE) {
                state = LewBleCmd.DeviceMode.MODE_NORMAL
            }
            Log.d("test12345", "lewSetDeviceMode $state")
            LpBleUtil.lewSetDeviceMode(Constant.BluetoothConfig.currentModel[0], state)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $state"
            binding.sendCmd.text = cmdStr
        }
        // 闹钟
        binding.lewGetAlarmInfo.setOnClickListener {
            LpBleUtil.lewGetAlarmClock(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetAlarmInfo.setOnClickListener {
            switchState = !switchState
            val info = AlarmClockInfo()
            val item = AlarmClockInfo.Item()
            item.hour = 7
            item.minute = 10
            item.repeat = switchState
            item.switch = switchState
            item.everySunday = switchState
            switchState = !switchState
            item.everyMonday = switchState
            switchState = !switchState
            item.everyTuesday = switchState
            switchState = !switchState
            item.everyWednesday = switchState
            switchState = !switchState
            item.everyThursday = switchState
            switchState = !switchState
            item.everyFriday = switchState
            switchState = !switchState
            item.everySaturday = switchState

            val item2 = AlarmClockInfo.Item()
            item2.hour = 17
            item2.minute = 10
            item2.repeat = switchState
            item2.switch = switchState
            item2.everySunday = switchState
            switchState = !switchState
            item2.everyMonday = switchState
            switchState = !switchState
            item2.everyTuesday = switchState
            switchState = !switchState
            item2.everyWednesday = switchState
            switchState = !switchState
            item2.everyThursday = switchState
            switchState = !switchState
            item2.everyFriday = switchState
            switchState = !switchState
            item2.everySaturday = switchState

            info.items.add(item)
            info.items.add(item2)
            Log.d("test12345", "lewSetAlarmInfo $info")
            LpBleUtil.lewSetAlarmClock(Constant.BluetoothConfig.currentModel[0], info)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $info"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetPhoneSwitch.setOnClickListener {
            LpBleUtil.lewGetPhoneSwitch(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetPhoneSwitch.setOnClickListener {
            val phoneSwitch = PhoneSwitch()
            switchState = !switchState
            phoneSwitch.call = switchState
            switchState = !switchState
            phoneSwitch.message = switchState
            Log.d("test12345", "lewSetPhoneSwitch $phoneSwitch")
            LpBleUtil.lewSetPhoneSwitch(Constant.BluetoothConfig.currentModel[0], phoneSwitch)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $phoneSwitch"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetMedicineRemind.setOnClickListener {
            LpBleUtil.lewGetMedicineRemind(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetMedicineRemind.setOnClickListener {
            val remind = MedicineRemind()
            val item = MedicineRemind.Item()
            item.hour = 9
            item.minute = 10
            item.repeat = switchState
            item.switch = switchState
            item.everySunday = switchState
            switchState = !switchState
            item.everyMonday = switchState
            switchState = !switchState
            item.everyTuesday = switchState
            switchState = !switchState
            item.everyWednesday = switchState
            switchState = !switchState
            item.everyThursday = switchState
            switchState = !switchState
            item.everyFriday = switchState
            switchState = !switchState
            item.everySaturday = switchState
            item.name = "感冒药"

            val item2 = MedicineRemind.Item()
            item2.hour = 17
            item2.minute = 10
            item2.repeat = switchState
            item2.switch = switchState
            item2.everySunday = switchState
            switchState = !switchState
            item2.everyMonday = switchState
            switchState = !switchState
            item2.everyTuesday = switchState
            switchState = !switchState
            item2.everyWednesday = switchState
            switchState = !switchState
            item2.everyThursday = switchState
            switchState = !switchState
            item2.everyFriday = switchState
            switchState = !switchState
            item2.everySaturday = switchState
            item2.name = "发烧药"

            remind.items.add(item)
            remind.items.add(item2)
            Log.d("test12345", "lewSetMedicineRemind $remind")
            LpBleUtil.lewSetMedicineRemind(Constant.BluetoothConfig.currentModel[0], remind)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $remind"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetMeasureSetting.setOnClickListener {
            LpBleUtil.lewGetMeasureSetting(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetMeasureSetting.setOnClickListener {
            val setting = MeasureSetting()

            val sportTarget = SportTarget()
            sportTarget.step = 12
            sportTarget.distance = 5000
            sportTarget.calories = 12
            sportTarget.sleep = 30
            sportTarget.sportTime = 12
            setting.sportTarget = sportTarget

            switchState = !switchState
            setting.targetRemind = switchState

            val sittingRemind = SittingRemind()
            switchState = !switchState
            sittingRemind.switch = switchState
            switchState = !switchState
            sittingRemind.noonSwitch = switchState
            sittingRemind.everySunday = switchState
            switchState = !switchState
            sittingRemind.everyMonday = switchState
            switchState = !switchState
            sittingRemind.everyTuesday = switchState
            switchState = !switchState
            sittingRemind.everyWednesday = switchState
            switchState = !switchState
            sittingRemind.everyThursday = switchState
            switchState = !switchState
            sittingRemind.everyFriday = switchState
            switchState = !switchState
            sittingRemind.everySaturday = switchState
            sittingRemind.startHour = 10
            sittingRemind.startMin = 0
            sittingRemind.stopHour = 18
            sittingRemind.stopMin = 30
            setting.sittingRemind = sittingRemind

            val hrDetect = HrDetect()
            hrDetect.switch = switchState
            hrDetect.interval = 5

            val oxyDetect = OxyDetect()
            oxyDetect.switch = switchState
            oxyDetect.interval = 5

            setting.hrDetect = hrDetect
            setting.oxyDetect = oxyDetect
            Log.d("test12345", "lewSetMeasureSetting $setting")
            LpBleUtil.lewSetMeasureSetting(Constant.BluetoothConfig.currentModel[0], setting)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $setting"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSportTarget.setOnClickListener {
            LpBleUtil.lewGetSportTarget(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetSportTarget.setOnClickListener {
            val sportTarget = SportTarget()
            sportTarget.step = 22
            sportTarget.distance = 5000
            sportTarget.calories = 22
            sportTarget.sleep = 30
            sportTarget.sportTime = 22
            Log.d("test12345", "lewSetSportTarget $sportTarget")
            LpBleUtil.lewSetSportTarget(Constant.BluetoothConfig.currentModel[0], sportTarget)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $sportTarget"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetTargetRemind.setOnClickListener {
            LpBleUtil.lewGetTargetRemind(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetTargetRemind.setOnClickListener {
            switchState = !switchState
            Log.d("test12345", "lewSetTargetRemind $switchState")
            LpBleUtil.lewSetTargetRemind(Constant.BluetoothConfig.currentModel[0], switchState)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $switchState"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSittingRemind.setOnClickListener {
            LpBleUtil.lewGetSittingRemind(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetSittingRemind.setOnClickListener {
            val sittingRemind = SittingRemind()
            switchState = !switchState
            sittingRemind.switch = switchState
            switchState = !switchState
            sittingRemind.noonSwitch = switchState
            sittingRemind.everySunday = switchState
            switchState = !switchState
            sittingRemind.everyMonday = switchState
            switchState = !switchState
            sittingRemind.everyTuesday = switchState
            switchState = !switchState
            sittingRemind.everyWednesday = switchState
            switchState = !switchState
            sittingRemind.everyThursday = switchState
            switchState = !switchState
            sittingRemind.everyFriday = switchState
            switchState = !switchState
            sittingRemind.everySaturday = switchState
            sittingRemind.startHour = 10
            sittingRemind.startMin = 0
            sittingRemind.stopHour = 18
            sittingRemind.stopMin = 30
            Log.d("test12345", "lewSetSittingRemind $sittingRemind")
            LpBleUtil.lewSetSittingRemind(Constant.BluetoothConfig.currentModel[0], sittingRemind)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $sittingRemind"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetHrDetect.setOnClickListener {
            LpBleUtil.lewGetHrDetect(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetHrDetect.setOnClickListener {
            switchState = !switchState
            val detect = HrDetect()
            detect.switch = switchState
            detect.interval = 5
            Log.d("test12345", "lewSetHrDetect $detect")
            LpBleUtil.lewSetHrDetect(Constant.BluetoothConfig.currentModel[0], detect)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $detect"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetOxyDetect.setOnClickListener {
            LpBleUtil.lewGetOxyDetect(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetOxyDetect.setOnClickListener {
            switchState = !switchState
            val detect = OxyDetect()
            detect.switch = switchState
            detect.interval = 5
            Log.d("test12345", "lewSetOxyDetect $detect")
            LpBleUtil.lewSetOxyDetect(Constant.BluetoothConfig.currentModel[0], detect)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $detect"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetUserInfo.setOnClickListener {
            LpBleUtil.lewGetUserInfo(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetUserInfo.setOnClickListener {
            val info = UserInfo()
            info.aid = 12345
            info.uid = -1
            info.fName = "魑"
            info.name = "魅魍魉123"
            info.birthday = "1990-10-20"
            info.height = 170
            info.weight = 70f
            info.gender = 0
            Log.d("test12345", "lewSetUserInfo $info")
            LpBleUtil.lewSetUserInfo(Constant.BluetoothConfig.currentModel[0], info)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $info"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetPhonebook.setOnClickListener {
            LpBleUtil.lewGetPhoneBook(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetPhonebook.setOnClickListener {
            val list = PhoneBook()
            list.leftSize = 0
            list.currentSize = 3

            val item = PhoneBook.Item()
            item.id = 11111
            item.name = "张三里abc111"
            item.phone = "13420111867"
            val item2 = PhoneBook.Item()
            item2.id = 11112
            item2.name = "张三里abc112"
            item2.phone = "13420111867"
            val item3 = PhoneBook.Item()
            item3.id = 11113
            item3.name = "张三里abc113"
            item3.phone = "13420111867"

            list.items.add(item)
            list.items.add(item2)
            list.items.add(item3)
            Log.d("test12345", "lewSetPhonebook $list")
            LpBleUtil.lewSetPhoneBook(Constant.BluetoothConfig.currentModel[0], list)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $list"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSos.setOnClickListener {
            LpBleUtil.lewGetSosContact(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetSos.setOnClickListener {
            val sos = SosContact()
            switchState = !switchState
            sos.switch = switchState

            val item = SosContact.Item()
            item.name = "张三里abc111"
            item.phone = "13420111867"
            state++
            if (state > LewBleCmd.RelationShip.OTHER) {
                state = LewBleCmd.RelationShip.FATHER
            }
            item.relation = state
            val item2 = SosContact.Item()
            item2.name = "张三里abc112"
            item2.phone = "13420111867"
            state++
            if (state > LewBleCmd.RelationShip.OTHER) {
                state = LewBleCmd.RelationShip.FATHER
            }
            item2.relation = state

            sos.items.add(item)
//            sos.items.add(item2)
            Log.d("test12345", "lewSetSos $sos")
            LpBleUtil.lewSetSosContact(Constant.BluetoothConfig.currentModel[0], sos)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $sos"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetDial.setOnClickListener {
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetDial.setOnClickListener {
            // ???
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSecondScreen.setOnClickListener {
            LpBleUtil.lewGetSecondScreen(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetSecondScreen.setOnClickListener {
            val screen = SecondScreen()
            switchState = !switchState
            screen.medicineRemind = switchState
            screen.weather = switchState
            switchState = !switchState
            screen.clock = switchState
            screen.heartRate = switchState
            switchState = !switchState
            screen.spo2 = switchState
            screen.peripherals = switchState
            Log.d("test12345", "lewSetSecondScreen $screen")
            LpBleUtil.lewSetSecondScreen(Constant.BluetoothConfig.currentModel[0], screen)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $screen"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetCards.setOnClickListener {
            LpBleUtil.lewGetCards(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetCards.setOnClickListener {
            val cards = intArrayOf(LewBleCmd.Cards.HR, LewBleCmd.Cards.TARGET, LewBleCmd.Cards.WEATHER)
            Log.d("test12345", "lewSetCards ${Arrays.toString(cards)}")
            LpBleUtil.lewSetCards(Constant.BluetoothConfig.currentModel[0], cards)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n ${Arrays.toString(cards)}"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSportData.setOnClickListener {
            LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], LewBleCmd.ListType.SPORT, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetEcgData.setOnClickListener {
            LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], LewBleCmd.ListType.ECG, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetRtData.setOnClickListener {
            LpBleUtil.lewGetRtData(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetOxyData.setOnClickListener {
            LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], LewBleCmd.ListType.OXY, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetHrData.setOnClickListener {
            LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], LewBleCmd.ListType.HR, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetSleepData.setOnClickListener {
            LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], LewBleCmd.ListType.SLEEP, 0)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetHrThreshold.setOnClickListener {
            LpBleUtil.lewGetHrThreshold(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetHrThreshold.setOnClickListener {
            val threshold = HrThreshold()
            switchState = !switchState
            threshold.switch = switchState
            threshold.threshold = 100
            Log.d("test12345", "lewSetHrThreshold $threshold")
            LpBleUtil.lewSetHrThreshold(Constant.BluetoothConfig.currentModel[0], threshold)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $threshold"
            binding.sendCmd.text = cmdStr
        }
        binding.lewGetOxyThreshold.setOnClickListener {
            LpBleUtil.lewGetOxyThreshold(Constant.BluetoothConfig.currentModel[0])
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
            binding.sendCmd.text = cmdStr
        }
        binding.lewSetOxyThreshold.setOnClickListener {
            val threshold = OxyThreshold()
            switchState = !switchState
            threshold.switch = switchState
            threshold.threshold = 90
            Log.d("test12345", "lewSetOxyThreshold $threshold")
            LpBleUtil.lewSetOxyThreshold(Constant.BluetoothConfig.currentModel[0], threshold)
            binding.responseCmd.text = ""
            binding.content.text = ""
            binding.sendCmd.text = ""
            cmdStr = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])} \n $threshold"
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
        binding.o2FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            if (binding.o2Version.text.isNullOrEmpty()) {
                enableVersion = false
            } else {
                config.setHwVersion(binding.o2Version.text.first())
            }
            var enableSn = true
            if (binding.o2Sn.text.isNullOrEmpty()) {
                enableSn = false
            } else {
                config.setSnCode(trimStr(binding.o2Sn.text.toString()))
            }
            var enableCode = true
            if (binding.o2Code.text.isNullOrEmpty()) {
                enableCode = false
            } else {
                config.setBranchCode(trimStr(binding.o2Code.text.toString()))
            }
            config.setBurnFlag(enableSn, enableVersion, enableCode)
            LpBleUtil.burnFactoryInfo(Constant.BluetoothConfig.currentModel[0], config)
        }
        binding.o2SetOxiThr.setOnClickListener {
            if (binding.o2OxiThr.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_OXI_THR, trimStr(binding.o2OxiThr.text.toString()).toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
        }
        binding.o2SetHrThr1.setOnClickListener {
            if (binding.o2HrThr1.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_HR_LOW_THR, trimStr(binding.o2HrThr1.text.toString()).toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
        }
        binding.o2SetHrThr2.setOnClickListener {
            if (binding.o2HrThr2.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_HR_HIGH_THR, trimStr(binding.o2HrThr2.text.toString()).toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
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
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S2
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
            if (binding.o2MtThr.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_MT_THR, trimStr(binding.o2MtThr.text.toString()).toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
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
            if (binding.o2IvThr.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_IV_THR, trimStr(binding.o2IvThr.text.toString()).toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
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
//            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0], OxyBleCmd.SYNC_TYPE_ALL_SW, state)
            LpBleUtil.updateSetting(Constant.BluetoothConfig.currentModel[0],
                arrayOf(OxyBleCmd.SYNC_TYPE_OXI_SWITCH, OxyBleCmd.SYNC_TYPE_HR_SWITCH, OxyBleCmd.SYNC_TYPE_MT_SW, OxyBleCmd.SYNC_TYPE_IV_SW),
                intArrayOf(state, state, state, state))
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
        // ---------------------------bpm---------------------------
        binding.bpmGetState.setOnClickListener {
            if (LpBleUtil.isRtStop(Constant.BluetoothConfig.currentModel[0])) {
                LpBleUtil.startRtTask()
            } else {
                LpBleUtil.stopRtTask()
            }
        }
        // ---------------------------pc60fw---------------------------
        binding.pc60fwSetCode.setOnClickListener {
            val code = trimStr(binding.pc60fwCode.text.toString())
            if (code.isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else if (code.length != 8) {
                Toast.makeText(context, "code请输入8位", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.pc60fwSetBranchCode(Constant.BluetoothConfig.currentModel[0], code)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
        }
    }

    private fun setReceiveCmd(bytes: ByteArray) {
        if (isReceive) {
            binding.receiveCmd.text = "receive : " + bytesToHex(bytes)
        }
    }

    private fun initLiveEvent() {
        //----------------------------er1/duoek-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnFactoryInfo)
            .observe(this) {
                Toast.makeText(context, "烧录成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetSwitcherState)
            .observe(this) {
                LpBleUtil.getEr1VibrateConfig(it.model)
                when (it.model) {
                    Bluetooth.MODEL_ER1 -> {
                        Toast.makeText(context, "ER1 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_ER1_N -> {
                        Toast.makeText(context, "VBeat 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM1 -> {
                        Toast.makeText(context, "HHM1 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_DUOEK -> {
                        Toast.makeText(context, "DuoEK 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM2 -> {
                        Toast.makeText(context, "HHM2 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM3 -> {
                        Toast.makeText(context, "HHM3 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(context, "ER1 设置参数成功", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1VibrateConfig)
            .observe(this) {
                val data = it.data as ByteArray
                setReceiveCmd(data)
                if (it.model == Bluetooth.MODEL_DUOEK || it.model == Bluetooth.MODEL_HHM2 || it.model == Bluetooth.MODEL_HHM3) {
                    val data = it.data as ByteArray
                    val config = SwitcherConfig.parse(data)
                    switchState = config.switcher
                    var temp = "关"
                    if (config.switcher)
                        temp = "开"
                    binding.er2SetConfig.text = "声音$temp"
                    binding.content.text = "switcher : ${config.switcher} vector : ${config.vector} motionCount : ${config.motionCount} motionWindows : ${config.motionWindows}"
                    when (it.model) {
                        Bluetooth.MODEL_DUOEK -> {
                            Toast.makeText(context, "DuoEK 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM2 -> {
                            Toast.makeText(context, "HHM2 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM3 -> {
                            Toast.makeText(context, "HHM3 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(context, "DuoEK 获取参数成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val config = VbVibrationSwitcherConfig.parse(data)
                    switchState = config.switcher
                    this.config = config
                    var temp = "关"
                    if (config.switcher)
                        temp = "开"
                    binding.er1SetSound.text = "震动$temp"
                    binding.er1Hr1.setText("${config.hr1}")
                    binding.er1Hr2.setText("${config.hr2}")
                    binding.content.text = "switcher : " + config.switcher + " hr1 : " + config.hr1 + " hr2 : " + config.hr2
                    when (it.model) {
                        Bluetooth.MODEL_ER1 -> {
                            Toast.makeText(context, "ER1 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_ER1_N -> {
                            Toast.makeText(context, "VBeat 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM1 -> {
                            Toast.makeText(context, "HHM1 获取参数成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(context, "ER1 获取参数成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        //-----------------------------er2------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BurnFactoryInfo)
            .observe(this) {
                Toast.makeText(context, "烧录成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetSwitcherState)
            .observe(this) {
                LpBleUtil.getEr2SwitcherState(it.model)
                when (it.model) {
                    Bluetooth.MODEL_ER2 -> {
                        Toast.makeText(context, "ER2 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_LP_ER2 -> {
                        Toast.makeText(context, "LP ER2 设置参数成功", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(context, "ER2 设置参数成功", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SwitcherState)
            .observe(this) {
                val data = it.data as ByteArray
                setReceiveCmd(data)
                val config = SwitcherConfig.parse(data)
                switchState = config.switcher
                this.config = config
                var temp = "关"
                if (config.switcher)
                    temp = "开"
                binding.er2SetConfig.text = "声音$temp"
                binding.content.text = "switcher : " + config.switcher + " vector : " + config.vector + " motionCount : " + config.motionCount + " motionWindows : " + config.motionWindows
                when (it.model) {
                    Bluetooth.MODEL_ER2 -> {
                        Toast.makeText(context, "ER2 获取参数成功", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_LP_ER2 -> {
                        Toast.makeText(context, "LP ER2 获取参数成功", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(context, "ER2 获取参数成功", Toast.LENGTH_SHORT).show()
                }
            }
        //----------------------------bp2/bp2a/bp2t-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigResult)
            .observe(this) {
                val data = it.data as Bp2Config
                var temp = "关"
                switchState = data.beepSwitch
                if (data.beepSwitch) {
                    temp = "开"
                }
                binding.bp2SetConfig.text = "声音$temp"
                Toast.makeText(context, "bp2 获取参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "bp2 设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
            .observe(this) {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2 获取设备状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2 设置设备状态$data", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyState)
            .observe(this) {
                val data = it.data as Bp2BlePhyState
                setReceiveCmd(data.bytes)
                config = data
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2 获取理疗状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError)
            .observe(this) {
                Toast.makeText(context, "获取失败，请先进入理疗模式", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetPhyState)
            .observe(this) {
                val data = it.data as Bp2BlePhyState
                setReceiveCmd(data.bytes)
                config = data
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2 设置理疗状态成功", Toast.LENGTH_SHORT).show()
            }

        //------------------------------bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wDeleteFile)
            .observe(this) {
                Toast.makeText(context, "bp2w 删除文件成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "bp2w 设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSwitchState)
            .observe(this) {
                LpBleUtil.bp2GetRtState(it.model)
                Toast.makeText(context, "bp2w 设置主机状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtState)
            .observe(this) {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.bp2wGetState.text = "实时状态${data.status}"
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2w 获取主机状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig)
            .observe(this) {
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
                Toast.makeText(context, "bp2w 获取参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiScanning)
            .observe(this) {
                binding.content.text = "设备正在扫描wifi"
                handler.postDelayed({
                    LpBleUtil.bp2GetWifiDevice(it.model)
                }, 1000)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiDevice)
            .observe(this) {
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                bp2wAdapter.setNewInstance(data.wifiList)
                bp2wAdapter.notifyDataSetChanged()
                binding.content.text = data.toString()
                Toast.makeText(context, "bp2w 获取路由成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetWifiConfig)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    LpBleUtil.bp2GetWifiConfig(it.model)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetWifiConfig)
            .observe(this) {
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

            }
        //------------------------------lp bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncUtcTime)
            .observe(this) {
                Toast.makeText(context, "le bp2w 同步 UTC 时间成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile)
            .observe(this) {
                Toast.makeText(context, "le bp2w 删除文件成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "le bp2w 设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState)
            .observe(this) {
                LpBleUtil.bp2GetRtState(it.model)
                Toast.makeText(context, "le bp2w 设置主机状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtState)
            .observe(this) {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.leBp2wGetState.text = "实时状态${data.status}"
                binding.content.text = data.toString()
                Toast.makeText(context, "le bp2w 获取主机状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetConfig)
            .observe(this) {
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
                Toast.makeText(context, "le bp2w 获取参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiScanning)
            .observe(this) {
                binding.content.text = "设备正在扫描wifi"
                LpBleUtil.bp2GetWifiDevice(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiDevice)
            .observe(this) {
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                leBp2wAdapter.setNewInstance(data.wifiList)
                leBp2wAdapter.notifyDataSetChanged()
                binding.content.text = data.toString()
                Toast.makeText(context, "le bp2w 获取路由成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    LpBleUtil.bp2GetWifiConfig(it.model)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetWifiConfig)
            .observe(this) {
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

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
            .observe(this) {
                val data = it.data as FileListCrc
                binding.content.text = data.toString()
                Toast.makeText(context, "le bp2w 获取文件列表校验成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WritingFileProgress)
            .observe(this) {
                val data = it.data as Bp2FilePart
                Toast.makeText(context, "le bp2w 写文件列表进度：" + (data.percent * 100).toInt().toString() + "%", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileComplete)
            .observe(this) {
                val data = it.data as FileListCrc
                Toast.makeText(context, "le bp2w 写文件完成 crc：" + data.crc, Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
            .observe(this) {
                val data = it.data as String
                Toast.makeText(context, "le bp2w 写文件错误 filename：$data", Toast.LENGTH_SHORT).show()
            }
        //------------------------------o2/babyO2-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyBurnFactoryInfo)
            .observe(this) {
                Toast.makeText(context, "烧录成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this) {
                Toast.makeText(context, "o2/babyO2 设置参数成功 ${it.data}", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this) {
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
                Toast.makeText(context, "o2/babyO2 获取参数成功", Toast.LENGTH_SHORT).show()
            }
        //------------------------------ap20-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20BatLevel)
            .observe(this) {
                var data = it.data as Int
                binding.content.text = "电量${data.toString()}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20GetConfigResult)
            .observe(this) {
                var data = it.data as GetConfigResult
//                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
            }
        //------------------------------lew-------------------------------------
        LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseContent)
            .observe(this) {
                binding.responseCmd.text = "receive : ${bytesToHex(it)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewDeviceInfo)
            .observe(this) {
                val data = it.data as DeviceInfo
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBoundDevice)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "请求绑定 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewUnBoundDevice)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "请求解绑 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFindPhone)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = data.toString()
                Toast.makeText(context, "lew手表 查找手机 $data", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceNetwork)
            .observe(this) {
                val data = it.data as DeviceNetwork
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取联网模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBatteryInfo)
            .observe(this) {
                val data = it.data as BatteryInfo
                binding.content.text = data.toString()
                Toast.makeText(context, "lew手表 获取电量成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTime)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "设置时间 : $data"
                Toast.makeText(context, "lew手表 设置时间成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTime)
            .observe(this) {
                val data = it.data as TimeData
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取时间成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSystemSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置系统配置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSystemSetting)
            .observe(this) {
                val data = it.data as SystemSetting
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取系统配置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLanguageSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置语言成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLanguageSetting)
            .observe(this) {
                val data = it.data as Int
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取语言成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUnitSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置单位成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUnitSetting)
            .observe(this) {
                val data = it.data as UnitSetting
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取单位成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHandRaiseSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置翻腕亮屏成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHandRaiseSetting)
            .observe(this) {
                val data = it.data as HandRaiseSetting
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取翻腕亮屏成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetLrHandSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置左右手成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetLrHandSetting)
            .observe(this) {
                val data = it.data as Int
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取左右手成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetNoDisturbMode)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置勿扰模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetNoDisturbMode)
            .observe(this) {
                val data = it.data as NoDisturbMode
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取勿扰模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAppSwitch)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置app提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAppSwitch)
            .observe(this) {
                val data = it.data as AppSwitch
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取app提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSendNotification)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 发送消息成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetDeviceMode)
            .observe(this) {
                val data = it.data as Int
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取设备模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetDeviceMode)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置设备模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetAlarmClock)
            .observe(this) {
                val data = it.data as AlarmClockInfo
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取闹钟成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetAlarmClock)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置闹钟成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneSwitch)
            .observe(this) {
                val data = it.data as PhoneSwitch
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取手机提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneSwitch)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置手机提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewPhoneCall)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 已挂断", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMeasureSetting)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置测量配置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMeasureSetting)
            .observe(this) {
                val data = it.data as MeasureSetting
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取测量配置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSportTarget)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置运动目标值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSportTarget)
            .observe(this) {
                val data = it.data as SportTarget
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取运动目标值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTargetRemind)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置达标提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetTargetRemind)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取达标提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetMedicineRemind)
            .observe(this) {
                val data = it.data as MedicineRemind
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取用药提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetMedicineRemind)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置用药提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSittingRemind)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置久坐提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSittingRemind)
            .observe(this) {
                val data = it.data as SittingRemind
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取久坐提醒成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrDetect)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置自动心率成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrDetect)
            .observe(this) {
                val data = it.data as HrDetect
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取自测心率成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyDetect)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置自动血氧成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyDetect)
            .observe(this) {
                val data = it.data as HrDetect
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取自测血氧成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetUserInfo)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置用户信息成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetUserInfo)
            .observe(this) {
                val data = it.data as UserInfo
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取用户信息成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetPhoneBook)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置通讯录成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetPhoneBook)
            .observe(this) {
                val data = it.data as PhoneBook
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取通讯录成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSosContact)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置紧急联系人成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSosContact)
            .observe(this) {
                val data = it.data as SosContact
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取紧急联系人成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetSecondScreen)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置副屏成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetSecondScreen)
            .observe(this) {
                val data = it.data as SecondScreen
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取副屏设置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetCards)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置卡片成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetCards)
            .observe(this) {
                val data = it.data as IntArray
                binding.content.text = "${data.joinToString()}"
                Toast.makeText(context, "lew手表 获取卡片成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetHrThreshold)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置心率阈值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetHrThreshold)
            .observe(this) {
                val data = it.data as HrThreshold
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取心率阈值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetOxyThreshold)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 设置血氧阈值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewGetOxyThreshold)
            .observe(this) {
                val data = it.data as OxyThreshold
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取血氧阈值成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.content.text = "$data"
                Toast.makeText(context, "lew手表 获取实时数据成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList)
            .observe(this) {
                val data = it.data as LewBleResponse.FileList
                when (data.type) {
                    LewBleCmd.ListType.SPORT -> {
                        val list = SportList(data.content)
                        binding.content.text = "$list"
                    }
                    LewBleCmd.ListType.ECG -> {
                        val list = EcgList(data.content)
                        binding.content.text = "$list"
                    }
                    LewBleCmd.ListType.HR -> {
                        val list = HrList(data.content)
                        binding.content.text = "$list"
                    }
                    LewBleCmd.ListType.OXY -> {
                        val list = OxyList(data.content)
                        binding.content.text = "$list"
                    }
                    LewBleCmd.ListType.SLEEP -> {
                        val list = SleepList(data.content)
                        binding.content.text = "$list"
                    }
                }
                Toast.makeText(context, "lew手表 获取列表成功 ${data.type}", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
            .observe(this) {
                val data = it.data as LewBleResponse.EcgFile
                binding.content.text = "${data.fileName} ${bytesToHex(data.content)}"
                Toast.makeText(context, "lew手表 获取心电文件成功", Toast.LENGTH_SHORT).show()
            }



        //------------------------------sp20-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20Battery)
            .observe(this) {
                val data = it.data as Int
                binding.content.text = "电量${data.toString()}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20GetConfig)
            .observe(this) {
                val config = it.data as Sp20Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = config.toString()
            }

        //---------------------------aoj20a-------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observe(this) {
                Toast.makeText(context, "aoj20a 测量完成", Toast.LENGTH_SHORT).show()
                val data = it.data as Aoj20aBleResponse.TempRtData
                setReceiveCmd(data.bytes)
                binding.content.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData)
            .observe(this) {
                Toast.makeText(context, "aoj20a 删除成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observe(this) {
                val data = it.data as Aoj20aBleResponse.ErrorMsg
                binding.content.text = data.toString()
            }

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
            .observe(this) {
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery)
            .observe(this) {
                val data = it.data as Int
                binding.lemGetBattery.text = "电量$data%"
                Toast.makeText(context, "lem 获取电量成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode)
            .observe(this) {
                val data = it.data as Boolean
                binding.lemHeatSwitch.text = "加热$data"
                Toast.makeText(context, "lem 设置加热模式 $data 成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode)
            .observe(this) {
                val data = it.data as Int
                binding.lemMassMode.text = when (data) {
                    LemBleCmd.MassageMode.VITALITY -> "活力模式"
                    LemBleCmd.MassageMode.DYNAMIC -> "动感模式"
                    LemBleCmd.MassageMode.HAMMERING -> "捶击模式"
                    LemBleCmd.MassageMode.SOOTHING -> "舒缓模式"
                    LemBleCmd.MassageMode.AUTOMATIC -> "自动模式"
                    else -> ""
                }
                Toast.makeText(context, "lem 设置按摩模式 $data 成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel)
            .observe(this) {
                val data = it.data as Int
                binding.lemMassLevel.text = "按摩力度$data"
                Toast.makeText(context, "lem 设置按摩力度 $data 成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime)
            .observe(this) {
                val data = it.data as Int
                binding.lemMassTime.text = when (data) {
                    LemBleCmd.MassageTime.MIN_15 -> "15min"
                    LemBleCmd.MassageTime.MIN_10 -> "10min"
                    LemBleCmd.MassageTime.MIN_5 -> "5min"
                    else -> ""
                }
                Toast.makeText(context, "lem 设置按摩时间 $data 成功", Toast.LENGTH_SHORT).show()
            }

        //------------------------bpm-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmState)
            .observe(this) {
                val data = it.data as BpmBleResponse.RtState
                binding.content.text = "$data"
            }

        //------------------------pc60fw-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwSetCode)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    Toast.makeText(context, "设置code成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "设置code失败", Toast.LENGTH_SHORT).show()
                }
            }

        //-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1SetTime)
            .observe(this) {
                binding.content.text = "device init"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureState)
            .observe(this) {
                val state = it.data as Int
                binding.content.text = if (state == 1) "start bp" else "stop bp"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1RtData)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.RtData
                binding.content.text = "压力值 ：" + data.pressure.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1ErrorResult)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.ErrorResult
                binding.content.text = "测量出错 类型：" + data.type + " 结果：" + data.result
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetFileListComplete)
            .observe(this) {
                val bpw1FileList = it.data as Bpw1BleResponse.Bpw1FileList
                binding.content.text = bpw1FileList.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureResult)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.BpData
                binding.content.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1DeviceInfo)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.DeviceInfo
                binding.content.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetMeasureTime)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.MeasureTime
                binding.content.text = data.toString()
            }
    }

}