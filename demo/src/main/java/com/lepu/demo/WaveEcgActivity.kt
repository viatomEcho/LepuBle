package com.lepu.demo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.ByteUtils
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.ecgData
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
    var model = Bluetooth.MODEL_ER1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave_ecg)

        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()
        mAlertDialog?.show()

//        val bundle = intent.getBundleExtra("bundle")
//        if (bundle != null) {
//            model = bundle.getInt("model", Bluetooth.MODEL_ER1)
//            waveData = bundle.getByteArray("waveData")
//            mills = bundle.getLong("recordingTime", 0)
//        }
        model = intent.getIntExtra("model", Bluetooth.MODEL_ER1)
//        waveData = intent.getByteArrayExtra("waveData")
//        mills = intent.getLongExtra("recordingTime", 0)

        waveData = ecgData.data
        filterWaveData = ecgData.shortData

        mills = ecgData.recordingTime

        handler.postDelayed({
            ecgWave()
        }, 1000)
    }

    private fun getEr1ShortArray(data: ByteArray): ShortArray {
        val convert = DataConvert()
        var invalid = 0
        var len = 0
        val tempData = ShortArray(data.size)
        for (i in data.indices) {
            val temp = convert.unCompressAlgECG(data[i])
            if (temp != (-32768).toShort()) {
                tempData[len] = temp
                len++
            }
        }
        for (j in len-1 downTo 0) {
            if (tempData[j] == (32767).toShort()) {
                invalid++
            } else {
                break
            }
        }
        val shortData = ShortArray(len-invalid)
        System.arraycopy(tempData, 0, shortData, 0, shortData.size)
        return DataConvert.shortfilter(shortData)
    }

    private fun getBp2ShortArray(data: ByteArray): ShortArray {
        val shortData = ShortArray(data.size.div(2))
        for (i in shortData.indices) {
            shortData[i] = ByteUtils.toSignedShort(data[i * 2], data[i * 2 + 1])
        }
        return DataConvert.shortfilter(shortData)
    }

    private fun ecgWave() {
        /*filterWaveData = when (model) {
            Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1_N, Bluetooth.MODEL_HHM1,
            Bluetooth.MODEL_DUOEK, Bluetooth.MODEL_HHM2, Bluetooth.MODEL_HHM3,
            Bluetooth.MODEL_ER2, Bluetooth.MODEL_LP_ER2 -> {
                getEr1ShortArray(waveData!!)
            }
            Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2W, Bluetooth.MODEL_LE_BP2W -> {
                getBp2ShortArray(waveData!!)
            }
            else -> {
                getEr1ShortArray(waveData!!)
            }
        }*/

        mAlertDialog?.dismiss()
        if (filterEcgView != null) {
            currentZoomLevel = filterEcgView!!.currentZoomPosition
        }

        val layout: RelativeLayout = findViewById(R.id.rl_ecg_container)
        val width = layout.width
        val height = layout.height
        if (filterEcgView == null && filterWaveData != null) {
            filterEcgView = WaveEcgView(this, mills*1000, filterWaveData, filterWaveData!!.size, width*1f, height*1f, currentZoomLevel, false, model)
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