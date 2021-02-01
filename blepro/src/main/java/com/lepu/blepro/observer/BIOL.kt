package com.lepu.blepro.observer

import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/21 10:22
 * description: 自动管理蓝牙订阅, 使用之前应保证设备对应的interface已经初始化, 否则无效
 */
class BIOL(val observer: BleChangeObserver, private val model: IntArray): BleInterfaceLifecycle {

    /**
     *  当lifecycleOwner OnCreate时候调用
     */
    override fun subscribeBI() {
        LepuBleLog.d("BIOL 开始订阅蓝牙 $model")
        model.toList().forEach {
            BleServiceHelper.BleServiceHelper.subscribeBI(it, observer)
        }

    }

    /**
     *  当lifecycleOwner OnCreate时候调用
     */
    override fun detachBI() {
        model.toList().forEach {
            BleServiceHelper.BleServiceHelper.detachBI(it, observer)
        }

        LepuBleLog.d("BIOL 已取消订阅蓝牙 $model")
    }
}