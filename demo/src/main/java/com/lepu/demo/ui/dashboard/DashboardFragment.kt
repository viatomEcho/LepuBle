package com.lepu.demo.ui.dashboard

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.data.DataController
import com.lepu.demo.data.OxyDataController
import com.lepu.demo.databinding.FragmentDashboardBinding
import com.lepu.demo.views.EcgBkg
import com.lepu.demo.views.EcgView
import com.lepu.demo.views.OxyView
import kotlin.math.floor

class DashboardFragment : Fragment(R.layout.fragment_dashboard){
    val TAG: String = "DashboardFragment"

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: DashboardViewModel by activityViewModels()

    private val binding: FragmentDashboardBinding by binding()

    var dataString = ""

    // 心电产品
    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView

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
                    100
                }
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LepuBleLog.d("DataRec: ${OxyDataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
            viewModel.dataOxySrc.value = OxyDataController.feed(viewModel.dataOxySrc.value, temp)
        }
    }

    private fun startWave(model: Int) {
        dataString = ""
        if (mainViewModel.runWave) {
            return
        }
        mainViewModel.runWave = true
        when(model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER2, Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LEW3 -> waveHandler.post(EcgWaveTask())
            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC100, Bluetooth.MODEL_PC_6N, Bluetooth.MODEL_AP20 -> waveHandler.post(OxyWaveTask())
            Bluetooth.MODEL_VETCORDER -> {
                waveHandler.post(EcgWaveTask())
                waveHandler.post(OxyWaveTask())
            }
        }

    }

    private fun stopWave() {
        mainViewModel.runWave = false
        DataController.clear()
        OxyDataController.clear()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLiveEvent(){
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).observeSticky(this, {
            startWave(it)
        })

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).observeSticky(this, {
            stopWave()
        })

        //------------------------------er1 duoek------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
            .observe(this, {
                val rtData = it.data as Er1BleResponse.RtData
                rtData.let { data ->
                    Log.d("er1 data ", "len = ${data.wave.wave.size}")
                    DataController.receive(data.wave.wFs)
                    binding.dataStr.text = data.param.toString()
                    viewModel.ecgHr.value = data.param.hr
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList).observe(this, { event ->
            (event.data as String).let {
                binding.dataStr.text = it
            }
        })
        //------------------------------er2------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData)
            .observe(this, {
                val rtData = it.data as Er2RtData
                rtData.let { data ->
                    Log.d("er2 data ", "len = ${data.waveData.size}")
                    DataController.receive(data.waveData.datas)
                    binding.dataStr.text = data.rtParam.toString()
                    viewModel.ecgHr.value = data.hr
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).observe(this, { event ->
            (event.data as Er2FileList).let {
                binding.dataStr.text = it.toString()
            }
        })
        //------------------------------lew3------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3RtData)
            .observe(this, {
                val rtData = it.data as LeW3RtData
                rtData.let { data ->
                    Log.d("lew3 data ", "len = ${data.waveData.size}")
                    DataController.receive(data.waveData.datas)
                    binding.dataStr.text = data.rtParam.toString()
                    viewModel.ecgHr.value = data.rtParam.hr
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3FileList).observe(this, { event ->
            (event.data as LeW3FileList).let {
                binding.dataStr.text = it.toString()
            }
        })
        //------------------------------pc80b------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bTrackData)
            .observe(this, {
                val rtData = it.data as PC80BleResponse.RtTrackData
                rtData.let { data ->
                    DataController.receive(data.data.ecgData!!.wFs)
                    binding.dataStr.text = data.toString()
                }
            })
        //------------------------------bp2 bp2a bp2w------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
            .observe(this, {
                val bp2Rt = it.data as Bp2BleRtData

                var data: Any
                when(bp2Rt.rtWave.waveDataType) {
                    0 -> {
                        data = Bp2DataBpIng(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data.pressure
                    }
                    1 -> {
                        data = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data.pressure
                        viewModel.sys.value = data.sys
                        viewModel.dia.value = data.dia
                        viewModel.mean.value = data.mean
                        viewModel.bpPr.value = data.pr
                    }
                    2 -> {
                        data = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        LepuBleLog.d("bp2 ecg hr = ${data.hr}")
                        viewModel.ecgHr.value = data.hr
                    }
                    3 -> {
                        data = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data.hr
                    }
                    else -> data = ""
                }

                LepuBleLog.d("bp2 ecg waveformSize = ${bp2Rt.rtWave.waveformSize}")

                val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                DataController.receive(mvs)

                binding.dataStr.text = "state: " + bp2Rt.rtWave.waveDataType + " " + data.toString()

            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList).observe(this, { event ->
            if (event.model == Bluetooth.MODEL_BP2W) {
                (event.data as Bp2BleFile).let {
                    when (it.type) {
                        Ble.File.ECG_TYPE -> {
                            val data = Bp2wEcgList(it.content)
                            binding.dataStr.text = data.toString()
                        }
                        Ble.File.BP_TYPE -> {
                            val data = Bp2wBpList(it.content)
                            binding.dataStr.text = data.toString()
                        }
                        Ble.File.USER_TYPE -> {
                            val data = Bp2wUserList(it.content)
                            binding.dataStr.text = data.toString()
                        }
                        else -> {
                            binding.dataStr.text = it.toString()
                        }
                    }
                }
            } else {
                (event.data as KtBleFileList).let {
                    binding.dataStr.text = it.toString()
                }
            }
        })
        //------------------------------bpm------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData)
            .observe(this, {
                val rtData = it.data as BpmCmd
                rtData.let { data ->
                    viewModel.ps.value = (data.data[0].toUInt() and 0xFFu).toInt()
                    binding.dataStr.text = data.toString()
                }
            })

        //------------------------------o2ring------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).observeForever { event ->
            (event.data as OxyBleResponse.RtWave).let { rtWave ->
                activity?.let {
                    //接收数据 开始添加采集数据
                    mainViewModel.checkStartCollect(it, rtWave.waveByte)

                    toPlayAlarm(rtWave.pr)

                    viewModel.oxyPr.value = rtWave.pr
                    viewModel.spo2.value = rtWave.spo2
                    LepuBleLog.d("o2ring pr = ${rtWave.pr}, spo2 = ${rtWave.spo2}")

                    OxyDataController.receive(rtWave.wFs)
                }
            }
        }
        // o2ring ppg
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this, {

                LpBleUtil.oxyGetPpgRt(it.model)

                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.let { data ->
                    Log.d("ppg", "len  = ${data.rawDataBytes.size}")
                }

            })
        //------------------------------pc60fw------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataWave)
            .observe(this, {
                val rtWave = it.data as PC60FwBleResponse.RtDataWave
                OxyDataController.receive(rtWave.waveIntData)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam)
            .observe(this, {
                val rtData = it.data as PC60FwBleResponse.RtDataParam
                viewModel.oxyPr.value = rtData.pr.toInt()
                viewModel.spo2.value = rtData.spo2.toInt()
                viewModel.pi.value = rtData.pi.toInt().div(10f)
                binding.dataStr.text = rtData.toString()
            })

        //------------------------------pc100------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtWave)
            .observe(this, {
                val rtWave = it.data as ByteArray
                binding.dataStr.text = bytesToHex(rtWave)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtParam)
            .observe(this, {
                val rtData = it.data as Pc100BleResponse.RtBoParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpRtData)
            .observe(this, {
                val rtData = it.data as Pc100BleResponse.RtBpData
                rtData.let { data ->
                    viewModel.ps.value = data.psValue
                    binding.dataStr.text = rtData.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this, {
                val rtData = it.data as Pc100BleResponse.BpResult
                rtData.let { data ->
                    viewModel.sys.value = data.sys
                    viewModel.dia.value = data.dia
                    viewModel.mean.value = data.map
                    viewModel.bpPr.value = data.plus
                    binding.dataStr.text = rtData.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this, {
                val rtData = it.data as Pc100BleResponse.BpResultError
                rtData.let { data ->
                    binding.dataStr.text = rtData.toString()
                }
            })
        //------------------------------ap10------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoWave)
            .observe(this, {
                val rtWave = it.data as Ap20BleResponse.RtBoWave
                OxyDataController.receive(rtWave.waveIntData)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBoParam)
            .observe(this, {
                val rtData = it.data as Ap20BleResponse.RtBoParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                binding.dataStr.text = rtData.toString()
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave)
            .observe(this, {
                val rtWave = it.data as Ap20BleResponse.RtBreathWave
                dataString += "\n rtWave : $rtWave"
                binding.dataStr.text = dataString
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam)
            .observe(this, {
                val rtData = it.data as Ap20BleResponse.RtBreathParam
                dataString += "\n rtData : $rtData"
                binding.dataStr.text = dataString
            })
        //------------------------------vetcorder------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Vetcorder.EventVetcorderInfo)
            .observe(this, {
                val rtData = it.data as VetcorderInfo
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
                viewModel.ecgHr.value = rtData.hr

                DataController.receive(rtData.ecgwFs)
                OxyDataController.receive(rtData.spo2wIs)

                binding.dataStr.text = rtData.toString()
            })

    }

    private fun initView() {

        mainViewModel.curBluetooth.observe(viewLifecycleOwner, {
            when (it!!.modelNo) {
                Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_ER2, Bluetooth.MODEL_PC80B, Bluetooth.MODEL_LEW3 -> {
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.bpLayout.visibility = View.GONE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2W -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_BPM -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_O2RING, Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC_6N, Bluetooth.MODEL_AP20 -> {
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                    binding.bpLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_PC100 -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_VETCORDER -> {
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.bpLayout.visibility = View.GONE
                }
            }
        })
        mainViewModel.bleState.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.ecgView.visibility = View.VISIBLE
                    binding.oxyView.visibility = View.VISIBLE

                } else {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.ecgView.visibility = View.INVISIBLE
                    binding.oxyView.visibility = View.INVISIBLE
                }
            }

        })

        //------------------------------ecg------------------------------
        binding.ecgBkg.post{
            initEcgView()
        }
        viewModel.ecgHr.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.hr.text = "?"
            } else {
                binding.hr.text = it.toString()
            }
        })
        binding.startRtEcg.setOnClickListener {
            LpBleUtil.startRtTask()
        }
        binding.stopRtEcg.setOnClickListener {
            // 停止实时任务后去获取设备列表
            LpBleUtil.stopRtTask {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
            }
        }
        viewModel.dataEcgSrc.observe(viewLifecycleOwner, {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
            }
        })

        //------------------------------bp------------------------------
        binding.startBp.setOnClickListener {
            LpBleUtil.startBp(mainViewModel.curBluetooth.value!!.modelNo)
        }
        binding.stopBp.setOnClickListener {
            LpBleUtil.stopBp(mainViewModel.curBluetooth.value!!.modelNo)
        }
        viewModel.ps.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvPs.text = "?"
            } else {
                binding.tvPs.text = it.toString()
            }
        })
        viewModel.sys.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvSys.text = "?"
            } else {
                binding.tvSys.text = it.toString()
            }
        })
        viewModel.dia.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvDia.text = "?"
            } else {
                binding.tvDia.text = it.toString()
            }
        })
        viewModel.mean.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvMean.text = "?"
            } else {
                binding.tvMean.text = it.toString()
            }
        })
        viewModel.bpPr.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvPrBp.text = "?"
            } else {
                binding.tvPrBp.text = it.toString()
            }
        })

        //------------------------------oxy------------------------------
        binding.oxyView.post{
            initOxyView()
        }
        viewModel.oxyPr.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvPr.text = "?"
            } else {
                binding.tvPr.text = it.toString()
            }
        })
        viewModel.spo2.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.tvOxy.text = "?"
            } else {
                binding.tvOxy.text = it.toString()
            }
        })
        viewModel.pi.observe(viewLifecycleOwner, {
            if (it == 0f) {
                binding.tvPi.text = "?"
            } else {
                binding.tvPi.text = it.toString()
            }
        })
        binding.startRtOxy.setOnClickListener {
            LpBleUtil.startRtTask()
        }
        binding.stopRtOxy.setOnClickListener {
            LpBleUtil.stopRtTask()
        }
        viewModel.dataOxySrc.observe(viewLifecycleOwner, {
            if (this::oxyView.isInitialized) {
                oxyView.setDataSrc(it)
                oxyView.invalidate()

            }
        })

    }

    private fun initEcgView() {
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(binding.ecgBkg.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        DataController.mm2px = mm2px

//        LepuBleLog.d("max index: $index", "mm2px: $mm2px")

        binding.ecgBkg.measure(0, 0)
        ecgBkg = EcgBkg(context)
        binding.ecgBkg.addView(ecgBkg)

        binding.ecgView.measure(0, 0)
        ecgView = EcgView(context)
        binding.ecgView.addView(ecgView)

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

}