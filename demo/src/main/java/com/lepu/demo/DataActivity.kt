package com.lepu.demo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.lepu.blepro.ble.data.r20.StatisticsFile
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.config.Constant
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecnData
import com.lepu.demo.util.DataConvert

class DataActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        initView()
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
                val data = StatisticsFile(ecnData.fileName, ecnData.data)
                textView.text = "文件名：${data.fileName}\n" +
                        "使用设备天数：${data.usageDays}天\n" +
                        "使用天数：1天\n" +
                        "不小于4小时天数：${data.moreThan4hDays}天\n" +
                        "总使用时间：${DataConvert.getEcgTimeStr(data.duration)}\n" +
                        "平均每天使用时间：${DataConvert.getEcgTimeStr(data.meanSecond)}\n" +
                        "大漏气量时间：${DataConvert.getEcgTimeStr(data.llTime)}\n" +
                        "自主呼吸占比：${data.spont}%\n" +
                        "AHI：${data.ahiCount}次\n" +
                        "AI：${data.aiCount}次\n" +
                        "HI：${data.hiCount}次\n" +
                        "CAI：${data.caiCount}次\n" +
                        "OAI：${data.oaiCount}次\n" +
                        "RERA：${data.rearCount}次\n" +
                        "PB：${data.pbCount}次\n" +
                        "SNI：${data.sniCount}次\n" +
                        "摘下次数：${data.takeOffCount}次\n" +
                        "吸气压力：${data.ipap.joinToString(",")}\n" +
                        "呼气压力：${data.epap.joinToString(",")}\n" +
                        "压力：${data.pressure.joinToString(",")}\n" +
                        "潮气量：${data.vt.joinToString(",")}\n" +
                        "漏气量：${data.leak.joinToString(",")}\n" +
                        "分钟通气量：${data.mv.joinToString(",")}\n" +
                        "呼吸频率：${data.rr.joinToString(",")}\n" +
                        "吸气时间：${data.ti.joinToString(",")}\n" +
                        "呼吸比：${data.ie.joinToString(",")}\n" +
                        "血氧：${data.spo2.joinToString(",")}\n" +
                        "脉率：${data.pr.joinToString(",")}\n" +
                        "心率：${data.hr.joinToString(",")}"
            }
        }
    }
}