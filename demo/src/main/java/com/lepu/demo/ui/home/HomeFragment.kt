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
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.cmd.PC60FwBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil


val SCAN_MODELS: IntArray = intArrayOf(Bluetooth.MODEL_O2RING)
class HomeFragment : Fragment(), BleChangeObserver{

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var adapter: DeviceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvent()
        // 当BleService onServiceConnected执行后发出通知 蓝牙sdk 初始化完成
        LiveEventBus.get(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).observeSticky(this, Observer {

            lifecycle.addObserver(BIOL(this, SCAN_MODELS))

        })


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
                    LpBleUtil.stopScan()

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

        //bp2 同步时间
        LiveEventBus.get(InterfaceEvent.BP2.EventBp2SyncTime)
            .observe(this, Observer{
                it as InterfaceEvent
                if (it.data as Boolean)
                    LpBleUtil.bp2SwitchState(Bluetooth.MODEL_BP2, Bp2BleCmd.SwitchState.ENTER_BP)

            })

        // o2ring 同步时间
        LiveEventBus.get(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this, Observer{
                LpBleUtil.startRtTask(Bluetooth.MODEL_O2RING)

            })

        // o2ring ppg
        LiveEventBus.get(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this, Observer{
                it as InterfaceEvent
                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.rawData.let { data ->
                    Log.d("o2ring ppg", "ir = ${data.ir}, red = ${data.red}, motion = ${data.motion}")
                }


            })



    }

    override fun onBleStateChanged(model: Int, state: Int) {

        if (state == LpBleUtil.State.CONNECTED){
            if (SCAN_MODELS.contains(Bluetooth.MODEL_BP2))
                 LpBleUtil.startRtTask(Bluetooth.MODEL_BP2)


        }
    }
}