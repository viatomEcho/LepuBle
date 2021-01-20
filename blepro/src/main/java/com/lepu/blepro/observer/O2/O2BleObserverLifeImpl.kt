package com.lepu.blepro.observer.O2

import com.lepu.blepro.ble.BleServiceHelper
import com.lepu.blepro.observer.O2.O2BleLifecycle
import com.lepu.blepro.observer.O2.O2BleObserver
import com.lepu.blepro.utils.LogUtils

/**
 * author: wujuan
 * created on: 2020/12/18 10:07
 * description: 提供给外部的，用于监听蓝牙改变订阅者的生命周期管理, 在不同生命周期时自动注册、注销
 *              使用：在需要订阅通知的LifecycleOwner onCreate(){getLifecycle().addObserver(new O2BleObserverLifeImpl(this));}
 *
 */
class O2BleObserverLifeImpl(private val observer: O2BleObserver): O2BleLifecycle {

    override fun onCreate() {
        LogUtils.d("onLifeCreate 开始订阅OxyBle")
        BleServiceHelper.BleServiceHelper.subscribeO2Ble(observer)
    }

    override fun onDestroy() {
        LogUtils.d("onLifeDestroy 取消订阅OxyBle")
        BleServiceHelper.BleServiceHelper.detachO2Ble(observer)
    }
}