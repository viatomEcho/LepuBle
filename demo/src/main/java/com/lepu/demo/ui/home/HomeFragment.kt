package com.lepu.demo.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.demo.MainActivity
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.singleConnect
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.currentModel
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.databinding.FragmentHomeBinding
import com.lepu.demo.util.CollectUtil
import com.lepu.demo.util.DialogUtil
import com.lepu.demo.util.ToastUtil


class HomeFragment : Fragment(R.layout.fragment_home){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val binding: FragmentHomeBinding by binding()

    private lateinit var adapter: DeviceAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }
    private fun initView(){

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
            mainViewModel._scanning.value = true
            binding.rcv.visibility = View.VISIBLE
        }

        binding.disconnect.setOnClickListener{
            LpBleUtil.disconnect(false)
        }
        binding.reconnectByName.setOnClickListener {

            mainViewModel.curBluetooth.value?.let { it1 ->
                LpBleUtil.reconnect(currentModel[0], it1.deviceName)
            }
        }
        binding.reconnectByAddress.setOnClickListener {
            mainViewModel.curBluetooth.value?.let { it1 ->
                LpBleUtil.reconnectByMac(currentModel[0], it1.deviceMacAddress)
            }
        }

        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.rcv.layoutManager = this
        }
        adapter = DeviceAdapter(R.layout.device_item, null).apply {
            binding.rcv.adapter = this
        }

        adapter.setOnItemClickListener { adapter, view, position ->
            (adapter.getItem(position) as Bluetooth).let {

                activity?.applicationContext?.let { it1 ->
                    if (singleConnect) currentModel[0] = it.model
                    LpBleUtil.setInterface(it.model, singleConnect)
                    activity?.lifecycle?.addObserver(BIOL(activity as MainActivity, Constant.BluetoothConfig.SUPPORT_MODELS))


                    LpBleUtil.connect(it1, it)
                    ToastUtil.showToast(activity, "正在连接蓝牙")
                    LpBleUtil.stopScan()
                    binding.rcv.visibility = View.GONE

                    mainViewModel._curBluetooth.value = DeviceEntity(it.name, it.macAddr, it.model)

                }

            }
        }

        mainViewModel.bleState.observe(viewLifecycleOwner, {
            binding.bleState.text = "连接状态：$it"
        })

        mainViewModel.curBluetooth.observe(viewLifecycleOwner, {
            binding.bleDevice.text = "当前蓝牙设备：\n" + it!!.deviceName + " " + it!!.deviceMacAddress
        })

    }

    var splitDevice: ArrayList<Bluetooth> = arrayListOf()

    private fun splitDevices(name: String) {
        splitDevice.clear()
        for (b in BluetoothController.getDevices()) {
            if (b.name.contains(name, true)) {
                splitDevice.add(b)
            }
        }

        adapter.setNewInstance(splitDevice)
        adapter.notifyDataSetChanged()

    }


    private fun initEvent(){
        //扫描通知
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this,  {
//                adapter.setNewInstance(BluetoothController.getDevices())
//                adapter.notifyDataSetChanged()
                splitDevices(binding.bleSplit.text.toString())
            })



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