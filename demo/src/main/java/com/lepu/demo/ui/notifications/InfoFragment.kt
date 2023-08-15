package com.lepu.demo.ui.notifications

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import androidx.lifecycle.ViewModelProvider
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.algpro.AlgorithmUtil
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.vals.server
import com.lepu.blepro.vals.wifiConfig
import com.lepu.blepro.vals.wifi
import com.lepu.demo.*
import com.lepu.demo.ble.*
import com.lepu.demo.config.Constant
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecgData
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecnData
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.oxyData
import com.lepu.demo.data.*
import com.lepu.demo.databinding.FragmentInfoBinding
import com.lepu.demo.ui.adapter.*
import com.lepu.demo.util.DataConvert
import com.lepu.demo.util.FileUtil
import com.lepu.demo.util.LogcatHelper
import java.io.*
import kotlin.collections.ArrayList

class InfoFragment : Fragment(R.layout.fragment_info){

    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var infoViewModel: InfoViewModel

    private val binding: FragmentInfoBinding by binding()

    private var fileNames: ArrayList<String> = arrayListOf()

    private var curFileName = ""
    private var readFileProcess = ""
    private var process = 0

    private var fileType = 0

    private var isReceive = false

    private lateinit var ecgAdapter: EcgAdapter
    var ecgList: ArrayList<EcgData> = arrayListOf()
    var analysisFile: ArrayList<AnalysisFile> = arrayListOf()
    private lateinit var oxyAdapter: OxyAdapter
    var oxyList: ArrayList<OxyData> = arrayListOf()
    private lateinit var bpAdapter: BpAdapter
    var bpList: ArrayList<BpData> = arrayListOf()
    private lateinit var ecnAdapter: EcnAdapter
    var ecnList: ArrayList<EcnData> = arrayListOf()

    var mAlertDialog: AlertDialog? = null
    var mAlertDialogCanCancel: AlertDialog? = null
    var mCancelDialog: AlertDialog? = null
    private lateinit var bp2wAdapter: WifiAdapter
    private var popupWindow: PopupWindow? = null
    private var handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
//        val data = OxyData()
//        data.fileName = "20230530010104"
//        data.oxyBleFile = OxyBleFile(FileUtil.readFileToByteArray(context, "/o2/20230530010104.dat"))
//        sleepAlg(data)
//        testEr3()
//        testEr3Decompress()

        // 画心电图
//        val data = FileUtil.readFileToString(context, "1.txt").split(",")
//        val shortData = mutableListOf<Short>()
//        for (d in data) {
//            shortData.add(d.toShort())
//        }
//        val temp = getEcgData(0, "导联 aVF", byteArrayOf(0), shortData.toShortArray(), 0)
//        ecgList.add(temp)
//        ecgAdapter.setNewInstance(ecgList)
//        ecgAdapter.notifyDataSetChanged()
    }

    private fun testEr3Decompress() {
        /*val fileName = "W20220921154419"
        val data = org.apache.commons.io.FileUtils.readFileToByteArray(File("${context?.getExternalFilesDir(null)?.absolutePath}/$fileName"))
        Er3BleResponse.getAllLeadShortsFromWaveBytes(data.copyOfRange(10, data.size-20), 0)
        Log.d("11111111111111", "${data.size}")*/

        val fileName = "20230523102452.txt"
        val list = FileUtil.readFileToString(context, fileName).split(",")
        val byteList = mutableListOf<Byte>()
        list.map { it.toShort() }.forEach { short ->
            shortToByteLittle(short).forEach {
                byteList.add(it)
            }
        }
        Er3BleResponse.getAllLeadShortsFromWaveBytes(byteList.toByteArray(), 2)
        Log.d("11111111111111", "${byteList.size}")
    }
    fun shortToByteLittle(n: Short): ByteArray {
        val b = ByteArray(2)
        b[0] = (n.toInt() and 0xff).toByte()
        b[1] = (n.toInt() shr 8 and 0xff).toByte()
        return b
    }

    private fun testEr3() {
//        val fileName = "W20221025150240"
        val fileName = "W20220921154419"
        val duration = FileUtil.saveEr3File(context, fileName)
//        val recordingTime = DateUtil.getSecondTimestamp("20221025150240")
        val recordingTime = DateUtil.getSecondTimestamp("20220921154419")
        val tempV6 = getEcgData(recordingTime, "导联 aVF", byteArrayOf(0), testGetLeadShortData("aVF", fileName), duration)
        ecgList.add(tempV6)
        /*val tempI = getEcgData(recordingTime, "导联 I", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("I", fileName)), duration)
        ecgList.add(tempI)
        val tempII = getEcgData(recordingTime, "导联 II", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("II", fileName)), duration)
        ecgList.add(tempII)
        val tempV1 = getEcgData(recordingTime, "导联 V1", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("V1", fileName)), duration)
        ecgList.add(tempV1)
        val tempV2 = getEcgData(recordingTime, "导联 V2", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("V2", fileName)), duration)
        ecgList.add(tempV2)
        val tempV3 = getEcgData(recordingTime, "导联 V3", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("V3", fileName)), duration)
        ecgList.add(tempV3)
        val tempV4 = getEcgData(recordingTime, "导联 V4", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("V4", fileName)), duration)
        ecgList.add(tempV4)
        val tempV5 = getEcgData(recordingTime, "导联 V5", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("V5", fileName)), duration)
        ecgList.add(tempV5)
        val tempIII = getEcgData(recordingTime, "导联 III", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("III", fileName)), duration)
        ecgList.add(tempIII)
        val tempaVR = getEcgData(recordingTime, "导联 aVR", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("aVR", fileName)), duration)
        ecgList.add(tempaVR)
        val tempaVL = getEcgData(recordingTime, "导联 aVL", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("aVL", fileName)), duration)
        ecgList.add(tempaVL)
        val tempaVF = getEcgData(recordingTime, "导联 aVF", byteArrayOf(0), DataConvert.getEr3ShortArray(testGetLeadData("aVF", fileName)), duration)
        ecgList.add(tempaVF)*/
        ecgAdapter.setNewInstance(ecgList)
        ecgAdapter.notifyDataSetChanged()
    }
    private fun testGetLeadShortData(leadName: String, fileName: String) : ShortArray {
        var file = File(context?.getExternalFilesDir(null)!!.absolutePath)
        file = File(file, "$fileName.txt")
        return try {
            val data = mutableListOf<Short>()
            file.bufferedReader().forEachLine { line ->
                data.addAll(Er3BleResponse.getEachLeadDataShorts(leadName, line.split(",").toTypedArray()))
            }
            data.toShortArray()
        } catch (e: Exception) {
            e.printStackTrace()
            ShortArray(0)
        }
    }

    private fun init() {

        mainViewModel.bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.infoLayout.visibility = View.VISIBLE
            } else {
                binding.infoLayout.visibility = View.GONE
            }
        }

        mCancelDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage(context?.getString(R.string.whether_continue_read))
            .setPositiveButton(context?.getString(R.string.confirm)) { _, _ ->
                val offset = DownloadHelper.readFile(Constant.BluetoothConfig.currentModel[0], "", curFileName)
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING_S) {
                    LpBleUtil.continueReadFile(Constant.BluetoothConfig.currentModel[0], "", curFileName, offset.size, fileType)
                } else {
                    LpBleUtil.continueReadFile(Constant.BluetoothConfig.currentModel[0], "", curFileName, offset.size)
                }
                mAlertDialog?.show()
            }
            .setNegativeButton(context?.getString(R.string.cancel)) { _, _ ->
                LpBleUtil.cancelReadFile(Constant.BluetoothConfig.currentModel[0])
            }
            .create()

        if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1_N
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM1
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10AW_1
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10BWS
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING_S) {
            mAlertDialog = AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setMessage(context?.getString(R.string.handling))
                .setNegativeButton(context?.getString(R.string.pause)) { _, _ ->
                    LpBleUtil.pauseReadFile(Constant.BluetoothConfig.currentModel[0])
                    mCancelDialog?.show()
                }
                .create()
        } else {
            mAlertDialog = AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setMessage(context?.getString(R.string.handling))
                .create()
        }
        mAlertDialogCanCancel = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setMessage(context?.getString(R.string.handling))
            .create()
        mainViewModel.downloadTip.observe(viewLifecycleOwner) {
            mAlertDialog?.setMessage("${context?.getString(R.string.handling)} $it")
            mAlertDialogCanCancel?.setMessage("${context?.getString(R.string.handling)} $it")
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
                    oxyData.fileName = it.fileName
                    oxyData.recordingTime = it.recordingTime
                    oxyData.avgSpo2 = it.avgSpo2
                    oxyData.avgHr = it.avgHr
                    oxyData.minSpo2 = it.minSpo2
                    oxyData.dropsTimes3Percent = it.dropsTimes3Percent
                    oxyData.dropsTimes4Percent = it.dropsTimes4Percent
                    oxyData.asleepTimePercent = it.asleepTimePercent
                    oxyData.durationTime90Percent = it.durationTime90Percent
                    oxyData.asleepTime = it.asleepTime
                    oxyData.o2Score = it.o2Score
                    oxyData.startTime = it.startTime
                    oxyData.spo2s = it.spo2s
                    oxyData.hrs = it.hrs
                    oxyData.motions = it.motions
                    oxyData.warningSpo2s = it.warningSpo2s
                    oxyData.warningHrs = it.warningHrs
                    if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10AW_1
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PF_10BWS) {
                        val intent = Intent(context, DataActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(context, OxyDataActivity::class.java)
                        startActivity(intent)
                    }
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
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.ecnRcv.layoutManager = this
        }
        ecnAdapter = EcnAdapter(R.layout.device_item, null).apply {
            binding.ecnRcv.adapter = this
        }
        ecnAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size > 0) {
                (adapter.getItem(position) as EcnData).let {
                    ecnData.fileName = it.fileName
                    ecnData.data = it.data
                    startActivity(Intent(context, DataActivity::class.java))
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
        binding.getWifiRoute.setOnClickListener {
            mAlertDialogCanCancel?.setMessage(context?.getString(R.string.handling))
            mAlertDialogCanCancel?.show()
            LpBleUtil.bp2GetWifiDevice(Constant.BluetoothConfig.currentModel[0])
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.wifiRcv.layoutManager = this
        }
        bp2wAdapter = WifiAdapter(R.layout.device_item, null).apply {
            binding.wifiRcv.adapter = this
        }
        bp2wAdapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bp2Wifi).let {
                val popupView = View.inflate(context, R.layout.popup_window, null)
                if (popupWindow == null) {
                    popupWindow = PopupWindow(context)
                }
                popupWindow?.let { v ->
                    v.width = ViewGroup.LayoutParams.MATCH_PARENT
                    v.height = 1000
                    v.contentView = popupView
                    v.isFocusable = true
                    v.showAsDropDown(view)
                    val attr = activity?.window?.attributes
                    attr?.alpha = 0.5f
                    activity?.window?.attributes = attr
                    v.setOnDismissListener {
                        val attr = activity?.window?.attributes
                        attr?.alpha = 1f
                        activity?.window?.attributes = attr
                    }
                }
                val password = popupView.findViewById<EditText>(R.id.password)
                password.visibility = View.VISIBLE
                val ssid = popupView.findViewById<TextView>(R.id.ssid)
                ssid.visibility = View.VISIBLE
                val server1 = popupView.findViewById<TextView>(R.id.server)
                val serverAddr = popupView.findViewById<EditText>(R.id.server_address)
                val serverPort = popupView.findViewById<EditText>(R.id.server_port)
                val sure = popupView.findViewById<Button>(R.id.sure)
                val cancel = popupView.findViewById<Button>(R.id.cancel)
                ssid.text = "WiFi：${it.ssid}"
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3W
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R20
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R21
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R10
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R11
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LERES) {
                    sure.setOnClickListener { it1 ->
                        val pass = trimStr(password.text.toString())
                        val addr = trimStr(serverAddr.text.toString())
                        val port = trimStr(serverPort.text.toString())
                        if (/*pass.isNullOrEmpty() || */addr.isNullOrEmpty() || port.isNullOrEmpty()) {
                            Toast.makeText(context, context?.getString(R.string.input_tips), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } else {
                            wifiConfig.option = 3
                            it.pwd = pass
                            wifiConfig.wifi = it
                            wifi = it
                            server.addr = addr
                            server.port = port.toInt()
                            wifiConfig.server = server
                            LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                            adapter.setList(null)
                            adapter.notifyDataSetChanged()
                            popupWindow?.dismiss()
                            mAlertDialogCanCancel?.setMessage(context?.getString(R.string.handling))
                            mAlertDialogCanCancel?.show()
                        }
                    }
                } else {
                    server1.visibility = View.GONE
                    serverAddr.visibility = View.GONE
                    serverPort.visibility = View.GONE
                    sure.setOnClickListener { it1 ->
                        val pass = trimStr(password.text.toString())
//                        if (pass.isNullOrEmpty()) {
//                            Toast.makeText(context, context?.getString(R.string.input_tips), Toast.LENGTH_SHORT).show()
//                            return@setOnClickListener
//                        } else {
                            wifiConfig.option = 3
                            it.pwd = pass
                            wifiConfig.wifi = it
                            wifi = it
                            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2W) {
                                server.addr = "34.209.148.123"
                                server.port = 7100
                            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LE_BP2W) {
                                server.addr = "212.129.241.54"
                                server.port = 7200
                            }
                            wifiConfig.server = server
                            LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                            adapter.setList(null)
                            adapter.notifyDataSetChanged()
                            popupWindow?.dismiss()
                            mAlertDialogCanCancel?.setMessage(context?.getString(R.string.handling))
                            mAlertDialogCanCancel?.show()
//                        }
                    }
                }
                cancel.setOnClickListener {
                    popupWindow?.dismiss()
                }
            }
        }

        // 公共方法测试
        // 获取设备信息
        binding.getInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
            fileNames.clear()
            refreshUI()
        }
        // 获取文件列表
        binding.getFileList.setOnClickListener {
            fileNames.clear()
            refreshUI()
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CMRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M_WPS
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SNOREO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_WEARO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYLINK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2_WPS
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SI_PO6
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYFIT
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYFIT_WPS
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
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKME) {
                fileType++
                if (fileType > CheckmeBleCmd.ListType.BPCAL_TYPE) {
                    fileType = CheckmeBleCmd.ListType.DLC_TYPE
                }
                LpBleUtil.checkmeGetFileList(Constant.BluetoothConfig.currentModel[0], fileType, 1)
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R20
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R21
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R10
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R11
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LERES) {
                fileType++
                if (fileType > 2) {
                    fileType = 1
                }
                // 获取当天统计数据
                val timestamp = com.lepu.demo.util.DateUtil.getDayTimestamp()
//                LpBleUtil.ventilatorGetFileList(Constant.BluetoothConfig.currentModel[0], 1, timestamp)
                LpBleUtil.ventilatorGetFileList(Constant.BluetoothConfig.currentModel[0], 1, 1686672000)
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING_S) {
                fileType++
                if (fileType > OxyIIBleCmd.FileType.PPG) {
                    fileType = OxyIIBleCmd.FileType.OXY
                }
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0], fileType)
            } else {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
            }
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }
        // 读文件
        binding.readFile.setOnClickListener {
            LogcatHelper.getInstance(context).stop()
            LogcatHelper.getInstance(context).start()
            readFileProcess = ""
            process = 0
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BTP
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ECN) {
                mAlertDialogCanCancel?.show()
            } else {
                mAlertDialog?.show()
            }
            refreshUI()
            readFile()
        }
        // 暂停读取文件
        binding.pauseRf.setOnClickListener {

        }
        // 继续读取文件
        binding.continueRf.setOnClickListener {

        }
        // 取消读取文件
        binding.cancelRf.setOnClickListener {

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
        // bp2 wifi
        binding.getWifiConfig.setOnClickListener {
            LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
        }
        binding.setWifiConfig.setOnClickListener {
            bp2wAdapter.setList(null)
            bp2wAdapter.notifyDataSetChanged()
            if (wifi != null) {
                mAlertDialogCanCancel?.setMessage(context?.getString(R.string.handling))
                mAlertDialogCanCancel?.show()
                wifiConfig.option = 3
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2W) {
                    // 源动健康测试服
                    server.addr = "34.209.148.123"
                    server.port = 7100
                } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LE_BP2W) {
                    // 乐普健康测试服
                    server.addr = "212.129.241.54"
                    server.port = 7200
                }
                wifiConfig.server = server
                LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
            } else {
                Toast.makeText(context, context?.getString(R.string.wifi_tips), Toast.LENGTH_SHORT).show()
            }
        }
        binding.set4gServer.setOnClickListener {
            val popupView = View.inflate(context, R.layout.popup_window, null)
            if (popupWindow == null) {
                popupWindow = PopupWindow(context)
            }
            popupWindow?.let { v ->
                v.width = ViewGroup.LayoutParams.MATCH_PARENT
                v.height = 1000
                v.contentView = popupView
                v.isFocusable = true
                v.showAsDropDown(view)
                val attr = activity?.window?.attributes
                attr?.alpha = 0.5f
                activity?.window?.attributes = attr
                v.setOnDismissListener {
                    val attr = activity?.window?.attributes
                    attr?.alpha = 1f
                    activity?.window?.attributes = attr
                }
            }
            val password = popupView.findViewById<EditText>(R.id.password)
            password.visibility = View.GONE
            val ssid = popupView.findViewById<TextView>(R.id.ssid)
            ssid.visibility = View.GONE
            val serverAddr = popupView.findViewById<EditText>(R.id.server_address)
            val serverPort = popupView.findViewById<EditText>(R.id.server_port)
            val sure = popupView.findViewById<Button>(R.id.sure)
            val cancel = popupView.findViewById<Button>(R.id.cancel)
            sure.setOnClickListener { it1 ->
                val addr = trimStr(serverAddr.text.toString())
                val port = trimStr(serverPort.text.toString())
                if (addr.isNullOrEmpty() || port.isNullOrEmpty()) {
                    Toast.makeText(context, context?.getString(R.string.input_tips), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    wifiConfig.option = 8
                    server.addr = addr
                    server.port = port.toInt()
                    wifiConfig.server = server
                    LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                    popupWindow?.dismiss()
                }
            }
            cancel.setOnClickListener {
                popupWindow?.dismiss()
            }
        }
        binding.get4gServer.setOnClickListener {
            LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
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

        when (Constant.BluetoothConfig.currentModel[0]) {
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_SLEEPO2, Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK, Bluetooth.MODEL_KIDSO2,
            Bluetooth.MODEL_OXYFIT, Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU, Bluetooth.MODEL_CMRING,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_OXYFIT_WPS, Bluetooth.MODEL_KIDSO2_WPS,
            Bluetooth.MODEL_SI_PO6 -> {
                infoViewModel = ViewModelProvider(this).get(OxyViewModel::class.java)
                (infoViewModel as OxyViewModel).initEvent(this)
                mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.swVersion}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\nfileList：${it.fileList}"
                }
            }
            Bluetooth.MODEL_CHECKO2 -> {
                infoViewModel = ViewModelProvider(this).get(OxyViewModel::class.java)
                (infoViewModel as OxyViewModel).initEvent(this)
                mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    var swVersion = it.swVersion.replace(".0", "")
                    swVersion = "1.$swVersion"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}$swVersion\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\nfileList：${it.fileList}"
                }
            }
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1,
            Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                infoViewModel = ViewModelProvider(this).get(Er1ViewModel::class.java)
                (infoViewModel as Er1ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_BPM -> {
                infoViewModel = ViewModelProvider(this).get(BpmViewModel::class.java)
                (infoViewModel as BpmViewModel).initEvent(this)
                mainViewModel.bpmInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.software_version)}${it.getFwVersion()}"
                }
            }
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                infoViewModel = ViewModelProvider(this).get(Bp2ViewModel::class.java)
                (infoViewModel as Bp2ViewModel).initEvent(this)
                mainViewModel.bp2Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fmV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_BP2W -> {
                infoViewModel = ViewModelProvider(this).get(Bp2wViewModel::class.java)
                (infoViewModel as Bp2wViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LE_BP2W -> {
                infoViewModel = ViewModelProvider(this).get(LpBp2wViewModel::class.java)
                (infoViewModel as LpBp2wViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "WiFi固件版本：${mainViewModel.wifiVersion}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                infoViewModel = ViewModelProvider(this).get(Er2ViewModel::class.java)
                (infoViewModel as Er2ViewModel).initEvent(this)
                mainViewModel.er2Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwVersion}\n" +
                            "sn：${it.serialNum}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC66B, Bluetooth.MODEL_OXYSMART,
            Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_10AW,
            Bluetooth.MODEL_PF_10AW1, Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20, Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_PC_60NW_NO_SN, Bluetooth.MODEL_PC60NW_BLE,
            Bluetooth.MODEL_PC60NW_WPS, Bluetooth.MODEL_S5W, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS,
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_SP20_WPS -> {
                mainViewModel.boInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_name)}${it.deviceName}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hardwareV}\n" +
                            "${context?.getString(R.string.software_version)}${it.softwareV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE, Bluetooth.MODEL_PC80B_BLE2 -> {
                infoViewModel = ViewModelProvider(this).get(Pc80bViewModel::class.java)
                (infoViewModel as Pc80bViewModel).initEvent(this)
                mainViewModel.pc80bInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hardwareV}\n" +
                            "${context?.getString(R.string.software_version)}${it.softwareV}"
                }
            }
            Bluetooth.MODEL_PC100 -> {
                mainViewModel.pc100Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_name)}${it.deviceName}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hardwareV}\n" +
                            "${context?.getString(R.string.software_version)}${it.softwareV}\n" +
                            "sn：${it.sn}"
                }
            }
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                infoViewModel = ViewModelProvider(this).get(LewViewModel::class.java)
                (infoViewModel as LewViewModel).initEvent(this)
                mainViewModel.lewInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_mode)}${it.deviceModeMess}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}"
                }
            }
            Bluetooth.MODEL_AOJ20A -> {
                infoViewModel = ViewModelProvider(this).get(Aoj20aViewModel::class.java)
                (infoViewModel as Aoj20aViewModel).initEvent(this, requireContext())
                mainViewModel.aoj20aInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "$it"
                }
            }
            Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS -> {
                infoViewModel = ViewModelProvider(this).get(CheckmePodViewModel::class.java)
                (infoViewModel as CheckmePodViewModel).initEvent(this, requireContext())
                mainViewModel.checkmePodInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.swVersion}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4 -> {
                infoViewModel = ViewModelProvider(this).get(PulsebitViewModel::class.java)
                (infoViewModel as PulsebitViewModel).initEvent(this)
                mainViewModel.pulsebitInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.swVersion}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC_68B -> {
                infoViewModel = ViewModelProvider(this).get(Pc68bViewModel::class.java)
                (infoViewModel as Pc68bViewModel).initEvent(this)
                mainViewModel.boInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_name)}${it.deviceName}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hardwareV}\n" +
                            "${context?.getString(R.string.software_version)}${it.softwareV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE, Bluetooth.MODEL_GM_300SNT,
            Bluetooth.MODEL_PC200_BLE, Bluetooth.MODEL_CMI_PC303 -> {
                mainViewModel.pc300Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_name)}${it.deviceName}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hardwareV}\n" +
                            "${context?.getString(R.string.software_version)}${it.softwareV}"
                }
            }
            Bluetooth.MODEL_CHECKME_LE -> {
                infoViewModel = ViewModelProvider(this).get(CheckmeLeViewModel::class.java)
                (infoViewModel as CheckmeLeViewModel).initEvent(this)
                mainViewModel.checkmeLeInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.swVersion}\n" +
                            "sn：${it.sn}"
                }
            }
            Bluetooth.MODEL_CHECKME -> {
                infoViewModel = ViewModelProvider(this).get(CheckmeViewModel::class.java)
                (infoViewModel as CheckmeViewModel).initEvent(this)
                mainViewModel.checkmeInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwVersion}\n" +
                            "${context?.getString(R.string.software_version)}${it.swVersion}\n" +
                            "sn：${it.sn}"
                }
            }
            Bluetooth.MODEL_LEM -> {
                mainViewModel.lemInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "$it"
                }
            }
            Bluetooth.MODEL_LES1 -> {
                infoViewModel = ViewModelProvider(this).get(LeS1ViewModel::class.java)
                (infoViewModel as LeS1ViewModel).initEvent(this, requireContext())
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_LPM311 -> {
                infoViewModel = ViewModelProvider(this).get(Lpm311ViewModel::class.java)
                (infoViewModel as Lpm311ViewModel).initEvent(this)
            }
            Bluetooth.MODEL_POCTOR_M3102 -> {
                infoViewModel = ViewModelProvider(this).get(PoctorM3102ViewModel::class.java)
                (infoViewModel as PoctorM3102ViewModel).initEvent(this, requireContext())
            }
            Bluetooth.MODEL_BIOLAND_BGM -> {
                infoViewModel = ViewModelProvider(this).get(BiolandBgmViewModel::class.java)
                (infoViewModel as BiolandBgmViewModel).initEvent(this, requireContext())
                mainViewModel.biolandInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.software_version)}${it.version}\n" +
                            "${context?.getString(R.string.battery)}${it.battery} %\nsn：${it.sn}"
                }
            }
            Bluetooth.MODEL_ER3 -> {
                infoViewModel = ViewModelProvider(this).get(Er3ViewModel::class.java)
                (infoViewModel as Er3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                infoViewModel = ViewModelProvider(this).get(LepodViewModel::class.java)
                (infoViewModel as LepodViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_VTM01 -> {
                infoViewModel = ViewModelProvider(this).get(Vtm01ViewModel::class.java)
                (infoViewModel as Vtm01ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_BTP -> {
                infoViewModel = ViewModelProvider(this).get(BtpViewModel::class.java)
                (infoViewModel as BtpViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_ECN -> {
                infoViewModel = ViewModelProvider(this).get(EcnViewModel::class.java)
                (infoViewModel as EcnViewModel).initEvent(this)
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                infoViewModel = ViewModelProvider(this).get(VentilatorViewModel::class.java)
                (infoViewModel as VentilatorViewModel).initEvent(this, requireContext())
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                binding.setWifiConfig.visibility = View.GONE
                binding.getWifiConfig.visibility = View.VISIBLE
                LpBleUtil.ventilatorGetVersionInfo(Constant.BluetoothConfig.currentModel[0])
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LP_BP3W -> {
                infoViewModel = ViewModelProvider(this).get(Bp3ViewModel::class.java)
                (infoViewModel as Bp3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                binding.setWifiConfig.visibility = View.GONE
                binding.getWifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LP_BP3C -> {
                infoViewModel = ViewModelProvider(this).get(Bp3ViewModel::class.java)
                (infoViewModel as Bp3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
                binding.serverConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_PF_10AW_1, Bluetooth.MODEL_PF_10BWS -> {
                infoViewModel = ViewModelProvider(this).get(Pf10Aw1ViewModel::class.java)
                (infoViewModel as Pf10Aw1ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                    "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_O2RING_S -> {
                infoViewModel = ViewModelProvider(this).get(OxyIIViewModel::class.java)
                (infoViewModel as OxyIIViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.hardware_version)}${it.hwV}\n" +
                            "${context?.getString(R.string.software_version)}${it.fwV}\n" +
                            "sn：${it.sn}\ncode：${it.branchCode}\n" +
                            "${context?.getString(R.string.battery)}${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_TMB_2088 -> {
                infoViewModel = ViewModelProvider(this).get(TmbViewModel::class.java)
                (infoViewModel as TmbViewModel).initEvent(this)
                mainViewModel.tmbInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "${context?.getString(R.string.device_name)}${it.name}\n" +
                            "制造商：${it.manufacturer}\n" +
                            "${context?.getString(R.string.hardware_version)}${it.hv}\n" +
                            "${context?.getString(R.string.software_version)}${it.fv}\n" +
                            "软件版本：${it.sv}\nsn：${it.serial}\n" +
                            "Device ID：${it.deviceId}\nUser ID：${it.userId}\n" +
                            "${context?.getString(R.string.battery)}${it.battery} %"
                }
            }
        }

        //----------------------------------------------------------------------------
        if (this@InfoFragment::infoViewModel.isInitialized) {
            infoViewModel.info.observe(viewLifecycleOwner) {
                if (it != null) {
                    binding.deviceInfo.text = it
                }
            }
            infoViewModel.reset.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, context?.getString(R.string.reset_success), Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.factoryReset.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, context?.getString(R.string.factory_reset_success), Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.factoryResetAll.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, context?.getString(R.string.factory_reset_all_success), Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.fileNames.observe(viewLifecycleOwner) {
                if (it != null) {
                    fileNames = it
                    Toast.makeText(context, "${context?.getString(R.string.get_file_list)} ${fileNames.size} ${context?.getString(R.string.files)}", Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.process.observe(viewLifecycleOwner) {
                if (it != null) {
                    process = it
                    if (process == 100) {
                        readFileProcess = "$readFileProcess$curFileName ${context?.getString(R.string.process)}100% \n"
                        binding.process.text = readFileProcess
                    } else {
                        binding.process.text = "$readFileProcess $curFileName ${context?.getString(R.string.process)}$process %"
                    }
                    mainViewModel._downloadTip.value = "${context?.getString(R.string.still_left)} ${fileNames.size} ${context?.getString(R.string.files)}\n$curFileName  \n${context?.getString(R.string.process)}$process %"
                }
            }
            infoViewModel.ecgData.observe(viewLifecycleOwner) {
                if (it != null) {
                    ecgList.add(it)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
                }
            }
            infoViewModel.analysisFile.observe(viewLifecycleOwner) {
                if (it != null) {
                    analysisFile.add(it)
                }
            }
            infoViewModel.oxyData.observe(viewLifecycleOwner) {
                if (it != null) {
                    oxyList.add(it)
                    oxyAdapter.setNewInstance(oxyList)
                    oxyAdapter.notifyDataSetChanged()
                }
            }
            infoViewModel.bpData.observe(viewLifecycleOwner) {
                if (it != null) {
                    bpList.add(it)
                    bpAdapter.setNewInstance(bpList)
                    bpAdapter.notifyDataSetChanged()
                }
            }
            infoViewModel.ecnData.observe(viewLifecycleOwner) {
                if (it != null) {
                    ecnList.add(it)
                    ecnAdapter.setNewInstance(ecnList)
                    ecnAdapter.notifyDataSetChanged()
                }
            }
            infoViewModel.readNextFile.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it) {
                        infoViewModel._readNextFile.value = false
                        if (binding.fileName.text.toString().isEmpty()) {
                            if (fileNames.size == 0) {
                                mAlertDialog?.dismiss()
                            } else {
                                fileNames.removeAt(0)
                                readFile()
                            }
                        } else {
                            mAlertDialog?.dismiss()
                        }
                    }
                }
            }
            infoViewModel.readFileError.observe(viewLifecycleOwner) {
                if (it != null) {
                    mAlertDialogCanCancel?.dismiss()
                    mAlertDialog?.dismiss()
                    Toast.makeText(context, context?.getString(R.string.read_error), Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.wifiDevice.observe(viewLifecycleOwner) {
                if (it != null) {
                    mAlertDialogCanCancel?.dismiss()
                    bp2wAdapter.setNewInstance(it.wifiList)
                    bp2wAdapter.notifyDataSetChanged()
                }
            }
            infoViewModel.wifiConfig.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3W
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R20
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R21
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R10
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R11
                        || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LERES) {
                        mAlertDialogCanCancel?.dismiss()
                        if (it.isWifiInitialized()) {
                            binding.wifiInfo.text = "${context?.getString(R.string.wifi)}${it.wifi.ssid}\n" +
                                    "${context?.getString(R.string.wifi_password)}${it.wifi.pwd}\n" +
                                    "${context?.getString(R.string.connect_state)}${when (it.wifi.state) {
                                        0 -> context?.getString(R.string.disconnect)
                                        1 -> context?.getString(R.string.connecting)
                                        2 -> context?.getString(R.string.connected)
                                        0xff -> context?.getString(R.string.password_error)
                                        0xfd -> context?.getString(R.string.not_found_ssid)
                                        else -> context?.getString(R.string.no_wifi)
                                    }}"
                        } else {
                            binding.wifiInfo.text = context?.getString(R.string.no_wifi)
                        }
                        if (it.isServerInitialized()) {
                            binding.wifiInfo.text = binding.wifiInfo.text.toString() + "\n${context?.getString(R.string.server_address)}${it.server.addr}\n" +
                                    "${context?.getString(R.string.server_port)}${it.server.port}\n" +
                                    "${context?.getString(R.string.connect_state)}${when (it.server.state) {
                                        0 -> context?.getString(R.string.disconnect)
                                        1 -> context?.getString(R.string.connecting)
                                        2 -> context?.getString(R.string.connected)
                                        0xff -> context?.getString(R.string.cannot_connect)
                                        else -> context?.getString(R.string.connect_error)
                                    }}"
                        } else {
                            binding.wifiInfo.text = binding.wifiInfo.text.toString() + "\n${context?.getString(R.string.no_server)}"
                        }
                    } else {
                        wifi?.let { w ->
                            w.state = it.wifi.state
                            binding.wifiInfo.text = "${context?.getString(R.string.wifi)}${w.ssid}\n" +
                                    "${context?.getString(R.string.wifi_password)}${w.pwd}\n" +
                                    "${context?.getString(R.string.connect_state)}${when (w.state) {
                                        0 -> context?.getString(R.string.disconnect)
                                        1 -> context?.getString(R.string.connecting)
                                        2 -> context?.getString(R.string.connected)
                                        0xff -> context?.getString(R.string.password_error)
                                        0xfd -> context?.getString(R.string.not_found_ssid)
                                        else -> context?.getString(R.string.no_wifi)
                                    }}"
                            if (((w.state == 0) && (it.wifi.ssid.isNotEmpty())) || w.state == 1) {
                                handler.postDelayed({
                                    LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
                                }, 3000)
                            } else {
                                mAlertDialogCanCancel?.dismiss()
                            }
                        } ?: kotlin.run {
                            binding.wifiInfo.text = context?.getString(R.string.wifi_tips)
                        }
                    }
                    if (it.isServerInitialized()) {
                        binding.serverInfo.text = "${context?.getString(R.string.server_address)}${it.server.addr}\n" +
                                "${context?.getString(R.string.server_port)}${it.server.port}\n" +
                                "${context?.getString(R.string.connect_state)}${when (it.server.state) {
                                    0 -> context?.getString(R.string.disconnect)
                                    1 -> context?.getString(R.string.connecting)
                                    2 -> context?.getString(R.string.connected)
                                    0xff -> context?.getString(R.string.cannot_connect)
                                    else -> context?.getString(R.string.connect_error)
                                }}"
                    }
                }
            }
            infoViewModel.noWifi.observe(viewLifecycleOwner) {
                if (it) {
                    if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3C) {
                        Toast.makeText(context, context?.getString(R.string.no_server), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context?.getString(R.string.no_wifi), Toast.LENGTH_SHORT).show()
                    }
                    if (wifi == null) {
                        binding.wifiInfo.text = context?.getString(R.string.wifi_tips)
                    }
                }
            }
            infoViewModel.toast.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 睡眠算法
    private fun sleepAlg(oxyData: OxyData) {
        AlgorithmUtil.sleepAlgInit(oxyData.startTime.toInt())
        val filePath = "${BleServiceHelper.BleServiceHelper.rawFolder?.get(Bluetooth.MODEL_O2RING)}/sleep_result_${oxyData.fileName}.txt"
        val isSave = File(filePath).exists()
        Log.d("111111111", "oxyData : $oxyData")
        val prList = mutableListOf<Int>()
        val vectorList = mutableListOf<Int>()
        for (i in 0 until oxyData.spo2s.size) {
            prList.add(oxyData.hrs[i])
            vectorList.add(oxyData.motions[i])
            val status = AlgorithmUtil.sleepAlgMainPro(oxyData.hrs[i].toShort(), oxyData.motions[i])
            if (!isSave) {
                FileUtil.saveTextFile(
                    filePath,
                    "脉率: ${oxyData.hrs[i]}，三轴值: ${oxyData.motions[i]}，睡眠状态: ${when (status) {
                        0 -> "深睡眠"
                        1 -> "浅睡眠"
                        2 -> "快速眼动"
                        3 -> "清醒"
                        4 -> "准备睡眠阶段"
                        else -> "未得出结果"
                    }}\n",
                    true)
            }
        }
//        AlgorithmUtil.sleepAlgMain(prList.toIntArray(), vectorList.toIntArray())
        val result = AlgorithmUtil.sleepAlgGetResult()
        if (!isSave) {
            val len = result[7]
            var temp = ""
            for (i in 0 until len) {
                temp += "${when (result[8+i]) {
                        0 -> "深睡眠"
                        1 -> "浅睡眠"
                        2 -> "快速眼动"
                        3 -> "清醒"
                        4 -> "准备睡眠阶段"
                        else -> "未得出结果"
                        }}, "
            }
            FileUtil.saveTextFile(
                filePath,
                "总睡眠时间: ${DataConvert.getEcgTimeStr(result[0])}\n" +
                        "深睡时间: ${DataConvert.getEcgTimeStr(result[1])}\n" +
                        "浅睡时间: ${DataConvert.getEcgTimeStr(result[2])}\n" +
                        "快速眼动时间: ${DataConvert.getEcgTimeStr(result[3])}\n" +
                        "清醒次数: ${result[4]}\n" +
                        "准备睡眠时间: ${DataConvert.getEcgTimeStr(result[5])}\n" +
                        "入睡时间点: ${result[6]}\n" +
                        "睡眠分期结果数组长度: ${result[7]}\n" +
                        "出睡时间点: ${result[8+len]}\n" +
                        "深睡比例: ${result[9+len]} %\n" +
                        "浅睡比例: ${result[10+len]} %\n" +
                        "快速眼动比例: ${result[11+len]} %\n" +
                        "睡眠分期结果: $temp",
                true)
        }
    }

    private fun refreshUI() {
        analysisFile.clear()
        ecgList.clear()
        ecgAdapter.setNewInstance(ecgList)
        ecgAdapter.notifyDataSetChanged()
        oxyList.clear()
        oxyAdapter.setNewInstance(oxyList)
        oxyAdapter.notifyDataSetChanged()
        bpList.clear()
        bpAdapter.setNewInstance(bpList)
        bpAdapter.notifyDataSetChanged()
        bp2wAdapter.setList(null)
        bp2wAdapter.notifyDataSetChanged()
        ecnList.clear()
        ecnAdapter.setNewInstance(ecnList)
        ecnAdapter.notifyDataSetChanged()
    }

    private fun getEcgData(recordingTime: Long, fileName: String, wave: ByteArray, shortData: ShortArray, duration: Int) : EcgData {
        val data = EcgData()
        data.recordingTime = recordingTime
        data.fileName = fileName
        data.data = wave
        data.shortData = shortData
        data.duration = duration
        return data
    }

    private fun readFile() {
        curFileName = if (binding.fileName.text.toString().isNotEmpty()) {
            trimStr(binding.fileName.text.toString())
        } else {
            if (fileNames.size == 0) {
                for (ecg in ecgList) {
                    for (analysis in analysisFile) {
                        val itFileName = ecg.fileName.replace("R", "")
                        val dataFileName = analysis.fileName.replace("a", "")
                        if (itFileName == dataFileName) {
                            ecg.isMotion = analysis.isMotion
                            break
                        }
                    }
                }
                ecgAdapter.notifyDataSetChanged()
                for (data in oxyList) {
                    sleepAlg(data)
                }
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BTP
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ECN) {
                    mAlertDialogCanCancel?.dismiss()
                } else {
                    mAlertDialog?.dismiss()
                }
                LogcatHelper.getInstance(context).stop()
                return
            }
            fileNames[0]
        }
        if (curFileName.contains("MKFS")) {
            fileNames.removeAt(0)
            readFile()
        } else {
            val offset = DownloadHelper.readFile(Constant.BluetoothConfig.currentModel[0], "", curFileName)
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING_S) {
                LpBleUtil.readFile( "", curFileName, Constant.BluetoothConfig.currentModel[0], offset.size, fileType)
            } else {
                LpBleUtil.readFile("", curFileName, Constant.BluetoothConfig.currentModel[0], offset.size)
            }
            binding.sendCmd.text = LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
    }

}