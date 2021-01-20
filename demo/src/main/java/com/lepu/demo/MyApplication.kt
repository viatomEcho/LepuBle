package com.lepu.demo

import android.app.Application
import com.lepu.blepro.ble.BleServiceHelper
import com.lepu.blepro.utils.LogUtils
import com.lepu.demo.ble.BleServiceObserverImpl

/**
 * author: wujuan
 * created on: 2021/1/19 17:09
 * description:
 */
class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        LogUtils.setDebug(true)

        BleServiceHelper.BleServiceHelper.initRunVal("")

        BleServiceHelper.BleServiceHelper.initService(this, BleServiceObserverImpl())

    }

}