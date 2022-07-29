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
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.*
import com.lepu.demo.ble.EcgAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.data.EcgData
import com.lepu.demo.databinding.FragmentInfoBinding


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

    private lateinit var adapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()

    var mAlertDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }
    private fun initView(){

        mainViewModel.bleState.observe(viewLifecycleOwner, {
            if (it) {
                binding.infoLayout.visibility = View.VISIBLE
            } else {
                binding.infoLayout.visibility = View.GONE
            }
        })

        mAlertDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.rcv.layoutManager = this
        }
        adapter = EcgAdapter(R.layout.device_item, null).apply {
            binding.rcv.adapter = this
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcgData).let {
                    val intent = Intent(context, WaveEcgActivity::class.java)
                    intent.putExtra("waveData", it.data)
                    intent.putExtra("recordingTime", it.recordingTime)
                    startActivity(intent)
                }
            }
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

        mainViewModel.er1Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.er2Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.pc80bInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.bp2Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.bpmInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.oxyInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.pc100Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.boInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.aoj20aInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.checkmePodInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.pulsebitInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.checkmeLeInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.pc300Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.lemInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.lewInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })

        // 公共方法测试
        // 获取设备信息
        binding.getInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
            ecgList.clear()
            adapter.setNewInstance(ecgList)
            adapter.notifyDataSetChanged()
        }
        // 获取文件列表
        binding.getFileList.setOnClickListener {
            fileCount = 0
            fileNames.clear()
            pc68bList.clear()
            ecgList.clear()
            adapter.setNewInstance(ecgList)
            adapter.notifyDataSetChanged()

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
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 读文件
        binding.readFile.setOnClickListener {
            readFileProcess = ""
            mAlertDialog?.show()
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
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂设置
        binding.factory.setOnClickListener {
            LpBleUtil.factoryReset(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂状态
        binding.factoryAll.setOnClickListener {
            LpBleUtil.factoryResetAll(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }

        binding.getMtu.setOnClickListener {
            binding.sendCmd.text = "mtu : " + LpBleUtil.getBleMtu(Constant.BluetoothConfig.currentModel[0])
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
            .observe(this, { event ->
                (event.data as String).let {
                    binding.info.text = it
                    for (fileName in it.split(",")) {
//                        if (fileName.contains("R")) {
                        if(fileName.isNotEmpty()) {
                            fileNames.add(fileName)
                        }
//                        }
                    }
                    Toast.makeText(context, "er1/duoek 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = "$readFileProcess $curFileName 读取进度: ${(it/10)} %"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete)
            .observe(this, { event ->
                (event.data as Er1BleResponse.Er1File).let {
                    if (it.fileName.contains("R")) {
                        val data = Er1EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = EcgData()
                        temp.recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                        temp.fileName = it.fileName
                        temp.data = data.waveData
                        temp.duration = data.recordingTime
                        ecgList.add(temp)
                        adapter.setNewInstance(ecgList)
                        adapter.notifyDataSetChanged()
                    } else {
                        val data = Er2AnalysisFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
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
            })
        //--------------------------------er2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList)
            .observe(this, { event ->
                (event.data as Er2FileList).let {
                    binding.info.text = it.toString()
                    for (fileName in it.fileNames) {
//                        if (fileName.contains("R")) {
                            fileNames.add(fileName)
//                        }
                    }
                    Toast.makeText(context, "er2 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
            .observe(this, { event ->
                (event.data as Er2File).let {
                    if (it.fileName.contains("R")) {
                        val data = Er1EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                        val temp = EcgData()
                        temp.recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                        temp.fileName = it.fileName
                        temp.data = data.waveData
                        temp.duration = data.recordingTime
                        ecgList.add(temp)
                        adapter.setNewInstance(ecgList)
                        adapter.notifyDataSetChanged()
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
            })
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
                Toast.makeText(context, "lew手表 获取列表成功 ${data.type}", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadingFileProgress)
            .observe(this) { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it / 10).toString() + "%"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
            .observe(this) { event ->
                (event.data as LewBleResponse.EcgFile).let {
                    setReceiveCmd(it.content)
                    val file = EcgFile(it.content)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $file \n"
                    binding.process.text = readFileProcess
                    Toast.makeText(context, "lew手表 获取心电文件$curFileName 成功", Toast.LENGTH_SHORT).show()
                    if (binding.fileName.text.toString().isEmpty()) {
                        fileNames.removeAt(0)
                        readFile()
                    } else {
                        mAlertDialog?.dismiss()
                    }
                }
            }
        //--------------------------------bp2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observe(this, { event ->
                (event.data as KtBleFileList).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = it.toString()
                    for (fileName in it.fileNameList) {
                        fileNames.add(fileName)
                    }
                    Toast.makeText(context, "bp2 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it.percent*100).toInt().toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observe(this, { event ->
                (event.data as Bp2BleFile).let {
                    if (it.type == 2) {
                        val data = Bp2EcgFile(it.content)
                        val shortData = ShortArray(data.waveData.size.div(2))
                        for (i in 0 until shortData.size) {
                            shortData[i] = toSignedShort(data.waveData[i*2], data.waveData[i*2+1])
                        }
                        Log.d("mytest", "${shortData.joinToString()}")
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                    } else if (it.type == 1) {
                        val data = Bp2BpFile(it.content)
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
            })
        //--------------------------------bp2w-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList)
            .observe(this, { event ->
                (event.data as KtBleFileList).let {
                    setReceiveCmd(it.bytes)
                    for (fileName in it.fileNameList) {
                        if (fileName != null) {
                            fileNames.add(fileName)
                        }
                    }
                    Toast.makeText(context, "bp2w 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.info.text = it.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress)
            .observe(this, { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it.percent*100).toInt().toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete)
            .observe(this, { event ->
                (event.data as Bp2BleFile).let {
                    if (it.type == 2) {
                        val data = Bp2EcgFile(it.content)
                        binding.info.text = "$data"
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $data \n"
                    } else if (it.type == 1) {
                        val data = Bp2BpFile(it.content)
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
            })
        //--------------------------------le bp2w-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wList)
            .observe(this, {
                val data = it.data as LeBp2wBleList
                binding.info.text = data.toString()
                setReceiveCmd(data.bytes)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList)
            .observe(this, { event ->
                (event.data as Bp2BleFile).let {
                    setReceiveCmd(it.content)
                    when (it.type) {
                        LeBp2wBleCmd.FileType.ECG_TYPE -> {
                            val data = LeBp2wEcgList(it.content)
                            binding.info.text = data.toString()
                            if (data.ecgFileList.size != 0) {
                                for (file in data.ecgFileList) {
                                    fileNames.add(file.fileName)
                                }
                                Toast.makeText(context, "le bp2w 获取心电文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "le bp2w 获取心电文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                        }
                        LeBp2wBleCmd.FileType.BP_TYPE -> {
                            val data = LeBp2wBpList(it.content)
                            binding.info.text = data.toString()
                            if (data.bpFileList.size != 0) {
                                Toast.makeText(context, "le bp2w 获取血压文件列表成功 共有${data.bpFileList.size}个记录", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "le bp2w 获取血压文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                        }
                        LeBp2wBleCmd.FileType.USER_TYPE -> {
                            val data = LeBp2wUserList(it.content)
                            binding.info.text = data.toString()
                            if (data.userList.size != 0) {
                                Toast.makeText(context, "le bp2w 获取用户文件列表成功 共有${data.userList.size}个用户", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "le bp2w 获取用户文件列表为空", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            binding.info.text = it.toString()
                        }
                    }
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadingFileProgress)
            .observe(this, { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it.percent*100).toInt().toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileComplete)
            .observe(this, { event ->
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
                }
            })
        //------------------------------bpm--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(this, { event ->
                (event.data as BpmBleResponse.RecordData).let {
                    setReceiveCmd(it.bytes)
                    fileCount++
                    readFileProcess += BpmBleResponse.RecordData(it.bytes).toString() + " fileCount : $fileCount \n\n"
                    binding.info.text = readFileProcess
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd)
            .observe(this) { event ->
                (event.data as Boolean).let {
                    Toast.makeText(context, "bpm 获取用户文件列表完成", Toast.LENGTH_SHORT).show()
                }
            }
        //------------------------------pc100--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this, { event ->
                (event.data as Pc100BleResponse.BpResult).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = it.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this, { event ->
                (event.data as Pc100BleResponse.BpResultError).let {
                    setReceiveCmd(it.bytes)
                    binding.info.text = it.toString()
                }
            })
        //------------------------------o2--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this, { event ->
                (event.data as OxyBleResponse.OxyInfo).let {
                    setReceiveCmd(it.bytes)
                    for (fileName in it.fileList.split(",")) {
                        if (fileName.isNotEmpty()) {
                            fileNames.add(fileName)
                        }
                    }
                    Toast.makeText(context, "o2 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.info.text = it.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(this, { event ->
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
                }
            })
        //---------------------------aoj20a-----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList)
            .observe(this, {
                val data = it.data as ArrayList<Aoj20aBleResponse.TempRecord>
                binding.info.text = data.toString()
                Toast.makeText(context, "aoj20a 获取文件列表成功 共有${data.size}个文件", Toast.LENGTH_SHORT).show()
            })
        //---------------------------checkme pod--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
            .observe(this, {
                val data = it.data as Boolean
                binding.process.text = "EventCheckmePodGetFileListError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress)
            .observe(this, {
                val data = it.data as Int
                binding.process.text = "读取进度:$data%"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList)
            .observe(this, {
                val data = it.data as CheckmePodBleResponse.FileList
                Toast.makeText(context, "checkme pod 获取文件列表成功 共有${data.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = data.toString()
            })
        //---------------------------pc68b---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bFileList)
            .observe(this, {
                val data = it.data as MutableList<String>
                for (i in data) {
                    fileNames.add(i)
                }
                Toast.makeText(context, "pc68b 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = fileNames.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bReadFileComplete)
            .observe(this, {
                val data = it.data as Pc68bBleResponse.Record
                pc68bList.add(data)
                binding.info.text = pc68bList.toString()
                if (binding.fileName.text.toString().isEmpty()) {
                    fileNames.removeAt(0)
                    readFile()
                } else {
                    mAlertDialog?.dismiss()
                }
                Toast.makeText(context, "pc68b 接收文件成功 已接收${pc68bList.size}个文件, 还剩${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
            })
        //---------------------------Pulsebit--------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList)
            .observe(this, {
                val data = it.data as PulsebitBleResponse.FileList
                for (file in data.list) {
                    fileNames.add(file.recordName)
                }
                Toast.makeText(context, "Pulsebit 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                binding.info.text = data.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError)
            .observe(this, {
                val data = it.data as Boolean
                binding.process.text = "EventPulsebitGetFileListError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress)
            .observe(this, {
                val data = it.data as Int
                binding.process.text = "读取进度:$data%"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete)
            .observe(this, {
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
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError)
            .observe(this, {
                val data = it.data as Boolean
                binding.process.text = "EventPulsebitReadFileError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress)
            .observe(this, {
                val data = it.data as Int
                binding.process.text = readFileProcess + curFileName + " 读取进度:" + data.toString() + "%"
            })

        //---------------------------CheckmeLE--------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileList)
            .observe(this, {
                val data = it.data as CheckmeLeBleResponse.ListContent
                when (data.type) {
                    CheckmeLeBleCmd.ListType.DLC_TYPE -> {
                        val list = CheckmeLeBleResponse.DlcList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "CheckmeLE 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = list.toString()
                    }
                    CheckmeLeBleCmd.ListType.TEMP_TYPE -> {
                        val list = CheckmeLeBleResponse.TempList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "CheckmeLE 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = list.toString()
                    }
                    CheckmeLeBleCmd.ListType.ECG_TYPE -> {
                        val list = CheckmeLeBleResponse.EcgList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "CheckmeLE 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = list.toString()
                    }
                    CheckmeLeBleCmd.ListType.OXY_TYPE -> {
                        val list = CheckmeLeBleResponse.OxyList(data.content)
                        for (file in list.list) {
                            fileNames.add(file.recordName)
                        }
                        Toast.makeText(context, "CheckmeLE 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                        binding.info.text = list.toString()
                    }
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListError)
            .observe(this, {
                val data = it.data as Boolean
                binding.process.text = "EventCheckmeLeGetFileListError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeGetFileListProgress)
            .observe(this, {
                val data = it.data as Int
                binding.process.text = "读取进度:$data%"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete)
            .observe(this, {
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
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileError)
            .observe(this, {
                val data = it.data as Boolean
                binding.process.text = "EventCheckmeLeReadFileError $data"
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadingFileProgress)
            .observe(this, {
                val data = it.data as Int
                binding.process.text = readFileProcess + curFileName + " 读取进度:" + data.toString() + "%"
            })

        //--------------------------------le S1-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile)
            .observe(this, { event ->
                (event.data as Boolean).let {
                    binding.info.text = "没有文件 $it"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileComplete)
            .observe(this, { event ->
                (event.data as LeS1BleResponse.BleFile).let {
                    setReceiveCmd(it.bytes)
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n $it \n"
                    binding.process.text = readFileProcess
                }
            })
    }

    private fun setReceiveCmd(bytes: ByteArray) {
        if (isReceive) {
            binding.receiveCmd.text = "receive : " + bytesToHex(bytes)
        }
    }

    private fun readFile() {
        if (binding.fileName.text.toString().isNotEmpty()) {
            LpBleUtil.readFile("", binding.fileName.text.toString(), Constant.BluetoothConfig.currentModel[0])
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