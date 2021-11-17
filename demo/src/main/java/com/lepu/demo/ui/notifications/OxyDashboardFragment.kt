package com.lepu.demo.ui.notifications

import android.content.Context.VIBRATOR_SERVICE
import android.os.*
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.demo.data.OxyDataController
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DataController
import com.lepu.demo.databinding.FragmentOxyDashboardBinding
import com.lepu.demo.views.OxyView
import kotlin.math.floor
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth


class OxyDashboardFragment : Fragment(R.layout.fragment_oxy_dashboard) {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: OxyDashboardViewModel by activityViewModels()

    private val binding: FragmentOxyDashboardBinding by binding()


    private lateinit var oxyView: OxyView




    /**
     * rt ecg wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
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
//            LepuBleLog.d("DataRec: ${OxyDataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
            viewModel.dataSrc.value = OxyDataController.feed(viewModel.dataSrc.value, temp)
        }
    }

    private var runWave = false
    private fun startWave() {
        if (runWave) {
            return
        }
        runWave = true
        waveHandler.post(WaveTask())
    }

    private fun stopWave() {
        runWave = false
        DataController.clear()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListener()
        initLiveEvent()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLiveEvent() {

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).observeSticky(this, {
            when(it){
                Bluetooth.MODEL_O2RING -> startWave()
            }

        })

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).observeSticky(this, {
            when(it){
                Bluetooth.MODEL_O2RING -> stopWave()
            }
        })


        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).observeForever { event ->
            (event.data as OxyBleResponse.RtWave).let { rtWave ->
                activity?.let {
//                    mainViewModel.openCollectSwitchByO2ring(rtWave.wFs)

                }



                toPlayAlarm(rtWave.pr)

                viewModel.pr.value = rtWave.pr
                viewModel.spo2.value = rtWave.spo2
                LepuBleLog.d("o2ring pr = ${rtWave.pr}, spo2 = ${rtWave.spo2}")

                OxyDataController.receive(rtWave.wFs)
            }
        }


        // o2ring ppg
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
            .observe(this, {

                LpBleUtil.oxyGetPpgRt(it.model)

                it as InterfaceEvent
                val ppgData = it.data as OxyBleResponse.PPGData
                ppgData.let { data ->
                    Log.d("ppg", "len  = ${data.rawDataBytes.size}")
                }

            })



    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun toPlayAlarm(newPr: Int){
        mainViewModel.oxyPrAlarmFlag.value?.let { flag ->
            if (flag) {
                viewModel.pr.value?.let { pr ->
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

    private fun initView() {
        binding.oxiView.post {
            initOxyView()
        }

        mainViewModel.bleState.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.oxiView.visibility = View.VISIBLE
                } else {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.oxiView.visibility = View.INVISIBLE
                }
            }

        })

        viewModel.pr.observe(viewLifecycleOwner, {
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


        binding.getRtData.setOnClickListener {
            LpBleUtil.startRtTask()
        }
        binding.stopRtData.setOnClickListener {
            LpBleUtil.stopRtTask()
        }


        viewModel.dataSrc.observe(viewLifecycleOwner, {
            if (this::oxyView.isInitialized) {
                oxyView.setDataSrc(it)
                oxyView.invalidate()

            }
        })


        mainViewModel.oxyInfo.observe(viewLifecycleOwner, {
            it?.let {
                binding.other.text = "lowHr = ${it.hrLowThr}, highHr = ${it.hrHighThr}"
            }
        })
    }

    private fun initOxyView() {
        // cal screen
        val dm = resources.displayMetrics
        val index = floor(binding.oxiView.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        OxyDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        OxyDataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        binding.oxiView.measure(0, 0)
        oxyView = OxyView(context)
        binding.oxiView.addView(oxyView)

        viewModel.dataSrc.value = OxyDataController.iniDataSrc(index)
    }

    fun initClickListener(){
        binding.startCollect.setOnClickListener {
            context?.let { it1 -> mainViewModel.startPreCollect(it1) }
        }
        binding.stopCollect.setOnClickListener {
            context?.let { it1 -> mainViewModel.breakCollecting(it1) }
        }
    }
}
