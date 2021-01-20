package com.lepu.blepro.observer.O2

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lepu.blepro.ble.BleServiceHelper

/**
 * author: wujuan
 * created on: 2020/12/18 9:26
 * description: 监听OxyBleInterface的订阅者的生命周期
 */
interface O2BleLifecycle: LifecycleObserver{


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy()


}