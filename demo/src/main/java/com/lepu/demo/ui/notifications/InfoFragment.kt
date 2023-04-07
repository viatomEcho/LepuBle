package com.lepu.demo.ui.notifications

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import androidx.lifecycle.ViewModelProvider
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.objs.Bluetooth
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
import com.lepu.demo.data.BpData
import com.lepu.demo.data.EcgData
import com.lepu.demo.data.EcnData
import com.lepu.demo.data.OxyData
import com.lepu.demo.databinding.FragmentInfoBinding
import com.lepu.demo.ui.adapter.*
import com.lepu.demo.util.FileUtil
import java.io.*

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
//        testEr3()
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
            .setMessage("是否继续读取文件?")
            .setPositiveButton("确定") { _, _ ->
                val offset = DownloadHelper.readFile(Constant.BluetoothConfig.currentModel[0], "", curFileName)
                LpBleUtil.continueReadFile(Constant.BluetoothConfig.currentModel[0], "", curFileName, offset.size)
                mAlertDialog?.show()
            }
            .setNegativeButton("取消") { _, _ ->
                LpBleUtil.cancelReadFile(Constant.BluetoothConfig.currentModel[0])
            }
            .create()

        if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ER1_N
            || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_HHM1) {
            mAlertDialog = AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setMessage("正在处理，请稍等...")
                .setNegativeButton("暂停") { _, _ ->
                    LpBleUtil.pauseReadFile(Constant.BluetoothConfig.currentModel[0])
                    mCancelDialog?.show()
                }
                .create()
        } else {
            mAlertDialog = AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setMessage("正在处理，请稍等...")
                .create()
        }
        mAlertDialogCanCancel = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setMessage("正在处理，请稍等...")
            .create()
        mainViewModel.downloadTip.observe(viewLifecycleOwner) {
            mAlertDialog?.setMessage("正在处理，请稍等... $it")
            mAlertDialogCanCancel?.setMessage("正在处理，请稍等... $it")
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
            mAlertDialogCanCancel?.setMessage("正在处理，请稍等...")
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
                    v.height = 800
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
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2W
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LE_BP2W) {
                    server1.visibility = View.GONE
                    serverAddr.visibility = View.GONE
                    serverPort.visibility = View.GONE
                    sure.setOnClickListener { it1 ->
                        val pass = trimStr(password.text.toString())
                        if (pass.isNullOrEmpty()) {
                            Toast.makeText(context, "请输入完整信息", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } else {
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
                            mAlertDialogCanCancel?.setMessage("正在处理，请稍等...")
                            mAlertDialogCanCancel?.show()
                        }
                    }
                } else {
                    sure.setOnClickListener { it1 ->
                        val pass = trimStr(password.text.toString())
                        val addr = trimStr(serverAddr.text.toString())
                        val port = trimStr(serverPort.text.toString())
                        if (pass.isNullOrEmpty() || addr.isNullOrEmpty() || port.isNullOrEmpty()) {
                            Toast.makeText(context, "请输入完整信息", Toast.LENGTH_SHORT).show()
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
                            mAlertDialogCanCancel?.setMessage("正在处理，请稍等...")
                            mAlertDialogCanCancel?.show()
                        }
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
            } else if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R20
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R21
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R10
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_R11
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LERES) {
                /*fileType++
                if (fileType > 2) {
                    fileType = 1
                }*/
                LpBleUtil.r20GetFileList(Constant.BluetoothConfig.currentModel[0], 1, 0)
            } else {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
            }
            binding.sendCmd.text = "send : ${LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])}"
        }
        // 读文件
        binding.readFile.setOnClickListener {
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
        // bp2 wifi
        binding.getWifiConfig.setOnClickListener {
            LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
        }
        binding.setWifiConfig.setOnClickListener {
            bp2wAdapter.setList(null)
            bp2wAdapter.notifyDataSetChanged()
            if (wifi != null) {
                mAlertDialogCanCancel?.setMessage("正在处理，请稍等...")
                mAlertDialogCanCancel?.show()
                wifiConfig.option = 3
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2W) {
                    // 源动健康测试服
                    server.addr = "34.209.148.123"
                    server.port = 7100
                    wifiConfig.server = server
                    LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                } else {
                    // 乐普健康测试服
                    server.addr = "212.129.241.54"
                    server.port = 7200
                    wifiConfig.server = server
                    LpBleUtil.bp2SetWifiConfig(Constant.BluetoothConfig.currentModel[0], wifiConfig)
                }
            } else {
                Toast.makeText(context, "请先完成首次配置WiFi，设置成功后连接其他设备可直接配置WiFi。", Toast.LENGTH_SHORT).show()
            }
        }
        binding.set4gServer.setOnClickListener {
            val popupView = View.inflate(context, R.layout.popup_window, null)
            if (popupWindow == null) {
                popupWindow = PopupWindow(context)
            }
            popupWindow?.let { v ->
                v.width = ViewGroup.LayoutParams.MATCH_PARENT
                v.height = 800
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
                    Toast.makeText(context, "请输入完整信息", Toast.LENGTH_SHORT).show()
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
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_CHECKO2,
            Bluetooth.MODEL_SLEEPO2, Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK, Bluetooth.MODEL_KIDSO2,
            Bluetooth.MODEL_OXYFIT, Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_BBSM_S1,
            Bluetooth.MODEL_BBSM_S2, Bluetooth.MODEL_OXYU, Bluetooth.MODEL_CMRING,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_OXYFIT_WPS, Bluetooth.MODEL_KIDSO2_WPS -> {
                infoViewModel = ViewModelProvider(this).get(OxyViewModel::class.java)
                (infoViewModel as OxyViewModel).initEvent(this)
                mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}\nfileList：${it.fileList}"
                }
            }
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1,
            Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3 -> {
                infoViewModel = ViewModelProvider(this).get(Er1ViewModel::class.java)
                (infoViewModel as Er1ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_BPM -> {
                infoViewModel = ViewModelProvider(this).get(BpmViewModel::class.java)
                (infoViewModel as BpmViewModel).initEvent(this)
                mainViewModel.bpmInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "版本：${it.getFwVersion()}"
                }
            }
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                infoViewModel = ViewModelProvider(this).get(Bp2ViewModel::class.java)
                (infoViewModel as Bp2ViewModel).initEvent(this)
                mainViewModel.bp2Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fmV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_BP2W -> {
                infoViewModel = ViewModelProvider(this).get(Bp2wViewModel::class.java)
                (infoViewModel as Bp2wViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                binding.setWifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LE_BP2W -> {
                infoViewModel = ViewModelProvider(this).get(LpBp2wViewModel::class.java)
                (infoViewModel as LpBp2wViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                binding.setWifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                infoViewModel = ViewModelProvider(this).get(Er2ViewModel::class.java)
                (infoViewModel as Er2ViewModel).initEvent(this)
                mainViewModel.er2Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.fwVersion}\nsn：${it.serialNum}\ncode：${it.branchCode}"
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
                    binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE, Bluetooth.MODEL_PC80B_BLE2 -> {
                infoViewModel = ViewModelProvider(this).get(Pc80bViewModel::class.java)
                (infoViewModel as Pc80bViewModel).initEvent(this)
                mainViewModel.pc80bInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}"
                }
            }
            Bluetooth.MODEL_PC100 -> {
                mainViewModel.pc100Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}\nsn：${it.sn}"
                }
            }
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                infoViewModel = ViewModelProvider(this).get(LewViewModel::class.java)
                (infoViewModel as LewViewModel).initEvent(this)
                mainViewModel.lewInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "设备模式：${it.deviceModeMess}\n硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}"
                }
            }
            Bluetooth.MODEL_AOJ20A -> {
                infoViewModel = ViewModelProvider(this).get(Aoj20aViewModel::class.java)
                (infoViewModel as Aoj20aViewModel).initEvent(this)
                mainViewModel.aoj20aInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "$it"
                }
            }
            Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_CHECKME_POD_WPS -> {
                infoViewModel = ViewModelProvider(this).get(CheckmePodViewModel::class.java)
                (infoViewModel as CheckmePodViewModel).initEvent(this)
                mainViewModel.checkmePodInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PULSEBITEX, Bluetooth.MODEL_HHM4, Bluetooth.MODEL_CHECKME -> {
                infoViewModel = ViewModelProvider(this).get(PulsebitViewModel::class.java)
                (infoViewModel as PulsebitViewModel).initEvent(this)
                mainViewModel.pulsebitInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC_68B -> {
                infoViewModel = ViewModelProvider(this).get(Pc68bViewModel::class.java)
                (infoViewModel as Pc68bViewModel).initEvent(this)
                mainViewModel.boInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE, Bluetooth.MODEL_GM_300SNT,
            Bluetooth.MODEL_PC200_BLE -> {
                mainViewModel.pc300Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "设备名称：${it.deviceName}\n硬件版本：${it.hardwareV}\n固件版本：${it.softwareV}"
                }
            }
            Bluetooth.MODEL_CHECKME_LE -> {
                infoViewModel = ViewModelProvider(this).get(CheckmeLeViewModel::class.java)
                (infoViewModel as CheckmeLeViewModel).initEvent(this)
                mainViewModel.checkmeLeInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwVersion}\n固件版本：${it.swVersion}\nsn：${it.sn}"
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
                (infoViewModel as LeS1ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_LPM311 -> {
                infoViewModel = ViewModelProvider(this).get(Lpm311ViewModel::class.java)
                (infoViewModel as Lpm311ViewModel).initEvent(this)
            }
            Bluetooth.MODEL_POCTOR_M3102 -> {
                infoViewModel = ViewModelProvider(this).get(PoctorM3102ViewModel::class.java)
                (infoViewModel as PoctorM3102ViewModel).initEvent(this)
            }
            Bluetooth.MODEL_BIOLAND_BGM -> {
                infoViewModel = ViewModelProvider(this).get(BiolandBgmViewModel::class.java)
                (infoViewModel as BiolandBgmViewModel).initEvent(this)
                mainViewModel.biolandInfo.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "版本：${it.version}\n电量：${it.battery} %\nsn：${it.sn}"
                }
            }
            Bluetooth.MODEL_ER3 -> {
                infoViewModel = ViewModelProvider(this).get(Er3ViewModel::class.java)
                (infoViewModel as Er3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_LEPOD -> {
                infoViewModel = ViewModelProvider(this).get(LepodViewModel::class.java)
                (infoViewModel as LepodViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_VTM01 -> {
                infoViewModel = ViewModelProvider(this).get(Vtm01ViewModel::class.java)
                (infoViewModel as Vtm01ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
            }
            Bluetooth.MODEL_BTP -> {
                infoViewModel = ViewModelProvider(this).get(BtpViewModel::class.java)
                (infoViewModel as BtpViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
            }
            Bluetooth.MODEL_ECN -> {
                infoViewModel = ViewModelProvider(this).get(EcnViewModel::class.java)
                (infoViewModel as EcnViewModel).initEvent(this)
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                infoViewModel = ViewModelProvider(this).get(R20ViewModel::class.java)
                (infoViewModel as R20ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                LpBleUtil.r20GetVersionInfo(Constant.BluetoothConfig.currentModel[0])
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LP_BP3W -> {
                infoViewModel = ViewModelProvider(this).get(Bp3ViewModel::class.java)
                (infoViewModel as Bp3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
                binding.wifiConfig.visibility = View.VISIBLE
                binding.getWifiConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_LP_BP3C -> {
                infoViewModel = ViewModelProvider(this).get(Bp3ViewModel::class.java)
                (infoViewModel as Bp3ViewModel).initEvent(this)
                mainViewModel.er1Info.observe(viewLifecycleOwner) {
                    binding.info.text = "$it"
                    binding.deviceInfo.text = "硬件版本：${it.hwV}\n固件版本：${it.fwV}\nsn：${it.sn}\ncode：${it.branchCode}\n电量：${mainViewModel._battery.value}"
                }
                binding.serverConfig.visibility = View.VISIBLE
                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
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
                    Toast.makeText(context, "复位成功", Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.factoryReset.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, "恢复出厂设置成功", Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.factoryResetAll.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, "恢复生产状态成功", Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.fileNames.observe(viewLifecycleOwner) {
                if (it != null) {
                    fileNames = it
                    Toast.makeText(context, "获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            }
            infoViewModel.process.observe(viewLifecycleOwner) {
                if (it != null) {
                    process = it
                    if (process == 100) {
                        readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                        binding.process.text = readFileProcess
                    } else {
                        binding.process.text = "$readFileProcess $curFileName 读取进度: $process %"
                    }
                    mainViewModel._downloadTip.value = "还剩${fileNames.size}个文件 \n$curFileName  \n读取进度: $process %"
                }
            }
            infoViewModel.ecgData.observe(viewLifecycleOwner) {
                if (it != null) {
                    ecgList.add(it)
                    ecgAdapter.setNewInstance(ecgList)
                    ecgAdapter.notifyDataSetChanged()
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
                    mAlertDialog?.dismiss()
                    Toast.makeText(context, "读文件出错", Toast.LENGTH_SHORT).show()
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
                    wifi?.let { w ->
                        w.state = it.wifi.state
                        binding.wifiInfo.text = "热点：${w.ssid}\n密码：${w.pwd}\n连接状态：${
                            when (w.state) {
                                0 -> "断开"
                                1 -> "连接中"
                                2 -> "已连接"
                                0xff -> "密码错误"
                                0xfd -> "找不到SSID"
                                else -> "未配置WiFi"
                            }
                        }"
                        if (((w.state == 0) && (it.wifi.ssid.isNotEmpty())) || w.state == 1) {
                            handler.postDelayed({
                                LpBleUtil.bp2GetWifiConfig(Constant.BluetoothConfig.currentModel[0])
                            }, 1000)
                        } else {
                            mAlertDialogCanCancel?.dismiss()
                        }
                    } ?: kotlin.run {
                        binding.wifiInfo.text = "注意：请先完成首次配置WiFi，设置成功后连接其他设备可直接配置WiFi。"
                    }
                    if (it.isServerInitialized()) {
                        binding.serverInfo.text = "服务器地址：${it.server.addr}\n端口号：${it.server.port}\n连接状态：${
                            when (it.server.state) {
                                0 -> "断开"
                                1 -> "连接中"
                                2 -> "已连接"
                                0xff -> "无法连接"
                                else -> "连接错误"
                            }
                        }"
                    }
                    if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3W) {
                        binding.wifiInfo.visibility = View.GONE
                    }
                }
            }
            infoViewModel.noWifi.observe(viewLifecycleOwner) {
                if (it) {
                    if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_LP_BP3C) {
                        Toast.makeText(context, "未配置服务器", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "未配置WiFi", Toast.LENGTH_SHORT).show()
                    }
                    if (wifi == null) {
                        binding.wifiInfo.text = "注意：请先完成首次配置WiFi，设置成功后连接其他设备可直接配置WiFi。"
                    }
                }
            }
        }
    }

    private fun refreshUI() {
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
                if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BTP
                    || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_ECN) {
                    mAlertDialogCanCancel?.dismiss()
                } else {
                    mAlertDialog?.dismiss()
                }
                return
            }
            fileNames[0]
        }
        val offset = DownloadHelper.readFile(Constant.BluetoothConfig.currentModel[0], "", curFileName)
        LpBleUtil.readFile("", curFileName, Constant.BluetoothConfig.currentModel[0], offset.size)
        binding.sendCmd.text = LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
    }

}