package com.lepu.demo.ui.o2

import android.annotation.SuppressLint
import android.bluetooth.le.ScanRecord
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.EventUI
import com.lepu.demo.R
import com.lepu.demo.ble.BleUtilService
import com.lepu.demo.ble.BleUtilService.State
import com.lepu.demo.ble.PairDevice
import com.lepu.demo.ui.scan.ScanViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val CURRENT_MODEL = "currentModel"
private const val MODEL_INDEX = "modelIndex"
private const val IS_CLEAR = "isClear"
private const val IS_MULTIPLY = "isMultiply"

/**
 * A simple [Fragment] subclass.
 * Use the [ConnectO2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 *
 */
class ConnectO2Fragment : Fragment(), BleChangeObserver{

    private val scanViewModel: ScanViewModel by activityViewModels()

    private var currentModel: Int = 0

    /**
     * 是否清除之前的interface
     */
    private var isClear:Boolean = true
    private var isMultiply:Boolean = false

    /**
     * 设备顺序，为此demo中在ViewModel取值方便
     */
    private var modelIndex = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentModel = it.getInt(CURRENT_MODEL)
            modelIndex = it.getInt(MODEL_INDEX)
            isClear = it.getBoolean(IS_CLEAR)
            isMultiply = it.getBoolean(IS_MULTIPLY)
        }

        //必须在订阅之前初始化, 同一model重复调用只会初始化一次
        BleUtilService.setInterface(currentModel,true)
        // 订阅蓝牙状态通知（实现 BleChangeObserver）
        lifecycle.addObserver(BIOL(this,intArrayOf(currentModel)))

        //订阅之后扫描
        // 组合套装 只有最后添加的fragment 开启扫描, 并且使用多设备过滤模式
        if ( !isMultiply ) BleUtilService.startScan(currentModel, true)
        else if (isMultiply && modelIndex == scanViewModel.state.value!!.size -1)  BleUtilService.startScan(intArrayOf(Bluetooth.MODEL_O2RING, currentModel),true)




        //注册通知：配对
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound_ScanRecord)
            .observe(this,
                {

                    val map = it as HashMap<String, Any?>
                    val b = map[EventMsgConst.Discovery.EventDeviceFound_Device] as Bluetooth
                    val record = map[EventMsgConst.Discovery.EventDeviceFound_ScanRecord] as ScanRecord

                    if (currentModel != b.model) return@observe


                    val pairO2: Boolean = PairDevice.pairO2(record)
                    LepuBleLog.d("配对结果 = $pairO2")
                    if (pairO2) {
                        //配对成功去连接(UI 包含连接与绑定的过程)
                        LiveEventBus.get(EventUI.ConnectingLoading).post(true)

                        scanViewModel.device.value!!.apply {
                            this[modelIndex] = b
                            scanViewModel._device.value = this
                        }
                        BleUtilService.connect(requireActivity().application, b)

                    }

                })

        //设备信息
//        LiveEventBus.get(EventMsgConst.Oxy.EventOxyInfo)
//            .observe(this,{
//                it?.let {
//                    //绑定
//                    if (BleUtilService.bind(it, scanViewModel.device.value!![modelIndex] as Bluetooth)) {
//                        LiveEventBus.get(EventUI.BindFinish).post(true)
//                    }
//                }
//            })
        //扫描通知
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound)
                .observe(this, Observer {
                    // 如果已绑定 则重连
                    val b =  it as Bluetooth
                    val bluetooth = scanViewModel.device.value!![modelIndex]

                    if (scanViewModel.state.value!![modelIndex]  > BleUtilService.State.UNBOUND
                        && bluetooth != null
                        && b.name == bluetooth.name) {//已绑定
                        BleUtilService.connect(requireActivity().application, b)
                    }
                })

        LiveEventBus.get(InterfaceEvent.Oxy.EventOxyInfo)
                .observe(this, {
                    it as InterfaceEvent
                    if (BleUtilService.bind(it.data, scanViewModel.device.value!![modelIndex] as Bluetooth)) {
                        LiveEventBus.get(EventUI.BindFinish).post(true)
                    }
                })


    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //---------------------inti  UI start-----------------------
        val root = inflater.inflate(R.layout.fragment_connect_o2, container, false)
        val textView: TextView = root.findViewById(R.id.text)
        val switch: Switch = root.findViewById(R.id.switch1)
        val disconnectAll: Button = root.findViewById(R.id.button1)
        val connectAll: Button = root.findViewById(R.id.button2)



        if (isMultiply){
            disconnectAll.setOnClickListener {

                val all = scanViewModel.state.value?.toList()?.all { it > State.UNBOUND}
                if (all == true) BleUtilService.disconnect(false)
            }

            connectAll.setOnClickListener {
                val all = scanViewModel.state.value?.toList()?.all { it > State.UNBOUND}
//                if (all == true) BleUtilService.reconnect(intArrayOf(currentModel, Bluetooth.MODEL_ER1), arrayOf(scanViewModel.device.value[0], scanViewModel.device.value[]))
            }
        }



        //手动切换连接状态
        switch.setOnCheckedChangeListener() { buttonView, isChecked ->

            LepuBleLog.d("setOnCheckedChangeListener ,$isChecked ,${scanViewModel.state.value}")
            if (isChecked && scanViewModel.state.value!![modelIndex] == State.DISCONNECTED)
                scanViewModel.device.value!![modelIndex]?.name?.let {
                    BleUtilService.reconnect(currentModel,
                        it
                    )
                }

            if (!isChecked && scanViewModel.state.value!![modelIndex] == State.CONNECTED) {
                BleUtilService.disconnect(currentModel, false)
//                DeviceHelper.disconnect(false) // 可使用于多设备时候全部断开

            }
        }
        //---------------------inti  UI start-----------------------



        // -------ViewModel 注册 start----------------
        // 蓝牙状态
        scanViewModel.state.observe(viewLifecycleOwner, Observer {
            val b = scanViewModel.device.value!![modelIndex]
            textView.text = "${BleUtilService.convertState(it[modelIndex])} => ${b?.name}"
            if (it[modelIndex] != State.CONNECTING) switch.isChecked = it[modelIndex] == State.CONNECTED
            switch.text = BleUtilService.convertState(it[modelIndex])
        })
        // 设备实例
        scanViewModel.device.observe(viewLifecycleOwner, Observer {
            LepuBleLog.d("${it[modelIndex] == null}")
            switch.isClickable = it[modelIndex] != null
        })

        // -------ViewModel 注册 end---------------------
        scanViewModel.device.value!![modelIndex] = null





        return root
    }


    companion object {
        @JvmStatic
        fun newInstance(currentModel: Int, modelIndex: Int, isMultiply: Boolean, isClear: Boolean) =
            ConnectO2Fragment().apply {
                arguments = Bundle().apply {
                    putInt(CURRENT_MODEL, currentModel)
                    putInt(MODEL_INDEX, modelIndex)
                    putBoolean(IS_CLEAR, isClear)
                    putBoolean(IS_MULTIPLY, isMultiply)

                }
            }
    }

    /**
     * 蓝牙状态改变通知
     * 组合套装，连接后检查是否有剩余设备未连接
     *
     */
    override fun onBleStateChanged(curModel: Int, state: Int) {
//        scanViewModel._state.value = state

        LepuBleLog.d("onBleStateChange ${state} ")

        // 组合套装 判断状态通知是否来自本设备
        if (currentModel != curModel) return

        scanViewModel.state.value!!.apply {
            this[modelIndex] = state
            scanViewModel._state.value = this
        }

        // 绑定组合套装时，一台设备连接后，检查是否还有未绑定的设备,重启扫描
        if (!isMultiply)return
        val any = scanViewModel.state.value?.toList()?.any { it == State.UNBOUND }
        if (any == true && state == State.CONNECTED )
            BleUtilService.startScan(intArrayOf(Bluetooth.MODEL_O2RING, currentModel),false)


    }
}