package com.lepu.demo

import android.app.Application
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.BleUtilService
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.ble.BleServiceObserverImpl

/**
 * author: wujuan
 * created on: 2021/1/19 17:09
 * description:
 */
class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        LepuBleLog.setDebug(true)
//
//        BleServiceHelper.BleServiceHelper.initService(this, BleServiceObserverImpl())
////                .setRawFolder()

        BleUtilService.initService(this, BleServiceObserverImpl())

    }

}