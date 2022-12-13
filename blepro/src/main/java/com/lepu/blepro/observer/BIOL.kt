package com.lepu.blepro.observer

import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/21 10:22
 * description: 自动管理蓝牙订阅, 使用之前应保证设备对应的interface已经初始化, 否则无效
 */
class BIOL(val observer: BleChangeObserver, private var model: IntArray): BleInterfaceLifecycle {

    private val tag = "BIOL"
    /**
     *  当lifecycleOwner OnCreate时候调用
     */
    override fun subscribeBI() {
        LepuBleLog.d(tag, "subscribeBI, 开始订阅蓝牙 model:${model.joinToString()}")
        model.let {
            for (m in model) BleServiceHelper.BleServiceHelper.subscribeBI(m, observer)
        }

    }

    /**
     *  当lifecycleOwner OnDestroy时候调用
     */
    override fun detachBI() {

        model.let {
            for (m in model) BleServiceHelper.BleServiceHelper.detachBI(m, observer)
        }
        LepuBleLog.d(tag, "detachBI, 已取消订阅蓝牙 model:${model.joinToString()}")
    }

    fun update(newModels: IntArray){
        model = newModels
        subscribeBI()
    }

}