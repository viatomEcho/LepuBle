package com.lepu.demo

import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.cmd.CheckmeLeBleResponse
import com.lepu.blepro.ble.cmd.PulsebitBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.views.WaveEcgView

class WaveEcgActivity : AppCompatActivity() {

    var filterEcgView: WaveEcgView? = null
    var currentZoomLevel = 1
    val handler = Handler()

    var filterWaveData: ShortArray? = null
    var mills = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave_ecg)

        initLiveEvent()
        BleServiceHelper.BleServiceHelper.readFile("", "20220414183025", Bluetooth.MODEL_PULSEBITEX, 0)
    }

    fun ecgWave() {
        if (filterEcgView != null) {
            currentZoomLevel = filterEcgView!!.getCurrentZoomPosition()
        }

        val layout: RelativeLayout = findViewById(R.id.rl_ecg_container)
        val width = layout.getWidth()
        val height = layout.getHeight()
        if (filterEcgView == null && filterWaveData != null) {
            filterEcgView = WaveEcgView(this, mills*1000, filterWaveData, filterWaveData!!.size, width*1f, height*1f, currentZoomLevel, false, 0)
        }
        if(filterEcgView != null) {
            layout.removeAllViews()
            layout.addView(filterEcgView)
        }
        val layoutParams = layout.layoutParams
        val lineHeight = width * 2 * 4 / (7 * 5) + 20
        layoutParams.height = lineHeight * 9 + 10
        layout.layoutParams = layoutParams
    }

    fun initLiveEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete)
            .observe(this, {
                val data = it.data as PulsebitBleResponse.EcgFile
                filterWaveData = data.waveShortData
                mills = DateUtil.getSecondTimestamp(data.fileName)
                handler.postDelayed({
                    ecgWave()
                }, 1000)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeReadFileComplete)
            .observe(this, {
                val data = it.data as CheckmeLeBleResponse.EcgFile
                filterWaveData = data.waveShortData
                mills = DateUtil.getSecondTimestamp(data.fileName)
                handler.postDelayed({
                    ecgWave()
                }, 1000)
            })
    }

}