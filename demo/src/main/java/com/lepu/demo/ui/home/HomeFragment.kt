package com.lepu.demo.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.le.ScanRecord
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LpBleCmd
import com.lepu.blepro.ble.cmd.ResponseError
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.vals.bleRssi
import com.lepu.demo.MainActivity
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ui.adapter.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.PairDevice
import com.lepu.demo.ui.adapter.StringAdapter
import com.lepu.demo.config.Constant
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.singleConnect
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.currentModel
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.databinding.FragmentHomeBinding
import com.lepu.demo.util.CollectUtil
import com.lepu.demo.util.DialogUtil
import com.lepu.demo.util.ToastUtil
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeFragment : Fragment(R.layout.fragment_home){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val binding: FragmentHomeBinding by binding()

    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var numberAdapter: StringAdapter
    var mAlertDialog: AlertDialog? = null
    private var isPairing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }
    private fun initView(){

        mAlertDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage(context?.getString(R.string.handling))
            .create()

        activity?.let {  activity ->
            CollectUtil.getInstance(activity.applicationContext).let { collectUtil ->
                binding.duration.text = collectUtil.currentCollectDuration.toString()

                binding.duration.setOnClickListener {
                    if(collectUtil.isTasking) return@setOnClickListener
                    showDialog(activity)
                }
                binding.duration.setOnClickListener {
                    if(collectUtil.isTasking) return@setOnClickListener
                    showDialog(activity)
                }
            }
        }

        binding.bleSplitBtn.setOnClickListener {
            splitDevices(binding.bleSplit.text.toString())
        }

        binding.scan.setOnClickListener {
            mainViewModel._scanning.value = !mainViewModel._scanning.value!!
            binding.rcv.visibility = View.VISIBLE
        }

        binding.disconnect.setOnClickListener {
            LpBleUtil.disconnect(false)
        }
        binding.disconnect2.setOnClickListener {
            isPairing = false
            LpBleUtil.disconnect(false)
            mainViewModel._scanning.value = true
            binding.rcv.visibility = View.VISIBLE
        }

        binding.reconnectByName.setOnClickListener {
            mainViewModel.curBluetooth.value?.let { it1 ->
                LpBleUtil.reconnect(intArrayOf(currentModel[0]), arrayOf(it1.deviceName))
            }
        }
        binding.reconnectByAddress.setOnClickListener {
            mainViewModel.curBluetooth.value?.let { it1 ->
                LpBleUtil.reconnectByMac(currentModel[0], it1.deviceMacAddress)
            }
        }
        binding.needPair.isChecked = Constant.BluetoothConfig.needPair
        binding.needPair.setOnCheckedChangeListener { buttonView, isChecked ->
            if (Constant.BluetoothConfig.splitType >= 10) {
                binding.needPair.isChecked = false
                Toast.makeText(context, context?.getString(R.string.cannot_pair_connect), Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            Constant.BluetoothConfig.needPair = isChecked
            LpBleUtil.setNeedPair(Constant.BluetoothConfig.needPair)
        }
        binding.scanByName.setOnClickListener {
            LpBleUtil.startScanByName(binding.scanName.text.toString())
        }
        binding.scanByAddress.setOnClickListener {
            LpBleUtil.startScanByAddress(binding.scanAddress.text.toString())
        }
        ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf(context?.getString(R.string.all), "BP2", "ER1", "VBeat", "HHM1", "DuoEK", "HHM2", "HHM3", "ER2", "O2", "PC", "ER3")
        ).apply {
            binding.deviceTypeSpinner.adapter = this
        }
        binding.deviceTypeSpinner.setSelection(Constant.BluetoothConfig.splitType)
        binding.deviceTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Constant.BluetoothConfig.splitType = position
                splitDevices(binding.bleSplit.text.toString())
                if (position >= 10) {
                    Constant.BluetoothConfig.needPair = false
                    binding.needPair.isChecked = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        GridLayoutManager(context, 6).apply {
            binding.numberLayout.layoutManager = this
        }
        numberAdapter = StringAdapter(R.layout.string_item,
            arrayListOf("0", "1", "2", "3", "4", "${context?.getString(R.string.delete)}", "5", "6", "7", "8", "9", "${context?.getString(R.string.clear)}")
        ).apply {
            binding.numberLayout.adapter = this
        }
        numberAdapter.setOnItemClickListener { adapter, view, position ->
            val temp = binding.bleSplit.text.toString()
            when (position) {
                5 -> {
                    if (temp.isNotEmpty()) {
                        binding.bleSplit.setText(temp.substring(0, temp.length - 1))
                    }
                }
                11 -> binding.bleSplit.setText("")
                else -> {
                    binding.bleSplit.setText(temp + adapter.data[position])
                }
            }
            splitDevices(binding.bleSplit.text.toString())
        }
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.rcv.layoutManager = this
        }
        deviceAdapter = DeviceAdapter(R.layout.device_item, null).apply {
            binding.rcv.adapter = this
        }
        deviceAdapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bluetooth).let {

                activity?.applicationContext?.let { it1 ->
                    LpBleUtil.disconnect(false)
                    if (singleConnect) currentModel[0] = it.model
                    LpBleUtil.setInterface(it.model, singleConnect)
                    activity?.lifecycle?.addObserver(BIOL(activity as MainActivity, Constant.BluetoothConfig.SUPPORT_MODELS))

                    mAlertDialog?.show()
                    // 扫描到需要升级的设备
                    if (it.name.contains("Updater")) {
                        LpBleUtil.connect(it1, it, false, true)
                    } else {
                        LpBleUtil.connect(it1, it)
                    }
                    ToastUtil.showToast(activity, context?.getString(R.string.connecting))
                    LpBleUtil.stopScan()
                    binding.rcv.visibility = View.GONE

                    mainViewModel._curBluetooth.value = DeviceEntity(it.name, it.macAddr, it.model)

                }

            }
        }
        binding.filterRssi.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                bleRssi  = -(progress * 0.6 + 40).toInt()
                binding.rssiFilterValue.text = "$bleRssi dBm"
                splitDevices(binding.bleSplit.text.toString())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.filterRssi.progress = (-40 - bleRssi) * 100 / 60
        binding.rssiFilterValue.text = "$bleRssi dBm"
        mainViewModel.bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.bleState.text = context?.getString(R.string.state_connect)
            } else {
                binding.bleState.text = context?.getString(R.string.state_disconnect)
            }
            if (it) {
                mAlertDialog?.dismiss()
            }
        }
        mainViewModel.curBluetooth.observe(viewLifecycleOwner) {
            binding.bleDevice.text = "${context?.getString(R.string.bluetooth_name)}${it!!.deviceName}\n" +
                    "${context?.getString(R.string.bluetooth_address)}${it.deviceMacAddress}"
        }
        binding.bleSplit.setText(Constant.BluetoothConfig.splitText)
        /*mainViewModel.oxyInfo.observe(viewLifecycleOwner) {
            if (it.branchCode == "2B010100") {
                if (binding.bleDevice.text.contains("code")) {
                    binding.bleDevice.text = binding.bleDevice.text.toString() + "\n${context?.getString(R.string.device_new_code)}${it.branchCode}"
                } else {
                    binding.bleDevice.text = binding.bleDevice.text.toString() + "\n${context?.getString(R.string.device_code)}${it.branchCode}"
                }
            } else {
                binding.bleDevice.text = binding.bleDevice.text.toString() + "\n${context?.getString(R.string.device_old_code)}${it.branchCode}"
            }
        }*/
    }

    var splitDevice: ArrayList<Bluetooth> = arrayListOf()

    private fun splitDevices(name: String) {
        splitDevice.clear()
        for (b in BluetoothController.getDevicesByRssi(bleRssi)) {
            if (b.name.contains(name, true) && b.name.contains(getNameFromDeviceType())) {
                splitDevice.add(b)
            }
        }
        splitDevice.sortWith { o1, o2 ->
            if (o2.rssi > o1.rssi) {
                return@sortWith 1
            } else {
                return@sortWith -1
            }
        }
        deviceAdapter.setNewInstance(splitDevice)
        deviceAdapter.notifyDataSetChanged()

    }
    private fun getNameFromDeviceType(): String {
        return when (Constant.BluetoothConfig.splitType) {
            0 -> ""
            1 -> "BP2"
            2 -> "ER1"
            3 -> "VBeat"
            4 -> "HHM1"
            5 -> "DuoEK"
            6 -> "HHM2"
            7 -> "HHM3"
            8 -> "ER2"
            9 -> "O2"
            10 -> "PC"
            11 -> "ER3"
            else -> ""
        }
    }
    private fun containModelFromDeviceType(model: Int): Boolean {
        when (Constant.BluetoothConfig.splitType) {
            0 -> return true
            1 -> return (model == Bluetooth.MODEL_BP2
                        || model == Bluetooth.MODEL_BP2A
                        || model == Bluetooth.MODEL_BP2T
                        || model == Bluetooth.MODEL_BP2W
                        || model == Bluetooth.MODEL_LP_BP2W)
            2 -> return model == Bluetooth.MODEL_ER1
            3 -> return model == Bluetooth.MODEL_ER1_N
            4 -> return model == Bluetooth.MODEL_HHM1
            5 -> return model == Bluetooth.MODEL_DUOEK
            6 -> return model == Bluetooth.MODEL_HHM2
            7 -> return model == Bluetooth.MODEL_HHM3
            8 -> return (model == Bluetooth.MODEL_ER2
                    || model == Bluetooth.MODEL_LP_ER2)
            9 -> return (model == Bluetooth.MODEL_O2RING
                    || model == Bluetooth.MODEL_BABYO2
                    || model == Bluetooth.MODEL_BABYO2N
                    || model == Bluetooth.MODEL_CHECKO2
                    || model == Bluetooth.MODEL_O2M
                    || model == Bluetooth.MODEL_SLEEPO2
                    || model == Bluetooth.MODEL_SNOREO2
                    || model == Bluetooth.MODEL_WEARO2
                    || model == Bluetooth.MODEL_KIDSO2
                    || model == Bluetooth.MODEL_KIDSO2_WPS
                    || model == Bluetooth.MODEL_O2M_WPS)
            10 -> return model == Bluetooth.MODEL_ER3
            else -> return false
        }
    }
    private fun initEvent(){
        //扫描通知
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this) {
//                adapter.setNewInstance(BluetoothController.getDevices())
//                adapter.notifyDataSetChanged()
                Constant.BluetoothConfig.splitText = binding.bleSplit.text.toString()
                splitDevices(Constant.BluetoothConfig.splitText)
            }
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observe(this) {
                mAlertDialog?.dismiss()
                val status = when (it) {
                    ConnectionObserver.REASON_UNKNOWN -> "EventBleDeviceDisconnectReason REASON_UNKNOWN"
                    ConnectionObserver.REASON_SUCCESS -> "EventBleDeviceDisconnectReason REASON_SUCCESS"
                    ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> "EventBleDeviceDisconnectReason REASON_TERMINATE_LOCAL_HOST"
                    ConnectionObserver.REASON_TERMINATE_PEER_USER -> "EventBleDeviceDisconnectReason REASON_TERMINATE_PEER_USER"
                    ConnectionObserver.REASON_LINK_LOSS -> "EventBleDeviceDisconnectReason REASON_LINK_LOSS"
                    ConnectionObserver.REASON_NOT_SUPPORTED -> "EventBleDeviceDisconnectReason REASON_NOT_SUPPORTED"
                    ConnectionObserver.REASON_TIMEOUT -> "EventBleDeviceDisconnectReason REASON_TIMEOUT"
                    else -> "EventBleDeviceDisconnectReason"
                }
//                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            }
        // 配对连接
        LiveEventBus.get<HashMap<String, Any>>(EventMsgConst.Discovery.EventDeviceFound_ScanRecord)
            .observe(this) {
                if (isPairing) return@observe
                val data = it as HashMap<String, Any>
                val b = data[EventMsgConst.Discovery.EventDeviceFound_Device] as Bluetooth
                val r = data[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] as ScanRecord
                if (!containModelFromDeviceType(b.model)) return@observe
                if (PairDevice.pairO2(r)) {
                    isPairing = true
                    LpBleUtil.disconnect(false)
                    if (singleConnect) currentModel[0] = b.model
                    LpBleUtil.setInterface(b.model, singleConnect)
                    activity?.lifecycle?.addObserver(BIOL(activity as MainActivity, Constant.BluetoothConfig.SUPPORT_MODELS))

                    mAlertDialog?.show()
                    if (b.name.contains("Updater")) {
                        LpBleUtil.connect(activity?.applicationContext!!, b, false, true)
                    } else {
                        LpBleUtil.connect(activity?.applicationContext!!, b)
                    }
                    ToastUtil.showToast(activity, context?.getString(R.string.connecting))
                    LpBleUtil.stopScan()
                    binding.rcv.visibility = View.GONE

                    mainViewModel._curBluetooth.value = DeviceEntity(b.name, b.macAddr, b.model)
                }
            }
        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError)
            .observe(this) {
                when (it.type) {
                    LpBleCmd.TYPE_FILE_NOT_FOUND -> Toast.makeText(context, context?.getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_FILE_READ_FAILED -> Toast.makeText(context, context?.getString(R.string.read_error), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_FILE_WRITE_FAILED -> Toast.makeText(context, context?.getString(R.string.write_file_error), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_FIRMWARE_UPDATE_FAILED -> Toast.makeText(context, context?.getString(R.string.software_upgrade_error), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_LANGUAGE_UPDATE_FAILED -> Toast.makeText(context, context?.getString(R.string.language_upgrade_error), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_PARAM_ILLEGAL -> Toast.makeText(context, context?.getString(R.string.param_illegal), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_PERMISSION_DENIED -> Toast.makeText(context, context?.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_DECRYPT_FAILED -> {
                        Toast.makeText(context, context?.getString(R.string.decrypt_failed), Toast.LENGTH_SHORT).show()
                        LpBleUtil.disconnect(false)
                    }
                    LpBleCmd.TYPE_DEVICE_BUSY -> Toast.makeText(context, context?.getString(R.string.device_busy), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_CMD_FORMAT_ERROR -> Toast.makeText(context, context?.getString(R.string.cmd_format_error), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_CMD_NOT_SUPPORTED -> Toast.makeText(context, context?.getString(R.string.cmd_not_support), Toast.LENGTH_SHORT).show()
                    LpBleCmd.TYPE_NORMAL_ERROR -> {
                        if (it.model == Bluetooth.MODEL_LERES
                            || it.model == Bluetooth.MODEL_R10
                            || it.model == Bluetooth.MODEL_R11
                            || it.model == Bluetooth.MODEL_R21
                            || it.model == Bluetooth.MODEL_R20) {
                            if (it.cmd == LpBleCmd.ENCRYPT) {
                                Toast.makeText(context, context?.getString(R.string.key_change_error), Toast.LENGTH_SHORT).show()
                                LpBleUtil.disconnect(false)
                            } else {
                                Toast.makeText(context, context?.getString(R.string.normal_error), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, context?.getString(R.string.normal_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun showDialog(activity: Activity){

        CollectUtil.getInstance(activity.applicationContext).let { collectUtil ->

            DialogUtil.showDurationDialog(activity, collectUtil.currentCollectDuration.toString()) {
                collectUtil.setCollectDurationAndType(if (it.isEmpty()) collectUtil.DEFAULT_DURATION else it.toLong())
                binding.duration.text = collectUtil.currentCollectDuration.toString()

            }
        }
    }

}