package com.lepu.demo.ui.notifications

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.*
import com.lepu.demo.ble.BpAdapter
import com.lepu.demo.ble.EcgAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.OxyAdapter
import com.lepu.demo.cofig.Constant
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.ecgData
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.oxyData
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.bpData
import com.lepu.demo.data.BpData
import com.lepu.demo.data.EcgData
import com.lepu.demo.data.OxyData
import com.lepu.demo.databinding.FragmentInfoBinding
import com.lepu.demo.util.DataConvert


class InfoFragment : Fragment(R.layout.fragment_info){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val binding: FragmentInfoBinding by binding()

    private var fileNames: ArrayList<String> = arrayListOf()

    private var curFileName = ""
    private var readFileProcess = ""
    private var fileCount = 0

    private var fileType = 0

    private var isReceive = false

    private var pc68bList = mutableListOf<Pc68bBleResponse.Record>()

    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    private lateinit var oxyAdapter: OxyAdapter
    var oxyList: ArrayList<OxyData> = arrayListOf()

    private lateinit var bpAdapter: BpAdapter
    var bpList: ArrayList<BpData> = arrayListOf()

    var mAlertDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }
    private fun initView(){

        mainViewModel.bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.infoLayout.visibility = View.VISIBLE
            } else {
                binding.infoLayout.visibility = View.GONE
            }
        }

        mAlertDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
//            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .create()

        mainViewModel.downloadTip.observe(viewLifecycleOwner) {
            mAlertDialog?.setMessage("正在处理，请稍等... $it")
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.ecgRcv.layoutManager = this
        }
        ecgAdapter = EcgAdapter(R.layout.device_item, null).apply {
            binding.ecgRcv.adapter = this
        }
        ecgAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(context, WaveEcgActivity::class.java)
                    intent.putExtra("model", Constant.BluetoothConfig.currentModel[0])

                    ecgData.recordingTime = it.recordingTime
                    ecgData.data = it.data
                    ecgData.shortData = it.shortData

                    startActivity(intent)
                }
            }
        }
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.oxyRcv.layoutManager = this
        }
        oxyAdapter = OxyAdapter(R.layout.device_item, null).apply {
            binding.oxyRcv.adapter = this
        }
        oxyAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as OxyData).let {
                    val intent = Intent(context, OxyDataActivity::class.java)
                    oxyData.fileName = it.fileName
                    oxyData.oxyBleFile = it.oxyBleFile
                    startActivity(intent)
                }
            }
        }
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.bpRcv.layoutManager = this
        }
        bpAdapter = BpAdapter(R.layout.device_item, null).apply {
            binding.bpRcv.adapter = this
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

        mainViewModel.er1Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1_N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_DUOEK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM3
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LE_BP2W) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.er2Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_ER2) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.fwVersion}\nsn：${it.serialNum}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.pc80bInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC80B
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC80B_BLE) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}"
            }
        }
        mainViewModel.bp2Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2A
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2T) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fmV}\nsn：${it.sn}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.bpmInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BPM) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "版本：${it.getFwVersion()}"
            }
        }
        mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SNOREO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_WEARO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYLINK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYFIT
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_AI_S100) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}\nfileList：${it.fileList}"
            }
        }
        mainViewModel.pc100Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC100) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}\nsn：${it.sn}"
            }
        }
        mainViewModel.boInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC60FW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC66B
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYSMART
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_POD_1W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_POD2B
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC_60NW_1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC_60B
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10AW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10AW1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10BW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10BW1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_20
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_20AW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_20B
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC_60NW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_S5W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_S6W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_S6W1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_S7W
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_S7BW) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}\nsn：${it.sn}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.aoj20aInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_AOJ20A) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "$it"
            }
        }
        mainViewModel.checkmePodInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECK_POD) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.pulsebitInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PULSEBITEX
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM4
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKME) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}"
            }
        }
        mainViewModel.checkmeLeInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKME_LE) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}"
            }
        }
        mainViewModel.pc300Info.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300_BLE) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}"
            }
        }
        mainViewModel.lemInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LEM) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "$it"
            }
        }
        mainViewModel.lewInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LEW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_W12C) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}"
            }
        }
        mainViewModel.biolandInfo.observe(viewLifecycleOwner) {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BIOLAND_BGM) {
                binding.info.text = "$it"
                binding.deviceInfo.text = "版本：${it.version}\n电量：${it.battery} %\nsn：${it.sn}"
            }
        }

        // 公共方法测试
        // 获取设备信息
        binding.getInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
            fileCount = 0
            fileNames.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            oxyList.clear()
            oxyAdapter.setNewInstance(oxyList)
            oxyAdapter.notifyDataSetChanged()
            bpList.clear()
            bpAdapter.setNewInstance(bpList)
            bpAdapter.notifyDataSetChanged()
        }
        // 获取文件列表
        binding.getFileList.setOnClickListener {
            fileCount = 0
            fileNames.clear()
            pc68bList.clear()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            oxyList.clear()
            oxyAdapter.setNewInstance(oxyList)
            oxyAdapter.notifyDataSetChanged()
            bpList.clear()
            bpAdapter.setNewInstance(bpList)
            bpAdapter.notifyDataSetChanged()

            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CMRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SNOREO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_WEARO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYLINK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYFIT
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_AI_S100
            ) {
                LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LEW
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_W12C) {
                fileType++
                if (fileType > LewBleCmd.ListType.SLEEP) {
                    fileType = LewBleCmd.ListType.SPORT
                }
                LpBleUtil.lewGetFileList(Constant.BluetoothConfig.currentModel[0], fileType, 0)
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LE_BP2W) {
                fileType++
                if (fileType > LeBp2wBleCmd.FileType.ECG_TYPE) {
                    fileType = LeBp2wBleCmd.FileType.USER_TYPE
                }
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0], fileType)
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKME_LE) {
                fileType++
                if (fileType > CheckmeLeBleCmd.ListType.TEMP_TYPE) {
                    fileType = CheckmeLeBleCmd.ListType.ECG_TYPE
                }
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0], fileType)
            } else {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
            }
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }
        // 读文件
        binding.readFile.setOnClickListener {
            readFileProcess = ""
            mAlertDialog?.show()
            ecgList.clear()
            ecgAdapter.setNewInstance(ecgList)
            ecgAdapter.notifyDataSetChanged()
            oxyList.clear()
            oxyAdapter.setNewInstance(oxyList)
            oxyAdapter.notifyDataSetChanged()
            bpList.clear()
            bpAdapter.setNewInstance(bpList)
            bpAdapter.notifyDataSetChanged()
            readFile()
        }
        // 暂停读取文件
        binding.pauseRf.setOnClickListener {
//            startActivity(Intent(context, WaveEcgActivity::class.java))
        }
        // 继续读取文件
        binding.continueRf.setOnClickListener {

        }
        //  dfu升级
        binding.upgrade.setOnClickListener {
            val intent = Intent(context, UpdateActivity::class.java)
            intent.putExtra("macAddr", mainViewModel._curBluetooth.value?.deviceMacAddress)
            intent.putExtra("bleName", mainViewModel._curBluetooth.value?.deviceName)
            startActivity(intent)
        }
        binding.scanCode.setOnClickListener {
            startActivity(Intent(context, ScanCodeActivity::class.java))
        }

        // 复位
        binding.reset.setOnClickListener {
            LpBleUtil.reset(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }
        // 恢复出厂设置
        binding.factory.setOnClickListener {
            LpBleUtil.factoryReset(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }
        // 恢复出厂状态
        binding.factoryAll.setOnClickListener {
            LpBleUtil.factoryResetAll(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }

        binding.getMtu.setOnClickListener {
            binding.sendCmd.text = "mtu : ${LpBleUtil.getBleMtu(Constant.BluetoothConfig.currentModel[0])}"
        }
        binding.setMtu.setOnClickListener {
            var mtu = 0
            if (binding.mtuText.text.toString() != "")
                mtu = binding.mtuText.text.toString().toInt()
            LpBleUtil.setBleMtu(Constant.BluetoothConfig.currentModel[0], mtu)
        }

    }

    private fun initEvent(){
        LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseContent)
            .observe(this) {
                binding.responseCmd.text = "receive : ${bytesToHex(it)}"
            }
        //--------------------------------er1 duoek-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList)
            .observe(this) { event ->
                (event.data as String).let {
                    binding.info.text = it
                    for (fileName in it.split(",")) {
//                        if (fileName.contains("R")) {
                        if (fileName.isNotEmpty()) {
                            fileNames.add(fileName)
                        }
//                        }
                    }
                    binding.deviceInfo.text = fileNames.toString()
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess $curFileName 读取进度: ${(it / 10)} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${it.div(10)} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete)
            .observe(this) { event ->
                (event.data as Er1BleResponse.Er1File).let {
                    if (event.model == Bluetooth.MODEL_ER1_N) {
                        val data = VBeatHrFile(it.content)
                        binding.info.text = "$data"
                        binding.deviceInfo.text = "$data"
                    } else {
                        if (it.fileName.contains("R")) {
                            val data = Er1EcgFile(it.content)
                            binding.info.text = "$data"
                            readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                            val temp = EcgData()
                            temp.recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                            temp.fileName = it.fileName
                            temp.data = data.waveData
                            temp.shortData = DataConvert.getEr1ShortArray(data.waveData)
                            temp.duration = data.recordingTime
                            ecgList.add(temp)
                            ecgAdapter.setNewInstance(ecgList)
                            ecgAdapter.notifyDataSetChanged()
                        } else {
                            val data = Er2AnalysisFile(it.content)
                            binding.info.text = "$data"
                            readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        }
                    }
                    setReceiveCmd(it.content)
                    binding.process.text = readFileProcess
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Reset)
            .observe(this) {
                Toast.makeText(context, "复位成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory)
            .observe(this) {
                Toast.makeText(context, "恢复出厂设置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactoryAll)
            .observe(this) {
                Toast.makeText(context, "恢复生产状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        //--------------------------------er2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList)
            .observe(this) { event ->
                (event.data as Er2FileList).let {
                    binding.info.text = "$it"
                    for (fileName in it.fileNames) {
                        if (fileName.contains("R")) {
                            fileNames.add(fileName)
                        }
                    }
                    binding.deviceInfo.text = fileNames.toString()
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.div(10))} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${it.div(10)} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
            .observe(this) { event ->
                (event.data as Er2File).let {
                    if (it.fileName.contains("R")) {
                        val data = Er1EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = EcgData()
                        temp.recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                        temp.fileName = it.fileName
                        temp.data = data.waveData
                        temp.shortData = DataConvert.getEr1ShortArray(data.waveData)
                        temp.duration = data.recordingTime
                        ecgList.add(temp)
                        ecgAdapter.setNewInstance(ecgList)
                        ecgAdapter.notifyDataSetChanged()
                    } else {
                        val data = Er2AnalysisFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                    }
                    binding.process.text = readFileProcess
                    setReceiveCmd(it.content)
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Reset)
            .observe(this) {
                Toast.makeText(context, "复位成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset)
            .observe(this) {
                Toast.makeText(context, "恢复出厂设置成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryResetAll)
            .observe(this) {
                Toast.makeText(context, "恢复生产状态成功", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        //--------------------------------lew-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList)
            .observe(this) {
                val data = it.data as LewBleResponse.FileList
                when (data.type) {
                    LewBleCmd.ListType.SPORT -> {
                        val list = SportList(data.content)
                        binding.info.text = "$list"
                    }
                    LewBleCmd.ListType.ECG -> {
                        val list = EcgList(data.content)
                        binding.info.text = "$list"
                        for (item in list.items) {
                            fileNames.add(item.name)
                        }
                    }
                    LewBleCmd.ListType.HR -> {
                        val list = HrList(data.content)
                        binding.info.text = "$list"
                    }
                    LewBleCmd.ListType.OXY -> {
                        val list = OxyList(data.content)
                        binding.info.text = "$list"
                    }
                }
                Toast.makeText(context, "获取列表成功 ${data.type}", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.div(10))} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
            .observe(this) { event ->
                (event.data as LewBleResponse.EcgFile).let {
                    setReceiveCmd(it.content)
                    val file = EcgFile(it.content)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $file \n"
                    binding.process.text = readFileProcess
                    Toast.makeText(context, "获取心电文件$curFileName 成功", Toast.LENGTH_SHORT).show()
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        //--------------------------------bp2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observe(this) { event ->
                (event.data as KtBleFileList).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = "$it"
                    for (fileName in it.fileNameList) {
                        fileNames.add(fileName)
                    }
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.deviceInfo.text = fileNames.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observe(this) { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.percent.times(100)).toInt()} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${it.percent.times(100)} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observe(this) { event ->
                (event.data as Bp2BleFile).let {
                    if (it.type == 2) {
                        val data = Bp2EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = EcgData()
                        temp.recordingTime = data.measureTime.toLong()
                        temp.fileName = it.name
                        temp.data = data.waveData
                        temp.shortData = DataConvert.getBp2ShortArray(data.waveData)
                        temp.duration = data.recordingTime
                        ecgList.add(temp)
                        ecgAdapter.setNewInstance(ecgList)
                        ecgAdapter.notifyDataSetChanged()
                    } else if (it.type == 1) {
                        val data = Bp2BpFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = BpData()
                        temp.fileName = it.name
                        temp.sys = data.sys
                        temp.dia = data.dia
                        temp.pr = data.pr
                        temp.mean = data.mean
                        bpList.add(temp)
                        bpAdapter.setNewInstance(bpList)
                        bpAdapter.notifyDataSetChanged()
                    }
                    binding.process.text = readFileProcess
                    setReceiveCmd(it.content)
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        //--------------------------------bp2w-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList)
            .observe(this) { event ->
                (event.data as KtBleFileList).let {
                    setReceiveCmd(it.bytes)
                    for (fileName in it.fileNameList) {
                        fileNames.add(fileName)
                    }
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.info.text = "$it"
                    binding.deviceInfo.text = fileNames.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress)
            .observe(this) { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.percent.times(100)).toInt()} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${it.percent.times(100)} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete)
            .observe(this) { event ->
                (event.data as Bp2BleFile).let {
                    if (it.type == 2) {
                        val data = Bp2EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = EcgData()
                        temp.recordingTime = data.measureTime.toLong()
                        temp.fileName = it.name
                        temp.data = data.waveData
                        temp.shortData = DataConvert.getBp2ShortArray(data.waveData)
                        temp.duration = data.recordingTime
                        ecgList.add(temp)
                        ecgAdapter.setNewInstance(ecgList)
                        ecgAdapter.notifyDataSetChanged()
                    } else if (it.type == 1) {
                        val data = Bp2BpFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = BpData()
                        temp.fileName = it.name
                        temp.sys = data.sys
                        temp.dia = data.dia
                        temp.pr = data.pr
                        temp.mean = data.mean
                        bpList.add(temp)
                        bpAdapter.setNewInstance(bpList)
                        bpAdapter.notifyDataSetChanged()
                    }
                    binding.process.text = readFileProcess
                    setReceiveCmd(it.content)
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        //--------------------------------le bp2w-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wList)
            .observe(this) {
                val data = it.data as LeBp2wBleList
                binding.info.text = "$data"
                setReceiveCmd(data.bytes)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList)
            .observe(this) { event ->
                (event.data as Bp2BleFile).let {
                    setReceiveCmd(it.content)
                    when (it.type) {
                        LeBp2wBleCmd.FileType.ECG_TYPE -> {
                            val data = LeBp2wEcgList(it.content)
                            binding.info.text = "$data"
                            if (data.ecgFileList.size != 0) {
                                for (file in data.ecgFileList) {
                                    fileNames.add(file.fileName)
                                }
                                Toast.makeText(context, "获取心电文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "获取心电文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                        }
                        LeBp2wBleCmd.FileType.BP_TYPE -> {
                            val data = LeBp2wBpList(it.content)
                            binding.info.text = "$data"
                            if (data.bpFileList.size != 0) {
                                Toast.makeText(context, "获取血压文件列表成功 共有${data.bpFileList.size}个记录", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "获取血压文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                            for (file in data.bpFileList) {
                                val temp = BpData()
                                temp.fileName = file.fileName
                                temp.sys = file.sys
                                temp.dia = file.dia
                                temp.pr = file.pr
                                temp.mean = file.mean
                                bpList.add(temp)
                                bpAdapter.setNewInstance(bpList)
                                bpAdapter.notifyDataSetChanged()
                            }
                        }
                        LeBp2wBleCmd.FileType.USER_TYPE -> {
                            val data = LeBp2wUserList(it.content)
                            binding.info.text = "$data"
                            if (data.userList.size != 0) {
                                Toast.makeText(context, "获取用户文件列表成功 共有${data.userList.size}个用户", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "获取用户文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            binding.info.text = "$it"
                        }
                    }
                    binding.deviceInfo.text = fileNames.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadingFileProgress)
            .observe(this) { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.percent.times(100)).toInt()} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${it.percent.times(100)} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileComplete)
            .observe(this) { event ->
                (event.data as LeBp2wEcgFile).let {
                    setReceiveCmd(it.content)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $it \n"
                    binding.process.text = readFileProcess
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                    val temp = EcgData()
                    temp.recordingTime = it.timestamp
                    temp.fileName = it.fileName
                    temp.data = it.waveData
                    temp.shortData = DataConvert.getBp2ShortArray(it.waveData)
                    temp.duration = it.waveData.size.div(2*250)
                    ecgList.add(temp)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                }
            }
        //------------------------------bpm--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(this) { event ->
                (event.data as BpmBleResponse.RecordData).let {
                    setReceiveCmd(it.bytes)
                    fileCount++
                    readFileProcess += "${BpmBleResponse.RecordData(it.bytes)} fileCount : $fileCount \n\n"
                    binding.info.text = readFileProcess
                    val temp = BpData()
                    temp.fileName = getTimeString(it.year, it.month, it.day, it.hour, it.minute, 0)
                    temp.sys = it.sys
                    temp.dia = it.dia
                    temp.pr = it.pr
                    bpList.add(temp)
                    bpAdapter.setNewInstance(bpList)
                    bpAdapter.notifyDataSetChanged()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd)
            .observe(this) { event ->
                (event.data as Boolean).let {
                    Toast.makeText(context, "获取用户文件列表完成", Toast.LENGTH_SHORT).show()
                }
            }
        //------------------------------pc100--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this) { event ->
                (event.data as Pc100BleResponse.BpResult).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = "$it"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this) { event ->
                (event.data as Pc100BleResponse.BpResultError).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = "$it"
                }
            }
        //------------------------------o2--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this) { event ->
                (event.data as OxyBleResponse.OxyInfo).let {
                    setReceiveCmd(it.bytes)
                    for (fileName in it.fileList.split(",")) {
                        if (fileName.isNotEmpty()) {
                            fileNames.add(fileName)
                        }
                    }
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.info.text = "$it"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.div(10))} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(this) { event ->
                (event.data as OxyBleResponse.OxyFile).let {
                    val data = OxyBleFile(it.fileContent)
                    binding.info.text = "$data"
                    setReceiveCmd(it.fileContent)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                    binding.process.text = readFileProcess
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                    val temp = OxyData()
                    temp.fileName = getTimeString(data.year, data.month, data.day, data.hour, data.minute, data.second)
                    temp.oxyBleFile = data
                    oxyList.add(temp)
                    oxyAdapter.setNewInstance(oxyList)
                    oxyAdapter.notifyDataSetChanged()
                }
            }
        //---------------------------aoj20a-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList)
            .observe(this) {
                val data = it.data as ArrayList<Aoj20aBleResponse.TempRecord>
                binding.info.text = "$data"
                var temp = ""
                for (record in data) {
                    temp += "时间 : ${getTimeString(record.year, record.month, record.day, record.hour, record.minute, 0)} ${record.temp} ℃\n\n"
                }
                binding.deviceInfo.text = temp
                Toast.makeText(context, "获取文件列表成功 共有${data.size}个文件", Toast.LENGTH_SHORT).show()
            }
        //---------------------------checkme pod--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                binding.process.text = "EventCheckmePodGetFileListError $data"
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress)
            .observe(this) {
                if (mAlertDialog?.isShowing == false) {
                    mAlertDialog?.show()
                }
                val data = it.data as Int
                binding.process.text = "读取进度: $data %"
                mainViewModel._downloadTip.value = "读取进度: $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList)
            .observe(this) {
                mAlertDialog?.dismiss()
                val data = it.data as CheckmePodBleResponse.FileList
                var temp = ""
                for (record in data.list) {
                    temp += "时间 : ${getTimeString(record.year, record.month, record.day, record.hour, record.minute, record.second)}\nSpO2 : ${record.spo2} %\nPR : ${record.pr}\nPI : ${record.pi}\nTemp : ${record.temp} ℃\n\n"
                }
                binding.deviceInfo.text = temp
                Toast.makeText(context, "获取文件列表成功 共有${data.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = "$data"
            }
        //---------------------------pc68b---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bFileList)
            .observe(this) {
                val data = it.data as MutableList<String>
                for (i in data) {
                    fileNames.add(i)
                }
                Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = fileNames.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bReadFileComplete)
            .observe(this) {
                val data = it.data as Pc68bBleResponse.Record
                pc68bList.add(data)
                binding.info.text = pc68bList.toString()
                if (binding.fileName.text.toString().isEmpty()) {
                    fileNames.removeAt(0)
                    readFile()
                } else {
                    mAlertDialog?.dismiss()
                }
                Toast.makeText(context, "接收文件成功 已接收${pc68bList.size}个文件, 还剩${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
            }
        //---------------------------pc80b-----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress)
            .observe(this) {
                if (mAlertDialog?.isShowing == false) {
                    mAlertDialog?.show()
                }
                val data = it.data as Int
                binding.process.text = "读取进度: $data %"
                mainViewModel._downloadTip.value = "读取进度: $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete)
            .observe(this) {
                mAlertDialog?.dismiss()
                val data = it.data as PC80BleResponse.ScpEcgFile
                binding.info.text = "$data"
                val fileName = getTimeString(data.section1.year, data.section1.month, data.section1.day, data.section1.hour, data.section1.minute, data.section1.second)
                for (file in ecgList) {
                    if (file.fileName == fileName)
                        return@observe
                }
                val temp = EcgData()
                temp.fileName = fileName
                temp.recordingTime = DateUtil.getSecondTimestamp(temp.fileName)
                temp.data = data.section6.ecgData.ecg
                temp.shortData = DataConvert.getPc80bShortArray(data.section6.ecgData.ecg)
                temp.duration = 30
                ecgList.add(temp)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
            }
        //---------------------------Pulsebit--------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList)
            .observe(this) {
                val data = it.data as PulsebitBleResponse.FileList
                for (file in data.list) {
                    fileNames.add(file.recordName)
                }
                Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = "$data"
                binding.deviceInfo.text = fileNames.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                binding.process.text = "EventPulsebitGetFileListError $data"
                Toast.makeText(context, "读文件列表出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                binding.process.text = "读取进度: $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete)
            .observe(this) {
                val data = it.data as PulsebitBleResponse.EcgFile
                setReceiveCmd(data.bytes)
                readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                binding.process.text = readFileProcess
                if (binding.fileName.text.toString().isEmpty()) {
                    fileNames.removeAt(0)
                    readFile()
                } else {
                    mAlertDialog?.dismiss()
                }
                val temp = EcgData()
                temp.recordingTime = DateUtil.getSecondTimestamp(data.fileName)
                temp.fileName = data.fileName
                temp.data = data.waveData
                temp.shortData = DataConvert.getExShortArray(data.waveData)
                temp.duration = data.recordingTime
                ecgList.add(temp)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.process.text = "$readFileProcess$curFileName 读取进度: $data %"
                mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: $data %"
            }

        //---------------------------CheckmeLE--------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileList)
            .observe(this) {
                val data = it.data as CheckmeLeBleResponse.ListContent
                when (data.type) {
                    CheckmeLeBleCmd.ListType.DLC_TYPE -> {
                        val list = CheckmeLeBleResponse.DlcList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = "$list"
                    }
                    CheckmeLeBleCmd.ListType.TEMP_TYPE -> {
                        val list = CheckmeLeBleResponse.TempList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = "$list"
                    }
                    CheckmeLeBleCmd.ListType.ECG_TYPE -> {
                        val list = CheckmeLeBleResponse.EcgList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = "$list"
                    }
                    CheckmeLeBleCmd.ListType.OXY_TYPE -> {
                        val list = CheckmeLeBleResponse.OxyList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = "$list"
                    }
                }
                binding.deviceInfo.text = fileNames.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError)
            .observe(this) {
                val data = it.data as Boolean
                binding.process.text = "EventCheckmeLeGetFileListError $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress)
            .observe(this) {
                val data = it.data as Int
                binding.process.text = "读取进度: $data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete)
            .observe(this) {
                val data = it.data as CheckmeLeBleResponse.EcgFile
                setReceiveCmd(data.bytes)
                readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                binding.process.text = readFileProcess
                if (binding.fileName.text.toString().isEmpty()) {
                    fileNames.removeAt(0)
                    readFile()
                } else {
                    mAlertDialog?.dismiss()
                }
                val temp = EcgData()
                temp.recordingTime = DateUtil.getSecondTimestamp(data.fileName)
                temp.fileName = data.fileName
                temp.data = data.waveData
                temp.shortData = DataConvert.getExShortArray(data.waveData)
                temp.duration = data.recordingTime
                ecgList.add(temp)
                ecgAdapter.setNewInstance(ecgList)
                ecgAdapter.notifyDataSetChanged()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError)
            .observe(this) {
                mAlertDialog?.dismiss()
                Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress)
            .observe(this) {
                val data = it.data as Int
                binding.process.text = "$readFileProcess$curFileName 读取进度: $data %"
                mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: $data %"
            }

        //--------------------------------le S1-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile)
            .observe(this) { event ->
                (event.data as Boolean).let {
                    binding.info.text = "没有文件 $it"
                    mAlertDialog?.dismiss()
                    Toast.makeText(context, "没有文件", Toast.LENGTH_SHORT).show()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess$curFileName 读取进度: ${(it.div(10))} %"
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: ${(it.div(10))} %"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileComplete)
            .observe(this) { event ->
                (event.data as LeS1BleResponse.BleFile).let {
                    setReceiveCmd(it.bytes)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $it \n"
                    binding.process.text = readFileProcess
                    val temp = EcgData()
                    temp.recordingTime = DateUtil.getSecondTimestamp("00000000000000")
                    temp.fileName = "00000000000000"
                    temp.data = it.ecgData
                    temp.shortData = DataConvert.getExShortArray(it.ecgData)
                    temp.duration = it.ecgResult?.recordingTime!!
                    ecgList.add(temp)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                }
            }
        //--------------------------------LPM311-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data)
            .observe(this) {
                val data = it.data as Lpm311Data
                binding.info.text = "$data"
                binding.deviceInfo.text = "user : ${data.user}\nCHOL : ${data.chol} （${data.cholStr}）\nTRIG : ${data.trig} （${data.trigStr}）\n" +
                        "HDL : ${data.hdl} （${data.hdlStr}）\nLDL : ${data.ldl} （${data.ldlStr}）\n" +
                        "CHOL/HDL : ${data.cholDivHdl} （${data.cholDivHdlStr}）\nUNIT : ${if (data.unit == 0) {"mmol/L"} else {"mg/dL"}}"
            }
        //------------------------------PoctorM3102--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PoctorM3102.EventPoctorM3102Data)
            .observe(this) {
                val data = it.data as PoctorM3102Data
                binding.info.text = "$data"
                binding.deviceInfo.text = when (data.type) {
                    0 -> "血糖 : ${if (data.normal) {"${data.result} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    1 -> "尿酸 : ${if (data.normal) {"${data.result} umol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    3 -> "血酮 : ${if (data.normal) {"${data.result} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    else -> "数据出错 : \n$data"
                }
            }
        //------------------------------Bioland-BGM--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown)
            .observe(this) {
                val data = it.data as Int
                binding.info.text = "$data"
                binding.deviceInfo.text = "倒计时 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData)
            .observe(this) {
                val data = it.data as BiolandBgmBleResponse.GluData
                binding.info.text = "$data"
                binding.deviceInfo.text = "血糖 : ${data.resultMg} mg/dL ${data.resultMmol} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData)
            .observe(this) {
                val data = it.data as Boolean
                binding.info.text = "$data"
                Toast.makeText(context, "没有文件", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setReceiveCmd(bytes: ByteArray) {
        if (isReceive) {
            binding.receiveCmd.text = "receive : ${bytesToHex(bytes)}"
        }
    }

    private fun readFile() {
        if (binding.fileName.text.toString().isNotEmpty()) {
            LpBleUtil.readFile("", trimStr(binding.fileName.text.toString()), Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        } else {
            if (fileNames.size == 0) {
                mAlertDialog?.dismiss()
                return
            }
            curFileName = fileNames[0]
            LpBleUtil.readFile("", fileNames[0], Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
    }

}