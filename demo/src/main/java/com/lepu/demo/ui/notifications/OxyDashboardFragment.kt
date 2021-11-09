package com.lepu.demo.ui.notifications

import android.content.Context.VIBRATOR_SERVICE
import android.os.*
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.demo.data.OxyDataController
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.CURRENT_MODEL
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DataController
import com.lepu.demo.databinding.FragmentOxyDashboardBinding
import com.lepu.demo.views.OxyView
import kotlin.math.floor
import androidx.annotation.RequiresApi
import com.lepu.blepro.event.EventMsgConst


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
        initLiveEvent()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLiveEvent() {

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).observeSticky(this, {
            startWave()
        })

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).observeSticky(this, {
            stopWave()
        })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).observeForever { event ->
            (event.data as OxyBleResponse.RtWave).let { rtWave ->

                viewModel.pr.value?.let { pr ->
                    if (pr != 0 && rtWave.pr == 0) {
                        alarm()
                    }

                    if (pr != 0 && rtWave.pr != 0) {
                        mainViewModel.oxyInfo.value?.let {
                            if (rtWave.pr >= it.hrHighThr || rtWave.pr <= it.hrLowThr)
                                alarm()
                        }
                    }


                }
                viewModel.pr.value = rtWave.pr
                viewModel.spo2.value = rtWave.spo2
                LepuBleLog.d("o2ring pr = ${rtWave.pr}, spo2 = ${rtWave.spo2}")

                OxyDataController.receive(rtWave.wFs)


            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun alarm(){
        LepuBleLog.d("alarm...")
        context?.let {
            val vibrator = it.getSystemService(VIBRATOR_SERVICE) as Vibrator

            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 2000L, 200L,  3000L), -1))

        }
    }

    private fun initView() {
        binding.oxiView.post {
            initOxyView()
//            mainViewModel.bleState.value?.let {
//                if (it) startWave()
//            }
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

            LpBleUtil.startRtTask(CURRENT_MODEL)
        }
        binding.stopRtData.setOnClickListener {
            LpBleUtil.stopRtTask(CURRENT_MODEL)
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
}
