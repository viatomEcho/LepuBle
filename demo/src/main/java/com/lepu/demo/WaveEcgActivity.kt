package com.lepu.demo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.lepu.demo.util.DataConvert
import com.lepu.demo.views.WaveEcgView

class WaveEcgActivity : AppCompatActivity() {

    var filterEcgView: WaveEcgView? = null
    var currentZoomLevel = 1
    val handler = Handler()
    var mAlertDialog: AlertDialog? = null

    var waveData: ByteArray? = null
    var filterWaveData: ShortArray? = null
    var mills = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave_ecg)

        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()
        mAlertDialog?.show()

        waveData = intent.getByteArrayExtra("waveData")
        mills = intent.getLongExtra("recordingTime", 0)
        handler.postDelayed({
            ecgWave()
        }, 1000)
    }

    private fun getShortArray(data: ByteArray): ShortArray {
        val convert = DataConvert()
        var invalid = 0
        var len = 0
        val shortData = ShortArray(data.size)
        for (i in data.indices) {
            val temp = convert.unCompressAlgECG(data[i])
            if (temp != (-32768).toShort()) {
                shortData[len] = temp
                len++
            }
        }
        for (j in len-1 downTo 0) {
            if (shortData[j] == (32767).toShort()) {
                invalid++
            } else {
                break
            }
        }
        return DataConvert.shortfilter(shortData)
    }

    private fun ecgWave() {
        filterWaveData = getShortArray(waveData!!)
        mAlertDialog?.dismiss()
        if (filterEcgView != null) {
            currentZoomLevel = filterEcgView!!.getCurrentZoomPosition()
        }

        val layout: RelativeLayout = findViewById(R.id.rl_ecg_container)
        val width = layout.width
        val height = layout.height
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

}