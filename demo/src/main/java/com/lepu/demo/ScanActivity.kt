package com.lepu.demo

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.lepu.blepro.ble.BleServiceHelper.Companion.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LogUtils
import com.lepu.demo.ble.DeviceHelper
import com.lepu.demo.ui.o2.ConnectO2Fragment
import com.lepu.demo.ui.o2.ConnectO2ViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ScanActivity : AppCompatActivity() {

    private lateinit var fragment: Fragment
    private var dialog: ProgressDialog? = null
    var currentModel: Int = Bluetooth.MODEL_O2RING

    var currentBleDevice: Bluetooth? = null


    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {

        dialog?.dismiss()
        Toast.makeText(this, "连接超时", Toast.LENGTH_SHORT).show()
        when (currentModel) {
            Bluetooth.MODEL_O2RING -> {
                currentBleDevice?.let {
                    DeviceHelper.connectO2(this, currentBleDevice!!)
                }
            }
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        initUI()
        EventBus.getDefault().register(this)
        if (savedInstanceState == null) {
            addO2Fragment()
        }

        BleServiceHelper.startDiscover()
    }




    private fun initUI() {
        when (currentModel) {
            Bluetooth.MODEL_O2RING -> {
                title = "O2Ring"
                addO2Fragment()
            }
        }

    }

    private fun addO2Fragment() {
        fragment = ConnectO2Fragment.newInstance(currentModel)
        supportFragmentManager.beginTransaction()
            .replace(R.id.connent_scan, fragment)
            .commitNow()

    }


    private fun finishBind() {
        LogUtils.d("finish bind")

        dialog?.dismiss()
        handler.removeCallbacks(runnable)
        finish()
    }


    private fun processBindDevice() {
        dialog = ProgressDialog(this)
        dialog?.setMessage("正在连接...")
        dialog?.setCancelable(false)
        dialog?.show()


        handler.postDelayed(runnable, 20000)
    }

    @Subscribe(threadMode = ThreadMode .MAIN)
    fun onBleProUIEvent(event: O2RingEvent) {
        LogUtils.d(event.action)
        when (event.action) {
            O2RingEvent.O2UIConnectingLoading -> { // 开始连接
                currentBleDevice = event.data as Bluetooth
                processBindDevice()

            }
            O2RingEvent.O2UIBindFinish -> { // 执行绑定完成
                finishBind()
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        handler.removeCallbacksAndMessages(null)
        BleServiceHelper.stopDiscover()
    }


}
