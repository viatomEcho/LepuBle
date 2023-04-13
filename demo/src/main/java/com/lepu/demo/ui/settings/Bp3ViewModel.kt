package com.lepu.demo.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Bp3BleCmd
import com.lepu.blepro.ble.cmd.LpBleCmd
import com.lepu.blepro.ble.cmd.ResponseError
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.Bp2Config
import com.lepu.blepro.ble.data.LeBp2wUserList
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DeviceFactoryData
import com.lepu.demo.databinding.FragmentSettingsBinding
import com.lepu.demo.util.StringUtil

class Bp3ViewModel : SettingViewModel() {

    private lateinit var binding: FragmentSettingsBinding

    fun initView(context: Context, binding: FragmentSettingsBinding, model: Int) {
        this.binding = binding
        binding.bp3Layout.factoryConfig.setOnClickListener {
            val config = FactoryConfig()
            var enableVersion = true
            val tempVersion = binding.bp3Layout.version.text
            if (tempVersion.isNullOrEmpty()) {
                enableVersion = false
            } else if (tempVersion.length == 1 && StringUtil.isBigLetter(tempVersion.toString())) {
                config.setHwVersion(tempVersion.first())
            } else {
                _toast.value = "硬件版本请输入A-Z字母"
                return@setOnClickListener
            }
            var enableSn = true
            val tempSn = trimStr(binding.bp3Layout.sn.text.toString())
            if (tempSn.isNullOrEmpty()) {
                enableSn = false
            } else if (tempSn.length == 10) {
                config.setSnCode(tempSn)
            } else {
                _toast.value = "sn请输入10位"
                return@setOnClickListener
            }
            var enableCode = true
            val tempCode = trimStr(binding.bp3Layout.code.text.toString())
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
        binding.bp3Layout.bpUnit.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.mmhg) {
                LpBleUtil.bp3SwitchBpUnit(model, 0)
            } else {
                LpBleUtil.bp3SwitchBpUnit(model, 1)
            }
        }
        ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayListOf("BP2 6621模拟器", "BP2 6621人体", "BP2 52832人体", "一体机")).apply {
            binding.bp3Layout.testMode.adapter = this
        }
        binding.bp3Layout.testMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LpBleUtil.bp3SwitchTestMode(model, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.bp3Layout.switchValve.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.bp3SwitchValve(model, isChecked)
        }
        binding.bp3Layout.switchWifi4g.setOnCheckedChangeListener { buttonView, isChecked ->
            LpBleUtil.bp3SwitchWifi4g(model, isChecked)
        }
        binding.bp3Layout.getRtPressure.setOnClickListener {
            LpBleUtil.bp3GetRtPressure(model, 1)
        }
        binding.bp3Layout.calibrationZero.setOnClickListener {
            LpBleUtil.bp3CalibrationZero(model)
        }
        binding.bp3Layout.calibrationSlope.setOnClickListener {
            val data = trimStr(binding.bp3Layout.calibrationSlopeText.text.toString())
            if (data.isNullOrEmpty()) {
                Toast.makeText(context, "请输入校准压力值！", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.bp3CalibrationSlope(model, data.toInt())
            }
        }
        binding.bp3Layout.pressureTest.setOnClickListener {
            val data = trimStr(binding.bp3Layout.pressureTestText.text.toString())
            if (data.isNullOrEmpty()) {
                Toast.makeText(context, "请输入目标压力值！", Toast.LENGTH_SHORT).show()
            } else {
                LpBleUtil.bp3CalibrationSlope(model, data.toInt())
            }
        }
        binding.bp3Layout.readUser.setOnClickListener {
            LpBleUtil.readFile("", "user.list", model)
        }
    }
    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3BurnFactoryInfo)
            .observe(owner) {
                _toast.value = "烧录成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetConfig)
            .observe(owner) {
                val config = it.data as Bp2Config
                binding.bp3Layout.calibrationSlopeText.setText("${config.slopePressure}")
                binding.bp3Layout.pressureTestText.setText("${config.bpTestTargetPressure}")
                binding.bp3Layout.switchWifi4g.isChecked = config.wifi4gSwitch
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchValve)
            .observe(owner) {
                _toast.value = "气阀开关设置成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchTestMode)
            .observe(owner) {
                _toast.value = "切换测试模式成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SwitchBpUnit)
            .observe(owner) {
                _toast.value = "切换血压单位成功"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtPressure)
            .observe(owner) {
                val data = it.data as Int
                binding.deviceInfo.text = "实时压力值：$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3CalibrationZero)
            .observe(owner) {
                val data = it.data as Int
                binding.deviceInfo.text = "校零adc值：$data\n780140<=zero<=1360372"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3CalibrationSlope)
            .observe(owner) {
                val data = it.data as Int
                binding.deviceInfo.text = "校准斜率值：$data\n5368<=slope<=7000 136.3LSB/mmHg-170.4LSB/mmHg"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WritingFileProgress)
            .observe(owner) {
                val data = it.data as Int
                binding.deviceInfo.text = binding.deviceInfo.text.toString() + "\n写文件进度：$data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WriteFileComplete)
            .observe(owner) {
                val crc = it.data as Int
                _toast.value = "写文件成功"
                binding.deviceInfo.text = binding.deviceInfo.text.toString() + "\n接收到设备返回CRC: $crc"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileError)
            .observe(owner) {
                _toast.value = "读文件错误"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadingFileProgress)
            .observe(owner) {
                val data = it.data as Int
                binding.deviceInfo.text = "读文件进度：$data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileComplete)
            .observe(owner) {
                val data = it.data as LeBp2wUserList
                for (user in data.userList) {
                    binding.deviceInfo.text = binding.deviceInfo.text.toString() + "\n用户名: ${user.fName}${user.name}"
                }
                _toast.value = "读文件成功"
            }
        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError)
            .observe(owner) {
                when (it.cmd) {
                    Bp3BleCmd.CALIBRATION_ZERO -> {
                        _toast.value = "校零失败"
                        binding.deviceInfo.text = "校零adc值：780140<=zero<=1360372\n校零失败"
                    }
                    Bp3BleCmd.CALIBRATION_SLOPE -> {
                        _toast.value = "校准失败"
                        binding.deviceInfo.text = "校准斜率值：5368<=slope<=7000 136.3LSB/mmHg-170.4LSB/mmHg\n校准失败"
                    }
                    LpBleCmd.WRITE_FILE_START, LpBleCmd.WRITE_FILE_DATA, LpBleCmd.WRITE_FILE_END -> {
                        _toast.value = "写文件失败"
                    }
                }
            }
    }

}