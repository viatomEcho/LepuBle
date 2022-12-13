package com.lepu.blepro.observer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * author: wujuan
 * created on: 2020/12/18 9:26
 * description: 观察BleInterface的订阅者的生命周期
 */
internal interface BleInterfaceLifecycle: LifecycleObserver{

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun subscribeBI()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detachBI()
}