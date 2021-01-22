package com.lepu.demo.ui.o2

import android.annotation.SuppressLint
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.EventUI
import com.lepu.demo.ble.PairDevice
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceHelper
import com.lepu.demo.ble.DeviceHelper.State
import com.lepu.demo.ui.scan.ScanViewModel
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "currentModel"

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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentModel = it.getInt(ARG_PARAM1)
        }
        // 订阅蓝牙状态（实现 BleChangeObserver）
        // 如果不订阅，则必须调用setInterface()初始化
        lifecycle.addObserver(BIOL(this, intArrayOf(currentModel) ))



        //注册通知：配对
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound_ScanResult)
            .observe(this,
                {

                    val map = it as HashMap<String, Any?>
                    val b = map[EventMsgConst.Discovery.EventDeviceFound_Device] as Bluetooth
                    val record = map[EventMsgConst.Discovery.EventDeviceFound_ScanResult] as ScanRecord

                    if (currentModel != b.model) return@observe


                    val pairO2: Boolean = PairDevice.pairO2(record)
                    LepuBleLog.d("配对结果 = $pairO2")
                    if (pairO2) {
                        //配对成功去连接(UI 包含连接与绑定的过程)
                        LiveEventBus.get(EventUI.ConnectingLoading).post(true)

                        scanViewModel._device.value = b
                        DeviceHelper.connect(requireContext(), b)

                    }

                })
        //注册通知：设备信息
        LiveEventBus.get(EventMsgConst.Oxy.EventOxyInfo)
            .observe(this,{
                it?.let {
                    //绑定
                    if (DeviceHelper.bind(it, scanViewModel.device.value as Bluetooth)) {
                        LiveEventBus.get(EventUI.BindFinish).post(true)
                    }
                }
            })
        // 重连
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound)
                .observe(this, Observer {

                    if (scanViewModel.state.value as Int > State.UNBOUND){//已绑定
                        DeviceHelper.connect(requireContext(), it as Bluetooth)
                    }
                })


    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_connect_o2, container, false)
        val textView: TextView = root.findViewById(R.id.text)
        val switch: Switch = root.findViewById(R.id.switch1)


        switch.setOnCheckedChangeListener() { buttonView, isChecked ->
            LepuBleLog.d("setOnCheckedChangeListener ,$isChecked ,${scanViewModel.state.value}")
            if (isChecked && scanViewModel.state.value == State.DISCONNECTED)
                DeviceHelper.reconnect(currentModel)

            if (!isChecked && scanViewModel.state.value == State.CONNECTED) {
//                DeviceHelper.disconnect(currentModel, false)
                DeviceHelper.disconnect(false) // 可使用于多设备时候全部断开

            }


        }

        scanViewModel.state.observe(viewLifecycleOwner, Observer {
            val b = scanViewModel.device.value
            textView.text = "${DeviceHelper.convertState(it)} => ${b?.name}"
            if (it != State.CONNECTING) switch.isChecked = it == State.CONNECTED
            switch.text = DeviceHelper.convertState(it)
        })

        scanViewModel.device.observe(viewLifecycleOwner, Observer {
            LepuBleLog.d("${it == null}")
            switch.isClickable = it != null
        })
        scanViewModel._device.value = null

        //开启扫描
        BleServiceHelper.BleServiceHelper.startScan()


        return root
    }


    companion object {
        @JvmStatic
        fun newInstance(currentModel: Int) =
            ConnectO2Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, currentModel)
                }
            }
    }


    override fun onBleStateChange(curModel: Int, state: Int) {
        //如果是多设备界面应该先判断curModel
        scanViewModel._state.value = state
    }
}