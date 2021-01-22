package com.lepu.demo.ui.scan

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.EventUI
import com.lepu.demo.R
import com.lepu.demo.ble.DeviceHelper
import com.lepu.demo.ui.o2.ConnectO2Fragment
import org.greenrobot.eventbus.EventBus


class ScanActivity : AppCompatActivity() {

    private val scanViewModel: ScanViewModel by viewModels()

    private lateinit var fragment: Fragment
    private var dialog: ProgressDialog? = null
    var curType: Int = 0 // 设备type 不同于model



    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {

        dialog?.dismiss()
        Toast.makeText(this, "连接超时", Toast.LENGTH_SHORT).show()
        DeviceHelper.connect(this, scanViewModel.device.value as Bluetooth )

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        setSupportActionBar(findViewById(R.id.toolbar))

        curType = intent.getIntExtra("curModel", Bluetooth.MODEL_O2RING)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }




        initUI()
        LiveEventBus.get(EventUI.ConnectingLoading)
                .observe(this, Observer {
                    if (it as Boolean) processBindDevice()
                })
        LiveEventBus.get(EventUI.BindFinish)
                .observe(this, Observer {
                    if (it as Boolean) finishBind()
                })


    }




    private fun initUI() {
        when (curType) {
            Bluetooth.MODEL_O2RING -> {
                title = "O2Ring"
                addO2Fragment(Bluetooth.MODEL_O2RING)
            }
        }

    }

    private fun addO2Fragment(model: Int) {
        fragment = ConnectO2Fragment.newInstance(model)
        supportFragmentManager.beginTransaction()
            .replace(R.id.connent_scan, fragment)
            .commitNow()


    }


    private fun finishBind() {
        LepuBleLog.d("finish bind")

        dialog?.dismiss()
        handler.removeCallbacks(runnable)
    }


    private fun processBindDevice() {
        dialog = ProgressDialog(this)
        dialog?.setMessage("正在绑定...")
        dialog?.setCancelable(false)
        dialog?.show()


        handler.postDelayed(runnable, 20000)
    }



    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        BleServiceHelper.stopScan()
    }


}
