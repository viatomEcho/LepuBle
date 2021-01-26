package com.lepu.blepro.observer

import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/21 10:22
 * description: 自动管理蓝牙订阅, 使用之前应保证设备对应的interface已经初始化, 否则无效
 */
class BIOL(val observer: BleChangeObserver, private val model: Int): BleInterfaceLifecycle {

    /**
     *  当lifecycleOwner OnCreate时候调用
     *  如果对应model的Interface未初始化则初始化并自动订阅
     */
    override fun subscribeBI() {
        LepuBleLog.d("BIOL 开始订阅蓝牙 $model")
        BleServiceHelper.BleServiceHelper.subscribeBI(model, observer)
    }

    /**
     *  当lifecycleOwner OnCreate时候调用
     *  初始化Interface并自动订阅
     */
    override fun detachBI() {
        BleServiceHelper.BleServiceHelper.detachBI(model, observer)
        LepuBleLog.d("BIOL 已取消订阅蓝牙 $model")
    }
}