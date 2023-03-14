package com.lepu.demo.ui.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.lew.RtData
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.DemoWidgetProvider
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.WirelessDataActivity
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.data.DataController
import com.lepu.demo.data.OxyDataController
import com.lepu.demo.data.WirelessData
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.databinding.FragmentDashboardBinding
import com.lepu.demo.util.DataConvert
import com.lepu.demo.util.DateUtil
import com.lepu.demo.util.FileUtil
import com.lepu.demo.views.EcgBkg
import com.lepu.demo.views.EcgView
import com.lepu.demo.views.Er3EcgView
import com.lepu.demo.views.OxyView
import org.json.JSONObject
import java.util.*
import kotlin.math.floor

class DashboardFragment : Fragment(R.layout.fragment_dashboard){
    val TAG: String = "DashboardFragment"

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: DashboardViewModel by activityViewModels()

    private val binding: FragmentDashboardBinding by binding()

    private var state = false
    private var type = 0
    // 采集
    private var o2RtType = 0  // 0: rt param, 1: rt wave, 2: rt ppg
    private var startCollectTime = 0
    private var stopCollectTime = 0
    private var collectBytesData = ByteArray(0)
    private var collectIntsData = mutableListOf<Int>()

    // 无线共存
    private var isStartWirelessTest = false
    private var tempTime = 0
    private var recordTime = 0L
    private var sendSeqNo = 0
    private var tempSeqNo = 0
    private var wirelessData = WirelessData()
    private var handler = Handler()
    private var task = WirelessTask()
    inner class WirelessTask : Runnable {
        override fun run() {
            LpBleUtil.r20Echo(Constant.BluetoothConfig.currentModel[0], ByteArray(12))
            sendSeqNo++
            wirelessData.totalBytes = sendSeqNo * 20
            wirelessData.recordTime = (System.currentTimeMillis() - wirelessData.startTime).div(1000).toInt()
            if ((wirelessData.recordTime - tempTime) != 0) {
                tempTime = wirelessData.recordTime
                wirelessData.speed = wirelessData.receiveBytes.div(wirelessData.recordTime*1.0)
                wirelessData.throughput = wirelessData.receiveBytes.div(wirelessData.recordTime*1024.0).times(3600)
                binding.wirelessDataLayout.speed.text = "测试时长：${DataConvert.getEcgTimeStr(wirelessData.recordTime)}\n" +
                        "吞吐量：${String.format("%.3f", wirelessData.throughput)} kb/h\n" +
                        "数据传输速度：${String.format("%.3f", wirelessData.speed)} b/s"
            }
            if (isStartWirelessTest) {
                handler.postDelayed(task, 100)
            }
        }

    }

    // 心电产品
    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView

    // er3
    private lateinit var ecgBkg1: EcgBkg
    private lateinit var ecgView1: Er3EcgView
    private lateinit var ecgBkg2: EcgBkg
    private lateinit var ecgView2: Er3EcgView
    private lateinit var ecgBkg3: EcgBkg
    private lateinit var ecgView3: Er3EcgView
    private lateinit var ecgBkg4: EcgBkg
    private lateinit var ecgView4: Er3EcgView
    private lateinit var ecgBkg5: EcgBkg
    private lateinit var ecgView5: Er3EcgView
    private lateinit var ecgBkg6: EcgBkg
    private lateinit var ecgView6: Er3EcgView
    private lateinit var ecgBkg7: EcgBkg
    private lateinit var ecgView7: Er3EcgView
    private lateinit var ecgBkg8: EcgBkg
    private lateinit var ecgView8: Er3EcgView
    private lateinit var ecgBkg9: EcgBkg
    private lateinit var ecgView9: Er3EcgView
    private lateinit var ecgBkg10: EcgBkg
    private lateinit var ecgView10: Er3EcgView
    private lateinit var ecgBkg11: EcgBkg
    private lateinit var ecgView11: Er3EcgView
    private lateinit var ecgBkg12: EcgBkg
    private lateinit var ecgView12: Er3EcgView

    // 血氧产品
    private lateinit var oxyView: OxyView

    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class EcgWaveTask : Runnable {
        override fun run() {
            if (!mainViewModel.runWave) {
                return
            }

            val interval: Int = when {
                DataController.dataRec.size > 250 -> {
                    30
                }
                DataController.dataRec.size > 150 -> {
                    35
                }
                DataController.dataRec.size > 75 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LepuBleLog.d("DataRec: ${DataController.dataRec.size}, delayed $interval")

            val temp = DataController.draw(5)
            viewModel.dataEcgSrc.value = DataController.feed(viewModel.dataEcgSrc.value, temp)
        }
    }
    inner class Er3EcgWaveTask : Runnable {
        override fun run() {
            if (!mainViewModel.runWave) {
                return
            }

            val interval: Int = when {
                Er3DataController.dataRec.size > 250*8*2 -> {
                    30
                }
                Er3DataController.dataRec.size > 150*8*2 -> {
                    35
                }
                Er3DataController.dataRec.size > 75*8*2 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())

            Er3DataController.draw(10)
            /**
             * update viewModel
             */
            viewModel.dataEcgSrc1.value = Er3DataController.src1
            viewModel.dataEcgSrc2.value = Er3DataController.src2
            viewModel.dataEcgSrc3.value = Er3DataController.src3
            viewModel.dataEcgSrc4.value = Er3DataController.src4
            viewModel.dataEcgSrc5.value = Er3DataController.src5
            viewModel.dataEcgSrc6.value = Er3DataController.src6
            viewModel.dataEcgSrc7.value = Er3DataController.src7
            viewModel.dataEcgSrc8.value = Er3DataController.src8
            viewModel.dataEcgSrc9.value = Er3DataController.src9
            viewModel.dataEcgSrc10.value = Er3DataController.src10
            viewModel.dataEcgSrc11.value = Er3DataController.src11
            viewModel.dataEcgSrc12.value = Er3DataController.src12
        }
    }
    inner class OxyWaveTask : Runnable {
        override fun run() {
            if (!mainViewModel.runWave) {
                return
            }

            val interval: Int = when {
                OxyDataController.dataRec.size > 250 -> {
                    30
                }
                OxyDataController.dataRec.size > 150 -> {
                    35
                }
                OxyDataController.dataRec.size > 75 -> {
                    40
                }
                else -> {
                    111  // 血氧波形50HZ，1s有50个点，速度大于100即可（1000ms/10包）
                         // 有时候设备1s发不够10包，只有9包，速度要111（1000ms/9包）
                }
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LepuBleLog.d("DataRec: ${OxyDataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
            viewModel.dataOxySrc.value = OxyDataController.feed(viewModel.dataOxySrc.value, temp)
        }
    }

    private fun startWave(model: Int) {
        if (mainViewModel.runWave) {
            return
        }
        mainViewModel.runWave = true
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_BP2,
            Bluetooth.MODEL_BP2W,
            Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_LE_BP2W,
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_LES1,
            Bluetooth.MODEL_W12C, Bluetooth.MODEL_HHM1,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3,
            Bluetooth.MODEL_LP_ER2, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> waveHandler.post(EcgWaveTask())

            Bluetooth.MODEL_ER3, Bluetooth.MODEL_LEPOD -> waveHandler.post(Er3EcgWaveTask())

            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_PC60FW,
            Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_PC100, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_BABYO2,
            Bluetooth.MODEL_AP20_WPS, Bluetooth.MODEL_SP20_WPS,
            Bluetooth.MODEL_BBSM_S1, Bluetooth.MODEL_BBSM_S2,
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_TV221U,
            Bluetooth.MODEL_BABYO2N, Bluetooth.MODEL_CHECKO2,
            Bluetooth.MODEL_O2M, Bluetooth.MODEL_SLEEPO2,
            Bluetooth.MODEL_SNOREO2, Bluetooth.MODEL_WEARO2,
            Bluetooth.MODEL_SLEEPU, Bluetooth.MODEL_OXYLINK,
            Bluetooth.MODEL_KIDSO2, Bluetooth.MODEL_OXYSMART,
            Bluetooth.MODEL_OXYFIT, Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_PC_68B,
            Bluetooth.MODEL_POD2B, Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_OXYRING, Bluetooth.MODEL_CMRING,
            Bluetooth.MODEL_OXYU, Bluetooth.MODEL_S5W,
            Bluetooth.MODEL_AI_S100, Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S7W, Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_S6W1, Bluetooth.MODEL_SP20_BLE,
            Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS,
            Bluetooth.MODEL_O2M_WPS, Bluetooth.MODEL_VTM01 -> waveHandler.post(OxyWaveTask())

            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_PC300,
            Bluetooth.MODEL_CHECK_ADV, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE -> {
                waveHandler.post(EcgWaveTask())
                waveHandler.post(OxyWaveTask())
            }
        }

    }

    private fun stopWave() {
        mainViewModel.runWave = false
        DataController.clear()
        OxyDataController.clear()
        Er3DataController.clear()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = this@DashboardFragment
        }
        initView()
        initLiveEvent()
    }

    private fun refresh(it: DeviceEntity) {
        when (it.modelNo) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N,
            Bluetooth.MODEL_HHM1, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3,
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2,
            Bluetooth.MODEL_LES1 -> {
                binding.ecgLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
                LpBleUtil.startRtTask()
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_LEW, Bluetooth.MODEL_W12C -> {
                LpBleUtil.startRtTask(it.modelNo, 2000)
            }
            Bluetooth.MODEL_ER3, Bluetooth.MODEL_LEPOD -> {
                binding.er3Layout.visibility = View.VISIBLE
                binding.ecgLayout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
                LpBleUtil.startRtTask()
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_PC80B_BLE,
            Bluetooth.MODEL_PC80B_BLE2 -> {
                binding.ecgLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LE_BP2W -> {
                binding.bpLayout.visibility = View.VISIBLE
                binding.ecgLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
                LpBleUtil.startRtTask()
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                binding.bpLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.ecgLayout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
                LpBleUtil.startRtTask()
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_BPM -> {
                binding.bpLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.ecgLayout.visibility = View.GONE
                binding.oxyLayout.visibility = View.GONE
            }
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_AI_S100,
            Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_BABYO2N,
            Bluetooth.MODEL_BBSM_S1, Bluetooth.MODEL_BBSM_S2,
            Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_O2M,
            Bluetooth.MODEL_SLEEPO2, Bluetooth.MODEL_SNOREO2,
            Bluetooth.MODEL_WEARO2, Bluetooth.MODEL_SLEEPU,
            Bluetooth.MODEL_OXYLINK, Bluetooth.MODEL_KIDSO2,
            Bluetooth.MODEL_OXYFIT, Bluetooth.MODEL_OXYRING,
            Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYU,
            Bluetooth.MODEL_CHECK_POD, Bluetooth.MODEL_O2M_WPS,
            Bluetooth.MODEL_VTM01 -> {
                binding.oxyLayout.visibility = View.VISIBLE
                binding.o2RtTypeLayout.visibility = View.VISIBLE
                binding.collectPpg.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.ecgLayout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                binding.o2RtTypeLayout.check(R.id.o2_rt_param)
                LpBleUtil.startRtTask()
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC_60B,
            Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
            Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_PC66B, Bluetooth.MODEL_AP20,
            Bluetooth.MODEL_SP20, Bluetooth.MODEL_SP20_BLE,
            Bluetooth.MODEL_SP20_WPS, Bluetooth.MODEL_AP20_WPS,
            Bluetooth.MODEL_TV221U, Bluetooth.MODEL_OXYSMART,
            Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_S5W,
            Bluetooth.MODEL_PC_68B, Bluetooth.MODEL_POD2B,
            Bluetooth.MODEL_PC_60NW_1, Bluetooth.MODEL_PC_60NW,
            Bluetooth.MODEL_S6W, Bluetooth.MODEL_S6W1,
            Bluetooth.MODEL_S7BW, Bluetooth.MODEL_S7W,
            Bluetooth.MODEL_PC60NW_BLE, Bluetooth.MODEL_PC60NW_WPS -> {
                binding.oxyLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.ecgLayout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_PC100 -> {
                binding.bpLayout.visibility = View.VISIBLE
                binding.oxyLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.ecgLayout.visibility = View.GONE
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                binding.ecgLayout.visibility = View.VISIBLE
                binding.oxyLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                binding.bpLayout.visibility = View.GONE
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_PC300, Bluetooth.MODEL_PC300_BLE,
            Bluetooth.MODEL_PC200_BLE -> {
                binding.ecgLayout.visibility = View.VISIBLE
                binding.oxyLayout.visibility = View.VISIBLE
                binding.bpLayout.visibility = View.VISIBLE
                binding.er3Layout.visibility = View.GONE
                startWave(it.modelNo)
            }
            Bluetooth.MODEL_BTP -> {
                LpBleUtil.startRtTask(it.modelNo, 2000)
                LpBleUtil.btpGetConfig(it.modelNo)
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_LERES -> {
                binding.wirelessDataLayout.root.visibility = View.VISIBLE
            }
        }
    }

    private fun initView() {

        mainViewModel.curBluetooth.observe(viewLifecycleOwner) { device ->
            device?.let {
                refresh(it)
            }
        }
        mainViewModel.bleState.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.er3BleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.dashLayout.visibility = View.VISIBLE
                    refresh(mainViewModel.curBluetooth.value!!)
                } else {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.er3BleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.dashLayout.visibility = View.INVISIBLE
                    isStartWirelessTest = false
                }
            }

        }
        mainViewModel.battery.observe(viewLifecycleOwner) {
            binding.bleBattery.text = "电量：$it"
            binding.bpBleBattery.text = "电量：$it"
            binding.oxyBleBattery.text = "电量：$it"
            binding.er3BleBattery.text = "电量：$it"
        }

        //------------------------------ecg------------------------------
        binding.ecgBkg.post{
            initEcgView()
        }
        binding.ecgBkg12.post {
            initEr3EcgView()
        }
        viewModel.ecgHr.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.hr.text = "?"
            } else {
                binding.hr.text = it.toString()
            }
        }
        binding.startRtEcg.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300_BLE
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC200_BLE) {
                LpBleUtil.startEcg(Constant.BluetoothConfig.currentModel[0])
            }
            LpBleUtil.startRtTask()
        }
        binding.stopRtEcg.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300_BLE
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC200_BLE) {
                LpBleUtil.stopEcg(Constant.BluetoothConfig.currentModel[0])
            }
            LpBleUtil.stopRtTask()
        }
        viewModel.dataEcgSrc.observe(viewLifecycleOwner) {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
            }
        }
        viewModel.dataEcgSrc1.observe(viewLifecycleOwner) {
            if (this::ecgView1.isInitialized) {
                ecgView1.setDataSrc(it)
                ecgView1.invalidate()
            }
        }
        viewModel.dataEcgSrc2.observe(viewLifecycleOwner) {
            if (this::ecgView2.isInitialized) {
                ecgView2.setDataSrc(it)
                ecgView2.invalidate()
            }
        }
        viewModel.dataEcgSrc3.observe(viewLifecycleOwner) {
            if (this::ecgView3.isInitialized) {
                ecgView3.setDataSrc(it)
                ecgView3.invalidate()
            }
        }
        viewModel.dataEcgSrc4.observe(viewLifecycleOwner) {
            if (this::ecgView4.isInitialized) {
                ecgView4.setDataSrc(it)
                ecgView4.invalidate()
            }
        }
        viewModel.dataEcgSrc5.observe(viewLifecycleOwner) {
            if (this::ecgView5.isInitialized) {
                ecgView5.setDataSrc(it)
                ecgView5.invalidate()
            }
        }
        viewModel.dataEcgSrc6.observe(viewLifecycleOwner) {
            if (this::ecgView6.isInitialized) {
                ecgView6.setDataSrc(it)
                ecgView6.invalidate()
            }
        }
        viewModel.dataEcgSrc7.observe(viewLifecycleOwner) {
            if (this::ecgView7.isInitialized) {
                ecgView7.setDataSrc(it)
                ecgView7.invalidate()
            }
        }
        viewModel.dataEcgSrc8.observe(viewLifecycleOwner) {
            if (this::ecgView8.isInitialized) {
                ecgView8.setDataSrc(it)
                ecgView8.invalidate()
            }
        }
        viewModel.dataEcgSrc9.observe(viewLifecycleOwner) {
            if (this::ecgView9.isInitialized) {
                ecgView9.setDataSrc(it)
                ecgView9.invalidate()
            }
        }
        viewModel.dataEcgSrc10.observe(viewLifecycleOwner) {
            if (this::ecgView10.isInitialized) {
                ecgView10.setDataSrc(it)
                ecgView10.invalidate()
            }
        }
        viewModel.dataEcgSrc11.observe(viewLifecycleOwner) {
            if (this::ecgView11.isInitialized) {
                ecgView11.setDataSrc(it)
                ecgView11.invalidate()
            }
        }
        viewModel.dataEcgSrc12.observe(viewLifecycleOwner) {
            if (this::ecgView12.isInitialized) {
                ecgView12.setDataSrc(it)
                ecgView12.invalidate()
            }
        }

        //------------------------------bp------------------------------
        binding.startBp.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2A
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BP2T) {
                LpBleUtil.startRtTask()
            } else {
                LpBleUtil.startBp(mainViewModel.curBluetooth.value!!.modelNo)
            }
        }
        binding.stopBp.setOnClickListener {
            LpBleUtil.stopBp(mainViewModel.curBluetooth.value!!.modelNo)
        }
        viewModel.ps.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvPs.text = "?"
            } else {
                binding.tvPs.text = it.toString()
            }
        }
        viewModel.sys.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvSys.text = "?"
            } else {
                binding.tvSys.text = it.toString()
            }
        }
        viewModel.dia.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvDia.text = "?"
            } else {
                binding.tvDia.text = it.toString()
            }
        }
        viewModel.mean.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvMean.text = "?"
            } else {
                binding.tvMean.text = it.toString()
            }
        }
        viewModel.bpPr.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvPrBp.text = "?"
            } else {
                binding.tvPrBp.text = it.toString()
            }
        }

        //------------------------------oxy------------------------------
        binding.oxyView.post{
            initOxyView()
        }
        viewModel.oxyPr.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvPr.text = "?"
            } else {
                binding.tvPr.text = it.toString()
            }
        }
        viewModel.spo2.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvOxy.text = "?"
            } else {
                binding.tvOxy.text = it.toString()
            }
        }
        viewModel.pi.observe(viewLifecycleOwner) {
            if (it == 0f) {
                binding.tvPi.text = "?"
            } else {
                binding.tvPi.text = it.toString()
            }
        }
        binding.startRtOxy.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CMRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYRING
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2N
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S1
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BBSM_S2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2M_WPS
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_CHECKO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_WEARO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SLEEPO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_SNOREO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYFIT
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_KIDSO2
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYLINK
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_OXYU
                || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_AI_S100
            ) {
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(Constant.BluetoothConfig.currentModel[0])
                    1 -> LpBleUtil.oxyGetRtWave(Constant.BluetoothConfig.currentModel[0])
                    2 -> LpBleUtil.oxyGetPpgRt(Constant.BluetoothConfig.currentModel[0])
                }
                startWave(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.startRtTask()
            }
        }
        binding.stopRtOxy.setOnClickListener {
            LpBleUtil.stopRtTask()
        }
        viewModel.dataOxySrc.observe(viewLifecycleOwner) {
            if (this::oxyView.isInitialized) {
                oxyView.setDataSrc(it)
                oxyView.invalidate()

            }
        }

        binding.enableRtOxy.setOnClickListener {
            Constant.BluetoothConfig.currentModel[0].let {
                when (it) {
                    Bluetooth.MODEL_AP20, Bluetooth.MODEL_AP20_WPS -> {
                        LpBleUtil.enableRtData(it, type, state)
                        type++
                        if (type > Ap20BleCmd.EnableType.BREATH_WAVE) {
                            type = Ap20BleCmd.EnableType.OXY_PARAM
                            state = !state
                        }
                    }
                    Bluetooth.MODEL_SP20, Bluetooth.MODEL_PC60FW,
                    Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                    Bluetooth.MODEL_PF_10AW, Bluetooth.MODEL_PF_10AW1,
                    Bluetooth.MODEL_PF_10BW, Bluetooth.MODEL_PF_10BW1,
                    Bluetooth.MODEL_PF_20AW, Bluetooth.MODEL_PF_20B,
                    Bluetooth.MODEL_PC66B, Bluetooth.MODEL_POD_1W,
                    Bluetooth.MODEL_PC_68B, Bluetooth.MODEL_POD2B,
                    Bluetooth.MODEL_PC_60NW_1, Bluetooth.MODEL_PC_60B,
                    Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_S5W,
                    Bluetooth.MODEL_S6W, Bluetooth.MODEL_S7W,
                    Bluetooth.MODEL_S7BW, Bluetooth.MODEL_S6W1,
                    Bluetooth.MODEL_SP20_WPS, Bluetooth.MODEL_PC60NW_WPS,
                    Bluetooth.MODEL_SP20_BLE, Bluetooth.MODEL_PC60NW_BLE -> {
                        LpBleUtil.enableRtData(it, type, state)
                        type++
                        if (type > Sp20BleCmd.EnableType.OXY_WAVE) {
                            type = Sp20BleCmd.EnableType.OXY_PARAM
                            state = !state
                        }
                    }
                }
            }
        }
        binding.er3StartEcg.setOnClickListener {
            LpBleUtil.startEcg(Constant.BluetoothConfig.currentModel[0])
        }
        binding.er3StopEcg.setOnClickListener {
            LpBleUtil.stopEcg(Constant.BluetoothConfig.currentModel[0])
        }
        binding.o2RtTypeLayout.setOnCheckedChangeListener { group, checkedId ->
            if (binding.collectPpg.isChecked) {
                when (o2RtType) {
                    0 -> binding.o2RtTypeLayout.check(R.id.o2_rt_param)
                    1 -> binding.o2RtTypeLayout.check(R.id.o2_rt_wave)
                    2 -> binding.o2RtTypeLayout.check(R.id.o2_rt_ppg)
                }
                Toast.makeText(context, "请先停止采集PPG数据！", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            when (checkedId) {
                R.id.o2_rt_param -> o2RtType = 0
                R.id.o2_rt_wave -> o2RtType = 1
                R.id.o2_rt_ppg -> o2RtType = 2
            }
        }
        binding.collectPpg.setOnCheckedChangeListener { buttonView, isChecked ->
            if (o2RtType != 2) {
                binding.collectPpg.isChecked = !isChecked
                Toast.makeText(context, "请先选择获取PPG数据，再进行PPG数据采集！", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            collectPpg(isChecked)
        }
        binding.wirelessDataLayout.startTest.setOnClickListener {
            if (isStartWirelessTest) return@setOnClickListener
            isStartWirelessTest = true
            tempTime = 0
            recordTime = System.currentTimeMillis()
            R20BleCmd.seqNo = 0
            tempSeqNo = 0
            sendSeqNo = 0
            wirelessData = WirelessData()
            wirelessData.startTime = System.currentTimeMillis()
            handler.post(task)
        }
        binding.wirelessDataLayout.stopTest.setOnClickListener {
            isStartWirelessTest = false
        }
        binding.wirelessDataLayout.saveTest.setOnClickListener {
            if (binding.wirelessDataLayout.testData.text.isEmpty()) {
                Toast.makeText(context, "无数据保存", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isAlreadySaveWirelessData()) {
                Toast.makeText(context, "数据已保存", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isStartWirelessTest = false
            FileUtil.saveTextFile("${context?.getExternalFilesDir(null)?.absolutePath}/wireless_test.txt", wirelessData.toString(), true)
            Toast.makeText(context, "保存成功，可在历史记录查看", Toast.LENGTH_SHORT).show()
        }
        binding.wirelessDataLayout.reviewTest.setOnClickListener {
            if (isStartWirelessTest) {
                Toast.makeText(context, "请先停止测试", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(context, WirelessDataActivity::class.java))
        }
    }

    private fun isAlreadySaveWirelessData() : Boolean {
        val data = FileUtil.readFileToString(context, "wireless_test.txt")
        val strs = data.split("WirelessData")
        if (strs.isEmpty()) return false
        for (str in strs) {
            if (str.isEmpty()) continue
            val temp = JSONObject(str)
            val da = WirelessData()
            da.startTime = temp.getLong("startTime")
            if (wirelessData.startTime == da.startTime) {
                return true
            }
        }
        return false
    }

    private fun initEcgView() {
        // cal screen
        val dm =resources.displayMetrics
        // 最多可以画多少点=屏幕宽度像素/每英寸像素*25.4mm/25mm/s走速*125个点/s
        val index = floor(binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        DataController.maxIndex = index

        // 每像素占多少mm=每英寸长25.4mm/每英寸像素
        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

        binding.ecgBkg.measure(0, 0)
        ecgBkg = EcgBkg(context)
        binding.ecgBkg.addView(ecgBkg)
        binding.ecgView.measure(0, 0)
        ecgView = EcgView(context)
        binding.ecgView.addView(ecgView)
    }

    private fun initEr3EcgView() {
        // cal screen
        val dm =resources.displayMetrics
        // 最多可以画多少点=屏幕宽度像素/每英寸像素*25.4mm/25mm/s走速*250个点/s
        val index = floor(binding.ecgBkg1.width / dm.xdpi * 25.4 / 25 * 250).toInt()
        Er3DataController.maxIndex = index

        // 每像素占多少mm=每英寸长25.4mm/每英寸像素
        val mm2px = 25.4f / dm.xdpi
        Er3DataController.mm2px = mm2px

        binding.ecgBkg1.measure(0, 0)
        ecgBkg1 = EcgBkg(context)
        binding.ecgBkg1.addView(ecgBkg1)
        binding.ecgView1.measure(0, 0)
        ecgView1 = Er3EcgView(context)
        binding.ecgView1.addView(ecgView1)

        binding.ecgBkg2.measure(0, 0)
        ecgBkg2 = EcgBkg(context)
        binding.ecgBkg2.addView(ecgBkg2)
        binding.ecgView2.measure(0, 0)
        ecgView2 = Er3EcgView(context)
        binding.ecgView2.addView(ecgView2)

        binding.ecgBkg3.measure(0, 0)
        ecgBkg3 = EcgBkg(context)
        binding.ecgBkg3.addView(ecgBkg3)
        binding.ecgView3.measure(0, 0)
        ecgView3 = Er3EcgView(context)
        binding.ecgView3.addView(ecgView3)

        binding.ecgBkg4.measure(0, 0)
        ecgBkg4 = EcgBkg(context)
        binding.ecgBkg4.addView(ecgBkg4)
        binding.ecgView4.measure(0, 0)
        ecgView4 = Er3EcgView(context)
        binding.ecgView4.addView(ecgView4)

        binding.ecgBkg5.measure(0, 0)
        ecgBkg5 = EcgBkg(context)
        binding.ecgBkg5.addView(ecgBkg5)
        binding.ecgView5.measure(0, 0)
        ecgView5 = Er3EcgView(context)
        binding.ecgView5.addView(ecgView5)

        binding.ecgBkg6.measure(0, 0)
        ecgBkg6 = EcgBkg(context)
        binding.ecgBkg6.addView(ecgBkg6)
        binding.ecgView6.measure(0, 0)
        ecgView6 = Er3EcgView(context)
        binding.ecgView6.addView(ecgView6)

        binding.ecgBkg7.measure(0, 0)
        ecgBkg7 = EcgBkg(context)
        binding.ecgBkg7.addView(ecgBkg7)
        binding.ecgView7.measure(0, 0)
        ecgView7 = Er3EcgView(context)
        binding.ecgView7.addView(ecgView7)

        binding.ecgBkg8.measure(0, 0)
        ecgBkg8 = EcgBkg(context)
        binding.ecgBkg8.addView(ecgBkg8)
        binding.ecgView8.measure(0, 0)
        ecgView8 = Er3EcgView(context)
        binding.ecgView8.addView(ecgView8)

        binding.ecgBkg9.measure(0, 0)
        ecgBkg9 = EcgBkg(context)
        binding.ecgBkg9.addView(ecgBkg9)
        binding.ecgView9.measure(0, 0)
        ecgView9 = Er3EcgView(context)
        binding.ecgView9.addView(ecgView9)

        binding.ecgBkg10.measure(0, 0)
        ecgBkg10 = EcgBkg(context)
        binding.ecgBkg10.addView(ecgBkg10)
        binding.ecgView10.measure(0, 0)
        ecgView10 = Er3EcgView(context)
        binding.ecgView10.addView(ecgView10)

        binding.ecgBkg11.measure(0, 0)
        ecgBkg11 = EcgBkg(context)
        binding.ecgBkg11.addView(ecgBkg11)
        binding.ecgView11.measure(0, 0)
        ecgView11 = Er3EcgView(context)
        binding.ecgView11.addView(ecgView11)

        binding.ecgBkg12.measure(0, 0)
        ecgBkg12 = EcgBkg(context)
        binding.ecgBkg12.addView(ecgBkg12)
        binding.ecgView12.measure(0, 0)
        ecgView12 = Er3EcgView(context)
        binding.ecgView12.addView(ecgView12)
    }

    private fun initOxyView() {
        // cal screen
        val dm = resources.displayMetrics
        val index = floor(binding.oxyView.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        OxyDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        OxyDataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        binding.oxyView.measure(0, 0)
        oxyView = OxyView(context)
        binding.oxyView.addView(oxyView)

        viewModel.dataOxySrc.value = OxyDataController.iniDataSrc(index)
    }

    private fun collectPpg(isCollect: Boolean) {
        if (isCollect) {
            startCollectTime = (System.currentTimeMillis()/1000).toInt()
            collectBytesData = ByteArray(0)
            collectIntsData.clear()
        } else {
            stopCollectTime = (System.currentTimeMillis()/1000).toInt()
            val ppgFile = PpgFile()
            ppgFile.sampleTime = startCollectTime
            ppgFile.sampleSize = collectBytesData.size.div(4)
            ppgFile.leadSize = 1
//            ppgFile.leadSize = 2
            ppgFile.leadConfig[0] = 1
//            ppgFile.leadConfig[1] = 2
            ppgFile.waveConfig[0] = 940
//            ppgFile.waveConfig[1] = 660
            ppgFile.accuracy = 0xffff
            ppgFile.maxValue = 0xffff
            ppgFile.deviceType = 1
            ppgFile.sn = mainViewModel.oxyInfo.value?.sn!!
            ppgFile.sampleData = collectIntsData.toIntArray()
            val fileName = "${DateUtil.stringFromDate(Date(startCollectTime*1000L), "yyyyMMddHHmmss")}.dat"
            FileUtil.saveFile(context, ppgFile.getDataBytes(), fileName)
            Log.d(TAG, "collectIntsData: ${collectIntsData.joinToString(",")}")
            val file = PpgFile(FileUtil.readFileToByteArray(context, fileName))
            Log.d(TAG, "file: $file")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLiveEvent(){
        //------------------------------er1 duoek------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
            .observe(this) {
                val rtData = it.data as Er1BleResponse.RtData
                rtData.let { data ->
                    val len = data.wave.wFs.size
                    Log.d("er1 data ", "len = $len")
                    for (i in 0 until len) {
                        val temp = DataConvert.filter(data.wave.wFs[i].toDouble(), false)
                        if (temp.isNotEmpty()) {
                            val d = FloatArray(temp.size)
                            for (j in d.indices) {
                                d[j] = temp[j].toFloat()
                            }
                            DataController.receive(d)
                        }
                    }
                    DemoWidgetProvider.updateWidgetView(context, "${data.param.hr}")
                    mainViewModel._battery.value = "${data.param.battery} %"
                    binding.dataStr.text = data.param.toString()
                    viewModel.ecgHr.value = data.param.hr
                    binding.measureDuration.text = " ${data.param.recordTime} s"
                    binding.deviceInfo.text = "导联状态：${data.param.leadOn}\n" +
                            "测量状态${data.param.curStatus}：${
                                when (data.param.curStatus) {
                                    0 -> "空闲待机(导联脱落)"
                                    1 -> "测量准备(主机丢弃前段波形阶段)"
                                    2 -> "记录中"
                                    3 -> "分析存储中"
                                    4 -> "已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)"
                                    5 -> "记录小于30s(记录中状态直接切换至此状态)"
                                    6 -> "重测已达6次，进入待机"
                                    7 -> "导联断开"
                                    else -> ""
                                } 
                            }"
                }
            }
        //------------------------------er2------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData)
            .observe(this) {
                val rtData = it.data as Er2RtData
                rtData.let { data ->
                    Log.d("er2 data ", "len = ${data.waveData.size}")
                    val len = data.waveData.datas.size
                    for (i in 0 until len) {
                        val temp = DataConvert.filter(data.waveData.datas[i].toDouble(), false)
                        if (temp.isNotEmpty()) {
                            val d = FloatArray(temp.size)
                            for (j in d.indices) {
                                d[j] = temp[j].toFloat()
                            }
                            DataController.receive(d)
                        }
                    }
                    mainViewModel._battery.value = "${data.rtParam.percent} %"
                    binding.dataStr.text = data.rtParam.toString()
                    viewModel.ecgHr.value = data.hr
                    binding.measureDuration.text = " ${data.rtParam.recordTime} s"
                    binding.deviceInfo.text = "测量状态${data.rtParam.currentState}：${
                        when (data.rtParam.currentState) {
                            0 -> "空闲待机(导联脱落)"
                            1 -> "测量准备(主机丢弃前段波形阶段)"
                            2 -> "记录中"
                            3 -> "分析存储中"
                            4 -> "已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)"
                            5 -> "记录小于30s(记录中状态直接切换至此状态)"
                            6 -> "重测已达6次，进入待机"
                            7 -> "导联断开"
                            else -> ""
                        }
                    }"
                }
            }
        //------------------------------lew------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewRtData)
            .observe(this) {
                val data = it.data as RtData
                binding.deviceInfo.text = "$data"
            }
        //------------------------------pc80b------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bTrackData)
            .observe(this) { event ->
                val rtData = event.data as PC80BleResponse.RtTrackData
                rtData.let { data ->
                    viewModel.ecgHr.value = data.hr
                    data.data.ecgData?.wFs.let {
                        DataController.receive(it)
                    }
                    binding.dataStr.text = data.toString()
                    binding.deviceInfo.text = "导联状态：${if (data.leadOff == 1) "脱落" else "正常"}\n" +
                            "测量状态${data.stage}：${
                                when (data.stage) {
                                    0 -> "正在检测通道"
                                    1 -> "正在准备测量"
                                    2 -> "测量进行中"
                                    3 -> "开始分析"
                                    4 -> "报告测量结果"
                                    5 -> "跟踪停止"
                                    else -> ""
                                }
                            }"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousData)
            .observe(this) { event ->
                val rtData = event.data as PC80BleResponse.RtContinuousData
                rtData.let { data ->
                    viewModel.ecgHr.value = data.hr
                    data.ecgData?.wFs.let {
                        DataController.receive(it)
                    }
                    binding.dataStr.text = data.toString()
                    binding.deviceInfo.text = "导联状态：${if (data.leadOff) "脱落" else "正常"}"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousDataEnd)
            .observe(this) {
                Toast.makeText(context, "测量已终止", Toast.LENGTH_SHORT).show()
            }
        //------------------------------bp2 bp2a bp2t------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
            .observe(this) {
                val bp2Rt = it.data as Bp2BleRtData

                val data: Any
                when (bp2Rt.rtWave.waveDataType) {
                    0 -> {
                        data = Bp2DataBpIng(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data.pressure
                        viewModel.bpPr.value = data.pr
                        binding.deviceInfo.text = "放气：${if (data.isDeflate) "是" else "否"}\n" +
                                "脉搏波：${if (data.isPulse) "有" else "没有"}"
                    }
                    1 -> {
                        data = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data.pressure
                        viewModel.sys.value = data.sys
                        viewModel.dia.value = data.dia
                        viewModel.mean.value = data.mean
                        viewModel.bpPr.value = data.pr
                        binding.deviceInfo.text = "放气：${if (data.isDeflate) "是" else "否"}\n" +
                                "测量结果${data.code}：${
                                    when (data.code) {
                                        0 -> "正常"
                                        1 -> "无法分析（袖套绑的太松，充气慢，缓慢漏气，气容大）"
                                        2 -> "波形混乱（打气过程中检测到胳膊有动作或者有其他干扰）"
                                        3 -> "信号弱，检测不到脉搏波（有干扰袖套的衣物）"
                                        else -> "设备错误（堵阀，血压测量超量程，袖套漏气严重，软件系统异常，硬件系统错误，以及其他异常）"
                                    }
                                }"
                    }
                    2 -> {
                        data = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data.hr
                        binding.measureDuration.text = " ${data.curDuration} s"
                        binding.deviceInfo.text = "导联状态：${if (data.isLeadOff) "脱落" else "正常"}\n" +
                                "信号弱：${if (data.isPoolSignal) "是" else "否"}"

                        val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                        val len = mvs.size
                        for (i in 0 until len) {
                            val temp = DataConvert.filter(mvs[i].toDouble(), false)
                            if (temp.isNotEmpty()) {
                                val d = FloatArray(temp.size)
                                for (j in d.indices) {
                                    d[j] = temp[j].toFloat()
                                }
                                DataController.receive(d)
                            }
                        }
                    }
                    3 -> {
                        data = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data.hr
                        binding.deviceInfo.text = "测量结果：${data.diagnosis.resultMess}\n" +
                                "qrs：${data.qrs}\n" +
                                "pvcs：${data.pvcs}\n" +
                                "qtc：${data.qtc}"
                    }
                    else -> data = ""
                }

                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: ${bp2Rt.rtWave.waveDataType} $data ----rtState-- ${bp2Rt.rtState}"
            }
        //------------------------------bp2w------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData)
            .observe(this) {
                val bp2Rt = it.data as Bp2BleRtData

                val data1: Any
                when (bp2Rt.rtWave.waveDataType) {
                    0 -> {
                        data1 = Bp2DataBpIng(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data1.pressure
                        viewModel.bpPr.value = data1.pr
                        binding.deviceInfo.text = "放气：${if (data1.isDeflate) "是" else "否"}\n" +
                                "脉搏波：${if (data1.isPulse) "有" else "没有"}"
                    }
                    1 -> {
                        data1 = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data1.pressure
                        viewModel.sys.value = data1.sys
                        viewModel.dia.value = data1.dia
                        viewModel.mean.value = data1.mean
                        viewModel.bpPr.value = data1.pr
                        binding.deviceInfo.text = "放气：${if (data1.isDeflate) "是" else "否"}\n" +
                                "测量结果${data1.code}：${
                                    when (data1.code) {
                                        0 -> "正常"
                                        1 -> "无法分析（袖套绑的太松，充气慢，缓慢漏气，气容大）"
                                        2 -> "波形混乱（打气过程中检测到胳膊有动作或者有其他干扰）"
                                        3 -> "信号弱，检测不到脉搏波（有干扰袖套的衣物）"
                                        else -> "设备错误（堵阀，血压测量超量程，袖套漏气严重，软件系统异常，硬件系统错误，以及其他异常）"
                                    }
                                }"
                    }
                    2 -> {
                        data1 = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data1.hr
                        binding.measureDuration.text = " ${data1.curDuration} s"
                        binding.deviceInfo.text = "导联状态：${if (data1.isLeadOff) "脱落" else "正常"}\n" +
                                "信号弱：${if (data1.isPoolSignal) "是" else "否"}"
                    }
                    3 -> {
                        data1 = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data1.hr
                        binding.deviceInfo.text = "测量结果：${data1.diagnosis.resultMess}\n" +
                                "qrs：${data1.qrs}\n" +
                                "pvcs：${data1.pvcs}\n" +
                                "qtc：${data1.qtc}"
                    }
                    else -> data1 = ""
                }
                val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                DataController.receive(mvs)
                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: " + bp2Rt.rtWave.waveDataType + " " + data1.toString() + "----rtState--" + bp2Rt.rtState.toString()
            }
        //------------------------------le bp2w------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtData)
            .observe(this) {
                val bp2Rt = it.data as Bp2BleRtData

                val data2: Any
                when (bp2Rt.rtWave.waveDataType) {
                    0 -> {
                        data2 = Bp2DataBpIng(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data2.pressure
                        viewModel.bpPr.value = data2.pr
                        binding.deviceInfo.text = "放气：${if (data2.isDeflate) "是" else "否"}\n" +
                                "脉搏波：${if (data2.isPulse) "有" else "没有"}"
                    }
                    1 -> {
                        data2 = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data2.pressure
                        viewModel.sys.value = data2.sys
                        viewModel.dia.value = data2.dia
                        viewModel.mean.value = data2.mean
                        viewModel.bpPr.value = data2.pr
                        binding.deviceInfo.text = "放气：${if (data2.isDeflate) "是" else "否"}\n" +
                                "测量结果${data2.code}：${
                                    when (data2.code) {
                                        0 -> "正常"
                                        1 -> "无法分析（袖套绑的太松，充气慢，缓慢漏气，气容大）"
                                        2 -> "波形混乱（打气过程中检测到胳膊有动作或者有其他干扰）"
                                        3 -> "信号弱，检测不到脉搏波（有干扰袖套的衣物）"
                                        else -> "设备错误（堵阀，血压测量超量程，袖套漏气严重，软件系统异常，硬件系统错误，以及其他异常）"
                                    }
                                }"
                    }
                    2 -> {
                        data2 = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data2.hr
                        binding.measureDuration.text = " ${data2.curDuration} s"
                        binding.deviceInfo.text = "导联状态：${if (data2.isLeadOff) "脱落" else "正常"}\n" +
                                "信号弱：${if (data2.isPoolSignal) "是" else "否"}"
                    }
                    3 -> {
                        data2 = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data2.hr
                        binding.deviceInfo.text = "测量结果：${data2.diagnosis.resultMess}\n" +
                                "qrs：${data2.qrs}\n" +
                                "pvcs：${data2.pvcs}\n" +
                                "qtc：${data2.qtc}"
                    }
                    else -> data2 = ""
                }
                val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                DataController.receive(mvs)
                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: " + bp2Rt.rtWave.waveDataType + " " + data2.toString() + "----rtState--" + bp2Rt.rtState.toString()
            }
        //------------------------------bpm------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData)
            .observe(this) {
                val rtData = it.data as BpmBleResponse.RtData
                viewModel.ps.value = rtData.ps
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureResult)
            .observe(this) {
                val rtData = it.data as BpmBleResponse.RecordData
                viewModel.sys.value = rtData.sys
                viewModel.dia.value = rtData.dia
                viewModel.bpPr.value = rtData.pr
                binding.dataStr.text = "$rtData"
                binding.deviceInfo.text = "测量时间：${getTimeString(rtData.year, rtData.month, rtData.day, rtData.hour, rtData.minute, 0)}\n" +
                        "测量结果：${if (rtData.regularHrFlag) "心率不齐" else "心率正常"}\n" +
                        "用户id：${rtData.deviceUserId}\n" +
                        "记录序号：${rtData.storeId}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult)
            .observe(this) {
                val rtData = it.data as BpmBleResponse.ErrorResult
                binding.dataStr.text = "$rtData"
                binding.deviceInfo.text = "测量结果错误：${rtData.resultMessZh}"
            }

        //------------------------------oxy------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).observeForever { event ->
            (event.data as OxyBleResponse.RtWave).let { data ->
                activity?.let {
                    //接收数据 开始添加采集数据
                    mainViewModel.checkStartCollect(it, data.waveByte)

                    toPlayAlarm(data.pr)
                    OxyDataController.receive(data.wFs)
                    when (o2RtType) {
                        0 -> LpBleUtil.oxyGetRtParam(event.model)
                        1 -> LpBleUtil.oxyGetRtWave(event.model)
                        2 -> LpBleUtil.oxyGetPpgRt(event.model)
                    }
                    viewModel.oxyPr.value = data.pr
                    viewModel.spo2.value = data.spo2
                    viewModel.oxyPr.value = data.pr
                }
            }
        }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData)
            .observe(this) {
                val data = it.data as OxyBleResponse.RtParam
                viewModel.oxyPr.value = data.pr
                viewModel.spo2.value = data.spo2
                viewModel.pi.value = data.pi.div(10f)
                viewModel.oxyPr.value = data.pr
                mainViewModel._battery.value = "${data.battery} %"
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(it.model)
                    1 -> LpBleUtil.oxyGetRtWave(it.model)
                    2 -> LpBleUtil.oxyGetPpgRt(it.model)
                }
                binding.dataStr.text = data.toString()
                binding.deviceInfo.text = "体动：${data.vector}\n" +
                        "导联状态：${if (data.state == 0) "脱落" else "正常"}\n" +
                        "导联脱落倒计时：${data.countDown}\n" +
                        "充电状态${data.batteryState}：${
                            when (data.batteryState) {
                                0 -> "没有充电"
                                1 -> "充电中"
                                2 -> "充电完成"
                                else -> ""
                            }
                        }"
            }
        // o2ring ppg
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this) {
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(it.model)
                    1 -> LpBleUtil.oxyGetRtWave(it.model)
                    2 -> LpBleUtil.oxyGetPpgRt(it.model)
                }
                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.let { data ->
                    if (binding.collectPpg.isChecked) {
                        collectBytesData = add(collectBytesData, data.irByteArray)
                        for (i in data.irArray) {
                            val temp = i.div(256)
                            collectIntsData.add(temp)
                        }
                        /*for (i in data.irRedArray) {
                            val temp = i.div(256)
                            collectIntsData.add(temp)
                            Log.d(TAG, "i: $i, temp: $temp")
                        }*/
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyPpgRes------------")
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(it.model)
                    1 -> LpBleUtil.oxyGetRtWave(it.model)
                    2 -> LpBleUtil.oxyGetPpgRt(it.model)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtWaveRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyRtWaveRes------------")
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(it.model)
                    1 -> LpBleUtil.oxyGetRtWave(it.model)
                    2 -> LpBleUtil.oxyGetPpgRt(it.model)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyRtParamRes------------")
                when (o2RtType) {
                    0 -> LpBleUtil.oxyGetRtParam(it.model)
                    1 -> LpBleUtil.oxyGetRtWave(it.model)
                    2 -> LpBleUtil.oxyGetPpgRt(it.model)
                }
            }
        //------------------------------pc60fw------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBattery)
            .observe(this) {
                val data = it.data as PC60FwBleResponse.Battery
                val level = byte2UInt(data.batteryLevel)
                mainViewModel._battery.value = "${level.times(25)} - ${(level+1).times(25)} %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataWave)
            .observe(this) {
                val rtWave = it.data as PC60FwBleResponse.RtDataWave
                OxyDataController.receive(rtWave.waveIntData)
                Log.d("test12345", "" + Arrays.toString(rtWave.waveIntData))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam)
            .observe(this) {
                val rtData = it.data as PC60FwBleResponse.RtDataParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                binding.dataStr.text = rtData.toString()
                mainViewModel._battery.value = "${rtData.battery.times(25)} - ${(rtData.battery+1).times(25)} %"
                binding.deviceInfo.text = "探头脱落，手指未接入：${rtData.isProbeOff}\n" +
                        "脉搏检测：${rtData.isPulseSearching}"
            }
        //------------------------------pc100------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtWave)
            .observe(this) {
                val rtWave = it.data as Pc100BleResponse.RtBoWave
                OxyDataController.receive(rtWave.waveIntData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtParam)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.RtBoParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                binding.deviceInfo.text = "血氧探头检测中：${rtData.isDetecting}\n" +
                        "血氧脉搏扫描中：${rtData.isScanning}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpRtData)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.RtBpData
                rtData.let { data ->
                    viewModel.ps.value = data.psValue
                    binding.dataStr.text = rtData.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.BpResult
                rtData.let { data ->
                    viewModel.sys.value = data.sys
                    viewModel.dia.value = data.dia
                    viewModel.mean.value = data.map
                    viewModel.bpPr.value = data.plus
                    binding.dataStr.text = rtData.toString()
                    binding.deviceInfo.text = "血压测量结果：${rtData.resultMess}"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.BpResultError
                binding.dataStr.text = "$rtData"
                binding.deviceInfo.text = "血压错误结果：${rtData.errorMess}\n" +
                        "血压错误编码类型：${rtData.errorType}\n" +
                        "血压错误编码号：${rtData.errorNum}"
            }
        //------------------------------ap20------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoWave)
            .observe(this) {
                val rtWave = it.data as Ap20BleResponse.RtBoWave
                OxyDataController.receive(rtWave.waveIntData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoParam)
            .observe(this) {
                val rtData = it.data as Ap20BleResponse.RtBoParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                binding.dataStr.text = rtData.toString()
                mainViewModel._battery.value = "${rtData.battery.times(25)} - ${(rtData.battery+1).times(25)} %"
                binding.deviceInfo.text = "探头脱落，手指未接入：${rtData.isProbeOff}\n" +
                        "脉搏检测：${rtData.isPulseSearching}\n" +
                        "探头故障或使用不当：${rtData.isCheckProbe}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave)
            .observe(this) {
//                val rtWave = it.data as Ap20BleResponse.RtBreathWave
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam)
            .observe(this) {
//                val rtData = it.data as Ap20BleResponse.RtBreathParam
            }
        //------------------------------vetcorder------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Vetcorder.EventVetcorderInfo)
            .observe(this) {
                val rtData = it.data as VetcorderInfo
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                viewModel.ecgHr.value = rtData.hr

                DataController.receive(rtData.ecgwFs)
                OxyDataController.receive(rtData.spo2wIs)

                binding.dataStr.text = rtData.toString()
                mainViewModel._battery.value = "${rtData.battery} %"
                binding.deviceInfo.text = "ECG -QRS：${rtData.qrs}\n" +
                        "ECG -ST：${rtData.st}\n" +
                        "ECG -PVCs：${rtData.pvcs}\n" +
                        "ECG –R wave mark：${rtData.mark}\n" +
                        "ECG -note：${rtData.ecgNote}\n" +
                        "SpO2-pulse sound：${rtData.pulseSound}\n" +
                        "SpO2-note：${rtData.spo2Note}"
            }
        //------------------------------sp20------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtWave)
            .observe(this) {
                val rtWave = it.data as Sp20BleResponse.RtWave
                OxyDataController.receive(rtWave.waveIntData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20RtParam)
            .observe(this) {
                val rtData = it.data as Sp20BleResponse.RtParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                binding.dataStr.text = rtData.toString()
                mainViewModel._battery.value = "${rtData.battery.times(25)} - ${(rtData.battery+1).times(25)} %"
                binding.deviceInfo.text = "探头脱落，手指未接入：${rtData.isProbeOff}\n" +
                        "脉搏检测：${rtData.isPulseSearching}\n" +
                        "探头故障或使用不当：${rtData.isCheckProbe}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20TempData)
            .observe(this) {
                val data = it.data as Sp20BleResponse.TempData
                val result = if (data.result == 1) {
                    "体温过低"
                } else if (data.result == 2) {
                    "体温过高"
                } else {
                    if (data.unit == 1) {
                        "体温 ：${data.value} ℉"
                    } else {
                        "体温 ：${data.value} ℃"
                    }
                }
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        //------------------------------PC68B------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtWave)
            .observe(this) {
                val rtWave = it.data as Pc68bBleResponse.RtWave
                OxyDataController.receive(rtWave.waveIntData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bRtParam)
            .observe(this) {
                val rtData = it.data as Pc68bBleResponse.RtParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi
                binding.dataStr.text = rtData.toString()
                mainViewModel._battery.value = "${rtData.battery.times(25)} - ${(rtData.battery+1).times(25)} %"
                binding.deviceInfo.text = "探头脱落，手指未接入：${rtData.isProbeOff}\n" +
                        "脉搏检测：${rtData.isPulseSearching}\n" +
                        "探头故障或使用不当：${rtData.isCheckProbe}"
            }
        //--------------------------vtm20f----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtWave)
            .observe(this) {
                val rtWave = it.data as Vtm20fBleResponse.RtWave
                OxyDataController.receive(intArrayOf(rtWave.wave))
                binding.dataStr.text = rtWave.toString()
                binding.deviceInfo.text = "脉搏波音标记：${rtWave.pulseSound}\n" +
                        "导连脱落标志：${rtWave.isSensorOff}\n" +
                        "干扰状态标记：${rtWave.isDisturb}\n" +
                        "低灌注标记：${rtWave.isLowPi}\n" +
                        "棒图：${rtWave.barChart}\n" +
                        "包序号：${rtWave.seqNo}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtParam)
            .observe(this) {
                val rtData = it.data as Vtm20fBleResponse.RtParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi
            }
        //--------------------------aoj20a----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observe(this) {
                val data = it.data as Aoj20aBleResponse.TempRtData
                binding.deviceInfo.text = "测温模式${data.mode}：${data.modeMsg}\n" +
                        "温度：${data.temp} ℃"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observe(this) {
                val data = it.data as Aoj20aBleResponse.ErrorMsg
                binding.deviceInfo.text = "错误结果${data.code}：${data.codeMsg}"
            }
        //-------------------------checkme pod------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtDataError)
            .observe(this) {
                binding.dataStr.text = "EventCheckmePodRtDataError"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtData)
            .observe(this) {
                val rtData = it.data as CheckmePodBleResponse.RtData
                viewModel.oxyPr.value = rtData.param.pr
                viewModel.spo2.value = rtData.param.spo2
                viewModel.pi.value = rtData.param.pi
                OxyDataController.receive(rtData.wave.wFs)
                binding.dataStr.text = "$rtData"
                mainViewModel._battery.value = "${rtData.param.battery} %"
                binding.deviceInfo.text = "温度：${rtData.param.temp} ℃\n" +
                        "血氧探头状态${rtData.param.oxyState}：${
                            when (rtData.param.oxyState) {
                                0 -> "未接入血氧电缆"
                                1 -> "未接入手指"
                                2 -> "接入手指"
                                else -> ""
                            }
                        }\n" +
                        "体温探头状态${rtData.param.tempState}：${
                            when (rtData.param.tempState) {
                                0 -> "未接入"
                                1 -> "接入"
                                else -> ""
                            }
                        }\n" +
                        "充电状态${rtData.param.batteryState}：${
                            when (rtData.param.batteryState) {
                                0 -> "没有充电"
                                1 -> "充电中"
                                2 -> "充电完成"
                                3 -> "低电量"
                                else -> ""
                            }
                        }\n" +
                        "运行状态${rtData.param.runStatus}：${
                            when (rtData.param.runStatus) {
                                0 -> "空闲"
                                1 -> "测量准备"
                                2 -> "测量中"
                                else -> ""
                            }
                        }"
            }
        //-------------------------pc300-------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyWave)
            .observe(this) {
                val data = it.data as Pc300BleResponse.RtOxyWave
                OxyDataController.receive(data.waveIntData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyParam)
            .observe(this) {
                val data = it.data as Pc300BleResponse.RtOxyParam
                viewModel.oxyPr.value = data.pr
                viewModel.spo2.value = data.spo2
                viewModel.pi.value = data.pi
                binding.deviceInfo.text = "血氧探头脱落，手指未接入：${data.isProbeOff}\n" +
                        "血氧脉搏检测：${data.isPulseSearching}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave)
            .observe(this) {
                val data = it.data as Pc300BleResponse.RtEcgWave
                DataController.receive(data.wFs)
                binding.deviceInfo.text = "心电包号：${data.seqNo}\n" +
                        "心电采样点位数类型：${data.digit}\n" +
                        "心电导联脱落：${data.isProbeOff}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.EcgResult
                viewModel.ecgHr.value = data.hr
                binding.deviceInfo.text = "心率：${data.hr}\n" +
                        "测量结果${data.result}：${data.resultMess}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtBpData)
            .observe(this) {
                val data = it.data as Int
                viewModel.ps.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.BpResult
                viewModel.sys.value = data.sys
                viewModel.dia.value = data.dia
                viewModel.mean.value = data.map
                viewModel.bpPr.value = data.plus
                binding.dataStr.text = "$data"
                binding.deviceInfo.text = "血压测量结果${data.result}：${data.resultMess}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.BpResultError
                binding.dataStr.text = "$data"
                binding.deviceInfo.text = "血压错误结果：${data.errorMess}\n" +
                        "血压错误编码类型：${data.errorType}\n" +
                        "血压错误编码号：${data.errorNum}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GluResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.GluResult
                binding.dataStr.text = "$data"
                binding.deviceInfo.text = "血糖结果类型${data.result}：${data.resultMess} ℃\n" +
                        "血糖单位${data.unit}：${
                            when (data.unit) {
                                0 -> "mmol/L"
                                1 -> "mg/dL"
                                else -> ""
                            }
                        }\n" +
                        "血糖值：${data.data}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStart)
            .observe(this) {
                LpBleUtil.startRtTask()
            }
        // ------------------------le S1--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1RtData)
            .observe(this) {
                val rtData = it.data as LeS1BleResponse.RtData
                rtData.let { data ->
                    Log.d("er1 data ", "len = ${data.wave.wave.size}")
                    DataController.receive(data.wave.wFs)
                    binding.dataStr.text = data.param.toString()
                    viewModel.ecgHr.value = data.param.hr
                    binding.measureDuration.text = " ${data.param.recordTime} s"
                    binding.deviceInfo.text = "运行状态${data.param.runStatus}：${
                                when (data.param.runStatus) {
                                    0 -> "待机"
                                    1 -> "称端测量中"
                                    2 -> "称端测量结束"
                                    3 -> "心电准备阶段"
                                    4 -> "心电测量中"
                                    5 -> "心电正常结束"
                                    6 -> "带阻抗心电异常结束"
                                    7 -> "不带阻抗异常结束"
                                    else -> ""
                                }
                            }\n" +
                            "心电导联状态：${data.param.leadOff}\n" +
                            "体重稳定状态：${data.scaleData.stable}\n" +
                            "体重单位${data.scaleData.unit}：${
                                when (data.scaleData.unit) {
                                    0 -> "kg"
                                    1 -> "LB"
                                    2 -> "ST"
                                    3 -> "LB-ST"
                                    4 -> "斤"
                                    else -> ""
                                }
                            }\n" +
                            "体重值KG：${data.scaleData.weight}\n" +
                            "阻抗值Ω：${data.scaleData.resistance}"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile).observe(this) { event ->
            (event.data as Boolean).let {
                binding.dataStr.text = "没有文件 $it"
            }
        }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrDeviceInfo)
            .observe(this) {
                val data = it.data as FhrBleResponse.DeviceInfo
                binding.dataStr.text = "$data"
                binding.deviceInfo.text = "设备名称：${data.deviceName}\n" +
                        "心率数据：${data.hr}\n" +
                        "音量数据：${data.volume}\n" +
                        "心音强度数据：${data.strength}\n" +
                        "电量数据：${data.battery}"
            }
        //------------------------------PoctorM3102--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PoctorM3102.EventPoctorM3102Data)
            .observe(this) {
                val data = it.data as PoctorM3102Data
                binding.deviceInfo.text = when (data.type) {
                    0 -> "血糖 : ${if (data.normal) {"${data.result} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    1 -> "尿酸 : ${if (data.normal) {"${data.result} umol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    3 -> "血酮 : ${if (data.normal) {"${data.result} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    else -> "数据出错 : \n$data"
                }
            }
        //------------------------------Bioland-BGM--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown)
            .observe(this) {
                val data = it.data as Int
                binding.deviceInfo.text = "倒计时 : $data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData)
            .observe(this) {
                val data = it.data as BiolandBgmBleResponse.GluData
                binding.deviceInfo.text = "血糖 : ${data.resultMg} mg/dL ${data.resultMmol} mmol/L\n时间 : ${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData)
            .observe(this) {
                val data = it.data as Boolean
                Toast.makeText(context, "没有文件", Toast.LENGTH_SHORT).show()
            }
        //------------------------------ER3--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3RtData)
            .observe(this) {
                val data = it.data as Er3BleResponse.RtData
                Er3DataController.receive(data.wave.waveMvs)
                Log.d("Er3Test", "data.wave.waveMvs.size ${data.wave.waveMvs.size}")
                binding.er3TempInfo.text = "固件版本：${mainViewModel._er1Info.value?.fwV}\n" +
                        "温度：${data.param.temp} ℃"
                mainViewModel._battery.value = "${data.param.battery} %"
                binding.deviceInfo.text = "心率：${data.param.hr} bpm\n" +
                        "体温：${data.param.temp} ℃\n" +
                        "血氧：${data.param.spo2} %\n" +
                        "pi：${data.param.pi} %\n" +
                        "脉率：${data.param.pr}\n" +
                        "呼吸率：${data.param.respRate}\n" +
                        "电池状态${data.param.batteryStatus}：${
                            when (data.param.batteryStatus) {
                                0 -> "正常使用"
                                1 -> "充电中"
                                2 -> "充满"
                                3 -> "低电量"
                                else -> ""
                            }
                        }\n" +
                        "心电导联线状态：${data.param.isInsertEcgLeadWire}\n" +
                        "血氧状态${data.param.oxyStatus}：${
                            when (data.param.oxyStatus) {
                                0 -> "未接入血氧"
                                1 -> "血氧状态正常"
                                2 -> "血氧手指脱落"
                                3 -> "探头故障"
                                else -> ""
                            }
                        }\n" +
                        "体温状态：${data.param.isInsertTemp}\n" +
                        "测量状态${data.param.measureStatus}：${
                            when (data.param.measureStatus) {
                                0 -> "空闲"
                                1 -> "准备状态"
                                2 -> "正式测量状态"
                                else -> ""
                            }
                        }\n" +
                        "测量中是否有配置设备：${data.param.isHasDevice}\n" +
                        "测量中是否有配置体温设备：${data.param.isHasTemp}\n" +
                        "测量中是否有配置血氧设备：${data.param.isHasOxy}\n" +
                        "测量中是否有配置呼吸设备：${data.param.isHasRespRate}\n" +
                        "已记录时长：${data.param.recordTime}\n" +
                        "开始测量时间：${data.param.year}-${data.param.month}-${data.param.day} ${data.param.hour}:${data.param.minute}:${data.param.second}\n" +
                        "导联类型${data.param.leadType}：${
                            when (data.param.leadType) {
                                0 -> "LEAD_12，12导"
                                1 -> "LEAD_6，6导"
                                2 -> "LEAD_5，5导"
                                3 -> "LEAD_3，3导"
                                4 -> "LEAD_3_TEMP，3导带体温"
                                5 -> "LEAD_3_LEG，3导胸贴"
                                6 -> "LEAD_5_LEG，5导胸贴"
                                7 -> "LEAD_6_LEG，6导胸贴"
                                0xFF -> "LEAD_NONSUP，不支持的导联"
                                else -> "UNKNOWN，未知导联"
                            }
                        }\n" +
                        "一次性导联的sn：${data.param.leadSn}\n" +
                        "I导联脱落：${data.param.isLeadOffI}\n" +
                        "II导联脱落：${data.param.isLeadOffII}\n" +
                        "III导联脱落：${data.param.isLeadOffIII}\n" +
                        "aVR导联脱落：${data.param.isLeadOffaVR}\n" +
                        "aVL导联脱落：${data.param.isLeadOffaVL}\n" +
                        "aVF导联脱落：${data.param.isLeadOffaVF}\n" +
                        "V1导联脱落：${data.param.isLeadOffV1}\n" +
                        "V2导联脱落：${data.param.isLeadOffV2}\n" +
                        "V3导联脱落：${data.param.isLeadOffV3}\n" +
                        "V4导联脱落：${data.param.isLeadOffV4}\n" +
                        "V5导联脱落：${data.param.isLeadOffV5}\n" +
                        "V6导联脱落：${data.param.isLeadOffV6}\n" +
                        "${data.param}"
            }
        //------------------------------Lepod--------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodRtData)
            .observe(this) {
                val data = it.data as LepodBleResponse.RtData
                Er3DataController.receive(data.wave.waveMvs)
                Log.d("LepodTest", "data.wave.waveMvs.size ${data.wave.waveMvs.size}")
                mainViewModel._battery.value = "${data.param.battery} %"
                binding.deviceInfo.text = "心率：${data.param.hr} bpm\n" +
                        "体温：${data.param.temp} ℃\n" +
                        "血氧：${data.param.spo2} %\n" +
                        "pi：${data.param.pi} %\n" +
                        "脉率：${data.param.pr}\n" +
                        "呼吸率：${data.param.respRate}\n" +
                        "电池状态${data.param.batteryStatus}：${
                            when (data.param.batteryStatus) {
                                0 -> "正常使用"
                                1 -> "充电中"
                                2 -> "充满"
                                3 -> "低电量"
                                else -> ""
                            }
                        }\n" +
                        "心电导联线状态：${data.param.isInsertEcgLeadWire}\n" +
                        "血氧状态${data.param.oxyStatus}：${
                            when (data.param.oxyStatus) {
                                0 -> "未接入血氧"
                                1 -> "血氧状态正常"
                                2 -> "血氧手指脱落"
                                3 -> "探头故障"
                                else -> ""
                            }
                        }\n" +
                        "体温状态：${data.param.isInsertTemp}\n" +
                        "测量状态${data.param.measureStatus}：${
                            when (data.param.measureStatus) {
                                0 -> "空闲"
                                1 -> "检测导联"
                                2 -> "准备状态"
                                3 -> "正式测量"
                                else -> ""
                            }
                        }\n" +
                        "已记录时长：${data.param.recordTime}\n" +
                        "开始测量时间：${data.param.year}-${data.param.month}-${data.param.day} ${data.param.hour}:${data.param.minute}:${data.param.second}\n" +
                        "导联类型${data.param.leadType}：${
                            when (data.param.leadType) {
                                0 -> "LEAD_12，12导"
                                1 -> "LEAD_6，6导"
                                2 -> "LEAD_5，5导"
                                3 -> "LEAD_3，3导"
                                4 -> "LEAD_3_TEMP，3导带体温"
                                5 -> "LEAD_3_LEG，3导胸贴"
                                6 -> "LEAD_5_LEG，5导胸贴"
                                7 -> "LEAD_6_LEG，6导胸贴"
                                0xFF -> "LEAD_NONSUP，不支持的导联"
                                else -> "UNKNOWN，未知导联"
                            }
                        }\n" +
                        "RA导联脱落：${data.param.isLeadOffRA}\n" +
                        "RL导联脱落：${data.param.isLeadOffRL}\n" +
                        "LA导联脱落：${data.param.isLeadOffLA}\n" +
                        "LL导联脱落：${data.param.isLeadOffLL}\n" +
                        "V1导联脱落：${data.param.isLeadOffV1}\n" +
                        "V2导联脱落：${data.param.isLeadOffV2}\n" +
                        "V3导联脱落：${data.param.isLeadOffV3}\n" +
                        "V4导联脱落：${data.param.isLeadOffV4}\n" +
                        "V5导联脱落：${data.param.isLeadOffV5}\n" +
                        "V6导联脱落：${data.param.isLeadOffV6}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStart)
            .observe(this) {
                Toast.makeText(context, "开始测量", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStop)
            .observe(this) {
                Toast.makeText(context, "结束测量", Toast.LENGTH_SHORT).show()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VCOMIN.EventVcominRtHr)
            .observe(this) {
                val data = it.data as VcominData
                binding.deviceInfo.text = "hr1 : ${data.hr1}, hr2 : ${data.hr2}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AD5.EventAd5RtHr)
            .observe(this) {
                val data = it.data as Ad5Data
                binding.deviceInfo.text = "hr1 : ${data.hr1}, hr2 : ${data.hr2}"
            }
        //------------------------vtm01----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01RtData)
            .observe(this) {
                val data = it.data as Vtm01BleResponse.RtData
                OxyDataController.receive(data.waveInt)
                viewModel.oxyPr.value = data.param.pr
                viewModel.spo2.value = data.param.spo2
                viewModel.pi.value = data.param.pi
                binding.deviceInfo.text = "探头状态${data.param.probeState}：${
                    when (data.param.probeState) {
                        0 -> "未检测到手指"
                        1 -> "正常测量"
                        2 -> "探头故障"
                        else -> ""
                    }
                }"
            }
        //------------------------btp----------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpRtData)
            .observe(this) {
                val data = it.data as BtpBleResponse.RtData
                binding.deviceInfo.text = "心率：${data.hr}\n"
                if (type == 1) {
                    binding.deviceInfo.text = binding.deviceInfo.text.toString() + "温度：${String.format("%.2f", (32+data.temp*1.8))} ℉\n"
                } else {
                    binding.deviceInfo.text = binding.deviceInfo.text.toString() + "温度：${data.temp} ℃\n"
                }
                binding.deviceInfo.text = binding.deviceInfo.text.toString() + "是否测量中：${data.isWearing}\n" +
                        "心率可信度：${data.level}\n" +
                        "心率状态${data.hrStatus}：${
                            when (data.hrStatus) {
                                0 -> "正常"
                                1 -> "心率低异常"
                                2 -> "心率高异常"
                                else -> ""
                            }
                        }\n" +
                        "温度状态${data.tempStatus}：${
                            when (data.tempStatus) {
                                0 -> "正常"
                                3 -> "温度低异常"
                                4 -> "温度高异常"
                                else -> ""
                            }
                        }"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetConfig)
            .observe(this) {
                val data = it.data as BtpBleResponse.ConfigInfo
                type = data.tempUnit
            }
        // ----------------------R20-------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20EchoData)
            .observe(this) {
                val data = it.data as R20BleResponse.BleResponse
                wirelessData.totalSize++
                wirelessData.receiveBytes += data.bytes.size
                wirelessData.oneDelay = System.currentTimeMillis() - recordTime
                wirelessData.totalDelay += wirelessData.oneDelay
                recordTime = System.currentTimeMillis()
                if ((data.pkgNo != 0) && ((data.pkgNo - tempSeqNo) != 1)) {
                    val pkg = if ((data.pkgNo > tempSeqNo)) {
                        data.pkgNo - tempSeqNo
                    } else {
                        data.pkgNo - (255-tempSeqNo)
                    }
                    wirelessData.missSize += pkg
                    wirelessData.errorBytes += pkg*20
                }
                wirelessData.errorPercent = wirelessData.errorBytes.div(wirelessData.totalBytes*1.0).times(100)
                wirelessData.missPercent = wirelessData.missSize.div(sendSeqNo*1.0).times(100)
                tempSeqNo = data.pkgNo
                binding.wirelessDataLayout.testData.text = "数据总量：${wirelessData.totalBytes.div(1024.0)} kb\n" +
                        "总包数：${wirelessData.totalSize}\n" +
                        "丢包数：${wirelessData.missSize}\n" +
                        "错误字节：${wirelessData.errorBytes}\n" +
                        "单次时延：${wirelessData.oneDelay} ms\n" +
                        "总时延：${wirelessData.totalDelay.div(wirelessData.totalSize)} ms\n" +
                        "丢包率：${String.format("%.3f", wirelessData.missPercent)} %\n" +
                        "误码率：${String.format("%.3f", wirelessData.errorPercent)} %"
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toPlayAlarm(newPr: Int){
        mainViewModel.oxyPrAlarmFlag.value?.let { flag ->
            if (flag) {
                viewModel.oxyPr.value?.let { pr ->
                    if (pr != 0 && newPr == 0) context?.let { it1 ->
                        mainViewModel.playAlarm(
                            it1
                        )
                    }
                    if (pr != 0 && newPr != 0) {
                        mainViewModel.oxyInfo.value?.let {
                            if (newPr >= it.hrHighThr || newPr <= it.hrLowThr)
                                context?.let { it1 ->
                                    mainViewModel.playAlarm(it1)
                                }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        LpBleUtil.stopRtTask()
        stopWave()
        isStartWirelessTest = false
        handler.removeCallbacks(task)
        super.onDestroy()
    }

}