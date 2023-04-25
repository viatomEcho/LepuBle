package com.lepu.demo.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.lepu.demo.R
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.FscaleUserInfo
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.HexString.hexToBytes
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.MainViewModel
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ui.adapter.WifiAdapter
import com.lepu.demo.config.Constant
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.DateUtil
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
    private lateinit var settingViewModel: SettingViewModel
    private lateinit var measureTime: Array<String?>

    private var deviceFactoryData = DeviceFactoryData()

    private lateinit var config: Any
    private var phy = Bp2BlePhyState()
    private var isReceive = false

    private var switchState = false
    private var state = 0
    private var cmdStr = ""

    private var fileType = LeBp2wBleCmd.FileType.ECG_TYPE

    private lateinit var bp2wAdapter: WifiAdapter
    private var popupWindow: PopupWindow? = null
    private var alertDialog: AlertDialog? = null

    private var handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
    }

    private fun setViewVisible(v: View?) {
        binding.er1Layout.root.visibility = View.GONE
        binding.er2Layout.root.visibility = View.GONE
        binding.bp2Layout.root.visibility = View.GONE
        binding.leBp2wLayout.visibility = View.GONE
        binding.o2Layout.root.visibility = View.GONE
        binding.scaleLayout.visibility = View.GONE
        binding.pc100Layout.visibility = View.GONE
        binding.ap20Layout.root.visibility = View.GONE
        binding.lewLayout.root.visibility = View.GONE
        binding.sp20Layout.root.visibility = View.GONE
        binding.aoj20aLayout.visibility = View.GONE
        binding.pc68bLayout.visibility = View.GONE
        binding.ad5Layout.visibility = View.GONE
        binding.pc300Layout.root.visibility = View.GONE
        binding.lemLayout.root.visibility = View.GONE
        binding.bpmLayout.visibility = View.GONE
        binding.pc60fwLayout.visibility = View.GONE
        binding.er3Layout.root.visibility = View.GONE
        binding.lepodLayout.root.visibility = View.GONE
        binding.vtm01Layout.root.visibility = View.GONE
        binding.btpLayout.root.visibility = View.GONE
        binding.r20Layout.root.visibility = View.GONE
        binding.bp3Layout.root.visibility = View.GONE
        binding.sendCmd.visibility = View.GONE
        binding.content.visibility = View.GONE
        if (v == null) return
        v.visibility = View.VISIBLE
    }

    private fun initView() {
        Constant.BluetoothConfig.currentModel[0].let {
            when (it) {
                Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1 -> {
                    setViewVisible(binding.er1Layout.root)
                    LpBleUtil.getEr1VibrateConfig(it)
                }
                Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                    setViewVisible(binding.er2Layout.root)
                    LpBleUtil.getEr1VibrateConfig(it)
                }
                Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                    setViewVisible(binding.er2Layout.root)
                    LpBleUtil.getEr2SwitcherState(it)
                }
                Bluetooth.MODEL_ER3 -> {
                    setViewVisible(binding.er3Layout.root)
                    LpBleUtil.er3GetConfig(it)
                }
                Bluetooth.MODEL_LEPOD -> {
                    setViewVisible(binding.lepodLayout.root)
                    LpBleUtil.lepodGetConfig(it)
                }
                Bluetooth.MODEL_BP2 -> {
                    setViewVisible(binding.bp2Layout.root)
                    binding.bp2Layout.bp2VolumeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2ModeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2PhyLayout.visibility = View.GONE
                    binding.bp2Layout.bp2DeleteFile.visibility = View.GONE
                    binding.bp2Layout.bp2WriteUser.visibility = View.GONE
                    LpBleUtil.bp2GetConfig(it)
                }
                Bluetooth.MODEL_BP2A -> {
                    setViewVisible(binding.bp2Layout.root)
                    binding.bp2Layout.bp2SetSwitch.visibility = View.GONE
                    binding.bp2Layout.bp2VolumeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2ModeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2PhyLayout.visibility = View.GONE
                    binding.bp2Layout.bp2DeleteFile.visibility = View.GONE
                    binding.bp2Layout.bp2WriteUser.visibility = View.GONE
                    LpBleUtil.bp2GetConfig(it)
                }
                Bluetooth.MODEL_BP2T -> {
                    setViewVisible(binding.bp2Layout.root)
                    binding.bp2Layout.bp2SetSwitch.visibility = View.GONE
                    binding.bp2Layout.bp2VolumeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2ModeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2DeleteFile.visibility = View.GONE
                    binding.bp2Layout.bp2WriteUser.visibility = View.GONE
                    LpBleUtil.bp2GetConfig(it)
                    LpBleUtil.bp2GetPhyState(it)
                }
                Bluetooth.MODEL_BP2W -> {
                    setViewVisible(binding.bp2Layout.root)
                    binding.bp2Layout.bp2VolumeLayout.visibility = View.GONE
                    binding.bp2Layout.bp2PhyLayout.visibility = View.GONE
                    binding.bp2Layout.bp2WriteUser.visibility = View.GONE
                    LpBleUtil.bp2GetConfig(it)
                }
                Bluetooth.MODEL_LE_BP2W -> {
                    setViewVisible(binding.bp2Layout.root)
                    binding.bp2Layout.bp2PhyLayout.visibility = View.GONE
                    LpBleUtil.bp2GetConfig(it)
                }
                Bluetooth.MODEL_O2RING, Bluetooth.MODEL_BABYO2,
                Bluetooth.MODEL_BBSM_S1, Bluetooth.MODEL_CHECKO2,
                Bluetooth.MODEL_O2M, Bluetooth.MODEL_SLEEPO2,
                Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
                Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
                Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYFIT,
                Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_CMRING,
                Bluetooth.MODEL_OXYU, Bluetooth.MODEL_AI_S100,
                Bluetooth.MODEL_O2M_WPS, Bluetooth.MODEL_OXYFIT_WPS,
                Bluetooth.MODEL_KIDSO2_WPS -> {
                    setViewVisible(binding.o2Layout.root)
                    settingViewModel = ViewModelProvider(this).get(OxyViewModel::class.java)
                    (settingViewModel as OxyViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as OxyViewModel).initEvent(this)
                    setSoundVibration(it)
                    LpBleUtil.getInfo(it)
                }
                Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_BBSM_S2 -> {
                    setViewVisible(binding.o2Layout.root)
                    settingViewModel = ViewModelProvider(this).get(OxyViewModel::class.java)
                    (settingViewModel as OxyViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as OxyViewModel).initEvent(this)
                    binding.o2Layout.o2S2Layout.visibility = View.VISIBLE
                    setSoundVibration(it)
                    LpBleUtil.getInfo(it)
                }
                Bluetooth.MODEL_F4_SCALE, Bluetooth.MODEL_MY_SCALE,
                Bluetooth.MODEL_F5_SCALE, Bluetooth.MODEL_F8_SCALE,
                Bluetooth.MODEL_S5_SCALE -> {
                    setViewVisible(binding.scaleLayout)
                }
                Bluetooth.MODEL_PC100 -> {
                    setViewVisible(binding.pc100Layout)
                }
                Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                    setViewVisible(binding.ap20Layout.root)
                    settingViewModel = ViewModelProvider(this).get(Ap20ViewModel::class.java)
                    (settingViewModel as Ap20ViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as Ap20ViewModel).initEvent(this)
                    LpBleUtil.ap20GetBattery(Constant.BluetoothConfig.currentModel[0])
                    LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], 0)
                    LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], 1)
                    LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], 2)
                    LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], 3)
                    LpBleUtil.ap20GetConfig(Constant.BluetoothConfig.currentModel[0], 4)
                }
                Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                    setViewVisible(binding.lewLayout.root)
                    binding.sendCmd.visibility = View.VISIBLE
                    binding.content.visibility = View.VISIBLE
                    settingViewModel = ViewModelProvider(this).get(LewViewModel::class.java)
                    (settingViewModel as LewViewModel).initView(binding, it)
                    (settingViewModel as LewViewModel).initEvent(this)
                }
                Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                    setViewVisible(binding.sp20Layout.root)
                    settingViewModel = ViewModelProvider(this).get(Sp20ViewModel::class.java)
                    (settingViewModel as Sp20ViewModel).initView(binding, it)
                    (settingViewModel as Sp20ViewModel).initEvent(this)
                    LpBleUtil.sp20GetBattery(Constant.BluetoothConfig.currentModel[0])
                    LpBleUtil.sp20GetConfig(Constant.BluetoothConfig.currentModel[0], 2)
                    LpBleUtil.sp20GetConfig(Constant.BluetoothConfig.currentModel[0], 3)
                    LpBleUtil.sp20GetConfig(Constant.BluetoothConfig.currentModel[0], 4)
                }
                Bluetooth.MODEL_AOJ20A -> {
                    setViewVisible(binding.aoj20aLayout)
                }
                Bluetooth.MODEL_PC_68B -> {
                    setViewVisible(binding.pc68bLayout)
                    settingViewModel = ViewModelProvider(this).get(Pc68bViewModel::class.java)
                    (settingViewModel as Pc68bViewModel).initView(binding, it)
                    (settingViewModel as Pc68bViewModel).initEvent(this)
                    LpBleUtil.pc68bGetConfig(it)
                }
                Bluetooth.MODEL_VTM_AD5 -> {
                    setViewVisible(binding.ad5Layout)
                }
                Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
                Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_GM_300SNT -> {
                    setViewVisible(binding.pc300Layout.root)
                    settingViewModel = ViewModelProvider(this).get(Pc300ViewModel::class.java)
                    (settingViewModel as Pc300ViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as Pc300ViewModel).initEvent(this)
                    LpBleUtil.pc300GetGlucometerType(it)
                }
                Bluetooth.MODEL_LEM -> {
                    setViewVisible(binding.lemLayout.root)
                    LpBleUtil.getInfo(it)
                }
                Bluetooth.MODEL_BPM -> {
                    setViewVisible(binding.bpmLayout)
                }
                Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
                Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
                Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
                Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
                Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
                Bluetooth.MODEL_S6W1 -> {
                    setViewVisible(binding.pc60fwLayout)
                }
                Bluetooth.MODEL_VTM01 -> {
                    setViewVisible(binding.vtm01Layout.root)
                }
                Bluetooth.MODEL_BTP -> {
                    setViewVisible(binding.btpLayout.root)
                    settingViewModel = ViewModelProvider(this).get(BtpViewModel::class.java)
                    (settingViewModel as BtpViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as BtpViewModel).initEvent(this)
                    LpBleUtil.btpGetConfig(it)
                }
                Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
                Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
                Bluetooth.MODEL_LERES -> {
                    setViewVisible(binding.r20Layout.root)
                    settingViewModel = ViewModelProvider(this).get(R20ViewModel::class.java)
                    (settingViewModel as R20ViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as R20ViewModel).initEvent(this)
                    LpBleUtil.r20GetRtState(it)
                    LpBleUtil.r20GetSystemSetting(it)
                    LpBleUtil.r20GetVentilationSetting(it)
                    LpBleUtil.r20GetMeasureSetting(it)
                    LpBleUtil.r20GetWarningSetting(it)
                }
                Bluetooth.MODEL_LP_BP3W, Bluetooth.MODEL_LP_BP3C -> {
                    setViewVisible(binding.bp3Layout.root)
                    settingViewModel = ViewModelProvider(this).get(Bp3ViewModel::class.java)
                    (settingViewModel as Bp3ViewModel).initView(requireContext(), binding, it)
                    (settingViewModel as Bp3ViewModel).initEvent(this)
                    LpBleUtil.bp3GetConfig(it)
                }
                else -> {
                    setViewVisible(null)
                }
            }
        }
        mainViewModel.curBluetooth.observe(viewLifecycleOwner) {
            deviceFactoryData.name = it?.deviceName
            deviceFactoryData.address = it?.deviceMacAddress
        }
        mainViewModel.bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.settingLayout.visibility = View.VISIBLE
            } else {
                binding.settingLayout.visibility = View.GONE
            }
        }
        mainViewModel.er1Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1_N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM1) {
                binding.er1Layout.er1Version.setText("${it.hwV}")
                binding.er1Layout.er1Sn.setText("${it.sn}")
                binding.er1Layout.er1Code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_DUOEK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM3) {
                binding.er2Layout.er2Version.setText("${it.hwV}")
                binding.er2Layout.er2Sn.setText("${it.sn}")
                binding.er2Layout.er2Code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_VTM01) {
                binding.vtm01Layout.version.setText("${it.hwV}")
                binding.vtm01Layout.sn.setText("${it.sn}")
                binding.vtm01Layout.code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BTP) {
                binding.btpLayout.version.setText("${it.hwV}")
                binding.btpLayout.sn.setText("${it.sn}")
                binding.btpLayout.code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LEPOD) {
                binding.lepodLayout.version.setText("${it.hwV}")
                binding.lepodLayout.sn.setText("${it.sn}")
                binding.lepodLayout.code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R20
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R21
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R10
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R11
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LERES) {
                binding.r20Layout.version.setText("${it.hwV}")
                binding.r20Layout.sn.setText("${it.sn}")
                binding.r20Layout.code.setText("${it.branchCode}")
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3C) {
                binding.bp3Layout.version.setText("${it.hwV}")
                binding.bp3Layout.sn.setText("${it.sn}")
                binding.bp3Layout.code.setText("${it.branchCode}")
            }
        }
        mainViewModel.er2Info.observe(viewLifecycleOwner) {
            binding.er2Layout.er2Version.setText("${it.hwVersion}")
            binding.er2Layout.er2Sn.setText("${it.serialNum}")
            binding.er2Layout.er2Code.setText("${it.branchCode}")
        }
        mainViewModel.boInfo.observe(viewLifecycleOwner) {
            binding.pc60fwCode.setText("${it.branchCode}")
        }
        mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
            binding.o2Layout.o2Version.setText("${it.hwVersion}")
            binding.o2Layout.o2Sn.setText("${it.sn}")
            binding.o2Layout.o2Code.setText("${it.branchCode}")
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
        binding.er1Layout.er1FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er1Layout.er1Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.er1Layout.er1Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.er1Layout.er1Code.text.toString())
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
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
            FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
        }
        binding.er1Layout.er1SetSound.setOnCheckedChangeListener { buttonView, isChecked ->
            val temp1 = trimStr(binding.er1Layout.er1Hr1.text.toString())
            val temp2 = trimStr(binding.er1Layout.er1Hr2.text.toString())
            if (temp1.isNullOrEmpty() || temp2.isNullOrEmpty()) {
                Toast.makeText(context, "阈值输入不能为空", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            } else {
                if (isNumber(temp1) && isNumber(temp2)) {
                    LpBleUtil.setEr1Vibrate(Constant.BluetoothConfig.currentModel[0], isChecked, temp1.toInt(), temp2.toInt())
                    cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                    binding.sendCmd.text = cmdStr
                } else {
                    Toast.makeText(context, "阈值请输入数字", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.er1Layout.er1SetHr.setOnClickListener {
            val temp1 = trimStr(binding.er1Layout.er1Hr1.text.toString())
            val temp2 = trimStr(binding.er1Layout.er1Hr2.text.toString())
            if (temp1.isNullOrEmpty() || temp2.isNullOrEmpty()) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show()
            } else {
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
        binding.er2Layout.er2FactoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.er2Layout.er2Version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.er2Layout.er2Sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.er2Layout.er2Code.text.toString())
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
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
            FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
        }
        binding.er2Layout.er2SetConfig.setOnCheckedChangeListener { buttonView, isChecked ->
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_ER2) {
                LpBleUtil.setEr2SwitcherState(Constant.BluetoothConfig.currentModel[0], isChecked)
            } else {
                LpBleUtil.setDuoekVibrate(Constant.BluetoothConfig.currentModel[0], isChecked, 0, 0, 0)
            }
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        //-------------------------bp2/bp2a/bp2t/bp2w/lp bp2w--------------------
        binding.bp2Layout.bp2SetSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            (config as Bp2Config).beepSwitch = isChecked
            LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2Layout.bp2SetPhyState.setOnClickListener {
            val tempMode = trimStr(binding.bp2Layout.bp2PhyMode.text.toString())
            if (isNumber(tempMode) && (tempMode.toInt() in 0..6)) {
                phy.mode = tempMode.toInt()
            } else {
                Toast.makeText(context, "请输入0-6之间的模式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tempIntensy = trimStr(binding.bp2Layout.bp2PhyIntensy.text.toString())
            if (isNumber(tempIntensy) && (tempIntensy.toInt() in 0..15)) {
                phy.intensy = tempIntensy.toInt()
            } else {
                Toast.makeText(context, "请输入0-15之间的强度", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tempTime = trimStr(binding.bp2Layout.bp2PhyTime.text.toString())
            if (isNumber(tempTime) && (tempTime.toInt() in 0..15)) {
                phy.remainingTime = tempTime.toInt()
            } else {
                Toast.makeText(context, "请输入0-15之间的时间", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            LpBleUtil.bp2SetPhyState(Constant.BluetoothConfig.currentModel[0], phy)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2Layout.bp2GetPhyState.setOnClickListener {
            LpBleUtil.bp2GetPhyState(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.bp2Layout.bp2DeleteFile.setOnClickListener {
            LpBleUtil.bp2DeleteFile(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("请选择", "血压测量", "心电测量", "历史回顾", "开机预备状态", "关机")
        ).apply {
            binding.bp2Layout.deviceStateSpinner.adapter = this
        }
        binding.bp2Layout.deviceStateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    Toast.makeText(context, "请选择设置状态", Toast.LENGTH_SHORT).show()
                    return
                }
                LpBleUtil.bp2SwitchState(Constant.BluetoothConfig.currentModel[0], position-1)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("关", "1", "2", "3")
        ).apply {
            binding.bp2Layout.volumeSpinner.adapter = this
        }
        binding.bp2Layout.volumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!this@SettingsFragment::config.isInitialized) return
                (config as Bp2Config).volume = position
                LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("x3模式关闭", "x3模式开启(时间间隔30s)", "x3模式开启(时间间隔60s)", "x3模式开启(时间间隔90s)", "x3模式开启(时间间隔120s)")
        ).apply {
            binding.bp2Layout.modeSpinner.adapter = this
        }
        binding.bp2Layout.modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!this@SettingsFragment::config.isInitialized) return
                (config as Bp2Config).avgMeasureMode = position
                LpBleUtil.bp2SetConfig(Constant.BluetoothConfig.currentModel[0], (config as Bp2Config))
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.bp2Layout.bp2WriteUser.setOnClickListener {
            writeUserInfo()
        }
        binding.bp3Layout.writeUser.setOnClickListener {
            writeUserInfo()
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
        //-------------------------o2-----------------------

        //-------------------------lew------------------------

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

        //-------------------------sp20-----------------------

        //----------------------aoj20a--------------------
        binding.aoj20aDeleteFile.setOnClickListener {
            LpBleUtil.aoj20aDeleteData(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.aoj20aRtData.setOnClickListener {
            LpBleUtil.aoj20aGetRtData(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //----------------------pc68b----------------------

        //----------------------ad5---------------------
        binding.ad5EnableRtData.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.enableRtData(Constant.BluetoothConfig.currentModel[0], 0, isChecked)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }

        //----------------------pc300--------------------

        // ---------------------------lem---------------------------
        binding.lemLayout.lemDeviceSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.lemDeviceSwitch(Constant.BluetoothConfig.currentModel[0], isChecked)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemLayout.lemHeatSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.lemHeatMode(Constant.BluetoothConfig.currentModel[0], isChecked)
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        binding.lemLayout.lemGetBattery.setOnClickListener {
            LpBleUtil.lemGetBattery(Constant.BluetoothConfig.currentModel[0])
            cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = cmdStr
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("活力模式", "动感模式", "捶击模式", "舒缓模式", "自动模式")
        ).apply {
            binding.lemLayout.massModeSpinner.adapter = this
        }
        binding.lemLayout.massModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.lemMassMode(Constant.BluetoothConfig.currentModel[0], position)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("15min", "10min", "5min")
        ).apply {
            binding.lemLayout.massTimeSpinner.adapter = this
        }
        binding.lemLayout.massTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.lemMassTime(Constant.BluetoothConfig.currentModel[0], position)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.lemLayout.lemSetMassLevel.setOnClickListener {
            val temp = trimStr(binding.lemLayout.lemMassLevel.text.toString())
            if (isNumber(temp)) {
                LpBleUtil.lemMassLevel(Constant.BluetoothConfig.currentModel[0], temp.toInt())
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            } else {
                Toast.makeText(context, "输入不正确，请重新输入", Toast.LENGTH_SHORT).show()
            }
        }
        // ---------------------------bpm---------------------------
        binding.bpmGetState.setOnClickListener {
            LpBleUtil.bpmGetRtState(Constant.BluetoothConfig.currentModel[0])
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
                deviceFactoryData.sn = mainViewModel._boInfo.value?.sn
                deviceFactoryData.code = code
                deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
                FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
        }
        // ---------------------------er3---------------------------
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("监护模式(带宽0.5-40HZ)", "手术模式(带宽1-20HZ)", "ST模式(带宽0.05-40HZ)")
        ).apply {
            binding.er3Layout.modeSpinner.adapter = this
            binding.lepodLayout.modeSpinner.adapter = this
        }
        binding.lepodLayout.modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.er3SetConfig(Constant.BluetoothConfig.currentModel[0], position)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // ---------------------------lepod---------------------------
        binding.lepodLayout.modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.lepodSetConfig(Constant.BluetoothConfig.currentModel[0], position)
                cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
                binding.sendCmd.text = cmdStr
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.lepodLayout.getRtParam.setOnClickListener {
            LpBleUtil.lepodGetRtParam(Constant.BluetoothConfig.currentModel[0])
        }
        binding.lepodLayout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.lepodLayout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.lepodLayout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.lepodLayout.code.text.toString())
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
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
            FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
        }
        // --------------------------vtm01--------------------------------
        binding.vtm01Layout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.vtm01Layout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                Toast.makeText(context, "硬件版本请输入A-Z字母", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.vtm01Layout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                Toast.makeText(context, "sn请输入10位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.vtm01Layout.code.text.toString())
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
            deviceFactoryData.sn = tempSn
            deviceFactoryData.code = tempCode
            deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
            FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
        }
        // --------------------------btp--------------------------------

        //---------------------------------------------------------------
        if (this@SettingsFragment::settingViewModel.isInitialized) {
            settingViewModel.toast.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
            settingViewModel.deviceFactoryData.observe(viewLifecycleOwner) {
                if (it != null) {
                    deviceFactoryData.sn = it.sn
                    deviceFactoryData.code = it.code
                    deviceFactoryData.time = DateUtil.stringFromDate(Date(System.currentTimeMillis()), DateUtil.DATE_ALL_ALL)
                    FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/device_factory_data.txt", deviceFactoryData.toString(), true)
                }
            }
        }
    }

    private fun setSoundVibration(model: Int) {
        when (model) {
            // 震动
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_OXYRING,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_SNOREO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_AI_S100 -> {
                binding.o2Layout.o2MotorText.text = "震动强度："
                binding.o2Layout.text01000.visibility = View.VISIBLE
                binding.o2Layout.text0100100.visibility = View.VISIBLE
                binding.o2Layout.text0350.visibility = View.GONE
                binding.o2Layout.text03535.visibility = View.GONE
            }
            // 声音
            Bluetooth.MODEL_OXYFIT, Bluetooth.MODEL_OXYFIT_WPS,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_KIDSO2_WPS,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_BBSM_S2,
            Bluetooth.MODEL_OXYLINK -> {
                binding.o2Layout.o2MotorText.text = "声音强度："
                binding.o2Layout.text0350.visibility = View.VISIBLE
                binding.o2Layout.text03535.visibility = View.VISIBLE
                binding.o2Layout.text01000.visibility = View.GONE
                binding.o2Layout.text0100100.visibility = View.GONE
            }
            // 震动+声音
            Bluetooth.MODEL_O2M, Bluetooth.MODEL_O2M_WPS -> {
                binding.o2Layout.o2MotorText.text = "震动强度："
                binding.o2Layout.text01000.visibility = View.VISIBLE
                binding.o2Layout.text0100100.visibility = View.VISIBLE
                binding.o2Layout.o2BuzzerLayout.visibility = View.VISIBLE
                binding.o2Layout.text0350.visibility = View.GONE
                binding.o2Layout.text03535.visibility = View.GONE
            }
            // "25010000" //PO1，震动版本，国内外
            // "25020000" //PO1B，蜂鸣版本，国内外
            // "25013001" //PO1B，蜂鸣版本，国内电商专用（新增心率阈值越限提醒）
            Bluetooth.MODEL_WEARO2 -> {
                if (mainViewModel.oxyInfo.value?.branchCode == "25020000"
                    || mainViewModel.oxyInfo.value?.branchCode == "25013001") {
                    binding.o2Layout.o2MotorText.text = "声音强度："
                    binding.o2Layout.text0350.visibility = View.VISIBLE
                    binding.o2Layout.text03535.visibility = View.VISIBLE
                    binding.o2Layout.text01000.visibility = View.GONE
                    binding.o2Layout.text0100100.visibility = View.GONE
                } else {
                    binding.o2Layout.o2MotorText.text = "震动强度："
                    binding.o2Layout.text01000.visibility = View.VISIBLE
                    binding.o2Layout.text0100100.visibility = View.VISIBLE
                    binding.o2Layout.text0350.visibility = View.GONE
                    binding.o2Layout.text03535.visibility = View.GONE
                }
            }
        }
    }

    private fun writeUserInfo() {
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
        userList.userList.add(userInfo1)
        userList.userList.add(userInfo2)
        userList.userList.add(userInfo3)
        userList.userList.add(userInfo4)

        FileUtil.saveFile(context, userList.getDataBytes(), "userlist.dat")

        LepuBleLog.d("test icon1 == " + bytesToHex(icon1.getDataBytes()))
        LepuBleLog.d("test icon1.getDataBytes().size == " + icon1.getDataBytes().size)
        LepuBleLog.d("test userInfo1 == " + bytesToHex(userInfo1.getDataBytes()))
        LepuBleLog.d("test userInfo1.getDataBytes().size == " + userInfo1.getDataBytes().size)
        LepuBleLog.d("test userList == " + bytesToHex(userList.getDataBytes()))
        LepuBleLog.d("test userList.getDataBytes().size == " + userList.getDataBytes().size)

        LpBleUtil.bp2WriteUserList(Constant.BluetoothConfig.currentModel[0], userList)
        cmdStr = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        binding.sendCmd.text = cmdStr
        val crc = CrcUtil.calCRC32(userList.getDataBytes())
        binding.deviceInfo.text = "用户信息计算出CRC: $crc"
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
                Toast.makeText(context, context?.getString(R.string.burn_info_success), Toast.LENGTH_SHORT).show()
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
                    binding.er2Layout.er2SetConfig.isChecked = config.switcher
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
                    this.config = config
                    binding.er1Layout.er1SetSound.isChecked = config.switcher
                    binding.er1Layout.er1Hr1.setText("${config.hr1}")
                    binding.er1Layout.er1Hr2.setText("${config.hr2}")
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
                Toast.makeText(context, context?.getString(R.string.burn_info_success), Toast.LENGTH_SHORT).show()
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
                this.config = config
                binding.er2Layout.er2SetConfig.isChecked = config.switcher
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
                val config = it.data as Bp2Config
                this.config = config
                binding.content.text = "$config"
                binding.bp2Layout.bp2SetSwitch.isChecked = config.beepSwitch
                Toast.makeText(context, "获取参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
            .observe(this) {
                val data = it.data as Bp2BleRtState
                setReceiveCmd(data.bytes)
                binding.content.text = "$data"
                state = data.status
//                Toast.makeText(context, "获取设备状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "设置设备状态$data", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyState)
            .observe(this) {
                val phy = it.data as Bp2BlePhyState
                this.phy = phy
                setReceiveCmd(phy.bytes)
                binding.content.text = "$phy"
                binding.bp2Layout.bp2PhyLeadStatus.text = "电极导联状态：${phy.leadOff}"
                binding.bp2Layout.bp2PhyMode.setText("${phy.mode}")
                binding.bp2Layout.bp2PhyIntensy.setText("${phy.intensy}")
                binding.bp2Layout.bp2PhyTime.setText("${phy.remainingTime}")
                Toast.makeText(context, "获取理疗状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError)
            .observe(this) {
                Toast.makeText(context, "获取失败，请先进入理疗模式", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetPhyState)
            .observe(this) {
                val phy = it.data as Bp2BlePhyState
                this.phy = phy
                setReceiveCmd(phy.bytes)
                binding.content.text = "$phy"
                binding.bp2Layout.bp2PhyLeadStatus.text = "电极导联状态：${phy.leadOff}"
                binding.bp2Layout.bp2PhyMode.setText("${phy.mode}")
                binding.bp2Layout.bp2PhyIntensy.setText("${phy.intensy}")
                binding.bp2Layout.bp2PhyTime.setText("${phy.remainingTime}")
                Toast.makeText(context, "设置理疗状态成功", Toast.LENGTH_SHORT).show()
            }
        //------------------------------bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wDeleteFile)
            .observe(this) {
                Toast.makeText(context, "删除文件成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSwitchState)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "设置主机状态$data", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig)
            .observe(this) {
                val config = it.data as Bp2Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = "$config"
                binding.bp2Layout.bp2SetSwitch.isChecked = config.beepSwitch
                binding.bp2Layout.modeSpinner.setSelection(config.avgMeasureMode)
                Toast.makeText(context, "获取参数成功", Toast.LENGTH_SHORT).show()
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
                alertDialog?.dismiss()
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                bp2wAdapter.setNewInstance(data.wifiList)
                bp2wAdapter.notifyDataSetChanged()
                binding.content.text = "$data"
                Toast.makeText(context, "获取WiFi列表成功", Toast.LENGTH_SHORT).show()
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
                binding.content.text = "$data"
                if (data.wifi.ssid.isNotEmpty()) {
                    binding.deviceInfo.text = "WiFi：${data.wifi.ssid}\n" +
                            "${context?.getString(R.string.wifi_password)}${data.wifi.pwd}\n" +
                            "WiFi连接状态：${data.wifi.state}\n" +
                            "(0:断开 1:连接中 2:已连接 0xff:密码错误 0xfd:找不到SSID)\n" +
                            "${context?.getString(R.string.server_address)}${data.server.addr}\n" +
                            "${context?.getString(R.string.server_port)}${data.server.port}\n" +
                            "服务器连接状态：${data.server.state}\n" +
                            "(0:断开 1:连接中 2:已连接 0xff:服务器无法连接)"
                } else {
//                    Toast.makeText(context, "尚未配置WiFi信息", Toast.LENGTH_SHORT).show()
                }
            }
        //------------------------------lp bp2w-------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncUtcTime)
            .observe(this) {
                Toast.makeText(context, "同步 UTC 时间成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile)
            .observe(this) {
                Toast.makeText(context, "删除文件成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig)
            .observe(this) {
                LpBleUtil.bp2GetConfig(it.model)
                Toast.makeText(context, "设置参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState)
            .observe(this) {
                val data = it.data as Boolean
                binding.content.text = "$data"
                Toast.makeText(context, "设置主机状态$data", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetConfig)
            .observe(this) {
                val config = it.data as Bp2Config
                this.config = config
                setReceiveCmd(config.bytes)
                binding.content.text = "$config"
                binding.bp2Layout.bp2SetSwitch.isChecked = config.beepSwitch
                binding.bp2Layout.volumeSpinner.setSelection(config.volume)
                binding.bp2Layout.modeSpinner.setSelection(config.avgMeasureMode)
                Toast.makeText(context, "获取参数成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiScanning)
            .observe(this) {
                binding.content.text = "设备正在扫描wifi"
                LpBleUtil.bp2GetWifiDevice(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiDevice)
            .observe(this) {
                alertDialog?.dismiss()
                val data = it.data as Bp2WifiDevice
                setReceiveCmd(data.bytes)
                bp2wAdapter.setNewInstance(data.wifiList)
                bp2wAdapter.notifyDataSetChanged()
                binding.content.text = "$data"
                Toast.makeText(context, "获取WiFi列表成功", Toast.LENGTH_SHORT).show()
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
                binding.content.text = "$data"
                if (data.wifi.ssid.isNotEmpty()) {
                    binding.deviceInfo.text = "${context?.getString(R.string.wifi)}${data.wifi.ssid}\n" +
                            "${context?.getString(R.string.wifi_password)}${data.wifi.pwd}\n" +
                            "WiFi连接状态：${data.wifi.state}\n" +
                            "(0:断开 1:连接中 2:已连接 0xff:密码错误 0xfd:找不到SSID)\n" +
                            "${context?.getString(R.string.server_address)}${data.server.addr}\n" +
                            "${context?.getString(R.string.server_port)}${data.server.port}\n" +
                            "服务器连接状态：${data.server.state}\n" +
                            "(0:断开 1:连接中 2:已连接 0xff:服务器无法连接)"
                } else {
//                    Toast.makeText(context, "尚未配置WiFi信息", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc)
            .observe(this) {
                val data = it.data as FileListCrc
                binding.content.text = "$data"
                Toast.makeText(context, "le bp2w 获取文件列表校验成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WritingFileProgress)
            .observe(this) {
                val data = it.data as Bp2FilePart
                Toast.makeText(context, "le bp2w 写文件列表进度：${(data.percent * 100).toInt()} %", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileComplete)
            .observe(this) {
                val data = it.data as FileListCrc
                Toast.makeText(context, "le bp2w 写文件完成 crc：${data.crc}", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError)
            .observe(this) {
                val data = it.data as String
                Toast.makeText(context, "le bp2w 写文件错误 filename：$data", Toast.LENGTH_SHORT).show()
            }
        //------------------------------o2/babyO2-----------------------------

        //------------------------------ap20-------------------------------------

        //------------------------------sp20-------------------------------------

        //------------------------------lew-------------------------------------

        //---------------------------aoj20a-------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observe(this) {
                Toast.makeText(context, "aoj20a 测量完成", Toast.LENGTH_SHORT).show()
                val data = it.data as Aoj20aBleResponse.TempRtData
                setReceiveCmd(data.bytes)
                binding.content.text = "$data"
                binding.deviceInfo.text = "测温模式：${data.modeMsg}\n温度：${data.temp} ℃"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData)
            .observe(this) {
                Toast.makeText(context, "aoj20a 删除成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observe(this) {
                val data = it.data as Aoj20aBleResponse.ErrorMsg
                binding.content.text = "$data"
                binding.deviceInfo.text = "错误结果：${data.codeMsg}"
            }

        //---------------------pc68b-------------------

        // --------------------------LEM--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo)
            .observe(this) {
                val data = it.data as LemBleResponse.DeviceInfo
                binding.content.text = "$data"
                binding.lemLayout.lemGetBattery.text = "电量${data.battery}%"
                binding.lemLayout.lemHeatSwitch.text = "加热模式${if (data.heatMode) "开" else "关" }"
                binding.lemLayout.massModeSpinner.setSelection(data.massageMode)
                binding.lemLayout.lemMassLevel.setText("${data.massageLevel}")
                binding.lemLayout.massTimeSpinner.setSelection(data.massageTime)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemBattery)
            .observe(this) {
                val data = it.data as Int
                binding.lemLayout.lemGetBattery.text = "电量$data%"
                Toast.makeText(context, "获取电量成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetHeatMode)
            .observe(this) {
                val data = it.data as Boolean
                binding.lemLayout.lemHeatSwitch.text = "加热模式${if (data) "开" else "关" }"
                Toast.makeText(context, "设置加热模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageMode)
            .observe(this) {
                val data = it.data as Int
                binding.lemLayout.massModeSpinner.setSelection(data)
                Toast.makeText(context, "设置按摩模式成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageLevel)
            .observe(this) {
                val data = it.data as Int
                binding.lemLayout.lemMassLevel.setText("$data")
                Toast.makeText(context, "设置按摩力度成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemSetMassageTime)
            .observe(this) {
                val data = it.data as Int
                binding.lemLayout.massTimeSpinner.setSelection(data)
                Toast.makeText(context, "设置按摩时间成功", Toast.LENGTH_SHORT).show()
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
        //------------------------er3-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3SetConfig)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    Toast.makeText(context, "设置mode成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "设置mode失败", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.er3GetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3GetConfig)
            .observe(this) {
                val data = it.data as Int
                binding.er3Layout.modeSpinner.setSelection(data)
                Toast.makeText(context, "获取mode成功", Toast.LENGTH_SHORT).show()
            }
        //------------------------lepod-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodBurnFactoryInfo)
            .observe(this) {

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetConfig)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    Toast.makeText(context, "设置mode成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "设置mode失败", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.lepodGetConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodGetConfig)
            .observe(this) {
                val data = it.data as Int
                binding.lepodLayout.modeSpinner.setSelection(data)
                Toast.makeText(context, "获取mode成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodRtParam)
            .observe(this) {
                val data = it.data as LepodBleResponse.RtParam
                binding.lepodLayout.deviceInfo.text = "心率：${data.hr} bpm\n" +
                        "体温：${data.temp} ℃\n" +
                        "血氧：${data.spo2} %\n" +
                        "pi：${data.pi} %\n" +
                        "脉率：${data.pr}\n" +
                        "呼吸率：${data.respRate}\n" +
                        "电池状态${data.batteryStatus}：${
                            when (data.batteryStatus) {
                                0 -> context?.getString(R.string.no_charge)
                                1 -> context?.getString(R.string.charging)
                                2 -> context?.getString(R.string.full)
                                3 -> context?.getString(R.string.low_battery)
                                else -> ""
                            }
                        }\n" +
                        "心电导联线状态：${data.isInsertEcgLeadWire}\n" +
                        "血氧状态${data.oxyStatus}：${
                            when (data.oxyStatus) {
                                0 -> "未接入血氧"
                                1 -> "血氧状态正常"
                                2 -> "血氧手指脱落"
                                3 -> "探头故障"
                                else -> ""
                            }
                        }\n" +
                        "体温状态：${data.isInsertTemp}\n" +
                        "${context?.getString(R.string.measure_state)}${data.measureStatus}：${
                            when (data.measureStatus) {
                                0 -> "空闲"
                                1 -> "检测导联"
                                2 -> "准备状态"
                                3 -> "正式测量"
                                else -> ""
                            }
                        }\n" +
                        "${context?.getString(R.string.duration)}${data.recordTime}\n" +
                        "${context?.getString(R.string.start_time)}${data.year}-${data.month}-${data.day} ${data.hour}:${data.minute}:${data.second}\n" +
                        "导联类型${data.leadType}：${
                            when (data.leadType) {
                                0 -> "LEAD_12，12导"
                                1 -> "LEAD_6，6导"
                                2 -> "LEAD_5，5导"
                                3 -> "LEAD_3，3导"
                                4 -> "LEAD_3_TEMP，3导带体温"
                                5 -> "LEAD_3_LEG，3导胸贴"
                                6 -> "LEAD_5_LEG，5导胸贴"
                                7 -> "LEAD_6_LEG，6导胸贴"
                                0xFF -> "LEAD_NONSUP，不支持的导联"
                                else -> "UNKNOWN，未知导联"
                            }
                        }\n" +
                        "RA导联脱落：${data.isLeadOffRA}\n" +
                        "RL导联脱落：${data.isLeadOffRL}\n" +
                        "LA导联脱落：${data.isLeadOffLA}\n" +
                        "LL导联脱落：${data.isLeadOffLL}\n" +
                        "V1导联脱落：${data.isLeadOffV1}\n" +
                        "V2导联脱落：${data.isLeadOffV2}\n" +
                        "V3导联脱落：${data.isLeadOffV3}\n" +
                        "V4导联脱落：${data.isLeadOffV4}\n" +
                        "V5导联脱落：${data.isLeadOffV5}\n" +
                        "V6导联脱落：${data.isLeadOffV6}"
                Toast.makeText(context, "获取实时参数成功", Toast.LENGTH_SHORT).show()
            }
        // --------------------------vtm01--------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01BurnFactoryInfo)
            .observe(this) {
                val data = it.data as Boolean
                if (data) {
                    Toast.makeText(context, context?.getString(R.string.burn_info_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "烧录失败", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.getInfo(it.model)
            }
        // --------------------------btp--------------------

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
                binding.content.text = "压力值 ：${data.pressure}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1ErrorResult)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.ErrorResult
                binding.content.text = "测量出错 类型：" + data.type + " 结果：" + data.result
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetFileListComplete)
            .observe(this) {
                val bpw1FileList = it.data as Bpw1BleResponse.Bpw1FileList
                binding.content.text = "$bpw1FileList"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1MeasureResult)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.BpData
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1DeviceInfo)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.DeviceInfo
                binding.content.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPW1.EventBpw1GetMeasureTime)
            .observe(this) {
                val data = it.data as Bpw1BleResponse.MeasureTime
                binding.content.text = "$data"
            }
    }

}