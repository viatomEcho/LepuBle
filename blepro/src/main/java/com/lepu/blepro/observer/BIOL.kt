package com.lepu.blepro.observer

import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/21 10:22
 * description: 自动管理蓝牙订阅
 */
class BIOL(val observer: BleChangeObserver, private val model: IntArray): BleInterfaceLifecycle {

    /**
     *  当lifecycleOwner OnCreate时候调用
     *  如果对应model的Interface未初始化则初始化并自动订阅
     */
    override fun subscribeBI() {
        LepuBleLog.d("BIOL 开始订阅蓝牙 ${model.joinToString()}")
        for (m in model){
            BleServiceHelper.BleServiceHelper.subscribeBI(m, observer)
        }
    }

    /**
     *  当lifecycleOwner OnCreate时候调用
     *  初始化Interface并自动订阅
     */
    override fun detachBI() {
        for (m in model) {
            BleServiceHelper.BleServiceHelper.detachBI(m, observer)
        }
        LepuBleLog.d("BIOL 已取消订阅蓝牙 ${model.joinToString()}")
    }
}