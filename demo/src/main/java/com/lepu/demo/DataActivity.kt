package com.lepu.demo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.ventilator.StatisticsFile
import com.lepu.blepro.ble.data.ventilator.SystemSetting
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.config.Constant
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecnData
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.oxyData
import com.lepu.demo.util.DataConvert
import java.util.*

class DataActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var textView: TextView

    val handler = Handler()
    var mAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(getString(R.string.handling))
            .create()
        mAlertDialog?.show()
        initView()
        initEvent()
        handler.postDelayed({
            mAlertDialog?.dismiss()
        }, 1000)
    }

    private fun initView() {
        pdfView = findViewById(R.id.document_pdf)
        textView = findViewById(R.id.info_text)
        when (Constant.BluetoothConfig.currentModel[0]) {
            Bluetooth.MODEL_ECN -> {
                textView.visibility = View.GONE
                pdfView.fromBytes(ecnData.data).load()
            }
            Bluetooth.MODEL_R20, Bluetooth.MODEL_R21,
            Bluetooth.MODEL_R10, Bluetooth.MODEL_R11,
            Bluetooth.MODEL_LERES -> {
                pdfView.visibility = View.GONE
                LpBleUtil.ventilatorGetSystemSetting(Constant.BluetoothConfig.currentModel[0])
            }
            Bluetooth.MODEL_PF_10AW_1, Bluetooth.MODEL_PF_10BWS -> {
                pdfView.visibility = View.GONE
                handler.post {
                    textView.text = "文件名：${oxyData.fileName}\n" +
                            "开始测量：${DateUtil.stringFromDate(Date(oxyData.startTime.times(1000)), "yyyy-MM-dd HH:mm:ss")}\n" +
                            "结束测量：${DateUtil.stringFromDate(Date((oxyData.startTime+oxyData.recordingTime).times(1000)), "yyyy-MM-dd HH:mm:ss")}\n" +
                            "记录时长：${DataConvert.getEcgTimeStr(oxyData.recordingTime)}\n" +
                            "血氧：${oxyData.spo2s.joinToString(",")}\n" +
                            "脉率：${oxyData.hrs.joinToString(",")}"
                }
            }
        }
    }

    private fun initEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting)
            .observe(this) {
                val data = it.data as SystemSetting
                val unit = data.unitSetting.pressureUnit
                val file = StatisticsFile(ecnData.fileName, ecnData.data)
                textView.text = "文件名：${file.fileName}\n" +
                        "使用设备天数：${file.usageDays}天\n" +
                        "不小于4小时天数：${file.moreThan4hDays}天\n" +
                        "总使用时间：${file.duration}秒\n" +
                        "平均每天使用时间：${file.meanSecond}秒\n" +
                        "大漏气量时间：${file.llTime}秒\n" +
                        "自主呼吸占比：${file.spont}%\n" +
                        "AHI：${file.ahiCount}次\n" +
                        "AI：${file.aiCount}次\n" +
                        "HI：${file.hiCount}次\n" +
                        "CAI：${file.caiCount}次\n" +
                        "OAI：${file.oaiCount}次\n" +
                        "RERA：${file.rearCount}次\n" +
                        "PB：${file.pbCount}次\n" +
                        "SNI：${file.sniCount}次\n" +
                        "摘下次数：${file.takeOffCount}次\n" +
                        "吸气压力：${file.ipap.joinToString(",")}\n" +
                        "呼气压力：${file.epap.joinToString(",")}\n" +
                        "压力：${file.pressure.joinToString(",")}\n" +
                        "潮气量：${file.vt.joinToString(",")}\n" +
                        "漏气量：${file.leak.joinToString(",")}\n" +
                        "分钟通气量：${file.mv.joinToString(",")}\n" +
                        "呼吸频率：${file.rr.joinToString(",")}\n" +
                        "吸气时间：${file.ti.joinToString(",")}\n" +
                        "呼吸比：${file.ie.joinToString(",")}\n" +
                        "血氧：${file.spo2.joinToString(",")}\n" +
                        "脉率：${file.pr.joinToString(",")}\n" +
                        "心率：${file.hr.joinToString(",")}"
                val time = file.duration.div(3600f)
                textView.text = "文件名：${file.fileName}\n" +
                        "使用天数：${file.usageDays}天\n" +
                        "不小于4小时天数：${file.moreThan4hDays}天\n" +
                        "总使用时间：${String.format("%.1f", time)}小时\n" +
                        "平均使用时间：${String.format("%.1f", file.meanSecond.div(3600f))}小时\n" +
                        "压力：${file.pressure[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "呼气压力：${file.epap[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "吸气压力：${file.ipap[4]}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "AHI：${String.format("%.1f", file.ahiCount.times(3600f).div(file.duration))}/小时\n" +
                        "AI：${String.format("%.1f", file.aiCount.times(3600f).div(file.duration))}/小时\n" +
                        "HI：${String.format("%.1f", file.hiCount.times(3600f).div(file.duration))}/小时\n" +
                        "CAI：${String.format("%.1f", file.caiCount.times(3600f).div(file.duration))}/小时\n" +
                        "OAI：${String.format("%.1f", file.oaiCount.times(3600f).div(file.duration))}/小时\n" +
                        "RERA：${String.format("%.1f", file.rearCount.times(3600f).div(file.duration))}/小时\n" +
                        "潮气量：${if (file.vt[3] < 0 || file.vt[3] > 3000) "**" else file.vt[3]}mL\n" +
                        "漏气量：${if (file.leak[4] < 0 || file.leak[4] > 120) "**" else file.leak[4]}L/min\n" +
                        "分钟通气量：${if (file.mv[3] < 0 || file.mv[3] > 60) "**" else file.mv[3]}L/min\n" +
                        "呼吸频率：${if (file.rr[3] < 0 || file.rr[3] > 60) "**" else file.rr[3]}bpm\n" +
                        "吸气时间：${if (file.ti[3] < 0 || file.ti[3] > 4) "--" else file.ti[3]}s\n" +
                        "吸呼比：${if (file.ie[3] < 0.02 || file.ie[3] > 3) "--" else {
                            if (file.ie[3] < 1) {
                                "1:" + String.format("%.1f", 1f/file.ie[3])
                            } else {
                                String.format("%.1f", file.ie[3].div(1f)) + ":1"
                            }
                        }}\n" +
                        "自主呼吸占比：${if (file.spont < 0 || file.spont > 100) "**" else file.spont}%\n" +
                        "血氧：${if (file.spo2[0] < 70 || file.spo2[0] > 100) "**" else file.spo2[0]}%\n" +
                        "脉率：${if (file.pr[2] < 30 || file.pr[2] > 250) "**" else file.pr[2]}bpm\n" +
                        "心率：${if (file.hr[2] < 30 || file.hr[2] > 250) "**" else file.hr[2]}bpm"
            }
    }
}