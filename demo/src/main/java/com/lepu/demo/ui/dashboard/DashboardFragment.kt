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
import com.lepu.blepro.ble.data.lew.BatteryInfo
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.data.DataController
import com.lepu.demo.data.OxyDataController
import com.lepu.demo.databinding.FragmentDashboardBinding
import com.lepu.demo.util.DataConvert
import com.lepu.demo.views.EcgBkg
import com.lepu.demo.views.EcgView
import com.lepu.demo.views.OxyView
import java.util.*
import kotlin.math.floor

class DashboardFragment : Fragment(R.layout.fragment_dashboard){
    val TAG: String = "DashboardFragment"

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: DashboardViewModel by activityViewModels()

    private val binding: FragmentDashboardBinding by binding()

    var dataString = ""

    private var state = false
    private var type = 0

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
                    101  // 血氧波形50HZ，1s有50个点，速度大于100即可
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
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK,
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_BP2,
            Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LEW,
            Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_LE_BP2W,
            Bluetooth.MODEL_PC80B, Bluetooth.MODEL_LES1,
            Bluetooth.MODEL_W12C, Bluetooth.MODEL_HHM1,
            Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3,
            Bluetooth.MODEL_LP_ER2 -> waveHandler.post(EcgWaveTask())

            Bluetooth.MODEL_O2RING, Bluetooth.MODEL_PC60FW,
            Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
            Bluetooth.MODEL_PC100, Bluetooth.MODEL_PC66B,
            Bluetooth.MODEL_AP20, Bluetooth.MODEL_BABYO2,
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
            Bluetooth.MODEL_S6W1 -> waveHandler.post(OxyWaveTask())

            Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_PC300,
            Bluetooth.MODEL_CHECK_ADV -> {
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
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).observeSticky(this) {
            startWave(it)
        }

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).observeSticky(this) {
            stopWave()
        }

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
                    mainViewModel._battery.value = "${data.param.battery} %"
                    binding.dataStr.text = data.param.toString()
                    viewModel.ecgHr.value = data.param.hr
                    binding.measureDuration.text = " ${data.param.recordTime} s"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList).observe(this) { event ->
            (event.data as String).let {
                binding.dataStr.text = it
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
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).observe(this) { event ->
            (event.data as Er2FileList).let {
                binding.dataStr.text = it.toString()
            }
        }
        //------------------------------lew------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewBatteryInfo)
            .observe(this) {
                val data = it.data as BatteryInfo
                dataString += "\n" + data.toString()
                binding.dataStr.text = dataString
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewRtData)
            .observe(this) {
                val rtData = it.data as LewBleResponse.RtData
                rtData.let { data ->
                    Log.d("lew data ", "len = ${data.wave.samplingNum}")
                    DataController.receive(data.wave.wFs)
                    dataString = data.param.toString()
                    binding.dataStr.text = dataString
                    viewModel.ecgHr.value = data.param.hr

                    LpBleUtil.lewGetBattery(it.model)

                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList).observe(this) { event ->
            (event.data as LewBleResponse.FileList).let {
                binding.dataStr.text = it.toString()
            }
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
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousDataEnd)
            .observe(this) {
                stopWave()
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
                        LepuBleLog.d("bp2 ecg waveformSize = ${bp2Rt.rtWave.waveformSize}")

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
                    }
                    else -> data = ""
                }

                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: ${bp2Rt.rtWave.waveDataType} $data ----rtState-- ${bp2Rt.rtState}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList).observe(this) { event ->
            (event.data as KtBleFileList).let {
                binding.dataStr.text = it.toString()
            }
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
                    }
                    1 -> {
                        data1 = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data1.pressure
                        viewModel.sys.value = data1.sys
                        viewModel.dia.value = data1.dia
                        viewModel.mean.value = data1.mean
                        viewModel.bpPr.value = data1.pr
                    }
                    2 -> {
                        data1 = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        LepuBleLog.d("bp2 ecg hr = ${data1.hr}")
                        viewModel.ecgHr.value = data1.hr
                    }
                    3 -> {
                        data1 = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data1.hr
                    }
                    else -> data1 = ""
                }

                LepuBleLog.d("bp2 ecg waveformSize = ${bp2Rt.rtWave.waveformSize}")

                val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                DataController.receive(mvs)
                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: " + bp2Rt.rtWave.waveDataType + " " + data1.toString() + "----rtState--" + bp2Rt.rtState.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList).observe(this) { event ->
            (event.data as KtBleFileList).let {
                binding.dataStr.text = it.toString()
            }
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
                    }
                    1 -> {
                        data2 = Bp2DataBpResult(bp2Rt.rtWave.waveData)
                        viewModel.ps.value = data2.pressure
                        viewModel.sys.value = data2.sys
                        viewModel.dia.value = data2.dia
                        viewModel.mean.value = data2.mean
                        viewModel.bpPr.value = data2.pr
                    }
                    2 -> {
                        data2 = Bp2DataEcgIng(bp2Rt.rtWave.waveData)
                        LepuBleLog.d("bp2 ecg hr = ${data2.hr}")
                        viewModel.ecgHr.value = data2.hr
                    }
                    3 -> {
                        data2 = Bp2DataEcgResult(bp2Rt.rtWave.waveData)
                        viewModel.ecgHr.value = data2.hr
                    }
                    else -> data2 = ""
                }

                LepuBleLog.d("bp2 ecg waveformSize = ${bp2Rt.rtWave.waveformSize}")

                val mvs = ByteUtils.bytes2mvs(bp2Rt.rtWave.waveform)
                DataController.receive(mvs)
                mainViewModel._battery.value = "${bp2Rt.rtState.battery.percent} %"
                binding.dataStr.text = "dataType: " + bp2Rt.rtWave.waveDataType + " " + data2.toString() + "----rtState--" + bp2Rt.rtState.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList).observe(this) { event ->
            (event.data as Bp2BleFile).let {
                when (it.type) {
                    LeBp2wBleCmd.FileType.ECG_TYPE -> {
                        val data = LeBp2wEcgList(it.content)
                        binding.dataStr.text = data.toString()
                    }
                    LeBp2wBleCmd.FileType.BP_TYPE -> {
                        val data = LeBp2wBpList(it.content)
                        binding.dataStr.text = data.toString()
                    }
                    LeBp2wBleCmd.FileType.USER_TYPE -> {
                        val data = LeBp2wUserList(it.content)
                        binding.dataStr.text = data.toString()
                    }
                    else -> {
                        binding.dataStr.text = it.toString()
                    }
                }
            }
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
                binding.dataStr.text = "$rtData"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult)
            .observe(this) {
                val rtData = it.data as BpmBleResponse.ErrorResult
                binding.dataStr.text = "$rtData"
            }

        //------------------------------o2ring------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).observeForever { event ->
            (event.data as OxyBleResponse.RtWave).let { data ->
                activity?.let {
                    //接收数据 开始添加采集数据
                    mainViewModel.checkStartCollect(it, data.waveByte)

                    toPlayAlarm(data.pr)
                    OxyDataController.receive(data.wFs)
                    LpBleUtil.oxyGetRtParam(event.model)
                    viewModel.oxyPr.value = data.pr
                    viewModel.spo2.value = data.spo2
                    viewModel.pi.value = data.pi.div(10f)
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
                LpBleUtil.oxyGetPpgRt(it.model)
                binding.dataStr.text = data.toString()
            }
        // o2ring ppg
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this) {

                LpBleUtil.oxyGetRtWave(it.model)
                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.let { data ->
                    oxyPpgSize += data.rawDataBytes.size
                    Log.d("ppg", "len  = ${data.rawDataBytes.size}")
                    Log.d(TAG, "oxyPpgSize == $oxyPpgSize")

                    var bytes = ByteArray(0)
                    for (i in 0 until data.len) {
                        bytes = bytes.plus(data.redByteArray[i]!!)
                    }

                    Log.d(TAG, "------------------------" + bytesToHex(bytes))

                }

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyPpgRes------------")
                LpBleUtil.oxyGetRtWave(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtWaveRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyRtWaveRes------------")
                LpBleUtil.oxyGetRtParam(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamRes)
            .observe(this) {
                Log.d(TAG, "------------EventOxyRtParamRes------------")
                LpBleUtil.oxyGetPpgRt(it.model)
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
            }

        //------------------------------pc100------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtWave)
            .observe(this) {
                val rtWave = it.data as ByteArray
                binding.dataStr.text = bytesToHex(rtWave)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtParam)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.RtBoParam
                viewModel.oxyPr.value = rtData.pr
                viewModel.spo2.value = rtData.spo2
                viewModel.pi.value = rtData.pi.div(10f)
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
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this) {
                val rtData = it.data as Pc100BleResponse.BpResultError
                binding.dataStr.text = "$rtData"
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathWave)
            .observe(this) {
//                val rtWave = it.data as Ap20BleResponse.RtBreathWave
//                dataString += "\n rtWave : $rtWave"
//                binding.dataStr.text = dataString
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20RtBreathParam)
            .observe(this) {
//                val rtData = it.data as Ap20BleResponse.RtBreathParam
//                dataString += "\n rtData : $rtData"
//                binding.dataStr.text = dataString
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20TempData)
            .observe(this) {
                val data = it.data as Sp20BleResponse.TempData
                if (data.result == 1) {
                    binding.tempStr.text = "体温过低"
                } else if (data.result == 2) {
                    binding.tempStr.text = "体温过高"
                } else {
                    if (data.unit == 1) {
                        binding.tempStr.text = "体温 ：" + data.value + "℉"
                    } else {
                        binding.tempStr.text = "体温 ：" + data.value + "℃"
                    }
                }
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
            }
        //--------------------------vtm20f----------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtWave)
            .observe(this) {
                val rtWave = it.data as Vtm20fBleResponse.RtWave
                OxyDataController.receive(intArrayOf(rtWave.wave))
                binding.dataStr.text = rtWave.toString()
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
                binding.tempStr.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observe(this) {
                val data = it.data as Aoj20aBleResponse.ErrorMsg
                binding.tempStr.text = data.toString()
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave)
            .observe(this) {
                val data = it.data as Pc300BleResponse.RtEcgWave
                DataController.receive(data.wFs)
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
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.BpResultError
                binding.dataStr.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300GluResult)
            .observe(this) {
                val data = it.data as Pc300BleResponse.GluResult
                binding.dataStr.text = "$data"
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
            }
    }

    var oxyPpgSize = 0

    private fun initView() {

        mainViewModel.curBluetooth.observe(viewLifecycleOwner) {
            when (it!!.modelNo) {
                Bluetooth.MODEL_ER1, Bluetooth.MODEL_DUOEK,
                Bluetooth.MODEL_ER2, Bluetooth.MODEL_PC80B,
                Bluetooth.MODEL_LEW, Bluetooth.MODEL_ER1_N,
                Bluetooth.MODEL_LES1, Bluetooth.MODEL_W12C,
                Bluetooth.MODEL_HHM1, Bluetooth.MODEL_HHM2,
                Bluetooth.MODEL_HHM3, Bluetooth.MODEL_LP_ER2 -> {
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.bpLayout.visibility = View.GONE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LE_BP2W -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_BPM, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                    binding.oxyLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_O2RING, Bluetooth.MODEL_PC60FW,
                Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                Bluetooth.MODEL_PC66B, Bluetooth.MODEL_AP20,
                Bluetooth.MODEL_BABYO2, Bluetooth.MODEL_SP20,
                Bluetooth.MODEL_BBSM_S1, Bluetooth.MODEL_BBSM_S2,
                Bluetooth.MODEL_TV221U, Bluetooth.MODEL_BABYO2N,
                Bluetooth.MODEL_CHECKO2, Bluetooth.MODEL_O2M,
                Bluetooth.MODEL_SLEEPO2, Bluetooth.MODEL_SNOREO2,
                Bluetooth.MODEL_WEARO2, Bluetooth.MODEL_SLEEPU,
                Bluetooth.MODEL_OXYLINK, Bluetooth.MODEL_KIDSO2,
                Bluetooth.MODEL_OXYSMART, Bluetooth.MODEL_OXYFIT,
                Bluetooth.MODEL_POD_1W, Bluetooth.MODEL_CHECK_POD,
                Bluetooth.MODEL_PC_68B, Bluetooth.MODEL_POD2B,
                Bluetooth.MODEL_PC_60NW_1, Bluetooth.MODEL_PC_60B,
                Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_OXYRING,
                Bluetooth.MODEL_CMRING, Bluetooth.MODEL_OXYU,
                Bluetooth.MODEL_S5W, Bluetooth.MODEL_AI_S100,
                Bluetooth.MODEL_S6W, Bluetooth.MODEL_S7W,
                Bluetooth.MODEL_S7BW, Bluetooth.MODEL_S6W1 -> {
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                    binding.bpLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_PC100 -> {
                    binding.bpLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.ecgLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_VETCORDER, Bluetooth.MODEL_CHECK_ADV -> {
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.bpLayout.visibility = View.GONE
                }
                Bluetooth.MODEL_PC300 -> {
                    binding.ecgLayout.visibility = View.VISIBLE
                    binding.oxyLayout.visibility = View.VISIBLE
                    binding.bpLayout.visibility = View.VISIBLE
                }
            }
        }
        mainViewModel.bleState.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.dashLayout.visibility = View.VISIBLE
                } else {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.bpBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.dashLayout.visibility = View.INVISIBLE
                }
            }

        }
        mainViewModel.battery.observe(viewLifecycleOwner) {
            binding.bleBattery.text = "电量：$it"
            binding.bpBleBattery.text = "电量：$it"
            binding.oxyBleBattery.text = "电量：$it"
        }

        //------------------------------ecg------------------------------
        binding.ecgBkg.post{
            initEcgView()
        }
        viewModel.ecgHr.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.hr.text = "?"
            } else {
                binding.hr.text = it.toString()
            }
        }
        binding.startRtEcg.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300) {
                LpBleUtil.startEcg(Constant.BluetoothConfig.currentModel[0])
            }
            LpBleUtil.startRtTask()
        }
        binding.stopRtEcg.setOnClickListener {
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_PC300) {
                LpBleUtil.stopEcg(Constant.BluetoothConfig.currentModel[0])
            }
            // 停止实时任务后去获取设备列表
            LpBleUtil.stopRtTask {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
            }
        }
        viewModel.dataEcgSrc.observe(viewLifecycleOwner) {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
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
                LpBleUtil.oxyGetRtParam(Constant.BluetoothConfig.currentModel[0])
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
                    Bluetooth.MODEL_AP20 -> {
                        LpBleUtil.enableRtData(it, type, state)
                        type++
                        if (type > Ap20BleCmd.EnableType.BREATH_WAVE) {
                            type = Ap20BleCmd.EnableType.OXY_PARAM
                            state = !state
                        }
                    }
                    Bluetooth.MODEL_SP20, Bluetooth.MODEL_PC60FW,
                    Bluetooth.MODEL_PF_10, Bluetooth.MODEL_PF_20,
                    Bluetooth.MODEL_PC66B, Bluetooth.MODEL_POD_1W,
                    Bluetooth.MODEL_PC_68B, Bluetooth.MODEL_POD2B,
                    Bluetooth.MODEL_PC_60NW_1, Bluetooth.MODEL_PC_60B,
                    Bluetooth.MODEL_PC_60NW, Bluetooth.MODEL_S5W,
                    Bluetooth.MODEL_S6W, Bluetooth.MODEL_S7W,
                    Bluetooth.MODEL_S7BW, Bluetooth.MODEL_S6W1 -> {
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

    override fun onDestroy() {
        LpBleUtil.stopRtTask()
        super.onDestroy()
    }

}