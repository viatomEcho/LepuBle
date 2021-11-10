package com.lepu.demo.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.Bp2Response
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.CURRENT_MODEL
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.DataController
import com.lepu.demo.databinding.FragmentDashboardBinding
import com.lepu.demo.views.EcgBkg
import com.lepu.demo.views.EcgView
import com.lepu.demo.views.OxyView
import kotlin.math.floor

class DashboardFragment : Fragment(R.layout.fragment_dashboard){

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: DashboardViewModel by activityViewModels()

    private val binding: FragmentDashboardBinding by binding()

    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView


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
//            LepuBleLog.d("DataRec: ${DataController.dataRec.size}, delayed $interval")

            val temp = DataController.draw(5)
            viewModel.dataSrc.value = DataController.feed(viewModel.dataSrc.value, temp)
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




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = this@DashboardFragment
        }
        initView()
        initLiveEvent()



    }

    private fun initLiveEvent(){
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStart).observeSticky(this, {
            startWave()
        })

        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStop).observeSticky(this, {
            stopWave()
        })

        //------------------------------bp2------------------------------------

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
            .observe(this, Observer{ event ->
                val bp2Rt = event.data as Bp2Response.RtData

                bp2Rt.wave.dataEcging?.let {
                    LepuBleLog.d("bp2 ecg hr = ${it.hr}")
                    LepuBleLog.d("bp2 ecg waveLen = ${ bp2Rt.wave.waveLen}")

                    viewModel.hr.value = it.hr
                    DataController.receive(bp2Rt.wave.waveFs)
                }



            })
        //----------------------------bp2 end------------------------------------------

    }

    private fun initView() {
        binding.ecgBkg.post{
            initEcgView()
        }

        mainViewModel.bleState.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_ok)
                    binding.ecgView.visibility = View.VISIBLE

                } else {
                    binding.bleState.setImageResource(R.mipmap.bluetooth_error)
                    binding.ecgView.visibility = View.INVISIBLE
                }
            }

        })

        viewModel.hr.observe(viewLifecycleOwner, {
            if (it == 0) {
                binding.hr.text = "?"
            } else {
                binding.hr.text = it.toString()
            }
        })


        binding.btStartRt.setOnClickListener {

            LpBleUtil.startRtTask(CURRENT_MODEL, 500)
        }
        binding.btStopRt.setOnClickListener {
            LpBleUtil.stopRtTask(CURRENT_MODEL)
        }


        viewModel.dataSrc.observe(viewLifecycleOwner, {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
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


}