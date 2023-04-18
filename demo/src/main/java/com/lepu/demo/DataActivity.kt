package com.lepu.demo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.r20.StatisticsFile
import com.lepu.blepro.ble.data.r20.SystemSetting
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.config.Constant
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecnData
import com.lepu.demo.util.DataConvert

class DataActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        initView()
        initEvent()
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
                LpBleUtil.r20GetSystemSetting(Constant.BluetoothConfig.currentModel[0])
            }
        }
    }

    private fun initEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetSystemSetting)
            .observe(this) {
                val data = it.data as SystemSetting
                val unit = data.unitSetting.pressureUnit
                val file = StatisticsFile(ecnData.fileName, ecnData.data)
                textView.text = "文件名：${file.fileName}\n" +
                        "使用设备天数：${file.usageDays}天\n" +
                        "使用天数：1天\n" +
                        "不小于4小时天数：${file.moreThan4hDays}天\n" +
                        "总使用时间：${DataConvert.getEcgTimeStr(file.duration)}\n" +
                        "平均每天使用时间：${DataConvert.getEcgTimeStr(file.meanSecond)}\n" +
                        "大漏气量时间：${DataConvert.getEcgTimeStr(file.llTime)}\n" +
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
                        "使用天数：1天\n" +
                        "不小于4小时天数：${if (time > 4) 1 else 0}天\n" +
                        "总使用时间：${String.format("%.1f", time)}小时\n" +
                        "平均使用时间：${String.format("%.1f", time)}小时\n" +
                        "压力：${file.pressure[4].div(10f)}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "呼气压力：${file.epap[4].div(10f)}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "吸气压力：${file.ipap[4].div(10f)}${if (unit == 0) "cmH2O" else "hPa"}\n" +
                        "AHI：${String.format("%.1f", file.ahiCount.times(3600f).div(file.duration))}/小时\n" +
                        "AI：${String.format("%.1f", file.aiCount.times(3600f).div(file.duration))}/小时\n" +
                        "HI：${String.format("%.1f", file.hiCount.times(3600f).div(file.duration))}/小时\n" +
                        "CAI：${String.format("%.1f", file.caiCount.times(3600f).div(file.duration))}/小时\n" +
                        "OAI：${String.format("%.1f", file.oaiCount.times(3600f).div(file.duration))}/小时\n" +
                        "RERA：${String.format("%.1f", file.rearCount.times(3600f).div(file.duration))}/小时\n" +
                        "潮气量：${if (file.vt[3] < 0 || file.vt[3] > 3000) "**" else file.vt[3]}mL\n" +
                        "漏气量：${if (file.leak[4] < 0 || file.leak[4] > 1200) "**" else file.leak[4].div(10f)}L/min\n" +
                        "分钟通气量：${if (file.mv[3] < 0 || file.mv[3] > 600) "**" else file.mv[3].div(10f)}L/min\n" +
                        "呼吸频率：${if (file.rr[3] < 0 || file.rr[3] > 60) "**" else file.rr[3]}bpm\n" +
                        "吸气时间：${if (file.ti[3] < 0 || file.ti[3] > 40) "--" else file.ti[3].div(10f)}s\n" +
                        "吸呼比：${if (file.ie[3] < 200 || file.ie[3] > 30000) "--" else {
                            "1:" + String.format("%.1f", 1.div(1.div(file.ie[3].div(10000f))))
                        }}\n" +
                        "自主呼吸占比：${if (file.spont < 0 || file.spont > 100) "**" else file.spont}%\n" +
                        "血氧：${if (file.spo2[0] < 70 || file.spo2[0] > 100) "**" else file.spo2[0]}%\n" +
                        "脉率：${if (file.pr[2] < 30 || file.pr[2] > 250) "**" else file.pr[2]}bpm\n" +
                        "心率：${if (file.hr[2] < 30 || file.hr[2] > 250) "**" else file.hr[2]}bpm"
            }
    }
}