package com.lepu.demo.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.le.ScanRecord
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.vals.bleRssi
import com.lepu.demo.DeviceFactoryDataActivity
import com.lepu.demo.MainActivity
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.ble.PairDevice
import com.lepu.demo.ble.StringAdapter
import com.lepu.demo.cofig.Constant
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.singleConnect
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.currentModel
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.databinding.FragmentHomeBinding
import com.lepu.demo.util.CollectUtil
import com.lepu.demo.util.DialogUtil
import com.lepu.demo.util.ToastUtil
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeFragment : Fragment(R.layout.fragment_home){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val binding: FragmentHomeBinding by binding()

    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var deviceTypeAdapter: ArrayAdapter<String>
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
            .setMessage("正在处理，请稍等...")
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
        binding.needPair.setOnClickListener {
            if (Constant.BluetoothConfig.splitType == 0 || Constant.BluetoothConfig.splitType == 6) {
                binding.needPair.isChecked = false
                Toast.makeText(context, "该设备类型不支持配对连接！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Constant.BluetoothConfig.needPair = binding.needPair.isChecked
            LpBleUtil.setNeedPair(Constant.BluetoothConfig.needPair)
        }
        binding.scanByName.setOnClickListener {
            LpBleUtil.startScanByName(binding.scanName.text.toString())
        }
        binding.scanByAddress.setOnClickListener {
            LpBleUtil.startScanByAddress(binding.scanAddress.text.toString())
        }
        deviceTypeAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf("全部", "BP2", "ER1", "ER2", "DuoEK", "O2", "PC")
        ).apply {
            binding.deviceTypeSpinner.adapter = this
        }
        binding.deviceTypeSpinner.setSelection(Constant.BluetoothConfig.splitType)
        binding.deviceTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Constant.BluetoothConfig.splitType = position
                splitDevices(binding.bleSplit.text.toString())
                if (position == 0 || position == 6) {
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
            arrayListOf("0", "1", "2", "3", "4", "删除", "5", "6", "7", "8", "9", "清空")
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
                    LpBleUtil.connect(it1, it)
                    ToastUtil.showToast(activity, "正在连接蓝牙")
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
                binding.bleState.text = "连接状态：已连接"
            } else {
                binding.bleState.text = "连接状态：未连接"
            }
            if (it) {
                mAlertDialog?.dismiss()
            }
        }
        mainViewModel.curBluetooth.observe(viewLifecycleOwner) {
            binding.bleDevice.text = "蓝牙名：${it!!.deviceName}\n蓝牙地址：${it.deviceMacAddress}"
        }
        binding.bleSplit.setText(Constant.BluetoothConfig.splitText)
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
        when (Constant.BluetoothConfig.splitType) {
            0 -> return ""
            1 -> return "BP2"
            2 -> return "ER1"
            3 -> return "ER2"
            4 -> return "DuoEK"
            5 -> return "O2"
            6 -> return "PC"
            else -> return ""
        }
    }
    private fun containModelFromDeviceType(model: Int): Boolean {
        when (Constant.BluetoothConfig.splitType) {
            1 -> return (model == Bluetooth.MODEL_BP2
                        || model == Bluetooth.MODEL_BP2A
                        || model == Bluetooth.MODEL_BP2T
                        || model == Bluetooth.MODEL_BP2W
                        || model == Bluetooth.MODEL_LE_BP2W)
            2 -> return (model == Bluetooth.MODEL_ER1
                    || model == Bluetooth.MODEL_ER1_N
                    || model == Bluetooth.MODEL_HHM1)
            3 -> return (model == Bluetooth.MODEL_ER2
                    || model == Bluetooth.MODEL_LP_ER2)
            4 -> return (model == Bluetooth.MODEL_DUOEK
                    || model == Bluetooth.MODEL_HHM2
                    || model == Bluetooth.MODEL_HHM3)
            5 -> return return (model == Bluetooth.MODEL_O2RING
                    || model == Bluetooth.MODEL_BABYO2
                    || model == Bluetooth.MODEL_BABYO2N
                    || model == Bluetooth.MODEL_CHECKO2
                    || model == Bluetooth.MODEL_O2M
                    || model == Bluetooth.MODEL_SLEEPO2
                    || model == Bluetooth.MODEL_SNOREO2
                    || model == Bluetooth.MODEL_WEARO2
                    || model == Bluetooth.MODEL_SLEEPU
                    || model == Bluetooth.MODEL_OXYLINK
                    || model == Bluetooth.MODEL_KIDSO2
                    || model == Bluetooth.MODEL_OXYFIT
                    || model == Bluetooth.MODEL_OXYRING
                    || model == Bluetooth.MODEL_BBSM_S1
                    || model == Bluetooth.MODEL_BBSM_S2
                    || model == Bluetooth.MODEL_OXYU
                    || model == Bluetooth.MODEL_AI_S100
                    || model == Bluetooth.MODEL_O2M_WPS
                    || model == Bluetooth.MODEL_CMRING)
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
                    ConnectionObserver.REASON_UNKNOWN -> "连接失败 REASON_UNKNOWN"
                    ConnectionObserver.REASON_SUCCESS -> "连接失败 REASON_SUCCESS"
                    ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> "连接失败 REASON_TERMINATE_LOCAL_HOST"
                    ConnectionObserver.REASON_TERMINATE_PEER_USER -> "连接失败 REASON_TERMINATE_PEER_USER"
                    ConnectionObserver.REASON_LINK_LOSS -> "连接失败 REASON_LINK_LOSS"
                    ConnectionObserver.REASON_NOT_SUPPORTED -> "连接失败 REASON_NOT_SUPPORTED"
                    ConnectionObserver.REASON_TIMEOUT -> "连接失败 REASON_TIMEOUT"
                    else -> "连接失败"
                }
                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
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
                    LpBleUtil.connect(activity?.applicationContext!!, b)
                    ToastUtil.showToast(activity, "正在连接蓝牙")
                    LpBleUtil.stopScan()
                    binding.rcv.visibility = View.GONE

                    mainViewModel._curBluetooth.value = DeviceEntity(b.name, b.macAddr, b.model)
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