package com.lepu.demo.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.PC60FwBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil


val SCAN_MODELS: IntArray = intArrayOf(Bluetooth.MODEL_PC60FW)
class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var adapter: DeviceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvent()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
       initView(root)

        return root
    }

    private fun initView(root: View){
        val scan: TextView = root.findViewById(R.id.scan)

        scan.setOnClickListener {
            LpBleUtil.startScan(SCAN_MODELS)
        }

        val rcv = root.findViewById<RecyclerView>(R.id.rcv)
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            rcv.layoutManager = this
        }
        adapter = DeviceAdapter(R.layout.device_item, null).apply {
            rcv.adapter = this
        }

        adapter.setOnItemClickListener { adapter, view, position ->
            adapter.getItem(position).let {

                activity?.applicationContext?.let { it1 ->
                    LpBleUtil.connect(it1, it as Bluetooth)

                }

            }
        }

    }



    private fun initEvent(){
        //扫描通知
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this, Observer {
                adapter.setNewInstance(BluetoothController.getDevices())
                adapter.notifyDataSetChanged()

            })

        // ------------------PC60Fw--------------------------
        LiveEventBus.get(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam)
            .observe(this, Observer {
                it as InterfaceEvent
                val rtData = it.data as PC60FwBleResponse.RtDataParam
                Log.d("PC60-rt","spo2 = ${rtData.spo2}，pi = ${rtData.pi}, pr = ${rtData.pr}")


            })
    }
}