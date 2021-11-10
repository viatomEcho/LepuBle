package com.lepu.demo.ui.home

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.cmd.PC60FwBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.CURRENT_MODEL
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.SCAN_MODELS
import com.lepu.demo.ble.DeviceAdapter
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.util.DateUtil
import com.lepu.demo.util.FileUtil
import com.lepu.demo.util.SdLocal
import java.util.*



val FILE_PPG_O2RING: Int = 0


class HomeFragment : Fragment(){

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var homeViewModel: HomeViewModel

    var o2ringPpg: ByteArray = ByteArray(0)

    var stopCollect: Boolean = true
    var collectStartTime: Long = 0L
    var collectEndTime: Long = 0L

    var rtFilePath: SparseArray<String> = SparseArray()



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
            clearO2ringPpg()
            stopCollect = false
        }

        val startCollect: TextView = root.findViewById(R.id.start_collect)
        startCollect.setOnClickListener{
           startCollectPpg()

        }


        val stopCollect: TextView = root.findViewById(R.id.stop_collect)
        stopCollect.setOnClickListener{
           stopCollectPpg()


        }


        val disconnect: TextView = root.findViewById(R.id.disconnect)
        disconnect.setOnClickListener{
            LpBleUtil.disconnect(false)
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
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this, Observer {
                adapter.setNewInstance(BluetoothController.getDevices())
                adapter.notifyDataSetChanged()

            })

        // ------------------PC60Fw--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam)
            .observe(this, Observer {
                it as InterfaceEvent
                val rtData = it.data as PC60FwBleResponse.RtDataParam
                Log.d("PC60-rt","spo2 = ${rtData.spo2}，pi = ${rtData.pi}, pr = ${rtData.pr}")


            })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime)
            .observe(this, Observer {
                Toast.makeText(requireContext(), "ER1 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.startRtTask(Bluetooth.MODEL_ER1, 200)


            })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
            .observe(this, Observer{


                it as InterfaceEvent
                val er1 = it.data as Er1BleResponse.RtData
                er1.let { data ->

                    Log.d("er1data ", "len  = ${data.wave.wave.size}")

                }


            })



        //bp2 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
            .observe(this, Observer{
                it as InterfaceEvent
                Toast.makeText(requireContext(), "bp2 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(CURRENT_MODEL)

//                LpBleUtil.startRtTask(Bluetooth.MODEL_BP2, 500)

            })

        // o2ring 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this, Observer{
                Toast.makeText(requireContext(), "o2ring 完成时间同步", Toast.LENGTH_SHORT).show()
//                LpBleUtil.startRtTask(CURRENT_MODEL)
//                LpBleUtil.oxyGetPpgRt(CURRENT_MODEL)
//                startCollectPpg()
                LpBleUtil.getInfo(CURRENT_MODEL)


            })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).observe(this, { event ->
            (event.data as OxyBleResponse.OxyInfo).let {
                LepuBleLog.d("o2ring device info = $it")
                mainViewModel._oxyInfo.value = it
            }
        })

        // o2ring ppg
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this, Observer{
                LpBleUtil.oxyGetPpgRt(CURRENT_MODEL)


                it as InterfaceEvent
                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.let { data ->

                    Log.d("ppg", "len  = ${data.rawDataBytes.size}")
                    if (!stopCollect && collectStartTime != 0L) collectRtData(data.rawDataBytes)

                }


            })




    }
    fun startCollectPpg(){

        stopCollect = false

        collectStartTime = System.currentTimeMillis()
    }
    fun stopCollectPpg(){
        stopCollect = true
        collectEndTime = System.currentTimeMillis()
        //File
        if (initExportFilePath()){
            Log.d("collect", "file size  = ${o2ringPpg.size}")

            FileUtil.saveFile(rtFilePath.get(FILE_PPG_O2RING), o2ringPpg, false)
        }
        clearO2ringPpg()


    }
    /**
     * 初始化本地文件地址
     */
    fun initExportFilePath(): Boolean {
        if (collectStartTime == 0L || collectEndTime == 0L) {
            return false
        }
        try {
            val endTime = DateUtil.stringFromDate(Date(collectEndTime), "yyyyMMddHHmmss")

            DateUtil.stringFromDate(Date(collectStartTime), "yyyyMMddHHmmss").let {
                SdLocal.getDataDatPath(context, "${it}_${endTime}_o2ring_ppg").let { p ->
                    rtFilePath.put(FILE_PPG_O2RING, p)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 收集各个设备实时数组
     */
    fun collectRtData(rtData: ByteArray) {
        o2ringPpg = addByteArrayData(o2ringPpg, rtData)

        Log.d("collect", "size  = ${o2ringPpg.size}")


    }
    private fun addByteArrayData(oldData: ByteArray, feed: ByteArray): ByteArray {
        return ByteArray(oldData.size + feed.size).apply {
            oldData.copyInto(this)
            feed.copyInto(this, oldData.size)
        }
    }




    override fun onDestroy() {
        super.onDestroy()
       clearO2ringPpg()
    }

    fun clearO2ringPpg(){
        o2ringPpg = ByteArray(0)
        collectStartTime = 0L
        collectEndTime = 0L
    }
}