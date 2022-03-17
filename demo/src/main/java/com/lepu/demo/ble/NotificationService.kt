package com.lepu.demo.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.Bp2BleRtData
import com.lepu.blepro.ble.data.Bp2DataBpIng
import com.lepu.blepro.event.InterfaceEvent

class NotificationService: LifecycleService() {

    private val tag = "NotificationService"
    private val mBinder = BleStateBinder()

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
        initLiveEvent()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    inner class BleStateBinder: Binder() {
        val service: NotificationService
        get() = this@NotificationService
    }

    private fun initLiveEvent() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData)
            .observe(this, {
                val data = it.data as Er1BleResponse.RtData
                if (data.param.hr in 1..59) {
                    ecgPsChange(data.param.hr)
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData)
            .observe(this, {
                val data = it.data as OxyBleResponse.RtParam
                if (data.spo2 in 1..98) {
                    ecgPsChange(data.spo2)
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData)
            .observe(this, {
                val data = it.data as Bp2BleRtData
                if (data.rtWave.waveDataType == 0) {
                    val da = Bp2DataBpIng(data.rtWave.waveData)
                    if (da.pressure > 5) {
                        ecgPsChange(da.pressure)
                    }
                }
            })
    }

    fun bleStateChange(state: Boolean) {
        Log.d("NotificationService", "bleStateChange == $state")
        bleStateNotification(state)
    }

    private fun ecgPsChange(ps: Int) {
        Log.d("NotificationService", "ecgPsChange == $ps")
        ecgPsNotification(ps)
    }

    private fun bleStateNotification(state: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NAME"
            val descriptionText = "DESCRIPTIONTEXT"
            // 提醒式通知(横幅显示)，不过大部分需要手动授权
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("ID", name, importance).apply {description = descriptionText}
            // 注册通道(频道)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(this)
                .setChannelId("ID")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle("蓝牙状态")
                .setContentText(""+state)
                .build()
            notificationManager.notify(2, notification)
        }
    }

    private fun ecgPsNotification(ps: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NAME"
            val descriptionText = "DESCRIPTIONTEXT"
            // 提醒式通知(横幅显示)，不过大部分需要手动授权
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("ID", name, importance).apply {description = descriptionText}
            // 注册通道(频道)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(this)
                .setChannelId("ID")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle("阈值警报")
                .setContentText(""+ps)
                .build()
            notificationManager.notify(1, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }
}