package com.lepu.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.lepu.demo.config.Constant.BluetoothConfig.Companion.ecnData

class DocumentActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        initView()
    }

    private fun initView() {
        pdfView = findViewById(R.id.document_pdf)
        pdfView.fromBytes(ecnData.data).load()
    }
}