package com.lepu.demo.ui.o2

import android.bluetooth.le.ScanRecord
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.lepu.blepro.ble.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.event.BleProEvent
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LogUtils
import com.lepu.blepro.utils.PairDevice
import com.lepu.demo.O2RingEvent
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "currentModel"

/**
 * A simple [Fragment] subclass.
 * Use the [ConnectO2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConnectO2Fragment : Fragment() {

    private val connectO2ViewModel: ConnectO2ViewModel by viewModels()

    private var currentModel: Int = 0

    private var currentBluetooth: Bluetooth? = null


    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        arguments?.let {
            currentModel = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

//        connectO2ViewModel =
//            ViewModelProvider(this).get(ConnectO2ViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_connect_o2, container, false)
        textView = root.findViewById(R.id.text)

        connectO2ViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        return root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConnectO2Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(currentModel: Int) =
            ConnectO2Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, currentModel)
                }
            }
    }






    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBleProEvent(event: BleProEvent) {
        when (event.action) {
            EventMsgConst.Oxy.EventOxyPairO2Ring -> {
                LogUtils.d("EventOxyPairO2Ring")
                val map =
                    event.data as HashMap<String, Any>
                if (map != null) {
                    val bluetooth =
                        map[EventMsgConst.Oxy.EventOxyKeyDevice] as Bluetooth?
                    val scanRecord =
                        map[EventMsgConst.Oxy.EventOxyKeyScanRecord] as ScanRecord?

                    scanRecord?.let {
                        val pairO2: Boolean = PairDevice.pairO2(scanRecord)
                        LogUtils.d("配对结果 = $pairO2")
                        if (pairO2) {
                            currentBluetooth = bluetooth

                            currentBluetooth?.let { it1 -> context?.let { it2 ->
                                DeviceHelper.connectO2(
                                    it2, it1)
                                connectO2ViewModel._text.value = it1.name
                            } }
                            O2RingEvent.post(O2RingEvent.O2UIConnectingLoading, currentBluetooth)

                        }
                    }


                }
            }
            EventMsgConst.Oxy.EventOxyInfo -> DeviceHelper.bindO2(
                event,
                currentModel,
                currentBluetooth
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}