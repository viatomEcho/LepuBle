package com.lepu.demo

import android.Manifest
import android.app.Application
import android.util.Log
import android.util.SparseArray
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.ble.BleSO
import com.lepu.demo.ble.BleServiceObserverImpl
import com.lepu.demo.ble.LpBleUtil
import com.permissionx.guolindev.PermissionX

/**
 * author: wujuan
 * created on: 2021/1/19 17:09
 * description:
 */


class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()




        LepuBleLog.setDebug(true)






    }



}