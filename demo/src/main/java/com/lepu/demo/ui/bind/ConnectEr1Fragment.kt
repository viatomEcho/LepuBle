package com.lepu.demo.ui.bind

import android.os.Bundle
import android.text.BoringLayout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.vals.er1Name
import com.lepu.blepro.vals.hasEr1
import com.lepu.demo.EventUI
import com.lepu.demo.R
import com.lepu.demo.ble.BleAdapter
import com.lepu.demo.ble.DeviceHelper
import com.lepu.demo.ui.scan.ScanViewModel

/**
 * A fragment representing a list of Items.
 */
class ConnectEr1Fragment : Fragment(), BleChangeObserver{



    private val scanViewModel: ScanViewModel by activityViewModels()

    private var currentModel: Int = 0

    private var modelIndex = 0

    private var isClear: Boolean = true
    private var isMultiply: Boolean = false

    private lateinit var adapter: BleAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            currentModel = it.getInt(ARG_CURRENT_MODEL)
            modelIndex = it.getInt(ARG_MODEL_INDEX)
            isClear = it.getBoolean(ARG_IS_CLEAR)
            isMultiply = it.getBoolean(ARG_IS_MULTIPLY)
        }
        //必须在订阅之前
        DeviceHelper.setInterface(currentModel, isClear)

        lifecycle.addObserver(BIOL(this, currentModel))


        if ( !isMultiply) DeviceHelper.startScan()
        else if (isMultiply && modelIndex == scanViewModel.state.value!!.size -1)DeviceHelper.startScan(false, currentModel)




        //扫描通知
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this, Observer { it ->
                val b =  it as Bluetooth
                val bluetooth = scanViewModel.device.value!![modelIndex]

                if (scanViewModel.state.value!![modelIndex]  > DeviceHelper.State.UNBOUND
                    && bluetooth != null
                    && b.name == bluetooth.name) {//已绑定
                    DeviceHelper.connect(requireContext(), b)
                }
                adapter.deviceList = BluetoothController.getDevices(currentModel)
                adapter.notifyDataSetChanged()

            })
        // 设备信息通知
        LiveEventBus.get(EventMsgConst.ER1.EventEr1Info)
            .observe(this, {
                it?.let {
                    //去绑定
                    if (DeviceHelper.bind(it, scanViewModel.device.value!![modelIndex] as Bluetooth)) {
                        LiveEventBus.get(EventUI.BindFinish).post(true)
                    }
                }
            })


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_connect_er1_list, container, false)

        val textView: TextView = root.findViewById(R.id.text)
        val switch: Switch = root.findViewById(R.id.switch1)
        val listView = root.findViewById<ListView>(R.id.ble_list)

        BluetoothController.clear()
        adapter = BleAdapter(requireContext(),  BluetoothController.getDevices(currentModel))
        listView.adapter = adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val b = adapter.deviceList[position]
            }


        //手动切换连接状态
        switch.setOnCheckedChangeListener() { buttonView, isChecked ->

            LepuBleLog.d("setOnCheckedChangeListener ,$isChecked ,${scanViewModel.state.value}")
            if (isChecked && scanViewModel.state.value!![modelIndex] == DeviceHelper.State.DISCONNECTED)
                DeviceHelper.reconnect(currentModel)

            if (!isChecked && scanViewModel.state.value!![modelIndex] == DeviceHelper.State.CONNECTED) {
                DeviceHelper.disconnect(currentModel, false)
//                DeviceHelper.disconnect(false) // 可使用于多设备时候全部断开

            }
        }

        //注册ViewModel
        scanViewModel.state.observe(viewLifecycleOwner, Observer {
            val b = scanViewModel.device.value!![modelIndex]
            textView.text = "${DeviceHelper.convertState(it[modelIndex])} => ${b?.name}"
            if (it[modelIndex] != DeviceHelper.State.CONNECTING) switch.isChecked = it[modelIndex] == DeviceHelper.State.CONNECTED
            switch.text = DeviceHelper.convertState(it[modelIndex])
        })
        // 设备实例
        scanViewModel.device.observe(viewLifecycleOwner, Observer {
            LepuBleLog.d("${it[modelIndex] == null}")
            switch.isClickable = it[modelIndex] != null
        })


        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val b = adapter.deviceList[position]
                // 去连接
                scanViewModel.device.value!!.apply {
                    this[modelIndex] = b
                    scanViewModel._device.value = this
                }
                LiveEventBus.get(EventUI.ConnectingLoading).post(true)
                DeviceHelper.connect(requireContext(), b)

            }


        scanViewModel.device.value!![modelIndex] = null // 不能点击switch


        return root
    }


    companion object {

        const val ARG_CURRENT_MODEL = "currentModel"
        const val ARG_MODEL_INDEX = "modelIndex"
        const val ARG_IS_CLEAR = "isClear"
        const val ARG_IS_MULTIPLY = "isMultiply"

        @JvmStatic
        fun newInstance(curModel: Int, modelIndex: Int, isMultiply: Boolean,isClear: Boolean) =
                ConnectEr1Fragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_CURRENT_MODEL, curModel)
                        putInt(ARG_MODEL_INDEX, modelIndex)
                        putBoolean(ARG_IS_CLEAR, isClear)
                        putBoolean(ARG_IS_MULTIPLY, isMultiply)
                    }
                }
    }

    override fun onBleStateChange(curModel: Int, state: Int) {
        LepuBleLog.d("onBleStateChange ${state}, ${scanViewModel.state.value?.joinToString()}")
        scanViewModel.state.value!!.apply {
            this[modelIndex] = state
            scanViewModel._state.value = this
        }

        val bluetooth = scanViewModel.device.value!![modelIndex] as Bluetooth
        if ( bluetooth.model != curModel) return

        if (!isMultiply || state !=  DeviceHelper.State.CONNECTED)return
        val any = scanViewModel.state.value?.toList()?.any { it == DeviceHelper.State.UNBOUND }
        if (any == true )
            DeviceHelper.startScan(false, 0)


    }
}