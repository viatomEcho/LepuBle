package com.lepu.demo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanCodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    lateinit var mScannerView : ZXingScannerView
    lateinit var mCodeContent : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_code)
        mScannerView = findViewById(R.id.scanner)
        mScannerView.setAutoFocus(true)
        mCodeContent = findViewById(R.id.code_content)
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        mCodeContent.text = "二维码内容 ：${rawResult.text}"
        Toast.makeText(this, "二维码内容 ：" + rawResult.text, Toast.LENGTH_LONG).show()
    }

}